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

package com.microsoft.windowsazure.services.media.entityoperations;

public interface EntityDeleteOperation {

    /**
     * Supplies the current proxy information to the action.
     * 
     * @param proxyData
     */
    void setProxyData(EntityProxyData proxyData);

    /**
     * Get the URI to use to delete an entity
     * 
     * @return The uri
     */
    String getUri();
}
