// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpecificPathCertificatesTest {

    SpecificPathCertificates specificPathCertificates;

    public static String getFilePath(String packageName) {
        String filepath = "\\src\\test\\resources\\" + packageName;
        return System.getProperty("user.dir") + filepath.replace("\\", System.getProperty("file.separator"));
    }

    @Test
    public void testSetCertificateEntry() {
        specificPathCertificates = SpecificPathCertificates.getSpecificPathCertificates(getFilePath("custom\\"));
        Assertions.assertTrue(specificPathCertificates.getAliases().contains("sideload"));
    }
}
