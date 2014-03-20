/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.microsoft.windowsazure.core.tracing.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import com.microsoft.windowsazure.tracing.CloudTracingInterceptor;

/**
 * 
 * @author andrerod
 *
 */
/// <summary>
/// Implementation for CloudTracingInterceptor that works using log4net framework.
/// </summary>
public class JavaTracingInterceptor implements CloudTracingInterceptor {
    private Logger logger;
    
    /**
     * Initializes a new instance of the JavaTracingInterceptor.
     */
    public JavaTracingInterceptor() {
        logger = Logger.getLogger(this.getClass().getName());
    }
    
    /**
     * Trace information.
     * 
     * @param message
     *            The information to trace.
     */
    public void information(String message) {
        logger.log(Level.INFO, message);
    }

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
    public void configuration(String source, String name, String value) {
        logger.log(Level.CONFIG, String.format("Configuration: source=%s, name=%s, value=%s", source, name, value));
    }

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
    public void enter(String invocationId, Object instance, String method,
            HashMap<String, Object> parameters) {
        ArrayList<Object> valuesList = new ArrayList<Object>(parameters.values());
        logger.entering(instance.getClass().getName(), method, valuesList.toArray());
    }

    /**
     * Send an HTTP request.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param request
     *            The request about to be sent.
     */
    public void sendRequest(String invocationId, HttpRequest request) {
        String requestAsString = request == null ? "" : request.toString();
        logger.log(Level.INFO, String.format("invocationId: %s\r\nrequest: %s", invocationId, requestAsString));
    }

    /**
     * Receive an HTTP response.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param response
     *            The response instance.
     */
    public void receiveResponse(String invocationId, HttpResponse response) {
        String responseAsString = response == null ? "" : response.toString();
        logger.log(Level.INFO, String.format("invocationId: %s\r\nrequest: %s", invocationId, responseAsString));
    }

    /**
     * Raise an error.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param exception
     *            The error.
     */
    public void error(String invocationId, Exception exception) {
        logger.log(Level.SEVERE, String.format("invocationId: %s", invocationId), exception);
    }

    /**
     * Exit a method. Note: Exit will not be called in the event of an error.
     * 
     * @param invocationId
     *            Method invocation identifier.
     * @param returnValue
     *            Method return value.
     */
    public void exit(String invocationId, Object returnValue) {
        logger.log(Level.INFO, String.format("Exit with invocation id %s, the return value is %s",
                invocationId,
                returnValue));
    }
}