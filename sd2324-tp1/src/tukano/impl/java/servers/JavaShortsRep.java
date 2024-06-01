package tukano.impl.java.servers;

import static java.lang.String.format;
import static tukano.api.java.Result.error;
import static tukano.api.java.Result.errorOrResult;
import static tukano.api.java.Result.errorOrValue;
import static tukano.api.java.Result.errorOrVoid;
import static tukano.api.java.Result.ok;
import static tukano.api.java.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.java.Result.ErrorCode.FORBIDDEN;
import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static tukano.api.java.Result.ErrorCode.TIMEOUT;
import static tukano.impl.java.clients.Clients.BlobsClients;
import static tukano.impl.java.clients.Clients.UsersClients;
import static utils.DB.getOne;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedShorts;
import tukano.impl.api.java.ExtendedShortsRep;
import tukano.impl.java.servers.data.Following;
import tukano.impl.java.servers.data.Likes;
import tukano.impl.kafka.KafkaPublisher;
import tukano.impl.kafka.KafkaSubscriber;
import tukano.impl.kafka.RecordProcessor;
import tukano.impl.kafka.sync.SyncPoint;
import utils.DB;
import utils.Token;

/**
 * TODO: The onRecieve helper methods could be optimized, by sending the objects
 * already declared
 */
public class JavaShortsRep extends Thread implements RecordProcessor, ExtendedShortsRep {
    private static final String BLOB_COUNT = "*";

    private static Logger Log = Logger.getLogger(JavaShortsRep.class.getName());

    AtomicLong counter = new AtomicLong(totalShortsInDatabase());

    private static final Gson GSON = new Gson();

    final KafkaPublisher sender;
    final KafkaSubscriber receiver;
    final SyncPoint<String> sync;

    private static final long USER_CACHE_EXPIRATION = 3000;
    private static final long SHORTS_CACHE_EXPIRATION = 3000;
    private static final long BLOBS_USAGE_CACHE_EXPIRATION = 10000;
    static final String FROM_BEGINNING = "earliest";
    static final String TOPIC = "single_partition_topic";
    static final String KAFKA_BROKERS = "kafka:9092";
    static final String CREATE_SHORT = "createShort";
    static final String DELETE_SHORT = "deleteShort";
    static final String GET_SHORT = "getShort";
    static final String GET_SHORTS = "getShorts";
    static final String FOLLOW = "follow";
    static final String FOLLOWERS = "follower";
    static final String LIKE = "like";
    static final String LIKES = "likes";
    static final String GET_FEED = "getFeed";
    static final String DELETE_ALL_SHORTS = "deleteAllShorts";

    public JavaShortsRep() {
        this.sender = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        this.receiver = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
        receiver.start(false, this);
        this.sync = SyncPoint.getInstance();

    }

    static record Credentials(String userId, String pwd) {
        static Credentials from(String userId, String pwd) {
            return new Credentials(userId, pwd);
        }
    }

