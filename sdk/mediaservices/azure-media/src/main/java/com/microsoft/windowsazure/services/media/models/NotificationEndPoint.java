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
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.NotificationEndPointType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate notification end point entities.
 * 
 */
public final class NotificationEndPoint {

    private static final String ENTITY_SET = "NotificationEndPoints";

    private NotificationEndPoint() {
    }

    /**
     * Creates an operation to create a new notification end point.
     * 
     * @param name
     *            name of the notification end point.
     * @param endPointType
     *            the type of the notification end point.
     * @param endPointAddress
     *            the address of the end point.
     * @return The operation
     */
    public static EntityCreateOperation<NotificationEndPointInfo> create(
            String name, EndPointType endPointType, String endPointAddress) {
        return new Creator(name, endPointType, endPointAddress);
    }

    public static class Creator extends
            EntityOperationSingleResultBase<NotificationEndPointInfo> implements
            EntityCreateOperation<NotificationEndPointInfo> {
        private final String name;
        private final EndPointType endPointType;
        private final String endPointAddress;

        public Creator(String name, EndPointType endPointType,
                String endPointAddress) {

            super(ENTITY_SET, NotificationEndPointInfo.class);

            this.name = name;
            this.endPointType = endPointType;
            this.endPointAddress = endPointAddress;
        }

        @Override
        public Object getRequestContents() {
            return new NotificationEndPointType().setName(name)
                    .setEndPointType(endPointType.getCode())
                    .setEndPointAddress(endPointAddress);
        }
    }

    /**
     * Create an operation that will retrieve the given notification end point
     * 
     * @param notificationEndPointId
     *            id of notification end point to retrieve
     * @return the operation
     */
    public static EntityGetOperation<NotificationEndPointInfo> get(
            String notificationEndPointId) {
        return new DefaultGetOperation<NotificationEndPointInfo>(ENTITY_SET,
                notificationEndPointId, NotificationEndPointInfo.class);
    }

    /**
     * Create an operation that will retrieve the notification end point at the
     * given link
     * 
     * @param link
     *            the link
     * @return the operation
     */
    public static EntityGetOperation<NotificationEndPointInfo> get(
            LinkInfo<NotificationEndPointInfo> link) {
        return new DefaultGetOperation<NotificationEndPointInfo>(
                link.getHref(), NotificationEndPointInfo.class);
    }

    /**
     * Create an operation that will retrieve all notification end points
     * 
     * @return the operation
     */
    public static DefaultListOperation<NotificationEndPointInfo> list() {
        return new DefaultListOperation<NotificationEndPointInfo>(ENTITY_SET,
                new GenericType<ListResult<NotificationEndPointInfo>>() {
                });
    }

    public static Updater update(String notificationEndPointId) {
        return new Updater(notificationEndPointId);
    }

    /**
     * Create an operation to delete the given notification end point
     * 
     * @param notificationEndPointId
     *            id of notification end point to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String notificationEndPointId) {
        return new DefaultDeleteOperation(ENTITY_SET, notificationEndPointId);
    }

    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements
            EntityUpdateOperation {

        /** The name. */
        private String name;

        /**
         * Instantiates a new updater.
         * 
         * @param notificationEndPointId
         *            the asset id
         */
        protected Updater(String notificationEndPointId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET,
                    notificationEndPointId));
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityUpdateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            NotificationEndPointType notificationEndPointType = new NotificationEndPointType();
            notificationEndPointType.setName(name);
            return notificationEndPointType;
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
    }

}
