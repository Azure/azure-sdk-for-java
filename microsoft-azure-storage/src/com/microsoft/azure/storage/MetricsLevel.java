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
package com.microsoft.azure.storage;

/**
 * Enumeration representing the state of metrics collection in a service.
 */
public enum MetricsLevel {
    /**
     * Metrics collection is disabled.
     */
    DISABLED,

    /**
     * Service-level metrics collection is enabled.
     */
    SERVICE,

    /**
     * Service-level and API metrics collection are enabled.
     */
    SERVICE_AND_API;
}
