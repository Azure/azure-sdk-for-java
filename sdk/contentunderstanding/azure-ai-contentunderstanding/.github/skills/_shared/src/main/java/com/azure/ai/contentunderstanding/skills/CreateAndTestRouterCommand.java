// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Stage 2 of the analyzer-authoring loop (classify-and-route variant):
 * given an outer classifier schema and N inner field-extractor schemas,
 * create all of them, wire the outer's `contentCategories[*].analyzerId`
 * to the real inner analyzer IDs, batch-test inputs, and print a
 * category-aware stdout summary. Mirrors Python's
 * create_and_test_router.py and the .NET CreateAndTestRouterCommand.cs.
 */

package com.azure.ai.contentunderstanding.skills;

import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.BinaryData;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.nio.charset.StandardCharsets;

final class CreateAndTestRouterCommand {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Set<String> SUPPORTED_SUFFIXES = new LinkedHashSet<>(
        Arrays.asList(".pdf", ".png", ".jpg", ".jpeg", ".tif", ".tiff", ".bmp", ".heif", ".heic",
            ".wav", ".mp3", ".m4a", ".mp4", ".mov"));

    private CreateAndTestRouterCommand() {
    }

    static int run(String[] args) throws IOException {
        Options opts = parseArgs(args);
        if (opts == null) {
            return 2;
        }
        Path outerPath = Paths.get(opts.outerSchema);
        if (!Files.exists(outerPath)) {
            System.err.println("outer schema not found: " + outerPath);
            return 2;
        }
        Path inputPath = Paths.get(opts.input);
        if (!Files.exists(inputPath)) {
            System.err.println("input not found: " + inputPath);
            return 2;
        }

        // 1. Resolve inner schemas: either --inner-schema ALIAS=PATH ... or
        //    --schema-dir DIR (which auto-discovers *.json files and uses the
        //    file stem as the alias).
        Map<String, Path> aliasToPath = new LinkedHashMap<>();
        for (String spec : opts.innerSchemas) {
            int eq = spec.indexOf('=');
            if (eq <= 0) {
                System.err.println("--inner-schema must be ALIAS=PATH; got: " + spec);
                return 2;
            }
            String alias = spec.substring(0, eq);
            Path p = Paths.get(spec.substring(eq + 1));
            if (!Files.exists(p)) {
                System.err.println("inner schema not found: " + p);
                return 2;
            }
            aliasToPath.put(alias, p);
        }
        if (opts.schemaDir != null) {
            Path dir = Paths.get(opts.schemaDir);
            if (!Files.isDirectory(dir)) {
                System.err.println("--schema-dir is not a directory: " + dir);
                return 2;
            }
            try (var stream = Files.list(dir)) {
                stream
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(p -> aliasToPath.putIfAbsent(
                        stripExtension(p.getFileName().toString()), p));
            }
        }
        if (aliasToPath.isEmpty()) {
            System.err.println("provide at least one --inner-schema or --schema-dir");
            return 2;
        }

        // 2. Validate outer + each inner locally.
        SchemaValidator.Result outerVal = SchemaValidator.validateFile(outerPath);
        if (!outerVal.isOk()) {
            for (String e : outerVal.getErrors()) {
                System.err.println("[VALIDATE outer] " + e);
            }
            return 2;
        }
        ObjectNode outerSchema = CreateAndTestCommand.stripComments(
            (ObjectNode) MAPPER.readTree(Files.readString(outerPath)));

        Map<String, ObjectNode> aliasToSchema = new LinkedHashMap<>();
        for (Map.Entry<String, Path> e : aliasToPath.entrySet()) {
            SchemaValidator.Result r = SchemaValidator.validateFile(e.getValue());
            if (!r.isOk()) {
                for (String msg : r.getErrors()) {
                    System.err.println("[VALIDATE inner " + e.getKey() + "] " + msg);
                }
                return 2;
            }
            aliasToSchema.put(
                e.getKey(),
                CreateAndTestCommand.stripComments(
                    (ObjectNode) MAPPER.readTree(Files.readString(e.getValue()))));
        }

        // Cross-check: every contentCategories[alias].analyzerId placeholder
        // in the outer schema should have a matching inner alias.
        JsonNode cats = outerSchema.path("config").path("contentCategories");
        if (cats.isObject()) {
            Iterator<String> it = cats.fieldNames();
            while (it.hasNext()) {
                String cat = it.next();
                JsonNode catEntry = cats.get(cat);
                if (catEntry.has("analyzerId") && !aliasToSchema.containsKey(cat)) {
                    System.err.println(
                        "[VALIDATE] outer category '" + cat
                            + "' references an inner analyzer but no --inner-schema with that alias was provided");
                    return 2;
                }
            }
        }

        List<Path> inputs = enumerateInputs(inputPath);
        if (inputs.isEmpty()) {
            System.err.println("no supported documents found under " + inputPath);
            return 2;
        }
        Files.createDirectories(Paths.get(opts.output));

        // 3. Compute deterministic IDs (or timestamp IDs when --reuse not set).
        String outerId = opts.analyzerId;
        if (outerId == null || outerId.isEmpty()) {
            String stem = stripExtension(outerPath.getFileName().toString());
            // Include the inner hashes in the outer hash so any inner-schema
            // edit forces a fresh outer ID. Otherwise --reuse could pick up
            // an outer that points at stale inner IDs.
            StringBuilder combined = new StringBuilder();
            combined.append(CreateAndTestCommand.canonicalize(outerSchema));
            for (Map.Entry<String, ObjectNode> e : new TreeMap<>(aliasToSchema).entrySet()) {
                combined.append("|").append(e.getKey()).append("=")
                    .append(CreateAndTestCommand.canonicalize(e.getValue()));
            }
            outerId = opts.reuse
                ? stem + "_" + sha1Prefix(combined.toString())
                : stem + "_" + (System.currentTimeMillis() / 1000L);
        }

        Map<String, String> aliasToId = new LinkedHashMap<>();
        for (Map.Entry<String, ObjectNode> e : aliasToSchema.entrySet()) {
            String suffix = opts.reuse
                ? sha1Prefix(CreateAndTestCommand.canonicalize(e.getValue()))
                : Long.toString(System.currentTimeMillis() / 1000L);
            aliasToId.put(e.getKey(), outerId + "_inner_" + e.getKey() + "_" + suffix);
        }

        // 4. Patch outer schema to point at the real inner IDs.
        ObjectNode wiredOuter = wireInnerIds(outerSchema, aliasToId);

        ContentUnderstandingClient client = ExtractLayoutCommand.buildClient();
        boolean reused = false;
        int fail = 0;
        List<CreateAndTestCommand.NamedDoc> results = new ArrayList<>();
        try {
            // 5. Create all inner analyzers first.
            for (Map.Entry<String, ObjectNode> e : aliasToSchema.entrySet()) {
                String id = aliasToId.get(e.getKey());
                if (opts.reuse) {
                    CreateAndTestCommand.ensureAnalyzer(client, id, e.getValue());
                } else {
                    CreateAndTestCommand.createAnalyzer(client, id, e.getValue());
                }
            }
            // 6. Then the outer classifier.
            if (opts.reuse) {
                reused = CreateAndTestCommand.ensureAnalyzer(client, outerId, wiredOuter);
            } else {
                CreateAndTestCommand.createAnalyzer(client, outerId, wiredOuter);
            }

            // 7. Analyze inputs through the outer.
            for (Path file : inputs) {
                String stem = stripExtension(file.getFileName().toString());
                Path outPath = Paths.get(opts.output, stem + ".json");
                try {
                    System.out.println("[ANALYZE] " + file + " -> " + outPath);
                    CreateAndTestCommand.AnalyzeResult r =
                        CreateAndTestCommand.analyzeFile(client, outerId, file);
                    Files.writeString(
                        outPath,
                        MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(r.doc));
                    if (r.llmMarkdown != null && !r.llmMarkdown.isEmpty()) {
                        Files.writeString(
                            outPath.resolveSibling(stem + ".llm.md"),
                            r.llmMarkdown);
                    }
                    results.add(new CreateAndTestCommand.NamedDoc(stem, r.doc));
                } catch (Exception ex) {
                    System.err.println("[FAIL]    " + file + ": " + ex.getMessage());
                    fail++;
                }
            }
        } finally {
            if (opts.ephemeral) {
                List<String> toDelete = new ArrayList<>();
                toDelete.add(outerId);
                toDelete.addAll(aliasToId.values());
                for (String id : toDelete) {
                    try {
                        System.out.println("[CLEANUP] delete " + id);
                        client.deleteAnalyzer(id);
                    } catch (RuntimeException ex) {
                        System.err.println("[CLEANUP] delete failed for " + id + ": " + ex.getMessage());
                    }
                }
            } else if (reused) {
                System.out.println("[KEEP]    reused analyzers retained");
            } else {
                System.out.println("[KEEP]    analyzers retained (use --ephemeral to delete)");
                System.out.println("           outer: " + outerId);
                for (Map.Entry<String, String> e : aliasToId.entrySet()) {
                    System.out.println("           inner [" + e.getKey() + "]: " + e.getValue());
                }
            }
        }

        System.out.println(summarizeRouted(results));
        return fail == 0 ? 0 : 1;
    }

