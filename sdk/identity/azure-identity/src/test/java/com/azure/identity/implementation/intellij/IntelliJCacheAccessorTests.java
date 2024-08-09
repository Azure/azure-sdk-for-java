// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.intellij;

import com.azure.identity.implementation.IntelliJCacheAccessor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntelliJCacheAccessorTests {
    @Test
    public void getRefreshSecret() {

        String json = null;
        try {
            json = new String(Files.readAllBytes(Paths.get(getPath("IntelliJCache.json"))), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntelliJCacheAccessor accessor = new IntelliJCacheAccessor(null);


        String secret = accessor.parseRefreshTokenFromJson(json);

        assertEquals("refresh_fake_secret", secret);
    }

    private String getPath(String filename) {
        String path =  getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
