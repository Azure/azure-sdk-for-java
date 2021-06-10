// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PackageVersionChecker {

    private static final String VERSION = getVersionFromPomFile();

    public static boolean isBeta() {
        return VERSION.contains("beta");
    }

    private static String getVersionFromPomFile() {
        String fileName = "pom.xml";
        File file = new File(fileName);
        Map doc = null;
        try {
            doc = new XmlMapper().readValue(file, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (String) doc.get("version");
    }
}
