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