    protected final LoadingCache<Credentials, Result<User>> usersCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(USER_CACHE_EXPIRATION)).removalListener((e) -> {
            }).build(new CacheLoader<>() {
                @Override
                public Result<User> load(Credentials u) throws Exception {
                    var res = UsersClients.get().getUser(u.userId(), u.pwd());
                    if (res.error() == TIMEOUT)
                        return error(BAD_REQUEST);
                    return res;
                }
            });

    protected final LoadingCache<String, Result<Short>> shortsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(SHORTS_CACHE_EXPIRATION)).removalListener((e) -> {
            }).build(new CacheLoader<>() {
                @Override
                public Result<Short> load(String shortId) throws Exception {

                    var query = format("SELECT count(*) FROM Likes l WHERE l.shortId = '%s'", shortId);
                    var likes = DB.sql(query, Long.class);
                    return errorOrValue(getOne(shortId, Short.class), shrt -> shrt.copyWith(likes.get(0)));
                }
            });

    protected final LoadingCache<String, Map<String, Long>> blobCountCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(BLOBS_USAGE_CACHE_EXPIRATION)).removalListener((e) -> {
            }).build(new CacheLoader<>() {
                @Override
                public Map<String, Long> load(String __) throws Exception {
                    final var QUERY = "SELECT REGEXP_SUBSTRING(s.blobUrl, '^(\\w+:\\/\\/)?([^\\/]+)\\/([^\\/]+)') AS baseURI, count('*') AS usage From Short s GROUP BY baseURI";
                    var hits = DB.sql(QUERY, BlobServerCount.class);

                    var candidates = hits.stream()
                            .collect(Collectors.toMap(BlobServerCount::baseURI, BlobServerCount::count));

                    for (var uri : BlobsClients.all())
                        candidates.putIfAbsent(uri.toString(), 0L);

                    return candidates;

                }
            });

    private long version;

    private List<Object> getArgs(Object... args) {
        List<Object> argsList = Arrays.asList(args);
        return argsList;
    }

    @Override
    public Result<Short> createShort(Long version, String userId, String password) {
        Log.info(() -> format("createShort : userId = %s, pwd = %s\n", userId, password));

        return errorOrResult(okUser(userId, password), user -> {

            var shortId = format("%s-%d", userId, counter.incrementAndGet());
            var blobUrl = format("%s/%s/%s", getLeastLoadedBlobServerURI(), Blobs.NAME, shortId);
            var shrt = new Short(shortId, userId, blobUrl);

            // makes sure that the operation is executed, only when the version value is
            // equal to the servers counter
            if (version != null) {
                sync.waitForResult(version);
            }

            var args = getArgs(shortId, GSON.toJson(shrt));
            sender.publish(TOPIC, CREATE_SHORT, GSON.toJson(args));

            return DB.insertOne(shrt);
        });
    }

    @Override
    public Result<Short> getShort(Long version, String shortId) {
        Log.info(() -> format("getShort : shortId = %s\n", shortId));

        if (shortId == null)
            return error(BAD_REQUEST);

        if (version != null) {
            sync.waitForResult(version);
        }

        return shortFromCache(shortId);
    }

    @Override
    public Result<Void> deleteShort(Long version, String shortId, String password) {
        Log.info(() -> format("deleteShort : shortId = %s, pwd = %s\n", shortId, password));

        return errorOrResult(getShort(version, shortId), shrt -> {

            return errorOrResult(okUser(shrt.getOwnerId(), password), user -> {
                return DB.transaction(hibernate -> {

                    shortsCache.invalidate(shortId);
                    hibernate.remove(shrt);

                    var query = format("SELECT * FROM Likes l WHERE l.shortId = '%s'", shortId);
                    hibernate.createNativeQuery(query, Likes.class).list().forEach(hibernate::remove);

                    BlobsClients.get().delete(shrt.getBlobUrl(), Token.get());

                    if (version != null) {
                        sync.waitForResult(version);
                    }

                    var args = getArgs(shortId);
                    sender.publish(TOPIC, DELETE_SHORT, GSON.toJson(args));
                });
            });
        });
    }

    @Override
    public Result<List<String>> getShorts(Long version, String userId) {
        Log.info(() -> format("getShorts : userId = %s\n", userId));

        var query = format("SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'", userId);

        if (version != null) {
            sync.waitForResult(version);
        }

        return errorOrValue(okUser(userId), DB.sql(query, String.class));
    }

    @Override
    public Result<Void> follow(Long version, String userId1, String userId2, boolean isFollowing, String password) {
        Log.info(() -> format("follow : userId1 = %s, userId2 = %s, isFollowing = %s, pwd = %s\n", userId1, userId2,
                isFollowing, password));

        return errorOrResult(okUser(userId1, password), user -> {
            var f = new Following(userId1, userId2);
            if (version != null) {
                sync.waitForResult(version);
            }
            var args = getArgs(userId1, userId2, isFollowing);
            sender.publish(TOPIC, FOLLOW, GSON.toJson(args));
            return errorOrVoid(okUser(userId2), isFollowing ? DB.insertOne(f) : DB.deleteOne(f));

        });
    }

    @Override
    public Result<List<String>> followers(Long version, String userId, String password) {
        Log.info(() -> format("followers : userId = %s, pwd = %s\n", userId, password));

        var query = format("SELECT f.follower FROM Following f WHERE f.followee = '%s'", userId);
        if (version != null) {
            sync.waitForResult(version);
        }
        return errorOrValue(okUser(userId, password), DB.sql(query, String.class));
    }

    @Override
    public Result<Void> like(Long version, String shortId, String userId, boolean isLiked, String password) {
        Log.info(() -> format("like : shortId = %s, userId = %s, isLiked = %s, pwd = %s\n", shortId, userId, isLiked,
                password));

        return errorOrResult(getShort(version, shortId), shrt -> {
            shortsCache.invalidate(shortId);

            var l = new Likes(userId, shortId, shrt.getOwnerId());

            if (version != null) {
                sync.waitForResult(version);
            }

            var args = getArgs(shortId, userId, isLiked);
            sender.publish(TOPIC, LIKE, GSON.toJson(args));

            return errorOrVoid(okUser(userId, password), isLiked ? DB.insertOne(l) : DB.deleteOne(l));
        });
    }

    @Override
    public Result<List<String>> likes(Long version, String shortId, String password) {
        Log.info(() -> format("likes : shortId = %s, pwd = %s\n", shortId, password));

        return errorOrResult(getShort(version, shortId), shrt -> {

            var query = format("SELECT l.userId FROM Likes l WHERE l.shortId = '%s'", shortId);

            if (version != null) {
                sync.waitForResult(version);
            }

            return errorOrValue(okUser(shrt.getOwnerId(), password), DB.sql(query, String.class));
        });
    }

    @Override
    public Result<List<String>> getFeed(Long version, String userId, String password) {
        Log.info(() -> format("getFeed : userId = %s, pwd = %s\n", userId, password));

        final var QUERY_FMT = """
                SELECT s.shortId, s.timestamp FROM Short s WHERE	s.ownerId = '%s'
                UNION
                SELECT s.shortId, s.timestamp FROM Short s, Following f
                	WHERE
                		f.followee = s.ownerId AND f.follower = '%s'
                ORDER BY s.timestamp DESC""";

        if (version != null) {
            sync.waitForResult(version);
        }

        return errorOrValue(okUser(userId, password), DB.sql(format(QUERY_FMT, userId, userId), String.class));
    }

    protected Result<User> okUser(String userId, String pwd) {
        try {
            return usersCache.get(new Credentials(userId, pwd));
        } catch (Exception x) {
            x.printStackTrace();
            return Result.error(INTERNAL_ERROR);
        }
    }

    private Result<Void> okUser(String userId) {
        var res = okUser(userId, "");
        if (res.error() == FORBIDDEN)
            return ok();
        else
            return error(res.error());
    }

    protected Result<Short> shortFromCache(String shortId) {
        try {
            return shortsCache.get(shortId);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return error(INTERNAL_ERROR);
        }
    }

    // Extended API

    @Override
    public Result<Void> deleteAllShorts(Long version, String userId, String password, String token) {
        Log.info(() -> format("deleteAllShorts : userId = %s, password = %s, token = %s\n", userId, password, token));

        if (!Token.matches(token))
            return error(FORBIDDEN);

        return DB.transaction((hibernate) -> {

            usersCache.invalidate(new Credentials(userId, password));

            // delete shorts
            var query1 = format("SELECT * FROM Short s WHERE s.ownerId = '%s'", userId);
            hibernate.createNativeQuery(query1, Short.class).list().forEach(s -> {
                shortsCache.invalidate(s.getShortId());
                hibernate.remove(s);
            });

            // delete follows
            var query2 = format("SELECT * FROM Following f WHERE f.follower = '%s' OR f.followee = '%s'", userId,
                    userId);
            hibernate.createNativeQuery(query2, Following.class).list().forEach(hibernate::remove);

            // delete likes
            var query3 = format("SELECT * FROM Likes l WHERE l.ownerId = '%s' OR l.userId = '%s'", userId, userId);
            hibernate.createNativeQuery(query3, Likes.class).list().forEach(l -> {
                shortsCache.invalidate(l.getShortId());
                hibernate.remove(l);

                if (version != null) {
                    sync.waitForResult(version);
                }

                var args = getArgs(userId, password);
                sender.publish(TOPIC, DELETE_ALL_SHORTS, GSON.toJson(args));
            });
        });
    }

    private String getLeastLoadedBlobServerURI() {
        try {
            var servers = blobCountCache.get(BLOB_COUNT);

            var leastLoadedServer = servers.entrySet()
                    .stream()
                    .sorted((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
                    .findFirst();

            if (leastLoadedServer.isPresent()) {
                var uri = leastLoadedServer.get().getKey();
                servers.compute(uri, (k, v) -> v + 1L);
                return uri;
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return "?";
    }

    static record BlobServerCount(String baseURI, Long count) {
    };

    private long totalShortsInDatabase() {
        var hits = DB.sql("SELECT count('*') FROM Short", Long.class);
        return 1L + (hits.isEmpty() ? 0L : hits.get(0));
    }

    public long getVersion() {
        return this.version;
    }

    @Override
    public void onReceive(ConsumerRecord<String, String> r) {
        version = r.offset();
        var result = r.value().toString();
        Type listType = new TypeToken<List<Object>>() {
        }.getType();
        List<Object> args = new Gson().fromJson(r.value(), listType);
        switch (r.key()) {
            case CREATE_SHORT:
                _createShort(
                        GSON.fromJson((String) args.get(0), Short.class));
                break;
            case DELETE_SHORT:
                _deleteShort((String) args.get(0));
                break;
            case FOLLOW:
                _follow((String) args.get(0), (String) args.get(1), (boolean) args.get(2));
                break;
            case LIKE:
                _like((String) args.get(0), (String) args.get(1), (boolean) args.get(2));
                break;
            case DELETE_ALL_SHORTS:
                _deleteAllShorts((String) args.get(0), (String) args.get(1));
                break;
        }
        sync.setResult(version, result);
    }

    private void _deleteAllShorts(String userId, String password) {
        DB.transaction((hibernate) -> {

            usersCache.invalidate(new Credentials(userId, password));

            // delete shorts
            var query1 = format("SELECT * FROM Short s WHERE s.ownerId = '%s'", userId);
            hibernate.createNativeQuery(query1, Short.class).list().forEach(s -> {
                shortsCache.invalidate(s.getShortId());
                hibernate.remove(s);
            });

            // delete follows
            var query2 = format("SELECT * FROM Following f WHERE f.follower = '%s' OR f.followee = '%s'", userId,
                    userId);
            hibernate.createNativeQuery(query2, Following.class).list().forEach(hibernate::remove);

            // delete likes
            var query3 = format("SELECT * FROM Likes l WHERE l.ownerId = '%s' OR l.userId = '%s'", userId, userId);
            hibernate.createNativeQuery(query3, Likes.class).list().forEach(l -> {
                shortsCache.invalidate(l.getShortId());
                hibernate.remove(l);
            });
        });
    }

    private void _like(String shortId, String userId, boolean isLiked) {
        Short shrt = getShort(version, shortId).value();
        shortsCache.invalidate(shortId);
        var l = new Likes(userId, shortId, shrt.getOwnerId());
        errorOrVoid(okUser(userId), isLiked ? DB.insertOne(l) : DB.deleteOne(l));
    }

    private void _follow(String userId1, String userId2, boolean isFollowing) {
        var f = new Following(userId1, userId2);
        errorOrVoid(okUser(userId2), isFollowing ? DB.insertOne(f) : DB.deleteOne(f));
    }

    private void _deleteShort(String shortId) {
        Short shrt = getShort(this.getVersion(), shortId).value();
        DB.transaction(hibernate -> {

            shortsCache.invalidate(shortId);
            hibernate.remove(shrt);

            var query = format("SELECT * FROM Likes l WHERE l.shortId = '%s'", shortId);
            hibernate.createNativeQuery(query, Likes.class).list().forEach(hibernate::remove);

            BlobsClients.get().delete(shrt.getBlobUrl(), Token.get());
        });

    }

    private void _createShort(Short shrt) {
        DB.insertOne(shrt);
    }
}
