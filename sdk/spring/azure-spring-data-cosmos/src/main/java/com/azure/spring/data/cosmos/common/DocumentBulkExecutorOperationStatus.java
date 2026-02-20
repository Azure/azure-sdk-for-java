// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DocumentBulkExecutorOperationStatus {
    private final AtomicBoolean flushCalled;
    private final AtomicLong operationsCompleted;
    private final AtomicLong operationsScheduled;

    private final AtomicLong requestChargeTracker;

    private final Set<String> pendingOperations;
    private final String operationId;

    private final String lockObject;

    private final List<BulkImportFailure> failures;

    private final List<Object> badInputDocuments;

    private final List<Object> goodInputDocuments;

    private final Instant startedAt;

    public DocumentBulkExecutorOperationStatus() {
        this(UUID.randomUUID().toString());
    }

    public DocumentBulkExecutorOperationStatus(String operationId) {
        Objects.requireNonNull(operationId, "Argument 'operationId' must not be null.");
        this.operationId = operationId;
        this.flushCalled = new AtomicBoolean(false);
        this.operationsCompleted = new AtomicLong(0);
        this.operationsScheduled = new AtomicLong(0);
        this.requestChargeTracker = new AtomicLong(0);
        this.pendingOperations = ConcurrentHashMap.newKeySet();
        this.lockObject = UUID.randomUUID().toString();
        this.failures = new CopyOnWriteArrayList<>();
        this.badInputDocuments = new CopyOnWriteArrayList<>();
        this.goodInputDocuments = new CopyOnWriteArrayList<>();
        this.startedAt = Instant.now();
    }

    public AtomicBoolean getFlushCalled() {
        return this.flushCalled;
    }

    public AtomicLong getOperationsCompleted() {
        return this.operationsCompleted;
    }

    public AtomicLong getOperationsScheduled() {
        return this.operationsScheduled;
    }

    AtomicLong getRequestChargeTracker() {
        return this.requestChargeTracker;
    }

    public double getTotalRequestChargeSnapshot() {
        return this.requestChargeTracker.get() / 100d;
    }

    public List<BulkImportFailure> getFailuresSnapshot() {
        return this.failures;
    }

    public List<Object> getBadInputDocumentsSnapshot() {
        return this.badInputDocuments;
    }

    public List<Object> getGoodInputDocumentsSnapshot() {
        return this.goodInputDocuments;
    }

    public Instant getStartedAt() {
        return this.startedAt;
    }

    public String getOperationId() {
        return this.operationId;
    }

    public List<String> getPendingOperationsSampleSnapshot(int countHint) {
        synchronized (this.lockObject) {
            return this
                .pendingOperations
                .stream()
                .limit(Math.min(countHint, 100))
                .collect(Collectors.toList());
        }
    }

    void clearPendingOperations() {
        synchronized (this.lockObject) {
            this.pendingOperations.clear();
        }
    }

    Set<String> getPendingOperations() {
        return pendingOperations;
    }

    String getLockObject() {
        return this.lockObject;
    }

    void addFailure(Object badInputDocument, BulkImportFailure failure) {
        Objects.requireNonNull(failure, "Argument 'failure' must not be null.");
        if (badInputDocument != null) {
            this.badInputDocuments.add(badInputDocument);
        }

        this.failures.add(failure);
    }

    void addSuccess(Object goodInputDocument) {
        if (goodInputDocument != null) {
            this.goodInputDocuments.add(goodInputDocument);
        }
    }
}
