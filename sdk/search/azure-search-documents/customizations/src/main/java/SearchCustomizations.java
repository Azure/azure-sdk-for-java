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
        repairAsyncSynonymMapsConvenienceMethod(indexes.getClass("SearchIndexAsyncClient"));

        // After hiding BinaryData protocol methods, add typed public convenience wrappers on the async client
        // that mirror what the sync client already has as hand-written methods.
        addAsyncKnowledgeBaseConvenienceMethods(indexes.getClass("SearchIndexAsyncClient"));

        // SearchResourceEncryptionKey workaround: the spec marks keyVaultUri and keyVaultKeyName as required,
        // but they are not required when isServiceLevelKey is true. Add a no-arg constructor.
        addNoArgConstructorToEncryptionKey(libraryCustomization.getPackage("com.azure.search.documents.indexes.models")
            .getClass("SearchResourceEncryptionKey"));
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
    // retrieval stream customizations isolated in this section so they can be removed when native support is available.
    private static final String PUBLIC_MODELS_PATH = "src/main/java/com/azure/search/documents/models/";
    private static final String MODELS_PATH = "src/main/java/com/azure/search/documents/knowledgebases/models/";
    private static final String SSE_PATH = "src/main/java/com/azure/search/documents/models/implementation/sse/";
    private static final String CLIENT_PATH = "src/main/java/com/azure/search/documents/knowledgebases/";
    private static final String CLIENT_IMPLEMENTATION_PATH
        = "src/main/java/com/azure/search/documents/knowledgebases/implementation/";

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
        customization.getRawEditor().addFile(PUBLIC_MODELS_PATH + "ServerSentEvent.java", serverSentEventSource());
        customization.getRawEditor()
            .addFile(PUBLIC_MODELS_PATH + "ServerSentEventListener.java", serverSentEventListenerSource());
        customization.getRawEditor().addFile(SSE_PATH + "ServerSentEventHelper.java", serverSentEventHelperSource());
        customization.getRawEditor()
            .addFile(SSE_PATH + "ServerSentEventStreamResponse.java", serverSentEventStreamResponseSource());
        customization.getRawEditor().addFile(SSE_PATH + "ServerSentEventStream.java", serverSentEventStreamSource());
        customization.getRawEditor().addFile(SSE_PATH + "ServerSentEventStreams.java", serverSentEventStreamsSource());
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
        customization.getRawEditor().removeFile(CLIENT_PATH + "KnowledgeBaseRetrievalStreamEventConverter.java");
        customization.getRawEditor()
            .addFile(CLIENT_IMPLEMENTATION_PATH + "KnowledgeBaseRetrievalStreamEventConverter.java", converterSource());
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
                            + "@ServiceMethod(returns = ReturnType.COLLECTION)\n"
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
                            + "@ServiceMethod(returns = ReturnType.COLLECTION)\n"
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
                            + "@ServiceMethod(returns = ReturnType.SINGLE)\n"
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
                            + "@ServiceMethod(returns = ReturnType.SINGLE)\n"
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

    private static String serverSentEventSource() {
        return header("com.azure.search.documents.models") + "import com.azure.core.annotation.Immutable;\n"
            + "import com.azure.search.documents.models.implementation.sse.ServerSentEventHelper;\n\n"
            + "import java.time.Duration;\n\n" + "/**\n"
            + " * Represents a server-sent event with a typed data payload.\n" + " *\n"
            + " * <p>An emitted server-sent event contains data and may expose an identifier, event name, comment, "
            + "and retry interval.\n"
            + " * The identifier and retry interval represent the effective stream state when the event was "
            + "dispatched, including\n" + " * values inherited from earlier metadata-only blocks.</p>\n" + " *\n"
            + " * <p>The identifier and retry interval are protocol metadata only. The client does not reconnect or "
            + "replay the\n"
            + " * request. Metadata-only updates received after the latest emitted event aren't exposed as an "
            + "additional event.</p>\n" + " *\n" + " * @param <T> The type of the event data.\n" + " */\n"
            + "@Immutable\n" + "public final class ServerSentEvent<T> {\n" + "    private final String id;\n"
            + "    private final String event;\n" + "    private final T data;\n"
            + "    private final String comment;\n" + "    private final Duration retryAfter;\n\n" + "    static {\n"
            + "        ServerSentEventHelper.setAccessor(new ServerSentEventHelper.ServerSentEventAccessor() {\n"
            + "            @Override\n"
            + "            public <U> ServerSentEvent<U> create(String id, String event, U data, String comment, "
            + "Duration retryAfter) {\n"
            + "                return new ServerSentEvent<>(id, event, data, comment, retryAfter);\n"
            + "            }\n" + "        });\n" + "    }\n\n"
            + "    private ServerSentEvent(String id, String event, T data, String comment, Duration retryAfter) {\n"
            + "        this.id = id;\n" + "        this.event = event;\n" + "        this.data = data;\n"
            + "        this.comment = comment;\n" + "        this.retryAfter = retryAfter;\n" + "    }\n\n"
            + "    /**\n" + "     * Gets the effective last-event identifier when this event was dispatched.\n"
            + "     *\n"
            + "     * @return The effective last-event identifier, {@code null} if no valid {@code id} field was "
            + "received before this\n"
            + "     * event, or an empty string if an empty {@code id} field reset the identifier.\n" + "     */\n"
            + "    public String getId() {\n" + "        return id;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the event name.\n" + "     *\n"
            + "     * @return The event name, or {@code message} if no non-empty {@code event} field was specified.\n"
            + "     */\n" + "    public String getEvent() {\n" + "        return event;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the event data.\n" + "     *\n"
            + "     * @return The event data, or {@code null} if event data wasn't specified.\n" + "     */\n"
            + "    public T getData() {\n" + "        return data;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the event comment.\n" + "     *\n"
            + "     * @return The event comment, or {@code null} if it wasn't specified.\n" + "     */\n"
            + "    public String getComment() {\n" + "        return comment;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the effective retry interval when this event was dispatched.\n" + "     *\n"
            + "     * @return The latest valid retry interval received before this event, or {@code null} if no valid\n"
            + "     * {@code retry} field was received.\n" + "     */\n" + "    public Duration getRetryAfter() {\n"
            + "        return retryAfter;\n" + "    }\n" + "}\n";
    }

    private static String serverSentEventListenerSource() {
        return header("com.azure.search.documents.models") + "/**\n"
            + " * A listener for receiving server-sent events.\n" + " *\n"
            + " * <p>Errors terminate processing, invoke {@link #onError(Throwable)} and {@link #onClose()}, and are "
            + "rethrown to the\n" + " * synchronous service caller as unchecked exceptions.</p>\n" + " *\n"
            + " * @param <T> The type of the event data.\n" + " */\n" + "@FunctionalInterface\n"
            + "public interface ServerSentEventListener<T> {\n" + "    /**\n" + "     * Handles a server-sent event.\n"
            + "     *\n" + "     * @param event The server-sent event.\n"
            + "     * @throws RuntimeException If an error occurs while handling the event.\n" + "     */\n"
            + "    void onEvent(ServerSentEvent<T> event);\n\n" + "    /**\n"
            + "     * Handles an error that terminates event processing.\n" + "     *\n"
            + "     * @param error The error that terminated event processing.\n" + "     */\n"
            + "    default void onError(Throwable error) {\n" + "        // No-op by default.\n" + "    }\n\n"
            + "    /**\n" + "     * Handles closure of the event stream.\n" + "     */\n"
            + "    default void onClose() {\n" + "        // No-op by default.\n" + "    }\n" + "}\n";
    }

    private static String serverSentEventHelperSource() {
        return header("com.azure.search.documents.models.implementation.sse")
            + "import com.azure.search.documents.models.ServerSentEvent;\n\n" + "import java.time.Duration;\n"
            + "import java.util.Objects;\n" + "import java.util.concurrent.atomic.AtomicReference;\n\n" + "/**\n"
            + " * Accesses non-public members of {@link ServerSentEvent}.\n" + " */\n"
            + "public final class ServerSentEventHelper {\n"
            + "    private static final AtomicReference<ServerSentEventAccessor> ACCESSOR = new AtomicReference<>();\n\n"
            + "    private ServerSentEventHelper() {\n" + "    }\n\n" + "    /**\n"
            + "     * Defines access to non-public members of {@link ServerSentEvent}.\n" + "     */\n"
            + "    public interface ServerSentEventAccessor {\n" + "        /**\n"
            + "         * Creates a server-sent event.\n" + "         *\n"
            + "         * @param id The event identifier.\n" + "         * @param event The event name.\n"
            + "         * @param data The event data.\n" + "         * @param comment The event comment.\n"
            + "         * @param retryAfter The retry interval.\n"
            + "         * @param <T> The type of the event data.\n" + "         * @return The server-sent event.\n"
            + "         */\n"
            + "        <T> ServerSentEvent<T> create(String id, String event, T data, String comment, "
            + "Duration retryAfter);\n" + "    }\n\n" + "    /**\n" + "     * Sets the accessor.\n" + "     *\n"
            + "     * @param accessor The accessor.\n" + "     */\n"
            + "    public static void setAccessor(ServerSentEventAccessor accessor) {\n"
            + "        ACCESSOR.set(Objects.requireNonNull(accessor, \"'accessor' cannot be null.\"));\n" + "    }\n\n"
            + "    static <T> ServerSentEvent<T> create(String id, String event, T data, String comment, "
            + "Duration retryAfter) {\n"
            + "        return getAccessor().create(id, event, data, comment, retryAfter);\n" + "    }\n\n"
            + "    private static ServerSentEventAccessor getAccessor() {\n"
            + "        ServerSentEventAccessor accessor = ACCESSOR.get();\n" + "        if (accessor == null) {\n"
            + "            try {\n" + "                Class.forName(ServerSentEvent.class.getName(), true, "
            + "ServerSentEvent.class.getClassLoader());\n"
            + "            } catch (ClassNotFoundException exception) {\n"
            + "                throw new IllegalStateException(\"Unable to initialize ServerSentEvent.\", exception);\n"
            + "            }\n" + "            accessor = ACCESSOR.get();\n" + "        }\n"
            + "        return accessor;\n" + "    }\n" + "}\n";
    }

    private static String serverSentEventStreamResponseSource() {
        return header("com.azure.search.documents.models.implementation.sse")
            + "import com.azure.core.http.HttpHeaderName;\n" + "import com.azure.core.http.rest.Response;\n"
            + "import com.azure.core.util.BinaryData;\n" + "import org.reactivestreams.Subscription;\n"
            + "import reactor.core.publisher.BaseSubscriber;\n\n" + "import java.nio.ByteBuffer;\n"
            + "import java.util.Locale;\n" + "import java.util.Objects;\n\n" + "/**\n"
            + " * Validates and exposes the fields used by a server-sent event stream.\n" + " */\n"
            + "final class ServerSentEventStreamResponse {\n" + "    private final int statusCode;\n"
            + "    private final BinaryData body;\n\n"
            + "    private ServerSentEventStreamResponse(int statusCode, BinaryData body) {\n"
            + "        this.statusCode = statusCode;\n" + "        this.body = body;\n" + "    }\n\n"
            + "    static ServerSentEventStreamResponse fromResponse(Response<BinaryData> response) {\n"
            + "        Objects.requireNonNull(response, \"'response' cannot be null.\");\n"
            + "        int statusCode = response.getStatusCode();\n"
            + "        if (statusCode != 200 && statusCode != 204) {\n"
            + "            cancelBody(response.getValue());\n"
            + "            throw new IllegalStateException(\"Expected a server-sent event response to have status "
            + "code 200 or 204.\");\n" + "        }\n\n" + "        if (statusCode == 200\n"
            + "            && !isTextEventStream(response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {\n"
            + "            cancelBody(response.getValue());\n" + "            throw new IllegalStateException(\n"
            + "                \"Expected a successful server-sent event response to have Content-Type "
            + "'text/event-stream'.\");\n" + "        }\n\n" + "        BinaryData body = response.getValue();\n"
            + "        if (statusCode == 200 && body == null) {\n"
            + "            throw new NullPointerException(\"'response.getValue()' cannot be null unless the status "
            + "code is 204.\");\n" + "        }\n" + "        if (statusCode == 204) {\n"
            + "            cancelBody(body);\n" + "        }\n"
            + "        return new ServerSentEventStreamResponse(statusCode, body);\n" + "    }\n\n"
            + "    static void cancelBody(BinaryData body) {\n" + "        if (body == null) {\n"
            + "            return;\n" + "        }\n\n"
            + "        body.toFluxByteBuffer().subscribe(new BaseSubscriber<ByteBuffer>() {\n"
            + "            @Override\n" + "            protected void hookOnSubscribe(Subscription subscription) {\n"
            + "                cancel();\n" + "            }\n" + "        });\n" + "    }\n\n"
            + "    private static boolean isTextEventStream(String contentType) {\n"
            + "        if (contentType == null || contentType.indexOf(',') >= 0) {\n" + "            return false;\n"
            + "        }\n" + "        int parameterIndex = contentType.indexOf(';');\n"
            + "        String mediaType = parameterIndex < 0 ? contentType : contentType.substring(0, parameterIndex);\n"
            + "        return \"text/event-stream\".equals(mediaType.trim().toLowerCase(Locale.ROOT));\n" + "    }\n\n"
            + "    int getStatusCode() {\n" + "        return statusCode;\n" + "    }\n\n"
            + "    BinaryData getBody() {\n" + "        return body;\n" + "    }\n" + "}\n";
    }

    private static String serverSentEventStreamSource() {
        return header("com.azure.search.documents.models.implementation.sse")
            + "import com.azure.core.http.rest.Response;\n" + "import com.azure.core.util.BinaryData;\n"
            + "import com.azure.search.documents.models.ServerSentEvent;\n"
            + "import com.azure.search.documents.models.ServerSentEventListener;\n"
            + "import reactor.core.publisher.Flux;\n\n"
            + "import java.io.IOException;\n" + "import java.io.InputStream;\n"
            + "import java.io.UncheckedIOException;\n" + "import java.nio.ByteBuffer;\n"
            + "import java.nio.charset.CharacterCodingException;\n" + "import java.nio.charset.CodingErrorAction;\n"
            + "import java.nio.charset.StandardCharsets;\n" + "import java.time.Duration;\n"
            + "import java.util.ArrayList;\n" + "import java.util.Arrays;\n" + "import java.util.Collections;\n"
            + "import java.util.List;\n" + "import java.util.Objects;\n"
            + "import java.util.concurrent.atomic.AtomicBoolean;\n" + "import java.util.function.BiFunction;\n"
            + "import java.util.function.Predicate;\n\n" + "/**\n" + " * Parses one server-sent event response.\n"
            + " */\n" + "final class ServerSentEventStream {\n"
            + "    private static final String DEFAULT_EVENT = \"message\";\n\n"
            + "    private ServerSentEventStream() {\n" + "    }\n\n" + "    /**\n"
            + "     * Decodes an SSE response until the response body ends.\n" + "     *\n"
            + "     * @param response The streaming response.\n"
            + "     * @param converter Converts an event name and data payload into the event data type.\n"
            + "     * @param <T> The event data type.\n" + "     * @return A flux of decoded events.\n" + "     */\n"
            + "    static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,\n"
            + "        BiFunction<String, String, T> converter) {\n"
            + "        Objects.requireNonNull(response, \"'response' cannot be null.\");\n"
            + "        Objects.requireNonNull(converter, \"'converter' cannot be null.\");\n"
            + "        return toFluxInternal(response, converter, null);\n" + "    }\n\n" + "    /**\n"
            + "     * Decodes an SSE response until an inclusive terminal event is emitted.\n" + "     *\n"
            + "     * @param response The streaming response.\n"
            + "     * @param converter Converts an event name and data payload into the event data type.\n"
            + "     * @param terminalEvent Identifies an inclusive terminal event that ends processing early.\n"
            + "     * @param <T> The event data type.\n" + "     * @return A flux of decoded events.\n" + "     */\n"
            + "    static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,\n"
            + "        BiFunction<String, String, T> converter, Predicate<ServerSentEvent<T>> terminalEvent) {\n"
            + "        Objects.requireNonNull(response, \"'response' cannot be null.\");\n"
            + "        Objects.requireNonNull(converter, \"'converter' cannot be null.\");\n"
            + "        Objects.requireNonNull(terminalEvent, \"'terminalEvent' cannot be null.\");\n"
            + "        return toFluxInternal(response, converter, terminalEvent);\n" + "    }\n\n"
            + "    private static <T> Flux<ServerSentEvent<T>> toFluxInternal(Response<BinaryData> response,\n"
            + "        BiFunction<String, String, T> converter, Predicate<ServerSentEvent<T>> terminalEvent) {\n"
            + "        AtomicBoolean subscribed = new AtomicBoolean();\n" + "        return Flux.defer(() -> {\n"
            + "            if (!subscribed.compareAndSet(false, true)) {\n" + "                return Flux.error(\n"
            + "                    new IllegalStateException(\"This server-sent event stream supports only one "
            + "subscription.\"));\n" + "            }\n\n"
            + "            ServerSentEventStreamResponse streamResponse\n"
            + "                = ServerSentEventStreamResponse.fromResponse(response);\n"
            + "            Flux<ServerSentEvent<T>> events = streamResponse.getStatusCode() == 204\n"
            + "                ? Flux.empty()\n" + "                : decode(streamResponse.getBody(), converter);\n\n"
            + "            if (terminalEvent != null) {\n" + "                events = events.takeUntil(terminalEvent);\n"
            + "            }\n" + "            return events;\n"
            + "        });\n" + "    }\n\n" + "    /**\n"
            + "     * Processes an SSE response until the response body ends.\n" + "     *\n"
            + "     * @param response The streaming response.\n"
            + "     * @param converter Converts an event name and data payload into the event data type.\n"
            + "     * @param listener The event listener.\n" + "     * @param <T> The event data type.\n" + "     */\n"
            + "    static <T> void listen(Response<BinaryData> response, "
            + "BiFunction<String, String, T> converter,\n" + "        ServerSentEventListener<T> listener) {\n"
            + "        Objects.requireNonNull(response, \"'response' cannot be null.\");\n"
            + "        Objects.requireNonNull(converter, \"'converter' cannot be null.\");\n"
            + "        Objects.requireNonNull(listener, \"'listener' cannot be null.\");\n"
            + "        listenInternal(response, converter, null, listener);\n" + "    }\n\n" + "    /**\n"
            + "     * Processes an SSE response until an inclusive terminal event is delivered.\n" + "     *\n"
            + "     * @param response The streaming response.\n"
            + "     * @param converter Converts an event name and data payload into the event data type.\n"
            + "     * @param terminalEvent Identifies an inclusive terminal event that ends processing early.\n"
            + "     * @param listener The event listener.\n" + "     * @param <T> The event data type.\n" + "     */\n"
            + "    static <T> void listen(Response<BinaryData> response, "
            + "BiFunction<String, String, T> converter,\n"
            + "        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) {\n"
            + "        Objects.requireNonNull(response, \"'response' cannot be null.\");\n"
            + "        Objects.requireNonNull(converter, \"'converter' cannot be null.\");\n"
            + "        Objects.requireNonNull(terminalEvent, \"'terminalEvent' cannot be null.\");\n"
            + "        Objects.requireNonNull(listener, \"'listener' cannot be null.\");\n"
            + "        listenInternal(response, converter, terminalEvent, listener);\n" + "    }\n\n"
            + "    private static <T> void listenInternal(Response<BinaryData> response, "
            + "BiFunction<String, String, T> converter,\n"
            + "        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) {\n"
            + "        try {\n" + "            ServerSentEventStreamResponse streamResponse\n"
            + "                = ServerSentEventStreamResponse.fromResponse(response);\n"
            + "            if (streamResponse.getStatusCode() != 204) {\n"
            + "                process(streamResponse.getBody(), converter, terminalEvent, listener);\n"
            + "            }\n"
            + "        } catch (IOException exception) {\n" + "            listener.onError(exception);\n"
            + "            throw new UncheckedIOException(exception);\n"
            + "        } catch (RuntimeException exception) {\n" + "            listener.onError(exception);\n"
            + "            throw exception;\n" + "        } finally {\n" + "            listener.onClose();\n"
            + "        }\n" + "    }\n\n" + "    private static <T> Flux<ServerSentEvent<T>> decode(BinaryData body,\n"
            + "        BiFunction<String, String, T> converter) {\n"
            + "        ServerSentEventDecoder decoder = new ServerSentEventDecoder();\n"
            + "        Flux<ServerSentEventFrame> frames = body.toFluxByteBuffer()\n" + "            .hide()\n"
            + "            .concatMap(buffer -> Flux.fromIterable(decoder.feed(buffer)), 1)\n"
            + "            .concatWith(Flux.defer(() -> Flux.fromIterable(decoder.finish())));\n"
            + "        return frames.concatMap(frame -> {\n"
            + "            T data = converter.apply(frame.event, frame.data);\n"
            + "            return data == null ? Flux.empty() : Flux.just(frame.toEvent(data));\n" + "        }, 1);\n"
            + "    }\n\n"
            + "    private static <T> boolean process(BinaryData body, BiFunction<String, String, T> converter,\n"
            + "        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) "
            + "throws IOException {\n" + "        ServerSentEventDecoder decoder = new ServerSentEventDecoder();\n"
            + "        byte[] readBuffer = new byte[8192];\n\n"
            + "        try (InputStream stream = body.toStream()) {\n" + "            while (true) {\n"
            + "                checkInterrupted();\n" + "                int read = stream.read(readBuffer);\n"
            + "                if (read == -1) {\n"
            + "                    return processFrames(decoder.finish(), converter, terminalEvent, listener);\n"
            + "                }\n" + "                if (read > 0\n"
            + "                    && processFrames(decoder.feed(ByteBuffer.wrap(readBuffer, 0, read)), converter, "
            + "terminalEvent,\n" + "                        listener)) {\n" + "                    return true;\n"
            + "                }\n" + "            }\n" + "        }\n" + "    }\n\n"
            + "    private static <T> boolean processFrames(List<ServerSentEventFrame> frames,\n"
            + "        BiFunction<String, String, T> converter, Predicate<ServerSentEvent<T>> terminalEvent,\n"
            + "        ServerSentEventListener<T> listener) {\n"
            + "        for (ServerSentEventFrame frame : frames) {\n" + "            checkInterrupted();\n"
            + "            T data = converter.apply(frame.event, frame.data);\n" + "            if (data != null) {\n"
            + "                ServerSentEvent<T> event = frame.toEvent(data);\n"
            + "                listener.onEvent(event);\n"
            + "                if (terminalEvent != null && terminalEvent.test(event)) {\n"
            + "                    return true;\n" + "                }\n" + "            }\n" + "        }\n"
            + "        return false;\n" + "    }\n\n" + "    private static void checkInterrupted() {\n"
            + "        if (Thread.currentThread().isInterrupted()) {\n"
            + "            throw new RuntimeException(\"Interrupted while processing the server-sent event stream.\",\n"
            + "                new InterruptedException());\n" + "        }\n" + "    }\n\n"
            + "    private static String removeOptionalSpace(String value) {\n"
            + "        return value.startsWith(\" \") ? value.substring(1) : value;\n" + "    }\n\n"
            + "    private static Duration parseRetryAfter(String value) {\n" + "        if (value.isEmpty()) {\n"
            + "            return null;\n" + "        }\n" + "        for (int i = 0; i < value.length(); i++) {\n"
            + "            char character = value.charAt(i);\n"
            + "            if (character < '0' || character > '9') {\n" + "                return null;\n"
            + "            }\n" + "        }\n" + "        try {\n"
            + "            return Duration.ofMillis(Long.parseLong(value));\n"
            + "        } catch (NumberFormatException ignored) {\n" + "            return null;\n" + "        }\n"
            + "    }\n\n" + "    private static final class ServerSentEventDecoder {\n"
            + "        private final StreamState state = new StreamState();\n"
            + "        private byte[] lineBytes = new byte[256];\n" + "        private int lineLength;\n"
            + "        private boolean pendingCarriageReturn;\n" + "        private boolean firstLine = true;\n"
            + "        private String event;\n" + "        private List<String> data;\n"
            + "        private String comment;\n\n"
            + "        private List<ServerSentEventFrame> feed(ByteBuffer source) {\n"
            + "            ByteBuffer buffer = source.duplicate();\n"
            + "            List<ServerSentEventFrame> events = new ArrayList<>();\n"
            + "            while (buffer.hasRemaining()) {\n" + "                byte value = buffer.get();\n"
            + "                if (pendingCarriageReturn) {\n" + "                    pendingCarriageReturn = false;\n"
            + "                    if (value == '\\n') {\n" + "                        continue;\n"
            + "                    }\n" + "                }\n" + "                if (value == '\\n') {\n"
            + "                    processLine(decodeLine(), events);\n"
            + "                } else if (value == '\\r') {\n"
            + "                    processLine(decodeLine(), events);\n"
            + "                    pendingCarriageReturn = true;\n" + "                } else {\n"
            + "                    appendByte(value);\n" + "                }\n" + "            }\n"
            + "            return events;\n" + "        }\n\n"
            + "        private List<ServerSentEventFrame> finish() {\n" + "            if (lineLength > 0) {\n"
            + "                // Validate trailing bytes even though an unterminated SSE event is discarded.\n"
            + "                decodeLine();\n" + "            }\n" + "            return Collections.emptyList();\n"
            + "        }\n\n" + "        private void appendByte(byte value) {\n"
            + "            if (lineLength == lineBytes.length) {\n"
            + "                lineBytes = Arrays.copyOf(lineBytes, lineBytes.length * 2);\n" + "            }\n"
            + "            lineBytes[lineLength++] = value;\n" + "        }\n\n"
            + "        private String decodeLine() {\n" + "            String line;\n" + "            try {\n"
            + "                line = StandardCharsets.UTF_8.newDecoder()\n"
            + "                    .onMalformedInput(CodingErrorAction.REPORT)\n"
            + "                    .onUnmappableCharacter(CodingErrorAction.REPORT)\n"
            + "                    .decode(ByteBuffer.wrap(lineBytes, 0, lineLength))\n"
            + "                    .toString();\n" + "            } catch (CharacterCodingException exception) {\n"
            + "                throw new IllegalStateException(\"Failed to decode the server-sent event stream.\", "
            + "exception);\n" + "            }\n" + "            lineLength = 0;\n" + "            if (firstLine) {\n"
            + "                firstLine = false;\n"
            + "                if (!line.isEmpty() && line.charAt(0) == (char) 0xFEFF) {\n"
            + "                    return line.substring(1);\n" + "                }\n" + "            }\n"
            + "            return line;\n" + "        }\n\n"
            + "        private void processLine(String line, List<ServerSentEventFrame> events) {\n"
            + "            if (line.isEmpty()) {\n"
            + "                ServerSentEventFrame parsedEvent = buildEvent();\n"
            + "                if (parsedEvent != null) {\n" + "                    events.add(parsedEvent);\n"
            + "                }\n" + "                return;\n" + "            }\n"
            + "            if (line.charAt(0) == ':') {\n"
            + "                comment = removeOptionalSpace(line.substring(1));\n" + "                return;\n"
            + "            }\n\n" + "            int colonIndex = line.indexOf(':');\n"
            + "            String field = colonIndex < 0 ? line : line.substring(0, colonIndex);\n"
            + "            String value = colonIndex < 0 ? \"\" : removeOptionalSpace(line.substring(colonIndex + 1));\n"
            + "            switch (field) {\n" + "                case \"event\":\n"
            + "                    event = value;\n" + "                    break;\n"
            + "                case \"data\":\n" + "                    if (data == null) {\n"
            + "                        data = new ArrayList<>();\n" + "                    }\n"
            + "                    data.add(value);\n" + "                    break;\n"
            + "                case \"id\":\n" + "                    if (value.indexOf('\\0') < 0) {\n"
            + "                        state.setLastEventId(value);\n" + "                    }\n"
            + "                    break;\n" + "                case \"retry\":\n"
            + "                    Duration parsedRetryAfter = parseRetryAfter(value);\n"
            + "                    if (parsedRetryAfter != null) {\n"
            + "                        state.setRetryAfter(parsedRetryAfter);\n" + "                    }\n"
            + "                    break;\n" + "                default:\n" + "                    break;\n"
            + "            }\n" + "        }\n\n" + "        private ServerSentEventFrame buildEvent() {\n"
            + "            String currentEvent = event;\n" + "            List<String> currentData = data;\n"
            + "            String currentComment = comment;\n" + "            event = null;\n"
            + "            data = null;\n" + "            comment = null;\n\n"
            + "            if (currentData == null) {\n" + "                return null;\n" + "            }\n"
            + "            if (currentEvent == null || currentEvent.isEmpty()) {\n"
            + "                currentEvent = DEFAULT_EVENT;\n" + "            }\n"
            + "            return new ServerSentEventFrame(state.lastEventId, currentEvent, "
            + "String.join(\"\\n\", currentData),\n" + "                currentComment, state.retryAfter);\n"
            + "        }\n" + "    }\n\n" + "    private static final class StreamState {\n"
            + "        private String lastEventId;\n" + "        private Duration retryAfter;\n\n"
            + "        private void setLastEventId(String lastEventId) {\n"
            + "            this.lastEventId = lastEventId;\n" + "        }\n\n"
            + "        private void setRetryAfter(Duration retryAfter) {\n"
            + "            this.retryAfter = retryAfter;\n" + "        }\n" + "    }\n\n"
            + "    private static final class ServerSentEventFrame {\n" + "        private final String id;\n"
            + "        private final String event;\n" + "        private final String data;\n"
            + "        private final String comment;\n" + "        private final Duration retryAfter;\n\n"
            + "        private ServerSentEventFrame(String id, String event, String data, String comment, "
            + "Duration retryAfter) {\n" + "            this.id = id;\n" + "            this.event = event;\n"
            + "            this.data = data;\n" + "            this.comment = comment;\n"
            + "            this.retryAfter = retryAfter;\n" + "        }\n\n"
            + "        private <T> ServerSentEvent<T> toEvent(T data) {\n"
            + "            return ServerSentEventHelper.create(id, event, data, comment, retryAfter);\n" + "        }\n"
            + "    }\n" + "}\n";
    }

    private static String serverSentEventStreamsSource() {
        return header("com.azure.search.documents.models.implementation.sse")
            + "import com.azure.core.http.rest.Response;\n" + "import com.azure.core.util.BinaryData;\n"
            + "import com.azure.search.documents.models.ServerSentEvent;\n"
            + "import com.azure.search.documents.models.ServerSentEventListener;\n"
            + "import reactor.core.publisher.Flux;\n\n" + "import java.util.function.BiFunction;\n"
            + "import java.util.function.Predicate;\n\n" + "/**\n"
            + " * Consumes a single HTTP response as a server-sent event stream.\n" + " */\n"
            + "public final class ServerSentEventStreams {\n" + "    private ServerSentEventStreams() {\n" + "    }\n\n"
            + "    /**\n" + "     * Decodes one response until the response body ends.\n" + "     *\n"
            + "     * @param response The streaming response.\n"
            + "     * @param converter Converts an event name and data payload into the event data type.\n"
            + "     * @param <T> The event data type.\n" + "     * @return A flux of decoded server-sent events.\n"
            + "     */\n" + "    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,\n"
            + "        BiFunction<String, String, T> converter) {\n"
            + "        return ServerSentEventStream.toFlux(response, converter);\n" + "    }\n\n" + "    /**\n"
            + "     * Decodes one response until an inclusive terminal event is emitted.\n" + "     *\n"
            + "     * <p>HTTP 204 and response-body EOF complete normally without requiring a terminal event.</p>\n"
            + "     *\n"
            + "     * @param response The streaming response.\n"
            + "     * @param converter Converts an event name and data payload into the event data type.\n"
            + "     * @param terminalEvent Identifies an inclusive terminal event that ends processing early.\n"
            + "     * @param <T> The event data type.\n" + "     * @return A flux of decoded server-sent events.\n"
            + "     */\n" + "    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,\n"
            + "        BiFunction<String, String, T> converter, Predicate<ServerSentEvent<T>> terminalEvent) {\n"
            + "        return ServerSentEventStream.toFlux(response, converter, terminalEvent);\n" + "    }\n\n"
            + "    /**\n"
            + "     * Decodes one response and delivers events to a listener until the response body ends.\n"
            + "     *\n" + "     * @param response The streaming response.\n"
            + "     * @param converter Converts an event name and data payload into the event data type.\n"
            + "     * @param listener The listener that receives events and lifecycle notifications.\n"
            + "     * @param <T> The event data type.\n" + "     */\n"
            + "    public static <T> void listen(Response<BinaryData> response, "
            + "BiFunction<String, String, T> converter,\n" + "        ServerSentEventListener<T> listener) {\n"
            + "        ServerSentEventStream.listen(response, converter, listener);\n" + "    }\n\n" + "    /**\n"
            + "     * Decodes one response until an inclusive terminal event is delivered to a listener.\n" + "     *\n"
            + "     * <p>HTTP 204 and response-body EOF close normally without requiring a terminal event.</p>\n"
            + "     *\n"
            + "     * @param response The streaming response.\n"
            + "     * @param converter Converts an event name and data payload into the event data type.\n"
            + "     * @param terminalEvent Identifies an inclusive terminal event that ends processing early.\n"
            + "     * @param listener The listener that receives events and lifecycle notifications.\n"
            + "     * @param <T> The event data type.\n" + "     */\n"
            + "    public static <T> void listen(Response<BinaryData> response, "
            + "BiFunction<String, String, T> converter,\n"
            + "        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) {\n"
            + "        ServerSentEventStream.listen(response, converter, terminalEvent, listener);\n" + "    }\n"
            + "}\n";
    }

    private static String baseEventSource() {
        return header("com.azure.search.documents.knowledgebases.models") + "/**\n"
            + " * Base type for events emitted by a streaming knowledge base retrieval.\n" + " */\n"
            + "public abstract class KnowledgeBaseRetrievalStreamEvent {\n"
            + "    private final String eventName;\n\n" + "    /**\n" + "     * Creates a stream event.\n" + "     *\n"
            + "     * @param eventName The server-sent event name.\n" + "     */\n"
            + "    protected KnowledgeBaseRetrievalStreamEvent(String eventName) {\n"
            + "        this.eventName = eventName;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the server-sent event name.\n" + "     *\n" + "     * @return The event name.\n"
            + "     */\n" + "    public final String getEventName() {\n" + "        return eventName;\n" + "    }\n\n"
            + "    /**\n" + "     * Gets whether this event terminates the retrieval stream.\n" + "     *\n"
            + "     * @return {@code true} if this is a terminal event; otherwise {@code false}.\n" + "     */\n"
            + "    public boolean isTerminal() {\n" + "        return false;\n" + "    }\n" + "}\n";
    }

    private static String unknownEventSource() {
        return header("com.azure.search.documents.knowledgebases.models") + "/**\n"
            + " * Represents a knowledge base retrieval stream event that is not recognized by this SDK version.\n"
            + " */\n" + "public final class UnknownKnowledgeBaseRetrievalStreamEvent\n"
            + "    extends KnowledgeBaseRetrievalStreamEvent {\n" + "    private final String data;\n\n" + "    /**\n"
            + "     * Creates an unknown stream event.\n" + "     *\n"
            + "     * @param eventName The server-sent event name.\n"
            + "     * @param data The raw server-sent event data.\n" + "     */\n"
            + "    public UnknownKnowledgeBaseRetrievalStreamEvent(String eventName, String data) {\n"
            + "        super(eventName);\n" + "        this.data = data;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the raw server-sent event data.\n" + "     *\n" + "     * @return The raw event data.\n"
            + "     */\n" + "    public String getData() {\n" + "        return data;\n" + "    }\n" + "}\n";
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
            ? "\n    @Override\n" + "    public boolean isTerminal() {\n" + "        return true;\n" + "    }\n"
            : "";

        return header("com.azure.search.documents.knowledgebases.models") + "import com.azure.json.JsonReader;\n"
            + "import com.azure.json.JsonSerializable;\n" + "import com.azure.json.JsonWriter;\n\n"
            + "import java.io.IOException;\n" + listImport + "\n" + "/**\n" + " * Represents the {@code " + eventName
            + "} knowledge base retrieval stream event.\n" + " */\n" + "public final class " + className
            + " extends KnowledgeBaseRetrievalStreamEvent\n" + "    implements JsonSerializable<" + className + "> {\n"
            + "    private final " + valueType + " value;\n\n" + "    /**\n" + "     * Creates an event wrapper.\n"
            + "     *\n" + "     * @param value The event payload.\n" + "     */\n" + "    public " + className + "("
            + valueType + " value) {\n" + "        super(\"" + eventName + "\");\n" + "        this.value = value;\n"
            + "    }\n\n" + "    /**\n"
            + "     * Gets the event payload.\n" + "     *\n" + "     * @return The event payload.\n" + "     */\n"
            + "    public " + valueType + " getValue() {\n" + "        return value;\n" + "    }\n\n"
            + terminalOverride + "\n" + "    @Override\n"
            + "    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {\n" + toJson + "    }\n\n"
            + "    /**\n" + "     * Reads an event wrapper from JSON.\n" + "     *\n"
            + "     * @param jsonReader The reader to read from.\n" + "     * @return The parsed event wrapper.\n"
            + "     * @throws IOException If the event payload cannot be read.\n" + "     */\n" + "    public static "
            + className + " fromJson(JsonReader jsonReader) throws IOException {\n" + fromJson + "    }\n" + "}\n";
    }

    private static String converterSource() {
        return header("com.azure.search.documents.knowledgebases.implementation")
            + "import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityCompletedStreamEvent;\n"
            + "import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedStreamEvent;\n"
            + "import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedStreamEvent;\n"
            + "import com.azure.search.documents.knowledgebases.models.KnowledgeBaseErrorStreamEvent;\n"
            + "import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReferencesCompletedStreamEvent;\n"
            + "import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedStreamEvent;\n"
            + "import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedStreamEvent;\n"
            + "import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;\n"
            + "import com.azure.search.documents.knowledgebases.models.UnknownKnowledgeBaseRetrievalStreamEvent;\n"
            + "import com.azure.json.JsonProviders;\n" + "import com.azure.json.JsonReader;\n\n"
            + "import java.io.IOException;\n" + "import java.io.UncheckedIOException;\n\n"
            + "/**\n" + " * Converts knowledge base retrieval stream event payloads to typed event models.\n" + " */\n"
            + "public final class KnowledgeBaseRetrievalStreamEventConverter {\n"
            + "    private KnowledgeBaseRetrievalStreamEventConverter() {\n" + "    }\n\n"
            + "    /**\n" + "     * Converts a stream event payload.\n" + "     *\n"
            + "     * @param eventName The stream event name.\n" + "     * @param data The stream event data.\n"
            + "     * @return The typed stream event.\n" + "     */\n"
            + "    public static KnowledgeBaseRetrievalStreamEvent convert(String eventName, String data) {\n"
            + "        switch (eventName) {\n" + "            case \"retrieval.started\":\n"
            + "                return read(eventName, data, KnowledgeBaseRetrievalStartedStreamEvent::fromJson);\n"
            + "            case \"activity.started\":\n"
            + "                return read(eventName, data, KnowledgeBaseActivityStartedStreamEvent::fromJson);\n"
            + "            case \"activity.completed\":\n"
            + "                return read(eventName, data, KnowledgeBaseActivityCompletedStreamEvent::fromJson);\n"
            + "            case \"answer.completed\":\n"
            + "                return read(eventName, data, KnowledgeBaseAnswerCompletedStreamEvent::fromJson);\n"
            + "            case \"references.completed\":\n"
            + "                return read(eventName, data, KnowledgeBaseReferencesCompletedStreamEvent::fromJson);\n"
            + "            case \"error\":\n"
            + "                return read(eventName, data, KnowledgeBaseErrorStreamEvent::fromJson);\n"
            + "            case \"response.completed\":\n"
            + "                return read(eventName, data, KnowledgeBaseResponseCompletedStreamEvent::fromJson);\n"
            + "            default:\n" + "                return new UnknownKnowledgeBaseRetrievalStreamEvent(eventName, data);\n"
            + "        }\n" + "    }\n\n"
            + "    private static KnowledgeBaseRetrievalStreamEvent read(String eventName, String data,\n"
            + "        EventReader eventReader) {\n"
            + "        try (JsonReader reader = JsonProviders.createReader(data)) {\n"
            + "            return eventReader.read(reader);\n"
            + "        } catch (IOException exception) {\n"
            + "            throw new UncheckedIOException(\"Failed to decode knowledge base retrieval stream event: \"\n"
            + "                + eventName, exception);\n" + "        }\n" + "    }\n\n"
            + "    @FunctionalInterface\n" + "    private interface EventReader {\n"
            + "        KnowledgeBaseRetrievalStreamEvent read(JsonReader reader) throws IOException;\n" + "    }\n"
            + "}\n";
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
                entries.add(0, new EnumConstantDeclaration(enumName).addArgument(new StringLiteralExpr(version))
                    .setJavadocComment("Enum value " + version + "."));
            }

            enumDeclaration.setEntries(entries);
        }));
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
