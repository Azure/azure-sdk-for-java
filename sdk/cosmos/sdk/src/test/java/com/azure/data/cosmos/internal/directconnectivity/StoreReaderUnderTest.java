// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.ISessionContainer;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoreReaderUnderTest extends StoreReader {

    public List<Pair<Method, List<Object>>> invocations = Collections.synchronizedList(new ArrayList<>());

    public StoreReaderUnderTest(TransportClient transportClient, AddressSelector addressSelector, ISessionContainer sessionContainer) {
        super(transportClient, addressSelector, sessionContainer);
    }

    @Override
    public Mono<List<StoreResult>> readMultipleReplicaAsync(RxDocumentServiceRequest entity, boolean includePrimary, int replicaCountToRead, boolean requiresValidLsn, boolean useSessionToken, ReadMode readMode) {
        Method method = new Object(){}.getClass().getEnclosingMethod();
        ImmutableList<Object> list = ImmutableList.of(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode);
        invocations.add(Pair.of(method, list));

        return super.readMultipleReplicaAsync(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode);
    }

    @Override
    public Mono<List<StoreResult>> readMultipleReplicaAsync(RxDocumentServiceRequest entity, boolean includePrimary, int replicaCountToRead, boolean requiresValidLsn, boolean useSessionToken, ReadMode readMode, boolean checkMinLSN, boolean forceReadAll) {
        Method method = new Object(){}.getClass().getEnclosingMethod();
        ImmutableList<Object> list = ImmutableList.of(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, checkMinLSN, forceReadAll);
        invocations.add(Pair.of(method, list));
        return super.readMultipleReplicaAsync(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, checkMinLSN, forceReadAll);
    }

    @Override
    public Mono<StoreResult> readPrimaryAsync(RxDocumentServiceRequest entity, boolean requiresValidLsn, boolean useSessionToken) {
        Method method = new Object(){}.getClass().getEnclosingMethod();
        ImmutableList<Object> list = ImmutableList.of(entity, requiresValidLsn, useSessionToken);
        invocations.add(Pair.of(method, list));
        return super.readPrimaryAsync(entity, requiresValidLsn, useSessionToken);
    }

}