    /**
     * Replace each {@code contentCategories[alias].analyzerId} placeholder
     * in the outer schema with the real inner analyzer ID.
     */
    static ObjectNode wireInnerIds(ObjectNode outerSchema, Map<String, String> aliasToId) {
        ObjectNode out = outerSchema.deepCopy();
        JsonNode config = out.get("config");
        if (!(config instanceof ObjectNode configObj)) {
            return out;
        }
        JsonNode cats = configObj.get("contentCategories");
        if (!(cats instanceof ObjectNode catsObj)) {
            return out;
        }
        Iterator<String> it = catsObj.fieldNames();
        List<String> aliases = new ArrayList<>();
        it.forEachRemaining(aliases::add);
        for (String alias : aliases) {
            JsonNode entry = catsObj.get(alias);
            if (entry instanceof ObjectNode entryObj && aliasToId.containsKey(alias)) {
                entryObj.put("analyzerId", aliasToId.get(alias));
            }
        }
        return out;
    }

    /**
     * Like {@link CreateAndTestCommand#summarize}, but groups by the
     * {@code category} field that classify-and-route stamps on each segment
     * and uses per-category segment counts as the fill-rate denominator.
     */
    static String summarizeRouted(List<CreateAndTestCommand.NamedDoc> results) {
        // category -> field -> list[(docName, value, confidence)]
        Map<String, Map<String, List<RowEntry>>> table = new LinkedHashMap<>();
        Map<String, Integer> segmentsPerCategory = new LinkedHashMap<>();
        for (CreateAndTestCommand.NamedDoc nd : results) {
            // Count segments per category in this document.
            Map<String, Integer> segCounts = new LinkedHashMap<>();
            JsonNode contents = nd.doc.get("contents");
            if (contents != null && contents.isArray()) {
                for (JsonNode c : contents) {
                    String cat = c.has("category") && c.get("category").isTextual()
                        ? c.get("category").asText() : "";
                    segCounts.merge(cat, 1, Integer::sum);
                }
            }
            for (Map.Entry<String, Integer> sc : segCounts.entrySet()) {
                segmentsPerCategory.merge(sc.getKey(), sc.getValue(), Integer::sum);
            }
            for (CreateAndTestCommand.FieldRecord f : CreateAndTestCommand.iterFields(nd.doc)) {
                table
                    .computeIfAbsent(f.category, k -> new LinkedHashMap<>())
                    .computeIfAbsent(f.fieldPath, k -> new ArrayList<>())
                    .add(new RowEntry(nd.name, fieldValueRouted(f.fieldVal), fieldConfRouted(f.fieldVal)));
            }
        }
        if (table.isEmpty()) {
            return "[SUMMARY] (category-aware) no fields extracted across any document.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(72)).append("\n");
        sb.append("[SUMMARY] (category-aware)\n");
        // Sort categories alphabetically for stable output.
        List<String> cats = new ArrayList<>(table.keySet());
        cats.sort(Comparator.naturalOrder());
        for (String cat : cats) {
            String label = cat.isEmpty() ? "(uncategorized)" : cat;
            int segCount = segmentsPerCategory.getOrDefault(cat, 0);
            sb.append("\ncategory: ").append(label).append("  (")
                .append(segCount).append(" segment").append(segCount == 1 ? "" : "s").append(")\n");
            String header = "category: " + label;
            sb.append("-".repeat(header.length())).append("\n");
            sb.append(String.format("  %-30s %-10s %-10s%n", "field", "fill rate", "avg conf"));
            // Sort fields alphabetically.
            List<Map.Entry<String, List<RowEntry>>> fieldEntries =
                new ArrayList<>(table.get(cat).entrySet());
            fieldEntries.sort(Map.Entry.comparingByKey());
            for (Map.Entry<String, List<RowEntry>> field : fieldEntries) {
                // Use the field's own row count as the denominator so array
                // leaves don't inflate the rate. Mirrors single-type
                // create-and-test and matches Python/.NET semantics.
                List<RowEntry> rows = field.getValue();
                int denom = rows.size();
                long filled = rows.stream().filter(r -> r.value != null).count();
                double fillRate = denom == 0 ? 0.0 : (double) filled / denom;
                List<Double> confs = new ArrayList<>();
                for (RowEntry r : rows) {
                    if (r.value != null && r.confidence != null) {
                        confs.add(r.confidence);
                    }
                }
                String confStr = confs.isEmpty() ? "n/a"
                    : String.format("%.3f", confs.stream().mapToDouble(Double::doubleValue).average().orElse(0));
                sb.append(String.format(
                    "  %-30s %-9s %s%n",
                    field.getKey(),
                    String.format("%.1f%%", fillRate * 100), confStr));
            }
        }
        // Lowest-confidence triples (category, field, doc) across all results.
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
            sb.append("\nlowest-confidence fields across all categories:\n");
            for (int i = 0; i < Math.min(3, lows.size()); i++) {
                LowConfRow row = lows.get(i);
                sb.append(String.format(
                    "  %.3f  [%s] %s  (%s)%n",
                    row.confidence, row.category, row.field, row.docName));
            }
        }
        sb.append("=".repeat(72));
        return sb.toString();
    }

