// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Contains customizations for Azure AI Search code generation.
 */
public class SearchCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization documents = libraryCustomization.getPackage("com.azure.search.documents");
        PackageCustomization indexes = libraryCustomization.getPackage("com.azure.search.documents.indexes");
        PackageCustomization knowledge = libraryCustomization.getPackage("com.azure.search.documents.knowledgebases");

        hideGeneratedSearchApis(documents);

        addSearchAudienceScopeHandling(documents.getClass("SearchClientBuilder"), logger);
        addSearchAudienceScopeHandling(indexes.getClass("SearchIndexClientBuilder"), logger);
        addSearchAudienceScopeHandling(indexes.getClass("SearchIndexerClientBuilder"), logger);
        addSearchAudienceScopeHandling(knowledge.getClass("KnowledgeBaseRetrievalClientBuilder"), logger);

        ClassCustomization serviceVersion = documents.getClass("SearchServiceVersion");
        includeOldApiVersions(serviceVersion);
        removeOldPreviewApiVersions(serviceVersion);

        ClassCustomization searchClient = documents.getClass("SearchClient");
        ClassCustomization searchAsyncClient = documents.getClass("SearchAsyncClient");

        removeGetApis(searchClient);
        removeGetApis(searchAsyncClient);

        hideSearchDocumentsResultInternalProperties(
            libraryCustomization.getPackage("com.azure.search.documents.models").getClass("SearchDocumentsResult"));

        hideWithResponseBinaryDataApis(searchClient);
        hideWithResponseBinaryDataApis(searchAsyncClient);
        hideWithResponseBinaryDataApis(indexes.getClass("SearchIndexClient"));
        hideWithResponseBinaryDataApis(indexes.getClass("SearchIndexAsyncClient"));
        hideWithResponseBinaryDataApis(indexes.getClass("SearchIndexerClient"));
        hideWithResponseBinaryDataApis(indexes.getClass("SearchIndexerAsyncClient"));
        hideWithResponseBinaryDataApis(knowledge.getClass("KnowledgeBaseRetrievalClient"));
        hideWithResponseBinaryDataApis(knowledge.getClass("KnowledgeBaseRetrievalAsyncClient"));
        preserveFileParameterOrder(indexes.getClass("SearchIndexClient"));
        preserveFileParameterOrder(indexes.getClass("SearchIndexAsyncClient"));
        for (boolean async : Arrays.asList(false, true)) {
            String suffix = async ? "AsyncClient" : "Client";
            repairListingConvenience(indexes.getClass("SearchIndex" + suffix), "SynonymMaps", "SynonymMap", async);
            repairListingConvenience(indexes.getClass("SearchIndexer" + suffix), "DataSourceConnections",
                "SearchIndexerDataSourceConnection", async);
            repairListingConvenience(indexes.getClass("SearchIndexer" + suffix), "Indexers", "SearchIndexer", async);
            repairListingConvenience(indexes.getClass("SearchIndexer" + suffix), "Skillsets", "SearchIndexerSkillset",
                async);
        }

        addGraphResponseMethods(searchClient, false);
        addGraphResponseMethods(searchAsyncClient, true);
        preservePagedSearchAuthorization(searchClient);
        preservePagedSearchAuthorization(searchAsyncClient);
        preserveScoringFunctionCompatibility(
            libraryCustomization.getPackage("com.azure.search.documents.indexes.models").getClass("ScoringFunction"));
        libraryCustomization.getPackage("com.azure.search.documents.indexes.models")
            .getClass("KnowledgeSourceFileIngestionOperation")
            .customizeAst(ast -> {
                ast.getClassByName("KnowledgeSourceFileIngestionOperation").ifPresent(clazz -> clazz
                    .getFieldByName("kind").ifPresent(field -> {
                        field.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);
                        field.getVariable(0).setName("KIND");
                    }));
                ast.findAll(com.github.javaparser.ast.expr.FieldAccessExpr.class).stream()
                    .filter(access -> access.getScope().isThisExpr() && access.getNameAsString().equals("kind"))
                    .forEach(access -> access
                        .setScope(StaticJavaParser.parseExpression("KnowledgeSourceFileIngestionOperation"))
                        .setName("KIND"));
            });
        for (String kind : Arrays.asList("Distance", "Freshness", "Magnitude", "Tag")) {
            preserveScoringConstructor(
                libraryCustomization.getPackage("com.azure.search.documents.indexes.models")
                    .getClass(kind + "ScoringFunction"), kind);
        }
        libraryCustomization.getPackage("com.azure.search.documents.indexes.models")
            .getClass("SearchIndexKnowledgeSourceFilterHint")
            .customizeAst(ast -> ast.getClassByName("SearchIndexKnowledgeSourceFilterHint").ifPresent(clazz -> {
                if (clazz.getConstructors().stream().noneMatch(constructor -> constructor.getParameters().size() == 2)) {
                    clazz.addMember(StaticJavaParser.parseBodyDeclaration(
                        "public SearchIndexKnowledgeSourceFilterHint(String field, List<String> fieldValues) {"
                            + "this(field); setFieldValues(fieldValues); }"));
                    clazz.getConstructors().stream().filter(constructor -> constructor.getParameters().size() == 2)
                        .forEach(constructor -> constructor.setJavadocComment("Creates a filter hint with explicit values.\n"
                            + "@param field the field to filter.\n@param fieldValues the allowed filter values."));
                }
            }));

        customizeKnowledgeBaseRetrievalStream(libraryCustomization, logger);
        customizeKnowledgeSourceStatusDurationParsing(
            libraryCustomization.getPackage("com.azure.search.documents.knowledgebases.models")
                .getClass("KnowledgeSourceStatus"));
        repairAsyncSynonymMapsConvenienceMethod(indexes.getClass("SearchIndexAsyncClient"));

        // After hiding BinaryData protocol methods, add typed public convenience wrappers on the async client
        // that mirror what the sync client already has as hand-written methods.
        addAsyncKnowledgeBaseConvenienceMethods(indexes.getClass("SearchIndexAsyncClient"));

        // SearchResourceEncryptionKey workaround: the spec marks keyVaultUri and keyVaultKeyName as required,
        // but they are not required when isServiceLevelKey is true. Add a no-arg constructor.
        addNoArgConstructorToEncryptionKey(libraryCustomization.getPackage("com.azure.search.documents.indexes.models")
            .getClass("SearchResourceEncryptionKey"));
    }

    private static void preservePagedSearchAuthorization(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> clazz
            .getMethodsByName("search").forEach(method -> method
                .findAll(com.github.javaparser.ast.expr.MethodCallExpr.class)
                .stream()
                .filter(call -> call.getNameAsString().equals("searchWithResponse")
                    && call.getArguments().size() == 2
                    && call.getArgument(1).toString().equals("requestOptions"))
                .forEach(call -> call.setArgument(1,
                    StaticJavaParser.parseExpression("SearchUtils.addSearchHeaders(requestOptions, options)"))))));
    }

    private static void repairListingConvenience(ClassCustomization customization, String operation, String model,
        boolean async) {
        customization.customizeAst(ast -> {
            ast.addImport("com.azure.search.documents.indexes.models.ListingSearchType");
            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                String methodName = "get" + operation;
                String serviceMethod = "list" + operation + (async ? "Async" : "");
                String body = "{ RequestOptions requestOptions = new RequestOptions();"
                    + "if (select != null) { requestOptions.addQueryParam(\"$select\", String.join(\",\", select), false); }"
                    + "if (search != null) { requestOptions.addQueryParam(\"search\", search, false); }"
                    + "if (pageSize != null) { requestOptions.addQueryParam(\"pageSize\", pageSize.toString(), false); }"
                    + "if (searchType != null) { requestOptions.addQueryParam(\"searchType\", searchType.toString(), false); }"
                    + "return serviceClient." + serviceMethod
                    + "(requestOptions).mapPage(value -> value.toObject(" + model + ".class)); }";
                if (clazz.getMethodsByName(methodName).stream()
                    .noneMatch(method -> method.getParameters().size() == 4)) {
                    clazz.addMember(StaticJavaParser.parseBodyDeclaration((async ? "PagedFlux<" : "PagedIterable<")
                        + model + "> " + methodName
                        + "(List<String> select, String search, Integer pageSize, ListingSearchType searchType) " + body));
                } else {
                    clazz.getMethodsByName(methodName).stream()
                        .filter(method -> method.getParameters().size() == 4)
                        .forEach(method -> method.setBody(StaticJavaParser.parseBlock(body)));
                }
            });
        });
    }

    private static void preserveFileParameterOrder(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            for (MethodDeclaration method : clazz.getMethods()) {
                if (method.isPublic() && method.getParameters().size() >= 2
                    && method.getParameter(0).getNameAsString().equals("name")
                    && (method.getNameAsString().equals("deleteKnowledgeSourceFile")
                        || method.getNameAsString().equals("deleteKnowledgeSourceFileWithResponse")
                        || method.getNameAsString().equals("updateKnowledgeSourceFile")
                        || method.getNameAsString().equals("uploadKnowledgeSourceFile"))) {
                    com.github.javaparser.ast.body.Parameter first = method.getParameter(0);
                    method.getParameters().set(0, method.getParameter(1));
                    method.getParameters().set(1, first);
                    clazz.findAll(com.github.javaparser.ast.expr.MethodCallExpr.class).stream()
                        .filter(call -> call.getNameAsString().equals(method.getNameAsString())
                            && call.getArguments().size() >= 2
                            && (!call.getScope().isPresent() || call.getScope().get().isThisExpr()))
                        .forEach(call -> {
                            com.github.javaparser.ast.expr.Expression argument = call.getArgument(0);
                            call.getArguments().set(0, call.getArgument(1));
                            call.getArguments().set(1, argument);
                        });
                }
            }
        }));
    }

    private static void preserveScoringConstructor(ClassCustomization customization, String kind) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            if (clazz.getConstructors().stream().noneMatch(constructor -> constructor.getParameters().size() == 3
                && constructor.getParameter(0).getType().asString().equals("String"))) {
                clazz.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public " + kind + "ScoringFunction(String fieldName, double boost, " + kind
                        + "ScoringParameters parameters) { this(boost, fieldName, parameters); }"));
                clazz.getConstructors().stream().filter(constructor -> constructor.getParameters().size() == 3
                    && constructor.getParameter(0).getType().asString().equals("String"))
                    .forEach(constructor -> constructor.setJavadocComment("Creates a field-based scoring function.\n"
                        + "@param fieldName the field used as input.\n@param boost the score multiplier.\n"
                        + "@param parameters the scoring parameters."));
            }
        }));
    }

    private static void addGraphResponseMethods(ClassCustomization customization, boolean async) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            String responseType = async ? "Mono<Response<GraphQueryResult>>" : "Response<GraphQueryResult>";
            String converter = async ? "mapResponse" : "convertResponse";
            if (clazz.getMethodsByName("graphWithResponse").isEmpty()) {
                clazz.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + responseType
                    + " graphWithResponse(GraphOptions options, RequestOptions requestOptions) {"
                    + "return SearchUtils." + converter
                    + "(hiddenGeneratedGraphWithResponse(BinaryData.fromObject(SearchUtils.fromGraphOptions(options)),"
                    + "SearchUtils.addGraphHeaders(requestOptions, options)), GraphQueryResult.class); }"));
            }
            clazz.getMethodsByName("graphWithResponse")
                .forEach(method -> method.getAllContainedComments().forEach(com.github.javaparser.ast.comments.Comment::remove));
            clazz.getMethodsByName("graphWithResponse").forEach(method -> method.setJavadocComment(
                "Executes a graph query and returns its HTTP response and diagnostics.\n"
                    + "@param options the graph query and diagnostic options.\n"
                    + "@param requestOptions the HTTP request options.\n"
                    + "@return the graph result together with the HTTP response.\n"
                    + "@throws NullPointerException if options is null."));
        }));
    }

    private static void preserveScoringFunctionCompatibility(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            if (!clazz.getFieldByName("fieldName").isPresent()) {
                clazz.addField(String.class, "fieldName", Modifier.Keyword.PRIVATE);
                clazz.addField(double.class, "boost", Modifier.Keyword.PRIVATE);
            }
            if (clazz.getConstructors().stream().noneMatch(constructor -> constructor.getParameters().size() == 2)) {
                clazz.addMember(StaticJavaParser.parseBodyDeclaration(
                "public ScoringFunction(String fieldName, double boost) {"
                    + "this.fieldName = fieldName; this.boost = boost; }"));
            }
            if (clazz.getMethodsByName("getFieldName").isEmpty()) {
                clazz.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public String getFieldName() { return this.fieldName; }"));
            }
            if (clazz.getMethodsByName("getBoost").isEmpty()) {
                clazz.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public double getBoost() { return this.boost; }"));
            }
            clazz.getConstructors().stream().filter(constructor -> constructor.getParameters().size() == 2)
                .forEach(constructor -> constructor.getAllContainedComments()
                    .forEach(com.github.javaparser.ast.comments.Comment::remove));
            for (String getter : Arrays.asList("getFieldName", "getBoost")) {
                clazz.getMethodsByName(getter).forEach(method -> method.getAllContainedComments()
                    .forEach(com.github.javaparser.ast.comments.Comment::remove));
            }
            clazz.getConstructors().stream().filter(constructor -> constructor.getParameters().size() == 2)
                .forEach(constructor -> constructor.setJavadocComment("Creates a field-based scoring function.\n"
                    + "@param fieldName the field used as input.\n@param boost the score multiplier."));
            clazz.getMethodsByName("getFieldName").forEach(method -> method.setJavadocComment(
                "Gets the field used as input.\n@return the field name."));
            clazz.getMethodsByName("getBoost").forEach(method -> method.setJavadocComment(
                "Gets the score multiplier.\n@return the boost."));
            clazz.getMethodsByName("toJson").stream()
                .filter(method -> !method.getBody().get().toString().contains("this.fieldName"))
                .forEach(method -> method.setBody(StaticJavaParser.parseBlock(
                method.getBody().get().toString().replace("jsonWriter.writeStartObject();",
                    "jsonWriter.writeStartObject(); if (this.fieldName != null) {"
                        + "jsonWriter.writeStringField(\"fieldName\", this.fieldName);"
                        + "jsonWriter.writeDoubleField(\"boost\", this.boost); }"))));
            clazz.getMethodsByName("fromJsonKnownDiscriminator")
                .stream()
                .filter(method -> !method.getBody().get().toString().contains("deserializedScoringFunction.fieldName"))
                .forEach(method -> method.setBody(StaticJavaParser.parseBlock(
                    method.getBody().get().toString().replace("if (\"type\".equals(fieldName)) {",
                        "if (\"fieldName\".equals(fieldName)) {"
                            + "deserializedScoringFunction.fieldName = reader.getString();"
                            + "} else if (\"boost\".equals(fieldName)) {"
                            + "deserializedScoringFunction.boost = reader.getDouble();"
                            + "} else if (\"type\".equals(fieldName)) {"))));
        }));
    }

    // Weird quirk in the Java generator where SearchOptions is inferred from the parameters of searchPost in TypeSpec,
    // where that class doesn't actually exist in TypeSpec so it requires making the searchPost API public which we
    // don't want. This customization hides the searchPost APIs that were exposed.
    private static void hideGeneratedSearchApis(PackageCustomization documents) {
        for (String className : Arrays.asList("SearchClient", "SearchAsyncClient")) {
            documents.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
                clazz.getMethodsByName("searchWithResponse")
                    .stream()
                    .filter(method -> method.isAnnotationPresent("Generated"))
                    .forEach(MethodDeclaration::setModifiers);

                clazz.getMethodsByName("autocompleteWithResponse")
                    .stream()
                    .filter(method -> method.isAnnotationPresent("Generated"))
                    .forEach(MethodDeclaration::setModifiers);

                clazz.getMethodsByName("suggestWithResponse")
                    .stream()
                    .filter(method -> method.isAnnotationPresent("Generated"))
                    .forEach(MethodDeclaration::setModifiers);
            }));
        }
    }

    // The TypeSpec Java emitter compiles and loads only this configured customization class. Keep the premature
    // retrieval stream API customizations isolated so they can be removed when native support is available.
    private static final String MODELS_PATH = "src/main/java/com/azure/search/documents/knowledgebases/models/";

    private static void customizeKnowledgeBaseRetrievalStream(LibraryCustomization libraryCustomization,
        Logger logger) {
        logger.info("Adding knowledge base retrieval stream convenience APIs and models");
        addStreamModels(libraryCustomization);

        PackageCustomization knowledgeBases
            = libraryCustomization.getPackage("com.azure.search.documents.knowledgebases");
        addAsyncRetrieveStream(knowledgeBases.getClass("KnowledgeBaseRetrievalAsyncClient"));
        addSyncRetrieveStream(knowledgeBases.getClass("KnowledgeBaseRetrievalClient"));
    }

    private static void addStreamModels(LibraryCustomization customization) {
        customization.getRawEditor().addFile(MODELS_PATH + "KnowledgeBaseRetrievalStreamEvent.java", baseEventSource());
        customization.getRawEditor()
            .addFile(MODELS_PATH + "UnknownKnowledgeBaseRetrievalStreamEvent.java", unknownEventSource());
        customization.getRawEditor()
            .addFile(MODELS_PATH + "KnowledgeBaseRetrievalStartedStreamEvent.java",
                wrapperSource("KnowledgeBaseRetrievalStartedStreamEvent", "KnowledgeBaseRetrievalStartedEvent",
                    "retrieval.started", false, false));
        customization.getRawEditor()
            .addFile(MODELS_PATH + "KnowledgeBaseActivityStartedStreamEvent.java",
                wrapperSource("KnowledgeBaseActivityStartedStreamEvent", "KnowledgeBaseActivityStartedEvent",
                    "activity.started", false, false));
        customization.getRawEditor()
            .addFile(MODELS_PATH + "KnowledgeBaseActivityCompletedStreamEvent.java",
                wrapperSource("KnowledgeBaseActivityCompletedStreamEvent", "KnowledgeBaseActivityRecord",
                    "activity.completed", false, false));
        customization.getRawEditor()
            .addFile(MODELS_PATH + "KnowledgeBaseAnswerCompletedStreamEvent.java",
                wrapperSource("KnowledgeBaseAnswerCompletedStreamEvent", "KnowledgeBaseAnswerCompletedEvent",
                    "answer.completed", false, false));
        customization.getRawEditor()
            .addFile(MODELS_PATH + "KnowledgeBaseReferencesCompletedStreamEvent.java",
                wrapperSource("KnowledgeBaseReferencesCompletedStreamEvent", "KnowledgeBaseReference",
                    "references.completed", false, true));
        customization.getRawEditor()
            .addFile(MODELS_PATH + "KnowledgeBaseErrorStreamEvent.java",
                wrapperSource("KnowledgeBaseErrorStreamEvent", "KnowledgeBaseStreamErrorEvent", "error", true, false));
        customization.getRawEditor()
            .addFile(MODELS_PATH + "KnowledgeBaseResponseCompletedStreamEvent.java",
                wrapperSource("KnowledgeBaseResponseCompletedStreamEvent", "KnowledgeBaseResponseCompletedEvent",
                    "response.completed", true, false));
        replaceStreamModel(customization, "KnowledgeBaseRetrievalStreamEvent", baseEventSource());
        replaceStreamModel(customization, "KnowledgeBaseRetrievalStartedStreamEvent",
            wrapperSource("KnowledgeBaseRetrievalStartedStreamEvent", "KnowledgeBaseRetrievalStartedEvent",
                "retrieval.started", false, false));
        replaceStreamModel(customization, "KnowledgeBaseActivityStartedStreamEvent",
            wrapperSource("KnowledgeBaseActivityStartedStreamEvent", "KnowledgeBaseActivityStartedEvent",
                "activity.started", false, false));
        replaceStreamModel(customization, "KnowledgeBaseActivityCompletedStreamEvent",
            wrapperSource("KnowledgeBaseActivityCompletedStreamEvent", "KnowledgeBaseActivityRecord",
                "activity.completed", false, false));
        replaceStreamModel(customization, "KnowledgeBaseAnswerCompletedStreamEvent",
            wrapperSource("KnowledgeBaseAnswerCompletedStreamEvent", "KnowledgeBaseAnswerCompletedEvent",
                "answer.completed", false, false));
        replaceStreamModel(customization, "KnowledgeBaseReferencesCompletedStreamEvent",
            wrapperSource("KnowledgeBaseReferencesCompletedStreamEvent", "KnowledgeBaseReference",
                "references.completed", false, true));
        replaceStreamModel(customization, "KnowledgeBaseErrorStreamEvent",
            wrapperSource("KnowledgeBaseErrorStreamEvent", "KnowledgeBaseStreamErrorEvent", "error", true, false));
        replaceStreamModel(customization, "KnowledgeBaseResponseCompletedStreamEvent",
            wrapperSource("KnowledgeBaseResponseCompletedStreamEvent", "KnowledgeBaseResponseCompletedEvent",
                "response.completed", true, false));
    }

    private static void replaceStreamModel(LibraryCustomization customization, String className, String source) {
        customization.getPackage("com.azure.search.documents.knowledgebases.models")
            .getClass(className)
            .customizeAst(ast -> {
                com.github.javaparser.ast.CompilationUnit replacement = StaticJavaParser.parse(source);
                ast.setImports(replacement.getImports());
                ast.setTypes(replacement.getTypes());
            });
    }

    private static void customizeKnowledgeSourceStatusDurationParsing(ClassCustomization customization) {
        customization.customizeAst(ast -> ast
            .addImport(
                "com.azure.search.documents.knowledgebases.implementation.KnowledgeSourceDurationParser")
            .getClassByName(customization.getClassName())
            .ifPresent(clazz -> clazz.getMethodsByName("fromJson").forEach(method -> {
                BlockStmt body = method.getBody()
                    .orElseThrow(() -> new IllegalStateException("KnowledgeSourceStatus.fromJson has no body."));
                String generatedParser = "Duration.parse(nonNullReader.getString())";
                String bodyText = body.toString();
                if (!bodyText.contains(generatedParser)) {
                    throw new IllegalStateException(
                        "KnowledgeSourceStatus.fromJson no longer uses the expected generated duration parser.");
                }
                method.setBody(StaticJavaParser.parseBlock(bodyText.replace(generatedParser,
                    "KnowledgeSourceDurationParser.parse(nonNullReader.getString())")));
            })));
    }

    private static void addAsyncRetrieveStream(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.addImport("com.azure.core.http.HttpHeaderName")
            .addImport("com.azure.search.documents.models.ServerSentEvent")
            .addImport("com.azure.search.documents.models.implementation.sse.ServerSentEventStreams")
            .addImport(
                "com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter")
            .addImport("com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent")
            .addImport("reactor.core.publisher.Flux")
            .getClassByName(customization.getClassName())
            .ifPresent(clazz -> {
                clazz.getMethodsByName("retrieveStream").forEach(MethodDeclaration::remove);
                MethodDeclaration method
                    = StaticJavaParser
                        .parseBodyDeclaration("@Generated\n"
                            + "public Flux<ServerSentEvent<KnowledgeBaseRetrievalStreamEvent>> retrieveStream("
                                + "KnowledgeBaseRetrievalOptions retrievalRequest) {\n"
                                + "    RequestOptions requestOptions = new RequestOptions();\n"
                                + "    return hiddenGeneratedRetrieveStreamWithResponse("
                                + "BinaryData.fromObject(retrievalRequest), requestOptions)\n"
                                + "        .flatMapMany(response -> ServerSentEventStreams.toFlux(response,\n"
                                + "            KnowledgeBaseRetrievalStreamEventConverter::convert,\n"
                                + "            event -> event.getData().isTerminal()));\n"
                                + "}\n")
                        .asMethodDeclaration();
                method.setJavadocComment(
                    "Retrieves relevant data from backing stores and streams progress and results as server-sent "
                        + "events.\n\n"
                        + "If received, the terminal {@code error} or {@code response.completed} event is emitted "
                        + "before the stream completes. End-of-stream without a terminal event completes normally. "
                        + "Transport and decoding failures are propagated through the reactive error path. The client "
                        + "does not reconnect automatically.\n\n"
                        + "@param retrievalRequest The retrieval request to process.\n"
                        + "@return A stream of typed knowledge base retrieval events.");
                clazz.addMember(method);

                MethodDeclaration methodWithAuthorizationHeaders
                    = StaticJavaParser
                        .parseBodyDeclaration("@Generated\n"
                            + "public Flux<ServerSentEvent<KnowledgeBaseRetrievalStreamEvent>> retrieveStream("
                                + "KnowledgeBaseRetrievalOptions retrievalRequest, String querySourceAuthorization,\n"
                                + "    String queryWorkIQSourceAuthorization) {\n"
                                + "    RequestOptions requestOptions = new RequestOptions();\n"
                                + "    if (querySourceAuthorization != null) {\n"
                                + "        requestOptions.setHeader(\n"
                                + "            HttpHeaderName.fromString(\"x-ms-query-source-authorization\"),\n"
                                + "            querySourceAuthorization);\n"
                                + "    }\n"
                                + "    if (queryWorkIQSourceAuthorization != null) {\n"
                                + "        requestOptions.setHeader(\n"
                                + "            HttpHeaderName.fromString(\"x-ms-query-work-iq-source-authorization\"),\n"
                                + "            queryWorkIQSourceAuthorization);\n"
                                + "    }\n"
                                + "    return hiddenGeneratedRetrieveStreamWithResponse("
                                + "BinaryData.fromObject(retrievalRequest), requestOptions)\n"
                                + "        .flatMapMany(response -> ServerSentEventStreams.toFlux(response,\n"
                                + "            KnowledgeBaseRetrievalStreamEventConverter::convert,\n"
                                + "            event -> event.getData().isTerminal()));\n" + "}\n")
                        .asMethodDeclaration();
                methodWithAuthorizationHeaders.setJavadocComment(
                    "Retrieves relevant data from backing stores and streams progress and results as server-sent "
                        + "events.\n\n"
                        + "If received, the terminal {@code error} or {@code response.completed} event is emitted "
                        + "before the stream completes. End-of-stream without a terminal event completes normally. "
                        + "Transport and decoding failures are propagated through the reactive error path. The client "
                        + "does not reconnect automatically.\n\n"
                        + "@param retrievalRequest The retrieval request to process.\n"
                        + "@param querySourceAuthorization Token identifying the user for which the query is being "
                        + "executed. This token is used to enforce security restrictions on documents.\n"
                        + "@param queryWorkIQSourceAuthorization User assertion token for a customer-owned Entra app "
                        + "registration configured on a Work IQ knowledge source. Used for on-behalf-of "
                        + "authentication to the Work IQ API.\n"
                        + "@return A stream of typed knowledge base retrieval events.");
                clazz.addMember(methodWithAuthorizationHeaders);
            }));
    }

    private static void addSyncRetrieveStream(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.addImport("com.azure.core.http.HttpHeaderName")
            .addImport("com.azure.search.documents.models.ServerSentEventListener")
            .addImport("com.azure.search.documents.models.implementation.sse.ServerSentEventStreams")
            .addImport(
                "com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter")
            .addImport("com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent")
            .getClassByName(customization.getClassName())
            .ifPresent(clazz -> {
                clazz.getMethodsByName("retrieveStream").forEach(MethodDeclaration::remove);
                MethodDeclaration method
                    = StaticJavaParser
                        .parseBodyDeclaration("@Generated\n"
                            + "public void retrieveStream(KnowledgeBaseRetrievalOptions retrievalRequest,\n"
                                + "    ServerSentEventListener<KnowledgeBaseRetrievalStreamEvent> listener) {\n"
                                + "    RequestOptions requestOptions = new RequestOptions();\n"
                                + "    ServerSentEventStreams.listen(hiddenGeneratedRetrieveStreamWithResponse(\n"
                                + "        BinaryData.fromObject(retrievalRequest), requestOptions),\n"
                                + "        KnowledgeBaseRetrievalStreamEventConverter::convert,\n"
                                + "        event -> event.getData().isTerminal(), listener);\n"
                                + "}\n")
                        .asMethodDeclaration();
                method.setJavadocComment(
                    "Retrieves relevant data from backing stores and streams progress and results as server-sent "
                        + "events.\n\n"
                        + "If received, the terminal {@code error} or {@code response.completed} event is delivered "
                        + "before {@link ServerSentEventListener#onClose()} is invoked. End-of-stream without a "
                        + "terminal event closes normally. Transport and decoding failures are reported through "
                        + "{@link ServerSentEventListener#onError(Throwable)}. The client does not reconnect "
                        + "automatically.\n\n"
                        + "@param retrievalRequest The retrieval request to process.\n"
                        + "@param listener The listener that receives events and lifecycle notifications.");
                clazz.addMember(method);

                MethodDeclaration methodWithAuthorizationHeaders
                    = StaticJavaParser
                        .parseBodyDeclaration("@Generated\n"
                            + "public void retrieveStream(KnowledgeBaseRetrievalOptions retrievalRequest,\n"
                                + "    String querySourceAuthorization, String queryWorkIQSourceAuthorization,\n"
                                + "    ServerSentEventListener<KnowledgeBaseRetrievalStreamEvent> listener) {\n"
                                + "    RequestOptions requestOptions = new RequestOptions();\n"
                                + "    if (querySourceAuthorization != null) {\n"
                                + "        requestOptions.setHeader(\n"
                                + "            HttpHeaderName.fromString(\"x-ms-query-source-authorization\"),\n"
                                + "            querySourceAuthorization);\n"
                                + "    }\n"
                                + "    if (queryWorkIQSourceAuthorization != null) {\n"
                                + "        requestOptions.setHeader(\n"
                                + "            HttpHeaderName.fromString(\"x-ms-query-work-iq-source-authorization\"),\n"
                                + "            queryWorkIQSourceAuthorization);\n"
                                + "    }\n"
                                + "    ServerSentEventStreams.listen(hiddenGeneratedRetrieveStreamWithResponse(\n"
                                + "        BinaryData.fromObject(retrievalRequest), requestOptions),\n"
                                + "        KnowledgeBaseRetrievalStreamEventConverter::convert,\n"
                                + "        event -> event.getData().isTerminal(), listener);\n" + "}\n")
                        .asMethodDeclaration();
                methodWithAuthorizationHeaders.setJavadocComment(
                    "Retrieves relevant data from backing stores and streams progress and results as server-sent "
                        + "events.\n\n"
                        + "If received, the terminal {@code error} or {@code response.completed} event is delivered "
                        + "before {@link ServerSentEventListener#onClose()} is invoked. End-of-stream without a "
                        + "terminal event closes normally. Transport and decoding failures are reported through "
                        + "{@link ServerSentEventListener#onError(Throwable)}. The client does not reconnect "
                        + "automatically.\n\n"
                        + "@param retrievalRequest The retrieval request to process.\n"
                        + "@param querySourceAuthorization Token identifying the user for which the query is being "
                        + "executed. This token is used to enforce security restrictions on documents.\n"
                        + "@param queryWorkIQSourceAuthorization User assertion token for a customer-owned Entra app "
                        + "registration configured on a Work IQ knowledge source. Used for on-behalf-of "
                        + "authentication to the Work IQ API.\n"
                        + "@param listener The listener that receives events and lifecycle notifications.");
                clazz.addMember(methodWithAuthorizationHeaders);
            }));
    }

    private static String baseEventSource() {
        return header("com.azure.search.documents.knowledgebases.models")
            + "import com.azure.core.annotation.Generated;\n\n" + "/**\n"
            + " * Base type for events emitted by a streaming knowledge base retrieval.\n" + " */\n"
            + "public abstract class KnowledgeBaseRetrievalStreamEvent {\n" + "    @Generated\n"
            + "    private final String eventName;\n\n" + "    /**\n" + "     * Creates a stream event.\n" + "     *\n"
            + "     * @param eventName The server-sent event name.\n" + "     */\n"
            + "    @Generated\n" + "    protected KnowledgeBaseRetrievalStreamEvent(String eventName) {\n"
            + "        this.eventName = eventName;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the server-sent event name.\n" + "     *\n" + "     * @return The event name.\n"
            + "     */\n" + "    @Generated\n" + "    public final String getEventName() {\n"
            + "        return eventName;\n" + "    }\n\n"
            + "    /**\n" + "     * Gets whether this event terminates the retrieval stream.\n" + "     *\n"
            + "     * @return {@code true} if this is a terminal event; otherwise {@code false}.\n" + "     */\n"
            + "    @Generated\n" + "    public boolean isTerminal() {\n" + "        return false;\n" + "    }\n"
            + "}\n";
    }


    private static String unknownEventSource() {
        return header("com.azure.search.documents.knowledgebases.models")
            + "import com.azure.core.annotation.Generated;\n" + "import com.azure.core.annotation.Immutable;\n\n"
            + "/**\n"
            + " * Represents a knowledge base retrieval stream event that is not recognized by this SDK version.\n"
            + " */\n" + "@Immutable\n" + "public final class UnknownKnowledgeBaseRetrievalStreamEvent\n"
            + "    extends KnowledgeBaseRetrievalStreamEvent {\n" + "    @Generated\n"
            + "    private final String data;\n\n" + "    /**\n"
            + "     * Creates an unknown stream event.\n" + "     *\n"
            + "     * @param eventName The server-sent event name.\n"
            + "     * @param data The raw server-sent event data.\n" + "     */\n"
            + "    @Generated\n" + "    public UnknownKnowledgeBaseRetrievalStreamEvent(String eventName, String data) {\n"
            + "        super(eventName);\n" + "        this.data = data;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the raw server-sent event data.\n" + "     *\n" + "     * @return The raw event data.\n"
            + "     */\n" + "    @Generated\n" + "    public String getData() {\n" + "        return data;\n"
            + "    }\n" + "}\n";
    }


    private static String wrapperSource(String className, String payloadType, String eventName, boolean terminal,
        boolean listPayload) {
        String valueType = listPayload ? "List<" + payloadType + ">" : payloadType;
        String listImport = listPayload ? "import java.util.List;\n" : "";
        String toJson = listPayload
            ? "        return jsonWriter.writeArray(value, (writer, item) -> item.toJson(writer));\n"
            : "        return value.toJson(jsonWriter);\n";
        String fromJson = listPayload
            ? "        return new " + className + "(jsonReader.readArray(reader -> " + payloadType
                + ".fromJson(reader)));\n"
            : "        return new " + className + "(" + payloadType + ".fromJson(jsonReader));\n";
        String terminalOverride = terminal
            ? "\n    @Generated\n" + "    @Override\n" + "    public boolean isTerminal() {\n"
                + "        return true;\n" + "    }\n"
            : "";

        return header("com.azure.search.documents.knowledgebases.models")
            + "import com.azure.core.annotation.Generated;\n" + "import com.azure.core.annotation.Immutable;\n"
            + "import com.azure.json.JsonReader;\n"
            + "import com.azure.json.JsonSerializable;\n" + "import com.azure.json.JsonWriter;\n\n"
            + "import java.io.IOException;\n" + listImport + "\n" + "/**\n" + " * Represents the {@code " + eventName
            + "} knowledge base retrieval stream event.\n" + " */\n" + "@Immutable\n" + "public final class " + className
            + " extends KnowledgeBaseRetrievalStreamEvent\n" + "    implements JsonSerializable<" + className + "> {\n"
            + "    @Generated\n" + "    private final " + valueType + " value;\n\n" + "    /**\n"
            + "     * Creates an event wrapper.\n" + "     *\n" + "     * @param value The event payload.\n"
            + "     */\n" + "    @Generated\n" + "    public " + className + "("
            + valueType + " value) {\n" + "        super(\"" + eventName + "\");\n" + "        this.value = value;\n"
            + "    }\n\n" + "    /**\n"
            + "     * Gets the event payload.\n" + "     *\n" + "     * @return The event payload.\n" + "     */\n"
            + "    @Generated\n" + "    public " + valueType + " getValue() {\n" + "        return value;\n"
            + "    }\n\n" + terminalOverride + "\n" + "    @Generated\n" + "    @Override\n"
            + "    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {\n" + toJson + "    }\n\n"
            + "    /**\n" + "     * Reads an event wrapper from JSON.\n" + "     *\n"
            + "     * @param jsonReader The reader to read from.\n" + "     * @return The parsed event wrapper.\n"
            + "     * @throws IOException If the event payload cannot be read.\n" + "     */\n" + "    @Generated\n"
            + "    public static " + className + " fromJson(JsonReader jsonReader) throws IOException {\n" + fromJson
            + "    }\n" + "}\n";
    }


    private static String header(String packageName) {
        return "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
            + "// Licensed under the MIT License.\n\n" + "package " + packageName + ";\n\n";
    }


    // Adds SearchAudience handling to generated builders. This is a temporary fix until
    // https://github.com/microsoft/typespec/issues/9458 is addressed.
    private static void addSearchAudienceScopeHandling(ClassCustomization customization, Logger logger) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            // Make sure 'DEFAULT_SCOPES' exists before adding instance level 'scopes'
            if (clazz.getMembers()
                .stream()
                .noneMatch(declaration -> declaration.isFieldDeclaration()
                    && "DEFAULT_SCOPES".equals(declaration.asFieldDeclaration().getVariable(0).getNameAsString()))) {
                logger.info(
                    "Client builder didn't contain field 'DEFAULT_SCOPES', skipping adding support for SearchAudience");
                return;
            }

            // Add mutable instance 'String[] scopes' with an initialized value of 'DEFAULT_SCOPES'. Also, add the
            // Generated annotation so this will get cleaned up automatically in the future when the TypeSpec issue is
            // resolved.
            clazz.addMember(new FieldDeclaration().setModifiers(Modifier.Keyword.PRIVATE)
                .addMarkerAnnotation("Generated")
                .addVariable(
                    new VariableDeclarator().setName("scopes").setType("String[]").setInitializer("DEFAULT_SCOPES")));

            // Get the 'createHttpPipeline' method and change the 'BearerTokenAuthenticationPolicy' to use 'scopes'
            // instead of 'DEFAULT_SCOPES' when creating the object.
            clazz.getMethodsByName("createHttpPipeline")
                .forEach(method -> method.getBody()
                    .ifPresent(body -> method
                        .setBody(StaticJavaParser.parseBlock(body.toString().replace("DEFAULT_SCOPES", "scopes")))));
        }));
    }

    // At the time this was added, Java TypeSpec generation doesn't support partial update behavior (inline manual
    // modifications to generated files), so this adds back older service versions in a regeneration safe way.
    private static void includeOldApiVersions(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getEnumByName(customization.getClassName()).ifPresent(enumDeclaration -> {
            NodeList<EnumConstantDeclaration> entries = enumDeclaration.getEntries();
            for (String version : Arrays.asList("2026-04-01", "2025-09-01",
                "2024-07-01", "2023-11-01", "2020-06-30")) {
                String enumName = ("V" + version.replace("-", "_"));
                enumName = enumName.toUpperCase(java.util.Locale.ROOT);
                final String name = enumName;
                if (entries.stream().noneMatch(entry -> entry.getNameAsString().equals(name))) {
                    entries.add(0, new EnumConstantDeclaration(enumName).addArgument(new StringLiteralExpr(version))
                        .setJavadocComment("Enum value " + version + "."));
                }
            }

            enumDeclaration.setEntries(entries);
        }));
    }

    private static void removeOldPreviewApiVersions(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getEnumByName(customization.getClassName())
            .ifPresent(enumDeclaration -> enumDeclaration.getEntries()
                .removeIf(entry -> Arrays.asList("V2025_11_01_PREVIEW", "V2026_05_01_PREVIEW", "V2026_08_01_PREVIEW")
                    .contains(entry.getNameAsString()))));
    }

    // At the time this was added, Java TypeSpec for Azure-type generation doesn't use 'T' in WithResponse APIs, which
    // we want, so hide all the WithResponse APIs using BinaryData in the specified class and manually add 'T' APIs.
    private static void hideWithResponseBinaryDataApis(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName())
            .ifPresent(clazz -> clazz.getMethods().forEach(method -> {
                if (!method.isPublic() || !method.isAnnotationPresent("Generated")) {
                    // Method either isn't public or isn't Generated, skip deeper inspection.
                    return;
                }

                boolean returnsBinaryData = hasBinaryDataInType(method.getType());
                boolean acceptsBinaryData
                    = method.getParameters().stream().anyMatch(param -> hasBinaryDataInType(param.getType()));

                // Only hide methods that return BinaryData or accept BinaryData in WithResponse methods.
                // Convenience methods that accept BinaryData as input (e.g., file upload) should remain public.
                boolean isWithResponse = method.getNameAsString().contains("WithResponse");
                if (returnsBinaryData || (acceptsBinaryData && isWithResponse)) {
                    String methodName = method.getNameAsString();
                    String newMethodName
                        = "hiddenGenerated" + Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
                    method.setModifiers().setName(newMethodName);

                    String returnTypeName = method.getType().toString();
                    if (returnTypeName.contains("PagedIterable")) {
                        // PagedIterable generation behaves differently and will break with the logic below.
                        return;
                    }

                    clazz.getMethodsByName(methodName.replace("WithResponse", "")).forEach(nonWithResponse -> {
                        String body = nonWithResponse.getBody().map(BlockStmt::toString).get();
                        body = body.replace(methodName, newMethodName);
                        nonWithResponse.setBody(StaticJavaParser.parseBlock(body));
                    });
                }
            })));
    }

    private static boolean hasBinaryDataInType(Type type) {
        return type.toString().contains("BinaryData");
    }

    private static void repairAsyncSynonymMapsConvenienceMethod(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> clazz
            .getMethodsByName("getSynonymMaps")
            .stream()
            .filter(method -> method.getParameters().isEmpty() && method.isAnnotationPresent("Generated"))
            .findFirst()
            .ifPresent(method -> method.setBody(
                StaticJavaParser.parseBlock("{ return getSynonymMaps(null, null, null, null); }")))));
    }

    // Removes GET equivalents of POST APIs in SearchClient and SearchAsyncClient as we never plan to expose those.
    private static void removeGetApis(ClassCustomization customization) {
        List<String> methodPrefixesToRemove = Arrays.asList("searchGet", "suggestGet", "autocompleteGet");
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName())
            .ifPresent(clazz -> clazz.getMethods().forEach(method -> {
                String methodName = method.getNameAsString();
                if (methodPrefixesToRemove.stream().anyMatch(methodName::startsWith)) {
                    method.remove();
                }
            })));
    }

    // @@access on model properties is not supported by the Java TypeSpec emitter — it only works on whole models and
    // operations. This customization makes getNextLink() and getNextPageParameters() package-private since they are
    // internal continuation details not meant for public consumption.
    private static void hideSearchDocumentsResultInternalProperties(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            for (String methodName : Arrays.asList("getNextLink", "getNextPageParameters")) {
                clazz.getMethodsByName(methodName).forEach(MethodDeclaration::setModifiers);
            }
        }));
    }

    // SearchResourceEncryptionKey has keyName and vaultUrl as required (final) fields, but when
    // isServiceLevelKey is true, they are not needed. This adds a no-arg constructor and makes those fields non-final.
    private static void addNoArgConstructorToEncryptionKey(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            // Make keyName and vaultUrl non-final
            clazz.getFieldByName("keyName").ifPresent(field -> field.setModifiers(Modifier.Keyword.PRIVATE));
            clazz.getFieldByName("vaultUrl").ifPresent(field -> field.setModifiers(Modifier.Keyword.PRIVATE));

            // Add no-arg constructor
            clazz.addMember(StaticJavaParser.parseBodyDeclaration("/**\n"
                + " * Creates an instance of SearchResourceEncryptionKey class. Used when isServiceLevelKey is\n"
                + " * set to true, in which case keyName and vaultUrl are not required.\n" + " */\n"
                + "public SearchResourceEncryptionKey() {\n" + "    this.keyName = null;\n"
                + "    this.vaultUrl = null;\n" + "}\n"));
        }));
    }

    // Adds public convenience methods to SearchIndexAsyncClient for knowledge base and knowledge source
    // createOrUpdate operations. The sync client has equivalent hand-written wrappers, but the async client
    // only has package-private generated convenience methods after hideWithResponseBinaryDataApis runs.
    private static void addAsyncKnowledgeBaseConvenienceMethods(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            // Add: public Mono<KnowledgeBase> createOrUpdateKnowledgeBase(KnowledgeBase knowledgeBase)
            MethodDeclaration createOrUpdateKB = StaticJavaParser
                .parseBodyDeclaration("@ServiceMethod(returns = ReturnType.SINGLE)\n"
                    + "public Mono<KnowledgeBase> createOrUpdateKnowledgeBase(KnowledgeBase knowledgeBase) {\n"
                    + "    return createOrUpdateKnowledgeBase(knowledgeBase.getName(), knowledgeBase);\n" + "}\n")
                .asMethodDeclaration();
            createOrUpdateKB
                .setJavadocComment("Creates a new knowledge base or updates a knowledge base if it already exists.\n"
                    + "\n" + "@param knowledgeBase The definition of the knowledge base to create or update.\n"
                    + "@return the knowledge base that was created or updated.");
            clazz.addMember(createOrUpdateKB);

            // Add: public Mono<Response<KnowledgeBase>> createOrUpdateKnowledgeBaseWithResponse(
            //          KnowledgeBase knowledgeBase, RequestOptions requestOptions)
            MethodDeclaration createOrUpdateKBWithResponse
                = StaticJavaParser.parseBodyDeclaration("@ServiceMethod(returns = ReturnType.SINGLE)\n"
                    + "public Mono<Response<KnowledgeBase>> createOrUpdateKnowledgeBaseWithResponse("
                    + "KnowledgeBase knowledgeBase, RequestOptions requestOptions) {\n"
                    + "    return mapResponse(this.serviceClient.createOrUpdateKnowledgeBaseWithResponseAsync("
                    + "knowledgeBase.getName(), BinaryData.fromObject(knowledgeBase), requestOptions), "
                    + "KnowledgeBase.class);\n" + "}\n").asMethodDeclaration();
            createOrUpdateKBWithResponse
                .setJavadocComment("Creates a new knowledge base or updates a knowledge base if it already exists.\n"
                    + "\n" + "@param knowledgeBase The definition of the knowledge base to create or update.\n"
                    + "@param requestOptions The options to configure the HTTP request before HTTP client sends it.\n"
                    + "@return the knowledge base that was created or updated along with {@link Response}.");
            clazz.addMember(createOrUpdateKBWithResponse);

            // Add: public Mono<Response<KnowledgeSource>> createOrUpdateKnowledgeSourceWithResponse(
            //          KnowledgeSource knowledgeSource, RequestOptions requestOptions)
            MethodDeclaration createOrUpdateKSWithResponse
                = StaticJavaParser.parseBodyDeclaration("@ServiceMethod(returns = ReturnType.SINGLE)\n"
                    + "public Mono<Response<KnowledgeSource>> createOrUpdateKnowledgeSourceWithResponse("
                    + "KnowledgeSource knowledgeSource, RequestOptions requestOptions) {\n"
                    + "    return mapResponse(this.serviceClient.createOrUpdateKnowledgeSourceWithResponseAsync("
                    + "knowledgeSource.getName(), BinaryData.fromObject(knowledgeSource), requestOptions), "
                    + "KnowledgeSource.class);\n" + "}\n").asMethodDeclaration();
            createOrUpdateKSWithResponse.setJavadocComment(
                "Creates a new knowledge source or updates a knowledge source if it already exists.\n" + "\n"
                    + "@param knowledgeSource The definition of the knowledge source to create or update.\n"
                    + "@param requestOptions The options to configure the HTTP request before HTTP client sends it.\n"
                    + "@return the knowledge source that was created or updated along with {@link Response}.");
            clazz.addMember(createOrUpdateKSWithResponse);
        }));
    }

}
