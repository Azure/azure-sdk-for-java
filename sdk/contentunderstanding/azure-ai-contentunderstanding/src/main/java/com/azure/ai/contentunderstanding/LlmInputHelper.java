// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding;

import com.azure.ai.contentunderstanding.models.AnalysisContent;
import com.azure.ai.contentunderstanding.models.AnalysisContentKind;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AudioVisualContent;
import com.azure.ai.contentunderstanding.models.ContentArrayField;
import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentJsonField;
import com.azure.ai.contentunderstanding.models.ContentObjectField;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentContentSegment;
import com.azure.ai.contentunderstanding.models.DocumentPage;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts an {@link AnalysisResult} into LLM-friendly text (YAML front matter + markdown).
 *
 * <p>This helper produces a formatted string suitable for injecting into an LLM prompt,
 * storing in a vector database, or passing as tool output.
 *
 * <p>For single-content results (documents, images), the output is a flat text block.
 * For multi-segment results (video, audio), each segment is rendered with its time range.
 * For document classification results (parent with nested segments), the helper
 * automatically expands the parent into per-segment blocks with category labels
 * and markdown slices.
 *
 * <p><strong>Example usage:</strong>
 * <pre>{@code
 * AnalysisResult result = client.beginAnalyze("prebuilt-invoice",
 *     Arrays.asList(new AnalysisInput().setUrl(url)))
 *     .getFinalResult();
 *
 * String text = LlmInputHelper.toLlmInput(result);
 * }</pre>
 */
public final class LlmInputHelper {

