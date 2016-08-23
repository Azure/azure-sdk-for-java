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
 * Supported JsonWebKey key types (kty).
 */
public final class JsonWebKeyType {

    /**
     * The Elliptic Curve 'EC' key type.
     */
    public static final String EC     = "EC";
    
    /**
     * The 'RSA' key type.
     */
    public static final String RSA    = "RSA";
    
    /**
     * The 'RSA-HSM' key type.
     */
    public static final String RSAHSM = "RSA-HSM";
    
    /**
     * The Octet 'oct' key type.
     */
    public static final String OCT    = "oct";

    /**
     * All JWK key types.
     */
    public static final List<String> ALL_TYPES = 
            Collections.unmodifiableList(
                    Arrays.asList(new String[] {EC, RSA, RSAHSM, OCT}));

    private JsonWebKeyType() {
        // not instantiable
    }
}
