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
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

/**
 * Customization class for Content Understanding SDK.
 * This class contains customization code to modify the AutoRest/TypeSpec generated code.
 */
public class ContentUnderstandingCustomizations extends Customization {

    private static final String PACKAGE_NAME = "com.azure.ai.contentunderstanding";
    private static final String MODELS_PACKAGE = "com.azure.ai.contentunderstanding.models";
    private static final String IMPLEMENTATION_PACKAGE = "com.azure.ai.contentunderstanding.implementation";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // 1. Add operationId field to AnalyzeResult model
        customizeAnalyzeResult(customization, logger);

        // 2. Customize PollingUtils to add parseOperationId method
        customizePollingUtils(customization, logger);

        // 3. Customize PollingStrategy to extract and set operationId
        customizePollingStrategy(customization, logger);

        // 4. Add simplified beginAnalyze methods (hide stringEncoding, keep processingLocation and modelDeployments)
        addSimplifiedAnalyzeMethods(customization, logger);

        // 5. Add static accessor helper for operationId
        addStaticAccessorForOperationId(customization, logger);

        // 6. Add convenience methods to model classes (equivalent to *.Extensions.cs)
        customizeContentFieldExtensions(customization, logger);
        customizeArrayFieldExtensions(customization, logger);
        customizeObjectFieldExtensions(customization, logger);

        // 7. SERVICE-FIX: Add keyFrameTimesMs case-insensitive deserialization
        customizeAudioVisualContentDeserialization(customization, logger);

        // 8. Add simplified beginAnalyzeBinary overload with default contentType
        addSimplifiedAnalyzeBinaryMethods(customization, logger);

        // 9. SERVICE-FIX: Fix SupportedModels to use List<String> instead of Map<String, String>
        // The service returns arrays for completion/embedding, not maps
        customizeSupportedModels(customization, logger);

        // 10. SERVICE-FIX: Fix copyAnalyzer API path and expected responses
        // The TypeSpec spec incorrectly uses ":copyAnalyzer" but the service uses ":copy"
        // Also, the service returns 200/201/202 not just 202
        customizeCopyAnalyzerApi(customization, logger);

        // 11. Make ContentUnderstandingDefaults constructor public for convenience methods
        customizeContentUnderstandingDefaults(customization, logger);

