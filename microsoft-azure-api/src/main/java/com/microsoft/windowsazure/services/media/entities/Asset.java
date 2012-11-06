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
        return new CreatorImpl();
    }

    /**
     * Interface defining optional fields that can be set at asset creation.
     * 
     */
    public interface Creator extends EntityCreationOperation<AssetInfo> {
        /**
         * Set the name of the asset to be created
         * 
         * @param name
         *            The name
         * @return The creator object (for call chaining)
         */
        Creator name(String name);

        /**
         * Sets the alternate id of the asset to be created.
         * 
         * @param alternateId
         *            The id
         * 
         * @return The creator object (for call chaining)
         */
        Creator alternateId(String alternateId);
    }

    private static class CreatorImpl extends EntityOperationSingleResultBase<AssetInfo> implements Creator {
        private String name;
        private String alternateId;

        public CreatorImpl() {
            super("Assets", AssetInfo.class);
        }

        @Override
        public Object getRequestContents() {
            AssetType assetType = new AssetType();
            assetType.setName(name);
            assetType.setAlternateId(alternateId);
            return assetType;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.Asset.Creator#name(java.lang.String)
         */
        @Override
        public Creator name(String name) {
            this.name = name;
            return this;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.Asset.Creator#alternateId(java.lang.String)
         */
        @Override
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
        return new UpdaterImpl(assetId);
    }

    /**
     * Interface defining which fields can be updated after asset creation
     * 
     */
    public interface Updater extends EntityUpdateOperation {
        /**
         * Sets new name for asset
         * 
         * @param name
         *            The new name
         * @return Updater instance
         */
        Updater name(String name);

        /**
         * Sets new alternate id for asset
         * 
         * @param alternateId
         *            the new alternate id
         * @return Updater instance
         */
        Updater alternateId(String alternateId);
    }

    private static class UpdaterImpl extends EntityOperationBase implements Updater {
        private String name;
        private String alternateId;

        protected UpdaterImpl(String assetId) {
            super(new EntityOperationBase.EntityIdUriBuilder("Assets", assetId));
        }

        @Override
        public Object getRequestContents() {
            AssetType assetType = new AssetType();
            assetType.setName(name);
            assetType.setAlternateId(alternateId);
            return assetType;
        }

        @Override
        public Updater name(String name) {
            this.name = name;
            return this;
        }

        @Override
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
