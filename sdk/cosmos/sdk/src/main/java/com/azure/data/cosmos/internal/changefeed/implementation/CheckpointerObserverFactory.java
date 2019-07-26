// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverFactory;
import com.azure.data.cosmos.internal.changefeed.CheckpointFrequency;

/**
 * Factory class used to create instance(s) of {@link ChangeFeedObserver}.
 */
class CheckpointerObserverFactory implements ChangeFeedObserverFactory {
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
