// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Stage 1 of the analyzer-authoring loop: extract document layout into
 * <file>.layout.{json,md} files. Mirrors Python's extract_layout.py and the
 * .NET ExtractLayoutCommand.cs.
 *
 * Defaults to analyzerId = prebuilt-documentSearch (richer markdown than
 * prebuilt-document) so the layout output is useful as Copilot context.
 */

package com.azure.ai.contentunderstanding.skills;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class ExtractLayoutCommand {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Set<String> SUPPORTED_SUFFIXES = new LinkedHashSet<>(
        Arrays.asList(".pdf", ".png", ".jpg", ".jpeg", ".tif", ".tiff", ".bmp", ".heif", ".heic"));

    private ExtractLayoutCommand() {
    }

    static int run(String[] args) throws IOException {
        Options opts = parseArgs(args);
        if (opts == null) {
            return 2;
        }

        Path input = Paths.get(opts.input);
        Path output = Paths.get(opts.output);
        if (!Files.exists(input)) {
            System.err.println("input not found: " + input);
            return 2;
        }
        Files.createDirectories(output);

        List<Path> files = enumerateInputs(input);
        if (files.isEmpty()) {
            System.err.println("no supported documents found under " + input);
            return 2;
        }

        ContentUnderstandingClient client = buildClient();
        int ok = 0;
        int fail = 0;
        for (Path file : files) {
            String stem = stripExtension(file.getFileName().toString());
            try {
                System.out.println("[RUN ] " + file + " -> " + output + "/" + stem + ".layout.{json,md}");
                byte[] bytes = Files.readAllBytes(file);
                String contentType = guessContentType(file);

                SyncPoller<BinaryData, BinaryData> poller = client.beginAnalyzeBinary(
                    opts.analyzerId,
                    contentType,
                    BinaryData.fromBytes(bytes),
                    new RequestOptions());
                // Unwrap the LRO envelope `{id, status, result, usage}` so the
                // on-disk shape matches the Python skill output (just the
                // analysis result, like `poller.result()` in Python).
                JsonNode envelope = MAPPER.readTree(poller.getFinalResult().toString());
                JsonNode payload = envelope.has("result") ? envelope.get("result") : envelope;

                Files.writeString(
                    output.resolve(stem + ".layout.json"),
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(payload));

                String markdown = extractMarkdown(payload);
                Files.writeString(output.resolve(stem + ".layout.md"), markdown);
                ok++;
            } catch (Exception ex) {
                System.err.println("[FAIL] " + file + ": " + ex.getMessage());
                fail++;
            }
        }

        System.out.println();
        System.out.println("[DONE] " + ok + " ok, " + fail + " failed; output -> " + output);
        return fail == 0 ? 0 : 1;
    }

    private static String extractMarkdown(JsonNode payload) {
        JsonNode contents = payload.get("contents");
        if (contents != null && contents.isArray()) {
            for (JsonNode c : contents) {
                JsonNode md = c.get("markdown");
                if (md != null && md.isTextual() && !md.asText().isEmpty()) {
                    return md.asText();
                }
            }
        }
        return "";
    }

    private static String guessContentType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (name.endsWith(".tif") || name.endsWith(".tiff")) {
            return "image/tiff";
        }
        if (name.endsWith(".bmp")) {
            return "image/bmp";
        }
        if (name.endsWith(".heif") || name.endsWith(".heic")) {
            return "image/heif";
        }
        return "application/octet-stream";
    }

    private static List<Path> enumerateInputs(Path inputPath) throws IOException {
        List<Path> result = new ArrayList<>();
        if (Files.isRegularFile(inputPath)) {
            result.add(inputPath);
            return result;
        }
        try (var stream = Files.list(inputPath)) {
            stream
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                    int dot = n.lastIndexOf('.');
                    return dot >= 0 && SUPPORTED_SUFFIXES.contains(n.substring(dot));
                })
                .sorted()
                .forEach(result::add);
        }
        return result;
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    static ContentUnderstandingClient buildClient() {
        String endpoint = readEnv("CONTENTUNDERSTANDING_ENDPOINT");
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException(
                "CONTENTUNDERSTANDING_ENDPOINT is not set. Configure your .env file (see cu-sdk-setup).");
        }
        // Strip trailing slash to match the convention from samples — avoids
        // double-slash URLs when the env var was copy-pasted from the portal.
        while (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder()
            .endpoint(endpoint);

        String key = readEnv("CONTENTUNDERSTANDING_KEY");
        if (key != null && !key.isBlank()) {
            builder.credential(new AzureKeyCredential(key));
        } else {
            // DefaultAzureCredentialBuilder in the Java SDK does not expose
            // an exclude API (unlike .NET), and its ManagedIdentity probe
            // blocks for ~30s on dev boxes (WSL, laptops) before timing out.
            // Build a focused chain: Environment first (CI), then Azure CLI
            // (dev boxes). This covers both worlds without the IMDS stall.
            TokenCredential cred = new ChainedTokenCredentialBuilder()
                .addLast(new EnvironmentCredentialBuilder().build())
                .addLast(new AzureCliCredentialBuilder().build())
                .build();
            builder.credential(cred);
        }
        return builder.buildClient();
    }

    /**
     * Reads an env var and strips one layer of surrounding single or double
     * quotes — `python-dotenv` strips them by default but raw `export` from a
     * shell does not, so users who source .env manually still get a clean value.
     */
    static String readEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() >= 2
            && ((value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
                || (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\''))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static Options parseArgs(String[] args) {
        String input = null;
        String output = null;
        String analyzerId = "prebuilt-documentSearch";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--input":
                    input = args[++i];
                    break;
                case "--output":
                    output = args[++i];
                    break;
                case "--analyzer-id":
                    analyzerId = args[++i];
                    break;
                case "-h":
                case "--help":
                    printUsage();
                    return null;
                default:
                    System.err.println("unknown argument: " + args[i]);
                    printUsage();
                    return null;
            }
        }
        if (input == null || output == null) {
            System.err.println("--input and --output are required");
            printUsage();
            return null;
        }
        Options o = new Options();
        o.input = input;
        o.output = output;
        o.analyzerId = analyzerId;
        return o;
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  cu-skill extract-layout --input <file-or-folder> --output <dir> [--analyzer-id <id>]");
        System.err.println();
        System.err.println("Stage 1: extract layout JSON + markdown for each input document.");
    }

    private static final class Options {
        String input;
        String output;
        String analyzerId;
    }
}
