/**
 * Copyright 2012 Microsoft Corporation
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

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyRestType;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationSingleResultBase;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Access Policy entities.
 * 
 */
public class ContentKey {

    private static final String ENTITY_SET = "AccessPolicies";

    private ContentKey() {
    }

    /**
     * Creates an operation to create a new access policy
     * 
     * @param name
     *            name of the access policy
     * @param durationInMinutes
     *            how long the access policy will be in force
     * @param permissions
     *            permissions allowed by this access policy
     * @return The operation
     */
    public static EntityCreationOperation<ContentKeyInfo> create(String id, ContentKeyType contentKeyType,
            String encryptedContentKey) {
        return new Creator(id, contentKeyType, encryptedContentKey);
    }

    private static class Creator extends EntityOperationSingleResultBase<ContentKeyInfo> implements
            EntityCreationOperation<ContentKeyInfo> {
        private final String id;
        private ContentKeyType contentKeyType;
        private final String encryptedContentKey;
        private String name;
        private String checksum;

        public Creator(String id, ContentKeyType contentKeyType, String encryptedContentKey) {

            super(ENTITY_SET, ContentKeyInfo.class);

            this.id = id;
            this.contentKeyType = contentKeyType;
            this.encryptedContentKey = encryptedContentKey;
        }

        @Override
        public Object getRequestContents() {
            ContentKeyRestType contentKeyRestType = new ContentKeyRestType();
            contentKeyRestType.setId(id);
            if (contentKeyType != null) {
                contentKeyRestType.setContentKeyType(contentKeyType.getCode());
            }
            contentKeyRestType.setEncryptedContentKey(encryptedContentKey);
            contentKeyRestType.setName(name);
            contentKeyRestType.setChecksum(checksum);
            return contentKeyRestType;
        }

        public ContentKeyType getContentKeyType() {
            return contentKeyType;
        }

        public EntityOperationSingleResultBase<ContentKeyInfo> setContentKeyType(ContentKeyType contentKeyType) {
            this.contentKeyType = contentKeyType;
            return this;
        }

        public String getEncryptedContentKey() {
            return encryptedContentKey;
        }

    }

    /**
     * Create an operation that will retrieve the given access policy
     * 
     * @param ContentKeyId
     *            id of access policy to retrieve
     * @return the operation
     */
    public static EntityGetOperation<ContentKeyInfo> get(String ContentKeyId) {
        return new DefaultGetOperation<ContentKeyInfo>(ENTITY_SET, ContentKeyId, ContentKeyInfo.class);
    }

    /**
     * Create an operation that will retrieve all access policies
     * 
     * @return the operation
     */
    public static EntityListOperation<ContentKeyInfo> list() {
        return new DefaultListOperation<ContentKeyInfo>(ENTITY_SET, new GenericType<ListResult<ContentKeyInfo>>() {
        });
    }

    /**
     * Create an operation that will retrieve all access policies that match the given query parameters
     * 
     * @param queryParameters
     *            query parameters to add to the request
     * @return the operation
     */
    public static EntityListOperation<ContentKeyInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new DefaultListOperation<ContentKeyInfo>(ENTITY_SET, new GenericType<ListResult<ContentKeyInfo>>() {
        }, queryParameters);
    }

    /**
     * Create an operation to delete the given access policy
     * 
     * @param ContentKeyId
     *            id of access policy to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String ContentKeyId) {
        return new DefaultDeleteOperation(ENTITY_SET, ContentKeyId);
    }
}
