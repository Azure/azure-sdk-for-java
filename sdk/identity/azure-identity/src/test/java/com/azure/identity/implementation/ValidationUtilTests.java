// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.azure.identity.implementation.util.IdentityUtil.isLinuxPlatform;
import static com.azure.identity.implementation.util.IdentityUtil.isWindowsPlatform;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledOnOs({OS.MAC})
public class ValidationUtilTests {
    private static final ClientLogger LOGGER = new ClientLogger(ValidationUtilTests.class);

    private static File good;
    private static File fileTooLong;
    private static File wrongPrefix;
    private static File wrongExtension;
    private static File fileWithRelativeSegments;


    @BeforeAll
    public static void setupClass() {
        Path beginning = null;
        if (isWindowsPlatform()) {
            beginning = Paths.get(System.getenv("ProgramData"), "AzureConnectedMachineAgent", "Tokens");
        } else if (isLinuxPlatform()) {

            beginning = Paths.get("/", "var", "opt", "azcmagent", "tokens");
        }

        good = new TestFile(Paths.get(beginning.toString(), "good.key").toString());
        fileTooLong = new TestFile(Paths.get(beginning.toString(), "fileTooLong.key").toString(), 4097);
        wrongPrefix = new TestFile(Paths.get("wrongPrefix", ".key").toString());
        wrongExtension = new TestFile(Paths.get(beginning.toString(), "wrongExtension.txt").toString());
        fileWithRelativeSegments = new TestFile(Paths.get(beginning.toString(), "..", "file.key").toString());

    }
    @Test
    public void testValidPath() {
        assertDoesNotThrow(() -> ValidationUtil.validateSecretFile(good, LOGGER));
    }

    @Test
    public void testInvalidTooLong() {
        Throwable thrown = assertThrows(ClientAuthenticationException.class, () -> ValidationUtil.validateSecretFile(fileTooLong, LOGGER));
        assertTrue(thrown.getMessage().contains("The secret key file is too large"));
    }

    @Test
    public void testInvalidWrongPrefix() {
        Throwable thrown = assertThrows(ClientAuthenticationException.class, () -> ValidationUtil.validateSecretFile(wrongPrefix, LOGGER));
        assertTrue(thrown.getMessage().contains("The secret key file is not located in the expected directory"));
    }

    @Test
    public void testInvalidWrongExtension() {
        Throwable thrown = assertThrows(ClientAuthenticationException.class, () -> ValidationUtil.validateSecretFile(wrongExtension, LOGGER));
        assertTrue(thrown.getMessage().contains("The secret key file does not have the expected file extension"));
    }

    @Test
    public void testInvalidRelativeSegments() {
        Throwable thrown = assertThrows(ClientAuthenticationException.class, () -> ValidationUtil.validateSecretFile(fileWithRelativeSegments, LOGGER));
        assertTrue(thrown.getMessage().contains("The secret key file is not located in the expected directory"));
    }

    static class TestFile extends File {
        long length = 4096;
        TestFile(String pathname) {
            super(pathname);
        }

        TestFile(String pathName, long length) {
            super(pathName);
            this.length = length;
        }

        @Override
        public long length() {
            return length;
        }
    }
}
