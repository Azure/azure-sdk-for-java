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

import java.net.URI;

import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
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
     * Creates a channel creator.
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

        /** The description. */
        private String description;

        /** The preview uri. */
        private URI previewUri;

        /** The ingest uri. */
        private URI ingestUri;

        /** The state. */
        private ChannelState state;

        /** The size. */
        private ChannelSize size;

        /** The settings. */
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
                channelType.setState(state.toString());
            }
            if (size != null) {
                channelType.setSize(size.toString());
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
         * Sets the description.
         * 
         * @param description
         *            the description
         * @return the creator
         */
        public Creator setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the preview uri.
         * 
         * @param previewUri
         *            the preview uri
         * @return the creator
         */
        public Creator setPreviewUri(URI previewUri) {
            this.previewUri = previewUri;
            return this;
        }

        /**
         * Sets the ingest uri.
         * 
         * @param ingestUri
         *            the ingest uri
         * @return the creator
         */
        public Creator setIngestUri(URI ingestUri) {
            this.ingestUri = ingestUri;
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

        /**
         * Sets the size.
         * 
         * @param size
         *            the size
         * @return the creator
         */
        public Creator setSize(ChannelSize size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the settings.
         * 
         * @param settings
         *            the settings
         * @return the creator
         */
        public Creator setSettings(ChannelSettings settings) {
            this.settings = settings;
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
     * Get the Channel at the given link.
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
     * @param channelId
     *            the channel id
     * @return the update operation
     */
    public static Updater update(String channelId) {
        return new Updater(channelId);
    }

    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {

        /** The description. */
        private String description;

        /** The ingest uri. */
        private URI ingestUri;

        /** The preview uri. */
        private URI previewUri;

        /** The size. */
        private ChannelSize size;

        /** The state. */
        private ChannelState state;

        /** The settings. */
        private ChannelSettings settings;

        /**
         * Instantiates a new updater.
         * 
         * @param channelId
         *            the Channel id
         */
        protected Updater(String channelId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, channelId));
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
            channelType.setDescription(description);
            channelType.setIngestUri(ingestUri);
            channelType.setPreviewUri(previewUri);
            if (size != null) {
                channelType.setSize(size.toString());
            }

            if (state != null) {
                channelType.setState(state.toString());
            }

            if (settings != null) {
                String channelSettings = null;
                channelType.setSettings(channelSettings);
            }

            return channelType;
        }

        /**
         * Sets the description.
         * 
         * @param description
         *            the description
         * @return the updater
         */
        public Updater setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the ingest uri.
         * 
         * @param ingestUri
         *            the ingest uri
         * @return the updater
         */
        public Updater setIngestUri(URI ingestUri) {
            this.ingestUri = ingestUri;
            return this;
        }

        /**
         * Sets the preview uri.
         * 
         * @param previewUri
         *            the preview uri
         * @return the updater
         */
        public Updater setPreviewUri(URI previewUri) {
            this.previewUri = previewUri;
            return this;
        }

        /**
         * Sets the size.
         * 
         * @param size
         *            the size
         * @return the updater
         */
        public Updater setSize(ChannelSize size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the state.
         * 
         * @param state
         *            the state
         * @return the updater
         */
        public Updater setState(ChannelState state) {
            this.state = state;
            return this;
        }

        /**
         * Sets the settings.
         * 
         * @param settings
         *            the settings
         * @return the updater
         */
        public Updater setSettings(ChannelSettings settings) {
            this.settings = settings;
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

}
