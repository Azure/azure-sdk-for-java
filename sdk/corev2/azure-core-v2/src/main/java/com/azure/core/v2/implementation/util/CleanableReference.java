// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation.util;

import java.lang.ref.PhantomReference;

/**
 * This class manages maintaining a reference to an object that will trigger a cleanup action once it is phantom
 * reachable.
 */
final class CleanableReference<T> extends PhantomReference<T> {
    // The cleanup action to run once the reference is phantom reachable.
    private final Runnable cleanupAction;

    // The list of cleanable references.
    private final CleanableReference<?> cleanupList;

    CleanableReference<?> previous = this;
    CleanableReference<?> next = this;

    CleanableReference() {
        super(null, null);
        this.cleanupAction = null;
        this.cleanupList = this;
    }

    CleanableReference(T referent, Runnable cleanupAction, ReferenceManagerImpl manager) {
        super(referent, manager.getQueue());
        this.cleanupAction = cleanupAction;
        this.cleanupList = manager.getCleanableReferenceList();
        insert();
    }

    public void clean() {
        if (remove()) {
            super.clear();
            cleanupAction.run();
        }
    }

    @Override
    public void clear() {
        if (remove()) {
            super.clear();
        }
    }

    boolean hasRemaining() {
        synchronized (cleanupList) {
            return cleanupList != cleanupList.next;
        }
    }

    private void insert() {
        synchronized (cleanupList) {
            previous = cleanupList;
            next = cleanupList.next;
            next.previous = this;
            cleanupList.next = this;
        }
    }

    private boolean remove() {
        synchronized (cleanupList) {
            if (next != this) {
                next.previous = previous;
                previous.next = next;
                previous = this;
                next = this;
                return true;
            }

            return false;
        }
    }
}
