// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.paging.PageRetriever;
import com.azure.core.util.paging.PageRetrieverSync;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.SessionIdPage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A {@link PageRetriever} that retrieves pages of session ids.
 */
final class ServiceBusSessionIdPageRetriever implements PageRetriever<String, ServiceBusSessionIdPage> {
    private final ConnectionCacheWrapper connectionCache;
    private final String entityPath;
    private final MessagingEntityType entityType;

    /**
     * Creates an instance of {@link ServiceBusSessionIdPageRetriever}.
     *
     * @param connectionCache the connection cache to obtain the connection to make the request to retrieve the sessions.
     * @param entityPath the entity path to retrieve the sessions.
     * @param entityType the type of the entity.
     */
    ServiceBusSessionIdPageRetriever(ConnectionCacheWrapper connectionCache, String entityPath,
        MessagingEntityType entityType) {
        this.connectionCache = connectionCache;
        this.entityPath = entityPath;
        this.entityType = entityType;
    }

    /**
     * Obtain a synchronous page retriever.
     *
     * @param defaultPageSize the default page size to use when retrieving the sessions.
     * @return the synchronous page retriever.
     */
    public PageRetrieverSync<String, ServiceBusSessionIdPage> sync(int defaultPageSize) {
        return new Sync(this, defaultPageSize);
    }

    @Override
    public Flux<ServiceBusSessionIdPage> get(String continuationToken, Integer pageSize) {
        assert pageSize != null;
        final int skip = toSkip(continuationToken);
        return getIntern(skip, pageSize).map(innerPage -> {
            final String nextToken = "" + innerPage.getSkip();
            return new ServiceBusSessionIdPage(innerPage.getIds(), nextToken);
        }).flux();
    }

    private Mono<SessionIdPage> getIntern(int skip, int top) {
        return connectionCache.getConnection().flatMap(connection -> {
            return connection.getManagementNode(entityPath, entityType);
        }).flatMap(managementNode -> {
            return managementNode.getSessionIds(skip, top);
        });
    }

    private static int toSkip(String continuationToken) {
        return continuationToken != null ? Integer.parseInt(continuationToken) : 0;
    }

    private static final class Sync implements PageRetrieverSync<String, ServiceBusSessionIdPage> {
        private final ServiceBusSessionIdPageRetriever inner;
        private final int defaultPageSize;

        Sync(ServiceBusSessionIdPageRetriever inner, int defaultPageSize) {
            this.inner = inner;
            this.defaultPageSize = defaultPageSize;
        }

        @Override
        public ServiceBusSessionIdPage getPage(String continuationToken, Integer pageSize) {
            final int skip = toSkip(continuationToken);
            final int top = pageSize == null ? defaultPageSize : pageSize;
            final SessionIdPage page = inner.getIntern(skip, top).block();
            assert page != null;
            final String nextToken = "" + page.getSkip();
            return new ServiceBusSessionIdPage(page.getIds(), nextToken);
        }
    }
}
