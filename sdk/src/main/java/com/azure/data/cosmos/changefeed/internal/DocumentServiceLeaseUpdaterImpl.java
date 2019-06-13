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
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.AccessCondition;
import com.azure.data.cosmos.AccessConditionType;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.changefeed.Lease;
import com.azure.data.cosmos.changefeed.ServiceItemLease;
import com.azure.data.cosmos.changefeed.ServiceItemLeaseUpdater;
import com.azure.data.cosmos.changefeed.exceptions.LeaseLostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

import static com.azure.data.cosmos.changefeed.internal.ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT;
import static com.azure.data.cosmos.changefeed.internal.ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND;
import static com.azure.data.cosmos.changefeed.internal.ChangeFeedHelper.HTTP_STATUS_CODE_PRECONDITION_FAILED;

/**
 * Implementation for service lease updater interface.
 */
public class DocumentServiceLeaseUpdaterImpl implements ServiceItemLeaseUpdater {
    private final Logger logger = LoggerFactory.getLogger(TraceHealthMonitor.class);
    private final int RETRY_COUNT_ON_CONFLICT = 5;
    private final ChangeFeedContextClient client;

    public DocumentServiceLeaseUpdaterImpl(ChangeFeedContextClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client");
        }

        this.client = client;
    }

    @Override
    public Mono<Lease> updateLease(Lease cachedLease, CosmosItem itemLink, CosmosItemRequestOptions requestOptions, Function<Lease, Lease> updateLease) {
        Lease lease = cachedLease;

        for (int retryCount = RETRY_COUNT_ON_CONFLICT; retryCount > 0; retryCount--) {
            lease = updateLease.apply(lease);

            if (lease == null) {
                return null;
            }

            lease.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
            CosmosItemProperties leaseDocument = this.tryReplaceLease(lease, itemLink);

            if (leaseDocument != null) {
                return Mono.just(ServiceItemLease.fromDocument(leaseDocument));
            }

            // Partition lease update conflict. Reading the current version of lease.
            CosmosItemProperties document = null;
            try {
                CosmosItemResponse response = this.client.readItem(itemLink, requestOptions)
                    .block();
                document = response.properties();
            } catch (RuntimeException re) {
                if (re.getCause() instanceof CosmosClientException) {
                    CosmosClientException ex = (CosmosClientException) re.getCause();
                    if (ex.statusCode() == HTTP_STATUS_CODE_NOT_FOUND) {
                        // Partition lease no longer exists
                        throw new LeaseLostException(lease);
                    }
                }
                throw  re;
            }

            ServiceItemLease serverLease = ServiceItemLease.fromDocument(document);
//            Logger.InfoFormat(
//                "Partition {0} update failed because the lease with token '{1}' was updated by host '{2}' with token '{3}'. Will retry, {4} retry(s) left.",
//                lease.LeaseToken,
//                lease.ConcurrencyToken,
//                serverLease.Owner,
//                serverLease.ConcurrencyToken,
//                retryCount);

            lease = serverLease;
        }

        throw new LeaseLostException(lease);
    }

    private CosmosItemProperties tryReplaceLease(Lease lease, CosmosItem itemLink) throws LeaseLostException {
        try {
            CosmosItemResponse response = this.client.replaceItem(itemLink, lease, this.getCreateIfMatchOptions(lease))
                .block();
            return response.properties();
        } catch (RuntimeException re) {
            if (re.getCause() instanceof CosmosClientException) {
                CosmosClientException ex = (CosmosClientException) re.getCause();
                switch (ex.statusCode()) {
                    case HTTP_STATUS_CODE_PRECONDITION_FAILED: {
                        return null;
                    }
                    case HTTP_STATUS_CODE_CONFLICT: {
                        throw new LeaseLostException(lease, ex, false);
                    }
                    case HTTP_STATUS_CODE_NOT_FOUND: {
                        throw new LeaseLostException(lease, ex, true);
                    }
                    default: {
                        throw re;
                    }
                }
            }
            throw re;
        }
    }

    private CosmosItemRequestOptions getCreateIfMatchOptions(Lease lease) {
        AccessCondition ifMatchCondition = new AccessCondition();
        ifMatchCondition.type(AccessConditionType.IF_MATCH);
        ifMatchCondition.condition(lease.getConcurrencyToken());

        CosmosItemRequestOptions createIfMatchOptions = new CosmosItemRequestOptions();
        createIfMatchOptions.accessCondition(ifMatchCondition);

        return createIfMatchOptions;
    }
}
