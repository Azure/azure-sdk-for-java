// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class LogsQueryErrorDetails {
    private final String message;
    private final String code;
    private final String target;

    /**
     * @param message
     * @param code
     * @param target
     */
    public LogsQueryErrorDetails(String message, String code, String target) {

        this.message = message;
        this.code = code;
        this.target = target;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * @return
     */
    public String getTarget() {
        return target;
    }
}
