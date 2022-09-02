/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
        NetworkFriendlyExceptions.CipherExceptionDetector cipherExceptionDetector =
            new NetworkFriendlyExceptions.CipherExceptionDetector(existingCiphers);
        assertThat(cipherExceptionDetector.detect(ioException)).isEqualTo(true);
    }

    @Test
    public void testCipherExceptionDetectorWithCiphers() {
        Exception ioException = new IOException();
        List<String> existingCiphers = Arrays.asList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        NetworkFriendlyExceptions.CipherExceptionDetector cipherExceptionDetector =
            new NetworkFriendlyExceptions.CipherExceptionDetector(existingCiphers);
        assertThat(cipherExceptionDetector.detect(ioException)).isEqualTo(false);
    }

    @Test
    public void testSslExceptionDetectorWithWrongMessage() {
        Exception sslException = new SSLHandshakeException("sample");
        NetworkFriendlyExceptions.SslExceptionDetector sslExceptionDetector =
            new NetworkFriendlyExceptions.SslExceptionDetector();
        assertThat(sslExceptionDetector.detect(sslException)).isEqualTo(false);
    }

    @Test
    public void testSslExceptionDetectorWithRightMessage() {
        Exception sslException =
            new SSLHandshakeException(
                "stuff: unable to find valid certification path to requested target");
        NetworkFriendlyExceptions.SslExceptionDetector sslExceptionDetector =
            new NetworkFriendlyExceptions.SslExceptionDetector();
        assertThat(sslExceptionDetector.detect(sslException)).isEqualTo(true);
    }

    @Test
    public void testUnknownHostExceptionDetector() {
        Exception unknownHostException = new UnknownHostException("sample");
        NetworkFriendlyExceptions.UnknownHostExceptionDetector unknownHostExceptionDetector =
            new NetworkFriendlyExceptions.UnknownHostExceptionDetector();
        assertThat(unknownHostExceptionDetector.detect(unknownHostException)).isEqualTo(true);
    }
}
