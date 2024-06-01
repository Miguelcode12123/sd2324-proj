package tukano.impl.java.servers.rest.servers;

import static tukano.impl.java.servers.rest.servers.RestResource.statusCodeFrom;

import java.util.List;

import jakarta.inject.Singleton;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import tukano.api.Short;
import tukano.api.rest.RestShorts;
import tukano.impl.api.java.ExtendedShorts;
import tukano.impl.api.java.ExtendedShortsRep;
import tukano.impl.api.rest.RestExtendedShorts;
import tukano.impl.api.rest.RestExtendedShortsRep;
import tukano.impl.java.servers.JavaShorts;
import tukano.impl.java.servers.JavaShortsRep;
import tukano.api.java.Result;

@Singleton
@Provider
public class RestShortsRepResource extends RestResource implements RestExtendedShortsRep {

    final JavaShortsRep impl;

    public RestShortsRepResource() {
        this.impl = new JavaShortsRep();
    }

    @Override
    public Short createShort(Long version, String userId, String password) {
        Short result = super.resultOrThrow(impl.createShort(version, userId, password));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .encoding(MediaType.APPLICATION_JSON)
                        .entity(result)
                        .build());
    }

    @Override
    public void deleteShort(Long version, String shortId, String password) {
        super.resultOrThrow(impl.deleteShort(version, shortId, password));
        throw new WebApplicationException(
                Response.status(204).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .build());
    }

    @Override
    public Short getShort(Long version, String shortId) {
        Short result = this.resultOrThrow(impl.getShort(version, shortId));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .encoding(MediaType.APPLICATION_JSON)
                        .entity(result)
                        .build());
    }

    @Override
    public List<String> getShorts(Long version, String userId) {
        List<String> result = super.resultOrThrow(impl.getShorts(version, userId));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .encoding(MediaType.APPLICATION_JSON)
                        .entity(result)
                        .build());
    }

    @Override
    public void follow(Long version, String userId1, String userId2, boolean isFollowing, String password) {
        super.resultOrThrow(impl.follow(version, userId1, userId2, isFollowing, password));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .build());
    }

    @Override
    public List<String> followers(Long version, String userId,
            String password) {
        List<String> result = super.resultOrThrow(impl.followers(version, userId, password));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .encoding(MediaType.APPLICATION_JSON)
                        .entity(result)
                        .build());
    }

    @Override
    public void like(Long version, String shortId, String userId, boolean isLiked, String password) {
        super.resultOrThrow(impl.like(version, shortId, userId, isLiked, password));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .build());
    }

    @Override
    public List<String> likes(Long version, String shortId, String password) {
        List<String> result = super.resultOrThrow(impl.likes(version, shortId, password));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .encoding(MediaType.APPLICATION_JSON)
                        .entity(result)
                        .build());
    }

    @Override
    public List<String> getFeed(Long version, String userId, String password) {
        List<String> result = super.resultOrThrow(impl.getFeed(version, userId, password));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .encoding(MediaType.APPLICATION_JSON)
                        .entity(result)
                        .build());
    }

    @Override
    public void deleteAllShorts(Long version, String userId, String password, String token) {
        super.resultOrThrow(impl.deleteAllShorts(version, userId, password, token));
        throw new WebApplicationException(
                Response.status(200).header(RestExtendedShortsRep.HEADER_VERSION, impl.getVersion())
                        .build());
    }

}
