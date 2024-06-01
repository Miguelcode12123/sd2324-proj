package tukano.impl.api.java;

import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.ShortsRep;

public interface ExtendedShortsRep extends ShortsRep {

    Result<Void> deleteAllShorts(Long version, String userId, String password, String token);

}
