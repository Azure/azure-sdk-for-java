package com.azure.core.polling;

import java.io.Serializable;

public interface PollerCallback extends Serializable {

    public void execute(PollResponse response);

    /** This function will be called when Service Operation needs to be cancelled.**/
    public PollResponse cancel();
}
