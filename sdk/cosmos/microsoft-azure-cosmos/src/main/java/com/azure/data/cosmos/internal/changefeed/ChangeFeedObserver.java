// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.CosmosItemProperties;

import java.util.List;

/**
 * The interface used to deliver change events to document feed observers.
 */
public interface ChangeFeedObserver {
    /**
     * This is called when change feed observer is opened.
     *
     * @param context the context specifying partition for this observer, etc.
     */
    void open(ChangeFeedObserverContext context);

    /**
     * This is called when change feed observer is closed.
     *
     * @param context the context specifying partition for this observer, etc.
     * @param reason the reason the observer is closed.
     */
    void close(ChangeFeedObserverContext context, ChangeFeedObserverCloseReason reason);

    /**
     * This is called when document changes are available on change feed.
     *
     * @param context the context specifying partition for this observer, etc.
     * @param docs the documents changed.
     */
    void processChanges(ChangeFeedObserverContext context, List<CosmosItemProperties> docs);
}
