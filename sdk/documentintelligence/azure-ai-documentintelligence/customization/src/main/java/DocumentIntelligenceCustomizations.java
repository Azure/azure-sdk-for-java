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
 * This class contains the customization code to customize the AutoRest generated code for OpenAI.
 */
public class DocumentIntelligenceCustomizations extends Customization {
    private static final String MODELS_PACKAGE = "com.azure.ai.documentintelligence.models";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeAnalyzeOperation(customization, logger);
        customizePollingStrategy(customization, logger);
        customizePollingUtils(customization, logger);
        customizeMethodImplForOverload(customization, logger);
        customizeAnalyzeDocumentOptions(customization);
        customizeAnalyzeBatchDocumentOptions(customization);
        customizeClassifyDocumentOptions(customization);
        customizeSamplesForOverload(customization, logger);
        addStaticAccessorForOperationId(customization, logger);
    }

    private void addStaticAccessorForOperationId(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing to add static operationnId accessor setter methods");
        customization.getClass(MODELS_PACKAGE, "AnalyzeOperationDetails").customizeAst(ast -> {
            ast.addImport("com.azure.ai.documentintelligence.implementation.AnalyzeOperationDetailsHelper");
            ast.getClassByName("AnalyzeOperationDetails").ifPresent(clazz -> clazz.getMembers()
                .add(0, new InitializerDeclaration(true, StaticJavaParser.parseBlock("{"
                    + "AnalyzeOperationDetailsHelper.setAccessor(new AnalyzeOperationDetailsHelper.AnalyzeOperationDetailsAccessor() {"
                    + "    @Override"
                    + "    public void setOperationId(AnalyzeOperationDetails analyzeOperation, String operationId) {"
                    + "        analyzeOperation.setOperationId(operationId);"
                    + "    }"
                    + "}); }"))));
        });
    }

    private void customizeSamplesForOverload(LibraryCustomization customization, Logger logger) {
        logger.info("Removing samples using old overloads");
        customization.getRawEditor().removeFile("src/samples/java/com/azure/ai/documentintelligence/generated/AnalyzeBatchDocuments.java");
        customization.getRawEditor().removeFile("src/samples/java/com/azure/ai/documentintelligence/generated/AnalyzeDocumentFromBase64.java");
        customization.getRawEditor().removeFile("src/samples/java/com/azure/ai/documentintelligence/generated/AnalyzeDocumentFromUrl.java");
        customization.getRawEditor().removeFile("src/samples/java/com/azure/ai/documentintelligence/generated/ClassifyDocumentFromUrl.java");
    }

    private void customizeMethodImplForOverload(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing to call internal method implementation");
        customizeImpl(customization.getClass("com.azure.ai.documentintelligence", "DocumentIntelligenceClient"));
        customizeImpl(customization.getClass("com.azure.ai.documentintelligence", "DocumentIntelligenceAsyncClient"));
    }

    private static void customizeImpl(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            clazz.getMethodsByName("beginAnalyzeDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("analyzeRequest").ifPresent(param -> param.setName("analyzeDocumentOptions"));
                    methodDeclaration.setBody(StaticJavaParser.parseBlock("{ Objects.requireNonNull(analyzeDocumentOptions, \"'analyzeDocumentOptions' cannot be null.\");"
                            + "return this.beginAnalyzeDocument(modelId, analyzeDocumentOptions, analyzeDocumentOptions.getPages() != null ?"
                            + "CoreUtils.stringJoin(\",\", analyzeDocumentOptions.getPages()) : null, analyzeDocumentOptions.getLocale(), "
                            + "analyzeDocumentOptions.getStringIndexType(), analyzeDocumentOptions.getDocumentAnalysisFeatures(), "
                            + "analyzeDocumentOptions.getQueryFields(), analyzeDocumentOptions.getOutputContentFormat(), "
                            + "analyzeDocumentOptions.getOutput()); }"))
                        .setJavadocComment(new Javadoc(JavadocDescription.parseText("Analyzes document with document model."))
                            .addBlockTag("param", "modelId", "Unique document model name.")
                            .addBlockTag("param", "analyzeDocumentOptions", "Analyze request parameters.")
                            .addBlockTag("throws", "IllegalArgumentException", "thrown if parameters fail the validation.")
                            .addBlockTag("throws", "HttpResponseException", "thrown if the request is rejected by server.")
                            .addBlockTag("throws", "ClientAuthenticationException", "thrown if the request is rejected by server on status code 401.")
                            .addBlockTag("throws", "ResourceNotFoundException", "thrown if the request is rejected by server on status code 404.")
                            .addBlockTag("throws", "ResourceModifiedException", "thrown if the request is rejected by server on status code 409.")
                            .addBlockTag("throws", "RuntimeException", "all other wrapped checked exceptions if the request fails to be sent.")
                            .addBlockTag("return", "the {@link SyncPoller} for polling of long-running operation."));
                }

                if (methodDeclaration.getParameterByName("outputContentFormat").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });

            clazz.getMethodsByName("beginAnalyzeBatchDocuments").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("analyzeBatchRequest").ifPresent(param -> param.setName("analyzeBatchDocumentOptions"));
                    methodDeclaration.setBody(StaticJavaParser.parseBlock("{ Objects.requireNonNull(analyzeBatchDocumentOptions, \"'analyzeBatchDocumentOptions' cannot be null.\");"
                        + "return this.beginAnalyzeBatchDocuments(modelId, analyzeBatchDocumentOptions, analyzeBatchDocumentOptions.getPages() != null ?"
                        + " CoreUtils.stringJoin(\",\", analyzeBatchDocumentOptions.getPages()) : null, analyzeBatchDocumentOptions.getLocale(), "
                        + "analyzeBatchDocumentOptions.getStringIndexType(), analyzeBatchDocumentOptions.getDocumentAnalysisFeatures(), "
                        + "analyzeBatchDocumentOptions.getQueryFields(), analyzeBatchDocumentOptions.getOutputContentFormat(), "
                        + "analyzeBatchDocumentOptions.getOutput()); }"))
                        .setJavadocComment(new Javadoc(JavadocDescription.parseText("Analyzes batch documents with document model."))
                            .addBlockTag("param", "modelId", "Unique document model name.")
                            .addBlockTag("param", "analyzeBatchDocumentOptions", "Analyze batch request parameters.")
                            .addBlockTag("throws", "IllegalArgumentException", "thrown if parameters fail the validation.")
                            .addBlockTag("throws", "HttpResponseException", "thrown if the request is rejected by server.")
                            .addBlockTag("throws", "ClientAuthenticationException", "thrown if the request is rejected by server on status code 401.")
                            .addBlockTag("throws", "ResourceNotFoundException", "thrown if the request is rejected by server on status code 404.")
                            .addBlockTag("throws", "ResourceModifiedException", "thrown if the request is rejected by server on status code 409.")
                            .addBlockTag("throws", "RuntimeException", "all other wrapped checked exceptions if the request fails to be sent.")
                            .addBlockTag("return", "the {@link SyncPoller} for polling of long-running operation."));

                }

                if (methodDeclaration.getParameterByName("outputContentFormat").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });

            clazz.getMethodsByName("beginClassifyDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("classifyRequest").ifPresent(param -> param.setName("classifyDocumentOptions"));
                    methodDeclaration.setBody(StaticJavaParser.parseBlock("{ Objects.requireNonNull(classifyDocumentOptions, \"'classifyDocumentOptions' cannot be null.\");"
                        + "return this.beginClassifyDocument(classifierId, classifyDocumentOptions, classifyDocumentOptions.getStringIndexType(), "
                        + "classifyDocumentOptions.getSplit(), classifyDocumentOptions.getPages() != null ? CoreUtils.stringJoin(\",\", classifyDocumentOptions.getPages()) : null); }"))
                        .setJavadocComment(new Javadoc(JavadocDescription.parseText("Classifies document with document classifier."))
                            .addBlockTag("param", "classifierId", "Unique document classifier name.")
                            .addBlockTag("param", "classifyDocumentOptions", "Classify request parameters.")
                            .addBlockTag("throws", "IllegalArgumentException", "thrown if parameters fail the validation.")
                            .addBlockTag("throws", "HttpResponseException", "thrown if the request is rejected by server.")
                            .addBlockTag("throws", "ClientAuthenticationException", "thrown if the request is rejected by server on status code 401.")
                            .addBlockTag("throws", "ResourceNotFoundException", "thrown if the request is rejected by server on status code 404.")
                            .addBlockTag("throws", "ResourceModifiedException", "thrown if the request is rejected by server on status code 409.")
                            .addBlockTag("throws", "RuntimeException", "all other wrapped checked exceptions if the request fails to be sent.")
                            .addBlockTag("return", "the {@link SyncPoller} for polling of long-running operation."));
                }

                if (methodDeclaration.getParameterByName("split").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });
        }));
    }

    private void customizeAnalyzeDocumentOptions(LibraryCustomization customization) {
        customization.getClass(MODELS_PACKAGE, "AnalyzeDocumentOptions").customizeAst(ast ->
            ast.getClassByName("AnalyzeDocumentOptions").ifPresent(clazz -> {
                clazz.getDefaultConstructor().ifPresent(NodeWithModifiers::setModifiers);
                clazz.getMethodsByName("setUrlSource").forEach(NodeWithModifiers::setModifiers);
                clazz.getMethodsByName("setBytesSource").forEach(NodeWithModifiers::setModifiers);
            }));
    }

    private void customizeAnalyzeBatchDocumentOptions(LibraryCustomization customization) {
        customization.getClass(MODELS_PACKAGE, "AnalyzeBatchDocumentsOptions").customizeAst(ast ->
            ast.getClassByName("AnalyzeBatchDocumentsOptions").ifPresent(clazz -> {
                clazz.getConstructorByParameterTypes(String.class).ifPresent(NodeWithModifiers::setModifiers);
                clazz.getMethodsByName("setAzureBlobSource").forEach(NodeWithModifiers::setModifiers);
                clazz.getMethodsByName("setAzureBlobFileListSource").forEach(NodeWithModifiers::setModifiers);
            }));
    }

    private void customizeClassifyDocumentOptions(LibraryCustomization customization) {
        customization.getClass(MODELS_PACKAGE, "ClassifyDocumentOptions").customizeAst(ast ->
            ast.getClassByName("ClassifyDocumentOptions").ifPresent(clazz -> {
                clazz.getDefaultConstructor().ifPresent(NodeWithModifiers::setModifiers);
                clazz.getMethodsByName("setUrlSource").forEach(NodeWithModifiers::setModifiers);
                clazz.getMethodsByName("setBytesSource").forEach(NodeWithModifiers::setModifiers);
            }));
    }

    private void customizeAnalyzeOperation(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the AnalyzeOperationDetails class");
        customization.getClass("com.azure.ai.documentintelligence.models", "AnalyzeOperationDetails")
            .customizeAst(ast -> ast.getClassByName("AnalyzeOperationDetails").ifPresent(clazz -> {
                clazz.getAnnotationByName("Immutable").ifPresent(Node::remove);

                clazz.addField("String", "operationId", Modifier.Keyword.PRIVATE);

                clazz.addMethod("getResultId", Modifier.Keyword.PUBLIC)
                    .setType("String")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Gets the operationId property: Operation ID."))
                        .addBlockTag("return", "the operationId value."))
                    .setBody(StaticJavaParser.parseBlock("{ return operationId; }"));

                clazz.addMethod("setOperationId", Modifier.Keyword.PRIVATE)
                    .setType("void")
                    .addParameter("String", "operationId")
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Sets the operationId property: Operation ID."))
                        .addBlockTag("param", "operationId the operationId value to set."))
                    .setBody(StaticJavaParser.parseBlock("{ this.operationId = operationId; }"));
            }));
    }

    private void customizePollingUtils(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the PollingUtils class");
        customization.getClass("com.azure.ai.documentintelligence.implementation", "PollingUtils").customizeAst(ast -> {
            ast.addImport("java.util.regex.Matcher");
            ast.addImport("java.util.regex.Pattern");
            ast.getClassByName("PollingUtils").ifPresent(clazz -> {
                clazz.addFieldWithInitializer("Pattern", "PATTERN",
                    StaticJavaParser.parseExpression("Pattern.compile(\"[^:]+://[^/]+/documentintelligence/.+/([^?/]+)\")"),
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

                clazz.addMethod("parseOperationId", Modifier.Keyword.PRIVATE)
                    .setType("String")
                    .setModifiers(Modifier.Keyword.STATIC)
                    .addParameter("String", "operationLocationHeader")
                    .setBody(StaticJavaParser.parseBlock("{ "
                        + "if (CoreUtils.isNullOrEmpty(operationLocationHeader)) { return null; }"
                        + "Matcher matcher = PATTERN.matcher(operationLocationHeader);"
                        + "if (matcher.find() && matcher.group(1) != null) { return matcher.group(1); }"
                        + "return null; }"));
            });
        });
    }

    private void customizePollingStrategy(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the SyncOperationLocationPollingStrategy class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.implementation");
        packageCustomization.getClass("SyncOperationLocationPollingStrategy").customizeAst(ast ->
            ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeOperationDetails")
                .getClassByName("SyncOperationLocationPollingStrategy").ifPresent(this::addSyncPollOverrideMethod));

        logger.info("Customizing the OperationLocationPollingStrategy class");
        packageCustomization.getClass("OperationLocationPollingStrategy").customizeAst(ast ->
            ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeOperationDetails")
                .getClassByName("OperationLocationPollingStrategy").ifPresent(this::addAsyncPollOverrideMethod));
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
                + "    if (pollResponse.getValue() instanceof AnalyzeOperationDetails) {"
                + "        AnalyzeOperationDetails operation = (AnalyzeOperationDetails) pollResponse.getValue();"
                + "        AnalyzeOperationDetailsHelper.setOperationId(operation, operationId);"
                + "    }"
                + "    return pollResponse;"
                + "}); }"));
    }

    private void addSyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("PollResponse<T>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addMarkerAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock("{ PollResponse<T> pollResponse = super.poll(pollingContext, pollResponseType);"
                + "String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));"
                + "String operationId = null;"
                + "if (operationLocationHeader != null) {"
                + "    operationId = PollingUtils.parseOperationId(operationLocationHeader);"
                + "}"
                + "if (pollResponse.getValue() instanceof AnalyzeOperationDetails) {"
                + "    AnalyzeOperationDetails operation = (AnalyzeOperationDetails) pollResponse.getValue();"
                + "    AnalyzeOperationDetailsHelper.setOperationId(operation, operationId);"
                + "}"
                + "return pollResponse; }"));
    }
}
