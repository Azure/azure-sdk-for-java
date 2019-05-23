package com.azure.core.polling;

import java.io.Serializable;
import java.util.function.Supplier;

public class PollRequestData implements Serializable {
    private static final long serialversionUID =139448132L;

    /**This is the max time after that poller will giveup and not poll anymore**/
    private int timeoutInMilliSeconds;

    private PollType pollType;

    SerializableSupplier<PollResponse> serviceSupplier;

    /*We will call this when we are done*/
    private PollerCallback callbackWhenDone;

    /* We will call this to cancel the operation*/
    private PollerCallback callbackToCancelOperation;

    /**If consumer do not want to poll. This will not stop the Service Operation.**/
    private boolean stopPolling;

    public PollRequestData(int timeoutInMilliSeconds, SerializableSupplier<PollResponse> serviceSupplier
                                                    , PollerCallback callbackWhenDone
                                                    , PollerCallback callbackToCancelOperation){
        this.timeoutInMilliSeconds = timeoutInMilliSeconds;
        this.callbackWhenDone = callbackWhenDone;
        this.callbackToCancelOperation = callbackToCancelOperation;
        this.serviceSupplier = serviceSupplier;

    }
    enum PollType implements Serializable{
        FIXED_INTERVAL, EXPONENTIAL;
        private static final long serialversionUID =119448131L;
    }

    public PollerCallback callbackToCancelOperation(){
        return callbackToCancelOperation;
    }

    public PollerCallback callbackWhenDone(){
        return callbackWhenDone;
    }
    public SerializableSupplier<PollResponse> serviceSupplier(){
        return serviceSupplier;
    }

    void setStopPolling(boolean stop){
        this.stopPolling =stop;
    }
    public boolean isPollingStopped(){
        return this.stopPolling;
    }
}
