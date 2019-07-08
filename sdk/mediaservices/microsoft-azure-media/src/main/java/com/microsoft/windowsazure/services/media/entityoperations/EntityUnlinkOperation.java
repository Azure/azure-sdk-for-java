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

package com.microsoft.windowsazure.services.media.entityoperations;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

/**
 * Generic implementation of $link operation of two entities.
 */
public class EntityUnlinkOperation extends DefaultDeleteOperation {

    /** The primary entity set. */
    private final String primaryEntitySet;

    /** The primary entity id. */
    private final String primaryEntityId;

    /** The secondary entity set. */
    private final String secondaryEntitySet;
    
    /** The secondary entity id. */
    private final String secondaryEntityId;

    /**
     * Instantiates a new entity unlink operation.
     * 
     * @param primaryEntitySet
     *            the primary entity set
     * @param primaryEntityId
     *            the primary entity id
     * @param secondaryEntitySet
     *            the secondary entity set
     * @param secondaryEntityUri
     *            the secondary entity id
     */
    public EntityUnlinkOperation(String primaryEntitySet, String primaryEntityId,
            String secondaryEntitySet, String secondaryEntityId) {
        super(primaryEntitySet, primaryEntityId);
        this.primaryEntitySet = primaryEntitySet;
        this.primaryEntityId = primaryEntityId;
        this.secondaryEntitySet = secondaryEntitySet;
        this.secondaryEntityId = secondaryEntityId;
    }
    
    @Override
    public String getUri() {
        String escapedPrimaryEntityId;
        String escapedSecondaryEntityId;
        try {
            escapedPrimaryEntityId = URLEncoder.encode(primaryEntityId, "UTF-8");
            escapedSecondaryEntityId = URLEncoder.encode(secondaryEntityId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new InvalidParameterException(
                    "UTF-8 encoding is not supported.");
        }
        return String.format("%s('%s')/$links/%s('%s')", primaryEntitySet,
                escapedPrimaryEntityId, secondaryEntitySet, escapedSecondaryEntityId);
    }
}
