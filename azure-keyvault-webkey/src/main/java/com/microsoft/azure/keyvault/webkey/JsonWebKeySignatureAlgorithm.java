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
 * Supported JsonWebKey Algorithms.
 */
public final class JsonWebKeySignatureAlgorithm {

    /**
     * The 'RS256' algorithm.
     */
    public static final String RS256  = "RS256";
    
    /**
     * The 'RS384' algorithm.
     */
    public static final String RS384  = "RS384";
    
    /**
     * The 'RS512' algorithm.
     */
    public static final String RS512  = "RS512";
    
    /**
     * The 'RSNULL' algorithm.
     */
    public static final String RSNULL = "RSNULL";

    /**
     * All JWK algorithms.
     */
    public static final List<String> ALL_ALGORITHMS = 
            Collections.unmodifiableList(
                    Arrays.asList(new String[] {RS256, RS384, RS512, RSNULL}));

    private JsonWebKeySignatureAlgorithm() {
        // not instantiable
    }
}
