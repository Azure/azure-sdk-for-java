package com.azure.core.polling;

import java.io.Serializable;

public interface PollerCallback extends Serializable {

    public void execute(PollResponse response);

    /** @Return boolean: Indicate if successfully able to cancel operation**/
    public PollResponse cancel();
}
