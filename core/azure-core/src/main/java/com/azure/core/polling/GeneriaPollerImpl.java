package com.azure.core.polling;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class GeneriaPollerImpl extends GenerialPoller {

    public GeneriaPollerImpl(PollRequestData pollRequestData ){
        super(pollRequestData);
    }

    /**This will deserialize the string data into poller**/
    static Poller deserializePoller(String serializedPollReqData) {
        Poller poller = null;
        try {
            byte b[] = serializedPollReqData.getBytes();
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            PollRequestData pollRequestData = (PollRequestData) si.readObject();
            poller = new GeneriaPollerImpl(pollRequestData);
        } catch (Exception e) {
            System.out.println(e);
        }
        return poller;
    }
}
