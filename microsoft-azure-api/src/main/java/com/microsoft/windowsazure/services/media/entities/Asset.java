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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * 
 */
public class Asset {

    // Prevent instantiation
    private Asset() {
    }

    public static Creator create() {
        return new CreatorImpl();
    }

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

    public static EntityGetOperation<AssetInfo> get(String assetId) {
        return new DefaultGetterOperation<AssetInfo>("Assets", assetId, AssetInfo.class);
    }

    public static EntityListOperation<AssetInfo> list() {
        return new ListerImpl();
    }

    public static EntityListOperation<AssetInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new ListerImpl(queryParameters);
    }

    private static class ListerImpl extends EntityOperationBase implements EntityListOperation<AssetInfo> {
        private final MultivaluedMap<String, String> queryParameters;

        public ListerImpl() {
            super("Assets");
            queryParameters = new MultivaluedMapImpl();
        }

        public ListerImpl(MultivaluedMap<String, String> queryParameters) {
            this();
            this.queryParameters.putAll(queryParameters);
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityListOperation#getQueryParameters()
         */
        @Override
        public MultivaluedMap<String, String> getQueryParameters() {
            return queryParameters;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityListOperation#getResponseGenericType()
         */
        @Override
        public GenericType<ListResult<AssetInfo>> getResponseGenericType() {
            return new GenericType<ListResult<AssetInfo>>() {
            };
        }
    }

    public static Updater update(String assetId) {
        return new UpdaterImpl(assetId);
    }

    public interface Updater extends EntityUpdateOperation {
        Updater name(String name);

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

    public static EntityDeleteOperation delete(String id) {
        return new DeleteImpl(id);
    }

    private static class DeleteImpl implements EntityDeleteOperation {
        private final String assetId;

        public DeleteImpl(String id) {
            this.assetId = id;
        }

        @Override
        public String getUri() {
            String escapedEntityId;
            try {
                escapedEntityId = URLEncoder.encode(assetId, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new InvalidParameterException(assetId);
            }
            return String.format("%s('%s')", "Assets", escapedEntityId);
        }
    }
}
