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
import com.microsoft.windowsazure.services.media.implementation.content.ChannelType;
import com.microsoft.windowsazure.services.media.implementation.content.ProgramType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Program entities.
 * 
 */
public class Program {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "Programs";

    // Prevent instantiation
    /**
     * Instantiates a new Program.
     */
    private Program() {
    }

    /**
     * Creates a program creator.
     * 
     * @return the creator
     */
    public static Creator create() {
        return new Creator();
    }

    /**
     * The Class Creator.
     */
    public static class Creator extends EntityOperationSingleResultBase<ProgramInfo> implements
            EntityCreateOperation<ProgramInfo> {

        /** The name. */
        private String name;

        /** The description. */
        private String description;

        /**
         * Instantiates a new creator.
         */
        public Creator() {
            super(ENTITY_SET, ProgramInfo.class);
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            ProgramType programType = new ProgramType();
            programType.setName(name);
            programType.setDescription(description);

            return programType;
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

    }

    /**
     * Create an operation object that will get the state of the specified program.
     * 
     * @param ProgramId
     *            id of Program to retrieve
     * @return the get operation
     */
    public static EntityGetOperation<ProgramInfo> get(String programId) {
        return new DefaultGetOperation<ProgramInfo>(ENTITY_SET, programId, ProgramInfo.class);
    }

    /**
     * Create an operation that will list all the Programs.
     * 
     * @return The list operation
     */
    public static DefaultListOperation<ProgramInfo> list() {
        return new DefaultListOperation<ProgramInfo>(ENTITY_SET, new GenericType<ListResult<ProgramInfo>>() {
        });
    }

    /**
     * Create an operation that will update the given program.
     * 
     * @param programId
     *            the program id
     * @return the update operation
     */
    public static Updater update(String programId) {
        return new Updater(programId);
    }

    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {

        /** The description. */
        private String description;

        /**
         * Instantiates a new updater.
         * 
         * @param programId
         *            the program id
         */
        protected Updater(String programId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, programId));
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

    }

    /**
     * Create an operation to delete the given program.
     * 
     * @param programId
     *            id of program to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String programId) {
        return new DefaultDeleteOperation(ENTITY_SET, programId);
    }

    /**
     * Start a channel with a specified program ID.
     * 
     * @param channelId
     *            the ID of the program.
     * @return the entity action operation
     */
    public static EntityActionOperation start(String programId) {
        return new DefaultEntityActionOperation(ENTITY_SET, programId, "Start");
    }

    /**
     * Stop a channel with a specified program ID.
     * 
     * @param channelId
     *            the ID of the program.
     * @return the entity action operation
     */
    public static EntityActionOperation stop(String programId) {
        return new DefaultEntityActionOperation(ENTITY_SET, programId, "Stop");
    }

}
