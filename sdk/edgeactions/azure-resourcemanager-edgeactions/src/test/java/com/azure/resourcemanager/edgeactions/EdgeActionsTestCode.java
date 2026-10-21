// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionDeploymentType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class EdgeActionsTestCode {
    private static final String FILE_NAME = "handler.js";

    private EdgeActionsTestCode() {
    }

    static String source() throws IOException {
        try (InputStream stream = EdgeActionsTestCode.class.getResourceAsStream("/edgeactions/" + FILE_NAME)) {
            if (stream == null) {
                throw new IOException("Missing Edge Actions fixture: " + FILE_NAME);
            }
            return new String(readBytes(stream), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }

    static String deploymentContent(EdgeActionVersionDeploymentType deploymentType) throws IOException {
        if (EdgeActionVersionDeploymentType.FILE.equals(deploymentType)) {
            return Base64.getEncoder().encodeToString(source().getBytes(StandardCharsets.UTF_8));
        }
        if (!EdgeActionVersionDeploymentType.ZIP.equals(deploymentType)) {
            throw new IllegalArgumentException("Unsupported test deployment type: " + deploymentType);
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            ZipEntry entry = new ZipEntry(FILE_NAME);
            entry.setTime(0);
            zip.putNextEntry(entry);
            zip.write(source().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    static String downloadedSource(String content) throws IOException {
        String expected = source();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(content)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String downloaded = new String(readBytes(zip), StandardCharsets.UTF_8);
                    if (expected.equals(downloaded)) {
                        return downloaded;
                    }
                }
            }
        }
        throw new IOException("Downloaded version archive does not contain the deployed JavaScript source.");
    }

    private static byte[] readBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = stream.read(buffer)) != -1) {
            bytes.write(buffer, 0, count);
        }
        return bytes.toByteArray();
    }
}
