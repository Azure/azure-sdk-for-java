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
import com.microsoft.windowsazure.services.media.implementation.entities.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationSingleResultBase;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate content key entities.
 * 
 */
public class ContentKey {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "ContentKeys";

    /**
     * Instantiates a new content key.
     */
    private ContentKey() {
    }

    /**
     * Creates an operation to create a new content key.
     * 
     * @param id
     *            the id
     * @param contentKeyType
     *            the content key type
     * @param encryptedContentKey
     *            the encrypted content key
     * @return The operation
     */
    public static Creator create(String id, ContentKeyType contentKeyType, String encryptedContentKey) {
        return new Creator(id, contentKeyType, encryptedContentKey);
    }

    /**
     * The Class Creator.
     */
    public static class Creator extends EntityOperationSingleResultBase<ContentKeyInfo> implements
            EntityCreateOperation<ContentKeyInfo> {

        /** The id. */
        private final String id;

        /** The content key type. */
        private final ContentKeyType contentKeyType;

        /** The encrypted content key. */
        private final String encryptedContentKey;

        /** The name. */
        private String name;

        /** The checksum. */
        private String checksum;

        /**
         * Instantiates a new creator.
         * 
         * @param id
         *            the id
         * @param contentKeyType
         *            the content key type
         * @param encryptedContentKey
         *            the encrypted content key
         */
        public Creator(String id, ContentKeyType contentKeyType, String encryptedContentKey) {

            super(ENTITY_SET, ContentKeyInfo.class);

            this.id = id;
            this.contentKeyType = contentKeyType;
            this.encryptedContentKey = encryptedContentKey;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityCreateOperation#getRequestContents()
         */
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

        /**
         * Sets the name.
         * 
         * @param name
         *            the name
         * @return the creator
         */
        public Creator setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the checksum.
         * 
         * @param checksum
         *            the checksum
         * @return the creator
         */
        public Creator setChecksum(String checksum) {
            this.checksum = checksum;
            return this;
        }
    }

    /**
     * Create an operation that will retrieve the given content key.
     * 
     * @param ContentKeyId
     *            id of content key to retrieve
     * @return the operation
     */
    public static EntityGetOperation<ContentKeyInfo> get(String ContentKeyId) {
        return new DefaultGetOperation<ContentKeyInfo>(ENTITY_SET, ContentKeyId, ContentKeyInfo.class);
    }

    /**
     * Create an operation that will retrieve all access policies.
     * 
     * @return the operation
     */
    public static DefaultListOperation<ContentKeyInfo> list() {
        return new DefaultListOperation<ContentKeyInfo>(ENTITY_SET, new GenericType<ListResult<ContentKeyInfo>>() {
        });
    }

    /**
     * Create an operation that will retrieve all access policies that match the given query parameters.
     * 
     * @param queryParameters
     *            query parameters to add to the request
     * @return the operation
     */
    public static DefaultListOperation<ContentKeyInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new DefaultListOperation<ContentKeyInfo>(ENTITY_SET, new GenericType<ListResult<ContentKeyInfo>>() {
        }, queryParameters);
    }

    /**
     * Create an operation that will list all the content keys at the given link.
     * 
     * @param link
     *            Link to request content keys from.
     * @return The list operation.
     */
    public static DefaultListOperation<ContentKeyInfo> list(LinkInfo link) {
        return new DefaultListOperation<ContentKeyInfo>(link.getHref(), new GenericType<ListResult<ContentKeyInfo>>() {
        });
    }

    /**
     * Create an operation to delete the given content key.
     * 
     * @param ContentKeyId
     *            id of content key to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String ContentKeyId) {
        return new DefaultDeleteOperation(ENTITY_SET, ContentKeyId);
    }

}
