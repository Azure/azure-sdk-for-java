/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.ISessionContainer;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import rx.Single;

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
    public Single<List<StoreResult>> readMultipleReplicaAsync(RxDocumentServiceRequest entity, boolean includePrimary, int replicaCountToRead, boolean requiresValidLsn, boolean useSessionToken, ReadMode readMode) {
        Method method = new Object(){}.getClass().getEnclosingMethod();
        ImmutableList<Object> list = ImmutableList.of(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode);
        invocations.add(Pair.of(method, list));

        return super.readMultipleReplicaAsync(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode);
    }

    @Override
    public Single<List<StoreResult>> readMultipleReplicaAsync(RxDocumentServiceRequest entity, boolean includePrimary, int replicaCountToRead, boolean requiresValidLsn, boolean useSessionToken, ReadMode readMode, boolean checkMinLSN, boolean forceReadAll) {
        Method method = new Object(){}.getClass().getEnclosingMethod();
        ImmutableList<Object> list = ImmutableList.of(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, checkMinLSN, forceReadAll);
        invocations.add(Pair.of(method, list));
        return super.readMultipleReplicaAsync(entity, includePrimary, replicaCountToRead, requiresValidLsn, useSessionToken, readMode, checkMinLSN, forceReadAll);
    }

    @Override
    public Single<StoreResult> readPrimaryAsync(RxDocumentServiceRequest entity, boolean requiresValidLsn, boolean useSessionToken) {
        Method method = new Object(){}.getClass().getEnclosingMethod();
        ImmutableList<Object> list = ImmutableList.of(entity, requiresValidLsn, useSessionToken);
        invocations.add(Pair.of(method, list));
        return super.readPrimaryAsync(entity, requiresValidLsn, useSessionToken);
    }

}
