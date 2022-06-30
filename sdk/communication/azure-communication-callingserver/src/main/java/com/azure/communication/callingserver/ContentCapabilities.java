// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.PlayResponse;
import com.azure.communication.callingserver.implementation.models.PlaySource;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.Collections;
import java.util.List;

public class ContentCapabilities {
    private final ContentCapabilitiesAsync contentCapabilitiesAsync;

    ContentCapabilities(ContentCapabilitiesAsync contentCapabilitiesAsync) {
        this.contentCapabilitiesAsync = contentCapabilitiesAsync;
    }

    public PlayResponse Play(PlaySource playSource, List<CommunicationIdentifier> playTo) {
        return contentCapabilitiesAsync.PlayAsync(playSource, playTo).block();
    }

    public PlayResponse PlayAll(PlaySource playSource) {
        return contentCapabilitiesAsync.PlayAllAsync(playSource).block();
    }

    public Response<PlayResponse> PlayWithResponse(
        PlaySource playSource,
        List<CommunicationIdentifier> playTo,
        Context context) {
        return contentCapabilitiesAsync.playAsyncWithResponseInternal(playSource, playTo, context).block();
    }

    public Response<PlayResponse> PlayAllWithResponse(PlaySource playSource, Context context) {
        return contentCapabilitiesAsync
            .playAsyncWithResponseInternal(playSource, Collections.emptyList(), context)
            .block();
    }
}
