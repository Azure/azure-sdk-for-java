package com.azure.core.polling;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class Poller {

    private PollRequestData pollRequestData;

    /*This will save last poll response.*/
    private PollResponse pollResponse;

    public Poller(PollRequestData pollRequestData ){
        this.pollRequestData = pollRequestData;
    }

    public PollRequestData getPollRequestData() {
        return pollRequestData;
    }

    public boolean isDone() {
        //First time we will not have poll response data.
        if (pollRequestData == null)
            return false;
        return !pollResponse.isOperationInProgress();
    }

    /**This will cancel polling from Azure Service if supported by service ***/
    public PollResponse cancelOperation() {
        return pollRequestData.callbackToCancelOperation().cancel();
    }

    //TODO : Make sure we do not pool every cpu cycle. Polling must be throttle by parameter defined in PollingType i.e interval or expeonential polling
    /**This will poll once**/
    public PollResponse pollOnce() {
        pollRequestData.setStopPolling(false);
        return pollRequestData.serviceSupplier.get();
    }

    /**This will keep polling until it is done.**/
    public PollResponse pollUntilDone() {
        boolean done = false;
        pollRequestData.setStopPolling(false);
        while (!isDone() && !pollRequestData.isPollingStopped()) {
            pollResponse =pollRequestData.serviceSupplier().get();
        }
        if (pollRequestData.callbackWhenDone() != null) {
            try {
                pollRequestData.callbackWhenDone().execute(pollResponse);
            }catch (Exception ex){
                //TODO handle Exception
            }
        }
        return pollResponse;
    }

    /**This will stop polling**/
    public void  stopPolling() {
        pollRequestData.setStopPolling(true);
    }

    static String serializePoller(Poller poller) {
        String serializedObject = "";
        try {
            ByteArrayOutputStream bArrOutStream = new ByteArrayOutputStream();
            ObjectOutputStream objOutStream = new ObjectOutputStream(bArrOutStream);
            objOutStream.writeObject(poller.getPollRequestData());
            objOutStream.flush();
            serializedObject = new String(Base64.getEncoder().encode(bArrOutStream.toByteArray()));
        } catch (Exception ex) {

        }
        return serializedObject;
    }

    /**This will deserialize the string data into poller**/
    static Poller deserializePoller(String serializedPollReqData) {
        Poller poller = null;
        try {
            byte b[] = Base64.getDecoder().decode(serializedPollReqData.getBytes());
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            PollRequestData pollRequestData = (PollRequestData) si.readObject();
            poller = new Poller(pollRequestData);
        } catch (Exception e) {
            System.out.println(e);
        }
        return poller;
    }
}
