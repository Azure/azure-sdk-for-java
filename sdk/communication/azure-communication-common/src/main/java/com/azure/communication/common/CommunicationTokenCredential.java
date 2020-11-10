// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import java.util.concurrent.ExecutionException;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import reactor.core.publisher.Mono;

/**
 * This class serves as a CommunicationUserCredential wrapper that 
 * allows using BearerAuthenticationPolicy in different clients
 */
public class CommunicationTokenCredential implements TokenCredential {
    private final CommunicationUserCredential credential;

    /**
     * Creates a CommunicationTokenCredential
     *
     * @param communicationUserCredential The {@link CommunicationUserCredential} to use 
     * in the BearerAuthenticationPolicy.
     */
    public CommunicationTokenCredential(CommunicationUserCredential communicationUserCredential) {
        credential = communicationUserCredential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        try {
            return Mono.just(credential.getToken().get());
        } catch (InterruptedException ex) {
            return Mono.error(ex);
        } catch (ExecutionException ex) {
            return Mono.error(ex);
        }
    }
}
