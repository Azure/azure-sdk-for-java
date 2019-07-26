// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverCloseReason;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverContext;
import com.azure.data.cosmos.internal.changefeed.exceptions.ObserverException;

import java.util.List;

/**
 * Exception wrapping decorator implementation for {@link ChangeFeedObserver}.
 */
class ObserverExceptionWrappingChangeFeedObserverDecorator implements ChangeFeedObserver {
    private ChangeFeedObserver changeFeedObserver;

    public ObserverExceptionWrappingChangeFeedObserverDecorator(ChangeFeedObserver changeFeedObserver)
    {
        this.changeFeedObserver = changeFeedObserver;
    }

    @Override
    public void open(ChangeFeedObserverContext context) {
        try
        {
            this.changeFeedObserver.open(context);
        }
        catch (RuntimeException userException)
        {
            // Logger.WarnException("Exception happened on Observer.OpenAsync", userException);
            throw new ObserverException(userException);
        }
    }

    @Override
    public void close(ChangeFeedObserverContext context, ChangeFeedObserverCloseReason reason) {
        try
        {
            this.changeFeedObserver.close(context, reason);
        }
        catch (RuntimeException userException)
        {
            // Logger.WarnException("Exception happened on Observer.CloseAsync", userException);
            throw new ObserverException(userException);
        }
    }

    @Override
    public void processChanges(ChangeFeedObserverContext context, List<CosmosItemProperties> docs) {
        try
        {
            this.changeFeedObserver.processChanges(context, docs);
        }
        catch (Exception userException)
        {
            // Logger.WarnException("Exception happened on Observer.OpenAsync", userException);
            throw new ObserverException(userException);
        }
    }
}
