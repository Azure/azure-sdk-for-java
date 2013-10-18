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

import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultEntityActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityProxyData;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.OriginSettingsMapper;
import com.microsoft.windowsazure.services.media.implementation.content.OriginType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Origin entities.
 * 
 */
public class Origin {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "Origins";

    // Prevent instantiation
    /**
     * Instantiates a new Origin.
     */
    private Origin() {
    }

    /**
     * Creates a Origin creator.
     * 
     * @return the creator
     */
    public static Creator create() {
        return new Creator();
    }

    /**
     * The Class Creator.
     */
    public static class Creator extends EntityOperationSingleResultBase<OriginInfo> implements
            EntityCreateOperation<OriginInfo> {

        /** The name. */
        private String name;

        /** The description. */
        private String description;

        /** The state. */
        private OriginState state;

        /** The settings. */
        private OriginSettings settings;

        /** The reserved units. */
        private int reservedUnits;

        /** The origin settings mapper. */
        private final OriginSettingsMapper originSettingsMapper = new OriginSettingsMapper();

        /**
         * Instantiates a new creator.
         */
        public Creator() {
            super(ENTITY_SET, OriginInfo.class);
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            OriginType originType = new OriginType();
            originType.setName(name);
            originType.setDescription(description);

            if (state != null) {
                originType.setState(state.toString());
            }

            if (settings != null) {
                String originSettings = originSettingsMapper.toString(this.settings);
                originType.setSettings(originSettings);
            }

            originType.setReservedUnits(reservedUnits);

            return originType;
        }

        /**
         * Set the name of the Origin to be created.
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
         * Sets the state.
         * 
         * @param state
         *            the state
         * @return the creator
         */
        public Creator setState(OriginState state) {
            this.state = state;
            return this;
        }

        /**
         * Sets the settings.
         * 
         * @param settings
         *            the settings
         * @return the creator
         */
        public Creator setSettings(OriginSettings settings) {
            this.settings = settings;
            return this;
        }

    }

    /**
     * Create an operation object that will get the state of the given Origin.
     * 
     * @param OriginId
     *            id of Origin to retrieve
     * @return the get operation
     */
    public static EntityGetOperation<OriginInfo> get(String OriginId) {
        return new DefaultGetOperation<OriginInfo>(ENTITY_SET, OriginId, OriginInfo.class);
    }

    /**
     * Get the Origin at the given link.
     * 
     * @param link
     *            the link
     * @return the get operation
     */
    public static EntityGetOperation<OriginInfo> get(LinkInfo<OriginInfo> link) {
        return new DefaultGetOperation<OriginInfo>(link.getHref(), OriginInfo.class);
    }

    /**
     * Create an operation that will list all the Origins.
     * 
     * @return The list operation
     */
    public static DefaultListOperation<OriginInfo> list() {
        return new DefaultListOperation<OriginInfo>(ENTITY_SET, new GenericType<ListResult<OriginInfo>>() {
        });
    }

    /**
     * Create an operation that will list all the Origins at the given link.
     * 
     * @param link
     *            Link to request Origins from.
     * @return The list operation.
     */
    public static DefaultListOperation<OriginInfo> list(LinkInfo<OriginInfo> link) {
        return new DefaultListOperation<OriginInfo>(link.getHref(), new GenericType<ListResult<OriginInfo>>() {
        });
    }

    /**
     * Create an operation that will update the given Origin.
     * 
     * @param OriginId
     *            the Origin id
     * @return the update operation
     */
    public static Updater update(String OriginId) {
        return new Updater(OriginId);
    }

    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {

        /** The description. */
        private String description;

        /** The state. */
        private OriginState state;

        /** The settings. */
        private OriginSettings settings;

        /** The origin settings mapper. */
        private final OriginSettingsMapper originSettingsMapper = new OriginSettingsMapper();

        /**
         * Instantiates a new updater.
         * 
         * @param OriginId
         *            the Origin id
         */
        protected Updater(String OriginId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, OriginId));
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
            OriginType OriginType = new OriginType();
            OriginType.setDescription(description);

            if (state != null) {
                OriginType.setState(state.toString());
            }

            if (settings != null) {
                String OriginSettings = originSettingsMapper.toString(this.settings);
                OriginType.setSettings(OriginSettings);
            }

            return OriginType;
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
         * Sets the state.
         * 
         * @param state
         *            the state
         * @return the updater
         */
        public Updater setState(OriginState state) {
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
        public Updater setSettings(OriginSettings settings) {
            this.settings = settings;
            return this;
        }

    }

    /**
     * Create an operation to delete the given Origin.
     * 
     * @param originId
     *            the origin id
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String originId) {
        return new DefaultDeleteOperation(ENTITY_SET, originId);
    }

    /**
     * Start a Origin with a specified Origin ID.
     * 
     * @param originId
     *            the origin id
     * @return the entity action operation
     */
    public static EntityActionOperation start(String originId) {
        return new DefaultEntityActionOperation(ENTITY_SET, originId, "Start");
    }

    /**
     * Stop a Origin with a specified Origin ID.
     * 
     * @param originId
     *            the origin id
     * @return the entity action operation
     */
    public static EntityActionOperation stop(String originId) {
        return new DefaultEntityActionOperation(ENTITY_SET, originId, "Stop");
    }

}
