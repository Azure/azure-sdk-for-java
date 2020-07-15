package com.azure.messaging.eventgrid;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

/**
 * A way to use a generated access token as a credential to publish events to a topic through a client.
 * The recommended way to authenticate event publishing is through {@link EventGridSharedKeyCredential}.
 */
public class EventGridTokenCredential implements TokenCredential {

    /**
     * Creates an instance of this object to authenticate calls to the EventGrid service.
     * @param tokenCredential the access token to use.
     */
    public EventGridTokenCredential(String tokenCredential) {
        // TODO: implement method
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return null;
    }
}
