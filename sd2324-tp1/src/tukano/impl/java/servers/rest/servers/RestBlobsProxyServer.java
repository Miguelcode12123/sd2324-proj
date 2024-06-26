package tukano.impl.java.servers.rest.servers;

import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tukano.api.java.Blobs;
import tukano.impl.java.servers.rest.servers.utils.CustomLoggingFilter;
import tukano.impl.java.servers.rest.servers.utils.GenericExceptionMapper;
import utils.Args;

public class RestBlobsProxyServer extends AbstractRestServer {
    public static final int PORT = 8896;

    private static Logger Log = Logger.getLogger(RestBlobsProxyServer.class.getName());

    RestBlobsProxyServer(int port) {
        super(Log, Blobs.NAME, port);
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.register(new RestBlobsProxyResource());
        config.register(new GenericExceptionMapper());
        config.register(new CustomLoggingFilter());
    }

    public static void main(String[] args) {
        Args.use(args);
        new RestBlobsProxyServer(Args.valueOf("-port", PORT)).start();
    }
}