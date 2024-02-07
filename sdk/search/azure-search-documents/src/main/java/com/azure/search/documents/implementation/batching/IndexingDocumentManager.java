// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation.batching;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.options.OnActionAddedOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is responsible for keeping track of the documents that are currently being indexed and the documents that
 * are waiting to be indexed.
 *
 * @param <T> The type of document that is being indexed.
 */
final class IndexingDocumentManager<T> {
    private final ClientLogger logger;

    private final LinkedList<TryTrackingIndexAction<T>> actions = new LinkedList<>();
    private final Semaphore semaphore = new Semaphore(1);

    IndexingDocumentManager(ClientLogger logger) {
        this.logger = Objects.requireNonNull(logger, "'logger' cannot be null.");
    }

    /*
     * This queue keeps track of documents that are currently being sent to the service for indexing. This queue is
     * resilient against cases where the request timeouts or is cancelled by an external operation, preventing the
     * documents from being lost.
     */
    private final Deque<TryTrackingIndexAction<T>> inFlightActions = new LinkedList<>();

    Collection<IndexAction<T>> getActions() {
        acquireSemaphore();
        try {
            List<IndexAction<T>> actions = new ArrayList<>(inFlightActions.size() + this.actions.size());

            for (TryTrackingIndexAction<T> inFlightAction : inFlightActions) {
                actions.add(inFlightAction.getAction());
            }

            for (TryTrackingIndexAction<T> action : this.actions) {
                actions.add(action.getAction());
            }

            return actions;
        } finally {
            semaphore.release();
        }
    }

    /**
     * Adds a document to the queue of documents to be indexed.
     *
     * @param actions The documents to be indexed.
     */
    int add(Collection<IndexAction<T>> actions, Function<T, String> documentKeyRetriever,
        Consumer<OnActionAddedOptions<T>> onActionAddedConsumer) {
        acquireSemaphore();

        try {
            for (IndexAction<T> action : actions) {
                this.actions.addLast(
                    new TryTrackingIndexAction<>(action, documentKeyRetriever.apply(action.getDocument())));

                if (onActionAddedConsumer != null) {
                    onActionAddedConsumer.accept(new OnActionAddedOptions<>(action));
                }
            }

            return this.actions.size();
        } finally {
            semaphore.release();
        }
    }

    boolean batchAvailableForProcessing(int batchActionCount) {
        acquireSemaphore();

        try {
            return (this.actions.size() + this.inFlightActions.size()) >= batchActionCount;
        } finally {
            semaphore.release();
        }
    }

    List<TryTrackingIndexAction<T>> createBatch(int batchActionCount) {
        acquireSemaphore();

        try {
            int actionSize = this.actions.size();
            int inFlightActionSize = this.inFlightActions.size();
            int size = Math.min(batchActionCount, actionSize + inFlightActionSize);
            final List<TryTrackingIndexAction<T>> batchActions = new ArrayList<>(size);

            // Make the set size larger than the expected batch size to prevent a resizing scenario. Don't use a load
            // factor of 1 as that would potentially cause collisions.
            final Set<String> keysInBatch = new HashSet<>(size * 2);

            // First attempt to fill the batch from documents that were lost in-flight.
            int inFlightDocumentsAdded = fillFromQueue(batchActions, inFlightActions, size, keysInBatch);

            // If the batch is filled using documents lost in-flight add the remaining back to the beginning of the queue.
            if (inFlightDocumentsAdded == size) {
                TryTrackingIndexAction<T> inflightAction;
                while ((inflightAction = inFlightActions.pollLast()) != null) {
                    actions.push(inflightAction);
                }
            } else {
                // Then attempt to fill the batch from documents in the actions queue.
                fillFromQueue(batchActions, actions, size - inFlightDocumentsAdded, keysInBatch);
            }

            return batchActions;
        } finally {
            semaphore.release();
        }
    }

    private int fillFromQueue(List<TryTrackingIndexAction<T>> batch, Collection<TryTrackingIndexAction<T>> queue,
        int requested, Set<String> duplicateKeyTracker) {
        int actionsAdded = 0;

        Iterator<TryTrackingIndexAction<T>> iterator = queue.iterator();
        while (actionsAdded < requested && iterator.hasNext()) {
            TryTrackingIndexAction<T> potentialDocumentToAdd = iterator.next();

            if (duplicateKeyTracker.contains(potentialDocumentToAdd.getKey())) {
                continue;
            }

            duplicateKeyTracker.add(potentialDocumentToAdd.getKey());
            batch.add(potentialDocumentToAdd);
            iterator.remove();
            actionsAdded += 1;
        }

        return actionsAdded;
    }

    void reinsertCancelledActions(List<TryTrackingIndexAction<T>> actionsInFlight) {
        acquireSemaphore();
        try {
            inFlightActions.addAll(actionsInFlight);
        } finally {
            semaphore.release();
        }
    }

    void reinsertFailedActions(List<TryTrackingIndexAction<T>> actionsToRetry) {
        acquireSemaphore();

        try {
            // Push all actions that need to be retried back into the queue.
            for (int i = actionsToRetry.size() - 1; i >= 0; i--) {
                this.actions.push(actionsToRetry.get(i));
            }
        } finally {
            semaphore.release();
        }
    }

    private void acquireSemaphore() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }
}
