package tukano.impl.java.servers.rest.servers;

import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tukano.api.java.ShortsRep;
import tukano.impl.java.servers.rest.servers.utils.CustomLoggingFilter;
import tukano.impl.java.servers.rest.servers.utils.GenericExceptionMapper;
import utils.Args;

public class RestShortsRepServer extends AbstractRestServer {
    public static final int PORT = 4567;

    private static Logger Log = Logger.getLogger(RestShortsRepServer.class.getName());

    RestShortsRepServer() {
        super(Log, ShortsRep.NAME, PORT);
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.registerInstances(RestShortsRepResource.class);
        config.register(new GenericExceptionMapper());
        config.register(new CustomLoggingFilter());
    }

    public static void main(String[] args) {
        Args.use(args);
        new RestShortsRepServer().start();
    }
}