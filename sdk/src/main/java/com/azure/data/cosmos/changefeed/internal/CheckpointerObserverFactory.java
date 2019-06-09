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

import com.azure.data.cosmos.ChangeFeedObserver;
import com.azure.data.cosmos.changefeed.CheckpointFrequency;
import com.azure.data.cosmos.ChangeFeedObserverFactory;

/**
 * Factory class used to create instance(s) of {@link ChangeFeedObserver}.
 */
public class CheckpointerObserverFactory implements ChangeFeedObserverFactory {
    private final ChangeFeedObserverFactory observerFactory;
    private final CheckpointFrequency checkpointFrequency;

    /**
     * Initializes a new instance of the {@link CheckpointerObserverFactory} class.
     *
     * @param observerFactory the instance of observer factory.
     * @param checkpointFrequency the the frequency of lease event.
     */
    public CheckpointerObserverFactory(ChangeFeedObserverFactory observerFactory, CheckpointFrequency checkpointFrequency)
    {
        if (observerFactory == null) throw new IllegalArgumentException("observerFactory");
        if (checkpointFrequency == null) throw new IllegalArgumentException("checkpointFrequency");

        this.observerFactory = observerFactory;
        this.checkpointFrequency = checkpointFrequency;
    }

    /**
     * @return a new instance of {@link ChangeFeedObserver}.
     */
    @Override
    public ChangeFeedObserver createObserver() {
        ChangeFeedObserver observer = new ObserverExceptionWrappingChangeFeedObserverDecorator(this.observerFactory.createObserver());
        if (this.checkpointFrequency.isExplicitCheckpoint()) return observer;

        return new AutoCheckpointer(this.checkpointFrequency, observer);
    }
}
