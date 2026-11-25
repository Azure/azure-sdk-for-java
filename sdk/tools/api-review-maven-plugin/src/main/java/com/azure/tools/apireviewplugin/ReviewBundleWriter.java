// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apireviewplugin;

import com.azure.tools.apiview.processor.model.ApiListing;
import com.azure.tools.apiview.processor.model.MarkdownRenderer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

final class ReviewBundleWriter {
    static final String MARKDOWN_FILE = "api.md";
    static final String METADATA_FILE = "api.metadata.yml";

    interface Mover {
        void move(Path source, Path destination) throws IOException;
    }

    private final Mover mover;

    ReviewBundleWriter() {
        this((source, destination) -> Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING));
    }

    ReviewBundleWriter(Mover mover) {
        this.mover = mover;
    }

    void write(ApiListing listing, String version, String packagePath, Path outputDirectory) throws IOException {
        if (version == null
            || version.trim().isEmpty()
            || version.contains("${")
            || version.contains("\n")
            || version.contains("\r")) {
            throw new IOException("Package version must be a nonempty literal version.");
        }
        packagePath = normalizePackagePath(packagePath);
        String markdown = MarkdownRenderer.render(listing).replace("\r\n", "\n").replace('\r', '\n');
        if (markdown.trim().isEmpty()) {
            throw new IOException("Generated API Markdown is empty.");
        }
        while (markdown.endsWith("\n")) {
            markdown = markdown.substring(0, markdown.length() - 1);
        }
        byte[] markdownBytes = (markdown + "\n").getBytes(StandardCharsets.UTF_8);
        String metadata = "apiHash: " + hash(markdownBytes) + "\n" + "packageVersion: " + quote(version) + "\n"
            + "packageRelativePath: " + quote(packagePath) + "\n";

        Files.createDirectories(outputDirectory);
        Map<Path, byte[]> previous = new LinkedHashMap<>();
        for (String name : new String[] { MARKDOWN_FILE, METADATA_FILE }) {
            Path path = outputDirectory.resolve(name);
            previous.put(path, Files.exists(path) ? Files.readAllBytes(path) : null);
        }
        Path temporary = Files.createTempDirectory(outputDirectory, "api-review-");
        IOException failure = null;
        try {
            Path stagedMarkdown = temporary.resolve(MARKDOWN_FILE);
            Path stagedMetadata = temporary.resolve(METADATA_FILE);
            Files.write(stagedMarkdown, markdownBytes);
            Files.write(stagedMetadata, metadata.getBytes(StandardCharsets.UTF_8));
            mover.move(stagedMarkdown, outputDirectory.resolve(MARKDOWN_FILE));
            mover.move(stagedMetadata, outputDirectory.resolve(METADATA_FILE));
        } catch (IOException exception) {
            failure = exception;
            for (Map.Entry<Path, byte[]> entry : previous.entrySet()) {
                try {
                    if (entry.getValue() == null) {
                        Files.deleteIfExists(entry.getKey());
                    } else {
                        Files.write(entry.getKey(), entry.getValue());
                    }
                } catch (IOException restoreFailure) {
                    exception.addSuppressed(restoreFailure);
                }
            }
            throw exception;
        } finally {
            try {
                Files.deleteIfExists(temporary.resolve(MARKDOWN_FILE));
                Files.deleteIfExists(temporary.resolve(METADATA_FILE));
                Files.delete(temporary);
            } catch (IOException cleanupFailure) {
                if (failure == null) {
                    throw cleanupFailure;
                }
                failure.addSuppressed(cleanupFailure);
            }
        }
    }

    static String normalizePackagePath(String value) {
        value = value.replace('\\', '/');
        if (value.trim().isEmpty()
            || value.startsWith("/")
            || value.matches("^[A-Za-z]:.*")
            || value.contains("\n")
            || value.contains("\r")) {
            throw new IllegalArgumentException("Package path must be a nonempty repository-relative path.");
        }
        for (String segment : value.split("/")) {
            if ("..".equals(segment)) {
                throw new IllegalArgumentException("Package path cannot contain '..'.");
            }
        }
        String normalized = Paths.get(value).normalize().toString().replace('\\', '/');
        return normalized.isEmpty() ? "." : normalized;
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    private static String hash(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder result = new StringBuilder();
            for (byte value : digest) {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
