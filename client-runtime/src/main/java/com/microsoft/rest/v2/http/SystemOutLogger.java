/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

/**
 * A HttpPipeline RequestPolicy logger that logs to the StdOut/System.out stream.
 */
public class SystemOutLogger extends HttpPipeline.AbstractLogger {
    @Override
    public void log(HttpPipeline.LogLevel logLevel, String message, Object... formattedArguments) {
        System.out.println(logLevel + ") " + format(message, formattedArguments));
    }
}
