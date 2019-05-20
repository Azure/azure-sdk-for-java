// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;

public interface IAmqpLink {
    /**
     * @param completionException completionException=null if open is successful
     */
    void onOpenComplete(Exception completionException);

    void onError(Exception exception);

    void onClose(ErrorCondition condition);
}
