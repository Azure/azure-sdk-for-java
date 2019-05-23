package com.azure.core.polling;

import java.io.Serializable;

public interface PollResponse extends Serializable {

    boolean isOperationComplete();

}
