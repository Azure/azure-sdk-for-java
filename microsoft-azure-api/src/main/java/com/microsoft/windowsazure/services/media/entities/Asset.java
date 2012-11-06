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

package com.microsoft.windowsazure.services.media.entities;

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Asset entities.
 * 
 */
public class Asset {

    // Prevent instantiation
    private Asset() {
    }

    public static Creator create() {
        return new Creator();
    }

    public static class Creator extends EntityOperationSingleResultBase<AssetInfo> implements
            EntityCreationOperation<AssetInfo> {
        private String name;
        private String alternateId;

        public Creator() {
            super("Assets", AssetInfo.class);
        }

        @Override
        public Object getRequestContents() {
            AssetType assetType = new AssetType();
            assetType.setName(name);
            assetType.setAlternateId(alternateId);
            return assetType;
        }

        /**
         * Set the name of the asset to be created
         * 
         * @param name
         *            The name
         * @return The creator object (for call chaining)
         */
        public Creator name(String name) {
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
        public Creator alternateId(String alternateId) {
            this.alternateId = alternateId;
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
        return new DefaultGetterOperation<AssetInfo>("Assets", assetId, AssetInfo.class);
    }

    /**
     * Create an operation that will list all the assets.
     * 
     * @return The list operation
     */
    public static EntityListOperation<AssetInfo> list() {
        return new DefaultListOperation<AssetInfo>("Assets", new GenericType<ListResult<AssetInfo>>() {
        });
    }

    /**
     * Create an operation that will list all the assets which match the given query parameters
     * 
     * @param queryParameters
     *            query parameters to pass to the server.
     * @return the list operation.
     */
    public static EntityListOperation<AssetInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new DefaultListOperation<AssetInfo>("Assets", new GenericType<ListResult<AssetInfo>>() {
        }, queryParameters);
    }

    /**
     * Create an operation that will update the given asset
     * 
     * @param assetId
     *            id of the asset to update
     * @return the update operation
     */
    public static Updater update(String assetId) {
        return new Updater(assetId);
    }

    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {
        private String name;
        private String alternateId;

        protected Updater(String assetId) {
            super(new EntityOperationBase.EntityIdUriBuilder("Assets", assetId));
        }

        @Override
        public Object getRequestContents() {
            AssetType assetType = new AssetType();
            assetType.setName(name);
            assetType.setAlternateId(alternateId);
            return assetType;
        }

        /**
         * Sets new name for asset
         * 
         * @param name
         *            The new name
         * @return Updater instance
         */
        public Updater name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets new alternate id for asset
         * 
         * @param alternateId
         *            the new alternate id
         * @return Updater instance
         */
        public Updater alternateId(String alternateId) {
            this.alternateId = alternateId;
            return this;
        }
    }

    /**
     * Create an operation to delete the given asset
     * 
     * @param assetId
     *            id of asset to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String assetId) {
        return new DefaultDeleteOperation("Assets", assetId);
    }
}
