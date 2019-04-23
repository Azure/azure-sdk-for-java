// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.MessagingEntityType;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.MiscRequestResponseOperationHandler;

final class SessionBrowser {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SessionBrowser.class);
    private static final int PAGESIZE = 100;
    // .net DateTime.MaxValue need to be passed
    private static final Date MAXDATE = new Date(253402300800000l);

    private final MessagingFactory messagingFactory;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private MiscRequestResponseOperationHandler miscRequestResponseHandler;

    SessionBrowser(MessagingFactory messagingFactory, String entityPath, MessagingEntityType entityType, MiscRequestResponseOperationHandler miscRequestResponseHandler) {
        this.messagingFactory = messagingFactory;
        this.entityPath = entityPath;
        this.entityType = entityType;
        this.miscRequestResponseHandler = miscRequestResponseHandler;
    }

    public CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync() {
        return this.getMessageSessionsAsync(MAXDATE);
    }

    public CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync(Date lastUpdatedTime) {
        return this.getMessageSessionsAsync(lastUpdatedTime, 0, null);
    }

    private CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync(Date lastUpdatedTime, int lastReceivedSkip, String lastSessionId) {
        TRACE_LOGGER.debug("Getting '{}' browsable sessions from entity '{}', lastUpdatedTime '{}', lastReceivedSkip '{}', lastSessionId '{}'", PAGESIZE, this.entityPath, lastUpdatedTime, lastReceivedSkip, lastSessionId);
        return this.miscRequestResponseHandler.getMessageSessionsAsync(lastUpdatedTime, lastReceivedSkip, PAGESIZE, lastSessionId).thenComposeAsync((p) ->
        {
            int newLastReceivedSkip = p.getSecondItem();
            String[] sessionIds = p.getFirstItem();
            ArrayList<IMessageSession> sessionsList = new ArrayList<>();
            if (sessionIds != null && sessionIds.length > 0) {
                TRACE_LOGGER.debug("Got '{}' browsable sessions from entity '{}', receivedSkip '{}'", sessionIds.length, this.entityPath, newLastReceivedSkip);
                CompletableFuture[] initFutures = new CompletableFuture[sessionIds.length];
                int initFutureIndex = 0;
                String newLastSessionId = sessionIds[sessionIds.length - 1];
                for (String sessionId : sessionIds) {
                    BrowsableMessageSession browsableSession = new BrowsableMessageSession(sessionId, this.messagingFactory, this.entityPath, this.entityType);
                    sessionsList.add(browsableSession);
                    initFutures[initFutureIndex++] = browsableSession.initializeAsync();
                }
                CompletableFuture<Void> allInitFuture = CompletableFuture.allOf(initFutures);
                return allInitFuture.thenComposeAsync((v) -> getMessageSessionsAsync(lastUpdatedTime, newLastReceivedSkip, newLastSessionId), MessagingFactory.INTERNAL_THREAD_POOL).thenApply((c) -> {
                    sessionsList.addAll(c);
                    return sessionsList;
                });
            } else {
                TRACE_LOGGER.debug("Got no browsable sessions from entity '{}'", this.entityPath);
                return CompletableFuture.completedFuture(sessionsList);
            }
        }, MessagingFactory.INTERNAL_THREAD_POOL);
    }
}
