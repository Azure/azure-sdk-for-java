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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.GenericType;

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

    private static class CreatorImpl implements Creator {
        private String name;
        private String alternateId;

        @Override
        public String getUri() {
            return "Assets";
        }

        @Override
        public MediaType getContentType() {
            return MediaType.APPLICATION_ATOM_XML_TYPE;
        }

        @Override
        public MediaType getAcceptType() {
            return MediaType.APPLICATION_ATOM_XML_TYPE;
        }

        @Override
        public Class<AssetInfo> getResponseClass() {
            return AssetInfo.class;
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
        return new GetterImpl(assetId);
    }

    private static class GetterImpl implements EntityGetOperation<AssetInfo> {

        private final String assetId;

        public GetterImpl(String assetId) {
            super();
            this.assetId = assetId;
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
            return String.format("Assets('%s')", escapedEntityId);
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityGetOperation#getContentType()
         */
        @Override
        public MediaType getContentType() {
            return MediaType.APPLICATION_ATOM_XML_TYPE;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityGetOperation#getAcceptType()
         */
        @Override
        public MediaType getAcceptType() {
            return MediaType.APPLICATION_ATOM_XML_TYPE;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityGetOperation#getResponseClass()
         */
        @Override
        public Class<AssetInfo> getResponseClass() {
            return AssetInfo.class;
        }
    }

    public static EntityListOperation<AssetInfo> list() {
        return new ListerImpl();
    }

    private static class ListerImpl implements EntityListOperation<AssetInfo> {

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityListOperation#getQueryParameters()
         */
        @Override
        public MultivaluedMap<String, String> getQueryParameters() {
            return null;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityOperation#getUri()
         */
        @Override
        public String getUri() {
            return "Assets";
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityOperation#getContentType()
         */
        @Override
        public MediaType getContentType() {
            return MediaType.APPLICATION_ATOM_XML_TYPE;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityOperation#getAcceptType()
         */
        @Override
        public MediaType getAcceptType() {
            return MediaType.APPLICATION_ATOM_XML_TYPE;
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
}
