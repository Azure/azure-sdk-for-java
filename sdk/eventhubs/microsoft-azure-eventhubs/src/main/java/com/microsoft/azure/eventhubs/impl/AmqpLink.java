// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;

public interface AmqpLink {
    /**
     * @param completionException completionException=null if open is successful
     */
    void onOpenComplete(Exception completionException);

    void onError(Exception exception, String failingLinkName);

    void onClose(ErrorCondition condition, String errorContext);
}
