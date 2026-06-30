// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Stage 2 of the analyzer-authoring loop (single-document-type variant):
 * validate the schema locally, create the analyzer, batch-test inputs, dump
 * per-document JSON + LLM-friendly markdown, and print a stdout summary with
 * per-field fill-rate + avg-confidence. Mirrors Python's create_and_test.py
 * and the .NET CreateAndTestCommand.cs.
 */

package com.azure.ai.contentunderstanding.skills;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.LlmInputHelper;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class CreateAndTestCommand {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Set<String> SUPPORTED_SUFFIXES = new LinkedHashSet<>(
        Arrays.asList(".pdf", ".png", ".jpg", ".jpeg", ".tif", ".tiff", ".bmp", ".heif", ".heic",
            ".wav", ".mp3", ".m4a", ".mp4", ".mov"));

    private CreateAndTestCommand() {
    }

    static int run(String[] args) throws IOException {
        Options opts = parseArgs(args);
        if (opts == null) {
            return 2;
        }
        if (opts.iterations < 1) {
            System.err.println("--iterations must be >= 1");
            return 2;
        }
        Path schemaPath = Paths.get(opts.schema);
        if (!Files.exists(schemaPath)) {
            System.err.println("schema not found: " + schemaPath);
            return 2;
        }
        Path inputPath = Paths.get(opts.input);
        if (!Files.exists(inputPath)) {
            System.err.println("input not found: " + inputPath);
            return 2;
        }
        return runCore(opts, schemaPath, inputPath);
    }

    static int runCore(Options opts, Path schemaPath, Path inputPath) throws IOException {
        // 1. Validate schema locally.
        SchemaValidator.Result validation = SchemaValidator.validateFile(schemaPath);
        if (!validation.isOk()) {
            for (String e : validation.getErrors()) {
                System.err.println("[VALIDATE] " + e);
            }
            return 2;
        }

        String rawJson = Files.readString(schemaPath);
        ObjectNode rawSchema = (ObjectNode) MAPPER.readTree(rawJson);
        ObjectNode schema = stripComments(rawSchema);

        // Pre-flight warning: fieldSchema without models.completion.
        if (schema.has("fieldSchema")) {
            JsonNode models = schema.get("models");
            JsonNode completion = models != null ? models.get("completion") : null;
            if (completion == null || !completion.isTextual() || completion.asText().isBlank()) {
                System.err.println(
                    "[WARN]   schema has fieldSchema but no models.completion; "
                        + "this will fail unless resource defaults are configured "
                        + "(see samples/sample_update_defaults.py).");
            }
        }

        List<Path> inputs = enumerateInputs(inputPath);
        if (inputs.isEmpty()) {
            System.err.println("no supported documents found under " + inputPath);
            return 2;
        }
        Files.createDirectories(Paths.get(opts.output));

        String analyzerId = opts.analyzerId;
        if (analyzerId == null || analyzerId.isEmpty()) {
            String stem = stripExtension(schemaPath.getFileName().toString());
            analyzerId = opts.reuse
                ? stem + "_" + schemaHash(schema)
                : stem + "_" + (System.currentTimeMillis() / 1000L);
        }

        ContentUnderstandingClient client = ExtractLayoutCommand.buildClient();

        boolean reused = false;
        int fail = 0;
        List<NamedDoc> results = new ArrayList<>();
        try {
            if (opts.reuse) {
                reused = ensureAnalyzer(client, analyzerId, schema);
            } else {
                createAnalyzer(client, analyzerId, schema);
            }
            for (Path file : inputs) {
                for (int iter = 1; iter <= opts.iterations; iter++) {
                    String suffix = opts.iterations > 1 ? String.format("_iter%03d", iter) : "";
                    String stem = stripExtension(file.getFileName().toString());
                    Path outPath = Paths.get(opts.output, stem + suffix + ".json");
                    try {
                        System.out.println("[ANALYZE] " + file + " -> " + outPath);
                        AnalyzeResult r = analyzeFile(client, analyzerId, file);
                        Files.writeString(
                            outPath,
                            MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(r.doc));
                        if (r.llmMarkdown != null && !r.llmMarkdown.isEmpty()) {
                            Path llmPath = outPath.resolveSibling(
                                stem + suffix + ".llm.md");
                            Files.writeString(llmPath, r.llmMarkdown);
                        }
                        results.add(new NamedDoc(stripExtension(outPath.getFileName().toString()), r.doc));
                    } catch (Exception ex) {
                        System.err.println("[FAIL]   " + file + ": " + ex.getMessage());
                        fail++;
                    }
                }
            }
        } finally {
            cleanup(client, analyzerId, opts.ephemeral, reused);
        }

        System.out.println(summarize(results));
        return fail == 0 ? 0 : 1;
    }

    // -----------------------------------------------------------------------
    // Service interaction
    // -----------------------------------------------------------------------

    static boolean ensureAnalyzer(ContentUnderstandingClient client, String analyzerId, ObjectNode schema)
        throws IOException {
        try {
            client.getAnalyzer(analyzerId);
            System.out.println("[REUSE]   analyzer " + analyzerId + " already exists");
            return true;
        } catch (HttpResponseException ex) {
            // Treat any error as "not found" — the next createAnalyzer call
            // will surface the real reason if it's something else (auth, etc.)
            // Mirrors Python's broad-except in ensure_analyzer.
        } catch (RuntimeException ex) {
            // Same rationale.
        }
        createAnalyzer(client, analyzerId, schema);
        return false;
    }

    static void createAnalyzer(ContentUnderstandingClient client, String analyzerId, ObjectNode schema)
        throws IOException {
        System.out.println("[CREATE] analyzer_id=" + analyzerId);
        // Use the protocol overload so we can pass arbitrary schema JSON
        // (the typed ContentAnalyzer model might not expose every property).
        BinaryData body = BinaryData.fromString(MAPPER.writeValueAsString(schema));
        SyncPoller<BinaryData, BinaryData> poller =
            client.beginCreateAnalyzer(analyzerId, body, new RequestOptions());
        // Block on completion so any service error surfaces here, not at
        // first analyze call.
        poller.waitForCompletion();
        System.out.println("[CREATE] " + analyzerId + " ready");
    }

    static AnalyzeResult analyzeFile(ContentUnderstandingClient client, String analyzerId, Path file)
        throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        String contentType = guessContentType(file);
        // Protocol overload returns raw service JSON (LRO envelope).
        SyncPoller<BinaryData, BinaryData> poller = client.beginAnalyzeBinary(
            analyzerId,
            contentType,
            BinaryData.fromBytes(bytes),
            new RequestOptions());
        BinaryData raw = poller.getFinalResult();
        JsonNode envelope = MAPPER.readTree(raw.toString());
        // Unwrap `{id, status, result, usage}` so on-disk shape matches
        // Python's `poller.result()` output.
        ObjectNode payload = envelope.has("result")
            ? (ObjectNode) envelope.get("result")
            : (ObjectNode) envelope;

        // Best-effort LLM-friendly markdown. We need a typed AnalysisResult
        // for LlmInputHelper, which requires re-deserializing the unwrapped
        // payload through Jackson into the SDK's BinaryData model loader.
        String llmText = null;
        try {
            AnalysisResult typed = BinaryData.fromString(MAPPER.writeValueAsString(payload))
                .toObject(AnalysisResult.class);
            llmText = LlmInputHelper.toLlmInput(typed);
        } catch (RuntimeException ex) {
            // Raw JSON is always written; LLM text is optional.
        }
        return new AnalyzeResult(payload, llmText);
    }

    static void cleanup(ContentUnderstandingClient client, String analyzerId, boolean ephemeral, boolean reused) {
        if (ephemeral) {
            try {
                System.out.println("[CLEANUP] delete analyzer " + analyzerId);
                client.deleteAnalyzer(analyzerId);
            } catch (RuntimeException ex) {
                System.err.println("[CLEANUP] delete failed: " + ex.getMessage());
            }
        } else if (reused) {
            System.out.println("[KEEP]    reused analyzer " + analyzerId + " retained");
        } else {
            System.out.println(
                "[KEEP]    analyzer " + analyzerId + " retained (use --ephemeral to delete)");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

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
        if (name.endsWith(".wav")) {
            return "audio/wav";
        }
        if (name.endsWith(".mp3")) {
            return "audio/mpeg";
        }
        if (name.endsWith(".m4a")) {
            return "audio/mp4";
        }
        if (name.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (name.endsWith(".mov")) {
            return "video/quicktime";
        }
        return "application/octet-stream";
    }

    /**
     * Recursively drop any object key whose name starts with {@code _}.
     * Lets templates carry {@code _comment} documentation keys without
     * poisoning the service request body.
     */
    static ObjectNode stripComments(ObjectNode obj) {
        ObjectNode out = MAPPER.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            if (e.getKey().startsWith("_")) {
                continue;
            }
            out.set(e.getKey(), stripCommentsNode(e.getValue()));
        }
        return out;
    }

    private static JsonNode stripCommentsNode(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            return stripComments(obj);
        }
        if (node instanceof ArrayNode arr) {
            ArrayNode out = MAPPER.createArrayNode();
            for (JsonNode item : arr) {
                out.add(stripCommentsNode(item));
            }
            return out;
        }
        return node;
    }

    /** Stable 8-char sha1 hash over the canonical JSON form of the schema. */
    static String schemaHash(ObjectNode schema) {
        try {
            String canonical = canonicalize(schema);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("hash failed: " + ex.getMessage(), ex);
        }
    }

    /** Object keys sorted alphabetically; arrays preserve order — same as Python. */
    static String canonicalize(JsonNode node) {
        try {
            return MAPPER.writer().writeValueAsString(sortKeys(node));
        } catch (IOException ex) {
            throw new IllegalStateException("canonicalize failed: " + ex.getMessage(), ex);
        }
    }

    private static JsonNode sortKeys(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            List<String> keys = new ArrayList<>();
            obj.fieldNames().forEachRemaining(keys::add);
            keys.sort(Comparator.naturalOrder());
            ObjectNode out = MAPPER.createObjectNode();
            for (String k : keys) {
                out.set(k, sortKeys(obj.get(k)));
            }
            return out;
        }
        if (node instanceof ArrayNode arr) {
            ArrayNode out = MAPPER.createArrayNode();
            for (JsonNode item : arr) {
                out.add(sortKeys(item));
            }
            return out;
        }
        return node;
    }

    // -----------------------------------------------------------------------
    // Summary
    // -----------------------------------------------------------------------

    /** Build the stdout summary string. */
    static String summarize(List<NamedDoc> results) {
        // category -> field -> list of (docName, value, confidence)
        Map<String, Map<String, List<RowEntry>>> table = new LinkedHashMap<>();
        for (NamedDoc nd : results) {
            for (FieldRecord f : iterFields(nd.doc)) {
                table
                    .computeIfAbsent(f.category, k -> new LinkedHashMap<>())
                    .computeIfAbsent(f.fieldPath, k -> new ArrayList<>())
                    .add(new RowEntry(nd.name, fieldValue(f.fieldVal), fieldConfidence(f.fieldVal)));
            }
        }
        if (table.isEmpty()) {
            return "[SUMMARY] no fields extracted across any document.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(72)).append("\n");
        sb.append("[SUMMARY]\n");
        for (Map.Entry<String, Map<String, List<RowEntry>>> catEntry : table.entrySet()) {
            // Distinct docs across all fields in this category — used for the
            // header count; for fill-rate we use each field's own row count
            // (matches Python/.NET semantics so array leaves don't inflate
            // the denominator).
            Set<String> docNames = new LinkedHashSet<>();
            for (List<RowEntry> rows : catEntry.getValue().values()) {
                for (RowEntry r : rows) {
                    docNames.add(r.docName);
                }
            }
            int nDocs = docNames.size();
            String catLabel = catEntry.getKey().isEmpty() ? "(single)" : catEntry.getKey();
            String header = "category: " + catLabel
                + "  (" + nDocs + " document" + (nDocs == 1 ? "" : "s") + ")";
            sb.append("\n").append(header).append("\n");
            sb.append("-".repeat(header.length())).append("\n");
            sb.append(String.format("  %-40s %-10s %-10s%n", "field", "fill rate", "avg conf"));
            for (Map.Entry<String, List<RowEntry>> field : catEntry.getValue().entrySet()) {
                List<RowEntry> rows = field.getValue();
                int denom = rows.size();
                long filled = rows.stream().filter(r -> r.value != null).count();
                double fillRate = denom == 0 ? 0.0 : (double) filled / denom;
                List<Double> confidences = new ArrayList<>();
                for (RowEntry r : rows) {
                    if (r.value != null && r.confidence != null) {
                        confidences.add(r.confidence);
                    }
                }
                String confStr = confidences.isEmpty() ? "n/a"
                    : String.format("%.3f", confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0));
                sb.append(String.format(
                    "  %-40s %-9s %s%n",
                    field.getKey(),
                    String.format("%.1f%%", fillRate * 100), confStr));
            }
        }

        // Lowest-confidence across all (category, field, doc) triples.
        List<LowConfRow> lows = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<RowEntry>>> catEntry : table.entrySet()) {
            for (Map.Entry<String, List<RowEntry>> field : catEntry.getValue().entrySet()) {
                for (RowEntry r : field.getValue()) {
                    if (r.confidence != null) {
                        lows.add(new LowConfRow(r.confidence, catEntry.getKey(), field.getKey(), r.docName));
                    }
                }
            }
        }
        if (!lows.isEmpty()) {
            lows.sort(Comparator.comparingDouble(x -> x.confidence));
            sb.append("\nlowest-confidence fields:\n");
            for (int i = 0; i < Math.min(3, lows.size()); i++) {
                LowConfRow row = lows.get(i);
                String catTag = row.category.isEmpty() ? "" : "[" + row.category + "] ";
                sb.append(String.format(
                    "  %.3f  %s%s  (%s)%n",
                    row.confidence, catTag, row.field, row.docName));
            }
        }
        sb.append("=".repeat(72));
        return sb.toString();
    }

    static List<FieldRecord> iterFields(ObjectNode doc) {
        List<FieldRecord> out = new ArrayList<>();
        JsonNode contents = doc.get("contents");
        if (contents == null || !contents.isArray()) {
            return out;
        }
        for (JsonNode content : contents) {
            String category = content.has("category") && content.get("category").isTextual()
                ? content.get("category").asText() : "";
            JsonNode fields = content.get("fields");
            if (fields == null || !fields.isObject()) {
                continue;
            }
            Iterator<Map.Entry<String, JsonNode>> it = fields.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                if (!(e.getValue() instanceof ObjectNode fieldObj)) {
                    continue;
                }
                for (PathLeaf pl : recurse(e.getKey(), fieldObj)) {
                    out.add(new FieldRecord(category, pl.path, pl.leaf));
                }
            }
        }
        return out;
    }

    private static List<PathLeaf> recurse(String prefix, ObjectNode fieldVal) {
        List<PathLeaf> out = new ArrayList<>();
        JsonNode arr = fieldVal.get("valueArray");
        if (arr != null && arr.isArray()) {
            for (JsonNode item : arr) {
                if (item instanceof ObjectNode itemObj && itemObj.has("valueObject")
                    && itemObj.get("valueObject") instanceof ObjectNode valueObj) {
                    Iterator<Map.Entry<String, JsonNode>> it = valueObj.fields();
                    while (it.hasNext()) {
                        Map.Entry<String, JsonNode> e = it.next();
                        if (e.getValue() instanceof ObjectNode childObj) {
                            out.addAll(recurse(prefix + "[]." + e.getKey(), childObj));
                        }
                    }
                } else {
                    ObjectNode wrap = MAPPER.createObjectNode();
                    if (item.isTextual()) {
                        wrap.put("valueString", item.asText());
                    } else if (item instanceof ObjectNode io) {
                        wrap = io.deepCopy();
                    }
                    out.add(new PathLeaf(prefix, wrap));
                }
            }
            return out;
        }
        JsonNode obj = fieldVal.get("valueObject");
        if (obj instanceof ObjectNode objVal) {
            Iterator<Map.Entry<String, JsonNode>> it = objVal.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                if (e.getValue() instanceof ObjectNode childObj) {
                    out.addAll(recurse(prefix + "." + e.getKey(), childObj));
                }
            }
            return out;
        }
        out.add(new PathLeaf(prefix, fieldVal));
        return out;
    }

    private static Object fieldValue(ObjectNode field) {
        for (String key : new String[] {
            "valueString", "valueNumber", "valueInteger", "valueBoolean", "valueDate", "valueTime"
        }) {
            JsonNode v = field.get(key);
            if (v != null && !v.isNull() && !(v.isTextual() && v.asText().isEmpty())) {
                return v;
            }
        }
        JsonNode arr = field.get("valueArray");
        if (arr != null && arr.isArray() && arr.size() > 0) {
            return arr;
        }
        JsonNode obj = field.get("valueObject");
        if (obj != null && obj.isObject() && obj.size() > 0) {
            return obj;
        }
        return null;
    }

    private static Double fieldConfidence(ObjectNode field) {
        JsonNode c = field.get("confidence");
        if (c != null && (c.isNumber() || c.canConvertToExactIntegral())) {
            return c.asDouble();
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // CLI parsing
    // -----------------------------------------------------------------------

    private static Options parseArgs(String[] args) {
        Options o = new Options();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--schema":
                    o.schema = args[++i];
                    break;
                case "--input":
                    o.input = args[++i];
                    break;
                case "--output":
                    o.output = args[++i];
                    break;
                case "--analyzer-id":
                    o.analyzerId = args[++i];
                    break;
                case "--iterations":
                    o.iterations = Integer.parseInt(args[++i]);
                    break;
                case "--ephemeral":
                    o.ephemeral = true;
                    break;
                case "--reuse":
                    o.reuse = true;
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
        if (o.schema == null || o.input == null || o.output == null) {
            System.err.println("--schema, --input, and --output are required");
            printUsage();
            return null;
        }
        return o;
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  cu-skill create-and-test");
        System.err.println("    --schema <file>");
        System.err.println("    --input <file-or-folder>");
        System.err.println("    --output <dir>");
        System.err.println("    [--analyzer-id <id>]");
        System.err.println("    [--iterations N]");
        System.err.println("    [--ephemeral]");
        System.err.println("    [--reuse]");
        System.err.println();
        System.err.println("Stage 2 (single-type): validate, create, batch-test a custom analyzer,");
        System.err.println("print a per-field fill-rate + avg-confidence summary.");
    }

    static final class Options {
        String schema;
        String input;
        String output;
        String analyzerId = "";
        int iterations = 1;
        boolean ephemeral;
        boolean reuse;
    }

    static final class AnalyzeResult {
        final ObjectNode doc;
        final String llmMarkdown;

        AnalyzeResult(ObjectNode doc, String llmMarkdown) {
            this.doc = doc;
            this.llmMarkdown = llmMarkdown;
        }
    }

    static final class NamedDoc {
        final String name;
        final ObjectNode doc;

        NamedDoc(String name, ObjectNode doc) {
            this.name = name;
            this.doc = doc;
        }
    }

    static final class FieldRecord {
        final String category;
        final String fieldPath;
        final ObjectNode fieldVal;

        FieldRecord(String category, String fieldPath, ObjectNode fieldVal) {
            this.category = category;
            this.fieldPath = fieldPath;
            this.fieldVal = fieldVal;
        }
    }

    private static final class PathLeaf {
        final String path;
        final ObjectNode leaf;

        PathLeaf(String path, ObjectNode leaf) {
            this.path = path;
            this.leaf = leaf;
        }
    }

    private static final class RowEntry {
        final String docName;
        final Object value;
        final Double confidence;

        RowEntry(String docName, Object value, Double confidence) {
            this.docName = docName;
            this.value = value;
            this.confidence = confidence;
        }
    }

    private static final class LowConfRow {
        final double confidence;
        final String category;
        final String field;
        final String docName;

        LowConfRow(double confidence, String category, String field, String docName) {
            this.confidence = confidence;
            this.category = category;
            this.field = field;
            this.docName = docName;
        }
    }
}
