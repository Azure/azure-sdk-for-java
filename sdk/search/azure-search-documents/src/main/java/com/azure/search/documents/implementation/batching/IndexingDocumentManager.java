// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation.batching;

import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.options.OnActionAddedOptions;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is responsible for keeping track of the documents that are currently being indexed and the documents that
 * are waiting to be indexed.
 */
final class IndexingDocumentManager {
    private final LinkedList<TryTrackingIndexAction> actions = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();

    IndexingDocumentManager() {
    }

    /*
     * This queue keeps track of documents that are currently being sent to the service for indexing. This queue is
     * resilient against cases where the request timeouts or is cancelled by an external operation, preventing the
     * documents from being lost.
     */
    private final Deque<TryTrackingIndexAction> inFlightActions = new LinkedList<>();

    Collection<IndexAction> getActions() {
        lock.lock();
        try {
            List<IndexAction> actions = new ArrayList<>(inFlightActions.size() + this.actions.size());

            for (TryTrackingIndexAction inFlightAction : inFlightActions) {
                actions.add(inFlightAction.getAction());
            }

            for (TryTrackingIndexAction action : this.actions) {
                actions.add(action.getAction());
            }

            return actions;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds documents to the batch and checks if there is a batch available for processing.
     * <p>
     * Adding documents and checking for a batch is done at the same time as these generally happen together and reduces
     * the number of times the lock needs to be acquired.
     *
     * @param actions The documents to be indexed.
     * @param documentKeyRetriever The function to retrieve the key from the document.
     * @param onActionAddedConsumer The consumer to be called when an action is added.
     * @param batchSize The size required to create a batch
     * @return A tuple of the number of actions in the batch and if a batch is available for processing.
     */
    Tuple2<Integer, Boolean> addAndCheckForBatch(Collection<IndexAction> actions,
        Function<Map<String, Object>, String> documentKeyRetriever,
        Consumer<OnActionAddedOptions> onActionAddedConsumer, int batchSize) {
        lock.lock();

        try {
            for (IndexAction action : actions) {
                this.actions.addLast(
                    new TryTrackingIndexAction(action, documentKeyRetriever.apply(action.getAdditionalProperties())));

                if (onActionAddedConsumer != null) {
                    onActionAddedConsumer.accept(new OnActionAddedOptions(action));
                }
            }

            int numberOfActions = this.actions.size();
            boolean hasBatch = numberOfActions + inFlightActions.size() >= batchSize;

            return Tuples.of(numberOfActions, hasBatch);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to create a batch of documents to be sent to the service for indexing.
     * <p>
     * If a batch fails to be created null will be returned. A batch can fail to be created if there aren't enough
     * documents to create a batch.
     *
     * @param batchSize The number of actions to include in the batch.
     * @param ignoreBatchSize If true, the batch size won't be checked and the batch will be created with the number of
     * actions available.
     * @return A list of documents to be sent to the service for indexing.
     */
    List<TryTrackingIndexAction> tryCreateBatch(int batchSize, boolean ignoreBatchSize) {
        lock.lock();

        try {
            int actionSize = this.actions.size();
            int inFlightActionSize = this.inFlightActions.size();
            if (!ignoreBatchSize && actionSize + inFlightActionSize < batchSize) {
                return null;
            }

            int size = Math.min(batchSize, actionSize + inFlightActionSize);
            final List<TryTrackingIndexAction> batchActions = new ArrayList<>(size);

            // Make the set size larger than the expected batch size to prevent a resizing scenario. Don't use a load
            // factor of 1 as that would potentially cause collisions.
            final Set<String> keysInBatch = new HashSet<>(size * 2);

            // First attempt to fill the batch from documents that were lost in-flight.
            int inFlightDocumentsAdded = fillFromQueue(batchActions, inFlightActions, size, keysInBatch);

            // If the batch is filled using documents lost in-flight add the remaining back to the beginning of the queue.
            if (inFlightDocumentsAdded == size) {
                TryTrackingIndexAction inflightAction;
                while ((inflightAction = inFlightActions.pollLast()) != null) {
                    actions.push(inflightAction);
                }
            } else {
                // Then attempt to fill the batch from documents in the actions queue.
                fillFromQueue(batchActions, actions, size - inFlightDocumentsAdded, keysInBatch);
            }

            return batchActions;
        } finally {
            lock.unlock();
        }
    }

    private int fillFromQueue(List<TryTrackingIndexAction> batch, Collection<TryTrackingIndexAction> queue,
        int requested, Set<String> duplicateKeyTracker) {
        int actionsAdded = 0;

        Iterator<TryTrackingIndexAction> iterator = queue.iterator();
        while (actionsAdded < requested && iterator.hasNext()) {
            TryTrackingIndexAction potentialDocumentToAdd = iterator.next();

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

    void reinsertCancelledActions(List<TryTrackingIndexAction> actionsInFlight) {
        lock.lock();
        try {
            inFlightActions.addAll(actionsInFlight);
        } finally {
            lock.unlock();
        }
    }

    void reinsertFailedActions(List<TryTrackingIndexAction> actionsToRetry) {
        lock.lock();

        try {
            // Push all actions that need to be retried back into the queue.
            for (int i = actionsToRetry.size() - 1; i >= 0; i--) {
                this.actions.push(actionsToRetry.get(i));
            }
        } finally {
            lock.unlock();
        }
    }
}
