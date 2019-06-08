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
package com.microsoft.azure.cosmos.changefeed.internal;

import com.microsoft.azure.cosmos.ChangeFeedObserver;
import com.microsoft.azure.cosmos.ChangeFeedObserverCloseReason;
import com.microsoft.azure.cosmos.ChangeFeedObserverContext;
import com.microsoft.azure.cosmos.CosmosItem;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmos.changefeed.exceptions.ObserverException;

import java.util.List;

/**
 * Exception wrapping decorator implementation for {@link ChangeFeedObserver}.
 */
public class ObserverExceptionWrappingChangeFeedObserverDecorator implements ChangeFeedObserver {
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
    public void processChanges(ChangeFeedObserverContext context, List<CosmosItemSettings> docs) {
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
