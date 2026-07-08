// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * TypeSpec customization for azure-storage-queue.
 *
 * <p>Two responsibilities, replacing what the AutoRest setup did via {@code swagger/README.md}
 * directives plus the old {@code QueueStorageCustomization} post-processor:</p>
 *
 * <ol>
 *   <li><b>Remove the generated public client surface.</b> typespec-java emits a public
 *       convenience client/builder per operation group (QueueClient, QueueAsyncClient,
 *       ServiceClient, MessagesClient, MessageIdsClient, AzureQueueStorageBuilder, ...) plus a
 *       QueuesServiceVersion. The shipped library hand-writes that surface
 *       (QueueClient/QueueAsyncClient/QueueServiceClient/QueueServiceAsyncClient/builders/
 *       QueueServiceVersion in {@code com.azure.storage.queue}). We drop the codegen copies so the
 *       hand-written versions are not overwritten by {@code tsp-client update}. The generated
 *       implementation layer ({@code com.azure.storage.queue.implementation.AzureQueueStorageImpl}
 *       and the *Impl operation groups) is preserved and used by the hand-written clients. No impl
 *       rename is needed because Q1 ({@code @@clientName(Storage.Queues,"AzureQueueStorage","java")})
 *       already keeps the impl named AzureQueueStorageImpl, which the hand-written layer references.</li>
 *
 *   <li><b>Map the internal exception type.</b> Wrap every impl proxy method so
 *       {@code QueueStorageExceptionInternal} is mapped to the public {@code QueueStorageException}
 *       (async: {@code .onErrorMap(QueueStorageExceptionInternal.class,
 *       ModelHelper::mapToQueueStorageException)}; sync: try/catch rethrowing
 *       {@code ModelHelper.mapToQueueStorageException(e)}). Ported verbatim from the AutoRest
 *       {@code QueueStorageCustomization}.</li>
 * </ol>
 */
public class QueueStorageCustomizations extends Customization {

    private static final String ROOT_FILE_PATH = "src/main/java/com/azure/storage/queue/";

    // Generated public client surface to drop so the hand-written equivalents survive regeneration.
    // QueueClient / QueueAsyncClient collide by name with the hand-written clients (the emitter
    // would otherwise overwrite them); the rest are net-new generated clients/builders/version that
    // the hand-written convenience layer replaces.
    private static final String[] FILES_TO_REMOVE = new String[] {
        "QueueClient.java",
        "QueueAsyncClient.java",
        "ServiceClient.java",
        "ServiceAsyncClient.java",
        "MessagesClient.java",
        "MessagesAsyncClient.java",
        "MessageIdsClient.java",
        "MessageIdsAsyncClient.java",
        "AzureQueueStorageBuilder.java",
        "QueuesServiceVersion.java"
    };

