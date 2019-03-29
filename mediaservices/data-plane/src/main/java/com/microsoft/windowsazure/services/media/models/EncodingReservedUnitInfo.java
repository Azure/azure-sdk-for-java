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

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.EncodingReservedUnitRestType;

/**
 * Type containing data about access policies.
 * 
 */
public class EncodingReservedUnitInfo extends ODataEntity<EncodingReservedUnitRestType> {

    /**
     * Creates a new {@link EncodingReservedUnitInfo} wrapping the given ATOM entry and
     * content objects.
     * 
     * @param entry
     *            Entry containing this AccessPolicy data
     * @param content
     *            Content with the AccessPolicy data
     */
    public EncodingReservedUnitInfo(EntryType entry, EncodingReservedUnitRestType content) {
        super(entry, content);
    }
    /**
     * @return the accountId
     */
    public String getAccountId() {
        return getContent().getAccountId();
    }

    /**
     * @return the reservedUnitType
     */
    public EncodingReservedUnitType getReservedUnitType() {
        return EncodingReservedUnitType.fromCode(getContent().getReservedUnitType());
    }

    /**
     * @return the maxReservableUnits
     */
    public int getMaxReservableUnits() {
        return getContent().getMaxReservableUnits();
    }

    /**
     * @return the currentReservedUnits
     */
    public int getCurrentReservedUnits() {
        return getContent().getCurrentReservedUnits();
    }    
}
