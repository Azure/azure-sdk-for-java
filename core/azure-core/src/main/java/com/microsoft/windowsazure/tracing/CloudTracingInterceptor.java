/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.tracing;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.util.HashMap;

/**
 * The CloudTracingInterceptor provides useful information about cloud
 * operations. Interception is global and a tracing interceptor can be added via
 * CloudContext.Configuration.Tracing.AddTracingInterceptor.
 */
public interface CloudTracingInterceptor {
    /**
     * Trace information.
     * 
     * @param message
     *            The information to trace.
     */
    void information(String message);

    /**
     * Probe configuration for the value of a setting.
     * 
     * @param source
     *            The configuration source.
     * @param name
     *            The name of the setting.
     * @param value
     *            The value of the setting in the source.
     */
    void configuration(String source, String name, String value);

    /**
     * Enter a method.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param instance
     *            The instance with the method.
     * @param method
     *            Name of the method.
     * @param parameters
     *            Method parameters.
     */
    void enter(String invocationId, Object instance, String method,
            HashMap<String, Object> parameters);

    /**
     * Send an HTTP request.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param request
     *            The request about to be sent.
     */
    void sendRequest(String invocationId, HttpRequest request);

    /**
     * Receive an HTTP response.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param response
     *            The response instance.
     */
    void receiveResponse(String invocationId, HttpResponse response);

    /**
     * Raise an error.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param exception
     *            The error.
     */
    void error(String invocationId, Exception exception);

    /**
     * Exit a method. Note: Exit will not be called in the event of an error.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param returnValue
     *            Method return value.
     */
    void exit(String invocationId, Object returnValue);
}