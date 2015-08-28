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

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;
import java.util.List;

import com.microsoft.windowsazure.services.media.entityoperations.EntityWithOperationIdentifier;
import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.CrossSiteAccessPoliciesType;
import com.microsoft.windowsazure.services.media.implementation.content.StreamingEndpointAccessControlType;
import com.microsoft.windowsazure.services.media.implementation.content.StreamingEndpointCacheControlType;
import com.microsoft.windowsazure.services.media.implementation.content.StreamingEndpointType;

/**
 * Data about a Media Services Asset entity.
 * 
 */
public class StreamingEndpointInfo extends ODataEntity<StreamingEndpointType>
        implements EntityWithOperationIdentifier {
    
    private String operationIdentifier = null;

    /**
     * Instantiates a new streaming end point info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public StreamingEndpointInfo(EntryType entry, StreamingEndpointType content) {
        super(entry, content);
    }

    /**
     * Get the streaming end point id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Get the streaming end point name.
     * 
     * @return the name
     */
    public String getName() {
        return this.getContent().getName();
    }
    
    /**
     * Get the streaming end point description.
     * 
     * @return the description
     */
    public String getDescription() {
        return this.getContent().getDescription();
    }
    
    /**
     * Get the creation date.
     * 
     * @return the date
     */
    public Date getCreated() {
        return this.getContent().getCreated();
    }

    /**
     * Get last modified date.
     * 
     * @return the date
     */
    public Date getLastModified() {
        return getContent().getLastModified();
    }
    
    /**
     * Get the streaming end point state.
     * 
     * @return the state
     */
    public StreamingEndpointState getState() {
        return StreamingEndpointState.fromCode(getContent().getState());
    }

    /**
     * Get the host name
     * 
     * @return the host name
     */
    public String getHostName() {
        return getContent().getHostName();
    }
    
    /**
     * Get the scale units
     * 
     * @return the scale units
     */
    public int getScaleUnits() {
        return getContent().getScaleUnits();
    }
    
    /**
     * Get the list of custom host names.
     * 
     * @return the id
     */
    public List<String> getCustomHostNames() {
        return getContent().getCustomHostName();
    }
    
    /**
     * True if CDN is enabled.
     */
    public boolean isCdnEnabled() {
        return getContent().isCdnEnabled();
    }

    /**
     * Get the access control policy
     * 
     * @return the access control policy
     */
    public StreamingEndpointAccessControlType getAccessControl() {
        return getContent().getAccessControl();
    }

    /**
     * Get the cache control policy
     * 
     * @return the cahe control policy
     */
    public StreamingEndpointCacheControlType getCacheControl() {
        return getContent().getCacheControl();
    }

    /**
     * Get the cross site access policy
     * 
     * @return the cross site access policy
     */
    public CrossSiteAccessPoliciesType getCrossSiteAccessPolicies() {
        return getContent().getCrossSiteAccessPolicies();
    }

    /**
     * Get the operation-id if any.
     */
    @Override
    public String getOperationId() {        
        return operationIdentifier;
    }

    /**
     * Set the operation-id.
     */
    @Override
    public void setOperationId(String operationIdentifier) {
        this.operationIdentifier = operationIdentifier;        
    }

    /**
     * @return true if the entity has an operation-id.
     */
    @Override
    public boolean hasOperationIdentifier() {
        return operationIdentifier != null;
    }    
}
