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

package com.microsoft.windowsazure.core;

import com.microsoft.windowsazure.exception.ServiceException;

/**
 * Exception indicating a service operation has timed out.
 */
public class ServiceTimeoutException extends ServiceException {

    private static final long serialVersionUID = 6612846403178749361L;

    /**
     * Construct a ServiceTimeoutException instance with default parameters.
     */
    public ServiceTimeoutException() {
    }

    /**
     * Construct a ServiceTimeoutException instance with the specified message.
     * 
     * @param message
     *            Exception message
     */
    public ServiceTimeoutException(String message) {
        super(message);
    }

    /**
     * Construct a ServiceTimeoutException instance with specified message and
     * cause
     * 
     * @param message
     *            Exception message
     * @param cause
     *            Exception that caused this exception to occur
     */
    public ServiceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a ServiceTimeoutException instance with the specified cause.
     * 
     * @param cause
     *            Exception that caused this exception to occur
     */
    public ServiceTimeoutException(Throwable cause) {
        super(cause);
    }
}
