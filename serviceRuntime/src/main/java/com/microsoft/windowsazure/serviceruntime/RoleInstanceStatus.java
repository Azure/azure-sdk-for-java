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
 * Defines role instance status values, such as Busy and Ready.
 * <p>
 * The {@link RoleEnvironment#setStatus} method uses a value from this
 * enumeration.
 */
public enum RoleInstanceStatus {
    /**
     * The role instance is unavailable for requests.
     */
    Busy,
    /**
     * The role instance is ready to accept requests.
     */
    Ready
}
