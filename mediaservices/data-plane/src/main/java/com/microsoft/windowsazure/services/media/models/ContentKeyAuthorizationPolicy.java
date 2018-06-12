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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityLinkOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUnlinkOperation;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Content Key Authorization Policy
 * entities.
 * 
 */
public final class ContentKeyAuthorizationPolicy {

    private static final String ENTITY_SET = "ContentKeyAuthorizationPolicies";

    private ContentKeyAuthorizationPolicy() {
    }

    /**
     * Creates an operation to create a new ContentKeyAuthorizationPolicy
     * 
     * @param name
     *            name of the content key authorization policy
     * @return The operation
     */
    public static EntityCreateOperation<ContentKeyAuthorizationPolicyInfo> create(String name) {
        return new Creator(name);
    }

    private static class Creator extends EntityOperationSingleResultBase<ContentKeyAuthorizationPolicyInfo>
            implements EntityCreateOperation<ContentKeyAuthorizationPolicyInfo> {

        private String name;

        public Creator(String name) {

            super(ENTITY_SET, ContentKeyAuthorizationPolicyInfo.class);

            this.name = name;
        }

        @Override
        public Object getRequestContents() {
            return new ContentKeyAuthorizationPolicyType().setName(name);
        }

    }

    /**
     * Create an operation that will retrieve the given content key
     * authorization policy
     * 
     * @param contentKeyAuthorizationPolicyId
     *            id of content key authorization policy to retrieve
     * @return the operation
     */
    public static EntityGetOperation<ContentKeyAuthorizationPolicyInfo> get(String contentKeyAuthorizationPolicyId) {
        return new DefaultGetOperation<ContentKeyAuthorizationPolicyInfo>(ENTITY_SET, contentKeyAuthorizationPolicyId,
                ContentKeyAuthorizationPolicyInfo.class);
    }

    /**
     * Create an operation that will retrieve the content key authorization
     * policy at the given link
     * 
     * @param link
     *            the link
     * @return the operation
     */
    public static EntityGetOperation<ContentKeyAuthorizationPolicyInfo> get(
            LinkInfo<ContentKeyAuthorizationPolicyInfo> link) {
        return new DefaultGetOperation<ContentKeyAuthorizationPolicyInfo>(link.getHref(),
                ContentKeyAuthorizationPolicyInfo.class);
    }

    /**
     * Create an operation that will retrieve all content key authorization
     * polices
     * 
     * @return the operation
     */

    public static DefaultListOperation<ContentKeyAuthorizationPolicyInfo> list() {
        return new DefaultListOperation<ContentKeyAuthorizationPolicyInfo>(ENTITY_SET,
                new GenericType<ListResult<ContentKeyAuthorizationPolicyInfo>>() {
                });
    }

    /**
     * Create an operation to delete the given content key authorization policy
     * 
     * @param contentKeyAuthorizationPolicyId
     *            id of content key authorization policy to delete
     * @return the delete operation
     */

    public static EntityDeleteOperation delete(String contentKeyAuthorizationPolicyId) {
        return new DefaultDeleteOperation(ENTITY_SET, contentKeyAuthorizationPolicyId);
    }

    /**
     * Link a content key authorization policy options.
     * 
     * @param contentKeyAuthorizationPolicyId
     *            the content key authorization policy id
     * @param contentKeyAuthorizationPolicyOptionId
     *            the content key authorization policy option id
     * @return the entity action operation
     */
    public static EntityLinkOperation linkOptions(String contentKeyAuthorizationPolicyId,
            String contentKeyAuthorizationPolicyOptionId) {
        String escapedContentKeyId = null;
        try {
            escapedContentKeyId = URLEncoder.encode(contentKeyAuthorizationPolicyOptionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new InvalidParameterException("contentKeyId");
        }
        URI contentKeyUri = URI
                .create(String.format("ContentKeyAuthorizationPolicyOptions('%s')", escapedContentKeyId));
        return new EntityLinkOperation(ENTITY_SET, contentKeyAuthorizationPolicyId, "Options", contentKeyUri);
    }
    
    /**
     * Unlink content key authorization policy options.
     * 
     * @param assetId
     *            the asset id
     * @param adpId
     *            the Asset Delivery Policy id
     * @return the entity action operation
     */
    public static EntityUnlinkOperation unlinkOptions(String contentKeyAuthorizationPolicyId,
            String contentKeyAuthorizationPolicyOptionId) {
        return new EntityUnlinkOperation(ENTITY_SET, contentKeyAuthorizationPolicyId, "Options", contentKeyAuthorizationPolicyOptionId);
    }
}
