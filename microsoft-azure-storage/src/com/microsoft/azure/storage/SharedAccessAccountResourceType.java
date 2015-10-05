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
package com.microsoft.azure.storage;

import java.util.EnumSet;

import com.microsoft.azure.storage.core.SR;

/**
 * Specifies the set of possible resource types for a shared access account policy.
 */
public enum SharedAccessAccountResourceType {
    /**
     * Permission to access service level APIs granted.
     */
    SERVICE('s'),

    /**
     * Permission to access container level APIs (Blob Containers, Tables, Queues, File Shares) granted.
     */
    CONTAINER('c'),

    /**
     * Permission to access object level APIs (Blobs, Table Entities, Queue Messages, Files) granted.
     */
    OBJECT('o');
    
    char value;

    /**
     * Create a <code>SharedAccessAccountResourceType</code>.
     * 
     * @param c
     *            The <code>char</code> which represents this resource type.
     */
    private SharedAccessAccountResourceType(char c) {
        this.value = c;
    }
    
    /**
     * Converts the given resource types to a <code>String</code>.
     * 
     * @param types
     *            The resource types to convert to a <code>String</code>.
     *            
     * @return A <code>String</code> which represents the <code>SharedAccessAccountResourceTypes</code>.
     */
    static String resourceTypesToString(EnumSet<SharedAccessAccountResourceType> types) {
        if (types == null) {
            return Constants.EMPTY_STRING;
        }
        
        StringBuilder value = new StringBuilder();
        
        for (SharedAccessAccountResourceType type : types) {
            value.append(type.value);
        }
        
        return value.toString();
    }
    
    /**
     * Creates an {@link EnumSet<SharedAccessAccountResourceType>} from the specified resource types string.
     * 
     * @param rsrcString
     *            A <code>String</code> which represents the <code>SharedAccessAccountResourceTypes</code>.
     * @return A {@link EnumSet<SharedAccessAccountResourceType>} generated from the given <code>String</code>.
     */
    static EnumSet<SharedAccessAccountResourceType> resourceTypesFromString(String rsrcString) {
        EnumSet<SharedAccessAccountResourceType> resources = EnumSet.noneOf(SharedAccessAccountResourceType.class);

        for (final char c : rsrcString.toLowerCase().toCharArray()) {
            boolean invalidCharacter = true;
            
            for (SharedAccessAccountResourceType rsrc : SharedAccessAccountResourceType.values()) {
                if (c == rsrc.value) {
                    resources.add(rsrc);
                    invalidCharacter = false;
                    break;
                }
            }
            
            if (invalidCharacter) {
                throw new IllegalArgumentException(
                        String.format(SR.ENUM_COULD_NOT_BE_PARSED, "Resource Types", rsrcString));
            }
        }
        
        return resources;
    }
}