    private static final Set<String> RESERVED_METADATA_KEYS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("contentType", "timeRange", "category", "pages", "fields", "rai_warnings")));

    private static final Pattern PAGE_BREAK_PATTERN = Pattern.compile("\\n*<!-- PageBreak -->\\n*");

    // YAML quoting patterns
    private static final Pattern YAML_SPECIAL_START = Pattern.compile("^[-?:,\\[\\]{}#&*!|>'\"%@`]");
    private static final Pattern YAML_SPECIAL_INSIDE = Pattern.compile("[:#] |[\\n\\r]");
    private static final Pattern YAML_BOOL
        = Pattern.compile("^(true|false|yes|no|on|off|null)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern YAML_NUMBER = Pattern.compile("^[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?$");
    private static final Pattern YAML_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");

    private LlmInputHelper() {
        // static utility class
    }

    /**
     * Convert a Content Understanding analysis result into LLM-friendly text.
     *
     * <p>Produces a YAML front matter block (delimited by {@code ---}) followed by a
     * markdown body. The YAML front matter may include:
     * {@code contentType} (document, audioVisual),
     * {@code pages} (page number or range),
     * {@code timeRange} (media time span for multi-segment audio/video),
     * {@code category} (classification label),
     * {@code fields} (extracted structured fields as YAML),
     * {@code rai_warnings} (content safety flags),
     * and any caller-supplied metadata entries.
     *
     * <p>The markdown body contains the extracted text with page-break markers
     * ({@code <!-- page N -->}) inserted at page boundaries so downstream consumers
     * can locate content by page number.
     *
     * @param result the {@link AnalysisResult} from a Content Understanding analyze operation.
     * @return a formatted string with YAML front matter followed by markdown content.
     *     Returns an empty string when {@code result.getContents()} is empty.
     * @throws NullPointerException if {@code result} is null.
     */
    public static String toLlmInput(AnalysisResult result) {
        return toLlmInput(result, null, null);
    }

    /**
     * Convert a Content Understanding analysis result into LLM-friendly text with metadata.
     *
     * <p>This is a convenience overload for the common case where only metadata needs
     * to be supplied (e.g., {@code {"source": "invoice.pdf"}} for RAG pipelines).
     *
     * @param result the {@link AnalysisResult} from a Content Understanding analyze operation.
     * @param metadata user-supplied key-value pairs to include in the YAML front matter.
     *     Keys must not conflict with helper-generated keys ({@code contentType}, {@code timeRange},
     *     {@code category}, {@code pages}, {@code fields}, {@code rai_warnings}).
     * @return a formatted string with YAML front matter followed by markdown content.
     *     Returns an empty string when {@code result.getContents()} is empty.
     * @throws NullPointerException if {@code result} is null.
     * @throws IllegalArgumentException if {@code metadata} contains a reserved front matter key.
     */
    public static String toLlmInput(AnalysisResult result, Map<String, Object> metadata) {
        return toLlmInput(result, metadata, null);
    }

    /**
     * Convert a Content Understanding analysis result into LLM-friendly text with metadata
     * and rendering options.
     *
     * @param result the {@link AnalysisResult} from a Content Understanding analyze operation.
     * @param metadata optional user-supplied key-value pairs to include in the YAML front matter.
     *     Pass {@code null} for no metadata.
     * @param options optional rendering options controlling field/markdown inclusion.
     *     Pass {@code null} for defaults (both fields and markdown included).
     * @return a formatted string with YAML front matter followed by markdown content.
     *     Returns an empty string when {@code result.getContents()} is empty.
     * @throws NullPointerException if {@code result} is null.
     * @throws IllegalArgumentException if {@code metadata} contains a reserved front matter key.
     */
    public static String toLlmInput(AnalysisResult result, Map<String, Object> metadata, ToLlmInputOptions options) {
        Objects.requireNonNull(result, "'result' must not be null.");

        boolean includeFields = options == null || options.isIncludeFields();
        boolean includeMarkdown = options == null || options.isIncludeMarkdown();
        validateMetadata(metadata);

        List<AnalysisContent> contents = result.getContents();
        if (contents == null || contents.isEmpty()) {
            return "";
        }

        List<RenderableContent> renderable = getRenderableContents(contents);
        if (renderable.isEmpty()) {
            return "";
        }

        int avCount = 0;
        for (RenderableContent rc : renderable) {
            if (rc.kind == AnalysisContentKind.AUDIO_VISUAL) {
                avCount++;
            }
        }
        boolean isMultiSegment = avCount > 1;

        List<String> blocks = new ArrayList<>();
        for (RenderableContent rc : renderable) {
            String block = renderContentBlock(rc, result, includeFields, includeMarkdown, metadata, isMultiSegment);
            if (block != null && !block.isEmpty()) {
                blocks.add(block);
            }
        }

        return String.join("\n\n*****\n\n", blocks);
    }

    // -----------------------------------------------------------------------
    // Metadata validation
    // -----------------------------------------------------------------------

    private static void validateMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return;
        }
        List<String> reserved = new ArrayList<>();
        for (String key : metadata.keySet()) {
            if (RESERVED_METADATA_KEYS.contains(key)) {
                reserved.add(key);
            }
        }
        if (!reserved.isEmpty()) {
            Collections.sort(reserved);
            throw new IllegalArgumentException(
                "metadata contains reserved front matter key(s): " + String.join(", ", reserved)
                    + ". Use custom keys such as 'source', 'documentId', or 'department' instead.");
        }
    }

    // -----------------------------------------------------------------------
    // Field resolution
    // -----------------------------------------------------------------------

    /**
     * Recursively flatten a {@code ContentField} map into plain Java types.
     * Object fields become nested {@code Map<String, Object>}, array fields become
     * {@code List<Object>}, and leaf fields produce their {@code getValue()} result
     * (with dates converted to ISO strings).
     *
     * @param fields the fields map from an {@link AnalysisContent}.
     * @return a plain map of resolved values.
     */
    static Map<String, Object> resolveFields(Map<String, ContentField> fields) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, ContentField> entry : fields.entrySet()) {
            Object val = resolveFieldValue(entry.getValue());
            if (val != null) {
                resolved.put(entry.getKey(), val);
            }
        }
        return resolved;
    }

    private static Object resolveFieldValue(ContentField field) {
        if (field == null) {
            return null;
        }

        // Object field
        if (field instanceof ContentObjectField) {
            Map<String, ContentField> obj = ((ContentObjectField) field).getValue();
            if (obj == null || obj.isEmpty()) {
                return null;
            }
            Map<String, Object> resolved = resolveFields(obj);
            return resolved.isEmpty() ? null : resolved;
        }

        // Array field
        if (field instanceof ContentArrayField) {
            List<ContentField> arr = ((ContentArrayField) field).getValue();
            if (arr == null || arr.isEmpty()) {
                return null;
            }
            List<Object> items = new ArrayList<>();
            for (ContentField item : arr) {
                Object resolved = resolveFieldValue(item);
                if (resolved != null) {
                    items.add(resolved);
                }
            }
            return items.isEmpty() ? null : items;
        }

        // JSON field — preserve the parsed JSON structure from BinaryData.
        if (field instanceof ContentJsonField) {
            BinaryData data = ((ContentJsonField) field).getValue();
            if (data == null) {
                return null;
            }
            return data.toObject(Object.class);
        }

        // Leaf field
        Object val = field.getValue();
        if (val == null) {
            return null;
        }

        // Convert dates to ISO strings for YAML
        if (val instanceof LocalDate) {
            return val.toString(); // ISO-8601 yyyy-MM-dd
        }

        return val;
    }

    // -----------------------------------------------------------------------
    // Content rendering
    // -----------------------------------------------------------------------

    /**
     * Internal representation for a renderable content block.
     * Needed because DocumentContent constructors are private, so we can't
     * create synthetic DocumentContent instances for classification segments.
     */
    private static final class RenderableContent {
        final AnalysisContentKind kind;
        final String category;
        final String markdown;
        final Map<String, ContentField> fields;
        final int startPageNumber;
        final int endPageNumber;
        final List<DocumentPage> pages;
        final long startTimeMs;
        final long endTimeMs;

        // Wrap an existing AnalysisContent
        RenderableContent(AnalysisContent content) {
            this.kind = content.getKind();
            this.category = content.getCategory();
            this.markdown = content.getMarkdown();
            this.fields = content.getFields();

            if (content instanceof DocumentContent) {
                DocumentContent dc = (DocumentContent) content;
                this.startPageNumber = dc.getStartPageNumber();
                this.endPageNumber = dc.getEndPageNumber();
                this.pages = dc.getPages();
                this.startTimeMs = 0;
                this.endTimeMs = 0;
            } else if (content instanceof AudioVisualContent) {
                AudioVisualContent av = (AudioVisualContent) content;
                this.startPageNumber = 0;
                this.endPageNumber = 0;
                this.pages = null;
                this.startTimeMs = av.getStartTime().toMillis();
                this.endTimeMs = av.getEndTime().toMillis();
            } else {
                this.startPageNumber = 0;
                this.endPageNumber = 0;
                this.pages = null;
                this.startTimeMs = 0;
                this.endTimeMs = 0;
            }
        }

        // Synthetic segment
        RenderableContent(String category, String markdown, int startPageNumber, int endPageNumber) {
            this.kind = AnalysisContentKind.DOCUMENT;
            this.category = category;
            this.markdown = markdown;
            this.fields = null;
            this.pages = null;
            this.startPageNumber = startPageNumber;
            this.endPageNumber = endPageNumber;
            this.startTimeMs = 0;
            this.endTimeMs = 0;
        }
    }

    /**
     * Flattens the contents list for rendering. In classification scenarios, the service
     * returns a parent DocumentContent (with full markdown and segments) plus separate
     * routed DocumentContent items (with their own markdown and fields) for segments
     * that matched a specific analyzer.
     *
     * <p>Example input:
     * <pre>
     *   contents[0] = parent doc
     *     path="input1", category=null
     *     markdown="INVOICE\nVendor: Contoso\nTotal: $1500\n&lt;!-- PageBreak --&gt;\nRECEIPT\nStore: Northwind"
     *     segments=[
     *       { segmentId: "seg1", category: "invoice", startPageNumber: 1, span: {offset:0, length:38} },
     *       { segmentId: "seg2", category: "receipt", startPageNumber: 2, span: {offset:55, length:37} }
     *     ]
     *   contents[1] = routed doc (produced by prebuilt-invoice analyzer)
     *     path="input1/seg1", category="invoice"
     *     markdown="INVOICE\nVendor: Contoso\nTotal: $1500"  (analyzer's own markdown)
     *     fields={ vendor: "Contoso", total: 1500 }
     * </pre>
     *
     * <p>This method:
     * <ol>
     *   <li>Identifies contents[1] as a routed version of seg1 (path "input1/seg1" matches).</li>
     *   <li>Skips seg1 during parent expansion — the routed version (with its own
     *       markdown and fields) will be used directly instead of slicing from the parent.</li>
     *   <li>Creates a synthetic RenderableContent for seg2 by slicing the parent's markdown
     *       using span {offset:55, length:37}.</li>
     *   <li>Sorts all results by page number so blocks appear in document order.</li>
     * </ol>
     *
     * <p>Result: [routed invoice (page 1, own markdown + fields), synthetic receipt (page 2, sliced markdown)]
     */
    private static List<RenderableContent> getRenderableContents(List<AnalysisContent> contents) {
        // Collect paths of routed top-level content items
        Set<String> routedPaths = new HashSet<>();
        for (AnalysisContent c : contents) {
            if (c instanceof DocumentContent && c.getCategory() != null && c.getPath() != null) {
                routedPaths.add(c.getPath());
            }
        }

        List<OrderedContent> ordered = new ArrayList<>();
        boolean expandedClassification = false;
        int originalOrder = 0;

        for (AnalysisContent c : contents) {
            if (c instanceof DocumentContent) {
                DocumentContent dc = (DocumentContent) c;
                List<DocumentContentSegment> segments = dc.getSegments();
                if (segments != null && !segments.isEmpty() && dc.getCategory() == null) {
                    expandedClassification = true;
                    String parentPath = dc.getPath() != null ? dc.getPath() : "";

                    for (DocumentContentSegment seg : segments) {
                        String segPath = seg.getSegmentId() != null ? parentPath + "/" + seg.getSegmentId() : null;
                        if (segPath != null && routedPaths.contains(segPath)) {
                            continue; // top-level version with fields will be used
                        }

                        String md = null;
                        if (dc.getMarkdown() != null && seg.getSpan() != null) {
                            int offset = seg.getSpan().getOffset();
                            int length = seg.getSpan().getLength();
                            if (offset >= 0 && offset + length <= dc.getMarkdown().length()) {
                                md = dc.getMarkdown().substring(offset, offset + length);
                            }
                        }

                        ordered.add(new OrderedContent(new RenderableContent(seg.getCategory(), md,
                            seg.getStartPageNumber(), seg.getEndPageNumber()), originalOrder++));
                    }
                    continue;
                }
            }
            ordered.add(new OrderedContent(new RenderableContent(c), originalOrder++));
        }

        if (expandedClassification) {
            ordered.sort((a, b) -> {
                int pa = a.content.startPageNumber;
                int pb = b.content.startPageNumber;
                return pa != pb ? Integer.compare(pa, pb) : Integer.compare(a.order, b.order);
            });
        }

        List<RenderableContent> result = new ArrayList<>(ordered.size());
        for (OrderedContent oc : ordered) {
            result.add(oc.content);
        }
        return result;
    }

    private static final class OrderedContent {
        final RenderableContent content;
        final int order;

        OrderedContent(RenderableContent content, int order) {
            this.content = content;
            this.order = order;
        }
    }

    private static String renderContentBlock(RenderableContent content, AnalysisResult result, boolean includeFields,
        boolean includeMarkdown, Map<String, Object> metadata, boolean isMultiSegment) {

        // Build ordered front matter entries
        List<Object[]> fm = new ArrayList<>();

        // 1. contentType
        fm.add(new Object[] { "contentType", content.kind != null ? content.kind.toString() : "unknown" });

        // 2. user metadata
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                fm.add(new Object[] { entry.getKey(), entry.getValue() });
            }
        }

        // 3. timeRange (only multi-segment audioVisual)
        if (content.kind == AnalysisContentKind.AUDIO_VISUAL && isMultiSegment) {
            fm.add(new Object[] { "timeRange", formatTimeRange(content.startTimeMs, content.endTimeMs) });
        }

        // 4. category
        if (content.category != null) {
            fm.add(new Object[] { "category", content.category });
        }

        // 5. pages
        Object pagesVal = formatPages(content);
        if (pagesVal != null) {
            fm.add(new Object[] { "pages", pagesVal });
        }

        // Build complex entries separately (fields, rai_warnings need structured YAML)
        Map<String, Object> resolvedFields = null;
        if (includeFields && content.fields != null && !content.fields.isEmpty()) {
            resolvedFields = resolveFields(content.fields);
            if (resolvedFields.isEmpty()) {
                resolvedFields = null;
            }
        }

        List<Map<String, String>> warningsList = null;
        if (result.getWarnings() != null && !result.getWarnings().isEmpty()) {
            warningsList = formatWarnings(result.getWarnings());
            if (warningsList.isEmpty()) {
                warningsList = null;
            }
        }

        String frontMatter = buildFrontMatter(fm, resolvedFields, warningsList);

        if (includeMarkdown && content.markdown != null && !content.markdown.isEmpty()) {
            String md = content.markdown;
            if (content.kind == AnalysisContentKind.DOCUMENT) {
                md = addPageMarkers(content, md);
            }
            return frontMatter + "\n" + md;
        }
        return frontMatter;
    }

    // -----------------------------------------------------------------------
    // Page numbering
    // -----------------------------------------------------------------------

    private static String addPageMarkers(RenderableContent content, String markdown) {
        if (content.pages != null && !content.pages.isEmpty()) {
            String result = pageMarkersFromSpans(markdown, content.pages);
            // Identity check: if spans were found, result differs from input
            if (!result.equals(markdown)) {
                return result;
            }
        }
        return pageMarkersFromBreaks(markdown, content);
    }

    private static String pageMarkersFromSpans(String markdown, List<DocumentPage> pages) {
        List<int[]> markers = new ArrayList<>(); // [offset, pageNumber]
        for (DocumentPage page : pages) {
            if (page.getSpans() != null && !page.getSpans().isEmpty()) {
                markers.add(new int[] { page.getSpans().get(0).getOffset(), page.getPageNumber() });
            }
        }
        if (markers.isEmpty()) {
            return markdown;
        }
        markers.sort((a, b) -> Integer.compare(a[0], b[0]));

        // Strip <!-- PageBreak --> since page markers replace them
        String cleaned = PAGE_BREAK_PATTERN.matcher(markdown).replaceAll("\n\n");

        // Compute offset shifts from cleaning
        List<int[]> shifts = new ArrayList<>(); // [position, delta]
        Matcher m = PAGE_BREAK_PATTERN.matcher(markdown);
        while (m.find()) {
            int replacementLen = 2; // "\n\n"
            shifts.add(new int[] { m.start(), m.end() - m.start() - replacementLen });
        }

        StringBuilder sb = new StringBuilder();
        int prev = 0;
        for (int[] marker : markers) {
            int adj = adjustedOffset(marker[0], shifts);
            adj = Math.min(adj, cleaned.length());
            if (adj > prev) {
                sb.append(cleaned, prev, adj);
            }
            sb.append("<!-- page ").append(marker[1]).append(" -->\n\n");
            prev = adj;
        }
        if (prev < cleaned.length()) {
            sb.append(cleaned, prev, cleaned.length());
        }
        return sb.toString();
    }

    private static int adjustedOffset(int orig, List<int[]> shifts) {
        int total = 0;
        for (int[] s : shifts) {
            if (orig > s[0]) {
                total += s[1];
            }
        }
        return orig - total;
    }

    private static String pageMarkersFromBreaks(String markdown, RenderableContent content) {
        int startPage = content.startPageNumber > 0 ? content.startPageNumber : 1;
        String[] chunks = PAGE_BREAK_PATTERN.split(markdown);
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < chunks.length; i++) {
            String text = chunks[i].trim();
            if (!text.isEmpty()) {
                parts.add("<!-- page " + (startPage + i) + " -->\n\n" + text);
            }
        }
        return String.join("\n\n", parts);
    }

    // -----------------------------------------------------------------------
    // Formatting helpers
    // -----------------------------------------------------------------------

    private static String formatTimeRange(long startMs, long endMs) {
        return formatTime(startMs) + " \u2013 " + formatTime(endMs);
    }

    private static String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static Object formatPages(RenderableContent content) {
        if (content.kind != AnalysisContentKind.DOCUMENT) {
            return null;
        }

        // Prefer actual page numbers from the pages list
        if (content.pages != null && !content.pages.isEmpty()) {
            List<Integer> pageNumbers = new ArrayList<>();
            for (DocumentPage p : content.pages) {
                if (p.getPageNumber() > 0) {
                    pageNumbers.add(p.getPageNumber());
                }
            }
            if (!pageNumbers.isEmpty()) {
                Collections.sort(pageNumbers);
                return compressPageNumbers(pageNumbers);
            }
        }

        // Fallback to start/end range
        int start = content.startPageNumber;
        int end = content.endPageNumber;
        if (start <= 0 && end <= 0) {
            return null;
        }
        if (start == end) {
            return start;
        }
        return start + "-" + end;
    }

    static Object compressPageNumbers(List<Integer> numbers) {
        if (numbers.isEmpty()) {
            return 0;
        }
        if (numbers.size() == 1) {
            return numbers.get(0);
        }
        List<String> ranges = new ArrayList<>();
        int rangeStart = numbers.get(0);
        int prev = numbers.get(0);
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) == prev + 1) {
                prev = numbers.get(i);
            } else {
                ranges.add(rangeStart == prev ? String.valueOf(rangeStart) : rangeStart + "-" + prev);
                rangeStart = numbers.get(i);
                prev = numbers.get(i);
            }
        }
        ranges.add(rangeStart == prev ? String.valueOf(rangeStart) : rangeStart + "-" + prev);
        return String.join(", ", ranges);
    }

    private static List<Map<String, String>> formatWarnings(List<ResponseError> warnings) {
        List<Map<String, String>> items = new ArrayList<>();
        for (ResponseError w : warnings) {
            if (w == null) {
                continue;
            }
            Map<String, String> entry = new LinkedHashMap<>();
            if (w.getCode() != null && !w.getCode().isEmpty()) {
                entry.put("code", w.getCode());
            }
            if (w.getMessage() != null && !w.getMessage().isEmpty()) {
                entry.put("message", w.getMessage());
            }
            if (!entry.isEmpty()) {
                items.add(entry);
            }
        }
        return items;
    }

    // -----------------------------------------------------------------------
    // Minimal YAML serializer (no external dependency)
    // -----------------------------------------------------------------------

    static String yamlScalar(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "true" : "false";
        }
        if (value instanceof Integer || value instanceof Long) {
            return value.toString();
        }
        if (value instanceof Double) {
            double d = (Double) value;
            if (!Double.isFinite(d)) {
                return String.valueOf(d);
            }
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        if (value instanceof Float) {
            float f = (Float) value;
            if (!Float.isFinite(f)) {
                return String.valueOf(f);
            }
            if (f == Math.floor(f) && !Float.isInfinite(f)) {
                return String.valueOf((long) f);
            }
            return String.valueOf(f);
        }

        String s = value.toString();
        boolean needsQuote = s.isEmpty()
            || YAML_BOOL.matcher(s).matches()
            || YAML_NUMBER.matcher(s).matches()
            || YAML_DATE.matcher(s).find()
            || YAML_SPECIAL_START.matcher(s).find()
            || YAML_SPECIAL_INSIDE.matcher(s).find();

        return needsQuote ? "'" + s.replace("'", "''") + "'" : s;
    }

    private static String buildFrontMatter(List<Object[]> simpleEntries, Map<String, Object> fields,
        List<Map<String, String>> warnings) {
        List<String> lines = new ArrayList<>();
        lines.add("---");

        // Simple key-value entries and user metadata.
        for (Object[] entry : simpleEntries) {
            emitEntry(lines, entry[0], entry[1], 0);
        }

        // Fields (structured)
        if (fields != null && !fields.isEmpty()) {
            lines.add("fields:");
            emitMapping(lines, fields, 1);
        }

        // rai_warnings (structured list)
        if (warnings != null && !warnings.isEmpty()) {
            lines.add("rai_warnings:");
            emitWarningSequence(lines, warnings, 0);
        }

        lines.add("---");
        return String.join("\n", lines);
    }

    private static void emitMapping(List<String> lines, Map<?, ?> mapping, int indent) {
        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
            emitEntry(lines, entry.getKey(), entry.getValue(), indent);
        }
    }

    private static void emitEntry(List<String> lines, Object key, Object value, int indent) {
        if (value == null) {
            return;
        }
        String prefix = repeat("  ", indent);
        String safeKey = yamlScalar(key);

        if (value instanceof Map) {
            Map<?, ?> nested = (Map<?, ?>) value;
            if (nested.isEmpty()) {
                return;
            }
            lines.add(prefix + safeKey + ":");
            emitMapping(lines, nested, indent + 1);
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return;
            }
            lines.add(prefix + safeKey + ":");
            emitSequence(lines, list, indent);
        } else {
            lines.add(prefix + safeKey + ": " + yamlScalar(value));
        }
    }

    private static void emitSequence(List<String> lines, List<?> sequence, int indent) {
        String prefix = repeat("  ", indent);
        for (Object item : sequence) {
            if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object entryValue = entry.getValue();
                    if (entryValue == null) {
                        continue;
                    }
                    String tag = first ? prefix + "- " : prefix + "  ";
                    String safeKey = yamlScalar(entry.getKey());

                    if (entryValue instanceof Map) {
                        Map<?, ?> nested = (Map<?, ?>) entryValue;
                        if (!nested.isEmpty()) {
                            lines.add(tag + safeKey + ":");
                            emitMapping(lines, nested, indent + 2);
                        } else {
                            lines.add(tag + safeKey + ": " + yamlScalar(entryValue));
                        }
                    } else if (entryValue instanceof List) {
                        List<?> list = (List<?>) entryValue;
                        if (!list.isEmpty()) {
                            lines.add(tag + safeKey + ":");
                            emitSequence(lines, list, indent + 2);
                        } else {
                            lines.add(tag + safeKey + ": " + yamlScalar(entryValue));
                        }
                    } else {
                        lines.add(tag + safeKey + ": " + yamlScalar(entryValue));
                    }
                    first = false;
                }
            } else {
                lines.add(prefix + "- " + yamlScalar(item));
            }
        }
    }

    private static void emitWarningSequence(List<String> lines, List<Map<String, String>> warnings, int indent) {
        String prefix = repeat("  ", indent);
        for (Map<String, String> w : warnings) {
            boolean first = true;
            for (Map.Entry<String, String> entry : w.entrySet()) {
                String tag = first ? prefix + "- " : prefix + "  ";
                lines.add(tag + yamlScalar(entry.getKey()) + ": " + yamlScalar(entry.getValue()));
                first = false;
            }
        }
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
