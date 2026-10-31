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
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Contains customizations for Azure AI Search code generation.
 */
public class SearchCustomizations extends Customization {
    /**
     * Creates the Azure AI Search code generation customizations.
     */
    public SearchCustomizations() {
    }

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
        removePreviewApiVersions(serviceVersion);

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

        customizeKnowledgeBaseRetrievalStream(libraryCustomization, logger);
        addQuerySourceAuthorizationOption(
            libraryCustomization.getPackage("com.azure.search.documents.knowledgebases.models")
                .getClass("KnowledgeBaseRetrievalOptions"));
        forwardQuerySourceAuthorizationOption(knowledge.getClass("KnowledgeBaseRetrievalClient"));
        forwardQuerySourceAuthorizationOption(knowledge.getClass("KnowledgeBaseRetrievalAsyncClient"));
        addQuerySourceAuthorizationToRetrieveImpl(
            libraryCustomization.getPackage("com.azure.search.documents.implementation")
                .getClass("KnowledgeBaseRetrievalClientImpl"));

        customizeResponseCompletedStatusCode(libraryCustomization);
        customizeKnowledgeSourceStatusDurationParsing(
            libraryCustomization.getPackage("com.azure.search.documents.knowledgebases.models")
                .getClass("KnowledgeSourceStatus"));
        for (String className : Arrays.asList("SearchIndexClient", "SearchIndexAsyncClient")) {
            ClassCustomization client = indexes.getClass(className);
            addTypedListingMethod(client, "listIndexStatsSummary", "IndexStatisticsSummary", false);
            addTypedListingMethod(client, "listKnowledgeSourceFiles", "KnowledgeSourceFile", true);
        }

        // After hiding BinaryData protocol methods, add typed public convenience wrappers on the async client
        // that mirror what the sync client already has as hand-written methods.
        addAsyncKnowledgeBaseConvenienceMethods(indexes.getClass("SearchIndexAsyncClient"));

        // The JSON property remains docUrl; only the Java getter name changes.
        libraryCustomization.getPackage("com.azure.search.documents.knowledgebases.models")
            .getClass("KnowledgeBaseIndexedSqlReference")
            .customizeAst(ast -> {
                List<MethodDeclaration> getters = ast.getClassByName("KnowledgeBaseIndexedSqlReference")
                    .orElseThrow(() -> new IllegalStateException("Class KnowledgeBaseIndexedSqlReference not found."))
                    .getMethodsByName("getDocUrl")
                    .stream()
                    .filter(method -> method.getParameters().isEmpty())
                    .collect(java.util.stream.Collectors.toList());
                if (getters.size() != 1) {
                    throw new IllegalStateException("Expected exactly one zero-argument getDocUrl() method in "
                        + "KnowledgeBaseIndexedSqlReference but found " + getters.size() + ".");
                }
                getters.get(0).setName("getDocumentUrl");
            });
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

