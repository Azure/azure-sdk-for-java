// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.intellij;

import com.azure.identity.implementation.IntelliJAuthMethodDetails;
import com.azure.identity.implementation.IntelliJCacheAccessor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntelliJKDBXDatabaseParsingTest {

    @Test
    public void testKdbxDatabaseParsing() throws Exception {
        InputStream inputStreamwow = new FileInputStream(getPath("c.kdbx"));
        IntelliJKdbxDatabase kdbxDatabase = IntelliJKdbxDatabase.parse(inputStreamwow, "testpassword");
        String password = kdbxDatabase.getDatabaseEntryValue("ADAuthManager");
        assertEquals("DummyEntry", password);
    }

    @Test
    public void testIntelliJAuthDetailsParsing() throws Exception {
        File authFile = new File(getPath("AuthMethodDetails.json"));
        IntelliJCacheAccessor cacheAccessor = new IntelliJCacheAccessor(null);
        IntelliJAuthMethodDetails authMethodDetails = cacheAccessor.parseAuthMethodDetails(authFile);
        assertEquals("dummyuser@email.com", authMethodDetails.getAccountEmail());
        assertEquals("pathFromFile", authMethodDetails.getCredFilePath());
        assertEquals("IDENTITY", authMethodDetails.getAuthMethod());
        assertEquals("AzureCloud", authMethodDetails.getAzureEnv());
    }

    private String getPath(String filename) {

        String path =  getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
