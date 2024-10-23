// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkFriendlyExceptionsTest {

    @Test
    public void testCipherExceptionDetectorWithNoCiphers() {
        Exception ioException = new IOException();
        List<String> existingCiphers = new ArrayList<>();
        NetworkFriendlyExceptions.CipherExceptionDetector cipherExceptionDetector
            = new NetworkFriendlyExceptions.CipherExceptionDetector(existingCiphers);
        assertThat(cipherExceptionDetector.detect(ioException)).isEqualTo(true);
    }

    @Test
    public void testCipherExceptionDetectorWithCiphers() {
        Exception ioException = new IOException();
        List<String> existingCiphers = Arrays.asList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        NetworkFriendlyExceptions.CipherExceptionDetector cipherExceptionDetector
            = new NetworkFriendlyExceptions.CipherExceptionDetector(existingCiphers);
        assertThat(cipherExceptionDetector.detect(ioException)).isEqualTo(false);
    }

    @Test
    public void testSslExceptionDetectorWithWrongMessage() {
        Exception sslException = new SSLHandshakeException("sample");
        NetworkFriendlyExceptions.SslExceptionDetector sslExceptionDetector
            = new NetworkFriendlyExceptions.SslExceptionDetector();
        assertThat(sslExceptionDetector.detect(sslException)).isEqualTo(false);
    }

    @Test
    public void testSslExceptionDetectorWithRightMessage() {
        Exception sslException
            = new SSLHandshakeException("stuff: unable to find valid certification path to requested target");
        NetworkFriendlyExceptions.SslExceptionDetector sslExceptionDetector
            = new NetworkFriendlyExceptions.SslExceptionDetector();
        assertThat(sslExceptionDetector.detect(sslException)).isEqualTo(true);
    }

    @Test
    public void testUnknownHostExceptionDetector() {
        Exception unknownHostException = new UnknownHostException("sample");
        NetworkFriendlyExceptions.UnknownHostExceptionDetector unknownHostExceptionDetector
            = new NetworkFriendlyExceptions.UnknownHostExceptionDetector();
        assertThat(unknownHostExceptionDetector.detect(unknownHostException)).isEqualTo(true);
    }
}
