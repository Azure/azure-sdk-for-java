// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.javadoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Compares source pairs until one rules out test exclusion, without executing the compared classes.
 */
public final class Main {
    private static final int MAX_SOURCE_BYTES = 2 * 1024 * 1024;
    private static final int MAX_ENCODED_SOURCE_LENGTH = ((MAX_SOURCE_BYTES + 2) / 3) * 4;

    private Main() {
    }

    /**
     * Reads numbered, tab-separated Base64 UTF-8 source pairs and emits numbered reason codes.
     *
     * @param args the input manifest path, or {@code --stdio} to read source pairs interactively
     * @throws IOException if the manifest cannot be read or is invalid
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one source-pair manifest path or --stdio.");
        }
        try (BufferedReader reader = "--stdio".equals(args[0])
            ? new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))
            : Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8)) {
            classify(reader, new PrintWriter(System.out, true));
        }
    }

    static void classify(BufferedReader reader, PrintWriter writer) throws IOException {
        DocumentationClassifier classifier = new DocumentationClassifier();
        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\\t", -1);
            if (fields.length != 3 || !Integer.toString(count).equals(fields[0])) {
                throw new IOException("Invalid source-pair manifest record.");
            }
            String reason = classifier.compare(decode(fields[1]), decode(fields[2]));
            writer.println(count + "\t" + reason);
            writer.flush();
            if (writer.checkError()) {
                throw new IOException("Could not write comparison results.");
            }
            count++;
            if (!DocumentationClassifier.isEligible(reason)) {
                return;
            }
        }
        if (count == 0) {
            throw new IOException("The source-pair manifest is empty.");
        }
    }

    private static String decode(String input) throws IOException {
        if (input.length() > MAX_ENCODED_SOURCE_LENGTH) {
            throw new IOException("Source exceeds the 2 MiB limit.");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(input);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid Base64 source input.", exception);
        }
        if (bytes.length > MAX_SOURCE_BYTES) {
            throw new IOException("Source exceeds the 2 MiB limit.");
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException exception) {
            throw new IOException("Source is not valid UTF-8.", exception);
        }
    }
}
