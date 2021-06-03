// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileSystemCertificatesTest {

    FileSystemCertificates fileSystemCertificates;

    public static String getFilePath() {
        String filepath = "\\src\\test\\java\\com\\azure\\security\\keyvault\\jca\\certificate\\";
        return System.getProperty("user.dir") + filepath.replace("\\", System.getProperty("file.separator"));
    }

    @Test
    public void testSetCertificateEntry() {
        fileSystemCertificates = new FileSystemCertificates(getFilePath());
        fileSystemCertificates.loadCertificatesFromFileSystem();
        Assertions.assertTrue(fileSystemCertificates.getAliases().contains("sideload"));
    }

}
