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

import java.util.Calendar;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.std.CalendarSerializer;

/**
 * The Class G20Key.
 */
public class G20Key {

    /** The expiration. */
    private Calendar expiration;

    /** The identifier. */
    private String identifier;

    /**
     * Gets the expiration.
     * 
     * @return the expiration
     */
    @JsonProperty("Expiration")
    @JsonSerialize(using = CalendarSerializer.class)
    public Calendar getExpiration() {
        return this.expiration;
    }

    /**
     * Sets the expiration.
     * 
     * @param expiration
     *            the expiration
     * @return the g20 key
     */
    @JsonProperty("Expiration")
    @JsonSerialize(using = CalendarSerializer.class)
    public G20Key setExpiration(Calendar expiration) {
        this.expiration = expiration;
        return this;
    }

    /**
     * Gets the identifier.
     * 
     * @return the identifier
     */
    @JsonProperty("Identifier")
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Sets the identifier.
     * 
     * @param identifier
     *            the identifier
     * @return the g20 key
     */
    @JsonProperty("Identifier")
    public G20Key setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }
}
