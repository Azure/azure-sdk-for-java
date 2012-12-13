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

import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityLinkOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationBase;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityUpdateOperation;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Asset entities.
 * 
 */
public class Asset {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "Assets";

    // Prevent instantiation
    /**
     * Instantiates a new asset.
     */
    private Asset() {
    }

    /**
     * Creates the.
     * 
     * @return the creator
     */
    public static Creator create() {
        return new Creator();
    }

    /**
     * The Class Creator.
     */
    public static class Creator extends EntityOperationSingleResultBase<AssetInfo> implements
            EntityCreationOperation<AssetInfo> {

        /** The name. */
        private String name;

        /** The alternate id. */
        private String alternateId;

        /** The options. */
        private AssetOption options;

        /** The state. */
        private AssetState state;

        /**
         * Instantiates a new creator.
         */
        public Creator() {
            super(ENTITY_SET, AssetInfo.class);
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            AssetType assetType = new AssetType();
            assetType.setName(name);
            assetType.setAlternateId(alternateId);
            if (options != null) {
                assetType.setOptions(options.getCode());
            }
            if (state != null) {
                assetType.setState(state.getCode());
            }
            return assetType;
        }

        /**
         * Set the name of the asset to be created.
         * 
         * @param name
         *            The name
         * @return The creator object (for call chaining)
         */
        public Creator setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the alternate id of the asset to be created.
         * 
         * @param alternateId
         *            The id
         * 
         * @return The creator object (for call chaining)
         */
        public Creator setAlternateId(String alternateId) {
            this.alternateId = alternateId;
            return this;
        }

        /**
         * Sets the options.
         * 
         * @param options
         *            the options
         * @return the creator
         */
        public Creator setOptions(AssetOption options) {
            this.options = options;
            return this;
        }

        /**
         * Sets the state.
         * 
         * @param state
         *            the state
         * @return the creator
         */
        public Creator setState(AssetState state) {
            this.state = state;
            return this;
        }
    }

    /**
     * Create an operation object that will get the state of the given asset.
     * 
     * @param assetId
     *            id of asset to retrieve
     * @return the get operation
     */
    public static EntityGetOperation<AssetInfo> get(String assetId) {
        return new DefaultGetOperation<AssetInfo>(ENTITY_SET, assetId, AssetInfo.class);
    }

    /**
     * Create an operation that will list all the assets.
     * 
     * @return The list operation
     */
    public static EntityListOperation<AssetInfo> list() {
        return new DefaultListOperation<AssetInfo>(ENTITY_SET, new GenericType<ListResult<AssetInfo>>() {
        });
    }

    /**
     * Create an operation that will list all the assets which match the given query parameters.
     * 
     * @param queryParameters
     *            query parameters to pass to the server.
     * @return the list operation.
     */
    public static EntityListOperation<AssetInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new DefaultListOperation<AssetInfo>(ENTITY_SET, new GenericType<ListResult<AssetInfo>>() {
        }, queryParameters);
    }

    /**
     * Create an operation that will list all the assets at the given link.
     * 
     * @param link
     *            Link to request assets from.
     * @return The list operation.
     */
    public static EntityListOperation<AssetInfo> list(LinkInfo link) {
        return new DefaultListOperation<AssetInfo>(link.getHref(), new GenericType<ListResult<AssetInfo>>() {
        });
    }

    /**
     * Create an operation that will update the given asset.
     * 
     * @param assetId
     *            id of the asset to update
     * @return the update operation
     */
    public static Updater update(String assetId) {
        return new Updater(assetId);
    }

    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {

        /** The name. */
        private String name;

        /** The alternate id. */
        private String alternateId;

        /**
         * Instantiates a new updater.
         * 
         * @param assetId
         *            the asset id
         */
        protected Updater(String assetId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, assetId));
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityUpdateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            AssetType assetType = new AssetType();
            assetType.setName(name);
            assetType.setAlternateId(alternateId);
            return assetType;
        }

        /**
         * Sets new name for asset.
         * 
         * @param name
         *            The new name
         * @return Updater instance
         */
        public Updater setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets new alternate id for asset.
         * 
         * @param alternateId
         *            the new alternate id
         * @return Updater instance
         */
        public Updater setAlternateId(String alternateId) {
            this.alternateId = alternateId;
            return this;
        }
    }

    /**
     * Create an operation to delete the given asset.
     * 
     * @param assetId
     *            id of asset to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String assetId) {
        return new DefaultDeleteOperation(ENTITY_SET, assetId);
    }

    /**
     * Link content key.
     * 
     * @param assetId
     *            the asset id
     * @param contentKeyUri
     *            the content key uri
     * @return the entity action operation
     */
    public static EntityLinkOperation linkContentKey(String assetId, URI contentKeyUri) {
        return new EntityLinkOperation("Assets", assetId, "ContentKeys", contentKeyUri);
    }

}
