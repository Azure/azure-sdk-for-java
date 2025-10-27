// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkExecutionThresholdsState;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BulkWriter implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BulkWriter.class);

    // app is using multiple bulk writer instances
    // concurrently - so, assuming one CPU per writer
    private final static int cpuCount = 1;
    private final static int maxPendingOperationCount = 1024 * 167 / cpuCount;

    // make sure we keep reference to micro batch size calculation state for
    // entire lifetime of JVM
    private final static CosmosBulkExecutionThresholdsState bulkProcessingThresholds =
        new CosmosBulkExecutionThresholdsState();

    private final static String  BULK_WRITER_INPUT_BOUNDED_ELASTIC_THREAD_NAME
        = "bwinput-";
    private final static String  BULK_WRITER_RESPONSES_BOUNDED_ELASTIC_THREAD_NAME
        = "bwrsp-";

    private final static String  BULK_WRITER_RETRY_DELAY_SCHEDULING_THREAD_NAME
        = "bwretrydelay-";
    private final static int TTL_FOR_SCHEDULER_WORKER_IN_SECONDS = 60;

    private final static Random rnd = new Random();

    // Custom bounded elastic scheduler to consume input flux
    private final Scheduler bulkWriterInputBoundedElastic = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + 2 * maxPendingOperationCount,
        BULK_WRITER_INPUT_BOUNDED_ELASTIC_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true);

    // Custom bounded elastic scheduler to switch off IO thread to process response.
    private final Scheduler  bulkWriterResponsesBoundedElastic = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + 2 * maxPendingOperationCount,
        BULK_WRITER_RESPONSES_BOUNDED_ELASTIC_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true);

    private final static ScheduledExecutorService retryDelayScheduler = Executors.newScheduledThreadPool(
        1,
        new CosmosDaemonThreadFactory(BULK_WRITER_RETRY_DELAY_SCHEDULING_THREAD_NAME));



    private final Sinks.Many<CosmosItemOperation> bulkInputEmitter =
        Sinks.many().unicast().onBackpressureBuffer();

    private final AtomicReference<Flux<Object>> processingFluxHolder = new AtomicReference<>(null);

    //Max items to be buffered to avoid out of memory error
    private final Semaphore semaphore = new Semaphore(1024 * 167 / cpuCount);

    private static final Sinks.EmitFailureHandler emitFailureHandler =
        (signalType, emitResult) -> {
            if (emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED)) {
                logger.debug("emitFailureHandler - Signal: [{}], Result: [{}]", signalType, emitResult);
                return true;
            } else if(emitResult.equals(Sinks.EmitResult.FAIL_CANCELLED)
                || emitResult.equals(Sinks.EmitResult.FAIL_TERMINATED)) {

                logger.debug(
                    "emitFailureHandlerForComplete - Already completed - Signal: [{}], Result: [{}]}",
                    signalType,
                    emitResult);
                return false;
            } else {
                logger.error("emitFailureHandler - Signal: [{}], Result: [{}]", signalType, emitResult);
                return false;
            }
        };

    private final CosmosAsyncContainer cosmosAsyncContainer;

    private final String identifier;

    private final CosmosBulkExecutionOptions bulkOptions;

    private final Lock lock = new ReentrantLock();
    private final Condition flushCompletedCondition = lock.newCondition();

    private final DocumentBulkExecutorOperationStatus status;

    private Map<String, JsonNode> inputBulkItems = new HashMap<>();

    public BulkWriter(
        CosmosAsyncContainer cosmosAsyncContainer,
        DocumentBulkExecutorOperationStatus status) {

        Objects.requireNonNull(cosmosAsyncContainer, "Argument 'cosmosAsyncContainer' must not be null.");
        Objects.requireNonNull(status, "Argument 'status' must not be null.");

        this.cosmosAsyncContainer = cosmosAsyncContainer;
        this.identifier = status.getOperationId();
        this.status = status;
        this.bulkOptions = new CosmosBulkExecutionOptions(bulkProcessingThresholds)
            .setInitialMicroBatchSize(1);
    }

    @Override
    public void close() {
        Sinks.EmitResult result = Sinks.EmitResult.FAIL_TERMINATED;

        try {
            result = this.bulkInputEmitter.tryEmitError(
                new IllegalStateException("Cancelling ingestion for batch [" + this.identifier + "]."));
        } catch (Throwable t) {
            logger.info(
                "Failed to close input emitter for batch [{}}].",
                this.identifier,
                t);
        }

        logger.debug(
            "Closed input emitter for batch [{}}] - {}.",
            this.identifier,
            result.name());

        try {
            this.bulkWriterInputBoundedElastic.dispose();
        } catch (Throwable t) {
            logger.info(
                "Failed to dispose bulkWriterInputBoundedElastic of batch [{}}].",
                this.identifier,
                t);
        }

        try {
            this.bulkWriterResponsesBoundedElastic.dispose();
        } catch (Throwable t) {
            logger.info(
                "Failed to dispose bulkWriterResponsesBoundedElastic of batch [{}}].",
                this.identifier,
                t);
        }

        this.status.clearPendingOperations();
    }

    private void scheduleRetry(
        CosmosItemOperation cosmosItemOperation,
        Duration retryAfterDuration,
        Throwable cause) {

        OperationContext originalCtx = cosmosItemOperation.getContext();
        int retryCount = originalCtx.getRetryCount();
        if (retryCount > 20) { //TODO: What should the count be?
            logger.error("Bulk ingestion of item Batch [{}], ID [{}], PK [{}] failed [{}] times. "
                    + "No more retries - aborting the ingestion job.",
                this.identifier,
                originalCtx.getId(),
                cosmosItemOperation.getPartitionKeyValue(),
                retryCount,
                cause);

            this.status.addFailure(
                null,
                createFailure(cosmosItemOperation, cause));

            return;
        }

        CosmosItemOperation newOperation;
        if (cosmosItemOperation.getOperationType() == CosmosItemOperationType.UPSERT) {
            newOperation = CosmosBulkOperations.
                getUpsertItemOperation(
                    cosmosItemOperation.getItem(),
                    new PartitionKey(originalCtx.getId()),
                    null,
                    originalCtx.createForRetry());
        } else if (cosmosItemOperation.getOperationType() == CosmosItemOperationType.CREATE) {
            newOperation = CosmosBulkOperations.
                getCreateItemOperation(
                    cosmosItemOperation.getItem(),
                    new PartitionKey(originalCtx.getId()),
                    null,
                    originalCtx.createForRetry());
        } else if (cosmosItemOperation.getOperationType() == CosmosItemOperationType.DELETE) {
            newOperation = CosmosBulkOperations.
                getDeleteItemOperation(
                    cosmosItemOperation.getItem(),
                    new PartitionKey(originalCtx.getId()),
                    null,
                    originalCtx.createForRetry());
        } else {
            throw new IllegalStateException("Unsupported operation type '"
                + cosmosItemOperation.getOperationType()
                + "'.");
        }

        if (retryCount > 0 || retryAfterDuration != null) {
            // min 10ms per retry - max 1 second per retry
            int delayInMs = Math.max(
                10 * retryCount + rnd.nextInt( 990 * Math.max(1, retryCount)),
                retryAfterDuration != null ? Math.min((int)retryAfterDuration.toMillis(), 5000) : 0);
            logger.warn(
                "Item Batch {}, Id {} failed already {} times. Retrying again in {}ms.",
                originalCtx.getIdentifier(),
                originalCtx.getId(),
                originalCtx.getRetryCount(),
                delayInMs);

            Runnable retrySchedulingTask = () -> scheduleInternalWrite(newOperation);

            retryDelayScheduler.schedule(retrySchedulingTask, delayInMs, TimeUnit.MILLISECONDS);
        } else {
            // retry immediately
            scheduleInternalWrite(newOperation);
        }
    }

    public void scheduleWrite(CosmosItemOperation cosmosItemOperation, String id, JsonNode item) {
        if (this.status.getFlushCalled().get()) {
            throw new IllegalStateException("No more writes can be scheduled after calling flush.");
        }

        Objects.requireNonNull(cosmosItemOperation, "Argument 'cosmosItemOperation' must not be null.");

        boolean acquired = false;
        while(!acquired) {

            try {
                acquired = semaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn(
                    "Thread interrupted waiting for semaphore in BulkWriter - retrying preemptively...");
            }

            if (!acquired) {
                logger.debug("Unable to acquire permit. Retrying...");
            }
        }
        logger.debug("Acquired permit");
        inputBulkItems.put(id, item);
        scheduleInternalWrite(cosmosItemOperation);
    }

    private void ensureIngestionStarted() {
        if (this.processingFluxHolder.get() == null) {
            synchronized (this.processingFluxHolder) {
                if (this.processingFluxHolder.get() == null) {

                    Flux<Object> ingestionFlux = startIngestion();

                    if (!this.processingFluxHolder.compareAndSet(null, ingestionFlux)) {
                        throw new IllegalStateException(
                            "Unexpected race condition initializing ingestion thread for " + this.identifier);
                    }

                    ingestionFlux.subscribe();
                }
            }
        }
    }

    private void scheduleInternalWrite(CosmosItemOperation cosmosItemOperation) {
        boolean isRetry = cosmosItemOperation.<OperationContext>getContext().getRetryCount() > 0;
        if (isRetry) {
            bulkInputEmitter.emitNext(cosmosItemOperation, emitFailureHandler);
        } else {
            OperationContext ctx = cosmosItemOperation.getContext();
            synchronized (this.status.getLockObject()) {
                bulkInputEmitter.emitNext(cosmosItemOperation, emitFailureHandler);
                this.status.getOperationsScheduled().incrementAndGet();
                this.status.getPendingOperations().add(ctx.getId());
                this.ensureIngestionStarted();
            }
        }
    }

    public <T> Iterable<T> flush() {
        Flux<Object> ingestionFluxSnapshot;
        synchronized (this.status.getLockObject()) {
            this.status.getFlushCalled().set(true);

            ingestionFluxSnapshot = this.processingFluxHolder.get();
            if (ingestionFluxSnapshot == null) {
                logger.info(
                    "Batch {} - Flush called without any writes being scheduled or ingestion started.",
                    this.identifier);

                return (Iterable<T>) this.status.getGoodInputDocumentsSnapshot();
            }

            if (this.status.getOperationsScheduled().get() == 0) {
                logger.info(
                    "Batch {} - No more pending writes when flush was called. Ingested {} items.",
                    this.identifier,
                    this.status.getOperationsCompleted().get());

                return (Iterable<T>) this.status.getGoodInputDocumentsSnapshot();
            }

            logger.info(
                "Batch {} - Flush called - waiting for {} pending items.",
                this.identifier,
                this.status.getOperationsScheduled().get());
        }

        Instant lastSnapshot = Instant.EPOCH;
        this.lock.lock();
        while (true) {
            boolean finished = false;
            try {
                finished = this.flushCompletedCondition
                    .await(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.info(
                    "Batch {} was interrupted in flush. Continuing preemptively.",
                    this.identifier,
                    e);
            }

            if (finished) {
                logger.info("Flush completed for batch [{}].", this.identifier);
                return (Iterable<T>) this.status.getGoodInputDocumentsSnapshot();
            }

            if (Duration.between(lastSnapshot, Instant.now()).compareTo(Duration.ofMinutes(1)) > 0) {
                lastSnapshot = Instant.now();
                synchronized (this.status.getLockObject()) {
                    List<String> samples = this.status.getPendingOperationsSampleSnapshot(3);

                    String sampleSet = String.join(", ", samples);

                    logger.info(
                        "Batch {} still waiting for ingestion to complete. Operations "
                            + "pending {} (Sample set {})",
                        this.identifier,
                        this.status.getOperationsScheduled(),
                        sampleSet);
                }
            }
        }
    }

    private Flux<Object> startIngestion () {
        Flux<CosmosItemOperation> inputFlux = bulkInputEmitter
            .asFlux()
            .onBackpressureBuffer()
            .publishOn(bulkWriterInputBoundedElastic)
            .doOnError(t -> logger.error("Input publishing flux failed {}", this.identifier, t));

        return cosmosAsyncContainer
            .executeBulkOperations(inputFlux, bulkOptions)
            .flatMap(bulkOperationResponse -> {
                processBulkOperationResponse(
                    bulkOperationResponse.getResponse(),
                    bulkOperationResponse.getOperation(),
                    bulkOperationResponse.getException());
                return Mono.empty();
            })
            .onBackpressureBuffer()
            .publishOn(bulkWriterResponsesBoundedElastic)
            .doOnError(t -> {
                logger.error("Bulk execution flux failed {}", this.identifier, t);
                this.lock.lock();
                try {
                    this.flushCompletedCondition.signal();
                } finally {
                    this.lock.unlock();
                }
            })
            .doOnComplete(() -> {
                logger.debug("Ingestion for batch [{}] completed successfully.", this.identifier);
                this.lock.lock();
                try {
                    this.flushCompletedCondition.signal();
                } finally {
                    this.lock.unlock();
                }
            });
    }

    private void processBulkOperationResponse(
        CosmosBulkItemResponse itemResponse,
        CosmosItemOperation itemOperation,
        Exception exception) {

        if (itemResponse != null) {
            this.status.getRequestChargeTracker().addAndGet((long) (100L * itemResponse.getRequestCharge()));
        } else if (exception instanceof CosmosException) {
            this.status.getRequestChargeTracker().addAndGet((long) (100L * ((CosmosException)exception).getRequestCharge()));
        }

        if (exception != null) {
            handleException(itemOperation, exception);
        } else if (itemResponse != null) {
            processResponseCode(itemResponse, itemOperation);
        }
    }

    private void processResponseCode(
        CosmosBulkItemResponse itemResponse,
        CosmosItemOperation itemOperation) {

        OperationContext ctx = itemOperation.getContext();
        if (itemResponse.isSuccessStatusCode()) {
            markSuccess(ctx, itemOperation, itemResponse.getStatusCode());
        } else if (itemResponse.getStatusCode() == 409) {
            markSuccess(ctx, itemOperation, 409);
        } else if (itemResponse.getStatusCode() == 404 && itemOperation.getOperationType() == CosmosItemOperationType.DELETE) {
            markSuccess(ctx, itemOperation, 404);
        } else if (shouldRetry(itemResponse.getStatusCode())) {
            //re-scheduling
            scheduleRetry(itemOperation, itemResponse.getRetryAfterDuration(), null);
        } else {
            this.status.addFailure(itemOperation.getItem(), createFailure(itemOperation, null));

            logger.error(
                "Bulk ingestion of item Batch [{}], ID [{}], PK [{}] failed [{}] times. "
                    + "Most recent failures is not retriable.",
                this.identifier,
                ctx.getId(),
                itemOperation.getPartitionKeyValue(),
                ctx.getRetryCount());
        }
    }

    private static BulkImportFailure createFailure(
        CosmosItemOperation itemOperation,
        Throwable cause) {

        BulkImportFailure failure = new BulkImportFailure();
        failure.setDocumentIdsFailedToImport(
            List.of(itemOperation.<OperationContext>getContext().getId()));

        if (cause != null) {
            if (cause instanceof Exception) {
                failure.setBulkImportFailureException((Exception) cause);
            } else {
                failure.setBulkImportFailureException(new RuntimeException(cause.getMessage(), cause));
            }
        }

        Object doc = itemOperation.getItem();
        try {
            ObjectMapper mapper = new ObjectMapper();
            failure.setDocumentsFailedToImport(
                List.of(mapper.writeValueAsString(doc))
            );
        } catch (JsonProcessingException e) {
            failure.setDocumentsFailedToImport(List.of(e.toString()));
        }

        return failure;
    }

    private void handleException(CosmosItemOperation itemOperation, Exception exception) {
        OperationContext ctx = itemOperation.getContext();
        if (!(exception instanceof CosmosException)) {
            logger.error(
                "The operation for Item Batch [{}], ID: [{}], PK: [{}] encountered"
                    + " an unexpected failure, Retry will be attempted optimistically...",
                ctx.getIdentifier(),
                ctx.getId(),
                itemOperation.getPartitionKeyValue(),
                exception);
            scheduleRetry(itemOperation, null, exception);
        } else {
            CosmosException cosmosException = (CosmosException) exception;
            if (cosmosException.getStatusCode() == 409) {
                // handle as success
                this.markSuccess(ctx, itemOperation, 409);
            } else if (shouldRetry(cosmosException.getStatusCode())) {
                scheduleRetry(itemOperation, cosmosException.getRetryAfterDuration(), exception);
            } else  {
                this.status.addFailure(itemOperation.getItem(), createFailure(itemOperation, cosmosException));

                logger.error(
                    "Bulk ingestion of item Batch [{}], ID [{}], PK [{}] failed [{}] times. "
                        + "Most recent failures is not retriable.",
                    this.identifier,
                    ctx.getId(),
                    itemOperation.getPartitionKeyValue(),
                    ctx.getRetryCount(),
                    cosmosException);
            }
        }
    }

    private void markSuccess(OperationContext ctx, CosmosItemOperation itemOperation, int statusCode) {

        long scheduledCountSnapshot;
        long completedCountSnapshot;
        synchronized (this.status.getLockObject()) {
            if (!this.status.getPendingOperations().remove(ctx.getId())) {
                logger.warn(
                    "No pending operation found for item Batch [{}], ID: [{}], PK: [{}]",
                    ctx.getIdentifier(),
                    ctx.getId(),
                    itemOperation.getPartitionKeyValue());
            }

            scheduledCountSnapshot = this.status.getOperationsScheduled().decrementAndGet();
            completedCountSnapshot = this.status.getOperationsCompleted().incrementAndGet();
            this.status.addSuccess(inputBulkItems.get(ctx.getId()));

            if (scheduledCountSnapshot == 0 && this.status.getFlushCalled().get()) {
                this.bulkInputEmitter.emitComplete(emitFailureHandler);

                logger.info(
                    "Ingestion completed for Batch [{}] [{}] items ingested.",
                    ctx.getIdentifier(),
                    completedCountSnapshot);

                return;
            }
        }

        logger.debug(
            "The operation for Item Batch [{}], ID: [{}], PK: [{}] completed successfully " +
                "with a response status code: [{}]",
            ctx.getIdentifier(),
            ctx.getId(),
            itemOperation.getPartitionKeyValue(),
            statusCode);
    }

    private boolean shouldRetry(int statusCode) {
        return statusCode == 408 ||
            statusCode == 429 ||
            statusCode == 503 ||
            statusCode == 500 ||
            statusCode == 449 ||
            statusCode == 410;
    }
}