        // 12. Add updateDefaults convenience methods (TypeSpec disabled these, but auto-generates updateAnalyzer)
        addUpdateDefaultsConvenienceMethods(customization, logger);
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
     * Customize client methods to:
     * 1. Hide methods with stringEncoding parameter (make them package-private)
     * 2. Add simplified overloads that use "utf16" as default
     */
    /**
     * Add simplified beginAnalyze methods that hide the stringEncoding parameter.
     * This matches .NET's approach of hiding stringEncoding while keeping processingLocation and modelDeployments.
     */
    private void addSimplifiedAnalyzeMethods(LibraryCustomization customization, Logger logger) {
        logger.info("Adding simplified beginAnalyze methods without stringEncoding parameter");

        // Add to sync client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.AnalyzeInput");
            ast.addImport("com.azure.ai.contentunderstanding.models.ProcessingLocation");
            ast.addImport("java.util.List");
            ast.addImport("java.util.Map");
            ast.addImport("com.azure.core.annotation.ServiceMethod");
            ast.addImport("com.azure.core.annotation.ReturnType");
            ast.getClassByName("ContentUnderstandingClient").ifPresent(clazz -> {
                // Add overload with all optional parameters (matches .NET parameter order)
                clazz.addMethod("beginAnalyze", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalyzeInput>", "inputs")
                    .addParameter("Map<String, String>", "modelDeployments")
                    .addParameter("ProcessingLocation", "processingLocation")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from inputs. "
                        + "This is a convenience method that uses default string encoding (utf16)."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "inputs The inputs to analyze.")
                        .addBlockTag("param", "modelDeployments Custom model deployment mappings. Set to null to use service defaults.")
                        .addBlockTag("param", "processingLocation The processing location for the analysis. Set to null to use the service default.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyze(analyzerId, \"utf16\", processingLocation, inputs, modelDeployments); }"));

                // Add simplified overload with only analyzerId and inputs (most common usage)
                clazz.addMethod("beginAnalyze", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalyzeInput>", "inputs")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from inputs. "
                        + "This is a convenience method that uses default string encoding (utf16), "
                        + "service default model deployments, and global processing location."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "inputs The inputs to analyze.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyze(analyzerId, inputs, null, null); }"));
            });
        });

        // Add to async client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingAsyncClient").customizeAst(ast -> {
            ast.addImport("com.azure.ai.contentunderstanding.models.AnalyzeInput");
            ast.addImport("com.azure.ai.contentunderstanding.models.ProcessingLocation");
            ast.addImport("java.util.List");
            ast.addImport("java.util.Map");
            ast.addImport("com.azure.core.annotation.ServiceMethod");
            ast.addImport("com.azure.core.annotation.ReturnType");
            ast.getClassByName("ContentUnderstandingAsyncClient").ifPresent(clazz -> {
                // Add overload with all optional parameters (matches .NET parameter order)
                clazz.addMethod("beginAnalyze", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalyzeInput>", "inputs")
                    .addParameter("Map<String, String>", "modelDeployments")
                    .addParameter("ProcessingLocation", "processingLocation")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from inputs. "
                        + "This is a convenience method that uses default string encoding (utf16)."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "inputs The inputs to analyze.")
                        .addBlockTag("param", "modelDeployments Custom model deployment mappings. Set to null to use service defaults.")
                        .addBlockTag("param", "processingLocation The processing location for the analysis. Set to null to use the service default.")
                        .addBlockTag("return", "the {@link PollerFlux} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyze(analyzerId, \"utf16\", processingLocation, inputs, modelDeployments); }"));

                // Add simplified overload with only analyzerId and inputs (most common usage)
                clazz.addMethod("beginAnalyze", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("List<AnalyzeInput>", "inputs")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from inputs. "
                        + "This is a convenience method that uses default string encoding (utf16), "
                        + "service default model deployments, and global processing location."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "inputs The inputs to analyze.")
                        .addBlockTag("return", "the {@link PollerFlux} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyze(analyzerId, inputs, null, null); }"));
            });
        });
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

        customization.getClass(MODELS_PACKAGE, "ArrayField").customizeAst(ast ->
            ast.getClassByName("ArrayField").ifPresent(clazz -> {
                // Add size() method - equivalent to Count property in C#
                clazz.addMethod("size", Modifier.Keyword.PUBLIC)
                    .setType("int")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets the number of items in the array."))
                        .addBlockTag("return", "the number of items in the array, or 0 if the array is null."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return getValueArray() != null ? getValueArray().size() : 0; }"));

                // Add get(int index) method - equivalent to indexer in C#
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
                        + "    throw new IndexOutOfBoundsException(\"Index \" + index + \" is out of range. Array has \" + size() + \" elements.\");"
                        + "}"
                        + "return getValueArray().get(index); }"));
            }));
    }

    /**
     * Add convenience methods to ObjectField class (equivalent to ObjectField.Extensions.cs)
     */
    private void customizeObjectFieldExtensions(LibraryCustomization customization, Logger logger) {
        logger.info("Adding convenience methods to ObjectField class");

        customization.getClass(MODELS_PACKAGE, "ObjectField").customizeAst(ast ->
            ast.getClassByName("ObjectField").ifPresent(clazz -> {
                // Add getField(String fieldName) method - equivalent to indexer in C#
                clazz.addMethod("getField", Modifier.Keyword.PUBLIC)
                    .setType("ContentField")
                    .addParameter("String", "fieldName")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Gets a field from the object by name."))
                        .addBlockTag("param", "fieldName The name of the field to retrieve.")
                        .addBlockTag("return", "The field if found.")
                        .addBlockTag("throws", "IllegalArgumentException if fieldName is null or empty.")
                        .addBlockTag("throws", "java.util.NoSuchElementException if the field is not found."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "if (fieldName == null || fieldName.isEmpty()) {"
                        + "    throw new IllegalArgumentException(\"fieldName cannot be null or empty.\");"
                        + "}"
                        + "if (getValueObject() != null && getValueObject().containsKey(fieldName)) {"
                        + "    return getValueObject().get(fieldName);"
                        + "}"
                        + "throw new java.util.NoSuchElementException(\"Field '\" + fieldName + \"' was not found in the object.\"); }"));

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
            }));
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
     * Add simplified beginAnalyzeBinary methods that don't require contentType parameter.
     * When contentType is not specified, defaults to "application/octet-stream".
     */
    private void addSimplifiedAnalyzeBinaryMethods(LibraryCustomization customization, Logger logger) {
        logger.info("Adding simplified beginAnalyzeBinary methods with default contentType");

        // Add to sync client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingClient").customizeAst(ast -> {
            ast.addImport("com.azure.core.annotation.ServiceMethod");
            ast.addImport("com.azure.core.annotation.ReturnType");
            ast.getClassByName("ContentUnderstandingClient").ifPresent(clazz -> {
                // Add simplified overload without contentType - delegates to version with default
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("BinaryData", "binaryInput")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from binary input using default content type (application/octet-stream)."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "binaryInput The binary content of the document to analyze.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyzeBinary(analyzerId, \"application/octet-stream\", binaryInput); }"));
            });
        });

        // Add to async client
        customization.getClass(PACKAGE_NAME, "ContentUnderstandingAsyncClient").customizeAst(ast -> {
            ast.addImport("com.azure.core.annotation.ServiceMethod");
            ast.addImport("com.azure.core.annotation.ReturnType");
            ast.getClassByName("ContentUnderstandingAsyncClient").ifPresent(clazz -> {
                // Add simplified overload without contentType - delegates to version with default
                clazz.addMethod("beginAnalyzeBinary", Modifier.Keyword.PUBLIC)
                    .setType("PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult>")
                    .addParameter("String", "analyzerId")
                    .addParameter("BinaryData", "binaryInput")
                    .addAnnotation(StaticJavaParser.parseAnnotation("@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Extract content and fields from binary input using default content type (application/octet-stream)."))
                        .addBlockTag("param", "analyzerId The unique identifier of the analyzer.")
                        .addBlockTag("param", "binaryInput The binary content of the document to analyze.")
                        .addBlockTag("return", "the {@link PollerFlux} for polling of the analyze operation.")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server."))
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "return beginAnalyzeBinary(analyzerId, \"application/octet-stream\", binaryInput); }"));
            });
        });
    }

    /**
     * SERVICE-FIX: Fix SupportedModels to use List<String> instead of Map<String, String>.
     * The service returns arrays for completion/embedding fields, not maps.
     * This fixes the deserialization error: "Unexpected token to begin map deserialization: START_ARRAY"
     */
    private void customizeSupportedModels(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing SupportedModels to use List<String> instead of Map<String, String>");

        customization.getClass(MODELS_PACKAGE, "SupportedModels").customizeAst(ast -> {
            ast.addImport("java.util.List");
            ast.addImport("java.util.ArrayList");

            ast.getClassByName("SupportedModels").ifPresent(clazz -> {
                // Change completion field from Map<String, String> to List<String>
                clazz.getFieldByName("completion").ifPresent(field -> {
                    field.getVariable(0).setType("List<String>");
                });

                // Change embedding field from Map<String, String> to List<String>
                clazz.getFieldByName("embedding").ifPresent(field -> {
                    field.getVariable(0).setType("List<String>");
                });

                // Update getCompletion return type
                clazz.getMethodsByName("getCompletion").forEach(method -> {
                    method.setType("List<String>");
                });

                // Update getEmbedding return type
                clazz.getMethodsByName("getEmbedding").forEach(method -> {
                    method.setType("List<String>");
                });

                // Update constructor parameter types
                clazz.getConstructors().forEach(constructor -> {
                    constructor.getParameters().forEach(param -> {
                        String paramName = param.getNameAsString();
                        if ("completion".equals(paramName) || "embedding".equals(paramName)) {
                            param.setType("List<String>");
                        }
                    });
                });

                // Update toJson method - change writeMapField to writeArrayField
                clazz.getMethodsByName("toJson").forEach(method -> {
                    method.getBody().ifPresent(body -> {
                        String bodyStr = body.toString();
                        // Replace writeMapField with writeArrayField for completion and embedding
                        bodyStr = bodyStr.replace(
                            "jsonWriter.writeMapField(\"completion\", this.completion, (writer, element) -> writer.writeString(element))",
                            "jsonWriter.writeArrayField(\"completion\", this.completion, (writer, element) -> writer.writeString(element))");
                        bodyStr = bodyStr.replace(
                            "jsonWriter.writeMapField(\"embedding\", this.embedding, (writer, element) -> writer.writeString(element))",
                            "jsonWriter.writeArrayField(\"embedding\", this.embedding, (writer, element) -> writer.writeString(element))");
                        method.setBody(StaticJavaParser.parseBlock(bodyStr));
                    });
                });

                // Update fromJson method - change readMap to readArray
                clazz.getMethodsByName("fromJson").forEach(method -> {
                    method.getBody().ifPresent(body -> {
                        String bodyStr = body.toString();
                        // Replace Map<String, String> with List<String>
                        bodyStr = bodyStr.replace("Map<String, String> completion = null;", "List<String> completion = null;");
                        bodyStr = bodyStr.replace("Map<String, String> embedding = null;", "List<String> embedding = null;");
                        // Replace readMap with readArray
                        bodyStr = bodyStr.replace(
                            "completion = reader.readMap(reader1 -> reader1.getString());",
                            "completion = reader.readArray(reader1 -> reader1.getString());");
                        bodyStr = bodyStr.replace(
                            "embedding = reader.readMap(reader1 -> reader1.getString());",
                            "embedding = reader.readArray(reader1 -> reader1.getString());");
                        method.setBody(StaticJavaParser.parseBlock(bodyStr));
                    });
                });
            });
        });
    }

    /**
     * SERVICE-FIX: Fix the copyAnalyzer API path and expected responses.
     *
     * The TypeSpec/Swagger spec incorrectly uses ":copyAnalyzer" as the action,
     * but the actual service endpoint uses ":copy". Additionally, the spec only
     * expects 202 response, but the service can return 200, 201, or 202.
     *
     * This customization modifies the ContentUnderstandingService interface annotations
     * to match the actual service behavior.
     */
    private void customizeCopyAnalyzerApi(LibraryCustomization customization, Logger logger) {
        logger.info("SERVICE-FIX: Customizing copyAnalyzer API path and expected responses");

        customization.getClass(IMPLEMENTATION_PACKAGE, "ContentUnderstandingClientImpl").customizeAst(ast -> {
            ast.addImport("com.azure.core.exception.ResourceNotFoundException");

            // Find the ContentUnderstandingService interface inside ContentUnderstandingClientImpl
            ast.getClassByName("ContentUnderstandingClientImpl").ifPresent(implClass -> {
                implClass.getMembers().stream()
                    .filter(member -> member instanceof ClassOrInterfaceDeclaration)
                    .map(member -> (ClassOrInterfaceDeclaration) member)
                    .filter(innerClass -> innerClass.getNameAsString().equals("ContentUnderstandingService"))
                    .findFirst()
                    .ifPresent(serviceInterface -> {
                        // Find and update copyAnalyzer method
                        serviceInterface.getMethodsByName("copyAnalyzer").forEach(method -> {
                            // Update @Post annotation from ":copyAnalyzer" to ":copy"
                            method.getAnnotationByName("Post").ifPresent(postAnnotation -> {
                                postAnnotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                                    if (pair.getValue().toString().contains(":copyAnalyzer")) {
                                        pair.setValue(StaticJavaParser.parseExpression(
                                            "\"/analyzers/{analyzerId}:copy\""));
                                        logger.info("Updated @Post path for copyAnalyzer async method");
                                    }
                                });
                                // Handle single value annotation
                                if (postAnnotation.isSingleMemberAnnotationExpr()) {
                                    String value = postAnnotation.asSingleMemberAnnotationExpr()
                                        .getMemberValue().toString();
                                    if (value.contains(":copyAnalyzer")) {
                                        postAnnotation.asSingleMemberAnnotationExpr().setMemberValue(
                                            StaticJavaParser.parseExpression("\"/analyzers/{analyzerId}:copy\""));
                                        logger.info("Updated @Post path for copyAnalyzer async method (single value)");
                                    }
                                }
                            });

                            // Update @ExpectedResponses from { 202 } to { 200, 201, 202 }
                            method.getAnnotationByName("ExpectedResponses").ifPresent(expectedAnnotation -> {
                                if (expectedAnnotation.isSingleMemberAnnotationExpr()) {
                                    expectedAnnotation.asSingleMemberAnnotationExpr().setMemberValue(
                                        StaticJavaParser.parseExpression("{ 200, 201, 202 }"));
                                    logger.info("Updated @ExpectedResponses for copyAnalyzer async method");
                                }
                            });

                            // Add @UnexpectedResponseExceptionType for 404 if not present
                            boolean has404Handler = method.getAnnotations().stream()
                                .filter(a -> a.getNameAsString().equals("UnexpectedResponseExceptionType"))
                                .anyMatch(a -> a.toString().contains("404"));

                            if (!has404Handler) {
                                // Add 404 handler annotation
                                method.addAnnotation(StaticJavaParser.parseAnnotation(
                                    "@UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })"));
                                logger.info("Added 404 exception handler for copyAnalyzer async method");
                            }
                        });

                        // Find and update copyAnalyzerSync method
                        serviceInterface.getMethodsByName("copyAnalyzerSync").forEach(method -> {
                            // Update @Post annotation from ":copyAnalyzer" to ":copy"
                            method.getAnnotationByName("Post").ifPresent(postAnnotation -> {
                                postAnnotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                                    if (pair.getValue().toString().contains(":copyAnalyzer")) {
                                        pair.setValue(StaticJavaParser.parseExpression(
                                            "\"/analyzers/{analyzerId}:copy\""));
                                        logger.info("Updated @Post path for copyAnalyzerSync method");
                                    }
                                });
                                // Handle single value annotation
                                if (postAnnotation.isSingleMemberAnnotationExpr()) {
                                    String value = postAnnotation.asSingleMemberAnnotationExpr()
                                        .getMemberValue().toString();
                                    if (value.contains(":copyAnalyzer")) {
                                        postAnnotation.asSingleMemberAnnotationExpr().setMemberValue(
                                            StaticJavaParser.parseExpression("\"/analyzers/{analyzerId}:copy\""));
                                        logger.info("Updated @Post path for copyAnalyzerSync method (single value)");
                                    }
                                }
                            });

                            // Update @ExpectedResponses from { 202 } to { 200, 201, 202 }
                            method.getAnnotationByName("ExpectedResponses").ifPresent(expectedAnnotation -> {
                                if (expectedAnnotation.isSingleMemberAnnotationExpr()) {
                                    expectedAnnotation.asSingleMemberAnnotationExpr().setMemberValue(
                                        StaticJavaParser.parseExpression("{ 200, 201, 202 }"));
                                    logger.info("Updated @ExpectedResponses for copyAnalyzerSync method");
                                }
                            });

                            // Add @UnexpectedResponseExceptionType for 404 if not present
                            boolean has404Handler = method.getAnnotations().stream()
                                .filter(a -> a.getNameAsString().equals("UnexpectedResponseExceptionType"))
                                .anyMatch(a -> a.toString().contains("404"));

                            if (!has404Handler) {
                                // Add 404 handler annotation
                                method.addAnnotation(StaticJavaParser.parseAnnotation(
                                    "@UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })"));
                                logger.info("Added 404 exception handler for copyAnalyzerSync method");
                            }
                        });
                    });
            });
        });
    }

    // =================== Update Convenience Methods ===================

    /**
     * Make ContentUnderstandingDefaults constructor public to allow creating instances
     * for the updateDefaults convenience method.
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
     * Add convenience methods for updateDefaults that accept typed objects
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
}
