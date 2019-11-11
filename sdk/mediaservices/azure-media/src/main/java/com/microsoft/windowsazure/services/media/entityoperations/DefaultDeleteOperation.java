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

/**
 * Generic implementation of Delete operation usable by most entities
 * 
 */
public class DefaultDeleteOperation implements EntityDeleteOperation {
    private final EntityOperationBase.EntityUriBuilder uriBuilder;
    private EntityProxyData proxyData;

    /**
     * 
     */
    public DefaultDeleteOperation(String entityUri, String entityId) {
        uriBuilder = new EntityOperationBase.EntityIdUriBuilder(entityUri,
                entityId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityDeleteOperation
     * #setProxyData(com.microsoft.windowsazure.services.media
     * .entityoperations.EntityProxyData)
     */
    @Override
    public void setProxyData(EntityProxyData proxyData) {
        this.proxyData = proxyData;
    }

    /**
     * Get currently set proxy data
     * 
     * @return the proxyData
     */
    protected EntityProxyData getProxyData() {
        return proxyData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entities.EntityDeleteOperation
     * #getUri()
     */
    @Override
    public String getUri() {
        return uriBuilder.getUri();
    }
}
