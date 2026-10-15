// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests to validate that CallRecording rejects invalid/malicious URLs
 * before sending credentials (SSRF protection).
 */
public class DownloadContentInvalidUrlTests {

    private static final String CONTENT = "VideoContents";
    private CallRecording callRecording;

    @BeforeEach
    public void setUp() {
        CallAutomationClient callAutomationClient
            = CallAutomationUnitTestBase.getCallAutomationClient(new ArrayList<>(Collections
                .singletonList(new SimpleEntry<>(CallAutomationUnitTestBase.generateDownloadResult(CONTENT), 200))));
        callRecording = callAutomationClient.getCallRecording();
    }

    // Test non-HTTPS protocols (HTTP)
    @Test
    public void downloadToWithHttpUrlthrowsIllegalArgumentException() {
        String httpUrl = "http://url.asm.skype.com/recording/file.mp4";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            callRecording.downloadTo(httpUrl, stream);
        });

        assertTrue(exception.getMessage().contains("must use HTTPS scheme for security"),
            "Expected error message about HTTPS requirement");
    }

    @Test
    public void downloadToWithResponsewithHttpUrlthrowsIllegalArgumentException() {

        String httpUrl = "http://url.asm.skype.com/recording/file.mp4";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            callRecording.downloadTo(httpUrl, stream, null, Context.NONE);
        });

        assertTrue(exception.getMessage().contains("must use HTTPS scheme for security"));
    }

    // Test malicious domain (SSRF attack vector)
    @Test
    public void downloadToWithMaliciousDomainthrowsIllegalArgumentException() {
        String maliciousUrl = "https://attacker.com/steal-credentials";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            callRecording.downloadTo(maliciousUrl, stream);
        });

        assertTrue(exception.getMessage().contains("is not a valid Azure Communication Services recording endpoint"),
            "Expected error message about invalid endpoint");
        assertTrue(exception.getMessage().contains("*.asm.skype.com"));
        assertTrue(exception.getMessage().contains("*.asyncgw.teams.microsoft.com"));
    }

    @Test
    public void downloadToWithResponsewithMaliciousDomainthrowsIllegalArgumentException() {

        String maliciousUrl = "https://evil.example.com/exfiltrate-tokens";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            callRecording.downloadTo(maliciousUrl, stream, null, Context.NONE);
        });

        assertTrue(exception.getMessage().contains("is not a valid Azure Communication Services recording endpoint"));
    }

    // Test IP address literals (SSRF attack vectors)
    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://127.0.0.1/recording/file.mp4",
            "https://192.168.1.1/recording/file.mp4",
            "https://10.0.0.1/recording/file.mp4" })
    public void downloadTowithIpLiteralsthrowsIllegalArgumentException(String ipUrl) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            callRecording.downloadTo(ipUrl, stream);
        });

        assertTrue(exception.getMessage().contains("is not a valid Azure Communication Services recording endpoint"),
            "IP literals should be rejected to prevent SSRF attacks");
    }

    // Test valid URL to ensure we don't break legitimate scenarios
    @Test
    public void downloadToWithValidAsmSkypeUrlsucceeds() throws IOException {

        String validUrl = "https://example.asm.skype.com/recording/file.mp4";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        callRecording.downloadTo(validUrl, stream);

        assertTrue(true, "Valid URL should be accepted");
    }

    @Test
    public void downloadToWithValidTeamsUrlsucceeds() throws IOException {

        String validUrl = "https://region.asyncgw.teams.microsoft.com/recording/file.mp4";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        callRecording.downloadTo(validUrl, stream);
        assertTrue(true, "Valid Teams URL should be accepted");
    }
}
