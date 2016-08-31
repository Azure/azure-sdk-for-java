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
public final class JsonWebKeyEncryptionAlgorithm {

    /**
     * The 'RSA-OAEP' algorithm.
     */
    public static final String RSAOAEP = "RSA-OAEP";
    
    /**
     * The 'RSA1_5' algorithm.
     */
    public static final String RSA15   = "RSA1_5";

    /**
     * All the JWK encryption algorithms.
     */
    public static final List<String> ALL_ALGORITHMS = 
            Collections.unmodifiableList(
                    Arrays.asList(new String[] {RSA15, RSAOAEP}));

    private JsonWebKeyEncryptionAlgorithm() {
        // not instantiable
    }

}
