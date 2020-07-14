package com.azure.messaging.eventgrid;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

public class EventGridSharedKeyCredential implements TokenCredential {

    /**
     * Creates an instance of the EventGridSharedKeyCredential using the given policy name
     * and its shared access key.
     * @param policyName the policy name, or the name associated with the key.
     * @param accessKey  the access key for the topic or domain to send to.
     */
    public EventGridSharedKeyCredential(String policyName, String accessKey) {
        // TODO: implement method
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        // TODO: implement method
        return null;
    }
}