    private static Object fieldValueRouted(ObjectNode field) {
        // Same logic as CreateAndTestCommand but private to keep the
        // helper graph easy to reason about.
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

    private static Double fieldConfRouted(ObjectNode field) {
        JsonNode c = field.get("confidence");
        if (c != null && (c.isNumber() || c.canConvertToExactIntegral())) {
            return c.asDouble();
        }
        return null;
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

    private static String sha1Prefix(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("sha1 failed: " + ex.getMessage(), ex);
        }
    }

    // -----------------------------------------------------------------------
    // CLI parsing
    // -----------------------------------------------------------------------

    private static Options parseArgs(String[] args) {
        Options o = new Options();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--outer-schema":
                    o.outerSchema = args[++i];
                    break;
                case "--inner-schema":
                    o.innerSchemas.add(args[++i]);
                    break;
                case "--schema-dir":
                    o.schemaDir = args[++i];
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
        if (o.outerSchema == null || o.input == null || o.output == null) {
            System.err.println("--outer-schema, --input, and --output are required");
            printUsage();
            return null;
        }
        if (o.innerSchemas.isEmpty() && o.schemaDir == null) {
            System.err.println("provide at least one --inner-schema ALIAS=PATH or --schema-dir DIR");
            printUsage();
            return null;
        }
        return o;
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  cu-skill create-and-test-router");
        System.err.println("    --outer-schema <file>");
        System.err.println("    (--inner-schema ALIAS=PATH [--inner-schema ...] | --schema-dir <dir>)");
        System.err.println("    --input <file-or-folder>");
        System.err.println("    --output <dir>");
        System.err.println("    [--analyzer-id <id>]");
        System.err.println("    [--ephemeral]");
        System.err.println("    [--reuse]");
        System.err.println();
        System.err.println("Classify-and-route Stage 2: validate, create N inner analyzers + 1");
        System.err.println("outer classifier, batch-test, print a category-aware summary.");
    }

    static final class Options {
        String outerSchema;
        List<String> innerSchemas = new ArrayList<>();
        String schemaDir;
        String input;
        String output;
        String analyzerId = "";
        boolean ephemeral;
        boolean reuse;
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
