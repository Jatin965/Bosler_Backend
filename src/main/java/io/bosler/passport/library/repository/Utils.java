package io.bosler.passport.library.repository;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.bosler.passport.library.models.Users;

import java.io.IOException;
import java.util.HashMap;

public class Utils {

//    public Users getUserInfoFromToken(String token) {
          // to do
//        String refresh_token = authorizationHeader.substring("Bearer ".length());
//        Algorithm algorithm = Algorithm.HMAC256(boslerSecret.getBytes());
//        JWTVerifier verifier = JWT.require(algorithm).build();
//        DecodedJWT decodedJWT = verifier.verify(refresh_token);
//        String username = decodedJWT.getSubject();



//    }

//    public void serializeCustomerAttributes() throws JsonProcessingException {
//        this.customerAttributeJSON = objectMapper.writeValueAsString(customerAttributes);
//    }
//
//    public void deserializeCustomerAttributes() throws IOException {
//        this.customerAttributes = objectMapper.readValue(customerAttributeJSON, HashMap.class);
//    }
}
