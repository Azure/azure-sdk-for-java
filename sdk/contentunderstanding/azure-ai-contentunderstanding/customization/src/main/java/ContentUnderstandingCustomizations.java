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
 * </ul>
 *
 * <p><b>Scenarios and before/after</b></p>
 *
 * <p><b>operationId on status</b> — Scenario: Caller needs the operation ID (e.g. to call
 * getResultFile or deleteResult) after starting analyze.</p>
 * <pre>{@code
 * // Before: generated model had no operationId; caller could not get it from the status.
 * SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult> poller = client.beginAnalyze(...);
 * ContentAnalyzerAnalyzeOperationStatus status = poller.getFinalResult(); // no getOperationId()
 *
 * // After: status carries operationId, set automatically by polling strategy from Operation-Location header.
 * String id = status.getOperationId();
 * client.getResultFile(analyzerId, id, ...);
 * }</pre>
 *
 * <p><b>Content/array/object field extensions</b> — Scenario: Reading document fields
 * (ContentField and subtypes StringField, NumberField, DateField, ObjectField, ArrayField) without
 * casting to each subtype or manually navigating getValueObject()/getValueArray().</p>
 * <p>ContentField.getValue() — get typed value without casting to StringField/NumberField/etc.:</p>
 * <pre>{@code
 * // Before: Cast to subtype and call type-specific getter, then print.
 * ContentField customerNameField = content.getFields().get("CustomerName");
 * String customerName = customerNameField instanceof StringField
 *     ? ((StringField) customerNameField).getValueString() : null;
 * System.out.println("Customer: " + customerName);
 *
 * // After: getValue() returns the typed value; no cast needed for console output.
 * ContentField customerNameField = content.getFields().get("CustomerName");
 * System.out.println("Customer: " + (customerNameField != null ? customerNameField.getValue() : null));
 * }</pre>
 * <p>ObjectField.getFieldOrDefault() — navigate nested object by name:</p>
 * <pre>{@code
 * // Before: Use getValueObject() and map lookup; cast to NumberField for value.
 * ContentField totalField = content.getFields().get("TotalAmount");
 * ObjectField totalObj = (ObjectField) totalField;
 * ContentField amountField = totalObj.getValueObject() != null ? totalObj.getValueObject().get("Amount") : null;
 * Double amount = amountField instanceof NumberField ? ((NumberField) amountField).getValueNumber() : null;
 *
 * // After: getFieldOrDefault(name); then getValue() for the typed value.
 * ContentField totalField = content.getFields().get("TotalAmount");
 * ObjectField totalObj = (ObjectField) totalField;
 * ContentField amountField = totalObj.getFieldOrDefault("Amount");
 * Double amount = amountField != null ? (Double) amountField.getValue() : null;
 * }</pre>
 * <p>ArrayField.size() and get(i) — iterate array elements without getValueArray():</p>
 * <pre>{@code
 * // Before: Call getValueArray() and use List size/get; null-check the list.
 * ContentField lineItemsField = content.getFields().get("LineItems");
 * ArrayField lineItems = (ArrayField) lineItemsField;
 * int count = lineItems.getValueArray() != null ? lineItems.getValueArray().size() : 0;
 * for (int i = 0; i < count; i++) {
 *     ContentField item = lineItems.getValueArray().get(i);
 *     // use item...
 * }
 *
 * // After: size() and get(i) convenience methods; get(i) throws IndexOutOfBoundsException if out of range.
 * ContentField lineItemsField = content.getFields().get("LineItems");
 * ArrayField lineItems = (ArrayField) lineItemsField;
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
 * SyncPoller<..., AnalyzeResult> poller = client.beginAnalyze(analyzerId, inputs);
 * SyncPoller<..., AnalyzeResult> binaryPoller = client.beginAnalyzeBinary(analyzerId, binaryInput);
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
        // Add operationId field to AnalyzeResult model
        customizeAnalyzeResult(customization, logger);

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

        // Add beginAnalyze convenience overloads (no stringEncoding)
        addBeginAnalyzeConvenienceOverloads(customization, logger);
    }

    /**
     * Add operationId field and getter/setter to ContentAnalyzerAnalyzeOperationStatus
     */
    private void customizeAnalyzeResult(LibraryCustomization customization, Logger logger) {
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
                        + "Returns the appropriate typed value for each field type:\n"
                        + "- StringField: returns String (from getValueString())\n"
                        + "- NumberField: returns Double (from getValueNumber())\n"
                        + "- IntegerField: returns Long (from getValueInteger())\n"
                        + "- DateField: returns LocalDate (from getValueDate())\n"
                        + "- TimeField: returns String (from getValueTime())\n"
                        + "- BooleanField: returns Boolean (from isValueBoolean())\n"
                        + "- ObjectField: returns Map (from getValueObject())\n"
                        + "- ArrayField: returns List (from getValueArray())\n"
                        + "- JsonField: returns String (from getValueJson())"))
                        .addBlockTag("return", "the field value, or null if not available."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "if (this instanceof StringField) { return ((StringField) this).getValueString(); }"
                        + "if (this instanceof NumberField) { return ((NumberField) this).getValueNumber(); }"
                        + "if (this instanceof IntegerField) { return ((IntegerField) this).getValueInteger(); }"
                        + "if (this instanceof DateField) { return ((DateField) this).getValueDate(); }"
                        + "if (this instanceof TimeField) { return ((TimeField) this).getValueTime(); }"
                        + "if (this instanceof BooleanField) { return ((BooleanField) this).isValueBoolean(); }"
                        + "if (this instanceof ObjectField) { return ((ObjectField) this).getValueObject(); }"
                        + "if (this instanceof ArrayField) { return ((ArrayField) this).getValueArray(); }"
                        + "if (this instanceof JsonField) { return ((JsonField) this).getValueJson(); }"
                        + "return null; }"));
            }));
    }

    /**
     * Add convenience methods to ArrayField class (equivalent to ArrayField.Extensions.cs)
     */
    private void customizeArrayFieldExtensions(LibraryCustomization customization, Logger logger) {
        logger.info("Adding convenience methods to ArrayField class");

        customization.getClass(MODELS_PACKAGE, "ArrayField").customizeAst(ast -> {
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ast.getClassByName("ArrayField").ifPresent(clazz -> {
                // Add static ClientLogger for throwing through Azure SDK lint (ThrowFromClientLoggerCheck)
                clazz.addFieldWithInitializer("ClientLogger", "LOGGER",
                    StaticJavaParser.parseExpression("new ClientLogger(ArrayField.class)"),
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
     * Add convenience methods to ObjectField class (equivalent to ObjectField.Extensions.cs)
     */
    private void customizeObjectFieldExtensions(LibraryCustomization customization, Logger logger) {
        logger.info("Adding convenience methods to ObjectField class");

        customization.getClass(MODELS_PACKAGE, "ObjectField").customizeAst(ast -> {
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ast.addImport("java.util.NoSuchElementException");
            ast.getClassByName("ObjectField").ifPresent(clazz -> {
                // Add static ClientLogger for throwing through Azure SDK lint (ThrowFromClientLoggerCheck)
                clazz.addFieldWithInitializer("ClientLogger", "LOGGER",
                    StaticJavaParser.parseExpression("new ClientLogger(ObjectField.class)"),
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
            ast.addImport("com.azure.core.util.BinaryData");
            ast.addImport("java.util.Map");

            ast.getClassByName("ContentUnderstandingClient").ifPresent(clazz -> {
                // Add updateDefaults convenience method with Map parameter - returns ContentUnderstandingDefaults directly
                clazz.addMethod("updateDefaults", Modifier.Keyword.PUBLIC)
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
            ast.addImport("com.azure.core.util.BinaryData");
            ast.addImport("java.util.Map");

            ast.getClassByName("ContentUnderstandingAsyncClient").ifPresent(clazz -> {
                // Add updateDefaults convenience method with Map parameter - returns Mono<ContentUnderstandingDefaults>
                clazz.addMethod("updateDefaults", Modifier.Keyword.PUBLIC)
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
     * Add beginAnalyzeBinary convenience overloads without stringEncoding.
     * Adds 2-param and 5-param overloads that default utf16.
     */
    private void addBeginAnalyzeBinaryConvenienceOverloads(LibraryCustomization customization, Logger logger) {
        logger.info("Adding beginAnalyzeBinary convenience overloads (2/5 param)");

        // Sync client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingClient").customizeAst(ast -> {
            ast.getClassByName("ContentUnderstandingClient").ifPresent(clazz -> {
                // 2-param: analyzerId, binaryInput
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
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
                        + "return beginAnalyzeBinary(analyzerId, binaryInput, null, \"application/octet-stream\", null); }"));

                // 5-param: analyzerId, binaryInput, inputRange, contentType, processingLocation
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("BinaryData", "binaryInput")
                    .addParameter("String", "inputRange")
                    .addParameter("String", "contentType")
                    .addParameter("ProcessingLocation", "processingLocation")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from binary input. Uses default string encoding (utf16)."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "binaryInput The binary content of the document to analyze.")
                        .addBlockTag("param", "inputRange Range of the input to analyze (ex. 1-3,5,9-). Document content uses 1-based page numbers; audio visual uses milliseconds.")
                        .addBlockTag("param", "contentType Request content type.")
                        .addBlockTag("param", "processingLocation The location where the data may be processed. Set to null for service default.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "RequestOptions requestOptions = new RequestOptions();"
                        + "if (inputRange != null) { requestOptions.addQueryParam(\"range\", inputRange, false); }"
                        + "if (processingLocation != null) { requestOptions.addQueryParam(\"processingLocation\", processingLocation.toString(), false); }"
                        + "requestOptions.addQueryParam(\"stringEncoding\", \"utf16\", false);"
                        + "return serviceClient.beginAnalyzeBinaryWithModel(analyzerId, contentType, binaryInput, requestOptions); }"));
            });
        });

        // Async client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingAsyncClient").customizeAst(ast -> {
            ast.getClassByName("ContentUnderstandingAsyncClient").ifPresent(clazz -> {
                // 2-param: analyzerId, binaryInput
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
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
                        + "return beginAnalyzeBinary(analyzerId, binaryInput, null, \"application/octet-stream\", null); }"));

                // 5-param: analyzerId, binaryInput, inputRange, contentType, processingLocation
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("BinaryData", "binaryInput")
                    .addParameter("String", "inputRange")
                    .addParameter("String", "contentType")
                    .addParameter("ProcessingLocation", "processingLocation")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from binary input. Uses default string encoding (utf16)."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "binaryInput The binary content of the document to analyze.")
                        .addBlockTag("param", "inputRange Range of the input to analyze (ex. 1-3,5,9-). Document content uses 1-based page numbers; audio visual uses milliseconds.")
                        .addBlockTag("param", "contentType Request content type.")
                        .addBlockTag("param", "processingLocation The location where the data may be processed. Set to null for service default.")
                        .addBlockTag("return", "the {@link PollerFlux} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "RequestOptions requestOptions = new RequestOptions();"
                        + "if (inputRange != null) { requestOptions.addQueryParam(\"range\", inputRange, false); }"
                        + "if (processingLocation != null) { requestOptions.addQueryParam(\"processingLocation\", processingLocation.toString(), false); }"
                        + "requestOptions.addQueryParam(\"stringEncoding\", \"utf16\", false);"
                        + "return serviceClient.beginAnalyzeBinaryWithModelAsync(analyzerId, contentType, binaryInput, requestOptions); }"));
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
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalyzeInput>", "inputs")
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
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalyzeInput>", "inputs")
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
                        + "AnalyzeRequest1 analyzeRequest1Obj = new AnalyzeRequest1().setInputs(inputs).setModelDeployments(modelDeployments);"
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
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalyzeInput>", "inputs")
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
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalyzeInput>", "inputs")
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
                        + "AnalyzeRequest1 analyzeRequest1Obj = new AnalyzeRequest1().setInputs(inputs).setModelDeployments(modelDeployments);"
                        + "BinaryData analyzeRequest1 = BinaryData.fromObject(analyzeRequest1Obj);"
                        + "return serviceClient.beginAnalyzeWithModelAsync(analyzerId, analyzeRequest1, requestOptions); }"));
            });
        });
    }
}
