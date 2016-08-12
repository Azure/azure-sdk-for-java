/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.webkey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Supported JsonWebKey operations.
 */
public final class JsonWebKeyOperation {

    /**
     * Encrypt operation.
     */
    public static final String ENCRYPT = "encrypt";
    
    /**
     * Decrypt operation.
     */
    public static final String DECRYPT = "decrypt";
    
    /**
     * Sign operation.
     */
    public static final String SIGN    = "sign";
    
    /**
     * Verify operation.
     */
    public static final String VERIFY  = "verify";
    
    /**
     * WrapKey operation.
     */
    public static final String WRAP    = "wrapKey";
    
    /**
     * UnwrapKey operation.
     */
    public static final String UNWRAP  = "unwrapKey";

    /**
     * All JWK operations.
     */
    public static final List<String> ALL_OPERATIONS = 
            Collections.unmodifiableList(
                    Arrays.asList(new String[] {ENCRYPT, DECRYPT, SIGN, VERIFY, WRAP, UNWRAP }));

    private JsonWebKeyOperation() {
        // not instantiable
    }

}
