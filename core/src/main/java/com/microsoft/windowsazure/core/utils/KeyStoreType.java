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

package com.microsoft.windowsazure.core.utils;

import java.security.InvalidParameterException;

/**
 * The Enum representing the type of the KeyStore.
 */
public enum KeyStoreType {

    /** The jceks. */
    jceks,
    /** The jks. */
    jks,
    /** The pkcs12. */
    pkcs12;
    
    public static KeyStoreType fromString(String keyStoreTypeString)
    {
        switch (keyStoreTypeString) {
        case "jceks":
            return KeyStoreType.jceks;
        case "jks":
            return KeyStoreType.jks;
        case "pkcs12":
            return KeyStoreType.pkcs12;
        default :
            throw new InvalidParameterException(String.format("keyStoreTypeString value %s cannot be recognized.", keyStoreTypeString));
            
        }
    }
}
