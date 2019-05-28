package com.azure.core.polling;

public class PollerOptions {
    private int timeoutInMilliSeconds ;

    private int pollIntervalInMillis;

    /*This will ensure that poll interval grow exponentially by factor specified here.
     * This should always be more than 1.*/
    private float pollIntervalGrowthFactor =1.0f;

    /*We will call this when we are done*/
    private Runnable callbackWhenDone;

    /* We will call this to cancel the operation*/
    private Runnable callbackToCancelOperation;

    /**
     * @param timeoutInMilliSeconds This is the max time after that poller will giveup and not poll anymore.
     * @param pollIntervalInMillis  This will ensure that poll happens only once in pollIntervalInMillis
     * @param pollIntervalGrowthFactor  This will ensure that poll interval grow exponentially by factor specified here.
     *                                  This should always be more than 1.
     * **/
    public PollerOptions( int timeoutInMilliSeconds
                         ,int pollIntervalInMillis
                         ,float pollIntervalGrowthFactor){
        this.timeoutInMilliSeconds = timeoutInMilliSeconds;
        this.pollIntervalInMillis = pollIntervalInMillis;
        this.pollIntervalGrowthFactor = pollIntervalGrowthFactor;
    }
    public int getTimeoutInMilliSeconds(){
        return timeoutInMilliSeconds;
    }
    public int getPollIntervalInMillis (){
        return pollIntervalInMillis;
    }
    public float getPollIntervalGrowthFactor(){
        return pollIntervalGrowthFactor;
    }
}
