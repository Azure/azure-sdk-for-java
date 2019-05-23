// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;

/**
 * All AmqpExceptions - which EventHub client handles internally. 
 */
public class AmqpException extends Exception {
    private static final long serialVersionUID = -750417419234273714L;
    private transient ErrorCondition errorCondition;

    public AmqpException(ErrorCondition errorCondition) {
        super(errorCondition.getDescription());
        this.errorCondition = errorCondition;
    }

    public ErrorCondition getError() {
        return this.errorCondition;
    }
}
