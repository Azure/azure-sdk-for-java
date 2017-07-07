/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

public class QuotaExceededException extends EventHubException {

    public QuotaExceededException(String message) {
        super(false, message);
    }

    public QuotaExceededException(Throwable cause) {
        super(false, cause);
    }

}
