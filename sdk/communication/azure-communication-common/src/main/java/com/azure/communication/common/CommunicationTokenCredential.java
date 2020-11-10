// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import java.util.concurrent.ExecutionException;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import reactor.core.publisher.Mono;

public class CommunicationTokenCredential implements TokenCredential {
    private CommunicationUserCredential credential;

    public CommunicationTokenCredential(CommunicationUserCredential communicationUserCrendential){
        credential = communicationUserCrendential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request){
        try{
            return Mono.just(credential.getToken().get());
        }
        catch (InterruptedException ex) {
            return Mono.error(ex);
        } catch (ExecutionException ex) {
            return Mono.error(ex);
        }
    }
}
