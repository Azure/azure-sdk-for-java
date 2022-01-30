// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.CoreUtils;
import com.azure.cosmos.implementation.HttpConstants;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureCosmosPropertiesTest {

    @Test(groups = "unit")
    public void verifyAzureCosmosProperties() {
        Map<String, String> properties =
            CoreUtils.getProperties(HttpConstants.Versions.AZURE_COSMOS_PROPERTIES_FILE_NAME);
        assertThat(properties).isNotNull();
        assertThat(properties).isNotEmpty();
        assertThat(properties.get("version")).isNotNull();
        assertThat(properties.get("name")).isNotNull();
    }

    @Test(groups = "unit")
    public void verifyProjectVersion() {
        assertThat(HttpConstants.Versions.getSdkVersion()).isNotNull();
        String pomFileVersion = getVersionFromPomFile();
        assertThat(HttpConstants.Versions.getSdkVersion()).isEqualTo(pomFileVersion);
    }

    private String getVersionFromPomFile() {
        String fileName = "pom.xml";
        String versionStartTag = "<version>";
        String versionEndTag = "</version>";
        File file = new File(fileName);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while((line = bufferedReader.readLine()) != null) {
                if (line.contains(versionStartTag) && line.contains("azure-cosmos")) {
                    int startIndex = line.indexOf(versionStartTag);
                    int endIndex = line.indexOf(versionEndTag);
                    return line.substring(startIndex + versionStartTag.length(), endIndex);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + fileName, e);
        }
        return null;
    }
}