    // Public models the emitter renders immutable (required-args or private constructor, final fields, no setters)
    // that the shipped library exposes as @Fluent (no-arg constructor + setters). See restoreFluentModels.
    private static final List<String> FLUENT_MODELS = Arrays.asList(
        "QueueRetentionPolicy", "QueueMetrics", "QueueCorsRule", "QueueAnalyticsLogging", "QueueSignedIdentifier",
        "GeoReplication", "QueueItem", "QueueServiceStatistics", "SendMessageResult", "UserDelegationKey");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        removeGeneratedPublicClients(editor, logger);
        preserveHandwrittenModuleInfo(editor, logger);
        retargetServiceVersionReferences(editor, logger);
        restoreAccessPolicyDateTimeType(editor, logger);
        restoreFluentModels(customization, logger);
        exposeRawListQueuesResponse(customization.getPackage("com.azure.storage.queue.implementation"), logger);
        updateImplToMapInternalException(customization.getPackage("com.azure.storage.queue.implementation"), logger);
    }

    // AccessPolicy.start/expiry are utcDateTime in the spec but carry @encode("rfc3339-fixed-width") (a non-standard
    // encoding present so the Rust emitter forces exactly 7 fractional-second digits). The typespec-java emitter does
    // not recognize that encoding and degrades the two properties to String, whereas the same emitter correctly emits
    // OffsetDateTime for a plain utcDateTime (see KeyInfo.expiry). The shipped public model
    // com.azure.storage.queue.models.QueueAccessPolicy exposes OffsetDateTime getStartsOn()/getExpiresOn() (and .NET's
    // migration keeps DateTimeOffset), and the hand-written setAccessPolicy path relies on OffsetDateTime (it truncates
    // to seconds before serializing). Restore the OffsetDateTime type using the exact serialization the emitter already
    // produces for utcDateTime (ISO_OFFSET_DATE_TIME on write, CoreUtils.parseBestOffsetDateTime on read); the second
    // truncation in the hand-written layer means no fractional-second precision is emitted, so the fixed-width concern
    // that motivated the encode does not arise on the Java write path.
    private static void restoreAccessPolicyDateTimeType(Editor editor, Logger logger) {
        String path = "src/main/java/com/azure/storage/queue/models/QueueAccessPolicy.java";
        String content = editor.getContents().get(path);
        if (content == null) {
            logger.info("QueueAccessPolicy.java not present; skipping date-time type restoration.");
            return;
        }
        if (content.contains("private OffsetDateTime startsOn;")) {
            logger.info("QueueAccessPolicy already uses OffsetDateTime; skipping.");
            return;
        }
        String updated = content
            .replace("import com.azure.core.annotation.Generated;",
                "import com.azure.core.annotation.Generated;\n"
                    + "import com.azure.core.util.CoreUtils;\n"
                    + "import java.time.OffsetDateTime;\n"
                    + "import java.time.format.DateTimeFormatter;")
            .replace("private String startsOn;", "private OffsetDateTime startsOn;")
            .replace("private String expiresOn;", "private OffsetDateTime expiresOn;")
            .replace("public String getStartsOn() {", "public OffsetDateTime getStartsOn() {")
            .replace("public String getExpiresOn() {", "public OffsetDateTime getExpiresOn() {")
            .replace("public QueueAccessPolicy setStartsOn(String startsOn) {",
                "public QueueAccessPolicy setStartsOn(OffsetDateTime startsOn) {")
            .replace("public QueueAccessPolicy setExpiresOn(String expiresOn) {",
                "public QueueAccessPolicy setExpiresOn(OffsetDateTime expiresOn) {")
            .replace("xmlWriter.writeStringElement(\"Start\", this.startsOn);",
                "xmlWriter.writeStringElement(\"Start\",\n"
                    + "            this.startsOn == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.startsOn));")
            .replace("xmlWriter.writeStringElement(\"Expiry\", this.expiresOn);",
                "xmlWriter.writeStringElement(\"Expiry\",\n"
                    + "            this.expiresOn == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.expiresOn));")
            .replace("deserializedQueueAccessPolicy.startsOn = reader.getStringElement();",
                "deserializedQueueAccessPolicy.startsOn = reader.getNullableElement(dateString -> CoreUtils.parseBestOffsetDateTime(dateString));")
            .replace("deserializedQueueAccessPolicy.expiresOn = reader.getStringElement();",
                "deserializedQueueAccessPolicy.expiresOn = reader.getNullableElement(dateString -> CoreUtils.parseBestOffsetDateTime(dateString));");
        editor.replaceFile(path, updated);
        logger.info("Restored OffsetDateTime type for QueueAccessPolicy startsOn/expiresOn.");
    }

    // The typespec-java emitter makes every model that has a required spec property immutable: input models
    // (required @@usage) get a public required-args constructor + final fields + no setters, and output-only
    // models get a private constructor + final fields + no setters (@Immutable). The shipped Storage public
    // models are uniformly @Fluent (no-arg constructor + fluent setters); all four hand-written clients, the
    // SAS helpers, the samples and the recorded tests build against that fluent shape, and .NET's migration
    // keeps the same mutable models. Restore the fluent shape so the migration stays 100% source-compatible
    // (no revapi break). The transform per model is uniform: swap @Immutable -> @Fluent, drop `final` from the
    // instance fields, replace the generated constructor with a public no-arg constructor, add a fluent setter
    // for every property that lost one, and rewrite fromXml to build via the no-arg constructor + field
    // assignment. DateTimeRfc1123 backing fields (GeoReplication.lastSyncTime, SendMessageResult.*Time) keep
    // their OffsetDateTime public accessors, reusing the exact OffsetDateTime<->DateTimeRfc1123 conversion the
    // emitter already generates on those fields.
    private static void restoreFluentModels(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.storage.queue.models");
        for (String className : FLUENT_MODELS) {
            if (models.getClass(className) == null) {
                logger.info("Model {} not present; skipping fluent restoration.", className);
                continue;
            }
            models.getClass(className).customizeAst(ast -> {
                ast.addImport("com.azure.core.annotation.Fluent");
                ast.getImports().removeIf(i -> i.getNameAsString().equals("com.azure.core.annotation.Immutable"));
                ast.getClassByName(className).ifPresent(clazz -> makeModelFluent(clazz, logger));
            });
            logger.info("Restored @Fluent shape for {}.", className);
        }
    }

    private static void makeModelFluent(ClassOrInterfaceDeclaration clazz, Logger logger) {
        String className = clazz.getNameAsString();

        // @Immutable -> @Fluent (QueueRetentionPolicy/QueueMetrics are already @Fluent).
        clazz.getAnnotationByName("Immutable").ifPresent(a -> a.remove());
        if (!clazz.isAnnotationPresent("Fluent")) {
            clazz.addMarkerAnnotation("Fluent");
        }

        // Drop `final` from the instance fields so they can be set after construction.
        clazz.getFields().forEach(field -> {
            if (!field.isStatic()) {
                field.setFinal(false);
            }
        });

        // Replace the generated constructor(s) with a single public no-arg constructor.
        new ArrayList<>(clazz.getConstructors()).forEach(ConstructorDeclaration::remove);
        ConstructorDeclaration ctor = clazz.addConstructor(Modifier.Keyword.PUBLIC);
        ctor.addMarkerAnnotation("Generated");
        ctor.setBody(new BlockStmt());

        // Add a fluent setter for every property that lost one. The setter's parameter type mirrors the
        // property's public accessor return type (which differs from the backing field for DateTimeRfc1123).
        for (FieldDeclaration field : clazz.getFields()) {
            if (field.isStatic()) {
                continue;
            }
            String fieldName = field.getVariable(0).getNameAsString();
            String setterName = "set" + capitalize(fieldName);
            if (!clazz.getMethodsByName(setterName).isEmpty()) {
                continue;
            }
            String fieldType = field.getElementType().asString();
            // Clone so the parameter does not steal the accessor/field's live Type node from the AST.
            Type accessorType = accessorReturnType(clazz, fieldName).orElse(field.getElementType()).clone();
            String body;
            if ("DateTimeRfc1123".equals(fieldType) && "OffsetDateTime".equals(accessorType.asString())) {
                body = "{ if (" + fieldName + " == null) { this." + fieldName + " = null; } else { this."
                    + fieldName + " = new DateTimeRfc1123(" + fieldName + "); } return this; }";
            } else {
                body = "{ this." + fieldName + " = " + fieldName + "; return this; }";
            }
            MethodDeclaration setter = clazz.addMethod(setterName, Modifier.Keyword.PUBLIC);
            setter.addMarkerAnnotation("Generated");
            setter.setType(className);
            setter.addParameter(new Parameter(accessorType, fieldName));
            setter.setBody(StaticJavaParser.parseBlock(body));
        }

        // Rewrite fromXml: the emitter builds the instance with the (now removed) generated constructor, e.g.
        // `return new X(a, b);` or `X d = new X(a); d.opt = opt;`. Rebuild via the no-arg constructor and assign
        // each former constructor argument to its field (converting OffsetDateTime -> DateTimeRfc1123 where the
        // backing field requires it), matching how the emitter already assigns the optional fields.
        clazz.findAll(ObjectCreationExpr.class).stream()
            .filter(oce -> oce.getType().getNameAsString().equals(className) && !oce.getArguments().isEmpty())
            .forEach(oce -> rewriteFromXmlConstruction(clazz, oce));
    }

    private static void rewriteFromXmlConstruction(ClassOrInterfaceDeclaration clazz, ObjectCreationExpr oce) {
        String className = clazz.getNameAsString();
        List<String> argNames = new ArrayList<>();
        oce.getArguments().forEach(arg -> argNames.add(arg.toString()));
        oce.setArguments(new NodeList<>()); // new X()

        Optional<VariableDeclarator> asInitializer = oce.getParentNode()
            .filter(p -> p instanceof VariableDeclarator).map(p -> (VariableDeclarator) p);
        if (asInitializer.isPresent()) {
            // Shape: `X deserializedX = new X(args); <existing optional assignments>`
            String localName = asInitializer.get().getNameAsString();
            Statement declStmt = findAncestor(oce, Statement.class).orElseThrow(
                () -> new IllegalStateException("No enclosing statement for " + className + " construction."));
            BlockStmt block = (BlockStmt) declStmt.getParentNode().orElseThrow(
                () -> new IllegalStateException("No enclosing block for " + className + " construction."));
            int idx = block.getStatements().indexOf(declStmt);
            int offset = 1;
            for (String argName : argNames) {
                block.addStatement(idx + offset, StaticJavaParser.parseStatement(
                    fieldAssignment(clazz, localName, argName)));
                offset++;
            }
        } else {
            // Shape: `return new X(args);` -> introduce a local, assign, and return it.
            ReturnStmt ret = findAncestor(oce, ReturnStmt.class).orElseThrow(
                () -> new IllegalStateException("Unexpected " + className + " construction context."));
            String localName = "deserialized" + className;
            BlockStmt rebuilt = new BlockStmt();
            rebuilt.addStatement(StaticJavaParser.parseStatement(className + " " + localName + " = new " + className + "();"));
            for (String argName : argNames) {
                rebuilt.addStatement(StaticJavaParser.parseStatement(fieldAssignment(clazz, localName, argName)));
            }
            rebuilt.addStatement(new ReturnStmt(StaticJavaParser.parseExpression(localName)));
            ret.replace(rebuilt);
        }
    }

    // Assignment for a former constructor argument (local var name == field name). OffsetDateTime locals feeding
    // a DateTimeRfc1123 field are converted, mirroring the emitter's own constructor logic for those fields.
    private static String fieldAssignment(ClassOrInterfaceDeclaration clazz, String localName, String fieldName) {
        boolean rfc1123 = clazz.getFieldByName(fieldName)
            .map(f -> "DateTimeRfc1123".equals(f.getElementType().asString())).orElse(false);
        if (rfc1123) {
            return localName + "." + fieldName + " = " + fieldName + " == null ? null : new DateTimeRfc1123("
                + fieldName + ");";
        }
        return localName + "." + fieldName + " = " + fieldName + ";";
    }

    private static Optional<Type> accessorReturnType(ClassOrInterfaceDeclaration clazz, String fieldName) {
        String suffix = capitalize(fieldName);
        for (String prefix : new String[] { "get", "is" }) {
            List<MethodDeclaration> getters = clazz.getMethodsByName(prefix + suffix);
            for (MethodDeclaration getter : getters) {
                if (getter.getParameters().isEmpty()) {
                    return Optional.of(getter.getType());
                }
            }
        }
        return Optional.empty();
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static <T extends Node> Optional<T> findAncestor(Node node, Class<T> type) {
        Optional<Node> parent = node.getParentNode();
        while (parent.isPresent()) {
            Node current = parent.get();
            if (type.isInstance(current)) {
                return Optional.of(type.cast(current));
            }
            parent = current.getParentNode();
        }
        return Optional.empty();
    }

    // The emitter models "List Queues" with @list + a body-sourced @continuationToken (NextMarker), but the
    // typespec-java protocol paging path (see ServicesImpl.getQueuesSinglePage[Async]) discards that marker
    // (continuation token hard-coded to null) and JSON-parses the XML body, so it cannot drive the hand-written
    // PagedFlux/PagedIterable<QueueItem> continuation loop. Until the emitter honors body @continuationToken (tracked
    // against @azure-tools/typespec-java; Go's azqueue and .NET's Azure.Storage.Queues both preserve NextMarker from
    // the same spec), expose a raw single-response accessor over the existing proxy call so the hand-written service
    // clients can deserialize ListQueuesSegmentResponse (getQueueItems + getNextMarker) themselves. This mirrors the
    // shape .NET's generator emits by default (GetQueuesSegment). The two injected methods are intentionally left
    // without exception mapping here so updateImplToMapInternalException wraps them identically to every other
    // WithResponse accessor. The accessor lives in the non-exported implementation package, so it is not public API.
    private static void exposeRawListQueuesResponse(PackageCustomization implPackage, Logger logger) {
        if (implPackage.getClass("ServicesImpl") == null) {
            logger.info("ServicesImpl not present; skipping raw list-queues accessor injection.");
            return;
        }
        implPackage.getClass("ServicesImpl").customizeAst(ast -> ast.getClassByName("ServicesImpl").ifPresent(clazz -> {
            clazz.addMember(StaticJavaParser.parseMethodDeclaration(
                "@ServiceMethod(returns = ReturnType.SINGLE)\n"
                    + "public Mono<Response<BinaryData>> getQueuesWithResponseAsync(RequestOptions requestOptions) {\n"
                    + "    final String accept = \"application/xml\";\n"
                    + "    return FluxUtil.withContext(context -> service.getQueues(this.client.getUrl(),\n"
                    + "        this.client.getServiceVersion().getVersion(), accept, requestOptions, context));\n"
                    + "}"));
            clazz.addMember(StaticJavaParser.parseMethodDeclaration(
                "@ServiceMethod(returns = ReturnType.SINGLE)\n"
                    + "public Response<BinaryData> getQueuesWithResponse(RequestOptions requestOptions) {\n"
                    + "    final String accept = \"application/xml\";\n"
                    + "    return service.getQueuesSync(this.client.getUrl(),\n"
                    + "        this.client.getServiceVersion().getVersion(), accept, requestOptions, Context.NONE);\n"
                    + "}"));
            logger.info("Injected raw getQueuesWithResponse[Async] accessors into ServicesImpl.");
        }));
    }

    // The emitter derives the service-version enum name from the "Queues" service namespace, emitting
    // QueuesServiceVersion (and referencing it from every impl proxy). The hand-written public layer
    // (builders/clients) uses the shipped QueueServiceVersion. We drop the generated QueuesServiceVersion.java
    // (in FILES_TO_REMOVE) and rewrite the impl references to the hand-written QueueServiceVersion, mirroring
    // the service-version retarget AppConfiguration does in its customization.
    private static void retargetServiceVersionReferences(Editor editor, Logger logger) {
        String implDir = "src/main/java/com/azure/storage/queue/implementation/";
        for (String fileName : new String[] {
            "AzureQueueStorageImpl.java", "ServicesImpl.java", "QueuesImpl.java",
            "MessagesImpl.java", "MessageIdsImpl.java" }) {
            String path = implDir + fileName;
            String content = editor.getContents().get(path);
            if (content == null || !content.contains("QueuesServiceVersion")) {
                continue;
            }
            // Rewrite both the import (com.azure.storage.queue.QueuesServiceVersion) and every bare reference.
            String updated = content.replace("QueuesServiceVersion", "QueueServiceVersion");
            editor.replaceFile(path, updated);
            logger.info("Retargeted QueuesServiceVersion -> QueueServiceVersion in {}.", fileName);
        }
    }

    // The emitter emits a generic module-info.java that drops the Storage-specific module directives
    // (requires transitive com.azure.storage.common, exports com.azure.storage.queue.sas,
    // opens com.azure.storage.queue.implementation). The shipped module-info is hand-written and is the
    // authoritative superset, so drop the generated copy to avoid overwriting it.
    private static void preserveHandwrittenModuleInfo(Editor editor, Logger logger) {
        String path = "src/main/java/module-info.java";
        if (editor.getContents().containsKey(path)) {
            editor.removeFile(path);
            logger.info("Removed generated module-info.java to preserve the hand-written module descriptor.");
        } else {
            logger.info("Generated module-info.java not present; nothing to remove.");
        }
    }

    private static void removeGeneratedPublicClients(Editor editor, Logger logger) {
        for (String fileName : FILES_TO_REMOVE) {
            String path = ROOT_FILE_PATH + fileName;
            if (editor.getContents().containsKey(path)) {
                editor.removeFile(path);
                logger.info("Removed generated public client {}", path);
            } else {
                logger.info("Generated file {} not present; skipping removal.", path);
            }
        }
    }

    /**
     * Customizes the implementation classes that will perform calls to the service. The following logic is used:
     * <p>
     * - Check for the return of the method not equaling to PagedFlux, PagedIterable, PollerFlux, or SyncPoller. Those
     * types wrap other APIs and those APIs being update is the correct change.
     * - For asynchronous methods, add a call to
     * {@code .onErrorMap(QueueStorageExceptionInternal.class, ModelHelper::mapToQueueStorageException)} to handle
     * mapping QueueStorageExceptionInternal to QueueStorageException.
     * - For synchronous methods, wrap the return statement in a try-catch block that catches
     * QueueStorageExceptionInternal and rethrows {@code ModelHelper.mapToQueueStorageException(e)}. Or, for void
     * methods wrap the last statement.
     *
     * @param implPackage The implementation package.
     */
    private static void updateImplToMapInternalException(PackageCustomization implPackage, Logger logger) {
        // The operation-group set depends on the @@clientLocation split in client.tsp. Guard each
        // class so the customization is robust whether the impl is the 4-group AutoRest topology
        // (MessageIds/Messages/Queues/Services) or a reduced set.
        List<String> implsToUpdate = Arrays.asList("MessageIdsImpl", "MessagesImpl", "QueuesImpl", "ServicesImpl");
        for (String implToUpdate : implsToUpdate) {
            if (implPackage.getClass(implToUpdate) == null) {
                logger.info("Impl class {} not present; skipping exception mapping.", implToUpdate);
                continue;
            }
            implPackage.getClass(implToUpdate).customizeAst(ast -> {
                ast.addImport("com.azure.storage.queue.implementation.util.ModelHelper");
                ast.addImport("com.azure.storage.queue.models.QueueStorageException");
                ast.addImport("com.azure.storage.queue.implementation.models.QueueStorageExceptionInternal");
                // The emitter annotates the @ServiceInterface methods with a default
                // @UnexpectedResponseExceptionType(HttpResponseException.class) plus per-status-code entries mapping
                // 401/404/409 to generic azure-core exceptions (ClientAuthenticationException, ResourceNotFoundException,
                // ResourceModifiedException). AutoRest emitted only the single default. Two problems with the per-code
                // entries: (1) their getValue() returns Object, so azure-core cannot deserialize the XML error body via
                // azure-xml and falls back to Jackson; (2) they would surface a non-QueueStorageException type to callers
                // for those status codes, breaking public behavior. Remove every per-code entry (any
                // @UnexpectedResponseExceptionType carrying a 'code' member) so all unexpected responses route through
                // the single default, matching AutoRest.
                ast.findAll(NormalAnnotationExpr.class).stream()
                    .filter(anno -> anno.getNameAsString().equals("UnexpectedResponseExceptionType")
                        && anno.getPairs().stream().anyMatch(p -> p.getNameAsString().equals("code")))
                    .collect(java.util.stream.Collectors.toList())
                    .forEach(Node::remove);
                // Retarget the remaining default annotation to the internal exception type so the pipeline throws
                // QueueStorageExceptionInternal (a subtype of HttpResponseException), which the error mapping below maps
                // to the public QueueStorageException. The durable fix is the tspconfig 'default-http-exception-type'
                // option; this keeps the customization self-sufficient regardless of that setting.
                ast.findAll(SingleMemberAnnotationExpr.class).forEach(anno -> {
                    if (anno.getNameAsString().equals("UnexpectedResponseExceptionType")
                        && "HttpResponseException.class".equals(anno.getMemberValue().toString())) {
                        anno.setMemberValue(StaticJavaParser.parseExpression("QueueStorageExceptionInternal.class"));
                    }
                });
                ast.getClassByName(implToUpdate).ifPresent(clazz -> {
                    clazz.getMethods().forEach(methodDeclaration -> {
                        Type returnType = methodDeclaration.getType();
                        // The way code generation works we only need to update the methods that have a class return type.
                        // As non-class return types, such as "void", call into the Response<Void> methods.
                        if (!returnType.isClassOrInterfaceType()) {
                            return;
                        }

                        ClassOrInterfaceType returnTypeClass = returnType.asClassOrInterfaceType();
                        String returnTypeName = returnTypeClass.getNameAsString();
                        if (returnTypeName.equals("PagedFlux") || returnTypeName.equals("PagedIterable")
                            || returnTypeName.equals("PollerFlux") || returnTypeName.equals("SyncPoller")) {
                            return;
                        }

                        if (returnTypeName.equals("Mono") || returnTypeName.equals("Flux")) {
                            addErrorMappingToAsyncMethod(methodDeclaration);
                        } else {
                            addErrorMappingToSyncMethod(methodDeclaration);
                        }
                    });
                });
            });
            logger.info("Applied QueueStorageExceptionInternal -> QueueStorageException mapping to {}.", implToUpdate);
        }
    }

    private static void addErrorMappingToAsyncMethod(MethodDeclaration method) {
        BlockStmt body = method.getBody().get();

        // Bit of hack to insert the 'onErrorMap' in the right location.
        // Unfortunately, 'onErrorMap' returns <T> which for some calls breaks typing, such as Void -> Object or
        // PagedResponse -> PagedResponseBase. So, 'onErrorMap' needs to be inserted after the first method call.
        // To do this, we track the first found '(' and the associated closing ')' to insert 'onErrorMap' after the ')'.
        // So, 'service.methodCall(parameters).map()' becomes 'service.methodCall(parameters).onErrorMap().map()'.
        String originalReturnStatement = body.getStatement(body.getStatements().size() - 1).asReturnStmt()
            .getExpression().get().toString();
        int insertionPoint = findAsyncOnErrorMapInsertionPoint(originalReturnStatement);
        String newReturnStatement = "return " + originalReturnStatement.substring(0, insertionPoint)
            + ".onErrorMap(QueueStorageExceptionInternal.class, ModelHelper::mapToQueueStorageException)"
            + originalReturnStatement.substring(insertionPoint) + ";";
        try {
            Statement newReturn = StaticJavaParser.parseStatement(newReturnStatement);
            body.getStatements().set(body.getStatements().size() - 1, newReturn);
        } catch (ParseProblemException ex) {
            throw new RuntimeException("Failed to parse: " + newReturnStatement, ex);
        }
    }

    private static int findAsyncOnErrorMapInsertionPoint(String returnStatement) {
        int openParenthesis = 0;
        int closeParenthesis = 0;
        for (int i = 0; i < returnStatement.length(); i++) {
            char c = returnStatement.charAt(i);
            if (c == '(') {
                openParenthesis++;
            } else if (c == ')') {
                closeParenthesis++;
                if (openParenthesis == closeParenthesis) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    private static void addErrorMappingToSyncMethod(MethodDeclaration method) {
        // Turn the entire method into a BlockStmt that will be used as the try block.
        BlockStmt tryBlock = method.getBody().get();
        BlockStmt catchBlock = new BlockStmt(new NodeList<>(StaticJavaParser.parseStatement(
            "throw ModelHelper.mapToQueueStorageException(internalException);")));
        Parameter catchParameter = new Parameter().setType("QueueStorageExceptionInternal")
            .setName("internalException");
        CatchClause catchClause = new CatchClause(catchParameter, catchBlock);
        TryStmt tryCatchMap = new TryStmt(tryBlock, new NodeList<>(catchClause), null);

        // Replace the last statement with the try-catch block.
        method.setBody(new BlockStmt(new NodeList<>(tryCatchMap)));
    }
}
