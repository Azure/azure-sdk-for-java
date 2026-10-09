// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
    /**
     * Creates the Search code generation customization.
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
        allowNoContentRetrievalStream(libraryCustomization.getPackage("com.azure.search.documents.implementation")
            .getClass("KnowledgeBaseRetrievalClientImpl"));
    }

    private static void allowNoContentRetrievalStream(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            for (String methodName : Arrays.asList("retrieveStream", "retrieveStreamSync")) {
                MethodDeclaration method = ast
                    .findFirst(ClassOrInterfaceDeclaration.class,
                        declaration -> declaration.isInterface()
                            && "KnowledgeBaseRetrievalClientService".equals(declaration.getNameAsString()))
                    .orElseThrow(
                        () -> new IllegalStateException("Knowledge base retrieval service interface is missing."))
                    .getMethodsByName(methodName)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing streaming REST operation: " + methodName));
                method.getAnnotationByName("ExpectedResponses")
                    .orElseThrow(() -> new IllegalStateException("Missing expected responses for " + methodName))
                    .replace(StaticJavaParser.parseAnnotation("@ExpectedResponses({200, 204})"));
            }
        });
    }

    private static void addStreamModels(LibraryCustomization customization) {
        customization.getRawEditor().addFile(MODELS_PATH + "KnowledgeBaseRetrievalStreamEvent.java", baseEventSource());
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

    private static String streamEventDocumentation() {
        return "Events are polymorphic: see {@link KnowledgeBaseRetrievalStreamEvent} for the known subtypes. "
            + "Use a known subtype's {@code getValue()} to access its typed payload, for example "
            + "{@link com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedStreamEvent#getValue()}. "
            + "For every event, including unrecognized names, "
            + "{@link KnowledgeBaseRetrievalStreamEvent#getRawValue()} provides the original decoded SSE data "
            + "with multiline data joined by newlines, not the complete wire frame.\n\n";
    }

    private static String baseEventSource() {
        return header("com.azure.search.documents.knowledgebases.models")
            + "import com.azure.core.annotation.Generated;\n\n" + "/**\n"
            + " * Abstract base for polymorphic events emitted by a streaming knowledge base retrieval.\n"
            + " * Known events expose typed payloads through their subtype's {@code getValue()} method.\n"
            + " * Use {@link #getEventName()} and {@link #getRawValue()} for unrecognized events.\n"
            + " * Stream events are envelopes, not JSON payload models; serialize the typed payload instead.\n" + " *\n"
            + " * @see KnowledgeBaseRetrievalStartedStreamEvent\n" + " * @see KnowledgeBaseActivityStartedStreamEvent\n"
            + " * @see KnowledgeBaseActivityCompletedStreamEvent\n"
            + " * @see KnowledgeBaseAnswerCompletedStreamEvent\n"
            + " * @see KnowledgeBaseReferencesCompletedStreamEvent\n" + " * @see KnowledgeBaseErrorStreamEvent\n"
            + " * @see KnowledgeBaseResponseCompletedStreamEvent\n" + " */\n"
            + "public abstract class KnowledgeBaseRetrievalStreamEvent {\n" + "    @Generated\n"
            + "    private final String eventName;\n\n" + "    @Generated\n" + "    private final String rawValue;\n\n"
            + "    /**\n" + "     * Creates a stream event.\n" + "     *\n"
            + "     * @param eventName The server-sent event name.\n" + "     */\n" + "    @Generated\n"
            + "    protected KnowledgeBaseRetrievalStreamEvent(String eventName) {\n"
            + "        this(eventName, null);\n" + "    }\n\n" + "    /**\n"
            + "     * Creates a stream event with its original decoded SSE data.\n" + "     *\n"
            + "     * @param eventName The server-sent event name.\n"
            + "     * @param rawValue The original decoded SSE data, or null if none was supplied.\n" + "     */\n"
            + "    @Generated\n"
            + "    protected KnowledgeBaseRetrievalStreamEvent(String eventName, String rawValue) {\n"
            + "        this.eventName = eventName;\n" + "        this.rawValue = rawValue;\n" + "    }\n\n"
            + "    /**\n" + "     * Gets the server-sent event name.\n" + "     *\n"
            + "     * @return The event name.\n" + "     */\n" + "    @Generated\n"
            + "    public final String getEventName() {\n" + "        return eventName;\n" + "    }\n\n" + "    /**\n"
            + "     * Gets the original decoded SSE data, with multiline data joined by newlines.\n"
            + "     * This is not the complete wire frame. Supplied data, including an empty string, is preserved.\n"
            + "     * Known subtypes with no supplied data lazily serialize their current typed payload as JSON.\n"
            + "     * That generated JSON need not match an original wire representation.\n" + "     *\n"
            + "     * @return The supplied data, generated payload JSON, or null if neither data nor payload is present.\n"
            + "     * @throws java.io.UncheckedIOException If payload serialization fails.\n" + "     */\n"
            + "    @Generated\n" + "    public String getRawValue() {\n" + "        return rawValue;\n" + "    }\n\n"
            + "    /**\n" + "     * Gets whether this event terminates the retrieval stream.\n" + "     *\n"
            + "     * @return {@code true} if this is a terminal event; otherwise {@code false}.\n" + "     */\n"
            + "    @Generated\n" + "    public boolean isTerminal() {\n" + "        return false;\n" + "    }\n"
            + "}\n";
    }

    private static String wrapperSource(String className, String payloadType, String eventName, boolean terminal,
        boolean listPayload) {
        String valueType = listPayload ? "List<" + payloadType + ">" : payloadType;
        String listImports = listPayload
            ? "import com.azure.json.JsonProviders;\n" + "import com.azure.json.JsonWriter;\n"
                + "import java.io.StringWriter;\n" + "import java.util.List;\n"
            : "";
        String serialize = listPayload
            ? "            StringWriter output = new StringWriter();\n"
                + "            try (JsonWriter writer = JsonProviders.createWriter(output)) {\n"
                + "                writer.writeArray(value, (jsonWriter, item) -> item.toJson(jsonWriter)).flush();\n"
                + "            }\n" + "            return output.toString();\n"
            : "            return value.toJsonString();\n";
        String terminalOverride = terminal
            ? "\n    @Generated\n" + "    @Override\n" + "    public boolean isTerminal() {\n"
                + "        return true;\n" + "    }\n"
            : "";

        return header("com.azure.search.documents.knowledgebases.models")
            + "import com.azure.core.annotation.Generated;\n" + "import com.azure.core.annotation.Immutable;\n"
            + "import com.azure.core.util.logging.ClientLogger;\n" + listImports + "import java.io.IOException;\n"
            + "import java.io.UncheckedIOException;\n\n" + "/**\n" + " * Represents the {@code " + eventName
            + "} knowledge base retrieval stream event.\n"
            + " * Access the typed payload with {@link #getValue()} or the decoded SSE data with {@link #getRawValue()}.\n"
            + " *\n" + " * @see KnowledgeBaseRetrievalStreamEvent\n" + " */\n" + "@Immutable\n" + "public final class "
            + className + " extends KnowledgeBaseRetrievalStreamEvent {\n" + "    @Generated\n"
            + "    private static final ClientLogger LOGGER = new ClientLogger(" + className + ".class);\n\n"
            + "    @Generated\n" + "    private final " + valueType + " value;\n\n" + "    /**\n"
            + "     * Creates an event wrapper.\n" + "     *\n" + "     * @param value The event payload.\n"
            + "     */\n" + "    @Generated\n" + "    public " + className + "(" + valueType + " value) {\n"
            + "        this(value, null);\n" + "    }\n\n" + "    /**\n"
            + "     * Creates an event wrapper with its original decoded SSE data.\n" + "     *\n"
            + "     * @param value The event payload.\n"
            + "     * @param rawValue The original decoded SSE data, or null to serialize the payload lazily.\n"
            + "     */\n" + "    @Generated\n" + "    public " + className + "(" + valueType
            + " value, String rawValue) {\n" + "        super(\"" + eventName + "\", rawValue);\n"
            + "        this.value = value;\n" + "    }\n\n" + "    /**\n" + "     * Gets the event payload.\n"
            + "     *\n" + "     * @return The event payload.\n" + "     */\n" + "    @Generated\n" + "    public "
            + valueType + " getValue() {\n" + "        return value;\n" + "    }\n\n" + terminalOverride + "\n"
            + "    /**\n"
            + "     * Gets the original decoded SSE data, or lazily generated JSON for the current payload.\n"
            + "     * Supplied data, including an empty string, always takes precedence over the typed payload.\n"
            + "     * Generated JSON is not cached and need not match an original wire representation.\n" + "     *\n"
            + "     * @return The supplied data, generated payload JSON, or null if neither data nor payload is present.\n"
            + "     * @throws UncheckedIOException If payload serialization fails.\n" + "     */\n" + "    @Generated\n"
            + "    @Override\n" + "    public String getRawValue() {\n"
            + "        String rawValue = super.getRawValue();\n" + "        if (rawValue != null || value == null) {\n"
            + "            return rawValue;\n" + "        }\n" + "        try {\n" + serialize
            + "        } catch (IOException exception) {\n"
            + "            throw LOGGER.logExceptionAsError(new UncheckedIOException("
            + "\"Failed to serialize knowledge base retrieval stream event: " + eventName + "\", exception));\n"
            + "        }\n" + "    }\n" + "}\n";
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

    private static void repairAsyncSynonymMapsConvenienceMethod(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName())
            .ifPresent(clazz -> clazz.getMethodsByName("getSynonymMaps")
                .stream()
                .filter(method -> method.getParameters().isEmpty() && method.isAnnotationPresent("Generated"))
                .findFirst()
                .ifPresent(method -> method
                    .setBody(StaticJavaParser.parseBlock("{ return getSynonymMaps(null, null, null, null); }")))));
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
