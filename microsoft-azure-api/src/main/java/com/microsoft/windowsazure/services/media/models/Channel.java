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
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityProxyData;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.ChannelType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Channel entities.
 * 
 */
public class Channel {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "Channels";

    // Prevent instantiation
    /**
     * Instantiates a new Channel.
     */
    private Channel() {
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
    public static class Creator extends EntityOperationSingleResultBase<ChannelInfo> implements
            EntityCreateOperation<ChannelInfo> {

        /** The name. */
        private String name;

        private String description;

        private URI previewUri;

        private URI ingestUri;

        private ChannelState state;

        private ChannelSize size;

        private ChannelSettings settings;

        /**
         * Instantiates a new creator.
         */
        public Creator() {
            super(ENTITY_SET, ChannelInfo.class);
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            ChannelType channelType = new ChannelType();
            channelType.setName(name);
            channelType.setDescription(description);
            channelType.setPreviewUri(previewUri);
            channelType.setIngestUri(ingestUri);

            if (state != null) {
                channelType.setState(state.getCode());
            }
            if (size != null) {
                channelType.setSize(size.getCode());
            }

            if (settings != null) {
                String channelSettings = null;
                channelType.setSettings(channelSettings);
            }

            return channelType;
        }

        /**
         * Set the name of the Channel to be created.
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
         * Sets the state.
         * 
         * @param state
         *            the state
         * @return the creator
         */
        public Creator setState(ChannelState state) {
            this.state = state;
            return this;
        }

        public Creator setDescription(String description) {
            this.description = description;
            return this;
        }
    }

    /**
     * Create an operation object that will get the state of the given Channel.
     * 
     * @param ChannelId
     *            id of Channel to retrieve
     * @return the get operation
     */
    public static EntityGetOperation<ChannelInfo> get(String ChannelId) {
        return new DefaultGetOperation<ChannelInfo>(ENTITY_SET, ChannelId, ChannelInfo.class);
    }

    /**
     * Get the Channel at the given link
     * 
     * @param link
     *            the link
     * @return the get operation
     */
    public static EntityGetOperation<ChannelInfo> get(LinkInfo<ChannelInfo> link) {
        return new DefaultGetOperation<ChannelInfo>(link.getHref(), ChannelInfo.class);
    }

    /**
     * Create an operation that will list all the Channels.
     * 
     * @return The list operation
     */
    public static DefaultListOperation<ChannelInfo> list() {
        return new DefaultListOperation<ChannelInfo>(ENTITY_SET, new GenericType<ListResult<ChannelInfo>>() {
        });
    }

    /**
     * Create an operation that will list all the Channels at the given link.
     * 
     * @param link
     *            Link to request Channels from.
     * @return The list operation.
     */
    public static DefaultListOperation<ChannelInfo> list(LinkInfo<ChannelInfo> link) {
        return new DefaultListOperation<ChannelInfo>(link.getHref(), new GenericType<ListResult<ChannelInfo>>() {
        });
    }

    /**
     * Create an operation that will update the given Channel.
     * 
     * @param ChannelId
     *            id of the Channel to update
     * @return the update operation
     */
    public static Updater update(String ChannelId) {
        return new Updater(ChannelId);
    }

    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {

        /** The name. */
        private String name;
        private String description;
        private URI ingestUri;
        private URI previewUri;
        private ChannelSize size;
        private ChannelState state;
        private ChannelSettings settings;

        /**
         * Instantiates a new updater.
         * 
         * @param ChannelId
         *            the Channel id
         */
        protected Updater(String ChannelId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, ChannelId));
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entityoperations.EntityOperation#setProxyData(com.microsoft.windowsazure.services.media.entityoperations.EntityProxyData)
         */
        @Override
        public void setProxyData(EntityProxyData proxyData) {
            // Deliberately empty
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            ChannelType channelType = new ChannelType();
            channelType.setName(name);
            channelType.setDescription(description);
            channelType.setIngestUri(ingestUri);
            channelType.setPreviewUri(previewUri);
            channelType.setSize(size.getCode());
            channelType.setState(state.getCode());
            if (settings != null) {
                String channelSettings = null;
                channelType.setSettings(channelSettings);
            }

            return channelType;
        }

        /**
         * Sets new name for Channel.
         * 
         * @param name
         *            The new name
         * @return Updater instance
         */
        public Updater setName(String name) {
            this.name = name;
            return this;
        }

    }

    /**
     * Create an operation to delete the given Channel.
     * 
     * @param channelName
     *            id of Channel to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String channelName) {
        return new DefaultDeleteOperation(ENTITY_SET, channelName);
    }

    /**
     * Link content key.
     * 
     * @param ChannelId
     *            the Channel id
     * @param contentKeyId
     *            the content key id
     * @return the entity action operation
     */
    public static EntityLinkOperation getIngressMetrics(String ChannelId, String contentKeyId) {
        String escapedContentKeyId = null;
        try {
            escapedContentKeyId = URLEncoder.encode(contentKeyId, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new InvalidParameterException("contentKeyId");
        }
        URI contentKeyUri = URI.create(String.format("ContentKeys('%s')", escapedContentKeyId));
        return new EntityLinkOperation("Channels", ChannelId, "ContentKeys", contentKeyUri);
    }
}
