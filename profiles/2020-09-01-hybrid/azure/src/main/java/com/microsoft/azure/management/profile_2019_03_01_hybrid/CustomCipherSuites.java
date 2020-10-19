/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.profile_2019_03_01_hybrid;

import okhttp3.CipherSuite;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Collects cipher suites in OkHttp for needed.
 */
public class CustomCipherSuites {
    // Copy from OkHttp https://github.com/square/okhttp/blob/parent-3.12.8/okhttp/src/main/java/okhttp3/ConnectionSpec.java#L63
    public static final CipherSuite[] APPROVED_CIPHER_SUITES = new CipherSuite[] {
        // TLSv1.3 // Not provide as enum for 3.11.0
        CipherSuite.forJavaName("TLS_AES_128_GCM_SHA256"),
        CipherSuite.forJavaName("TLS_AES_256_GCM_SHA384"),
        CipherSuite.forJavaName("TLS_CHACHA20_POLY1305_SHA256"),
        CipherSuite.forJavaName("TLS_AES_128_CCM_SHA256"),
        CipherSuite.forJavaName("TLS_AES_256_CCM_8_SHA256"),

        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
        CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
        CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,

        // Note that the following cipher suites are all on HTTP/2's bad cipher suites list. We'll
        // continue to include them until better suites are commonly available. For example, none
        // of the better cipher suites listed above shipped with Android 4.4 or Java 7.
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
        CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
        CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
        CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
        CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
        CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
    };

    public static final CipherSuite[] AZURE_STACK = new CipherSuite[] {
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
        CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
        CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256,
    };

    public static final CipherSuite[] ALL_CIPHER_SUITES = ArrayUtils.addAll(APPROVED_CIPHER_SUITES, AZURE_STACK);
}
