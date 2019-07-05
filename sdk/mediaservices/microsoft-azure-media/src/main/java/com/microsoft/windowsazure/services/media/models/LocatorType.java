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

import java.security.InvalidParameterException;

/**
 * The Enum LocatorType.
 */
public enum LocatorType {

    /** The None. */
    None(0),
    /** The sas. */
    SAS(1),
    /** The Origin. */
    OnDemandOrigin(2);

    /** The locator type code. */
    private int locatorTypeCode;

    /**
     * Instantiates a new locator type.
     * 
     * @param locatorTypeCode
     *            the locator type code
     */
    private LocatorType(int locatorTypeCode) {
        this.locatorTypeCode = locatorTypeCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return this.locatorTypeCode;
    }

    /**
     * From code.
     * 
     * @param type
     *            the type
     * @return the locator type
     */
    public static LocatorType fromCode(int type) {
        switch (type) {
        case 0:
            return LocatorType.None;
        case 1:
            return LocatorType.SAS;
        case 2:
            return LocatorType.OnDemandOrigin;
        default:
            throw new InvalidParameterException("type");
        }
    }
}
