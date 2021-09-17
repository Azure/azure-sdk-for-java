// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth.implementation;

import org.springframework.cloud.sleuth.Span;
import org.springframework.util.StringUtils;

public final class AmqpTraceUtil {

    private AmqpTraceUtil() { }

    /**
     * Parses a Sleuth Status from AMQP Error Condition.
     *
     * @param span the span to set the status for.
     * @param statusMessage AMQP description for this error condition.
     * @param throwable the error occurred during response transmission (optional).
     * @return the corresponding Sleuth {@link Span}.
     */
    public static Span parseStatusMessage(Span span, String statusMessage, Throwable throwable) {
        if (throwable != null) {
            return span.error(throwable);
        }

        if (StringUtils.hasText(statusMessage)) {
            return span.tag("status_message", statusMessage);
        }
        return span;
    }
}
