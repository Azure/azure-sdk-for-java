/*
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

/**
 * The Class ContentKeyAuthorizationPolicyRestriction.
 */
public class ContentKeyAuthorizationPolicyRestriction {
    
    public enum ContentKeyRestrictionType {
        Open(0),
        TokenRestricted(1),
        IPRestricted(2);
        
        private final int value;
        private ContentKeyRestrictionType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    private final String name;

    private final int keyRestrictionType;

    private final String requirements;

    /**
     * Instantiates a new Content Key Authorization Policy Restriction
     * 
     * @param name
     *            the name
     * @param keyRestrictionType
     *            the keyRestrictionType
     * @param requirements
     *            the requirements
     */
    public ContentKeyAuthorizationPolicyRestriction(String name, int keyRestrictionType, String requirements) {
        this.name = name;
        this.keyRestrictionType = keyRestrictionType;
        this.requirements = requirements;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the key restriction type.
     * 
     * @return the key restriction type
     */
    public int getKeyRestrictionType() {
        return this.keyRestrictionType;
    }

    /**
     * Gets the requirements.
     * 
     * @return the time requirements
     */
    public String getRequirements() {
        return this.requirements;
    }
}
