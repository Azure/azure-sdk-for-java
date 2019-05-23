package com.azure.core.polling;


public interface Poller {

    PollRequestData getPollRequestData();

    boolean isDone();

    /**This will cancel polling from Azure Service if supported by service ***/
    PollResponse cancelOperation() throws PollingException;

    /**This will poll once**/
    PollResponse poll();

    /**This will keep polling until it is done.**/
    PollResponse pollUntilDone();

    /**This will stop polling**/
    void stopPolling();
}
