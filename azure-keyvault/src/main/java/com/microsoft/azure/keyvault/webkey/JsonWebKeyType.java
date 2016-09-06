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
