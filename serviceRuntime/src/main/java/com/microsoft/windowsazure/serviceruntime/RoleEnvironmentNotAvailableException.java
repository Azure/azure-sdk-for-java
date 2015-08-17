/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

/**
 * Represents an exception indicating the role environment is not available.
 */
public class RoleEnvironmentNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = -6218741025124056882L;

    /**
     * Creates an instance of the
     * <code>RoleEnvironmentNotAvailableException</code> class.
     * 
     * @param t
     *            A <code>java.lang.Throwable</code> object that represents the
     *            cause for the exception.
     * 
     */

    public RoleEnvironmentNotAvailableException(Throwable t) {
        initCause(t);
    }
}
