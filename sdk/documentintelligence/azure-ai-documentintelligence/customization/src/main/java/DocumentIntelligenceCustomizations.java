import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
        customizeAnalyzeDocumentOptions(customization, logger);
        customizeAnalyzeBatchDocumentOptions(customization, logger);
        customizeClassifyDocumentOptions(customization, logger);
        customizeSamplesForOverload(customization, logger);
        addStaticAccessorForOperationId(customization, logger);
    }

    private void addStaticAccessorForOperationId(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing to add static operationnId accessor setter methods");
        ClassCustomization classCustomization = customization.getPackage(MODELS_PACKAGE).getClass("AnalyzeOperationDetails");
        classCustomization.addStaticBlock("AnalyzeOperationDetailsHelper.setAccessor(new AnalyzeOperationDetailsHelper.AnalyzeOperationDetailsAccessor() {\n" +
            "            @Override\n" +
            "            public void setOperationId(AnalyzeOperationDetails analyzeOperation, String operationId) {\n" +
            "                analyzeOperation.setOperationId(operationId);\n" +
            "            }\n" +
            "        });");
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
        classCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration documentIntelligenceClient = compilationUnit.getClassByName(className).get();
            documentIntelligenceClient.getMethodsByName("beginAnalyzeDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("analyzeRequest").get().setName("analyzeDocumentOptions");
                    methodDeclaration.setBody(StaticJavaParser.parseBlock(String.join("\n",
                        "{", "Objects.requireNonNull(analyzeDocumentOptions, \"'analyzeDocumentOptions' cannot be null.\");\n" +
                            "        return this.beginAnalyzeDocument(modelId, analyzeDocumentOptions, analyzeDocumentOptions.getPages() != null ? CoreUtils.stringJoin(\",\", analyzeDocumentOptions.getPages()) : null, analyzeDocumentOptions.getLocale(), analyzeDocumentOptions.getStringIndexType(), analyzeDocumentOptions.getDocumentAnalysisFeatures(), analyzeDocumentOptions.getQueryFields(), analyzeDocumentOptions.getOutputContentFormat(), analyzeDocumentOptions.getOutput());\n" +
                            "}")));
                    methodDeclaration.setJavadocComment("/**\n" +
                        "     * Analyzes document with document model.\n" +
                        "     *\n" +
                        "     * @param modelId Unique document model name.\n" +
                        "     * @param analyzeDocumentOptions Analyze request parameters.\n" +
                        "     * @throws IllegalArgumentException thrown if parameters fail the validation.\n" +
                        "     * @throws HttpResponseException thrown if the request is rejected by server.\n" +
                        "     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.\n" +
                        "     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.\n" +
                        "     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.\n" +
                        "     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.\n" +
                        "     * @return the {@link SyncPoller} for polling of long-running operation.\n" +
                        "     */");

                }
            });

            documentIntelligenceClient.getMethodsByName("beginAnalyzeBatchDocuments").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("analyzeBatchRequest").get().setName("analyzeBatchDocumentOptions");
                    methodDeclaration.setBody(StaticJavaParser.parseBlock(String.join("\n",
                        "{", "Objects.requireNonNull(analyzeBatchDocumentOptions, \"'analyzeBatchDocumentOptions' cannot be null.\");\n" +
                            "        return this.beginAnalyzeBatchDocuments(modelId, analyzeBatchDocumentOptions, analyzeBatchDocumentOptions.getPages() != null ? CoreUtils.stringJoin(\",\", analyzeBatchDocumentOptions.getPages()) : null, analyzeBatchDocumentOptions.getLocale(), analyzeBatchDocumentOptions.getStringIndexType(), analyzeBatchDocumentOptions.getDocumentAnalysisFeatures(), analyzeBatchDocumentOptions.getQueryFields(), analyzeBatchDocumentOptions.getOutputContentFormat(), analyzeBatchDocumentOptions.getOutput());\n" +
                            "}")));
                    methodDeclaration.setJavadocComment("/**\n" +
                        "     * Analyzes batch documents with document model.\n" +
                        "     *\n" +
                        "     * @param modelId Unique document model name.\n" +
                        "     * @param analyzeBatchDocumentOptions Analyze batch request parameters.\n" +
                        "     * @throws IllegalArgumentException thrown if parameters fail the validation.\n" +
                        "     * @throws HttpResponseException thrown if the request is rejected by server.\n" +
                        "     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.\n" +
                        "     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.\n" +
                        "     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.\n" +
                        "     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.\n" +
                        "     * @return the {@link SyncPoller} for polling of long-running operation.\n" +
                        "     */");

                }
            });

            documentIntelligenceClient.getMethodsByName("beginClassifyDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameters().size() == 2) {
                    methodDeclaration.getParameterByName("classifyRequest").get().setName("classifyDocumentOptions");
                    methodDeclaration.setBody(StaticJavaParser.parseBlock(String.join("\n",
                        "{", "Objects.requireNonNull(classifyDocumentOptions, \"'classifyDocumentOptions' cannot be null.\");\n" +
                            "        return this.beginClassifyDocument(classifierId, classifyDocumentOptions, classifyDocumentOptions.getStringIndexType(), classifyDocumentOptions.getSplit(), classifyDocumentOptions.getPages() != null ? CoreUtils.stringJoin(\",\", classifyDocumentOptions.getPages()) : null);\n" +
                            "}")));
                    methodDeclaration.setJavadocComment("/**\n" +
                        "     * Classifies document with document classifier.\n" +
                        "     *\n" +
                        "     * @param classifierId Unique document classifier name.\n" +
                        "     * @param classifyDocumentOptions Classify request parameters.\n" +
                        "     * @throws IllegalArgumentException thrown if parameters fail the validation.\n" +
                        "     * @throws HttpResponseException thrown if the request is rejected by server.\n" +
                        "     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.\n" +
                        "     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.\n" +
                        "     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.\n" +
                        "     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.\n" +
                        "     * @return the {@link SyncPoller} for polling of long-running operation.\n" +
                        "     */");
                }
            });
        });
    }

    private void customizeAnalyzeDocumentOptions(LibraryCustomization customization, Logger logger) {
        ClassCustomization classCustomization = customization.getPackage(MODELS_PACKAGE)
            .getClass("AnalyzeDocumentOptions");
        classCustomization.getConstructor("AnalyzeDocumentOptions()")
            .setModifier(0);

        classCustomization.getMethod("setUrlSource").setModifier(0);
        classCustomization.getMethod("setBytesSource").setModifier(0);
    }

    private void customizeAnalyzeBatchDocumentOptions(LibraryCustomization customization, Logger logger) {
        ClassCustomization classCustomization = customization.getPackage(MODELS_PACKAGE)
            .getClass("AnalyzeBatchDocumentsOptions");
        classCustomization.getConstructor("AnalyzeBatchDocumentsOptions(String resultContainerUrl)")
            .setModifier(0);

        classCustomization.getMethod("setAzureBlobSource").setModifier(0);
        classCustomization.getMethod("setAzureBlobFileListSource").setModifier(0);
    }

    private void customizeClassifyDocumentOptions(LibraryCustomization customization, Logger logger) {
        ClassCustomization classCustomization = customization.getPackage(MODELS_PACKAGE)
            .getClass("ClassifyDocumentOptions");
        classCustomization.getConstructor("ClassifyDocumentOptions()")
            .setModifier(0);

        classCustomization.getMethod("setUrlSource").setModifier(0);
        classCustomization.getMethod("setBytesSource").setModifier(0);
    }

    private void customizeModifierForOverloadMethods(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing to make overload methods package private");
        PackageCustomization models = customization.getPackage("com.azure.ai.documentintelligence");
        ClassCustomization classCustomization = models.getClass("DocumentIntelligenceClient");
        classCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration documentIntelligenceClient = compilationUnit.getClassByName("DocumentIntelligenceClient").get();
            documentIntelligenceClient.getMethodsByName("beginAnalyzeDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameterByName("outputContentFormat").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });

            documentIntelligenceClient.getMethodsByName("beginAnalyzeBatchDocuments").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameterByName("outputContentFormat").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });

            documentIntelligenceClient.getMethodsByName("beginClassifyDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameterByName("split").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });
        });
        ClassCustomization asyncClassCustomization = models.getClass("DocumentIntelligenceAsyncClient");
        asyncClassCustomization.customizeAst(compilationUnit1 -> {
            ClassOrInterfaceDeclaration documentIntelligenceAsyncClient = compilationUnit1.getClassByName("DocumentIntelligenceAsyncClient").get();
            documentIntelligenceAsyncClient.getMethodsByName("beginAnalyzeDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameterByName("outputContentFormat").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });

            documentIntelligenceAsyncClient.getMethodsByName("beginAnalyzeBatchDocuments").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameterByName("outputContentFormat").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });

            documentIntelligenceAsyncClient.getMethodsByName("beginClassifyDocument").forEach(methodDeclaration -> {
                if (methodDeclaration.getParameterByName("split").isPresent()) {
                    methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
                }
            });
        });
    }

    private void customizeAnalyzeOperation(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the AnalyzeOperationDetails class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.models");
        packageCustomization.getClass("AnalyzeOperationDetails")
            .removeAnnotation("Immutable")
            .customizeAst(ast ->
                ast.getClassByName("AnalyzeOperationDetails").ifPresent(clazz -> {
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
                .addBlockTag("param", "operationId the operationId value to set.")
            )
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "this.operationId = operationId;",
                "}")));
    }

    private void addOperationIdGetter(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("getResultId", Modifier.Keyword.PUBLIC)
            .setType("String")
            .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Gets the operationId property: Operation ID."))))
                .addBlockTag("return", "the operationId value.")
            )
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "return operationId;",
                "}")));
    }

    private void addOperationIdField(ClassOrInterfaceDeclaration clazz) {
        clazz.addField("String", "operationId", Modifier.Keyword.PRIVATE);
    }

    private void customizePollingUtils(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the PollingUtils class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.implementation");
        packageCustomization.getClass("PollingUtils").customizeAst(ast ->
            ast.getClassByName("PollingUtils").ifPresent(clazz -> {
                ast.addImport("java.util.regex.Matcher");
                ast.addImport("java.util.regex.Pattern");
                addParseOperationIdMethod(clazz);
            }));
    }

    private void addParseOperationIdMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("parseOperationId", Modifier.Keyword.PRIVATE)
            .setType("String")
            .setModifiers(Modifier.Keyword.STATIC)
            .addParameter("String", "operationLocationHeader")
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "if (CoreUtils.isNullOrEmpty(operationLocationHeader)) {",
                "    return null;",
                "}",
                "Pattern pattern = Pattern.compile(\"[^:]+://[^/]+/documentintelligence/.+/([^?/]+)\");",
                "Matcher matcher = pattern.matcher(operationLocationHeader);",
                "if (matcher.find() && matcher.group(1) != null) {",
                "    return matcher.group(1);",
                "}",
                "return null;",
                "}")));
    }

    private void customizePollingStrategy(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the SyncOperationLocationPollingStrategy class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.ai.documentintelligence.implementation");
        packageCustomization.getClass("SyncOperationLocationPollingStrategy").customizeAst(ast ->
            ast.getClassByName("SyncOperationLocationPollingStrategy").ifPresent(clazz -> {
                ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeOperationDetails");
                ast.addImport("static com.azure.ai.documentintelligence.implementation.PollingUtils.parseOperationId");
                addSyncPollOverrideMethod(clazz);
            }));

        logger.info("Customizing the OperationLocationPollingStrategy class");
        packageCustomization.getClass("OperationLocationPollingStrategy").customizeAst(ast ->
            ast.getClassByName("OperationLocationPollingStrategy").ifPresent(clazz -> {
                ast.addImport("com.azure.ai.documentintelligence.models.AnalyzeOperationDetails");
                ast.addImport("static com.azure.ai.documentintelligence.implementation.PollingUtils.parseOperationId");
                addAsyncPollOverrideMethod(clazz);
            }));

    }

    private void addAsyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("Mono<PollResponse<T>>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "return super.poll(pollingContext, pollResponseType)",
                "    .map(pollResponse -> {",
                "        String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));",
                "        String operationId = null;",
                "        if (operationLocationHeader != null) {",
                "            operationId = parseOperationId(operationLocationHeader);",
                "        }",
                "        if (pollResponse.getValue() instanceof AnalyzeOperationDetails) {",
                "            AnalyzeOperationDetails operation = (AnalyzeOperationDetails) pollResponse.getValue();",
                "            AnalyzeOperationDetailsHelper.setOperationId(operation, operationId);",
                "        }",
                "        return pollResponse;",
                "    });",
                "}")));
    }

    private void addSyncPollOverrideMethod(ClassOrInterfaceDeclaration clazz) {
        clazz.addMethod("poll", Modifier.Keyword.PUBLIC)
            .setType("PollResponse<T>")
            .addParameter("PollingContext<T>", "pollingContext")
            .addParameter("TypeReference<T>", "pollResponseType")
            .addAnnotation(Override.class)
            .setBody(StaticJavaParser.parseBlock(String.join("\n",
                "{",
                "PollResponse<T> pollResponse = super.poll(pollingContext, pollResponseType);",
                "String operationLocationHeader = pollingContext.getData(String.valueOf(PollingUtils.OPERATION_LOCATION_HEADER));",
                "String operationId = null;",
                "if (operationLocationHeader != null) {",
                "    operationId = parseOperationId(operationLocationHeader);",
                "}",
                "if (pollResponse.getValue() instanceof AnalyzeOperationDetails) {",
                "    AnalyzeOperationDetails operation = (AnalyzeOperationDetails) pollResponse.getValue();",
                "    AnalyzeOperationDetailsHelper.setOperationId(operation, operationId);",
                "}",
                "return pollResponse;",
                "}")));
    }
}
