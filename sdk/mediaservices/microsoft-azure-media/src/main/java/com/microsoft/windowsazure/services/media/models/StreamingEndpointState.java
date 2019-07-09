/*
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

package com.microsoft.windowsazure.services.media.models;

import java.security.InvalidParameterException;

/**
 * Specifies the states of the se.
 */
public enum StreamingEndpointState {

    Stopped("Stopped"),

    Starting("Starting"),
    
    Running("Running"),
    
    Scaling("Scaling"),
    
    Stopping("Stopping");

    /** The se state code. */
    private String streamingEndpointStateCode;

    /**
     * Instantiates a new se state.
     * 
     * @param streamingEndpointStateCode
     *            the se state code
     */
    private StreamingEndpointState(String streamingEndpointStateCode) {
        this.streamingEndpointStateCode = streamingEndpointStateCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public String getCode() {
        return streamingEndpointStateCode;
    }

    /**
     * Create an StreamingEndpointState instance from the corresponding int.
     * 
     * @param state
     *            state as integer
     * @return new StreamingEndpointState instance
     */
    public static StreamingEndpointState fromCode(String state) {
        if (state.equals("Stopped")) {
            return StreamingEndpointState.Stopped;
        } else if (state.equals("Starting")) {
            return StreamingEndpointState.Starting;
        } else if (state.equals("Running")) {
            return StreamingEndpointState.Running;
        } else if (state.equals("Scaling")) {
           return StreamingEndpointState.Scaling;
        } else if (state.equals("Stopping")) {
           return StreamingEndpointState.Stopping;
        } else {
            throw new InvalidParameterException("state");
        }
    }
}
