// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

/**
 * Customization class for Content Understanding SDK.
 * This class contains customization code to modify the AutoRest/TypeSpec generated code.
 *
 * <h2>Summary of customizations</h2>
 *
 * <h3>Extension of generated code to make it easier to use</h3>
 * <ul>
 *   <li>Add {@code operationId} field and getter/setter to {@code ContentAnalyzerAnalyzeOperationStatus};
 *       parse and set it from the Operation-Location header in polling strategies.</li>
 *   <li>Add {@code parseOperationId} to {@code PollingUtils} and override poll() in sync/async
 *       {@code OperationLocationPollingStrategy} to set operationId on the status.</li>
 *   <li>Add static accessor helper ({@code ContentAnalyzerAnalyzeOperationStatusHelper}) so polling
 *       can set the private operationId.</li>
 *   <li>Add convenience methods on model classes for content/array/object fields (equivalent to *.Extensions.cs).</li>
 *   <li>Make {@code ContentUnderstandingDefaults} constructor public and add {@code updateDefaults}
 *       convenience methods that accept {@code Map} or {@code ContentUnderstandingDefaults} (TypeSpec
 *       disabled these; we re-enable and add them).</li>
 *   <li>Add {@code beginAnalyze} and {@code beginAnalyzeBinary} convenience overloads without a
 *       {@code stringEncoding} parameter (default utf16).</li>
 *   <li>Add {@code beginAnalyzeBinary} overload accepting {@link com.azure.ai.contentunderstanding.models.ContentRange ContentRange}
 *       and {@code setContentRange(ContentRange)} on {@code AnalysisInput} for a self-documenting range API.</li>
 * </ul>
 *
 * <p><b>Scenarios and before/after</b></p>
 *
 * <p><b>operationId on status</b> — Scenario: Caller needs the operation ID (e.g. to call
 * getResultFile or deleteResult) after starting analyze.</p>
 * <pre>{@code
 * // Before: generated model had no operationId; caller could not get it from the status.
 * SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller = client.beginAnalyze(...);
 * ContentAnalyzerAnalyzeOperationStatus status = poller.getFinalResult(); // no getOperationId()
 *
 * // After: status carries operationId, set automatically by polling strategy from Operation-Location header.
 * String id = status.getOperationId();
 * client.getResultFile(analyzerId, id, ...);
 * }</pre>
 *
 * <p><b>Content/array/object field extensions</b> — Scenario: Reading document fields
 * (ContentField and subtypes ContentStringField, ContentNumberField, ContentDateField, ContentObjectField, ContentArrayField) without
 * casting to each subtype or manually navigating getValueObject()/getValueArray().</p>
 * <p>ContentField.getValue() — get typed value without casting to ContentStringField/ContentNumberField/etc.:</p>
 * <pre>{@code
 * // Before: Cast to subtype and call type-specific getter, then print.
 * ContentField customerNameField = content.getFields().get("CustomerName");
 * String customerName = customerNameField instanceof ContentStringField
 *     ? ((ContentStringField) customerNameField).getValueString() : null;
 * System.out.println("Customer: " + customerName);
 *
 * // After: getValue() returns the typed value; no cast needed for console output.
 * ContentField customerNameField = content.getFields().get("CustomerName");
 * System.out.println("Customer: " + (customerNameField != null ? customerNameField.getValue() : null));
 * }</pre>
 * <p>ContentObjectField.getFieldOrDefault() — navigate nested object by name:</p>
 * <pre>{@code
 * // Before: Use getValueObject() and map lookup; cast to ContentNumberField for value.
 * ContentField totalField = content.getFields().get("TotalAmount");
 * ContentObjectField totalObj = (ContentObjectField) totalField;
 * ContentField amountField = totalObj.getValueObject() != null ? totalObj.getValueObject().get("Amount") : null;
 * Double amount = amountField instanceof ContentNumberField ? ((ContentNumberField) amountField).getValueNumber() : null;
 *
 * // After: getFieldOrDefault(name); then getValue() for the typed value.
 * ContentField totalField = content.getFields().get("TotalAmount");
 * ContentObjectField totalObj = (ContentObjectField) totalField;
 * ContentField amountField = totalObj.getFieldOrDefault("Amount");
 * Double amount = amountField != null ? (Double) amountField.getValue() : null;
 * }</pre>
 * <p>ContentArrayField.size() and get(i) — iterate array elements without getValueArray():</p>
 * <pre>{@code
 * // Before: Call getValueArray() and use List size/get; null-check the list.
 * ContentField lineItemsField = content.getFields().get("LineItems");
 * ContentArrayField lineItems = (ContentArrayField) lineItemsField;
 * int count = lineItems.getValueArray() != null ? lineItems.getValueArray().size() : 0;
 * for (int i = 0; i < count; i++) {
 *     ContentField item = lineItems.getValueArray().get(i);
 *     // use item...
 * }
 *
 * // After: size() and get(i) convenience methods; get(i) throws IndexOutOfBoundsException if out of range.
 * ContentField lineItemsField = content.getFields().get("LineItems");
 * ContentArrayField lineItems = (ContentArrayField) lineItemsField;
 * for (int i = 0; i < lineItems.size(); i++) {
 *     ContentField item = lineItems.get(i);
 *     // use item...
 * }
 * }</pre>
 *
 * <p><b>beginAnalyze / beginAnalyzeBinary without stringEncoding</b> — Scenario: Start analyze
 * with the default string encoding (utf16) without exposing encoding in the API.</p>
 * <pre>{@code
 * // Before: Generated API required stringEncoding parameter (if present), e.g. beginAnalyze(..., "utf16").
 *
 * // After: Convenience overloads default utf16; caller uses simple signatures.
 * SyncPoller<..., AnalysisResult> poller = client.beginAnalyze(analyzerId, inputs);
 * SyncPoller<..., AnalysisResult> binaryPoller = client.beginAnalyzeBinary(analyzerId, binaryInput);
 * }</pre>
 *
 * <h3>Fix service issue (SERVICE-FIX)</h3>
 * <ul>
 *   <li>Add case-insensitive deserialization for {@code keyFrameTimesMs} / {@code KeyFrameTimesMs}
 *       in {@code AudioVisualContent} for forward-compatibility with service casing.</li>
 * </ul>
 *
 * <h3>Correct emitter limitations</h3>
 * <ul>
 *   <li>Hide generated methods that expose {@code stringEncoding} (make them package-private) so the
 *       public API does not expose it; simplified overloads use utf16 by default.</li>
 *   <li>Fix generated {@code beginAnalyze} / {@code beginAnalyzeBinary} bodies that call
 *       non-existent overloads after the generator stopped emitting stringEncoding overloads;
 *       rewrite bodies to call the implementation client with {@code stringEncoding=utf16} in
 *       RequestOptions.</li>
 *   <li>Make {@code ContentUnderstandingDefaults} constructor public so that {@code updateDefaults}
 *       convenience methods can be used (generated/TypeSpec code assumed a public constructor for
 *       updateDefaults; without this, updateDefaults usage would not compile).</li>
 * </ul>
 */
public class ContentUnderstandingCustomizations extends Customization {

    private static final String PACKAGE_NAME = "com.azure.ai.contentunderstanding";
    private static final String MODELS_PACKAGE = "com.azure.ai.contentunderstanding.models";
    private static final String IMPLEMENTATION_PACKAGE = "com.azure.ai.contentunderstanding.implementation";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // Add operationId field to AnalysisResult model
        customizeAnalysisResult(customization, logger);

        // Customize PollingUtils to add parseOperationId method
        customizePollingUtils(customization, logger);

        // Customize PollingStrategy to extract and set operationId
        customizePollingStrategy(customization, logger);

        // Add static accessor helper for operationId
        addStaticAccessorForOperationId(customization, logger);

        // Add convenience methods to model classes (equivalent to *.Extensions.cs)
        customizeContentFieldExtensions(customization, logger);
        customizeArrayFieldExtensions(customization, logger);
        customizeObjectFieldExtensions(customization, logger);

        // SERVICE-FIX: Add keyFrameTimesMs case-insensitive deserialization
        customizeAudioVisualContentDeserialization(customization, logger);

        // Fix generated polling strategy code (String.valueOf -> getCaseSensitiveName, error msg comma, @Override removal)
        fixGeneratedPollingStrategyCode(customization, logger);

        // Hide methods that expose stringEncoding parameter (if generator still emits them)
        hideStringEncodingMethods(customization, logger);

        // Make ContentUnderstandingDefaults constructor public for updateDefaults convenience methods
        customizeContentUnderstandingDefaults(customization, logger);

        // Add updateDefaults convenience methods (TypeSpec disabled these, but auto-generates updateAnalyzer)
        addUpdateDefaultsConvenienceMethods(customization, logger);

        // Add beginAnalyzeBinary convenience overloads (no stringEncoding)
        addBeginAnalyzeBinaryConvenienceOverloads(customization, logger);

        // Add ContentRange overloads for beginAnalyzeBinary and setInputRange on AnalysisInput
        addContentRangeOverloads(customization, logger);
        addContentRangeSetterToAnalyzeInput(customization, logger);

        // Add beginAnalyze convenience overloads (no stringEncoding)
        addBeginAnalyzeConvenienceOverloads(customization, logger);

        // Add typed getValue() to each ContentField subclass and hide verbose getters
        customizeFieldValueAccessors(customization, logger);

        // Add ContentSource class hierarchy for grounding source parsing
        addContentSourceClasses(customization, logger);

        // Add getGroundingSources() to ContentField
        addGroundingSourcesMethod(customization, logger);

