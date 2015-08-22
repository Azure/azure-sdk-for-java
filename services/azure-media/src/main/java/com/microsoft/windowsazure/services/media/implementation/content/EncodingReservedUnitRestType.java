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

package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Wrapper DTO for Media Services access policies.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EncodingReservedUnitRestType implements MediaServiceDTO {

    @XmlElement(name = "AccountId", namespace = Constants.ODATA_DATA_NS)
    private String accountId;

    @XmlElement(name = "ReservedUnitType", namespace = Constants.ODATA_DATA_NS)
    private int reservedUnitType;

    @XmlElement(name = "MaxReservableUnits", namespace = Constants.ODATA_DATA_NS)
    private Integer maxReservableUnits;

    @XmlElement(name = "CurrentReservedUnits", namespace = Constants.ODATA_DATA_NS)
    private int currentReservedUnits;

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the reservedUnitType
     */
    public int getReservedUnitType() {
        return reservedUnitType;
    }

    /**
     * @param reservedUnitType the reservedUnitType to set
     */
    public void setReservedUnitType(int reservedUnitType) {
        this.reservedUnitType = reservedUnitType;
    }

    /**
     * @return the maxReservableUnits
     */
    public int getMaxReservableUnits() {
        return maxReservableUnits;
    }

    /**
     * @param maxReservableUnits the maxReservableUnits to set
     */
    public void setMaxReservableUnits(int maxReservableUnits) {
        this.maxReservableUnits = maxReservableUnits;
    }

    /**
     * @return the currentReservedUnits
     */
    public int getCurrentReservedUnits() {
        return currentReservedUnits;
    }

    /**
     * @param currentReservedUnits the currentReservedUnits to set
     */
    public void setCurrentReservedUnits(int currentReservedUnits) {
        this.currentReservedUnits = currentReservedUnits;
    }
    
}
