/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information. 
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
