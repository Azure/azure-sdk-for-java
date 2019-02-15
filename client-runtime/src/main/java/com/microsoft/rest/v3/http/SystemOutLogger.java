/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

/**
 * A HttpPipeline HttpPipelinePolicy logger that logs to the StdOut/System.out stream.
 */
public class SystemOutLogger extends AbstractHttpPipelineLogger {
    @Override
    public void log(HttpPipelineLogLevel logLevel, String message, Object... formattedArguments) {
        System.out.println(logLevel + ") " + format(message, formattedArguments));
    }
}
