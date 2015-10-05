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
 * Specifies the set of possible permissions for a shared access account policy.
 */
public enum SharedAccessAccountPermissions {
    /**
     * Permission to read resources and list queues and tables granted.
     */
    READ('r'),

    /**
     * Permission to add messages, table entities, and append to blobs granted.
     */
    ADD('a'),

    /**
     * Permission to create blobs and files granted.
     */
    CREATE('c'),

    /**
     * Permission to write resources granted.
     */
    WRITE('w'),

    /**
     * Permission to delete resources granted.
     */
    DELETE('d'),

    /**
     * Permission to list blob containers, blobs, shares, directories, and files granted.
     */
    LIST('l'),

    /**
     * Permissions to update messages and table entities granted.
     */
    UPDATE('u'),

    /**
     * Permission to get and delete messages granted.
     */
    PROCESS_MESSAGES('p');
    
    final private char value;
    
    /**
     * Create a <code>SharedAccessAccountPermissions</code>.
     * 
     * @param c
     *            The <code>char</code> which represents this permission.
     */
    private SharedAccessAccountPermissions(char c) {
        this.value = c;
    }
    
    /**
     * Converts the given permissions to a <code>String</code>.
     * 
     * @param permissions
     *            The permissions to convert to a <code>String</code>.
     *            
     * @return A <code>String</code> which represents the <code>SharedAccessAccountPermissions</code>.
     */
    static String permissionsToString(EnumSet<SharedAccessAccountPermissions> permissions) {
        if (permissions == null) {
            return Constants.EMPTY_STRING;
        }
        
        StringBuilder value = new StringBuilder();
        
        for (SharedAccessAccountPermissions perm : permissions) {
            value.append(perm.value);
        }
        
        return value.toString();
    }
    
    /**
     * Creates an {@link EnumSet<SharedAccessAccountPermissions>} from the specified permissions string.
     * 
     * @param permString
     *            A <code>String</code> which represents the <code>SharedAccessAccountPermissions</code>.
     * @return A {@link EnumSet<SharedAccessAccountPermissions>} generated from the given <code>String</code>.
     */
    static EnumSet<SharedAccessAccountPermissions> permissionsFromString(String permString) {
        EnumSet<SharedAccessAccountPermissions> permissions = EnumSet.noneOf(SharedAccessAccountPermissions.class);

        for (final char c : permString.toLowerCase().toCharArray()) {
            boolean invalidCharacter = true;
            
            for (SharedAccessAccountPermissions perm : SharedAccessAccountPermissions.values()) {
                if (c == perm.value) {
                    permissions.add(perm);
                    invalidCharacter = false;
                    break;
                }
                
            }
            
            if (invalidCharacter) {
                throw new IllegalArgumentException(
                        String.format(SR.ENUM_COULD_NOT_BE_PARSED, "Permissions", permString));
            }
        }
        
        return permissions;
    }
}