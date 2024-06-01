package tukano.impl.java.servers.rest.clients;

import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.rest.RestShortsRep;
import tukano.impl.api.java.ExtendedShortsRep;
import tukano.impl.api.rest.RestExtendedShorts;

public class RestShortsClientRep extends RestClient implements ExtendedShortsRep {

    public RestShortsClientRep(String serverURI) {
        super(serverURI, RestShortsRep.PATH);
    }

    public Result<Short> _createShort(Long version, String userId, String password) {
        return super.toJavaResult(
                target
                        .path(userId)
                        .queryParam(RestShortsRep.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.json(null)),
                Short.class);
    }

    public Result<Void> _deleteShort(Long version, String shortId, String password) {
        return super.toJavaResult(
                target
                        .path(shortId)
                        .queryParam(RestShortsRep.PWD, password)
                        .request()
                        .delete());
    }

    public Result<Short> _getShort(Long version, String shortId) {
        return super.toJavaResult(
                target
                        .path(shortId)
                        .request()
                        .get(),
                Short.class);
    }

    public Result<List<String>> _getShorts(Long version, String userId) {
        return super.toJavaResult(
                target
                        .path(userId)
                        .path(RestShortsRep.SHORTS)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                new GenericType<List<String>>() {
                });
    }

    public Result<Void> _follow(Long version, String userId1, String userId2, boolean isFollowing, String password) {
        return super.toJavaResult(
                target
                        .path(userId1)
                        .path(userId2)
                        .path(RestShortsRep.FOLLOWERS)
                        .queryParam(RestShortsRep.PWD, password)
                        .request()
                        .post(Entity.entity(isFollowing, MediaType.APPLICATION_JSON)));
    }

    public Result<List<String>> _followers(Long version, String userId, String password) {
        return super.toJavaResult(
                target
                        .path(userId)
                        .path(RestShortsRep.FOLLOWERS)
                        .queryParam(RestShortsRep.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                new GenericType<List<String>>() {
                });
    }

    public Result<Void> _like(Long version, String shortId, String userId, boolean isLiked, String password) {
        return super.toJavaResult(
                target
                        .path(shortId)
                        .path(userId)
                        .path(RestShortsRep.LIKES)
                        .queryParam(RestShortsRep.PWD, password)
                        .request()
                        .post(Entity.entity(isLiked, MediaType.APPLICATION_JSON)));
    }

    public Result<List<String>> _likes(Long version, String shortId, String password) {
        return super.toJavaResult(
                target
                        .path(shortId)
                        .path(RestShortsRep.LIKES)
                        .queryParam(RestShortsRep.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                new GenericType<List<String>>() {
                });
    }

    public Result<List<String>> _getFeed(Long version, String userId, String password) {
        return super.toJavaResult(
                target
                        .path(userId)
                        .path(RestShortsRep.FEED)
                        .queryParam(RestShortsRep.PWD, password)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(),
                new GenericType<List<String>>() {
                });
    }

    public Result<Void> _deleteAllShorts(Long version, String userId, String password, String token) {
        return super.toJavaResult(
                target
                        .path(userId)
                        .path(RestShortsRep.SHORTS)
                        .queryParam(RestExtendedShorts.PWD, password)
                        .queryParam(RestExtendedShorts.TOKEN, token)
                        .request()
                        .delete());
    }

    public Result<Void> _verifyBlobURI(Long version, String blobId) {
        return super.toJavaResult(
                target
                        .path(blobId)
                        .request()
                        .get());
    }

    @Override
    public Result<Short> createShort(Long version, String userId, String password) {
        return super.reTry(() -> _createShort(version, userId, password));
    }

    @Override
    public Result<Void> deleteShort(Long version, String shortId, String password) {
        return super.reTry(() -> _deleteShort(version, shortId, password));
    }

    @Override
    public Result<Short> getShort(Long version, String shortId) {
        return super.reTry(() -> _getShort(version, shortId));
    }

    @Override
    public Result<List<String>> getShorts(Long version, String userId) {
        return super.reTry(() -> _getShorts(version, userId));
    }

    @Override
    public Result<Void> follow(Long version, String userId1, String userId2, boolean isFollowing, String password) {
        return super.reTry(() -> _follow(version, userId1, userId2, isFollowing, password));
    }

    @Override
    public Result<List<String>> followers(Long version, String userId, String password) {
        return super.reTry(() -> _followers(version, userId, password));
    }

    @Override
    public Result<Void> like(Long version, String shortId, String userId, boolean isLiked, String password) {
        return super.reTry(() -> _like(version, shortId, userId, isLiked, password));
    }

    @Override
    public Result<List<String>> likes(Long version, String shortId, String password) {
        return super.reTry(() -> _likes(version, shortId, password));
    }

    @Override
    public Result<List<String>> getFeed(Long version, String userId, String password) {
        return super.reTry(() -> _getFeed(version, userId, password));
    }

    @Override
    public Result<Void> deleteAllShorts(Long version, String userId, String password, String token) {
        return super.reTry(() -> _deleteAllShorts(version, userId, password, token));
    }
}
