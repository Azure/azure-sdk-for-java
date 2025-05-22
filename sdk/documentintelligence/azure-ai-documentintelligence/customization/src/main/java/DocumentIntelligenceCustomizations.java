import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import org.slf4j.Logger;

import java.util.List;

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
        customizeModifierForOverloadMethods(customization, logger);
        customizeMethodImplForOverload(customization, logger);
        customizeAnalyzeDocumentOptions(customization);
        customizeAnalyzeBatchDocumentOptions(customization);
        customizeClassifyDocumentOptions(customization);
        customizeSamplesForOverload(customization, logger);
        addStaticAccessorForOperationId(customization, logger);
    }

    private void addStaticAccessorForOperationId(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing to add static operationnId accessor setter methods");
        customization.getPackage(MODELS_PACKAGE)
            .getClass("AnalyzeOperationDetails")
            .customizeAst(ast -> {
                ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeOperationDetails.AnalyzeOperationDetailsHelper");
                ast.getClassByName("AnalyzeOperationDetails").ifPresent(clazz -> clazz.addConstructor().setStatic(true)
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "AnalyzeOperationDetailsHelper.setAccessor(new AnalyzeOperationDetailsHelper.AnalyzeOperationDetailsAccessor() {"
                        + "  @Override"
                        + "  public void setOperationId(AnalyzeOperationDetails analyzeOperation, String operationId) {"
                        + "    analyzeOperation.setOperationId(operationId);"
                        + "  }"
                        + "});"
                        + "}"
                    )));
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
        PackageCustomization models = customization.getPackage("com.azure.ai.documentintelligence");
        ClassCustomization classCustomization = models.getClass("DocumentIntelligenceClient");
        customizeImpl(classCustomization, "DocumentIntelligenceClient");
        ClassCustomization asynClassCustomization = models.getClass("DocumentIntelligenceAsyncClient");
        customizeImpl(asynClassCustomization, "DocumentIntelligenceAsyncClient");
    }

    private static void customizeImpl(ClassCustomization classCustomization, String className) {
        classCustomization.customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
            clazz.getMethodsByName("beginAnalyzeDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("analyzeRequest")
                        .ifPresent(param -> param.setName("analyzeDocumentOptions"));
                    methodDeclaration.setBody(StaticJavaParser.parseBlock(
                        "{ Objects.requireNonNull(analyzeDocumentOptions, \"'analyzeDocumentOptions' cannot be null.\");"
                            + "return this.beginAnalyzeDocument(modelId, analyzeDocumentOptions, analyzeDocumentOptions.getPages() != null ? CoreUtils.stringJoin(\",\", analyzeDocumentOptions.getPages()) : null, analyzeDocumentOptions.getLocale(), analyzeDocumentOptions.getStringIndexType(), analyzeDocumentOptions.getDocumentAnalysisFeatures(), analyzeDocumentOptions.getQueryFields(), analyzeDocumentOptions.getOutputContentFormat(), analyzeDocumentOptions.getOutput()); }"));
                    methodDeclaration.setJavadocComment(
                        new Javadoc(JavadocDescription.parseText("Analyzes document with document model.")).addBlockTag(
                                "param", "modelId", "Unique document model name.")
                            .addBlockTag("param", "analyzeDocumentOptions", "Analyze request parameters.")
                            .addBlockTag("throws", "IllegalArgumentException",
                                "thrown if parameters fail the validation.")
                            .addBlockTag("throws", "HttpResponseException",
                                "thrown if the request is rejected by server.")
                            .addBlockTag("throws", "ClientAuthenticationException",
                                "thrown if the request is rejected by server on status code 401.")
                            .addBlockTag("throws", "ResourceNotFoundException",
                                "thrown if the request is rejected by server on status code 404.")
                            .addBlockTag("throws", "ResourceModifiedException",
                                "thrown if the request is rejected by server on status code 409.")
                            .addBlockTag("throws", "RuntimeException",
                                "all other wrapped checked exceptions if the request fails to be sent.")
                            .addBlockTag("return", "the {@link SyncPoller} for polling of long-running operation."));
                }
            });

            clazz.getMethodsByName("beginAnalyzeBatchDocuments").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("analyzeBatchRequest")
                        .ifPresent(param -> param.setName("analyzeBatchDocumentOptions"));
                    methodDeclaration.setBody(StaticJavaParser.parseBlock(
                        "{ Objects.requireNonNull(analyzeBatchDocumentOptions, \"'analyzeBatchDocumentOptions' cannot be null.\");"
                            + "return this.beginAnalyzeBatchDocuments(modelId, analyzeBatchDocumentOptions, analyzeBatchDocumentOptions.getPages() != null ? CoreUtils.stringJoin(\",\", analyzeBatchDocumentOptions.getPages()) : null, analyzeBatchDocumentOptions.getLocale(), analyzeBatchDocumentOptions.getStringIndexType(), analyzeBatchDocumentOptions.getDocumentAnalysisFeatures(), analyzeBatchDocumentOptions.getQueryFields(), analyzeBatchDocumentOptions.getOutputContentFormat(), analyzeBatchDocumentOptions.getOutput()); }"));
                    methodDeclaration.setJavadocComment(new Javadoc(
                        JavadocDescription.parseText("Analyzes batch documents with document model.")).addBlockTag(
                            "param", "modelId", "Unique document model name.")
                        .addBlockTag("param", "analyzeBatchDocumentOptions", "Analyze batch request parameters.")
                        .addBlockTag("throws", "IllegalArgumentException", "thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException", "thrown if the request is rejected by server.")
                        .addBlockTag("throws", "ClientAuthenticationException",
                            "thrown if the request is rejected by server on status code 401.")
                        .addBlockTag("throws", "ResourceNotFoundException",
                            "thrown if the request is rejected by server on status code 404.")
                        .addBlockTag("throws", "ResourceModifiedException",
                            "thrown if the request is rejected by server on status code 409.")
                        .addBlockTag("throws", "RuntimeException",
                            "all other wrapped checked exceptions if the request fails to be sent.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of long-running operation."));
                }
            });

            clazz.getMethodsByName("beginClassifyDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("classifyRequest")
                        .ifPresent(param -> param.setName("classifyDocumentOptions"));
                    methodDeclaration.setBody(StaticJavaParser.parseBlock(
                        "{ Objects.requireNonNull(classifyDocumentOptions, \"'classifyDocumentOptions' cannot be null.\");"
                            + "  return this.beginClassifyDocument(classifierId, classifyDocumentOptions, classifyDocumentOptions.getStringIndexType(), classifyDocumentOptions.getSplit(), classifyDocumentOptions.getPages() != null ? CoreUtils.stringJoin(\",\", classifyDocumentOptions.getPages()) : null); }"));
                    methodDeclaration.setJavadocComment(new Javadoc(
                        JavadocDescription.parseText("Classifies document with document classifier.")).addBlockTag(
                            "param", "classifierId", "Unique document classifier name.")
                        .addBlockTag("param", "classifyDocumentOptions", "Classify request parameters.")
                        .addBlockTag("throws", "IllegalArgumentException", "thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException", "thrown if the request is rejected by server.")
                        .addBlockTag("throws", "ClientAuthenticationException",
                            "thrown if the request is rejected by server on status code 401.")
                        .addBlockTag("throws", "ResourceNotFoundException",
                            "thrown if the request is rejected by server on status code 404.")
                        .addBlockTag("throws", "ResourceModifiedException",
                            "thrown if the request is rejected by server on status code 409.")
                        .addBlockTag("throws", "RuntimeException",
                            "all other wrapped checked exceptions if the request fails to be sent.")
                        .addBlockTag("return", "the {@link SyncPoller} for polling of long-running operation."));
                }
            });
        }));
    }

    private void customizeAnalyzeDocumentOptions(LibraryCustomization customization) {
        customization.getPackage(MODELS_PACKAGE)
            .getClass("AnalyzeDocumentOptions")
            .customizeAst(ast -> ast.getClassByName("AnalyzeDocumentOptions").ifPresent(clazz -> {
                clazz.getDefaultConstructor().ifPresent(ConstructorDeclaration::setModifiers);
                clazz.getMethodsByName("setUrlSource").forEach(MethodDeclaration::setModifiers);
                clazz.getMethodsByName("setBytesSource").forEach(MethodDeclaration::setModifiers);
            }));
    }

    private void customizeAnalyzeBatchDocumentOptions(LibraryCustomization customization) {
        customization.getPackage(MODELS_PACKAGE)
            .getClass("AnalyzeBatchDocumentsOptions")
            .customizeAst(ast -> ast.getClassByName("AnalyzeBatchDocumentsOptions").ifPresent(clazz -> {
                clazz.getConstructors()
                    .stream()
                    .filter(ctor -> ctor.getParameters().size() == 1 && "String".equals(
                        ctor.getParameter(0).getType().asString()))
                    .forEach(ConstructorDeclaration::setModifiers);
                clazz.getMethodsByName("setAzureBlobSource").forEach(MethodDeclaration::setModifiers);
                clazz.getMethodsByName("setAzureBlobFileListSource").forEach(MethodDeclaration::setModifiers);
            }));
    }

    private void customizeClassifyDocumentOptions(LibraryCustomization customization) {
        customization.getPackage(MODELS_PACKAGE)
            .getClass("ClassifyDocumentOptions")
            .customizeAst(ast -> ast.getClassByName("ClassifyDocumentOptions").ifPresent(clazz -> {
                clazz.getDefaultConstructor().ifPresent(ConstructorDeclaration::setModifiers);
                clazz.getMethodsByName("setUrlSource").forEach(MethodDeclaration::setModifiers);
                clazz.getMethodsByName("setBytesSource").forEach(MethodDeclaration::setModifiers);
            }));
    }

    private void customizeModifierForOverloadMethods(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing to make overload methods package private");
        PackageCustomization models = customization.getPackage("com.azure.ai.documentintelligence");

        for (String className : List.of("DocumentIntelligenceClient", "DocumentIntelligenceAsyncClient")) {
            models.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
                clazz.getMethodsByName("beginAnalyzeDocument")
                    .forEach(methodDeclaration -> methodDeclaration.getParameterByName("outputContentFormat")
                        .ifPresent(ignored -> methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC)));

                clazz.getMethodsByName("beginAnalyzeBatchDocuments")
                    .forEach(methodDeclaration -> methodDeclaration.getParameterByName("outputContentFormat")
                        .ifPresent(ignored -> methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC)));

                clazz.getMethodsByName("beginClassifyDocument")
                    .forEach(methodDeclaration -> methodDeclaration.getParameterByName("split")
                        .ifPresent(ignored -> methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC)));
            }));
        }
    }

    private void customizeAnalyzeOperation(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the AnalyzeOperationDetails class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.models");
        packageCustomization.getClass("AnalyzeOperationDetails")
            .customizeAst(ast -> ast.getClassByName("AnalyzeOperationDetails").ifPresent(clazz -> {
                clazz.getAnnotationByName("Immutable").ifPresent(AnnotationExpr::remove);
                addOperationIdField(clazz);
                addOperationIdGetter(clazz);
                addOperationIdSetter(clazz);
            }));
    }

    private void addOperationIdSetter(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("setOperationId", Modifier.Keyword.PRIVATE)
            .setType("void")
            .addParameter("String", "operationId")
            .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Sets the operationId property: Operation ID."))))
                .addBlockTag("param", "operationId the operationId value to set."))
            .setBody(StaticJavaParser.parseBlock("{ this.operationId = operationId; }"));
    }

    private void addOperationIdGetter(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("getResultId", Modifier.Keyword.PUBLIC)
            .setType("String")
            .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Gets the operationId property: Operation ID."))))
                .addBlockTag("return", "the operationId value."))
            .setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr(new SimpleName("operationId")))));
    }

    private void addOperationIdField(ClassOrInterfaceDeclaration clazz) {
        clazz.addField("String", "operationId", Modifier.Keyword.PRIVATE);
    }

    private void customizePollingUtils(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the PollingUtils class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.implementation");
        packageCustomization.getClass("PollingUtils").customizeAst(ast -> {
            ast.addImport("java.util.regex.Matcher");
            ast.addImport("java.util.regex.Pattern");
            ast.getClassByName("PollingUtils").ifPresent(this::addParseOperationIdMethod);
        });
    }

    private void addParseOperationIdMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("parseOperationId", Modifier.Keyword.PRIVATE)
            .setType("String")
            .setModifiers(Modifier.Keyword.STATIC)
            .addParameter("String", "operationLocationHeader")
            .setBody(StaticJavaParser.parseBlock("{ if (CoreUtils.isNullOrEmpty(operationLocationHeader)) { return null; }"
                + "Pattern pattern = Pattern.compile(\"[^:]+://[^/]+/documentintelligence/.+/([^?/]+)\");"
                + "Matcher matcher = pattern.matcher(operationLocationHeader);"
                + "if (matcher.find() && matcher.group(1) != null) { return matcher.group(1); }"
                + "return null; }"));
    }

    private void customizePollingStrategy(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the SyncOperationLocationPollingStrategy class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.implementation");
        packageCustomization.getClass("SyncOperationLocationPollingStrategy").customizeAst(ast -> {
            ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeOperationDetails");
            ast.addImport("static com.azure.ai.documentintelligence.implementation.PollingUtils.parseOperationId");
            ast.getClassByName("SyncOperationLocationPollingStrategy").ifPresent(this::addSyncPollOverrideMethod);
        });

        logger.info("Customizing the OperationLocationPollingStrategy class");
        packageCustomization.getClass("OperationLocationPollingStrategy").customizeAst(ast -> {
            ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeOperationDetails");
            ast.addImport("static com.azure.ai.documentintelligence.implementation.PollingUtils.parseOperationId");
            ast.getClassByName("OperationLocationPollingStrategy").ifPresent(this::addAsyncPollOverrideMethod);
        });
    }

    private void addAsyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("Mono<PollResponse<T>>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock("{ return super.poll(pollingContext, pollResponseType)"
                + ".map(pollResponse -> {"
                + "  String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));"
                + "  String operationId = null;"
                + "  if (operationLocationHeader != null) {"
                + "    operationId = parseOperationId(operationLocationHeader);"
                + "  }"
                + "  if (pollResponse.getValue() instanceof AnalyzeOperationDetails) {"
                + "    AnalyzeOperationDetails operation = (AnalyzeOperationDetails) pollResponse.getValue();"
                + "    AnalyzeOperationDetailsHelper.setOperationId(operation, operationId);"
                + "  }"
                + "  return pollResponse;"
                + "}); }"));
    }

    private void addSyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("PollResponse<T>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock("{ PollResponse<T> pollResponse = super.poll(pollingContext, pollResponseType);"
                + "String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));"
                + "String operationId = null;"
                + "if (operationLocationHeader != null) { operationId = parseOperationId(operationLocationHeader); }"
                + "if (pollResponse.getValue() instanceof AnalyzeOperationDetails) {"
                + "    AnalyzeOperationDetails operation = (AnalyzeOperationDetails) pollResponse.getValue();"
                + "    AnalyzeOperationDetailsHelper.setOperationId(operation, operationId);"
                + "}"
                + "return pollResponse; }"));
    }
}