    // The TypeSpec Java emitter compiles and loads only this configured customization class. Keep the
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
        customization.getRawEditor()
            .replaceFile(MODELS_PATH + "KnowledgeBaseRetrievalStreamEvent.java", baseEventSource());
        customization.getRawEditor()
            .replaceFile(MODELS_PATH + "KnowledgeBaseRetrievalStartedStreamEvent.java",
                wrapperSource("KnowledgeBaseRetrievalStartedStreamEvent", "KnowledgeBaseRetrievalStartedEvent",
                    "retrieval.started", false, false));
        customization.getRawEditor()
            .replaceFile(MODELS_PATH + "KnowledgeBaseActivityStartedStreamEvent.java",
                wrapperSource("KnowledgeBaseActivityStartedStreamEvent", "KnowledgeBaseActivityStartedEvent",
                    "activity.started", false, false));
        customization.getRawEditor()
            .replaceFile(MODELS_PATH + "KnowledgeBaseActivityCompletedStreamEvent.java",
                wrapperSource("KnowledgeBaseActivityCompletedStreamEvent", "KnowledgeBaseActivityRecord",
                    "activity.completed", false, false));
        customization.getRawEditor()
            .replaceFile(MODELS_PATH + "KnowledgeBaseAnswerCompletedStreamEvent.java",
                wrapperSource("KnowledgeBaseAnswerCompletedStreamEvent", "KnowledgeBaseAnswerCompletedEvent",
                    "answer.completed", false, false));
        customization.getRawEditor()
            .replaceFile(MODELS_PATH + "KnowledgeBaseReferencesCompletedStreamEvent.java",
                wrapperSource("KnowledgeBaseReferencesCompletedStreamEvent", "KnowledgeBaseReference",
                    "references.completed", false, true));
        customization.getRawEditor()
            .replaceFile(MODELS_PATH + "KnowledgeBaseErrorStreamEvent.java",
                wrapperSource("KnowledgeBaseErrorStreamEvent", "KnowledgeBaseStreamErrorEvent", "error", true, false));
        customization.getRawEditor()
            .replaceFile(MODELS_PATH + "KnowledgeBaseResponseCompletedStreamEvent.java",
                wrapperSource("KnowledgeBaseResponseCompletedStreamEvent", "KnowledgeBaseResponseCompletedEvent",
                    "response.completed", true, false));
    }

    private static void customizeKnowledgeSourceStatusDurationParsing(ClassCustomization customization) {
        customization.customizeAst(ast -> ast
            .addImport("com.azure.search.documents.knowledgebases.implementation.KnowledgeSourceDurationParser")
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
            .addImport("com.azure.search.documents.models.implementation.sse.ServerSentEventStreams")
            .addImport(
                "com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter")
            .addImport("com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent")
            .addImport("reactor.core.publisher.Flux")
            .getClassByName(customization.getClassName())
            .ifPresent(clazz -> {
                clazz.getMethodsByName("retrieveStream").forEach(MethodDeclaration::remove);
                MethodDeclaration method = StaticJavaParser
                    .parseBodyDeclaration(
                        "@Generated\n" + "public Flux<KnowledgeBaseRetrievalStreamEvent> retrieveStream("
                            + "KnowledgeBaseRetrievalOptions retrievalRequest) {\n"
                            + "    RequestOptions requestOptions = new RequestOptions();\n"
                            + "    return hiddenGeneratedRetrieveStreamWithResponse("
                            + "BinaryData.fromObject(retrievalRequest), requestOptions)\n"
                            + "        .flatMapMany(response -> ServerSentEventStreams.toFlux(response,\n"
                            + "            KnowledgeBaseRetrievalStreamEventConverter::convert,\n"
                            + "            KnowledgeBaseRetrievalStreamEvent::isTerminal));\n" + "}\n")
                    .asMethodDeclaration();
                method.setJavadocComment(
                    "Retrieves relevant data from backing stores and streams progress and results as server-sent "
                        + "events.\n\n" + streamEventDocumentation()
                        + "If received, the terminal {@code error} or {@code response.completed} event is emitted "
                        + "before the stream completes. End-of-stream without a terminal event completes normally. "
                        + "Transport and decoding failures are propagated through the reactive error path. The client "
                        + "does not reconnect automatically.\n\n"
                        + "@param retrievalRequest The retrieval request to process.\n"
                        + "@return A stream of typed knowledge base retrieval events.");
                clazz.addMember(method);

                MethodDeclaration methodWithAuthorizationHeaders = StaticJavaParser
                    .parseBodyDeclaration(
                        "@Generated\n" + "public Flux<KnowledgeBaseRetrievalStreamEvent> retrieveStream("
                            + "KnowledgeBaseRetrievalOptions retrievalRequest, String querySourceAuthorization) {\n"
                            + "    RequestOptions requestOptions = new RequestOptions();\n"
                            + "    if (querySourceAuthorization != null) {\n" + "        requestOptions.setHeader(\n"
                            + "            HttpHeaderName.fromString(\"x-ms-query-source-authorization\"),\n"
                            + "            querySourceAuthorization);\n" + "    }\n"
                            + "    return hiddenGeneratedRetrieveStreamWithResponse("
                            + "BinaryData.fromObject(retrievalRequest), requestOptions)\n"
                            + "        .flatMapMany(response -> ServerSentEventStreams.toFlux(response,\n"
                            + "            KnowledgeBaseRetrievalStreamEventConverter::convert,\n"
                            + "            KnowledgeBaseRetrievalStreamEvent::isTerminal));\n" + "}\n")
                    .asMethodDeclaration();
                methodWithAuthorizationHeaders.setJavadocComment(
                    "Retrieves relevant data from backing stores and streams progress and results as server-sent "
                        + "events.\n\n" + streamEventDocumentation()
                        + "If received, the terminal {@code error} or {@code response.completed} event is emitted "
                        + "before the stream completes. End-of-stream without a terminal event completes normally. "
                        + "Transport and decoding failures are propagated through the reactive error path. The client "
                        + "does not reconnect automatically.\n\n"
                        + "@param retrievalRequest The retrieval request to process.\n"
                        + "@param querySourceAuthorization Token identifying the user for which the query is being "
                        + "executed. This token is used to enforce security restrictions on documents.\n"
                        + "@return A stream of typed knowledge base retrieval events.");
                clazz.addMember(methodWithAuthorizationHeaders);
            }));
    }

    private static void addSyncRetrieveStream(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.addImport("com.azure.core.http.HttpHeaderName")
            .addImport("com.azure.core.util.CloseableIterableStream")
            .addImport("com.azure.search.documents.models.implementation.sse.ServerSentEventStreams")
            .addImport(
                "com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter")
            .addImport("com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent")
            .getClassByName(customization.getClassName())
            .ifPresent(clazz -> {
                clazz.getMethodsByName("retrieveStream").forEach(MethodDeclaration::remove);
                MethodDeclaration method = StaticJavaParser.parseBodyDeclaration("@Generated\n"
                    + "public CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> retrieveStream("
                    + "KnowledgeBaseRetrievalOptions retrievalRequest) {\n"
                    + "    RequestOptions requestOptions = new RequestOptions();\n"
                    + "    return ServerSentEventStreams.toIterableStream(hiddenGeneratedRetrieveStreamWithResponse(\n"
                    + "        BinaryData.fromObject(retrievalRequest), requestOptions),\n"
                    + "        KnowledgeBaseRetrievalStreamEventConverter::convert,\n"
                    + "        KnowledgeBaseRetrievalStreamEvent::isTerminal);\n" + "}\n").asMethodDeclaration();
                method.setJavadocComment(
                    "Retrieves relevant data from backing stores and streams progress and results as server-sent "
                        + "events.\n\n" + streamEventDocumentation()
                        + "Events are decoded lazily by a single iterator. Use try-with-resources to close the "
                        + "stream when iteration ends early. The response is also closed on end-of-stream, a "
                        + "terminal event, or an iteration failure. Closing the stream is idempotent and may throw "
                        + "{@link java.io.IOException}.\n\n"
                        + "If received, the terminal {@code error} or {@code response.completed} event is emitted "
                        + "before iteration ends. A failure while closing after a terminal event is reported by the "
                        + "next iterator access or explicit close, after the terminal event is delivered. "
                        + "End-of-stream without a terminal event completes normally. "
                        + "Transport and decoding failures are thrown during iteration. The client does not "
                        + "reconnect automatically.\n\n" + "@param retrievalRequest The retrieval request to process.\n"
                        + "@return A closeable stream of typed knowledge base retrieval events.");
                clazz.addMember(method);

                MethodDeclaration methodWithAuthorizationHeaders = StaticJavaParser.parseBodyDeclaration("@Generated\n"
                    + "public CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> retrieveStream("
                    + "KnowledgeBaseRetrievalOptions retrievalRequest, String querySourceAuthorization) {\n"
                    + "    RequestOptions requestOptions = new RequestOptions();\n"
                    + "    if (querySourceAuthorization != null) {\n" + "        requestOptions.setHeader(\n"
                    + "            HttpHeaderName.fromString(\"x-ms-query-source-authorization\"),\n"
                    + "            querySourceAuthorization);\n" + "    }\n"
                    + "    return ServerSentEventStreams.toIterableStream(hiddenGeneratedRetrieveStreamWithResponse(\n"
                    + "        BinaryData.fromObject(retrievalRequest), requestOptions),\n"
                    + "        KnowledgeBaseRetrievalStreamEventConverter::convert,\n"
                    + "        KnowledgeBaseRetrievalStreamEvent::isTerminal);\n" + "}\n").asMethodDeclaration();
                methodWithAuthorizationHeaders.setJavadocComment(
                    "Retrieves relevant data from backing stores and streams progress and results as server-sent "
                        + "events.\n\n" + streamEventDocumentation()
                        + "Events are decoded lazily by a single iterator. Use try-with-resources to close the "
                        + "stream when iteration ends early. The response is also closed on end-of-stream, a "
                        + "terminal event, or an iteration failure. Closing the stream is idempotent and may throw "
                        + "{@link java.io.IOException}.\n\n"
                        + "If received, the terminal {@code error} or {@code response.completed} event is emitted "
                        + "before iteration ends. A failure while closing after a terminal event is reported by the "
                        + "next iterator access or explicit close, after the terminal event is delivered. "
                        + "End-of-stream without a terminal event completes normally. "
                        + "Transport and decoding failures are thrown during iteration. The client does not "
                        + "reconnect automatically.\n\n" + "@param retrievalRequest The retrieval request to process.\n"
                        + "@param querySourceAuthorization Token identifying the user for which the query is being "
                        + "executed. This token is used to enforce security restrictions on documents.\n"
                        + "@return A closeable stream of typed knowledge base retrieval events.");
                clazz.addMember(methodWithAuthorizationHeaders);
            }));
    }

    private static final String KNOWLEDGE_MODELS_PACKAGE = "com.azure.search.documents.knowledgebases.models";

    // The status code is exposed as a nullable Integer so unknown values round trip without a public wrapper type.
    private static void customizeResponseCompletedStatusCode(LibraryCustomization libraryCustomization) {
        String className = "KnowledgeBaseResponseCompletedEvent";
        libraryCustomization.getPackage(KNOWLEDGE_MODELS_PACKAGE)
            .getClass(className)
            .customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
                clazz.getFieldByName("statusCode")
                    .orElseThrow(() -> new IllegalStateException("statusCode field not found."))
                    .getVariable(0)
                    .setType("Integer");
                clazz.getConstructors().get(0).getParameter(0).setType("Integer");
                clazz.getMethodsByName("getStatusCode").get(0).setType("Integer");

                MethodDeclaration toJson = clazz.getMethodsByName("toJson").get(0);
                String toJsonBody = toJson.getBody().orElseThrow(() -> new IllegalStateException("No body.")).toString();
                String generatedWrite = "this.statusCode == null ? null : this.statusCode.getValue()";
                if (!toJsonBody.contains(generatedWrite)) {
                    throw new IllegalStateException("toJson no longer uses the expected generated statusCode write.");
                }
                toJson.setBody(StaticJavaParser.parseBlock(toJsonBody.replace(generatedWrite, "this.statusCode")));

                MethodDeclaration fromJson = clazz.getMethodsByName("fromJson").get(0);
                String fromJsonBody
                    = fromJson.getBody().orElseThrow(() -> new IllegalStateException("No body.")).toString();
                String generatedDeclaration = "KnowledgeBaseRetrievalStatusCode statusCode = null;";
                String generatedRead = "statusCode = KnowledgeBaseRetrievalStatusCode.fromValue(reader.getInt());";
                if (!fromJsonBody.contains(generatedDeclaration) || !fromJsonBody.contains(generatedRead)) {
                    throw new IllegalStateException("fromJson no longer uses the expected generated statusCode read.");
                }
                fromJson.setBody(StaticJavaParser.parseBlock(
                    fromJsonBody.replace(generatedDeclaration, "Integer statusCode = null;")
                        .replace(generatedRead, "statusCode = reader.getNullable(JsonReader::getInt);")));

                MethodDeclaration getStatusCode = clazz.getMethodsByName("getStatusCode").get(0);
                getStatusCode.setJavadocComment("Get the statusCode property: The semantic HTTP status of the completed "
                    + "retrieval, not the actual HTTP transport response status. The service wire schema requires "
                    + "this value, but the SDK treats it as nullable: it returns null when the status code is missing "
                    + "or JSON null, and unknown values round trip as plain integers.\n\n"
                    + "@return the statusCode value, or null if it was not provided.");
            }));

        libraryCustomization.getRawEditor().removeFile(MODELS_PATH + "KnowledgeBaseRetrievalStatusCode.java");
    }

    // Client-only transport metadata: stored on the options but never serialized to or read from JSON.
    private static void addQuerySourceAuthorizationOption(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            clazz.getFieldByName("querySourceAuthorization").ifPresent(FieldDeclaration::remove);
            clazz.getMethodsByName("getQuerySourceAuthorization").forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("setQuerySourceAuthorization").forEach(MethodDeclaration::remove);

            FieldDeclaration field = clazz.addField("String", "querySourceAuthorization", Modifier.Keyword.PRIVATE);
            field.addMarkerAnnotation("Generated");
            field.setJavadocComment("Token identifying the user for which the query is being executed. Client-only "
                + "value that is sent as the x-ms-query-source-authorization header and is never serialized "
                + "into the request body.");

            MethodDeclaration getter = StaticJavaParser
                .parseBodyDeclaration("@Generated\n"
                    + "public String getQuerySourceAuthorization() {\n    return this.querySourceAuthorization;\n}\n")
                .asMethodDeclaration();
            getter.setJavadocComment("Get the querySourceAuthorization property: Token identifying the user for which "
                + "the query is being executed. This token is used to enforce security restrictions on documents. "
                + "It is only sent as the x-ms-query-source-authorization request header and is never part of the "
                + "request body. A non-null authorization passed explicitly to a retrieve method overrides this "
                + "value; a null explicit authorization falls back to it.\n\n"
                + "@return the querySourceAuthorization value.");
            clazz.addMember(getter);

            MethodDeclaration setter = StaticJavaParser.parseBodyDeclaration("@Generated\n"
                + "public KnowledgeBaseRetrievalOptions setQuerySourceAuthorization(String querySourceAuthorization) {\n"
                    + "    this.querySourceAuthorization = querySourceAuthorization;\n    return this;\n}\n")
                .asMethodDeclaration();
            setter.setJavadocComment("Set the querySourceAuthorization property: Token identifying the user for which "
                + "the query is being executed. This token is used to enforce security restrictions on documents. "
                + "It is only sent as the x-ms-query-source-authorization request header and is never part of the "
                + "request body. A non-null authorization passed explicitly to a retrieve method overrides this "
                + "value; a null explicit authorization falls back to it. An empty string is treated as a value.\n\n"
                + "@param querySourceAuthorization the querySourceAuthorization value to set.\n"
                + "@return the KnowledgeBaseRetrievalOptions object itself.");
            clazz.addMember(setter);
        }));
    }

    // Must run after the stream methods are assembled. A non-null explicit parameter overrides the option, and a null
    // explicit parameter falls back to KnowledgeBaseRetrievalOptions#getQuerySourceAuthorization().
    private static void forwardQuerySourceAuthorizationOption(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            clazz.getMethodsByName("createRetrieveRequestOptions").forEach(MethodDeclaration::remove);
            clazz.addMember(StaticJavaParser.parseBodyDeclaration("@Generated\n"
                + "private static RequestOptions createRetrieveRequestOptions("
                + "KnowledgeBaseRetrievalOptions retrievalRequest,\n" + "    String querySourceAuthorization) {\n"
                + "    RequestOptions requestOptions = new RequestOptions();\n"
                + "    String authorization = querySourceAuthorization != null\n"
                + "        ? querySourceAuthorization\n"
                + "        : (retrievalRequest == null ? null : retrievalRequest.getQuerySourceAuthorization());\n"
                + "    if (authorization != null) {\n"
                + "        requestOptions.setHeader(HttpHeaderName.fromString(\"x-ms-query-source-authorization\"),\n"
                + "            authorization);\n" + "    }\n" + "    return requestOptions;\n" + "}\n"));

            int updated = 0;
            for (MethodDeclaration method : clazz.getMethods()) {
                String name = method.getNameAsString();
                if (!method.isPublic() || !("retrieve".equals(name) || "retrieveStream".equals(name))) {
                    continue;
                }
                boolean hasExplicit = method.getParameters().size() == 2;
                BlockStmt body = method.getBody()
                    .orElseThrow(() -> new IllegalStateException("Retrieve method has no body."));
                body.getStatements().removeIf(SearchCustomizations::isQuerySourceAuthorizationHeaderBlock);
                boolean replaced = false;
                for (int i = 0; i < body.getStatements().size(); i++) {
                    if (body.getStatements().get(i).toString().contains("new RequestOptions()")) {
                        body.getStatements()
                            .set(i, StaticJavaParser.parseStatement("RequestOptions requestOptions = "
                                + "createRetrieveRequestOptions(retrievalRequest, "
                                + (hasExplicit ? "querySourceAuthorization" : "null") + ");"));
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) {
                    throw new IllegalStateException("Could not find the request options in " + name + ".");
                }

                method.getJavadocComment().ifPresent(javadoc -> {
                    String content = javadoc.getContent()
                        .replaceFirst("@param retrievalRequest The retrieval request to process\\.",
                            "@param retrievalRequest The retrieval request to process. If its "
                                + "{@link KnowledgeBaseRetrievalOptions#getQuerySourceAuthorization()} is set, it is "
                                + "sent only as the x-ms-query-source-authorization header and is never serialized "
                                + "into the request body.");
                    if (hasExplicit) {
                        content = content.replaceFirst("security[\\s*]+restrictions[\\s*]+on[\\s*]+documents\\.",
                            "security restrictions on documents. A non-null value overrides "
                                + "{@link KnowledgeBaseRetrievalOptions#getQuerySourceAuthorization()}; null falls "
                                + "back to it, and an empty string is treated as an explicit value.");
                    }
                    javadoc.setContent(content);
                });
                updated++;
            }
            if (updated != 4) {
                throw new IllegalStateException("Expected to update 4 retrieve methods in "
                    + customization.getClassName() + " but updated " + updated + ".");
            }
        }));
    }

    // Adds a querySourceAuthorization header parameter to the internal retrieve REST methods so typed callers can
    // supply the header without mutating or copying the caller's RequestOptions. RestProxy applies the header before
    // RequestOptions callbacks, so a header explicitly set on RequestOptions still takes precedence.
    private static void addQuerySourceAuthorizationToRetrieveImpl(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.addImport("com.azure.core.annotation.Generated");
            com.github.javaparser.ast.body.ClassOrInterfaceDeclaration clazz
                = ast.getClassByName(customization.getClassName())
                    .orElseThrow(() -> new IllegalStateException("Class not found."));

            com.github.javaparser.ast.body.ClassOrInterfaceDeclaration service = clazz.getMembers()
                .stream()
                .filter(member -> member.isClassOrInterfaceDeclaration())
                .map(member -> member.asClassOrInterfaceDeclaration())
                .filter(type -> "KnowledgeBaseRetrievalClientService".equals(type.getNameAsString()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("KnowledgeBaseRetrievalClientService not found."));
            int serviceUpdated = 0;
            for (String name : Arrays.asList("retrieve", "retrieveSync")) {
                for (MethodDeclaration method : service.getMethodsByName(name)) {
                    method.getParameters().removeIf(p -> "querySourceAuthorization".equals(p.getNameAsString()));
                    int index = -1;
                    for (int i = 0; i < method.getParameters().size(); i++) {
                        if ("requestOptions".equals(method.getParameter(i).getNameAsString())) {
                            index = i;
                        }
                    }
                    if (index < 0) {
                        throw new IllegalStateException("requestOptions parameter not found in " + name + ".");
                    }
                    method.getParameters()
                        .add(index, StaticJavaParser.parseParameter(
                            "@HeaderParam(\"x-ms-query-source-authorization\") String querySourceAuthorization"));
                    serviceUpdated++;
                }
            }
            if (serviceUpdated != 2) {
                throw new IllegalStateException(
                    "Expected to update 2 service retrieve methods but updated " + serviceUpdated + ".");
            }

            for (boolean async : new boolean[] { true, false }) {
                String name = async ? "retrieveWithResponseAsync" : "retrieveWithResponse";
                clazz.getMethodsByName(name)
                    .stream()
                    .filter(method -> method.getParameters().size() == 3)
                    .collect(java.util.stream.Collectors.toList())
                    .forEach(MethodDeclaration::remove);
                MethodDeclaration original = clazz.getMethodsByName(name)
                    .stream()
                    .filter(method -> method.getParameters().size() == 2
                        && "BinaryData".equals(method.getParameter(0).getTypeAsString()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(name + " not found."));

                String returnType = original.getTypeAsString();
                String signature = "@Generated\n@ServiceMethod(returns = ReturnType.SINGLE)\n" + "public " + returnType
                    + " " + name + "(BinaryData retrievalRequest, String querySourceAuthorization,\n"
                    + "    RequestOptions requestOptions) {\n"
                    + "    final String accept = \"application/json;odata.metadata=minimal\";\n"
                    + "    final String contentType = \"application/json\";\n";
                String overloadSource;
                String delegate;
                if (async) {
                    overloadSource = signature
                        + "    return FluxUtil.withContext(context -> service.retrieve(this.getEndpoint(),\n"
                        + "        this.getServiceVersion().getVersion(), accept, this.getKnowledgeBaseName(), "
                        + "contentType,\n"
                        + "        retrievalRequest, querySourceAuthorization, requestOptions, context));\n" + "}\n";
                    delegate = "{\n    return retrieveWithResponseAsync(retrievalRequest, null, requestOptions);\n}";
                } else {
                    overloadSource = signature
                        + "    return service.retrieveSync(this.getEndpoint(), this.getServiceVersion().getVersion(),\n"
                        + "        accept, this.getKnowledgeBaseName(), contentType, retrievalRequest,\n"
                        + "        querySourceAuthorization, requestOptions, Context.NONE);\n" + "}\n";
                    delegate = "{\n    return retrieveWithResponse(retrievalRequest, null, requestOptions);\n}";
                }
                MethodDeclaration overload
                    = StaticJavaParser.parseBodyDeclaration(overloadSource).asMethodDeclaration();
                original.getJavadocComment().ifPresent(javadoc -> {
                    String content = javadoc.getContent();
                    String updated = content.replaceFirst("(\\* @param retrievalRequest [^\\n]*\\n)",
                        "$1     * @param querySourceAuthorization Token identifying the user for which the query is "
                            + "being executed. This token is used to enforce security restrictions on documents. A "
                            + "header set explicitly on requestOptions takes precedence.\n");
                    if (updated.equals(content)) {
                        throw new IllegalStateException("Could not locate retrievalRequest javadoc for " + name + ".");
                    }
                    overload.setJavadocComment(updated);
                });
                original.setBody(StaticJavaParser.parseBlock(delegate));
                clazz.addMember(overload);
            }
        });
    }



    // Matches only the generated `if (querySourceAuthorization != null) { requestOptions.setHeader(...); }` block.
    private static boolean isQuerySourceAuthorizationHeaderBlock(Statement statement) {
        return statement.isIfStmt()
            && "querySourceAuthorization != null".equals(statement.asIfStmt().getCondition().toString())
            && statement.toString().contains("x-ms-query-source-authorization");
    }

    private static String streamEventDocumentation() {
        return "Events are polymorphic: see {@link KnowledgeBaseRetrievalStreamEvent} for the known subtypes. "
            + "Use a known subtype's {@code getValue()} to access its typed payload, for example "
            + "{@link com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedStreamEvent"
            + "#getValue()}. " + "Unrecognized events expose only their names through "
            + "{@link KnowledgeBaseRetrievalStreamEvent#getEventName()}; their payloads are not available.\n\n";
    }

    private static String baseEventSource() {
        return header("com.azure.search.documents.knowledgebases.models")
            + "import com.azure.core.annotation.Generated;\n"
            + "import com.azure.search.documents.knowledgebases.implementation."
            + "KnowledgeBaseRetrievalStreamEventConverter;\n" + "import java.util.List;\n\n" + "/**\n"
            + " * Base for polymorphic events emitted by a streaming knowledge base retrieval.\n"
            + " * Known events expose typed payloads through their subtype's {@code getValue()} method.\n"
            + " * Unrecognized events expose only their names through\n"
            + " * {@link #getEventName()}; their payloads are not available and they are not terminal.\n"
            + " * Stream events are envelopes, not JSON payload models; serialize the typed payload instead.\n" + " *\n"
            + " * @see KnowledgeBaseRetrievalStartedStreamEvent\n" + " * @see KnowledgeBaseActivityStartedStreamEvent\n"
            + " * @see KnowledgeBaseActivityCompletedStreamEvent\n"
            + " * @see KnowledgeBaseAnswerCompletedStreamEvent\n"
            + " * @see KnowledgeBaseReferencesCompletedStreamEvent\n" + " * @see KnowledgeBaseErrorStreamEvent\n"
            + " * @see KnowledgeBaseResponseCompletedStreamEvent\n" + " */\n"
            + "public abstract class KnowledgeBaseRetrievalStreamEvent {\n" + "    static {\n"
            + "        KnowledgeBaseRetrievalStreamEventConverter.setConstructorAccessor(\n"
            + "            new KnowledgeBaseRetrievalStreamEventConverter.ConstructorAccessor() {\n"
            + "                @Override\n"
            + "                public KnowledgeBaseRetrievalStreamEvent createEvent(String eventName) {\n"
            + "                    return new KnowledgeBaseRetrievalStreamEvent(eventName) { };\n"
            + "                }\n\n" + "                @Override\n"
            + "                public KnowledgeBaseRetrievalStartedStreamEvent createRetrievalStarted(\n"
            + "                    KnowledgeBaseRetrievalStartedEvent value) {\n"
            + "                    return new KnowledgeBaseRetrievalStartedStreamEvent(value);\n"
            + "                }\n\n" + "                @Override\n"
            + "                public KnowledgeBaseActivityStartedStreamEvent createActivityStarted(\n"
            + "                    KnowledgeBaseActivityStartedEvent value) {\n"
            + "                    return new KnowledgeBaseActivityStartedStreamEvent(value);\n"
            + "                }\n\n" + "                @Override\n"
            + "                public KnowledgeBaseActivityCompletedStreamEvent createActivityCompleted(\n"
            + "                    KnowledgeBaseActivityRecord value) {\n"
            + "                    return new KnowledgeBaseActivityCompletedStreamEvent(value);\n"
            + "                }\n\n" + "                @Override\n"
            + "                public KnowledgeBaseAnswerCompletedStreamEvent createAnswerCompleted(\n"
            + "                    KnowledgeBaseAnswerCompletedEvent value) {\n"
            + "                    return new KnowledgeBaseAnswerCompletedStreamEvent(value);\n"
            + "                }\n\n" + "                @Override\n"
            + "                public KnowledgeBaseReferencesCompletedStreamEvent createReferencesCompleted(\n"
            + "                    List<KnowledgeBaseReference> value) {\n"
            + "                    return new KnowledgeBaseReferencesCompletedStreamEvent(value);\n"
            + "                }\n\n" + "                @Override\n"
            + "                public KnowledgeBaseErrorStreamEvent createError(KnowledgeBaseStreamErrorEvent value) {\n"
            + "                    return new KnowledgeBaseErrorStreamEvent(value);\n" + "                }\n\n"
            + "                @Override\n"
            + "                public KnowledgeBaseResponseCompletedStreamEvent createResponseCompleted(\n"
            + "                    KnowledgeBaseResponseCompletedEvent value) {\n"
            + "                    return new KnowledgeBaseResponseCompletedStreamEvent(value);\n"
            + "                }\n" + "            });\n" + "    }\n\n" + "    @Generated\n"
            + "    private final String eventName;\n\n" + "    /**\n" + "     * Creates a stream event.\n" + "     *\n"
            + "     * @param eventName The server-sent event name.\n" + "     */\n" + "    @Generated\n"
            + "    KnowledgeBaseRetrievalStreamEvent(String eventName) {\n" + "        this.eventName = eventName;\n"
            + "    }\n\n" + "    /**\n" + "     * Gets the server-sent event name.\n" + "     *\n"
            + "     * @return The event name.\n" + "     */\n" + "    @Generated\n"
            + "    public final String getEventName() {\n" + "        return eventName;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets whether this event terminates the retrieval stream.\n" + "     *\n"
            + "     * @return {@code true} if this is a terminal event; otherwise {@code false}.\n" + "     */\n"
            + "    @Generated\n" + "    public boolean isTerminal() {\n" + "        return false;\n" + "    }\n"
            + "}\n";
    }

    private static String wrapperSource(String className, String payloadType, String eventName, boolean terminal,
        boolean listPayload) {
        String valueType = listPayload ? "List<" + payloadType + ">" : payloadType;
        String listImports = listPayload ? "import java.util.List;\n" : "";
        String terminalOverride = terminal
            ? "\n    @Generated\n" + "    @Override\n" + "    public boolean isTerminal() {\n"
                + "        return true;\n" + "    }\n"
            : "";

        return header("com.azure.search.documents.knowledgebases.models")
            + "import com.azure.core.annotation.Generated;\n" + "import com.azure.core.annotation.Immutable;\n"
            + listImports + "\n" + "/**\n" + " * Represents the {@code " + eventName
            + "} knowledge base retrieval stream event.\n" + " * Access the typed payload with {@link #getValue()}.\n"
            + " *\n" + " * @see KnowledgeBaseRetrievalStreamEvent\n" + " */\n" + "@Immutable\n" + "public final class "
            + className + " extends KnowledgeBaseRetrievalStreamEvent {\n" + "    @Generated\n" + "    private final "
            + valueType + " value;\n\n" + "    /**\n" + "     * Creates an event wrapper.\n" + "     *\n"
            + "     * @param value The event payload.\n" + "     */\n" + "    @Generated\n" + "    " + className + "("
            + valueType + " value) {\n" + "        super(\"" + eventName + "\");\n" + "        this.value = value;\n"
            + "    }\n\n" + "    /**\n" + "     * Gets the event payload.\n" + "     *\n"
            + "     * @return The event payload.\n" + "     */\n" + "    @Generated\n" + "    public " + valueType
            + " getValue() {\n" + "        return value;\n" + "    }\n" + terminalOverride + "}\n";
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
            for (String version : Arrays.asList("2025-09-01", "2024-07-01", "2023-11-01", "2020-06-30")) {
                String enumName = ("V" + version.replace("-", "_"));
                if (entries.stream().noneMatch(entry -> enumName.equals(entry.getNameAsString()))) {
                    entries.add(0, new EnumConstantDeclaration(enumName).addArgument(new StringLiteralExpr(version))
                        .setJavadocComment("Enum value " + version + "."));
                }
            }

            enumDeclaration.setEntries(entries);
        }));
    }

    private static void removePreviewApiVersions(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getEnumByName(customization.getClassName())
            .ifPresent(enumDeclaration -> enumDeclaration.getEntries()
                .removeIf(entry -> entry.getNameAsString().endsWith("_PREVIEW"))));
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

    private static void addTypedListingMethod(ClassCustomization customization, String methodName, String itemType,
        boolean hasKnowledgeSourceName) {
        boolean async = customization.getClassName().endsWith("AsyncClient");
        String pagedType = async ? "PagedFlux" : "PagedIterable";
        String parameters
            = hasKnowledgeSourceName ? "String name, RequestOptions requestOptions" : "RequestOptions requestOptions";
        String arguments = hasKnowledgeSourceName ? "name, requestOptions" : "requestOptions";
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            String[] parameterTypes = hasKnowledgeSourceName
                ? new String[] { "String", "RequestOptions" }
                : new String[] { "RequestOptions" };
            clazz.getMethodsBySignature(methodName, parameterTypes).forEach(MethodDeclaration::remove);
            MethodDeclaration method = StaticJavaParser
                .parseMethodDeclaration("@Generated\n@ServiceMethod(returns = ReturnType.COLLECTION)\npublic "
                    + pagedType + "<" + itemType + "> " + methodName + "(" + parameters + ") {\n"
                    + "    return this.serviceClient." + methodName + (async ? "Async" : "") + "(" + arguments + ")\n"
                    + "        .mapPage(binaryData -> binaryData.toObject(" + itemType + ".class));\n}");
            method.setJavadocComment("Lists matching resources as typed, lazily retrieved pages.\n\n"
                + (hasKnowledgeSourceName ? "@param name The name of the knowledge source.\n" : "")
                + "@param requestOptions The options to configure the HTTP request, including listing filters.\n"
                + "@throws HttpResponseException if the service rejects the request.\n"
                + "@return The matching resources as a paginated response with {@link " + pagedType + "}.");
            clazz.addMember(method);
        }));
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



    // Adds public convenience methods to SearchIndexAsyncClient for knowledge base and knowledge source
    // createOrUpdate operations. The sync client has equivalent hand-written wrappers, but the async client
    // only has package-private generated convenience methods after hideWithResponseBinaryDataApis runs.
    private static void addAsyncKnowledgeBaseConvenienceMethods(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            clazz.getMethodsByName("createOrUpdateKnowledgeBase")
                .stream()
                .filter(method -> method.getParameters().size() == 1
                    && "KnowledgeBase".equals(method.getParameter(0).getTypeAsString()))
                .forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("createOrUpdateKnowledgeBaseWithResponse")
                .stream()
                .filter(method -> method.getParameters().size() == 2
                    && "KnowledgeBase".equals(method.getParameter(0).getTypeAsString())
                    && "RequestOptions".equals(method.getParameter(1).getTypeAsString()))
                .forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("createOrUpdateKnowledgeSourceWithResponse")
                .stream()
                .filter(method -> method.getParameters().size() == 2
                    && "KnowledgeSource".equals(method.getParameter(0).getTypeAsString())
                    && "RequestOptions".equals(method.getParameter(1).getTypeAsString()))
                .forEach(MethodDeclaration::remove);

            // Add: public Mono<KnowledgeBase> createOrUpdateKnowledgeBase(KnowledgeBase knowledgeBase)
            MethodDeclaration createOrUpdateKB = StaticJavaParser
                .parseBodyDeclaration("@Generated\n@ServiceMethod(returns = ReturnType.SINGLE)\n"
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
                = StaticJavaParser.parseBodyDeclaration("@Generated\n@ServiceMethod(returns = ReturnType.SINGLE)\n"
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
                = StaticJavaParser.parseBodyDeclaration("@Generated\n@ServiceMethod(returns = ReturnType.SINGLE)\n"
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
