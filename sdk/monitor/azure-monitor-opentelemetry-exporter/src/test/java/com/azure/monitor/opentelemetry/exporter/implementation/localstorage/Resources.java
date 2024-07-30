// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class Resources {

    static String readString(String resourceName) throws IOException {
        return new String(readBytes(resourceName), StandardCharsets.UTF_8);
    }

    static byte[] readBytes(String resourceName) throws IOException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourceName)) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toByteArray();
        }
    }

    private Resources() {
    }
}
