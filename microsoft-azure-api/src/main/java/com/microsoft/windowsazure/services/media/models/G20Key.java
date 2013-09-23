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

import java.util.Date;

/**
 * The Class G20Key.
 */
public class G20Key {

    /** The expiration. */
    private Date expiration;

    /** The identifier. */
    private String identifier;

    /**
     * Gets the expiration.
     * 
     * @return the expiration
     */
    public Date getExpiration() {
        return this.expiration;
    }

    /**
     * Sets the expiration.
     * 
     * @param expiration
     *            the expiration
     * @return the g20 key
     */
    public G20Key setExpiration(Date expiration) {
        this.expiration = expiration;
        return this;
    }

    /**
     * Gets the identifier.
     * 
     * @return the identifier
     */
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
    public G20Key setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }
}
