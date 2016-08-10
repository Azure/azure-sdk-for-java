package com.microsoft.azure;

import com.microsoft.rest.ServiceCall;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Type represents a set of REST calls running possibly in parallel.
 */
public class ParallelServiceCall<T> extends ServiceCall<T> {
    private ConcurrentLinkedQueue<ServiceCall<?>> serviceCalls;

    /**
     * Creates a ParallelServiceCall.
     */
    public ParallelServiceCall() {
        super(null);
        this.serviceCalls = new ConcurrentLinkedQueue<>();
    }

    /**
     * Cancels all the service calls currently executing.
     */
    public void cancel() {
        for (ServiceCall<?> call : this.serviceCalls) {
            call.cancel();
        }
    }

    /**
     * @return true if the call has been canceled; false otherwise.
     */
    public boolean isCancelled() {
        for (ServiceCall<?> call : this.serviceCalls) {
            if (!call.isCanceled()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a call to the list of parallel calls.
     *
     * @param call the call
     */
    public void addCall(ServiceCall<?> call) {
        this.serviceCalls.add(call);
    }
}
