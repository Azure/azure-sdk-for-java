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

import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityProxyData;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.EncodingReservedUnitRestType;

/**
 * Class for updating EncodingReservedUnitTypes
 * 
 */
public final class EncodingReservedUnit {

    private static final String ENTITY_SET = "EncodingReservedUnitTypes";

    private EncodingReservedUnit() {
    }
    
    /**
     * Get the EncodingReservedUnitTypes
     * 
     * @return the operation
     */
    public static EntityGetOperation<EncodingReservedUnitInfo> get() {
        return new DefaultGetOperation<EncodingReservedUnitInfo>(ENTITY_SET, 
                EncodingReservedUnitInfo.class);
    }   
    
    /**
     * Update the EncodingReservedUnitTypes
     * @param encodingReservedUnitInfo 
     * 
     * @return the update operation
     */
    public static Updater update(EncodingReservedUnitInfo encodingReservedUnitInfo) {
        return new Updater(encodingReservedUnitInfo);
    }

    /**
     * The Class Updater.
     */
    public static class Updater extends EntityOperationBase implements
            EntityUpdateOperation {

        private int reservedUnitType;
        private int currentReservedUnits;

        /**
         * Instantiates a new updater.
         * @param encodingReservedUnitInfo 
         * 
         * @param assetId
         *            the asset id
         */
        protected Updater(final EncodingReservedUnitInfo encodingReservedUnitInfo) {
            super(new EntityUriBuilder() {
                @Override
                public String getUri() {
                    URI uri = URI.create(String.format("%s(guid'%s')", ENTITY_SET, 
                            encodingReservedUnitInfo.getAccountId()));
                    return uri.toString();
                }
            });
            this.setReservedUnitType(encodingReservedUnitInfo.getReservedUnitType());
            this.setCurrentReservedUnits(encodingReservedUnitInfo.getCurrentReservedUnits());
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityOperation
         * #setProxyData(com.microsoft.windowsazure.services.media
         * .entityoperations.EntityProxyData)
         */
        @Override
        public void setProxyData(EntityProxyData proxyData) {
            // Deliberately empty
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityUpdateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() {
            EncodingReservedUnitRestType encodingReservedUnitType = new EncodingReservedUnitRestType();
            encodingReservedUnitType.setAccountId(null); // never send account Id
            encodingReservedUnitType.setCurrentReservedUnits(currentReservedUnits);
            encodingReservedUnitType.setReservedUnitType(reservedUnitType);
            return encodingReservedUnitType;
        }

        /**
         * Sets new quantity of reserved units.
         * 
         * @param currentReservedUnits
         *            the new quantity of reserved units.
         * @return Updater instance
         */
        public Updater setCurrentReservedUnits(int currentReservedUnits) {
            this.currentReservedUnits = currentReservedUnits;
            return this;
        }
        
        /**
         * Sets new reserved units type.
         * 
         * @param encodingReservedUnitType
         *            the new quantity of reserved units.
         * @return Updater instance
         */
        public Updater setReservedUnitType(EncodingReservedUnitType encodingReservedUnitType) {
            this.reservedUnitType = encodingReservedUnitType.getCode();
            return this;
        }
    }
}
