// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

public class QuotaExceededException extends EventHubException {

    public QuotaExceededException(String message) {
        super(false, message);
    }

    public QuotaExceededException(Throwable cause) {
        super(false, cause);
    }

}
