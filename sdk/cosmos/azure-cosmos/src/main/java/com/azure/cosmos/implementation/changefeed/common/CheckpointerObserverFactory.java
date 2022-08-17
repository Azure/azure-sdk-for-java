// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.implementation.changefeed.CheckpointFrequency;

/**
 * Factory class used to create instance(s) of {@link ChangeFeedObserver}.
 */
public class CheckpointerObserverFactory<T> implements ChangeFeedObserverFactory<T> {
    private final ChangeFeedObserverFactory<T> observerFactory;
    private final CheckpointFrequency checkpointFrequency;

    /**
     * Initializes a new instance of the {@link CheckpointerObserverFactory} class.
     *
     * @param observerFactory the instance of observer factory.
     * @param checkpointFrequency the the frequency of lease event.
     */
    public CheckpointerObserverFactory(ChangeFeedObserverFactory<T> observerFactory, CheckpointFrequency checkpointFrequency) {
        if (observerFactory == null) {
            throw new IllegalArgumentException("observerFactory");
        }

        if (checkpointFrequency == null) {
            throw new IllegalArgumentException("checkpointFrequency");
        }

        this.observerFactory = observerFactory;
        this.checkpointFrequency = checkpointFrequency;
    }

    /**
     * @return a new instance of {@link ChangeFeedObserver}.
     */
    @Override
    public ChangeFeedObserver<T> createObserver() {
        ChangeFeedObserver<T> observer = new ObserverExceptionWrappingChangeFeedObserverDecorator<>(this.observerFactory.createObserver());
        if (this.checkpointFrequency.isExplicitCheckpoint()) {
            return observer;
        }

        return new AutoCheckpointer<>(this.checkpointFrequency, observer);
    }
}
