package com.azure.core.polling;


import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class Poller<T>{

    private static final long serialversionUID =139448132L;

    T response =null;
    private Supplier<T> serviceSupplier;
    private PollerOptions pollerOptions;

    /*This will save last poll response.*/
    private PollResponse pollResponse;
    private Runnable callbackToCancelOperation;

    /*If consumer do not want to poll. This will not stop the Service Operation.*/
    private boolean stopPolling;

    /**
     * @param pollerOptions .
     * @param serviceSupplier
     * @param callbackToCancelOperation
     * **/
    public Poller(PollerOptions pollerOptions
                            , Supplier<T> serviceSupplier
                            , Runnable callbackToCancelOperation ){

        this.pollerOptions = pollerOptions;
        this.callbackToCancelOperation = callbackToCancelOperation;
        this.serviceSupplier = serviceSupplier;
    }

    public boolean isDone() {
        return pollResponse != null && pollResponse.isDone() ;
    }

    /**This will cancel polling from Azure Service if supported by service ***/
    public void cancelOperation() {
       if (callbackToCancelOperation!= null) new Thread(callbackToCancelOperation).start();
    }

    //TODO : Make sure we do not pool every cpu cycle. Polling must be throttle by parameter defined in PollingType i.e interval or expeonential polling
    /**This will poll once. If you had stopped polling erlier, we will enable polling again.**/
    public Mono<T> pollOnce() {
        return Mono.defer(() -> {
            setStopPolling(false);
            return Mono.just(serviceSupplier.get());
        });
    }

    /**This will keep polling until it is done.**/
    public Mono<T> pollUntilDone() {
        return Mono.defer(() -> {
            setStopPolling(false);
            while (!isDone() && !pollingStopped()) {
                System.out.println("Poller.pollUntilDone Invoking Azure Service , checking Operation status");
                response = serviceSupplier.get();
            }
            return Mono.just(response);
       });
    }

    /**This will stop polling**/
    public void  stopPolling() {
       setStopPolling(true);
    }

    private void setStopPolling(boolean stop){
        this.stopPolling =stop;
    }

    public boolean pollingStopped(){
        return this.stopPolling;
    }
    public PollResponse.OperationStatus status (){
        return pollResponse!=null?pollResponse.status():null;
    }
/*
    static String serializePoller(Poller poller) {
        String serializedObject = "";
        try {
            ByteArrayOutputStream bArrOutStream = new ByteArrayOutputStream();
            ObjectOutputStream objOutStream = new ObjectOutputStream(bArrOutStream);
            objOutStream.writeObject(poller);
            objOutStream.flush();
            serializedObject = new String(Base64.getEncoder().encode(bArrOutStream.toByteArray()));
        } catch (Exception ex) {
            //TODO Handle Exception
        }
        return serializedObject;
    }

    static Poller deserializePoller(String serializedPoller) {
        Poller poller = null;
        try {
            byte b[] = Base64.getDecoder().decode(serializedPoller.getBytes());
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            poller = (Poller) si.readObject();
        } catch (Exception e) {
            //TODO Handle Exception
        }
        return poller;
    }
    */
}
