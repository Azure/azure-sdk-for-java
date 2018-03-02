/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;

public interface AmqpLink {
    /**
     * @param completionException completionException=null if open is successful
     */
    void onOpenComplete(Exception completionException);

    void onError(Exception exception);

    void onClose(ErrorCondition condition);
}
