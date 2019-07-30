// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.CancellationToken;
import com.azure.data.cosmos.internal.changefeed.CancellationTokenSource;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverCloseReason;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverContext;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.LeaseRenewer;
import com.azure.data.cosmos.internal.changefeed.PartitionProcessor;
import com.azure.data.cosmos.internal.changefeed.PartitionSupervisor;
import com.azure.data.cosmos.internal.changefeed.exceptions.LeaseLostException;
import com.azure.data.cosmos.internal.changefeed.exceptions.ObserverException;
import com.azure.data.cosmos.internal.changefeed.exceptions.PartitionSplitException;
import com.azure.data.cosmos.internal.changefeed.exceptions.TaskCancelledException;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation for {@link PartitionSupervisor}.
 */
class PartitionSupervisorImpl implements PartitionSupervisor, Closeable {
    private final Lease lease;
    private final ChangeFeedObserver observer;
    private final PartitionProcessor processor;
    private final LeaseRenewer renewer;
    private CancellationTokenSource renewerCancellation;
    private CancellationTokenSource processorCancellation;

    private RuntimeException resultException;

    private ExecutorService executorService;

    public PartitionSupervisorImpl(Lease lease, ChangeFeedObserver observer, PartitionProcessor processor, LeaseRenewer renewer, ExecutorService executorService) {
        this.lease = lease;
        this.observer = observer;
        this.processor = processor;
        this.renewer = renewer;
        this.executorService = executorService;

        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(3);
        }
    }

    @Override
    public Mono<Void> run(CancellationToken shutdownToken) {
        PartitionSupervisorImpl self = this;
        this.resultException = null;

        ChangeFeedObserverContext context = new ChangeFeedObserverContextImpl(self.lease.getLeaseToken());

        self.observer.open(context);

        ChangeFeedObserverCloseReason closeReason = ChangeFeedObserverCloseReason.UNKNOWN;

        try {
            self.processorCancellation = new CancellationTokenSource();

            Thread processorThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    self.processor.run(self.processorCancellation.getToken()).block();
                }
            });

            self.renewerCancellation = new CancellationTokenSource();

            Thread renewerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    self.renewer.run(self.renewerCancellation.getToken()).block();
                }
            });

            self.executorService.execute(processorThread);
            self.executorService.execute(renewerThread);

            while (!shutdownToken.isCancellationRequested() && self.processor.getResultException() == null && self.renewer.getResultException() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException iex) {
                    break;
                }
            }

            this.processorCancellation.cancel();
            this.renewerCancellation.cancel();

            if (self.processor.getResultException() != null) {
                throw self.processor.getResultException();
            }

            if (self.renewer.getResultException() != null) {
                throw self.renewer.getResultException();
            }

            closeReason = shutdownToken.isCancellationRequested() ?
                ChangeFeedObserverCloseReason.SHUTDOWN :
                ChangeFeedObserverCloseReason.UNKNOWN;

        } catch (LeaseLostException llex) {
            closeReason = ChangeFeedObserverCloseReason.LEASE_LOST;
            self.resultException = llex;
        } catch (PartitionSplitException pex) {
            closeReason = ChangeFeedObserverCloseReason.LEASE_GONE;
            self.resultException = pex;
        } catch (TaskCancelledException tcex) {
            closeReason = ChangeFeedObserverCloseReason.SHUTDOWN;
            self.resultException = null;
        } catch (ObserverException oex) {
            closeReason = ChangeFeedObserverCloseReason.OBSERVER_ERROR;
            self.resultException = oex;
        } catch (Exception ex) {
            closeReason = ChangeFeedObserverCloseReason.UNKNOWN;
        } finally {
            self.observer.close(context, closeReason);
        }

        if (self.resultException != null) {
            return Mono.error(self.resultException);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    @Override
    public void close() throws IOException {
        if (this.processorCancellation != null) {
            this.processorCancellation.close();
        }

        this.renewerCancellation.close();
    }
}
