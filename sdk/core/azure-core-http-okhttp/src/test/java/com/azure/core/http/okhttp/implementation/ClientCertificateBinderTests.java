// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.validation.http.MtlsTestCertificates;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests {@link ClientCertificateBinder}.
 */
public class ClientCertificateBinderTests {
    @Test
    public void boundClientOnlyEnablesTls12() throws IOException {
        OkHttpClient client = ClientCertificateBinder.bindToCertificate(new OkHttpClient(),
            MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_A));

        try (SSLSocket socket = (SSLSocket) client.sslSocketFactory().createSocket()) {
            assertArrayEquals(new String[] { "TLSv1.2" }, socket.getEnabledProtocols());
        }
    }
}
