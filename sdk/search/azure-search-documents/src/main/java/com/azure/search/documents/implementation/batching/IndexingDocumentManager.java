// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation.batching;

import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.options.OnActionAddedOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is responsible for keeping track of the documents that are currently being indexed and the documents that
 * are waiting to be indexed.
 *
 * @param <T> The type of document that is being indexed.
 */
final class IndexingDocumentManager<T> {
    private final LinkedList<TryTrackingIndexAction<T>> actions = new LinkedList<>();
    /*
     * This queue keeps track of documents that are currently being sent to the service for indexing. This queue is
     * resilient against cases where the request timeouts or is cancelled by an external operation, preventing the
     * documents from being lost.
     */
    private final LinkedList<TryTrackingIndexAction<T>> inFlightActions = new LinkedList<>();

    synchronized Collection<IndexAction<T>> getActions() {
        List<IndexAction<T>> actions = new ArrayList<>(inFlightActions.size() + this.actions.size());

        for (TryTrackingIndexAction<T> inFlightAction : inFlightActions) {
            actions.add(inFlightAction.getAction());
        }

        for (TryTrackingIndexAction<T> action : this.actions) {
            actions.add(action.getAction());
        }

        return actions;
    }

    /**
     * Adds a document to the queue of documents to be indexed.
     *
     * @param actions The documents to be indexed.
     */
    synchronized int add(Collection<IndexAction<T>> actions,
        Function<T, String> documentKeyRetriever,
        Consumer<OnActionAddedOptions<T>> onActionAddedConsumer) {
        for (IndexAction<T> action : actions) {
            this.actions.add(new TryTrackingIndexAction<>(action, documentKeyRetriever.apply(action.getDocument())));

            if (onActionAddedConsumer != null) {
                onActionAddedConsumer.accept(new OnActionAddedOptions<>(action));
            }
        }

        return this.actions.size();
    }

    synchronized boolean batchAvailableForProcessing(int batchActionCount) {
        return (this.actions.size() + this.inFlightActions.size()) >= batchActionCount;
    }

    synchronized List<TryTrackingIndexAction<T>> createBatch(int batchActionCount) {
        final List<TryTrackingIndexAction<T>> batchActions;
        final Set<String> keysInBatch;

        int actionSize = this.actions.size();
        int inFlightActionSize = this.inFlightActions.size();
        int size = Math.min(batchActionCount, actionSize + inFlightActionSize);
        batchActions = new ArrayList<>(size);

        // Make the set size larger than the expected batch size to prevent a resizing scenario. Don't use a load
        // factor of 1 as that would potentially cause collisions.
        keysInBatch = new HashSet<>(size * 2);

        // First attempt to fill the batch from documents that were lost in-flight.
        int inFlightDocumentsAdded = fillFromQueue(batchActions, inFlightActions, size, keysInBatch);

        // If the batch is filled using documents lost in-flight add the remaining back to the queue.
        if (inFlightDocumentsAdded == size) {
            reinsertFailedActions(inFlightActions);
        } else {
            // Then attempt to fill the batch from documents in the actions queue.
            fillFromQueue(batchActions, actions, size - inFlightDocumentsAdded, keysInBatch);
        }

        return batchActions;
    }

    private int fillFromQueue(List<TryTrackingIndexAction<T>> batch,
        List<TryTrackingIndexAction<T>> queue,
        int requested,
        Set<String> duplicateKeyTracker) {
        int offset = 0;
        int actionsAdded = 0;
        int queueSize = queue.size();

        while (actionsAdded < requested && offset < queueSize) {
            TryTrackingIndexAction<T> potentialDocumentToAdd = queue.get(offset++ - actionsAdded);

            if (duplicateKeyTracker.contains(potentialDocumentToAdd.getKey())) {
                continue;
            }

            duplicateKeyTracker.add(potentialDocumentToAdd.getKey());
            batch.add(queue.remove(offset - 1 - actionsAdded));
            actionsAdded += 1;
        }

        return actionsAdded;
    }

    synchronized void reinsertCancelledActions(List<TryTrackingIndexAction<T>> actionsInFlight) {
        inFlightActions.addAll(actionsInFlight);
    }

    synchronized void reinsertFailedActions(List<TryTrackingIndexAction<T>> actionsToRetry) {
        // Push all actions that need to be retried back into the queue.
        for (int i = actionsToRetry.size() - 1; i >= 0; i--) {
            this.actions.push(actionsToRetry.get(i));
        }
    }
}
