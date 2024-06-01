package tukano.impl.api.rest;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import tukano.api.rest.RestShortsRep;

@Path(RestShortsRep.PATH)
public interface RestExtendedShortsRep extends RestShortsRep {

    String TOKEN = "token";

    @DELETE
    @Path("/{" + USER_ID + "}" + SHORTS)
    void deleteAllShorts(@HeaderParam(RestShortsRep.HEADER_VERSION) Long version, @PathParam(USER_ID) String userId,
            @QueryParam(PWD) String password,
            @QueryParam(TOKEN) String token);

}
