package com.azure.spring.aad.webapp;

import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

public class RefreshTokenGrantRequestEntityConverter extends OAuth2RefreshTokenGrantRequestEntityConverter {

    private final List<ClientRegistration> otherClients;

    public RefreshTokenGrantRequestEntityConverter(List<ClientRegistration> otherClients) {
        this.otherClients = otherClients;
    }

    public RequestEntity<?> convert(OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest) {
        RequestEntity<?> result = super.convert(refreshTokenGrantRequest);
        if (isRequestForDefaultClient(refreshTokenGrantRequest)) {
            Optional.ofNullable(result)
                .map(HttpEntity::getBody)
                .map(b -> (MultiValueMap<String, String>) b)
                .ifPresent(body -> body.add("scope", String.join(" " , refreshTokenGrantRequest.getClientRegistration().getScopes())));
        }
        return result;
    }

    private boolean isRequestForDefaultClient(OAuth2RefreshTokenGrantRequest request) {

        for(ClientRegistration c : otherClients){
            if(request.getClientRegistration().getClientName().equals(c.getClientName())){
                return true;
            }
        }
        return false;
    }


}
