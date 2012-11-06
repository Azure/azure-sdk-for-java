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

import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.content.LocatorRestType;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.sun.jersey.api.client.GenericType;

/**
 * Implementation of Locator entity
 * 
 */
public class Locator {

    private final static String ENTITY_SET = "Locators";

    private Locator() {
    }

    public static Creator create(String accessPolicyId, String assetId, LocatorType locatorType) {
        return new CreatorImpl(accessPolicyId, assetId, locatorType);
    }

    public interface Creator extends EntityCreationOperation<LocatorInfo> {
        /**
         * Set the date and time for when the locator starts to be available
         * 
         * @param startDateTime
         *            The date/time
         * @return The creator instance (for function chaining)
         */
        Creator startDateTime(Date startDateTime);

        /**
         * Set the date and time at which the locator will expire
         * 
         * @param expirationDateTime
         *            Expiration date and time
         * @return The creator instance (for function chaining)
         */
        Creator expirationDateTime(Date expirationDateTime);
    }

    private static class CreatorImpl extends EntityOperationSingleResultBase<LocatorInfo> implements Creator {
        private final String accessPolicyId;
        private final String assetId;
        private final LocatorType locatorType;
        private Date startDateTime;
        private Date expirationDateTime;

        protected CreatorImpl(String accessPolicyId, String assetId, LocatorType locatorType) {
            super(ENTITY_SET, LocatorInfo.class);
            this.accessPolicyId = accessPolicyId;
            this.assetId = assetId;
            this.locatorType = locatorType;
        }

        @Override
        public Object getRequestContents() {
            return new LocatorRestType().setAccessPolicyId(accessPolicyId).setAssetId(assetId)
                    .setStartTime(startDateTime).setExpirationDateTime(expirationDateTime)
                    .setType(locatorType.getCode());
        }

        @Override
        public Creator startDateTime(Date startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        @Override
        public Creator expirationDateTime(Date expirationDateTime) {
            this.expirationDateTime = expirationDateTime;
            return this;
        }
    }

    public static EntityGetOperation<LocatorInfo> get(String locatorId) {
        return new DefaultGetterOperation<LocatorInfo>(ENTITY_SET, locatorId, LocatorInfo.class);
    }

    public static EntityListOperation<LocatorInfo> list() {
        return new DefaultListOperation<LocatorInfo>(ENTITY_SET, new GenericType<ListResult<LocatorInfo>>() {
        });
    }

    public static EntityListOperation<LocatorInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new DefaultListOperation<LocatorInfo>(ENTITY_SET, new GenericType<ListResult<LocatorInfo>>() {
        }, queryParameters);
    }

    public static Updater update(String locatorId) {
        return new UpdaterImpl(locatorId);
    }

    public interface Updater extends EntityUpdateOperation {
        Updater startDateTime(Date startDateTime);

        Updater expirationDateTime(Date expirationDateTime);
    }

    private static class UpdaterImpl extends EntityOperationBase implements Updater {
        private Date startDateTime;
        private Date expirationDateTime;

        public UpdaterImpl(String locatorId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, locatorId));
        }

        @Override
        public Object getRequestContents() {
            return new LocatorRestType().setStartTime(startDateTime).setExpirationDateTime(expirationDateTime);
        }

        @Override
        public Updater startDateTime(Date startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        @Override
        public Updater expirationDateTime(Date expirationDateTime) {
            this.expirationDateTime = expirationDateTime;
            return this;
        }

    }

    public static EntityDeleteOperation delete(String locatorId) {
        return new DefaultDeleteOperation(ENTITY_SET, locatorId);
    }
}