        // Add Duration getters and hide *Ms() getters on time-based models
        customizeDurationProperties(customization, logger);
    }

    /**
     * Add operationId field and getter/setter to ContentAnalyzerAnalyzeOperationStatus
     */
    private void customizeAnalysisResult(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing ContentAnalyzerAnalyzeOperationStatus to add operationId field");

        customization.getClass(MODELS_PACKAGE, "ContentAnalyzerAnalyzeOperationStatus")
            .customizeAst(ast -> ast.getClassByName("ContentAnalyzerAnalyzeOperationStatus").ifPresent(clazz -> {
                // Remove @Immutable annotation if present
                clazz.getAnnotationByName("Immutable").ifPresent(Node::remove);

                // Add operationId field
                clazz.addField("String", "operationId", Modifier.Keyword.PRIVATE);

                // Add public getter for operationId
                clazz.addMethod("getOperationId", Modifier.Keyword.PUBLIC)
                    .setType("String")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets the operationId property: The unique ID of the analyze operation. "
                        + "Use this ID with getResultFile() and deleteResult() methods."))
                        .addBlockTag("return", "the operationId value."))
                    .setBody(StaticJavaParser.parseBlock("{ return operationId; }"));

                // Add private setter for operationId (used by helper)
                clazz.addMethod("setOperationId", Modifier.Keyword.PRIVATE)
                    .setType("void")
                    .addParameter("String", "operationId")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Sets the operationId property: The unique ID of the analyze operation."))
                        .addBlockTag("param", "operationId the operationId value to set."))
                    .setBody(StaticJavaParser.parseBlock("{ this.operationId = operationId; }"));
            }));
    }

    /**
     * Add parseOperationId method to PollingUtils
     */
    private void customizePollingUtils(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing PollingUtils to add parseOperationId method");

        customization.getClass(IMPLEMENTATION_PACKAGE, "PollingUtils").customizeAst(ast -> {
            ast.addImport("java.util.regex.Matcher");
            ast.addImport("java.util.regex.Pattern");

            ast.getClassByName("PollingUtils").ifPresent(clazz -> {
                // Add regex pattern for extracting operationId from Operation-Location header
                // Example: https://endpoint/contentunderstanding/analyzers/myAnalyzer/results/operationId?api-version=xxx
                clazz.addFieldWithInitializer("Pattern", "OPERATION_ID_PATTERN",
                    StaticJavaParser.parseExpression("Pattern.compile(\"[^:]+://[^/]+/contentunderstanding/.+/([^?/]+)\")"),
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

                // Add parseOperationId method
                clazz.addMethod("parseOperationId", Modifier.Keyword.STATIC)
                    .setType("String")
                    .addParameter("String", "operationLocationHeader")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Parses the operationId from the Operation-Location header."))
                        .addBlockTag("param", "operationLocationHeader the Operation-Location header value.")
                        .addBlockTag("return", "the operationId, or null if not found."))
                    .setBody(StaticJavaParser.parseBlock("{ "
                        + "if (CoreUtils.isNullOrEmpty(operationLocationHeader)) { return null; }"
                        + "Matcher matcher = OPERATION_ID_PATTERN.matcher(operationLocationHeader);"
                        + "if (matcher.find() && matcher.group(1) != null) { return matcher.group(1); }"
                        + "return null; }"));
            });
        });
    }

    /**
     * Customize polling strategies to extract operationId and set it on the result
     */
    private void customizePollingStrategy(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing SyncOperationLocationPollingStrategy class");
        PackageCustomization packageCustomization = customization.getPackage(IMPLEMENTATION_PACKAGE);

        packageCustomization.getClass("SyncOperationLocationPollingStrategy").customizeAst(ast ->
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus")
               .addImport("com.azure.ai.contentunderstanding.implementation.ContentAnalyzerAnalyzeOperationStatusHelper")
               .getClassByName("SyncOperationLocationPollingStrategy").ifPresent(this::addSyncPollOverrideMethod));

        logger.info("Customizing OperationLocationPollingStrategy class");
        packageCustomization.getClass("OperationLocationPollingStrategy").customizeAst(ast ->
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus")
               .addImport("com.azure.ai.contentunderstanding.implementation.ContentAnalyzerAnalyzeOperationStatusHelper")
               .getClassByName("OperationLocationPollingStrategy").ifPresent(this::addAsyncPollOverrideMethod));
    }

    private void addSyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("PollResponse<T>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addMarkerAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock("{ "
                + "PollResponse<T> pollResponse = super.poll(pollingContext, pollResponseType);"
                + "String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));"
                + "String operationId = null;"
                + "if (operationLocationHeader != null) {"
                + "    operationId = PollingUtils.parseOperationId(operationLocationHeader);"
                + "}"
                + "if (pollResponse.getValue() instanceof ContentAnalyzerAnalyzeOperationStatus) {"
                + "    ContentAnalyzerAnalyzeOperationStatus operation = (ContentAnalyzerAnalyzeOperationStatus) pollResponse.getValue();"
                + "    ContentAnalyzerAnalyzeOperationStatusHelper.setOperationId(operation, operationId);"
                + "}"
                + "return pollResponse; }"));
    }

    private void addAsyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("Mono<PollResponse<T>>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addMarkerAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock("{ return super.poll(pollingContext, pollResponseType)"
                + ".map(pollResponse -> {"
                + "    String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));"
                + "    String operationId = null;"
                + "    if (operationLocationHeader != null) {"
                + "        operationId = PollingUtils.parseOperationId(operationLocationHeader);"
                + "    }"
                + "    if (pollResponse.getValue() instanceof ContentAnalyzerAnalyzeOperationStatus) {"
                + "        ContentAnalyzerAnalyzeOperationStatus operation = (ContentAnalyzerAnalyzeOperationStatus) pollResponse.getValue();"
                + "        ContentAnalyzerAnalyzeOperationStatusHelper.setOperationId(operation, operationId);"
                + "    }"
                + "    return pollResponse;"
                + "}); }"));
    }

    /**
     * Add static accessor helper for setting operationId on ContentAnalyzerAnalyzeOperationStatus
     */
    private void addStaticAccessorForOperationId(LibraryCustomization customization, Logger logger) {
        logger.info("Adding ContentAnalyzerAnalyzeOperationStatusHelper class");

        // First, add the static initializer block to ContentAnalyzerAnalyzeOperationStatus
        customization.getClass(MODELS_PACKAGE, "ContentAnalyzerAnalyzeOperationStatus").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.implementation.ContentAnalyzerAnalyzeOperationStatusHelper");
            ast.getClassByName("ContentAnalyzerAnalyzeOperationStatus").ifPresent(clazz ->
                clazz.getMembers().add(0, new InitializerDeclaration(true,
                    StaticJavaParser.parseBlock("{"
                        + "ContentAnalyzerAnalyzeOperationStatusHelper.setAccessor("
                        + "new ContentAnalyzerAnalyzeOperationStatusHelper.ContentAnalyzerAnalyzeOperationStatusAccessor() {"
                        + "    @Override"
                        + "    public void setOperationId(ContentAnalyzerAnalyzeOperationStatus status, String operationId) {"
                        + "        status.setOperationId(operationId);"
                        + "    }"
                        + "}); }"))));
        });

        // Create the helper class file
        String helperContent =
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
            + "// Licensed under the MIT License.\n"
            + "package com.azure.ai.contentunderstanding.implementation;\n\n"
            + "import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;\n\n"
            + "/**\n"
            + " * Helper class to access private members of ContentAnalyzerAnalyzeOperationStatus.\n"
            + " */\n"
            + "public final class ContentAnalyzerAnalyzeOperationStatusHelper {\n"
            + "    private static ContentAnalyzerAnalyzeOperationStatusAccessor accessor;\n\n"
            + "    /**\n"
            + "     * Interface for accessing private members.\n"
            + "     */\n"
            + "    public interface ContentAnalyzerAnalyzeOperationStatusAccessor {\n"
            + "        void setOperationId(ContentAnalyzerAnalyzeOperationStatus status, String operationId);\n"
            + "    }\n\n"
            + "    /**\n"
            + "     * Sets the accessor.\n"
            + "     * @param accessorInstance the accessor instance.\n"
            + "     */\n"
            + "    public static void setAccessor(ContentAnalyzerAnalyzeOperationStatusAccessor accessorInstance) {\n"
            + "        accessor = accessorInstance;\n"
            + "    }\n\n"
            + "    /**\n"
            + "     * Sets the operationId on a ContentAnalyzerAnalyzeOperationStatus instance.\n"
            + "     * @param status the status instance.\n"
            + "     * @param operationId the operationId to set.\n"
            + "     */\n"
            + "    public static void setOperationId(ContentAnalyzerAnalyzeOperationStatus status, String operationId) {\n"
            + "        accessor.setOperationId(status, operationId);\n"
            + "    }\n\n"
            + "    private ContentAnalyzerAnalyzeOperationStatusHelper() {\n"
            + "    }\n"
            + "}\n";

        customization.getRawEditor().addFile(
            "src/main/java/com/azure/ai/contentunderstanding/implementation/ContentAnalyzerAnalyzeOperationStatusHelper.java",
            helperContent);
    }

    /**
     * Fix generated polling strategy code:
     * 1) Replace String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER) with
     *    PollingUtils.OPERATION_LOCATION_HEADER.getCaseSensitiveName() in onInitialResponse
     * 2) Fix error message format string: add missing comma after %d
     * 3) Remove @Override from getResult in SyncOperationLocationPollingStrategy
     */
    private void fixGeneratedPollingStrategyCode(LibraryCustomization customization, Logger logger) {
        logger.info("Fixing generated polling strategy code");

        // Fix OperationLocationPollingStrategy
        customization.getClass(IMPLEMENTATION_PACKAGE, "OperationLocationPollingStrategy").customizeAst(ast ->
            ast.getClassByName("OperationLocationPollingStrategy").ifPresent(clazz -> {
                for (MethodDeclaration method : clazz.getMethods()) {
                    method.getBody().ifPresent(body -> {
                        String bodyStr = body.toString();
                        String updated = bodyStr
                            .replace("String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER)",
                                "PollingUtils.OPERATION_LOCATION_HEADER.getCaseSensitiveName()")
                            .replace("Operation failed or cancelled with status code %d\"",
                                "Operation failed or cancelled with status code %d,\"");
                        if (!updated.equals(bodyStr)) {
                            method.setBody(StaticJavaParser.parseBlock(updated));
                        }
                    });
                }
            }));

        // Fix SyncOperationLocationPollingStrategy
        customization.getClass(IMPLEMENTATION_PACKAGE, "SyncOperationLocationPollingStrategy").customizeAst(ast ->
            ast.getClassByName("SyncOperationLocationPollingStrategy").ifPresent(clazz -> {
                for (MethodDeclaration method : clazz.getMethods()) {
                    // Remove @Override from getResult
                    if ("getResult".equals(method.getNameAsString())) {
                        method.getAnnotationByClass(Override.class).ifPresent(Node::remove);
                    }
                    method.getBody().ifPresent(body -> {
                        String bodyStr = body.toString();
                        String updated = bodyStr.replace(
                            "String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER)",
                            "PollingUtils.OPERATION_LOCATION_HEADER.getCaseSensitiveName()");
                        if (!updated.equals(bodyStr)) {
                            method.setBody(StaticJavaParser.parseBlock(updated));
                        }
                    });
                }
            }));
    }

    // =================== Extensions equivalent implementations ===================

    /**
     * Add getValue() method to ContentField class (equivalent to ContentField.Extensions.cs)
     * This allows users to get the typed value regardless of the field subtype.
     */
    private void customizeContentFieldExtensions(LibraryCustomization customization, Logger logger) {
        logger.info("Adding getValue() method to ContentField class");

        customization.getClass(MODELS_PACKAGE, "ContentField").customizeAst(ast ->
            ast.getClassByName("ContentField").ifPresent(clazz -> {
                // Add getValue() method that returns Object based on the actual type
                clazz.addMethod("getValue", Modifier.Keyword.PUBLIC)
                    .setType("Object")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets the value of the field, regardless of its type.\n"
                        + "This base method returns {@code Object}. Each subclass also overrides this method\n"
                        + "with a covariant return type for compile-time type safety:\n"
                        + "- {@link ContentStringField#getValue()} returns {@code String}\n"
                        + "- {@link ContentNumberField#getValue()} returns {@code Double}\n"
                        + "- {@link ContentIntegerField#getValue()} returns {@code Long}\n"
                        + "- {@link ContentDateField#getValue()} returns {@code LocalDate}\n"
                        + "- {@link ContentTimeField#getValue()} returns {@code String}\n"
                        + "- {@link ContentBooleanField#getValue()} returns {@code Boolean}\n"
                        + "- {@link ContentObjectField#getValue()} returns {@code Map<String, ContentField>}\n"
                        + "- {@link ContentArrayField#getValue()} returns {@code List<ContentField>}\n"
                        + "- {@link ContentJsonField#getValue()} returns {@code BinaryData}\n\n"
                        + "When you have a reference to the specific subclass, use its typed {@code getValue()}\n"
                        + "to avoid casting. When you only have a {@code ContentField} reference, this method\n"
                        + "returns the value as {@code Object}."))
                        .addBlockTag("return", "the field value, or null if not available."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "if (this instanceof ContentStringField) { return ((ContentStringField) this).getValueString(); }"
                        + "if (this instanceof ContentNumberField) { return ((ContentNumberField) this).getValueNumber(); }"
                        + "if (this instanceof ContentIntegerField) { return ((ContentIntegerField) this).getValueInteger(); }"
                        + "if (this instanceof ContentDateField) { return ((ContentDateField) this).getValueDate(); }"
                        + "if (this instanceof ContentTimeField) { return ((ContentTimeField) this).getValueTime(); }"
                        + "if (this instanceof ContentBooleanField) { return ((ContentBooleanField) this).isValueBoolean(); }"
                        + "if (this instanceof ContentObjectField) { return ((ContentObjectField) this).getValueObject(); }"
                        + "if (this instanceof ContentArrayField) { return ((ContentArrayField) this).getValueArray(); }"
                        + "if (this instanceof ContentJsonField) { return ((ContentJsonField) this).getValueJson(); }"
                        + "return null; }"));
            }));
    }

    /**
     * Add convenience methods to ContentArrayField class (equivalent to ArrayField.Extensions.cs)
     */
    private void customizeArrayFieldExtensions(LibraryCustomization customization, Logger logger) {
        logger.info("Adding convenience methods to ContentArrayField class");

        customization.getClass(MODELS_PACKAGE, "ContentArrayField").customizeAst(ast -> {
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ast.getClassByName("ContentArrayField").ifPresent(clazz -> {
                // Add static ClientLogger for throwing through Azure SDK lint (ThrowFromClientLoggerCheck)
                clazz.addFieldWithInitializer("ClientLogger", "LOGGER",
                    StaticJavaParser.parseExpression("new ClientLogger(ContentArrayField.class)"),
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

                // Add size() method - equivalent to Count property in C#
                clazz.addMethod("size", Modifier.Keyword.PUBLIC)
                    .setType("int")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets the number of items in the array."))
                        .addBlockTag("return", "the number of items in the array, or 0 if the array is null."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return getValueArray() != null ? getValueArray().size() : 0; }"));

                // Add get(int index) method - equivalent to indexer in C# (throw via ClientLogger per SDK lint)
                clazz.addMethod("get", Modifier.Keyword.PUBLIC)
                    .setType("ContentField")
                    .addParameter("int", "index")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets a field from the array by index."))
                        .addBlockTag("param", "index The zero-based index of the field to retrieve.")
                        .addBlockTag("return", "The field at the specified index.")
                        .addBlockTag("throws", "IndexOutOfBoundsException if the index is out of range."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "if (getValueArray() == null || index < 0 || index >= getValueArray().size()) {"
                        + "    throw LOGGER.logThrowableAsError(new IndexOutOfBoundsException(\"Index \" + index + \" is out of range. Array has \" + size() + \" elements.\"));"
                        + "}"
                        + "return getValueArray().get(index); }"));
            });
        });
    }

    /**
     * Add convenience methods to ContentObjectField class (equivalent to ObjectField.Extensions.cs)
     */
    private void customizeObjectFieldExtensions(LibraryCustomization customization, Logger logger) {
        logger.info("Adding convenience methods to ContentObjectField class");

        customization.getClass(MODELS_PACKAGE, "ContentObjectField").customizeAst(ast -> {
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ast.addImport("java.util.NoSuchElementException");
            ast.getClassByName("ContentObjectField").ifPresent(clazz -> {
                // Add static ClientLogger for throwing through Azure SDK lint (ThrowFromClientLoggerCheck)
                clazz.addFieldWithInitializer("ClientLogger", "LOGGER",
                    StaticJavaParser.parseExpression("new ClientLogger(ContentObjectField.class)"),
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

                // Add getField(String fieldName) method - equivalent to indexer in C# (throw via ClientLogger per SDK lint)
                clazz.addMethod("getField", Modifier.Keyword.PUBLIC)
                    .setType("ContentField")
                    .addParameter("String", "fieldName")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets a field from the object by name."))
                        .addBlockTag("param", "fieldName The name of the field to retrieve.")
                        .addBlockTag("return", "The field if found.")
                        .addBlockTag("throws", "IllegalArgumentException if fieldName is null or empty.")
                        .addBlockTag("throws", "NoSuchElementException if the field is not found."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "if (fieldName == null || fieldName.isEmpty()) {"
                        + "    throw LOGGER.logThrowableAsError(new IllegalArgumentException(\"fieldName cannot be null or empty.\"));"
                        + "}"
                        + "if (getValueObject() != null && getValueObject().containsKey(fieldName)) {"
                        + "    return getValueObject().get(fieldName);"
                        + "}"
                        + "throw LOGGER.logThrowableAsError(\n"
                        + "            new java.util.NoSuchElementException(\"Field '\" + fieldName + \"' was not found in the object.\")); }"));

                // Add getFieldOrDefault(String fieldName) method - returns null if not found
                clazz.addMethod("getFieldOrDefault", Modifier.Keyword.PUBLIC)
                    .setType("ContentField")
                    .addParameter("String", "fieldName")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets a field from the object by name, or null if the field does not exist."))
                        .addBlockTag("param", "fieldName The name of the field to retrieve.")
                        .addBlockTag("return", "The field if found, or null if not found."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "if (fieldName == null || fieldName.isEmpty() || getValueObject() == null) {"
                        + "    return null;"
                        + "}"
                        + "return getValueObject().get(fieldName); }"));
            });
        });
    }

    // =================== SERVICE-FIX implementations ===================

    /**
     * SERVICE-FIX: Customize AudioVisualContent deserialization to handle both "keyFrameTimesMs"
     * and "KeyFrameTimesMs" (capital K) property names for forward-compatibility when the service
     * fixes the casing issue.
     */
    private void customizeAudioVisualContentDeserialization(LibraryCustomization customization, Logger logger) {
        logger.info("SERVICE-FIX: Customizing AudioVisualContent to handle keyFrameTimesMs casing");

        customization.getClass(MODELS_PACKAGE, "AudioVisualContent").customizeAst(ast ->
            ast.getClassByName("AudioVisualContent").ifPresent(clazz -> {
                // Find the fromJson method and modify the keyFrameTimesMs handling
                clazz.getMethodsByName("fromJson").forEach(method -> {
                    method.getBody().ifPresent(body -> {
                        String currentBody = body.toString();
                        // Replace the exact match for keyFrameTimesMs with case-insensitive handling
                        // Original: } else if ("keyFrameTimesMs".equals(fieldName)) {
                        // New: } else if ("keyFrameTimesMs".equals(fieldName) || "KeyFrameTimesMs".equals(fieldName)) {
                        String updatedBody = currentBody.replace(
                            "} else if (\"keyFrameTimesMs\".equals(fieldName)) {",
                            "} else if (\"keyFrameTimesMs\".equals(fieldName) || \"KeyFrameTimesMs\".equals(fieldName)) {"
                        );

                        // Also wrap the keyFrameTimesMs assignment to prevent overwriting if both casings present
                        // Original: keyFrameTimesMs = reader.readArray(reader1 -> reader1.getLong());
                        // New: if (keyFrameTimesMs == null) { keyFrameTimesMs = reader.readArray(...); }
                        updatedBody = updatedBody.replace(
                            "keyFrameTimesMs = reader.readArray(reader1 -> reader1.getLong());",
                            "if (keyFrameTimesMs == null) { keyFrameTimesMs = reader.readArray(reader1 -> reader1.getLong()); }"
                        );

                        method.setBody(StaticJavaParser.parseBlock(updatedBody));
                    });
                });
            }));
    }

    /**
     * Hide generated methods that expose stringEncoding parameter by making them package-private.
     * This prevents stringEncoding from appearing in the public API while still allowing delegation
     * from simplified overloads that use utf16 by default.
     */
    private void hideStringEncodingMethods(LibraryCustomization customization, Logger logger) {
        logger.info("Hiding methods that expose stringEncoding (making package-private)");

        for (String clientClassName : new String[] { "ContentUnderstandingClient", "ContentUnderstandingAsyncClient" }) {
            customization.getClass(PACKAGE_NAME, clientClassName).customizeAst(ast ->
                ast.getClassByName(clientClassName).ifPresent(clazz -> {
                    for (MethodDeclaration method : clazz.getMethods()) {
                        String name = method.getNameAsString();
                        // Match methods that have a parameter (String stringEncoding) by name and type
                        boolean hasStringEncodingParam = method.getParameters().stream()
                            .anyMatch(p -> "stringEncoding".equals(p.getNameAsString())
                                && ("String".equals(p.getType().asString()) || "java.lang.String".equals(p.getType().asString())));

                        if ("beginAnalyze".equals(name)) {
                            // Hide useless 1-param overload beginAnalyze(String analyzerId) that creates empty AnalyzeRequest1
                            if (method.getParameters().size() == 1) {
                                String paramType = method.getParameters().get(0).getType().asString();
                                String paramName = method.getParameters().get(0).getNameAsString();
                                boolean isStringAnalyzerId = ("String".equals(paramType) || "java.lang.String".equals(paramType))
                                    && "analyzerId".equals(paramName);
                                if (isStringAnalyzerId) {
                                    method.removeModifier(Modifier.Keyword.PUBLIC);
                                }
                            }
                            // Hide any beginAnalyze that has (String stringEncoding) parameter
                            else if (hasStringEncodingParam) {
                                method.removeModifier(Modifier.Keyword.PUBLIC);
                            }
                        } else if ("beginAnalyzeBinary".equals(name)) {
                            // Remove overload beginAnalyzeBinary(String analyzerId, BinaryData binaryInput, String stringEncoding) to avoid signature conflict with our 2-param
                            if (method.getParameters().size() == 3) {
                                String t0 = method.getParameters().get(0).getType().asString();
                                String t1 = method.getParameters().get(1).getType().asString();
                                String t2 = method.getParameters().get(2).getType().asString();
                                String n0 = method.getParameters().get(0).getNameAsString();
                                String n1 = method.getParameters().get(1).getNameAsString();
                                String n2 = method.getParameters().get(2).getNameAsString();
                                boolean isString = "String".equals(t0) || "java.lang.String".equals(t0);
                                boolean isBinaryData = "BinaryData".equals(t1) || "com.azure.core.util.BinaryData".equals(t1);
                                boolean isStringEncoding = "String".equals(t2) || "java.lang.String".equals(t2);
                                if (isString && "analyzerId".equals(n0) && isBinaryData && "binaryInput".equals(n1) && isStringEncoding && "stringEncoding".equals(n2)) {
                                    method.remove();
                                }
                            }
                            // Hide any other beginAnalyzeBinary that has (String stringEncoding) parameter (e.g. 6-param)
                            else if (hasStringEncodingParam) {
                                method.removeModifier(Modifier.Keyword.PUBLIC);
                            }
                        }
                    }
                }));
        }
    }

    // =================== Update Convenience Methods ===================

    /**
     * EMITTER-FIX: Make ContentUnderstandingDefaults constructor public so that
     * updateDefaults convenience methods can create and use instances (generated code
     * assumes a public constructor; without this, updateDefaults would not compile).
     */
    private void customizeContentUnderstandingDefaults(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing ContentUnderstandingDefaults to make constructor public and remove @Immutable");

        customization.getClass(MODELS_PACKAGE, "ContentUnderstandingDefaults").customizeAst(ast -> {
            // Remove @Immutable annotation
            ast.getClassByName("ContentUnderstandingDefaults").ifPresent(clazz -> {
                clazz.getAnnotationByName("Immutable").ifPresent(Node::remove);

                // Find the existing constructor and make it public
                clazz.getConstructors().forEach(constructor -> {
                    constructor.removeModifier(Modifier.Keyword.PRIVATE);
                    constructor.addModifier(Modifier.Keyword.PUBLIC);

                    // Update Javadoc
                    constructor.setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Creates an instance of ContentUnderstandingDefaults class."))
                        .addBlockTag("param", "modelDeployments Mapping of model names to deployments. "
                            + "For example: { \"gpt-4.1\": \"myGpt41Deployment\", \"text-embedding-3-large\": \"myTextEmbedding3LargeDeployment\" }."));
                });
            });
        });
    }

    /**
     * EMITTER-FIX: Add convenience methods for updateDefaults that accept typed objects
     * instead of BinaryData. This is equivalent to C# Update Operations in ContentUnderstandingClient.Customizations.cs
     *
     * Note: TypeSpec auto-generates updateAnalyzer convenience methods, so we only add updateDefaults here.
     * The updateDefaults convenience methods were disabled in TypeSpec because they require a public constructor
     * on ContentUnderstandingDefaults, which we enable via customizeContentUnderstandingDefaults.
     */
    private void addUpdateDefaultsConvenienceMethods(LibraryCustomization customization, Logger logger) {
        logger.info("Adding updateDefaults convenience methods");

        // Add to sync client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentUnderstandingDefaults");
            ast.addImport("com.azure.core.annotation.ReturnType");
            ast.addImport("com.azure.core.annotation.ServiceMethod");
            ast.addImport("com.azure.core.util.BinaryData");
            ast.addImport("java.util.Map");

            ast.getClassByName("ContentUnderstandingClient").ifPresent(clazz -> {
                // Add updateDefaults convenience method with Map parameter - returns ContentUnderstandingDefaults directly
                clazz.addMethod("updateDefaults", Modifier.Keyword.PUBLIC)
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.SINGLE)"))
                    .setType("ContentUnderstandingDefaults")
                    .addParameter("Map<String, String>", "modelDeployments")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Update default model deployment settings.\n\n"
                        + "This is the recommended public API for updating default model deployment settings. "
                        + "This method provides a simpler API that accepts a Map of model names to deployment names."))
                        .addBlockTag("param", "modelDeployments Mapping of model names to deployment names. "
                            + "For example: { \"gpt-4.1\": \"myGpt41Deployment\", \"text-embedding-3-large\": \"myTextEmbedding3LargeDeployment\" }.")
                        .addBlockTag("return", "the updated ContentUnderstandingDefaults.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "ContentUnderstandingDefaults defaults = new ContentUnderstandingDefaults(modelDeployments);"
                        + "Response<BinaryData> response = updateDefaultsWithResponse(BinaryData.fromObject(defaults), null);"
                        + "return response.getValue().toObject(ContentUnderstandingDefaults.class); }"));

                // Add updateDefaults convenience method with ContentUnderstandingDefaults parameter
                clazz.addMethod("updateDefaults", Modifier.Keyword.PUBLIC)
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.SINGLE)"))
                    .setType("ContentUnderstandingDefaults")
                    .addParameter("ContentUnderstandingDefaults", "defaults")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Update default model deployment settings.\n\n"
                        + "This is a convenience method that accepts a ContentUnderstandingDefaults object."))
                        .addBlockTag("param", "defaults The ContentUnderstandingDefaults instance with settings to update.")
                        .addBlockTag("return", "the updated ContentUnderstandingDefaults.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "Response<BinaryData> response = updateDefaultsWithResponse(BinaryData.fromObject(defaults), null);"
                        + "return response.getValue().toObject(ContentUnderstandingDefaults.class); }"));
            });
        });

        // Add to async client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingAsyncClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentUnderstandingDefaults");
            ast.addImport("com.azure.core.annotation.ReturnType");
            ast.addImport("com.azure.core.annotation.ServiceMethod");
            ast.addImport("com.azure.core.util.BinaryData");
            ast.addImport("java.util.Map");

            ast.getClassByName("ContentUnderstandingAsyncClient").ifPresent(clazz -> {
                // Add updateDefaults convenience method with Map parameter - returns Mono<ContentUnderstandingDefaults>
                clazz.addMethod("updateDefaults", Modifier.Keyword.PUBLIC)
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.SINGLE)"))
                    .setType("Mono<ContentUnderstandingDefaults>")
                    .addParameter("Map<String, String>", "modelDeployments")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Update default model deployment settings.\n\n"
                        + "This is the recommended public API for updating default model deployment settings. "
                        + "This method provides a simpler API that accepts a Map of model names to deployment names."))
                        .addBlockTag("param", "modelDeployments Mapping of model names to deployment names. "
                            + "For example: { \"gpt-4.1\": \"myGpt41Deployment\", \"text-embedding-3-large\": \"myTextEmbedding3LargeDeployment\" }.")
                        .addBlockTag("return", "the updated ContentUnderstandingDefaults on successful completion of {@link Mono}.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "ContentUnderstandingDefaults defaults = new ContentUnderstandingDefaults(modelDeployments);"
                        + "return updateDefaultsWithResponse(BinaryData.fromObject(defaults), null)"
                        + ".map(response -> response.getValue().toObject(ContentUnderstandingDefaults.class)); }"));

                // Add updateDefaults convenience method with ContentUnderstandingDefaults parameter
                clazz.addMethod("updateDefaults", Modifier.Keyword.PUBLIC)
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.SINGLE)"))
                    .setType("Mono<ContentUnderstandingDefaults>")
                    .addParameter("ContentUnderstandingDefaults", "defaults")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Update default model deployment settings.\n\n"
                        + "This is a convenience method that accepts a ContentUnderstandingDefaults object."))
                        .addBlockTag("param", "defaults The ContentUnderstandingDefaults instance with settings to update.")
                        .addBlockTag("return", "the updated ContentUnderstandingDefaults on successful completion of {@link Mono}.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return updateDefaultsWithResponse(BinaryData.fromObject(defaults), null)"
                        + ".map(response -> response.getValue().toObject(ContentUnderstandingDefaults.class)); }"));
            });
        });
    }

    /**
     * Add beginAnalyzeBinary 2-param convenience overloads without stringEncoding.
     * 2-param overloads delegate to the ContentRange overload (added by addContentRangeOverloads).
     */
    private void addBeginAnalyzeBinaryConvenienceOverloads(LibraryCustomization customization, Logger logger) {
        logger.info("Adding beginAnalyzeBinary convenience overloads (2 param)");

        // Sync client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentRange");
            ast.getClassByName("ContentUnderstandingClient").ifPresent(clazz -> {
                // 2-param: analyzerId, binaryInput
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("BinaryData", "binaryInput")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from binary input. Uses default content type (application/octet-stream), "
                        + "default string encoding (utf16), and service default processing location."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "binaryInput The binary content of the document to analyze.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyzeBinary(analyzerId, binaryInput, (ContentRange) null, \"application/octet-stream\", null); }"));
            });
        });

        // Async client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingAsyncClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentRange");
            ast.getClassByName("ContentUnderstandingAsyncClient").ifPresent(clazz -> {
                // 2-param: analyzerId, binaryInput
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("BinaryData", "binaryInput")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from binary input. Uses default content type (application/octet-stream), "
                        + "default string encoding (utf16), and service default processing location."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "binaryInput The binary content of the document to analyze.")
                        .addBlockTag("return", "the {@link PollerFlux} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyzeBinary(analyzerId, binaryInput, (ContentRange) null, \"application/octet-stream\", null); }"));
            });
        });
    }

    /**
     * Add beginAnalyzeBinary overload accepting ContentRange for a self-documenting range API.
     * This is the primary convenience overload — the 2-param overload delegates here.
     * Adds to both sync and async clients.
     */
    private void addContentRangeOverloads(LibraryCustomization customization, Logger logger) {
        logger.info("Adding ContentRange overloads for beginAnalyzeBinary");

        // Sync client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentRange");
            ast.getClassByName("ContentUnderstandingClient").ifPresent(clazz -> {
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("BinaryData", "binaryInput")
                    .addParameter("ContentRange", "contentRange")
                    .addParameter("String", "contentType")
                    .addParameter("ProcessingLocation", "processingLocation")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from binary input. Uses default string encoding (utf16).\n\n"
                        + "Use factory methods such as {@link ContentRange#pages(int, int)}, "
                        + "{@link ContentRange#timeRange(long, long)}, or "
                        + "{@link ContentRange#combine(ContentRange...)} to build the range."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "binaryInput The binary content of the document to analyze.")
                        .addBlockTag("param", "contentRange Range of the input to analyze. Use ContentRange factory methods to build the range, or null to skip.")
                        .addBlockTag("param", "contentType Request content type.")
                        .addBlockTag("param", "processingLocation The location where the data may be processed. Set to null for service default.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "RequestOptions requestOptions = new RequestOptions();"
                        + "if (contentRange != null) { requestOptions.addQueryParam(\"range\", contentRange.toString(), false); }"
                        + "if (processingLocation != null) { requestOptions.addQueryParam(\"processingLocation\", processingLocation.toString(), false); }"
                        + "requestOptions.addQueryParam(\"stringEncoding\", \"utf16\", false);"
                        + "return serviceClient.beginAnalyzeBinaryWithModel(analyzerId, contentType, binaryInput, requestOptions); }"));
            });
        });

        // Async client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingAsyncClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentRange");
            ast.getClassByName("ContentUnderstandingAsyncClient").ifPresent(clazz -> {
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("BinaryData", "binaryInput")
                    .addParameter("ContentRange", "contentRange")
                    .addParameter("String", "contentType")
                    .addParameter("ProcessingLocation", "processingLocation")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from binary input. Uses default string encoding (utf16).\n\n"
                        + "Use factory methods such as {@link ContentRange#pages(int, int)}, "
                        + "{@link ContentRange#timeRange(long, long)}, or "
                        + "{@link ContentRange#combine(ContentRange...)} to build the range."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "binaryInput The binary content of the document to analyze.")
                        .addBlockTag("param", "contentRange Range of the input to analyze. Use ContentRange factory methods to build the range, or null to skip.")
                        .addBlockTag("param", "contentType Request content type.")
                        .addBlockTag("param", "processingLocation The location where the data may be processed. Set to null for service default.")
                        .addBlockTag("return", "the {@link PollerFlux} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "RequestOptions requestOptions = new RequestOptions();"
                        + "if (contentRange != null) { requestOptions.addQueryParam(\"range\", contentRange.toString(), false); }"
                        + "if (processingLocation != null) { requestOptions.addQueryParam(\"processingLocation\", processingLocation.toString(), false); }"
                        + "requestOptions.addQueryParam(\"stringEncoding\", \"utf16\", false);"
                        + "return serviceClient.beginAnalyzeBinaryWithModelAsync(analyzerId, contentType, binaryInput, requestOptions); }"));
            });
        });
    }

    /**
     * Add setContentRange(ContentRange) overload to AnalysisInput model class.
     * This allows users to pass a ContentRange directly instead of a raw String.
     */
    private void addContentRangeSetterToAnalyzeInput(LibraryCustomization customization, Logger logger) {
        logger.info("Adding setContentRange(ContentRange) overload to AnalysisInput");

        customization.getClass(MODELS_PACKAGE, "AnalysisInput").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.ContentRange");
            ast.getClassByName("AnalysisInput").ifPresent(clazz -> {
                clazz.addMethod("setContentRange", Modifier.Keyword.PUBLIC)
                    .setType("AnalysisInput")
                    .addParameter("ContentRange", "contentRange")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Set the contentRange property using a {@link ContentRange} for a self-documenting API.\n\n"
                        + "Use factory methods such as {@link ContentRange#pages(int, int)}, "
                        + "{@link ContentRange#timeRange(long, long)}, or "
                        + "{@link ContentRange#combine(ContentRange...)} to build the range."))
                        .addBlockTag("param", "contentRange the range value to set, or null to clear.")
                        .addBlockTag("return", "the AnalysisInput object itself."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "this.contentRange = contentRange != null ? contentRange.toString() : null;"
                        + "return this; }"));
            });
        });
    }

    /**
     * Add beginAnalyze convenience overloads without stringEncoding.
     * Adds 2-param and 4-param overloads that default utf16.
     */
    private void addBeginAnalyzeConvenienceOverloads(LibraryCustomization customization, Logger logger) {
        logger.info("Adding beginAnalyze convenience overloads (2/4 param)");

        // Sync client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.implementation.models.AnalyzeRequest1");
            ast.addImport("com.azure.core.util.BinaryData");
            ast.getClassByName("ContentUnderstandingClient").ifPresent(clazz -> {
                // 2-param: analyzerId, inputs
                clazz.addMethod("beginAnalyze", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalysisInput>", "inputs")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from inputs. Uses default string encoding (utf16), "
                        + "service default model deployments, and global processing location."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "inputs The inputs to analyze.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyze(analyzerId, inputs, null, null); }"));

                // 4-param: analyzerId, inputs, modelDeployments, processingLocation
                clazz.addMethod("beginAnalyze", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalysisInput>", "inputs")
                    .addParameter("Map<String, String>", "modelDeployments")
                    .addParameter("ProcessingLocation", "processingLocation")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from inputs. Uses default string encoding (utf16)."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "inputs The inputs to analyze.")
                        .addBlockTag("param", "modelDeployments Custom model deployment mappings. Set to null to use service defaults.")
                        .addBlockTag("param", "processingLocation The processing location for the analysis. Set to null to use the service default.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "RequestOptions requestOptions = new RequestOptions();"
                        + "if (processingLocation != null) { requestOptions.addQueryParam(\"processingLocation\", processingLocation.toString(), false); }"
                        + "requestOptions.addQueryParam(\"stringEncoding\", \"utf16\", false);"
                        + "AnalyzeRequest1 analyzeRequest1Obj = new AnalyzeRequest1(inputs).setModelDeployments(modelDeployments);"
                        + "BinaryData analyzeRequest1 = BinaryData.fromObject(analyzeRequest1Obj);"
                        + "return serviceClient.beginAnalyzeWithModel(analyzerId, analyzeRequest1, requestOptions); }"));
            });
        });

        // Async client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingAsyncClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.implementation.models.AnalyzeRequest1");
            ast.addImport("com.azure.core.util.BinaryData");
            ast.getClassByName("ContentUnderstandingAsyncClient").ifPresent(clazz -> {
                // 2-param: analyzerId, inputs
                clazz.addMethod("beginAnalyze", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalysisInput>", "inputs")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from inputs. Uses default string encoding (utf16), "
                        + "service default model deployments, and global processing location."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "inputs The inputs to analyze.")
                        .addBlockTag("return", "the {@link PollerFlux} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyze(analyzerId, inputs, null, null); }"));

                // 4-param: analyzerId, inputs, modelDeployments, processingLocation
                clazz.addMethod("beginAnalyze", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalysisInput>", "inputs")
                    .addParameter("Map<String, String>", "modelDeployments")
                    .addParameter("ProcessingLocation", "processingLocation")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from inputs. Uses default string encoding (utf16)."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "inputs The inputs to analyze.")
                        .addBlockTag("param", "modelDeployments Custom model deployment mappings. Set to null to use service defaults.")
                        .addBlockTag("param", "processingLocation The processing location for the analysis. Set to null to use the service default.")
                        .addBlockTag("return", "the {@link PollerFlux} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "RequestOptions requestOptions = new RequestOptions();"
                        + "if (processingLocation != null) { requestOptions.addQueryParam(\"processingLocation\", processingLocation.toString(), false); }"
                        + "requestOptions.addQueryParam(\"stringEncoding\", \"utf16\", false);"
                        + "AnalyzeRequest1 analyzeRequest1Obj = new AnalyzeRequest1(inputs).setModelDeployments(modelDeployments);"
                        + "BinaryData analyzeRequest1 = BinaryData.fromObject(analyzeRequest1Obj);"
                        + "return serviceClient.beginAnalyzeWithModelAsync(analyzerId, analyzeRequest1, requestOptions); }"));
            });
        });
    }

    /**
     * Add typed getValue() override to each ContentField subclass and hide the verbose getters
     * (e.g., getValueString, getValueNumber) by removing their PUBLIC modifier.
     *
     * Each subclass's getValue() returns the covariant return type (e.g., String for ContentStringField)
     * and delegates to the now-package-private verbose getter.
     */
    private void customizeFieldValueAccessors(LibraryCustomization customization, Logger logger) {
        logger.info("Adding typed getValue() to ContentField subclasses and hiding verbose getters");

        // ContentStringField: getValue() -> String, hide getValueString()
        addTypedGetValueAndHideVerbose(customization, "ContentStringField", "String", "getValueString",
            "getValueString()", logger);

        // ContentNumberField: getValue() -> Double, hide getValueNumber()
        addTypedGetValueAndHideVerbose(customization, "ContentNumberField", "Double", "getValueNumber",
            "getValueNumber()", logger);

        // ContentIntegerField: getValue() -> Long, hide getValueInteger()
        addTypedGetValueAndHideVerbose(customization, "ContentIntegerField", "Long", "getValueInteger",
            "getValueInteger()", logger);

        // ContentDateField: getValue() -> LocalDate, hide getValueDate()
        addTypedGetValueAndHideVerbose(customization, "ContentDateField", "LocalDate", "getValueDate",
            "getValueDate()", logger);

        // ContentTimeField: getValue() -> String, hide getValueTime()
        addTypedGetValueAndHideVerbose(customization, "ContentTimeField", "String", "getValueTime",
            "getValueTime()", logger);

        // ContentBooleanField: getValue() -> Boolean, hide isValueBoolean()
        addTypedGetValueAndHideVerbose(customization, "ContentBooleanField", "Boolean", "isValueBoolean",
            "isValueBoolean()", logger);

        // ContentObjectField: getValue() -> Map<String, ContentField>, hide getValueObject()
        addTypedGetValueAndHideVerbose(customization, "ContentObjectField", "Map<String, ContentField>", "getValueObject",
            "getValueObject()", logger);

        // ContentArrayField: getValue() -> List<ContentField>, hide getValueArray()
        addTypedGetValueAndHideVerbose(customization, "ContentArrayField", "List<ContentField>", "getValueArray",
            "getValueArray()", logger);

        // ContentJsonField: getValue() -> BinaryData, hide getValueJson()
        addTypedGetValueAndHideVerbose(customization, "ContentJsonField", "BinaryData", "getValueJson",
            "getValueJson()", logger);
    }

    /**
     * Helper: adds a typed getValue() override to a ContentField subclass and hides the verbose getter.
     */
    private void addTypedGetValueAndHideVerbose(LibraryCustomization customization, String className,
            String returnType, String verboseMethodName, String delegateCall, Logger logger) {
        customization.getClass(MODELS_PACKAGE, className).customizeAst(ast ->
            ast.getClassByName(className).ifPresent(clazz -> {
                // Hide the verbose getter by removing PUBLIC modifier
                clazz.getMethodsByName(verboseMethodName).forEach(method ->
                    method.removeModifier(Modifier.Keyword.PUBLIC));

                // Add typed getValue() override
                clazz.addMethod("getValue", Modifier.Keyword.PUBLIC)
                    .setType(returnType)
                    .addMarkerAnnotation(Override.class)
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets the strongly-typed value of this field."))
                        .addBlockTag("return", "the field value, or null if not available."))
                    .setBody(StaticJavaParser.parseBlock("{ return " + delegateCall + "; }"));
            }));
    }

    // =================== Duration property customizations ===================

    /**
     * Add Duration-returning getters to time-based models and hide the raw *Ms() getters
     * (make them package-private) so the public API exposes Duration instead of raw milliseconds.
     *
     * <p>Models and properties affected:</p>
     * <ul>
     *   <li>AudioVisualContent: startTimeMs, endTimeMs, cameraShotTimesMs (list), keyFrameTimesMs (list)</li>
     *   <li>AudioVisualContentSegment: startTimeMs, endTimeMs</li>
     *   <li>TranscriptPhrase: startTimeMs, endTimeMs</li>
     *   <li>TranscriptWord: startTimeMs, endTimeMs</li>
     * </ul>
     */
    private void customizeDurationProperties(LibraryCustomization customization, Logger logger) {
        logger.info("Adding Duration getters and hiding *Ms() getters on time-based models");

        // AudioVisualContent: scalar + list properties
        customization.getClass(MODELS_PACKAGE, "AudioVisualContent").customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ast.addImport("java.util.stream.Collectors");
            ast.getClassByName("AudioVisualContent").ifPresent(clazz -> {
                hideMsGetterAndAddDuration(clazz, "getStartTimeMs", "getStartTime", "startTimeMs", false);
                hideMsGetterAndAddDuration(clazz, "getEndTimeMs", "getEndTime", "endTimeMs", false);
                hideMsGetterAndAddDuration(clazz, "getCameraShotTimesMs", "getCameraShotTimes",
                    "cameraShotTimesMs", true);
                hideMsGetterAndAddDuration(clazz, "getKeyFrameTimesMs", "getKeyFrameTimes",
                    "keyFrameTimesMs", true);
            });
        });

        // AudioVisualContentSegment: scalar properties
        customization.getClass(MODELS_PACKAGE, "AudioVisualContentSegment").customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ast.getClassByName("AudioVisualContentSegment").ifPresent(clazz -> {
                hideMsGetterAndAddDuration(clazz, "getStartTimeMs", "getStartTime", "startTimeMs", false);
                hideMsGetterAndAddDuration(clazz, "getEndTimeMs", "getEndTime", "endTimeMs", false);
            });
        });

        // TranscriptPhrase: scalar properties
        customization.getClass(MODELS_PACKAGE, "TranscriptPhrase").customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ast.getClassByName("TranscriptPhrase").ifPresent(clazz -> {
                hideMsGetterAndAddDuration(clazz, "getStartTimeMs", "getStartTime", "startTimeMs", false);
                hideMsGetterAndAddDuration(clazz, "getEndTimeMs", "getEndTime", "endTimeMs", false);
            });
        });

        // TranscriptWord: scalar properties
        customization.getClass(MODELS_PACKAGE, "TranscriptWord").customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ast.getClassByName("TranscriptWord").ifPresent(clazz -> {
                hideMsGetterAndAddDuration(clazz, "getStartTimeMs", "getStartTime", "startTimeMs", false);
                hideMsGetterAndAddDuration(clazz, "getEndTimeMs", "getEndTime", "endTimeMs", false);
            });
        });
    }

    /**
     * Helper: hides a generated *Ms() getter (removes PUBLIC modifier) and adds a Duration-returning getter.
     *
     * @param clazz the class declaration to modify
     * @param msMethodName the generated getter name (e.g., "getStartTimeMs")
     * @param durationMethodName the new Duration getter name (e.g., "getStartTime")
     * @param fieldName the backing field name (e.g., "startTimeMs")
     * @param isList true if the property is List&lt;Long&gt; (returns List&lt;Duration&gt;)
     */
    private void hideMsGetterAndAddDuration(ClassOrInterfaceDeclaration clazz, String msMethodName,
            String durationMethodName, String fieldName, boolean isList) {
        // Hide the *Ms() getter by removing PUBLIC modifier
        clazz.getMethodsByName(msMethodName).forEach(method ->
            method.removeModifier(Modifier.Keyword.PUBLIC));

        if (isList) {
            // List<Long> -> List<Duration>
            clazz.addMethod(durationMethodName, Modifier.Keyword.PUBLIC)
                .setType("List<Duration>")
                .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                    "Gets the " + fieldName.replace("Ms", "") + " as a list of Duration values."))
                    .addBlockTag("return", "the durations, or null if not available."))
                .setBody(StaticJavaParser.parseBlock(
                    "{ if (this." + fieldName + " == null) { return null; } "
                    + "return this." + fieldName + ".stream()"
                    + ".map(Duration::ofMillis)"
                    + ".collect(Collectors.toList()); }"));
        } else {
            // long -> Duration
            clazz.addMethod(durationMethodName, Modifier.Keyword.PUBLIC)
                .setType("Duration")
                .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                    "Gets the " + fieldName.replace("Ms", "") + " as a Duration."))
                    .addBlockTag("return", "the duration."))
                .setBody(StaticJavaParser.parseBlock(
                    "{ return Duration.ofMillis(this." + fieldName + "); }"));
        }
    }

    // =================== ContentSource class hierarchy ===================

    private static final String SRC_MODELS = "src/main/java/com/azure/ai/contentunderstanding/models/";

    /**
     * Add ContentSource, DocumentSource, AudioVisualSource, TrackletSource, and geometry types
     * (PointF, RectangleF, Rectangle) as custom files via the raw editor.
     */
    private void addContentSourceClasses(LibraryCustomization customization, Logger logger) {
        logger.info("Adding ContentSource class hierarchy and geometry types");

        customization.getRawEditor().addFile(SRC_MODELS + "PointF.java", POINT_F_CONTENT);
        customization.getRawEditor().addFile(SRC_MODELS + "RectangleF.java", RECTANGLE_F_CONTENT);
        customization.getRawEditor().addFile(SRC_MODELS + "Rectangle.java", RECTANGLE_CONTENT);
        customization.getRawEditor().addFile(SRC_MODELS + "ContentSource.java", CONTENT_SOURCE_CONTENT);
        customization.getRawEditor().addFile(SRC_MODELS + "DocumentSource.java", DOCUMENT_SOURCE_CONTENT);
        customization.getRawEditor().addFile(SRC_MODELS + "AudioVisualSource.java", AUDIO_VISUAL_SOURCE_CONTENT);
        customization.getRawEditor().addFile(SRC_MODELS + "TrackletSource.java", TRACKLET_SOURCE_CONTENT);
    }

    /**
     * Add getGroundingSources() method to ContentField via AST customization.
     */
    private void addGroundingSourcesMethod(LibraryCustomization customization, Logger logger) {
        logger.info("Adding getGroundingSources() to ContentField");

        customization.getClass(MODELS_PACKAGE, "ContentField").customizeAst(ast ->
            ast.getClassByName("ContentField").ifPresent(clazz ->
                clazz.addMethod("getGroundingSources", Modifier.Keyword.PUBLIC)
                    .setType("ContentSource[]")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Parses the encoded source string into typed grounding sources.\n\n"
                        + "The returned array contains {@link DocumentSource}, {@link AudioVisualSource}, "
                        + "or {@link TrackletSource} instances depending on the wire format.\n"
                        + "Returns {@code null} if the source string is null or empty."))
                        .addBlockTag("return", "an array of {@link ContentSource} instances, or null if no source is available.")
                        .addBlockTag("see", "ContentSource#parseAll(String)"))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "String src = getSource();"
                        + "return (src == null || src.isEmpty()) ? null : ContentSource.parseAll(src); }"))));
    }

    // =================== ContentSource file contents ===================

    private static final String POINT_F_CONTENT =
        "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
        + "// Licensed under the MIT License.\n\n"
        + "package com.azure.ai.contentunderstanding.models;\n\n"
        + "import com.azure.core.annotation.Immutable;\n\n"
        + "import java.util.Objects;\n\n"
        + "/**\n"
        + " * Represents a point with float-precision x and y coordinates.\n"
        + " * Used by {@link DocumentSource} to define polygon vertices in document coordinate space.\n"
        + " */\n"
        + "@Immutable\n"
        + "public final class PointF {\n"
        + "    private final float x;\n"
        + "    private final float y;\n\n"
        + "    /**\n"
        + "     * Creates a new {@link PointF}.\n"
        + "     *\n"
        + "     * @param x The x-coordinate.\n"
        + "     * @param y The y-coordinate.\n"
        + "     */\n"
        + "    public PointF(float x, float y) {\n"
        + "        this.x = x;\n"
        + "        this.y = y;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the x-coordinate.\n"
        + "     *\n"
        + "     * @return The x-coordinate.\n"
        + "     */\n"
        + "    public float getX() {\n"
        + "        return x;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the y-coordinate.\n"
        + "     *\n"
        + "     * @return The y-coordinate.\n"
        + "     */\n"
        + "    public float getY() {\n"
        + "        return y;\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public String toString() {\n"
        + "        return \"(\" + x + \", \" + y + \")\";\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public boolean equals(Object obj) {\n"
        + "        if (this == obj) {\n"
        + "            return true;\n"
        + "        }\n"
        + "        if (!(obj instanceof PointF)) {\n"
        + "            return false;\n"
        + "        }\n"
        + "        PointF other = (PointF) obj;\n"
        + "        return Float.compare(x, other.x) == 0 && Float.compare(y, other.y) == 0;\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public int hashCode() {\n"
        + "        return Objects.hash(x, y);\n"
        + "    }\n"
        + "}\n";

    private static final String RECTANGLE_F_CONTENT =
        "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
        + "// Licensed under the MIT License.\n\n"
        + "package com.azure.ai.contentunderstanding.models;\n\n"
        + "import com.azure.core.annotation.Immutable;\n\n"
        + "import java.util.Objects;\n\n"
        + "/**\n"
        + " * Represents an axis-aligned rectangle with float-precision coordinates.\n"
        + " * Used by {@link DocumentSource} as the bounding box computed from polygon coordinates.\n"
        + " */\n"
        + "@Immutable\n"
        + "public final class RectangleF {\n"
        + "    private final float x;\n"
        + "    private final float y;\n"
        + "    private final float width;\n"
        + "    private final float height;\n\n"
        + "    /**\n"
        + "     * Creates a new {@link RectangleF}.\n"
        + "     *\n"
        + "     * @param x The x-coordinate of the top-left corner.\n"
        + "     * @param y The y-coordinate of the top-left corner.\n"
        + "     * @param width The width of the rectangle.\n"
        + "     * @param height The height of the rectangle.\n"
        + "     */\n"
        + "    public RectangleF(float x, float y, float width, float height) {\n"
        + "        this.x = x;\n"
        + "        this.y = y;\n"
        + "        this.width = width;\n"
        + "        this.height = height;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the x-coordinate of the top-left corner.\n"
        + "     *\n"
        + "     * @return The x-coordinate.\n"
        + "     */\n"
        + "    public float getX() {\n"
        + "        return x;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the y-coordinate of the top-left corner.\n"
        + "     *\n"
        + "     * @return The y-coordinate.\n"
        + "     */\n"
        + "    public float getY() {\n"
        + "        return y;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the width of the rectangle.\n"
        + "     *\n"
        + "     * @return The width.\n"
        + "     */\n"
        + "    public float getWidth() {\n"
        + "        return width;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the height of the rectangle.\n"
        + "     *\n"
        + "     * @return The height.\n"
        + "     */\n"
        + "    public float getHeight() {\n"
        + "        return height;\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public String toString() {\n"
        + "        return \"[x=\" + x + \", y=\" + y + \", width=\" + width + \", height=\" + height + \"]\";\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public boolean equals(Object obj) {\n"
        + "        if (this == obj) {\n"
        + "            return true;\n"
        + "        }\n"
        + "        if (!(obj instanceof RectangleF)) {\n"
        + "            return false;\n"
        + "        }\n"
        + "        RectangleF other = (RectangleF) obj;\n"
        + "        return Float.compare(x, other.x) == 0\n"
        + "            && Float.compare(y, other.y) == 0\n"
        + "            && Float.compare(width, other.width) == 0\n"
        + "            && Float.compare(height, other.height) == 0;\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public int hashCode() {\n"
        + "        return Objects.hash(x, y, width, height);\n"
        + "    }\n"
        + "}\n";

    private static final String RECTANGLE_CONTENT =
        "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
        + "// Licensed under the MIT License.\n\n"
        + "package com.azure.ai.contentunderstanding.models;\n\n"
        + "import com.azure.core.annotation.Immutable;\n\n"
        + "import java.util.Objects;\n\n"
        + "/**\n"
        + " * Represents an axis-aligned rectangle with integer coordinates.\n"
        + " * Used by {@link AudioVisualSource} as the bounding box for spatial information (e.g., face detection).\n"
        + " */\n"
        + "@Immutable\n"
        + "public final class Rectangle {\n"
        + "    private final int x;\n"
        + "    private final int y;\n"
        + "    private final int width;\n"
        + "    private final int height;\n\n"
        + "    /**\n"
        + "     * Creates a new {@link Rectangle}.\n"
        + "     *\n"
        + "     * @param x The x-coordinate of the top-left corner.\n"
        + "     * @param y The y-coordinate of the top-left corner.\n"
        + "     * @param width The width of the rectangle.\n"
        + "     * @param height The height of the rectangle.\n"
        + "     */\n"
        + "    public Rectangle(int x, int y, int width, int height) {\n"
        + "        this.x = x;\n"
        + "        this.y = y;\n"
        + "        this.width = width;\n"
        + "        this.height = height;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the x-coordinate of the top-left corner.\n"
        + "     *\n"
        + "     * @return The x-coordinate.\n"
        + "     */\n"
        + "    public int getX() {\n"
        + "        return x;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the y-coordinate of the top-left corner.\n"
        + "     *\n"
        + "     * @return The y-coordinate.\n"
        + "     */\n"
        + "    public int getY() {\n"
        + "        return y;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the width of the rectangle.\n"
        + "     *\n"
        + "     * @return The width.\n"
        + "     */\n"
        + "    public int getWidth() {\n"
        + "        return width;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the height of the rectangle.\n"
        + "     *\n"
        + "     * @return The height.\n"
        + "     */\n"
        + "    public int getHeight() {\n"
        + "        return height;\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public String toString() {\n"
        + "        return \"[x=\" + x + \", y=\" + y + \", width=\" + width + \", height=\" + height + \"]\";\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public boolean equals(Object obj) {\n"
        + "        if (this == obj) {\n"
        + "            return true;\n"
        + "        }\n"
        + "        if (!(obj instanceof Rectangle)) {\n"
        + "            return false;\n"
        + "        }\n"
        + "        Rectangle other = (Rectangle) obj;\n"
        + "        return x == other.x && y == other.y && width == other.width && height == other.height;\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public int hashCode() {\n"
        + "        return Objects.hash(x, y, width, height);\n"
        + "    }\n"
        + "}\n";

    private static final String CONTENT_SOURCE_CONTENT =
        "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
        + "// Licensed under the MIT License.\n\n"
        + "package com.azure.ai.contentunderstanding.models;\n\n"
        + "import com.azure.core.annotation.Immutable;\n"
        + "import com.azure.core.util.logging.ClientLogger;\n\n"
        + "import java.util.ArrayList;\n"
        + "import java.util.List;\n"
        + "import java.util.Objects;\n\n"
        + "/**\n"
        + " * Abstract base class for parsed grounding sources returned by Content Understanding.\n"
        + " *\n"
        + " * <p>The service encodes source positions as compact strings in the {@link ContentField#getSource()} property.\n"
        + " * This class hierarchy parses those strings into strongly-typed objects:</p>\n"
        + " * <ul>\n"
        + " *   <li>{@link DocumentSource} &mdash; {@code D(page,x1,y1,x2,y2,x3,y3,x4,y4)}</li>\n"
        + " *   <li>{@link AudioVisualSource} &mdash; {@code AV(time[,x,y,w,h])}</li>\n"
        + " *   <li>{@link TrackletSource} &mdash; {@code AV(...)-AV(...)}</li>\n"
        + " * </ul>\n"
        + " *\n"
        + " * <p>Use {@link #parse(String)} to parse a single segment, or {@link #parseAll(String)} to parse\n"
        + " * a semicolon-delimited string containing multiple segments.</p>\n"
        + " *\n"
        + " * @see ContentField#getGroundingSources()\n"
        + " */\n"
        + "@Immutable\n"
        + "public abstract class ContentSource {\n"
        + "    private static final ClientLogger LOGGER = new ClientLogger(ContentSource.class);\n\n"
        + "    private final String rawValue;\n\n"
        + "    /**\n"
        + "     * Initializes a new instance of {@link ContentSource}.\n"
        + "     *\n"
        + "     * @param rawValue The raw wire-format source string.\n"
        + "     */\n"
        + "    protected ContentSource(String rawValue) {\n"
        + "        this.rawValue = Objects.requireNonNull(rawValue, \"'rawValue' cannot be null.\");\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the original wire-format source string.\n"
        + "     *\n"
        + "     * @return The raw source string.\n"
        + "     */\n"
        + "    public String getRawValue() {\n"
        + "        return rawValue;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Parses a single source segment, automatically detecting the source type.\n"
        + "     *\n"
        + "     * <p>Tracklet pairs ({@code AV(...)-AV(...)}) are automatically detected and returned\n"
        + "     * as {@link TrackletSource} instances.</p>\n"
        + "     *\n"
        + "     * @param source The source string to parse.\n"
        + "     * @return A {@link ContentSource} subclass instance.\n"
        + "     * @throws NullPointerException if {@code source} is null.\n"
        + "     * @throws IllegalArgumentException if {@code source} is empty or has an unrecognized format.\n"
        + "     */\n"
        + "    public static ContentSource parse(String source) {\n"
        + "        Objects.requireNonNull(source, \"'source' cannot be null.\");\n"
        + "        if (source.isEmpty()) {\n"
        + "            throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"'source' cannot be empty.\"));\n"
        + "        }\n"
        + "        if (source.startsWith(\"D(\")) {\n"
        + "            return DocumentSource.parse(source);\n"
        + "        }\n"
        + "        if (source.startsWith(\"AV(\")) {\n"
        + "            if (source.indexOf(\")-AV(\") >= 0) {\n"
        + "                return TrackletSource.parse(source);\n"
        + "            }\n"
        + "            return AudioVisualSource.parse(source);\n"
        + "        }\n"
        + "        throw LOGGER.logExceptionAsError(\n"
        + "            new IllegalArgumentException(\"Unrecognized source format: '\" + source + \"'.\"));\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Parses a semicolon-delimited string containing one or more source segments.\n"
        + "     *\n"
        + "     * <p>Each segment is parsed individually using {@link #parse(String)}.\n"
        + "     * Tracklet pairs within segments are automatically detected.</p>\n"
        + "     *\n"
        + "     * @param source The source string (may contain {@code ;} delimiters).\n"
        + "     * @return An array of {@link ContentSource} instances.\n"
        + "     * @throws NullPointerException if {@code source} is null.\n"
        + "     * @throws IllegalArgumentException if {@code source} is empty or any segment has an unrecognized format.\n"
        + "     */\n"
        + "    public static ContentSource[] parseAll(String source) {\n"
        + "        Objects.requireNonNull(source, \"'source' cannot be null.\");\n"
        + "        if (source.isEmpty()) {\n"
        + "            throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"'source' cannot be empty.\"));\n"
        + "        }\n"
        + "        String[] segments = source.split(\";\");\n"
        + "        List<ContentSource> results = new ArrayList<>(segments.length);\n"
        + "        for (String segment : segments) {\n"
        + "            String trimmed = segment.trim();\n"
        + "            if (!trimmed.isEmpty()) {\n"
        + "                results.add(parse(trimmed));\n"
        + "            }\n"
        + "        }\n"
        + "        return results.toArray(new ContentSource[0]);\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Returns the wire-format string representation of this source.\n"
        + "     *\n"
        + "     * @return The raw source string.\n"
        + "     */\n"
        + "    @Override\n"
        + "    public String toString() {\n"
        + "        return rawValue;\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public boolean equals(Object obj) {\n"
        + "        if (this == obj) {\n"
        + "            return true;\n"
        + "        }\n"
        + "        if (!(obj instanceof ContentSource)) {\n"
        + "            return false;\n"
        + "        }\n"
        + "        ContentSource other = (ContentSource) obj;\n"
        + "        return Objects.equals(rawValue, other.rawValue);\n"
        + "    }\n\n"
        + "    @Override\n"
        + "    public int hashCode() {\n"
        + "        return Objects.hashCode(rawValue);\n"
        + "    }\n"
        + "}\n";

    private static final String DOCUMENT_SOURCE_CONTENT =
        "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
        + "// Licensed under the MIT License.\n\n"
        + "package com.azure.ai.contentunderstanding.models;\n\n"
        + "import com.azure.core.annotation.Immutable;\n"
        + "import com.azure.core.util.logging.ClientLogger;\n\n"
        + "import java.util.ArrayList;\n"
        + "import java.util.Collections;\n"
        + "import java.util.List;\n"
        + "import java.util.Objects;\n\n"
        + "/**\n"
        + " * Represents a parsed document grounding source in the format {@code D(page,x1,y1,x2,y2,x3,y3,x4,y4)}.\n"
        + " *\n"
        + " * <p>The page number is 1-based. The polygon is a quadrilateral defined by four points\n"
        + " * with coordinates in the document's coordinate space.</p>\n"
        + " *\n"
        + " * @see ContentSource\n"
        + " */\n"
        + "@Immutable\n"
        + "public final class DocumentSource extends ContentSource {\n"
        + "    private static final ClientLogger LOGGER = new ClientLogger(DocumentSource.class);\n"
        + "    private static final String PREFIX = \"D(\";\n"
        + "    private static final int EXPECTED_PARAM_COUNT = 9;\n\n"
        + "    private final int pageNumber;\n"
        + "    private final List<PointF> polygon;\n"
        + "    private final RectangleF boundingBox;\n\n"
        + "    private DocumentSource(String source) {\n"
        + "        super(source);\n"
        + "        if (!source.startsWith(PREFIX) || !source.endsWith(\")\")) {\n"
        + "            throw LOGGER.logExceptionAsError(\n"
        + "                new IllegalArgumentException(\"Document source must start with '\" + PREFIX + \"' and end with ')': '\" + source + \"'.\"));\n"
        + "        }\n"
        + "        String inner = source.substring(PREFIX.length(), source.length() - 1);\n"
        + "        String[] parts = inner.split(\",\");\n"
        + "        if (parts.length != EXPECTED_PARAM_COUNT) {\n"
        + "            throw LOGGER.logExceptionAsError(\n"
        + "                new IllegalArgumentException(\"Document source expected \" + EXPECTED_PARAM_COUNT\n"
        + "                    + \" parameters (page + 8 coordinates), got \" + parts.length + \": '\" + source + \"'.\"));\n"
        + "        }\n"
        + "        try {\n"
        + "            this.pageNumber = Integer.parseInt(parts[0].trim());\n"
        + "        } catch (NumberFormatException e) {\n"
        + "            throw LOGGER.logExceptionAsError(\n"
        + "                new IllegalArgumentException(\"Invalid page number in document source: '\" + parts[0] + \"'.\", e));\n"
        + "        }\n"
        + "        if (this.pageNumber < 1) {\n"
        + "            throw LOGGER.logExceptionAsError(\n"
        + "                new IllegalArgumentException(\"Page number must be >= 1, got \" + this.pageNumber + \".\"));\n"
        + "        }\n"
        + "        List<PointF> points = new ArrayList<>(4);\n"
        + "        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;\n"
        + "        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;\n"
        + "        for (int i = 0; i < 4; i++) {\n"
        + "            int xIndex = 1 + (i * 2);\n"
        + "            int yIndex = 2 + (i * 2);\n"
        + "            float x, y;\n"
        + "            try {\n"
        + "                x = Float.parseFloat(parts[xIndex].trim());\n"
        + "            } catch (NumberFormatException e) {\n"
        + "                throw LOGGER.logExceptionAsError(\n"
        + "                    new IllegalArgumentException(\"Invalid x-coordinate at index \" + xIndex + \": '\" + parts[xIndex] + \"'.\", e));\n"
        + "            }\n"
        + "            try {\n"
        + "                y = Float.parseFloat(parts[yIndex].trim());\n"
        + "            } catch (NumberFormatException e) {\n"
        + "                throw LOGGER.logExceptionAsError(\n"
        + "                    new IllegalArgumentException(\"Invalid y-coordinate at index \" + yIndex + \": '\" + parts[yIndex] + \"'.\", e));\n"
        + "            }\n"
        + "            points.add(new PointF(x, y));\n"
        + "            minX = Math.min(minX, x);\n"
        + "            minY = Math.min(minY, y);\n"
        + "            maxX = Math.max(maxX, x);\n"
        + "            maxY = Math.max(maxY, y);\n"
        + "        }\n"
        + "        this.polygon = Collections.unmodifiableList(points);\n"
        + "        this.boundingBox = new RectangleF(minX, minY, maxX - minX, maxY - minY);\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the 1-based page number.\n"
        + "     *\n"
        + "     * @return The page number.\n"
        + "     */\n"
        + "    public int getPageNumber() {\n"
        + "        return pageNumber;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the polygon coordinates as four points defining a quadrilateral region.\n"
        + "     *\n"
        + "     * @return An unmodifiable list of four {@link PointF} values.\n"
        + "     */\n"
        + "    public List<PointF> getPolygon() {\n"
        + "        return polygon;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the axis-aligned bounding rectangle computed from the polygon coordinates.\n"
        + "     * Useful for drawing highlight rectangles over extracted fields.\n"
        + "     *\n"
        + "     * @return The bounding box.\n"
        + "     */\n"
        + "    public RectangleF getBoundingBox() {\n"
        + "        return boundingBox;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Parses a single document source segment.\n"
        + "     *\n"
        + "     * @param source The source string in the format {@code D(page,x1,y1,...,x4,y4)}.\n"
        + "     * @return A new {@link DocumentSource}.\n"
        + "     * @throws NullPointerException if {@code source} is null.\n"
        + "     * @throws IllegalArgumentException if the source string is not in the expected format.\n"
        + "     */\n"
        + "    public static DocumentSource parse(String source) {\n"
        + "        Objects.requireNonNull(source, \"'source' cannot be null.\");\n"
        + "        if (source.isEmpty()) {\n"
        + "            throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"'source' cannot be empty.\"));\n"
        + "        }\n"
        + "        return new DocumentSource(source);\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Parses a source string containing one or more document source segments separated by {@code ;}.\n"
        + "     *\n"
        + "     * @param source The source string (may contain {@code ;} delimiters).\n"
        + "     * @return An array of {@link DocumentSource} instances.\n"
        + "     * @throws NullPointerException if {@code source} is null.\n"
        + "     * @throws IllegalArgumentException if any segment is not in the expected format.\n"
        + "     */\n"
        + "    public static DocumentSource[] parseAll(String source) {\n"
        + "        Objects.requireNonNull(source, \"'source' cannot be null.\");\n"
        + "        if (source.isEmpty()) {\n"
        + "            throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"'source' cannot be empty.\"));\n"
        + "        }\n"
        + "        String[] segments = source.split(\";\");\n"
        + "        List<DocumentSource> results = new ArrayList<>(segments.length);\n"
        + "        for (String segment : segments) {\n"
        + "            String trimmed = segment.trim();\n"
        + "            if (!trimmed.isEmpty()) {\n"
        + "                results.add(new DocumentSource(trimmed));\n"
        + "            }\n"
        + "        }\n"
        + "        return results.toArray(new DocumentSource[0]);\n"
        + "    }\n"
        + "}\n";

    private static final String AUDIO_VISUAL_SOURCE_CONTENT =
        "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
        + "// Licensed under the MIT License.\n\n"
        + "package com.azure.ai.contentunderstanding.models;\n\n"
        + "import com.azure.core.annotation.Immutable;\n"
        + "import com.azure.core.util.logging.ClientLogger;\n"
        + "import java.time.Duration;\n"
        + "import java.util.ArrayList;\n"
        + "import java.util.List;\n"
        + "import java.util.Objects;\n\n"
        + "/**\n"
        + " * Represents a parsed audio/visual grounding source in the format {@code AV(time[,x,y,w,h])}.\n"
        + " *\n"
        + " * <p>The time is in milliseconds. The bounding box (x, y, width, height) is optional and\n"
        + " * present only when spatial information is available (e.g., face detection).</p>\n"
        + " *\n"
        + " * <p>Face tracklet pairs use the format {@code AV(...)-AV(...)}. When parsing with\n"
        + " * {@link ContentSource#parse(String)} or {@link ContentSource#parseAll(String)},\n"
        + " * tracklet pairs are automatically detected and returned as {@link TrackletSource} instances.</p>\n"
        + " *\n"
        + " * @see ContentSource\n"
        + " * @see TrackletSource\n"
        + " */\n"
        + "@Immutable\n"
        + "public final class AudioVisualSource extends ContentSource {\n"
        + "    private static final ClientLogger LOGGER = new ClientLogger(AudioVisualSource.class);\n"
        + "    private static final String PREFIX = \"AV(\";\n\n"
        + "    private final int timeMs;\n"
        + "    private final Rectangle boundingBox;\n\n"
        + "    AudioVisualSource(String source) {\n"
        + "        super(source);\n"
        + "        if (!source.startsWith(PREFIX) || !source.endsWith(\")\")) {\n"
        + "            throw LOGGER.logExceptionAsError(\n"
        + "                new IllegalArgumentException(\"Audio/visual source must start with '\" + PREFIX + \"' and end with ')': '\" + source + \"'.\"));\n"
        + "        }\n"
        + "        String inner = source.substring(PREFIX.length(), source.length() - 1);\n"
        + "        String[] parts = inner.split(\",\");\n"
        + "        if (parts.length != 1 && parts.length != 5) {\n"
        + "            throw LOGGER.logExceptionAsError(\n"
        + "                new IllegalArgumentException(\"Audio/visual source expected 1 or 5 parameters, got \" + parts.length + \": '\" + source + \"'.\"));\n"
        + "        }\n"
        + "        try {\n"
        + "            this.timeMs = Integer.parseInt(parts[0].trim());\n"
        + "        } catch (NumberFormatException e) {\n"
        + "            throw LOGGER.logExceptionAsError(\n"
        + "                new IllegalArgumentException(\"Invalid time value in audio/visual source: '\" + parts[0] + \"'.\", e));\n"
        + "        }\n"
        + "        if (parts.length == 5) {\n"
        + "            int xVal, yVal, wVal, hVal;\n"
        + "            try { xVal = Integer.parseInt(parts[1].trim()); }\n"
        + "            catch (NumberFormatException e) { throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"Invalid x value: '\" + parts[1] + \"'.\", e)); }\n"
        + "            try { yVal = Integer.parseInt(parts[2].trim()); }\n"
        + "            catch (NumberFormatException e) { throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"Invalid y value: '\" + parts[2] + \"'.\", e)); }\n"
        + "            try { wVal = Integer.parseInt(parts[3].trim()); }\n"
        + "            catch (NumberFormatException e) { throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"Invalid width value: '\" + parts[3] + \"'.\", e)); }\n"
        + "            try { hVal = Integer.parseInt(parts[4].trim()); }\n"
        + "            catch (NumberFormatException e) { throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"Invalid height value: '\" + parts[4] + \"'.\", e)); }\n"
        + "            this.boundingBox = new Rectangle(xVal, yVal, wVal, hVal);\n"
        + "        } else {\n"
        + "            this.boundingBox = null;\n"
        + "        }\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the time in milliseconds.\n"
        + "     *\n"
        + "     * @return The time in milliseconds.\n"
        + "     */\n"
        + "    public int getTimeMs() {\n"
        + "        return timeMs;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the time as a Duration.\n"
        + "     *\n"
        + "     * @return The time as a Duration.\n"
        + "     */\n"
        + "    public Duration getTime() {\n"
        + "        return Duration.ofMillis(timeMs);\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the bounding box in pixel coordinates, or {@code null} if no spatial information\n"
        + "     * is available (e.g., audio-only).\n"
        + "     *\n"
        + "     * @return The bounding box, or {@code null}.\n"
        + "     */\n"
        + "    public Rectangle getBoundingBox() {\n"
        + "        return boundingBox;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Parses a single audio/visual source segment.\n"
        + "     *\n"
        + "     * @param source The source string in the format {@code AV(time[,x,y,w,h])}.\n"
        + "     * @return A new {@link AudioVisualSource}.\n"
        + "     * @throws NullPointerException if {@code source} is null.\n"
        + "     * @throws IllegalArgumentException if the source string is not in the expected format.\n"
        + "     */\n"
        + "    public static AudioVisualSource parse(String source) {\n"
        + "        Objects.requireNonNull(source, \"'source' cannot be null.\");\n"
        + "        if (source.isEmpty()) {\n"
        + "            throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"'source' cannot be empty.\"));\n"
        + "        }\n"
        + "        return new AudioVisualSource(source);\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Parses a source string containing one or more audio/visual source segments separated by {@code ;}.\n"
        + "     *\n"
        + "     * @param source The source string (may contain {@code ;} delimiters).\n"
        + "     * @return An array of {@link AudioVisualSource} instances.\n"
        + "     * @throws NullPointerException if {@code source} is null.\n"
        + "     * @throws IllegalArgumentException if any segment is not in the expected format.\n"
        + "     */\n"
        + "    public static AudioVisualSource[] parseAll(String source) {\n"
        + "        Objects.requireNonNull(source, \"'source' cannot be null.\");\n"
        + "        if (source.isEmpty()) {\n"
        + "            throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"'source' cannot be empty.\"));\n"
        + "        }\n"
        + "        String[] segments = source.split(\";\");\n"
        + "        List<AudioVisualSource> results = new ArrayList<>(segments.length);\n"
        + "        for (String segment : segments) {\n"
        + "            String trimmed = segment.trim();\n"
        + "            if (!trimmed.isEmpty()) {\n"
        + "                results.add(new AudioVisualSource(trimmed));\n"
        + "            }\n"
        + "        }\n"
        + "        return results.toArray(new AudioVisualSource[0]);\n"
        + "    }\n"
        + "}\n";

    private static final String TRACKLET_SOURCE_CONTENT =
        "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
        + "// Licensed under the MIT License.\n\n"
        + "package com.azure.ai.contentunderstanding.models;\n\n"
        + "import com.azure.core.annotation.Immutable;\n"
        + "import com.azure.core.util.logging.ClientLogger;\n\n"
        + "import java.util.Objects;\n\n"
        + "/**\n"
        + " * Represents a face tracklet &mdash; a continuous track of a detected face across consecutive video frames.\n"
        + " * Encoded as {@code AV(startTime,x,y,w,h)-AV(endTime,x,y,w,h)} in the wire format.\n"
        + " *\n"
        + " * <p>A tracklet captures where a face appeared at the start and end of one continuous appearance.\n"
        + " * The {@code -} separator joins the start/end frames within a single tracklet.\n"
        + " * Multiple tracklets for the same person (reappearing after being absent) are separated by {@code ;}\n"
        + " * and returned as separate {@link TrackletSource} elements in the\n"
        + " * {@link ContentField#getGroundingSources()} array.</p>\n"
        + " *\n"
        + " * @see ContentSource\n"
        + " * @see AudioVisualSource\n"
        + " */\n"
        + "@Immutable\n"
        + "public final class TrackletSource extends ContentSource {\n"
        + "    private static final ClientLogger LOGGER = new ClientLogger(TrackletSource.class);\n"
        + "    private static final String SEPARATOR = \")-AV(\";\n\n"
        + "    private final AudioVisualSource start;\n"
        + "    private final AudioVisualSource end;\n\n"
        + "    private TrackletSource(String source) {\n"
        + "        super(source);\n"
        + "        int separatorIndex = source.indexOf(SEPARATOR);\n"
        + "        if (separatorIndex < 0) {\n"
        + "            throw LOGGER.logExceptionAsError(\n"
        + "                new IllegalArgumentException(\"Expected a tracklet pair in the format 'AV(...)-AV(...)': '\" + source + \"'.\"));\n"
        + "        }\n"
        + "        String first = source.substring(0, separatorIndex + 1);\n"
        + "        String second = source.substring(separatorIndex + 2);\n"
        + "        this.start = new AudioVisualSource(first);\n"
        + "        this.end = new AudioVisualSource(second);\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the audio/visual source at the start of the tracklet.\n"
        + "     *\n"
        + "     * @return The start source.\n"
        + "     */\n"
        + "    public AudioVisualSource getStart() {\n"
        + "        return start;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Gets the audio/visual source at the end of the tracklet.\n"
        + "     *\n"
        + "     * @return The end source.\n"
        + "     */\n"
        + "    public AudioVisualSource getEnd() {\n"
        + "        return end;\n"
        + "    }\n\n"
        + "    /**\n"
        + "     * Parses a tracklet pair string.\n"
        + "     *\n"
        + "     * @param source The source string in the format {@code AV(...)-AV(...)}.\n"
        + "     * @return A new {@link TrackletSource}.\n"
        + "     * @throws NullPointerException if {@code source} is null.\n"
        + "     * @throws IllegalArgumentException if the source string is not a valid tracklet pair.\n"
        + "     */\n"
        + "    public static TrackletSource parse(String source) {\n"
        + "        Objects.requireNonNull(source, \"'source' cannot be null.\");\n"
        + "        if (source.isEmpty()) {\n"
        + "            throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"'source' cannot be empty.\"));\n"
        + "        }\n"
        + "        return new TrackletSource(source);\n"
        + "    }\n"
        + "}\n";
}
