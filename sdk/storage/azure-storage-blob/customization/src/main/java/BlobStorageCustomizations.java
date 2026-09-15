// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
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
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * TypeSpec customization for azure-storage-blob.
 * <p>
 * The public surface (BlobClient/BlobAsyncClient, BlobContainer*, BlobService*, the specialized Append/Block/Page
 * clients, and the *ClientBuilder types) is hand-written and delegates to the generated implementation/*Impl
 * operation layer (AzureBlobStorageImpl + Blobs/Containers/Services/BlockBlobs/PageBlobs/AppendBlobsImpl).
 * typespec-java also emits convenience clients + a builder + a service-version enum on top of that layer; those
 * are removed here so the hand-written surface is preserved. removeFile operates on the emitter's output before it
 * is copied over the SDK, so removing a generated file leaves the hand-written file of the same name untouched.
 */
public class BlobStorageCustomizations extends Customization {

    private static final String PKG_ROOT = "src/main/java/com/azure/storage/blob/";

    private static final String MODELS_PACKAGE = "com.azure.storage.blob.models";

    private static final String IMPL_PACKAGE = "com.azure.storage.blob.implementation";

    // Generated convenience clients + builder + service version emitted on top of the implementation/*Impl
    // operation layer. The public surface is hand-written, so delete the generated ones. Names that collide with a
    // hand-written file (BlobClient/BlobAsyncClient/BlobServiceVersion) are removed so the emitter output does not
    // overwrite the hand-written copy; the rest (Container/Service/AppendBlob/BlockBlob/PageBlob convenience clients,
    // whose real clients live in the specialized subpackage) are spurious top-level files.
    private static final List<String> GENERATED_CLIENTS_TO_REMOVE = Arrays.asList(
        // The per-resource convenience clients are NOT removed: they carry the typed WithResponse API and are
        // relocated into implementation instead (see CONVENIENCE_CLIENTS_TO_RELOCATE). Only the generated builder,
        // the generated service-version enum (the hand-written BlobServiceVersion is authoritative) and the
        // generated package-info are dropped.
        "AzureBlobStorageBuilder",
        "BlobServiceVersion",
        "package-info");

    // Hand-authored module descriptor carries the full requires/exports/opens; typespec-java regenerates a minimal
    // version that would overwrite it, so drop the generated copy and keep the hand-written descriptor.
    private static final List<String> GENERATED_DESCRIPTOR_FILES_TO_REMOVE = Arrays.asList(
        "src/main/java/module-info.java");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        logGeneratedPublicClients(editor, logger);
        relocateConvenienceClientsToImplementation(editor, logger);
        removeGeneratedFiles(editor, logger);
        fixXmlSerializerRedundantCast(editor, logger);
        fixUrlAcronymHeaderNames(editor, logger);
        customizeQueryFormat(editor, logger);
        retypeStreamingResponses(editor, logger);
        retypeArrowListingResponses(editor, logger);
        addSdkOnlyIsPrefix(customization.getPackage(IMPL_PACKAGE + ".models"), logger);
        removeBufferedStreamingConvenienceMethods(customization.getPackage(IMPL_PACKAGE), logger);
        restoreFluentModels(customization, logger);
        restoreHeaderSetters(customization.getPackage(IMPL_PACKAGE + ".models"), logger);
        addContentTypeHeaderProperty(customization.getPackage(IMPL_PACKAGE + ".models"), logger);
        restoreObjectReplicationHeaderCollection(editor, logger);
        removeMultipartBatchConvenience(customization, editor, logger);
        // Follow-up stages (ported from the queue customization) build on this removal pass:
        //   - restoreMetadataHeaderCollection (x-ms-meta-* on *GetPropertiesHeaders)
        //   - updateImplToMapInternalException (BlobStorageExceptionInternal -> BlobStorageException)
        // Each is added once its stage is reconciled against the RevApi/compile report.
    }

    private static void removeGeneratedFiles(Editor editor, Logger logger) {
        for (String className : GENERATED_CLIENTS_TO_REMOVE) {
            removeFileIfPresent(editor, PKG_ROOT + className + ".java", logger);
        }
        for (String path : GENERATED_DESCRIPTOR_FILES_TO_REMOVE) {
            removeFileIfPresent(editor, path, logger);
        }

    }

    private static void removeFileIfPresent(Editor editor, String path, Logger logger) {
        if (editor.getContents().containsKey(path)) {
            editor.removeFile(path);
            logger.info("Removed generated file {}", path);
        } else {
            logger.info("Generated file {} not present; skipping removal.", path);
        }
    }

    // Models that shipped as @Fluent (public no-arg ctor + setters) before the TypeSpec migration. With
    // required-fields-as-ctor-args:true these regenerate as @Immutable with a required-args ctor and no setters --
    // a breaking change for the public models and a compile break for the hand-written code that populates the
    // implementation ones (e.g. ArrowBlobListDeserializer). Derived from the shipped setters each model lost;
    // expand from the RevApi "method removed" report.
    private static final List<String> PUBLIC_FLUENT_MODELS_TO_RESTORE = Arrays.asList(
        "UserDelegationKey", "BlobCorsRule", "BlobAnalyticsLogging", "BlobRetentionPolicy", "BlobAccessPolicy",
        "BlobSignedIdentifier", "BlobMetrics", "BlobServiceStatistics", "KeyInfo", "Block", "BlockList",
        "BlockLookupList", "PageRange", "ClearRange", "GeoReplication", "StaticWebsite", "BlobPrefix");

    private static final List<String> IMPL_FLUENT_MODELS_TO_RESTORE = Arrays.asList(
        "BlobItemPropertiesInternal", "BlobItemInternal", "FilterBlobItem", "QueryFormat", "QueryRequest",
        "QuerySerialization", "BlobTag", "BlobTags", "BlobName", "BlobHierarchyListSegment",
        // BlobQueryReader builds these field by field when translating the public query serialization options.
        "ArrowFieldInternal", "ArrowTextConfigurationInternal");

    // The public BlobDownloadHeaders and BlobQueryHeaders are @Fluent wrappers that delegate every setter to the
    // generated header class, which the emitter makes read-only. These need the setters back, but NOT the rest of
    // the fluent-model treatment: a header class is built from the raw HttpHeaders, so it must keep the
    // (HttpHeaders) constructor that restoreFluentModels replaces with a no-arg one.
    private static final List<String> HEADER_MODELS_TO_MAKE_SETTABLE
        = Arrays.asList("BlobsDownloadHeaders", "BlobsQueryHeaders");

    // Submit Batch sends a multipart/mixed body of nested HTTP requests. The emitter models it as a
    // SubmitBatchRequest whose single part is a BodyFileDetails, generates it as JsonSerializable, and types the
    // convenience methods as ResponseBase<...Headers, SubmitBatchRequest> -- the REQUEST model as the response body.
    // None of that compiles or describes the operation. The protocol methods
    // (submitBatchWithResponseInternal(long contentLength, BinaryData body, RequestOptions)) are correct and are
    // what azure-storage-blob-batch, which owns the batch clients, calls; so drop the convenience layer and the two
    // multipart models and keep the protocol methods.
    private static final List<String> BATCH_CLIENTS = Arrays.asList("BlobServiceAsyncClientInternal",
        "BlobServiceClientInternal", "BlobContainerAsyncClientInternal", "BlobContainerClientInternal");

    private static void removeMultipartBatchConvenience(LibraryCustomization customization, Editor editor,
        Logger logger) {
        PackageCustomization implementation = customization.getPackage(IMPL_PACKAGE);
        for (String className : BATCH_CLIENTS) {
            if (implementation.getClass(className) == null) {
                logger.info("{} not present; skipping the batch convenience removal.", className);
                continue;
            }
            implementation.getClass(className).customizeAst(ast -> {
                ast.getImports()
                    .removeIf(i -> i.getNameAsString().endsWith(".SubmitBatchRequest")
                        || i.getNameAsString().endsWith(".BodyFileDetails"));
                ast.getClassByName(className).ifPresent(clazz -> {
                    int removed = 0;
                    for (MethodDeclaration method : new ArrayList<>(clazz.getMethods())) {
                        if (method.getNameAsString().startsWith("submitBatch")
                            && !method.getNameAsString().contains("Internal")) {
                            method.remove();
                            removed++;
                        }
                    }
                    logger.info("Removed {} batch convenience methods from {}.", removed, className);
                });
            });
        }
        removeFileIfPresent(editor, PKG_ROOT + "implementation/models/SubmitBatchRequest.java", logger);
        removeFileIfPresent(editor, PKG_ROOT + "implementation/models/BodyFileDetails.java", logger);
    }

    // x-ms-or-* is a prefix collection, like x-ms-meta-*, but nothing in the spec can tell an emitter that (see the
    // NOTE on ObjectReplicationHeaders in Common/models.tsp). With the property typed as Record<string> the emitter
    // takes the only other reading available to it and JSON-deserializes a single x-ms-or header, which is the wrong
    // wire shape and does not even compile -- it references TypeReference and JacksonAdapter without importing them.
    // Replace that with the same prefix scan the emitter itself generates for x-ms-meta-.
    private static final String OBJECT_REPLICATION_JSON_BLOCK
        = "String objectReplicationRules = rawHeaders\\.getValue\\(X_MS_OR\\);\\s*try \\{[\\s\\S]*?"
            + "\\} catch \\(IOException ex\\) \\{[^}]*\\}";

    private static final String OBJECT_REPLICATION_PREFIX_SCAN
        = "Map<String, String> objectReplicationRuleHeaderCollection = new LinkedHashMap<>();\n"
            + "        rawHeaders.stream().forEach(header -> {\n"
            + "            String headerName = header.getName();\n"
            + "            if (headerName.toLowerCase(Locale.ROOT).startsWith(\"x-ms-or-\")) {\n"
            + "                objectReplicationRuleHeaderCollection.put(headerName.substring(8), header.getValue());\n"
            + "            }\n"
            + "        });\n"
            + "        this.objectReplicationRules = objectReplicationRuleHeaderCollection;";

    private static final List<String> OBJECT_REPLICATION_HEADER_MODELS
        = Arrays.asList("BlobsDownloadHeaders", "BlobsGetPropertiesHeaders");

    private static void restoreObjectReplicationHeaderCollection(Editor editor, Logger logger) {
        for (String className : OBJECT_REPLICATION_HEADER_MODELS) {
            String path = PKG_ROOT + "implementation/models/" + className + ".java";
            String content = editor.getContents().get(path);
            if (content == null) {
                logger.info("{} not present; skipping the object replication collection.", className);
                continue;
            }
            String updated = content.replaceAll(OBJECT_REPLICATION_JSON_BLOCK,
                OBJECT_REPLICATION_PREFIX_SCAN);
            if (updated.equals(content)) {
                throw new IllegalStateException(
                    "Object replication header block not found in " + className + "; the emitter output changed.");
            }
            editor.replaceFile(path, updated);
            logger.info("Restored the x-ms-or-* header collection in {}.", className);
        }
    }

    // The spec does declare Content-Type on these responses, but TypeSpec treats it as the response's content-type
    // metadata rather than a header (hence the content-type-ignored suppressions on the operation templates), so
    // the emitter leaves it out of the header model. getContentType()/setContentType() are public API on the
    // shipped BlobDownloadHeaders and BlobQueryHeaders, so add the property back and populate it from the raw
    // headers the class is already built from.
    private static void addContentTypeHeaderProperty(PackageCustomization implModels, Logger logger) {
        for (String className : HEADER_MODELS_TO_MAKE_SETTABLE) {
            if (implModels.getClass(className) == null) {
                logger.info("Header model {} not present; skipping the Content-Type property.", className);
                continue;
            }
            implModels.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
                if (!clazz.getFieldByName("contentType").isPresent()) {
                    clazz.addField("String", "contentType", Modifier.Keyword.PRIVATE)
                        .setJavadocComment("The Content-Type property.");
                }
                clazz.getConstructors()
                    .stream()
                    .filter(ctor -> ctor.getParameters().size() == 1
                        && "HttpHeaders".equals(ctor.getParameter(0).getType().asString()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                        "No (HttpHeaders) constructor on " + className + " to populate Content-Type from."))
                    .getBody()
                    .addStatement(StaticJavaParser
                        .parseStatement("this.contentType = rawHeaders.getValue(HttpHeaderName.CONTENT_TYPE);"));
                if (clazz.getMethodsByName("getContentType").isEmpty()) {
                    MethodDeclaration getter = clazz.addMethod("getContentType", Modifier.Keyword.PUBLIC);
                    getter.setType("String");
                    getter.setJavadocComment(new Javadoc(
                        JavadocDescription.parseText("Get the contentType property: The Content-Type property."))
                            .addBlockTag("return", "the contentType value."));
                    getter.setBody(StaticJavaParser.parseBlock("{ return this.contentType; }"));
                }
                addFluentSetters(clazz);
            }));
            logger.info("Added the Content-Type property to {}.", className);
        }
    }

    private static void restoreHeaderSetters(PackageCustomization implModels, Logger logger) {
        for (String className : HEADER_MODELS_TO_MAKE_SETTABLE) {
            if (implModels.getClass(className) == null) {
                logger.info("Header model {} not present; skipping setter restoration.", className);
                continue;
            }
            implModels.getClass(className).customizeAst(ast -> {
                ast.addImport("com.azure.core.annotation.Fluent");
                ast.getImports().removeIf(i -> i.getNameAsString().equals("com.azure.core.annotation.Immutable"));
                ast.getClassByName(className).ifPresent(clazz -> {
                    clazz.getAnnotationByName("Immutable").ifPresent(a -> a.remove());
                    if (!clazz.isAnnotationPresent("Fluent")) {
                        clazz.addMarkerAnnotation("Fluent");
                    }
                    clazz.getFields().forEach(field -> {
                        if (!field.isStatic()) {
                            field.setFinal(false);
                        }
                    });
                    addFluentSetters(clazz);
                });
            });
            logger.info("Restored the fluent setters on {}.", className);
        }
    }

    private static void restoreFluentModels(LibraryCustomization customization, Logger logger) {
        restoreFluentModels(customization.getPackage(MODELS_PACKAGE), PUBLIC_FLUENT_MODELS_TO_RESTORE, logger);
        restoreFluentModels(customization.getPackage(IMPL_PACKAGE + ".models"), IMPL_FLUENT_MODELS_TO_RESTORE,
            logger);
    }

    private static void restoreFluentModels(PackageCustomization models, List<String> classNames, Logger logger) {
        for (String className : classNames) {
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

        clazz.getAnnotationByName("Immutable").ifPresent(a -> a.remove());
        if (!clazz.isAnnotationPresent("Fluent")) {
            clazz.addMarkerAnnotation("Fluent");
        }

        clazz.getFields().forEach(field -> {
            if (!field.isStatic()) {
                field.setFinal(false);
            }
        });

        new ArrayList<>(clazz.getConstructors()).forEach(ConstructorDeclaration::remove);
        ConstructorDeclaration ctor = clazz.addConstructor(Modifier.Keyword.PUBLIC);
        ctor.addMarkerAnnotation("Generated");
        ctor.setJavadocComment(
            new Javadoc(JavadocDescription.parseText("Creates an instance of " + className + " class.")));
        ctor.setBody(new BlockStmt());

        addFluentSetters(clazz);

        clazz.findAll(ObjectCreationExpr.class).stream()
            .filter(oce -> oce.getType().getNameAsString().equals(className) && !oce.getArguments().isEmpty())
            .forEach(oce -> rewriteFromXmlConstruction(clazz, oce));
    }

    // Adds a fluent setter for each instance field that does not already have one, immediately after the field's
    // getter so the pairs sit together as they did in the shipped models. Shared by the fluent-model restoration
    // and the header-setter restoration.
    private static void addFluentSetters(ClassOrInterfaceDeclaration clazz) {
        String className = clazz.getNameAsString();
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
            String description = accessorDescription(clazz, fieldName);
            String summary = description == null
                ? "Set the " + fieldName + " property."
                : "Set the " + fieldName + " property: " + description;
            setter.setJavadocComment(new Javadoc(JavadocDescription.parseText(summary))
                .addBlockTag("param", fieldName, "the " + fieldName + " value to set.")
                .addBlockTag("return", "the " + className + " object itself."));
            setter.setBody(StaticJavaParser.parseBlock(body));
        }
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
            // Shape: `return new X(args);` -> introduce a local, assign its fields, and return it. The statements are
            // inserted directly into the enclosing block (not wrapped in a nested block, which Checkstyle rejects).
            ReturnStmt ret = findAncestor(oce, ReturnStmt.class).orElseThrow(
                () -> new IllegalStateException("Unexpected " + className + " construction context."));
            BlockStmt block = (BlockStmt) ret.getParentNode().orElseThrow(
                () -> new IllegalStateException("No enclosing block for " + className + " return."));
            String localName = "deserialized" + className;
            int idx = block.getStatements().indexOf(ret);
            block.addStatement(idx,
                StaticJavaParser.parseStatement(className + " " + localName + " = new " + className + "();"));
            int offset = 1;
            for (String argName : argNames) {
                block.addStatement(idx + offset, StaticJavaParser.parseStatement(
                    fieldAssignment(clazz, localName, argName)));
                offset++;
            }
            ret.setExpression(StaticJavaParser.parseExpression(localName));
        }
    }

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

    private static String accessorDescription(ClassOrInterfaceDeclaration clazz, String fieldName) {
        String suffix = capitalize(fieldName);
        for (String prefix : new String[] { "get", "is" }) {
            for (MethodDeclaration getter : clazz.getMethodsByName(prefix + suffix)) {
                if (getter.getParameters().isEmpty() && getter.getJavadoc().isPresent()) {
                    String text = getter.getJavadoc().get().getDescription().toText().trim();
                    int idx = text.indexOf(": ");
                    return idx >= 0 ? text.substring(idx + 2).trim() : text;
                }
            }
        }
        return null;
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

    // isPrefix is an SDK-only property: the AutoRest swagger README injected it into BlobItem
    // ($.properties.IsPrefix = { "type": "boolean" }), so it exists in neither the wire contract nor the TypeSpec
    // spec. The shipped public BlobItem exposes isPrefix()/setIsPrefix, and BlobContainer*Client and
    // ArrowBlobListDeserializer use it to mark synthesized prefix entries, so restore it on the generated internal
    // model. Runs before restoreFluentModels so the fluent pass generates the matching setIsPrefix setter.
    private static void addSdkOnlyIsPrefix(PackageCustomization implModelsPackage, Logger logger) {
        if (implModelsPackage.getClass("BlobItemInternal") == null) {
            logger.info("BlobItemInternal not present; skipping isPrefix restoration.");
            return;
        }
        implModelsPackage.getClass("BlobItemInternal")
            .customizeAst(ast -> ast.getClassByName("BlobItemInternal").ifPresent(clazz -> {
                if (clazz.getFieldByName("isPrefix").isPresent()) {
                    return;
                }
                clazz.addField("Boolean", "isPrefix", Modifier.Keyword.PRIVATE)
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Whether this item is a prefix rather than a blob. SDK-only; not part of the wire "
                            + "contract.")));
                MethodDeclaration getter = clazz.addMethod("isPrefix", Modifier.Keyword.PUBLIC);
                getter.setType("Boolean");
                getter.setJavadocComment(new Javadoc(JavadocDescription.parseText(
                    "Get the isPrefix property: whether this item is a prefix rather than a blob."))
                        .addBlockTag("return", "the isPrefix value."));
                getter.setBody(StaticJavaParser.parseBlock("{ return this.isPrefix; }"));
                logger.info("Restored SDK-only isPrefix property on BlobItemInternal.");
            }));
    }


    // The generated XmlSerializer casts typeReference.getJavaClass() to Class<T>, which javac reports as a redundant
    // cast; the build runs with -Werror, so it fails compilation.
    private static void fixXmlSerializerRedundantCast(Editor editor, Logger logger) {
        String path = PKG_ROOT + "implementation/XmlSerializer.java";
        String content = editor.getContents().get(path);
        if (content == null) {
            logger.info("XmlSerializer not present in editor; skipping cast fix.");
            return;
        }
        String updated = content.replace("(Class<T>) typeReference.getJavaClass()", "typeReference.getJavaClass()");
        if (!updated.equals(content)) {
            editor.replaceFile(path, updated);
            logger.info("Removed redundant cast in XmlSerializer.");
        } else {
            logger.info("XmlSerializer redundant cast not found; skipping.");
        }
    }

    // ParquetConfiguration is `Record<unknown>` in the spec. typespec-java's XML path for a free-form record emits
    // calls that do not exist (xmlWriter.writeUntypedElement) and a QName/String mismatch on read, so the generated
    // ParquetTextConfiguration does not compile. The shipped SDK has no such model at all: QueryFormat exposes the
    // property as Object and writes an empty <ParquetTextConfiguration/> element, which is what the AutoRest
    // customizeQueryFormat customization produced. Reproduce that shape and drop the generated model.
    private static void customizeQueryFormat(Editor editor, Logger logger) {
        String path = PKG_ROOT + "implementation/models/QueryFormat.java";
        String content = editor.getContents().get(path);
        if (content == null) {
            logger.info("QueryFormat not present; skipping parquet customization.");
            return;
        }
        String updated = content.replace("private ParquetTextConfiguration parquetTextConfiguration;",
            "private Object parquetTextConfiguration;")
            .replace("public ParquetTextConfiguration getParquetTextConfiguration() {",
                "public Object getParquetTextConfiguration() {")
            .replace("public QueryFormat setParquetTextConfiguration(ParquetTextConfiguration parquetTextConfiguration) {",
                "public QueryFormat setParquetTextConfiguration(Object parquetTextConfiguration) {")
            .replace("xmlWriter.writeXml(this.parquetTextConfiguration, \"ParquetTextConfiguration\");",
                "if (this.parquetTextConfiguration != null) {\n"
                    + "            xmlWriter.writeStartElement(\"ParquetTextConfiguration\").writeEndElement();\n"
                    + "        }")
            .replace("ParquetTextConfiguration parquetTextConfiguration = null;",
                "Object parquetTextConfiguration = null;")
            .replace(
                "parquetTextConfiguration = ParquetTextConfiguration.fromXml(reader, \"ParquetTextConfiguration\");",
                "parquetTextConfiguration = new Object();\n"
                    + "                    reader.skipElement();");
        if (updated.equals(content)) {
            logger.info("QueryFormat parquet references already customized; skipping.");
            return;
        }
        editor.replaceFile(path, updated);
        removeFileIfPresent(editor, PKG_ROOT + "implementation/models/ParquetTextConfiguration.java", logger);
        logger.info("Retyped QueryFormat.parquetTextConfiguration to Object and removed the generated model.");
    }

    // Download and Query stream their response bodies. typespec-java types both as Response<BinaryData>, and
    // azure-core's AsyncRestProxy turns that into BinaryData.fromFlux(body), which aggregates the entire body into
    // a byte array: every blob download would be buffered in memory, and the reliable-download retry in
    // BlobAsyncClientBase cannot resume a range from a buffered response. AutoRest emitted StreamResponse for both,
    // which azure-core recognises as Response<Flux<ByteBuffer>> and leaves unbuffered, so restore that shape on the
    // two operations and drop the ResponseBase wrapper from their convenience methods. The wrapper only added
    // typed headers built from the raw HttpHeaders, which the hand-written clients build for themselves.
    // Every other operation keeps Response<BinaryData>: their bodies are XML documents that must be buffered.
    private static final String STREAMING_RESPONSE_IMPORT = "import com.azure.core.http.rest.StreamResponse;";

    // Alongside each WithResponse method the emitter generates a plain download()/query() that returns the body as
    // BinaryData. Once the response is a StreamResponse those no longer compile, and buffering the body is exactly
    // what the retype exists to avoid. Nothing calls them -- the hand-written clients always go through the
    // WithResponse overloads -- so drop them rather than reintroducing a buffered path.
    private static final List<String> STREAMING_CLIENTS = Arrays.asList("BlobAsyncClientInternal",
        "BlobClientInternal", "BlobContainerAsyncClientInternal", "BlobContainerClientInternal");

    private static void removeBufferedStreamingConvenienceMethods(PackageCustomization implementation, Logger logger) {
        for (String className : STREAMING_CLIENTS) {
            if (implementation.getClass(className) == null) {
                logger.info("{} not present; skipping buffered convenience removal.", className);
                continue;
            }
            implementation.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
                int removed = 0;
                for (MethodDeclaration method : new ArrayList<>(clazz.getMethods())) {
                    String name = method.getNameAsString();
                    boolean isStreamingBody = "download".equals(name)
                        || "query".equals(name)
                        || name.endsWith("ApacheArrow");
                    if (isStreamingBody
                        && (method.getType().asString().contains("BinaryData")
                            || method.getType().asString().contains("String"))) {
                        method.remove();
                        removed++;
                    }
                }
                logger.info("Removed {} buffered download/query convenience methods from {}.", removed, className);
            }));
        }
    }

    // The customization runs on the emitter's output before it is formatted, so the wrapper bodies are matched with
    // whitespace-tolerant patterns. Each anchors on the ResponseBase construction, which is what distinguishes the
    // WithResponse convenience method from the plain one that maps the same protocol call to its body.
    private static final String ASYNC_DOWNLOAD_WRAPPER = "return downloadWithResponseInternal\\(requestOptions\\)"
        + "\\s*\\.map\\(protocolMethodResponse -> new ResponseBase<>\\([^;]*;";

    private static final String ASYNC_QUERY_WRAPPER = "return queryWithResponseInternal\\(BinaryData"
        + "\\.fromObject\\(queryRequest, XML_SERIALIZER\\), requestOptions\\)"
        + "\\s*\\.map\\(protocolMethodResponse -> new ResponseBase<>\\([^;]*;";

    private static final String SYNC_DOWNLOAD_WRAPPER = "Response<BinaryData> protocolMethodResponse\\s*=\\s*"
        + "downloadWithResponseInternal\\(requestOptions\\);\\s*return new ResponseBase<>\\([^;]*;";

    private static final String SYNC_QUERY_WRAPPER = "Response<BinaryData> protocolMethodResponse\\s*=\\s*"
        + "queryWithResponseInternal\\(BinaryData\\.fromObject\\(queryRequest, XML_SERIALIZER\\), requestOptions\\);"
        + "\\s*return new ResponseBase<>\\([^;]*;";

    // The two Apache Arrow listings return an Arrow byte stream (the spec says `@body body: bytes` and the accept
    // header is application/vnd.apache.arrow.stream). The emitter types the convenience body as String and fills it
    // by XML-deserializing the payload, which is not a shape the response can take; the hand-written container
    // clients read it with FluxUtil.collectBytesInByteBufferStream. Same treatment as download and query.
    private static final String ARROW_ASYNC_WRAPPER = "return (listBlob\\w*ApacheArrowWithResponseInternal)"
        + "\\(([^;]*?)\\)\\s*\\.map\\(protocolMethodResponse -> new ResponseBase<>\\([^;]*;";

    private static final String ARROW_SYNC_WRAPPER = "Response<BinaryData> protocolMethodResponse\\s*=\\s*"
        + "(listBlob\\w*ApacheArrowWithResponseInternal)\\(([^;]*?)\\);\\s*"
        + "return new ResponseBase<>\\([^;]*;";

    private static void retypeArrowListingResponses(Editor editor, Logger logger) {
        retypeStreamingResponses(editor, logger, PKG_ROOT + "implementation/ContainersImpl.java", new String[][] {
            { "Mono<Response<BinaryData>> listBlobFlatSegmentApacheArrow(@HostParam",
                "Mono<StreamResponse> listBlobFlatSegmentApacheArrow(@HostParam" },
            { "Response<BinaryData> listBlobFlatSegmentApacheArrowSync(@HostParam",
                "StreamResponse listBlobFlatSegmentApacheArrowSync(@HostParam" },
            { "Mono<Response<BinaryData>> listBlobHierarchySegmentApacheArrow(@HostParam",
                "Mono<StreamResponse> listBlobHierarchySegmentApacheArrow(@HostParam" },
            { "Response<BinaryData> listBlobHierarchySegmentApacheArrowSync(@HostParam",
                "StreamResponse listBlobHierarchySegmentApacheArrowSync(@HostParam" },
            { "public Response<BinaryData> listBlobFlatSegmentApacheArrowWithResponseInternal(",
                "public StreamResponse listBlobFlatSegmentApacheArrowWithResponseInternal(" },
            { "public Response<BinaryData> listBlobHierarchySegmentApacheArrowWithResponseInternal(",
                "public StreamResponse listBlobHierarchySegmentApacheArrowWithResponseInternal(" } },
            new String[][] { { "public Mono<Response<BinaryData>>\\s+(listBlob\\w*ApacheArrowWithResponseInternalAsync\\()",
                "public Mono<StreamResponse> $1" } });

        retypeStreamingResponses(editor, logger, PKG_ROOT + "implementation/BlobContainerAsyncClientInternal.java",
            new String[][] {
                { "Mono<Response<BinaryData>> listBlobFlatSegmentApacheArrowWithResponseInternal(",
                    "Mono<StreamResponse> listBlobFlatSegmentApacheArrowWithResponseInternal(" },
                { "Mono<Response<BinaryData>> listBlobHierarchySegmentApacheArrowWithResponseInternal(",
                    "Mono<StreamResponse> listBlobHierarchySegmentApacheArrowWithResponseInternal(" },
                { "Mono<ResponseBase<ContainersListBlobFlatSegmentApacheArrowHeaders, String>>",
                    "Mono<StreamResponse>" },
                { "Mono<ResponseBase<ContainersListBlobHierarchySegmentApacheArrowHeaders, String>>",
                    "Mono<StreamResponse>" } },
            new String[][] { { ARROW_ASYNC_WRAPPER, "return $1($2);" } });

        retypeStreamingResponses(editor, logger, PKG_ROOT + "implementation/BlobContainerClientInternal.java",
            new String[][] {
                { "Response<BinaryData> listBlobFlatSegmentApacheArrowWithResponseInternal(",
                    "StreamResponse listBlobFlatSegmentApacheArrowWithResponseInternal(" },
                { "Response<BinaryData> listBlobHierarchySegmentApacheArrowWithResponseInternal(",
                    "StreamResponse listBlobHierarchySegmentApacheArrowWithResponseInternal(" },
                { "ResponseBase<ContainersListBlobFlatSegmentApacheArrowHeaders, String>", "StreamResponse" },
                { "ResponseBase<ContainersListBlobHierarchySegmentApacheArrowHeaders, String>", "StreamResponse" } },
            new String[][] { { ARROW_SYNC_WRAPPER, "return $1($2);" } });
    }

    private static void retypeStreamingResponses(Editor editor, Logger logger) {
        retypeStreamingResponses(editor, logger, PKG_ROOT + "implementation/BlobsImpl.java", new String[][] {
            { "Mono<Response<BinaryData>> download(@HostParam", "Mono<StreamResponse> download(@HostParam" },
            { "Response<BinaryData> downloadSync(@HostParam", "StreamResponse downloadSync(@HostParam" },
            { "Mono<Response<BinaryData>> query(@HostParam", "Mono<StreamResponse> query(@HostParam" },
            { "Response<BinaryData> querySync(@HostParam", "StreamResponse querySync(@HostParam" },
            { "public Mono<Response<BinaryData>> downloadWithResponseInternalAsync(",
                "public Mono<StreamResponse> downloadWithResponseInternalAsync(" },
            { "public Response<BinaryData> downloadWithResponseInternal(",
                "public StreamResponse downloadWithResponseInternal(" },
            { "public Mono<Response<BinaryData>> queryWithResponseInternalAsync(",
                "public Mono<StreamResponse> queryWithResponseInternalAsync(" },
            { "public Response<BinaryData> queryWithResponseInternal(",
                "public StreamResponse queryWithResponseInternal(" } }, NO_PATTERNS);

        retypeStreamingResponses(editor, logger, PKG_ROOT + "implementation/BlobAsyncClientInternal.java",
            new String[][] {
                { "Mono<Response<BinaryData>> downloadWithResponseInternal(",
                    "Mono<StreamResponse> downloadWithResponseInternal(" },
                { "Mono<Response<BinaryData>> queryWithResponseInternal(",
                    "Mono<StreamResponse> queryWithResponseInternal(" },
                { "public Mono<ResponseBase<BlobsDownloadHeaders, BinaryData>> downloadWithResponse(",
                    "public Mono<StreamResponse> downloadWithResponse(" },
                { "public Mono<ResponseBase<BlobsQueryHeaders, BinaryData>> queryWithResponse(",
                    "public Mono<StreamResponse> queryWithResponse(" } },
            new String[][] {
                { ASYNC_DOWNLOAD_WRAPPER, "return downloadWithResponseInternal(requestOptions);" },
                { ASYNC_QUERY_WRAPPER, "return queryWithResponseInternal("
                    + "BinaryData.fromObject(queryRequest, XML_SERIALIZER), requestOptions);" } });

        retypeStreamingResponses(editor, logger, PKG_ROOT + "implementation/BlobClientInternal.java", new String[][] {
            { "Response<BinaryData> downloadWithResponseInternal(", "StreamResponse downloadWithResponseInternal(" },
            { "Response<BinaryData> queryWithResponseInternal(", "StreamResponse queryWithResponseInternal(" },
            { "public ResponseBase<BlobsDownloadHeaders, BinaryData> downloadWithResponse(",
                "public StreamResponse downloadWithResponse(" },
            { "public ResponseBase<BlobsQueryHeaders, BinaryData> queryWithResponse(",
                "public StreamResponse queryWithResponse(" } },
            new String[][] {
                { SYNC_DOWNLOAD_WRAPPER, "return downloadWithResponseInternal(requestOptions);" },
                { SYNC_QUERY_WRAPPER, "return queryWithResponseInternal("
                    + "BinaryData.fromObject(queryRequest, XML_SERIALIZER), requestOptions);" } });
    }

    private static final String[][] NO_PATTERNS = new String[0][];

    private static void retypeStreamingResponses(Editor editor, Logger logger, String path, String[][] replacements,
        String[][] patterns) {
        String content = editor.getContents().get(path);
        if (content == null) {
            logger.info("{} not present in editor; skipping the streaming retype.", path);
            return;
        }
        String updated = content;
        for (String[] replacement : replacements) {
            if (!updated.contains(replacement[0])) {
                throw new IllegalStateException(
                    "Streaming retype target not found in " + path + ": " + replacement[0]);
            }
            updated = updated.replace(replacement[0], replacement[1]);
        }
        for (String[] pattern : patterns) {
            // Replacements may reference capture groups, so they are not quoted.
            String replaced = updated.replaceAll(pattern[0], pattern[1]);
            if (replaced.equals(updated)) {
                throw new IllegalStateException(
                    "Streaming retype pattern matched nothing in " + path + ": " + pattern[0]);
            }
            updated = replaced;
        }
        updated = addImport(updated, STREAMING_RESPONSE_IMPORT);
        updated = removeImportIfUnused(updated, "com.azure.storage.blob.implementation.models.BlobsDownloadHeaders");
        updated = removeImportIfUnused(updated, "com.azure.storage.blob.implementation.models.BlobsQueryHeaders");
        editor.replaceFile(path, updated);
        logger.info("Retyped the streaming download and query responses in {}.", path);
    }

    // The generated files all import com.azure.core.http.rest.Response, so anchor on it to keep the block sorted.
    private static String addImport(String content, String importLine) {
        if (content.contains(importLine)) {
            return content;
        }
        String anchor = "import com.azure.core.http.rest.Response;";
        return content.replace(anchor, anchor + "\n" + importLine);
    }

    private static String removeImportIfUnused(String content, String qualifiedName) {
        String importLine = "import " + qualifiedName + ";\n";
        if (!content.contains(importLine)) {
            return content;
        }
        String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
        String withoutImport = content.replace(importLine, "");
        return withoutImport.contains(simpleName) ? content : withoutImport;
    }

    // typespec-java writes these five header models to a file named ...FromURLHeaders.java but declares the class
    // as ...FromUrlHeaders, which does not compile ("class X is public, should be declared in a file named X.java").
    // The file name matches the shipped AutoRest name that the hand-written clients reference, so the class (and
    // every reference to it) is renamed to match the file. Only these five are affected: the emitter normalises the
    // URL acronym in the file name for this subset, while AppendBlockFromUrl/PutBlobFromUrl generate consistently
    // as ...FromUrlHeaders and are deliberately left alone, since the hand-written clients expect that spelling.
    private static final List<String> URL_ACRONYM_HEADER_MODELS = Arrays.asList(
        "BlobsAbortCopyFromUrlHeaders", "BlobsCopyFromUrlHeaders", "BlobsStartCopyFromUrlHeaders",
        "BlockBlobsStageBlockFromUrlHeaders", "PageBlobsUploadPagesFromUrlHeaders");

    private static void fixUrlAcronymHeaderNames(Editor editor, Logger logger) {
        int renamed = 0;
        for (String path : new ArrayList<>(editor.getContents().keySet())) {
            String content = editor.getContents().get(path);
            if (content == null) {
                continue;
            }
            String updated = content;
            for (String wrongName : URL_ACRONYM_HEADER_MODELS) {
                updated = updated.replace(wrongName, wrongName.replace("FromUrlHeaders", "FromURLHeaders"));
            }
            if (!updated.equals(content)) {
                editor.replaceFile(path, updated);
                renamed++;
            }
        }
        logger.info("Aligned FromUrlHeaders class names with their FromURLHeaders file names in {} file(s).", renamed);
    }

    // Per-resource convenience clients emitted by typespec-java under max-overload: model. They carry the typed
    // WithResponse methods (ResponseBase<XxxHeaders, Model> / Response<XxxHeaders>) that wrap the protocol
    // *WithResponseInternal methods, so they are RETAINED as the internal typed layer rather than deleted. They are
    // moved out of the public package into implementation so they add no public API, and the hand-written Blob
    // clients delegate to them. Mirrors File Share's ShareStorageCustomization
    // (Azure/azure-sdk-for-java#50354) relocateConvenienceClientsToImplementation.
    private static final List<String> CONVENIENCE_CLIENTS_TO_RELOCATE = Arrays.asList(
        "ServiceClient", "ServiceAsyncClient",
        "ContainerClient", "ContainerAsyncClient",
        "BlobClient", "BlobAsyncClient",
        "AppendBlobClient", "AppendBlobAsyncClient",
        "BlockBlobClient", "BlockBlobAsyncClient",
        "PageBlobClient", "PageBlobAsyncClient");

    // Logs every generated client sitting in the public package, so the relocate/remove lists can be reconciled
    // against what the emitter actually produced rather than an assumed list.
    private static void logGeneratedPublicClients(Editor editor, Logger logger) {
        for (String path : new ArrayList<>(editor.getContents().keySet())) {
            if (!path.startsWith(PKG_ROOT) || !path.endsWith("Client.java")) {
                continue;
            }
            String rest = path.substring(PKG_ROOT.length());
            if (rest.contains("/")) {
                continue;
            }
            String content = editor.getContents().get(path);
            if (content != null && content.contains("Code generated by")) {
                logger.info("Generated public-package client present: {}", rest);
            }
        }
    }

    private static void relocateConvenienceClientsToImplementation(Editor editor, Logger logger) {
        for (String className : CONVENIENCE_CLIENTS_TO_RELOCATE) {
            String oldPath = PKG_ROOT + className + ".java";
            String content = editor.getContents().get(oldPath);
            if (content == null) {
                logger.info("Convenience client {} not generated; skipping relocation.", className);
                continue;
            }
            String newName = internalClientName(className);
            // Move to the implementation package.
            content = content.replace("package com.azure.storage.blob;", "package com.azure.storage.blob.implementation;");
            // Drop the @ServiceClient marker; its builder (AzureBlobStorageBuilder) is removed. Match both the
            // simple and fully-qualified forms.
            content = removeServiceClientAnnotation(content);
            content = content.replace("import com.azure.core.annotation.ServiceClient;" + System.lineSeparator(), "");
            // The generated constructor is package-private; callers now live in a different package. Anchored on the
            // line start rather than a platform line separator: the emitter output uses "\n" regardless of platform,
            // so matching System.lineSeparator() silently fails on Windows.
            content = content.replaceFirst("(?m)^(\\s*)" + className + "\\(", "$1public " + className + "(");
            // Rename the class declaration, constructor and self-references. Longer names that merely contain this
            // one (e.g. PageBlobClientBuilder) are protected by requiring a non-identifier character on each side.
            content = renameIdentifier(content, className, newName);
            editor.removeFile(oldPath);
            editor.addFile(PKG_ROOT + "implementation/" + newName + ".java", content);
            logger.info("Relocated convenience client {} -> implementation/{}", className, newName);
        }
    }

    // Removes the @ServiceClient marker annotation line. Written without a regex because the annotation is emitted
    // either simple or fully qualified, and the class literally named ServiceClient carries the qualified form.
    private static String removeServiceClientAnnotation(String content) {
        StringBuilder result = new StringBuilder(content.length());
        for (String line : content.split("\n", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("@ServiceClient(")
                || trimmed.startsWith("@com.azure.core.annotation.ServiceClient(")) {
                continue;
            }
            if (result.length() > 0) {
                result.append('\n');
            }
            result.append(line);
        }
        return result.toString();
    }

    // Replaces whole-identifier occurrences of oldName with newName. Equivalent to a word-boundary regex, but written
    // with explicit character checks so the customization source carries no regex escapes.
    private static String renameIdentifier(String content, String oldName, String newName) {
        StringBuilder result = new StringBuilder(content.length());
        int index = 0;
        while (true) {
            int found = content.indexOf(oldName, index);
            if (found < 0) {
                result.append(content, index, content.length());
                return result.toString();
            }
            int after = found + oldName.length();
            boolean beforeOk = found == 0 || !isIdentifierChar(content.charAt(found - 1));
            boolean afterOk = after >= content.length() || !isIdentifierChar(content.charAt(after));
            result.append(content, index, found).append(beforeOk && afterOk ? newName : oldName);
            index = after;
        }
    }

    private static boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }

    // Maps a generated per-resource convenience client name to its internal name. The hand-written public clients are
    // BlobServiceClient / BlobContainerClient / BlobClient and the specialized Append/Block/Page clients, so the
    // service and container clients take a Blob prefix while the blob-scoped ones already carry it.
    private static String internalClientName(String generatedName) {
        String prefixed = (generatedName.startsWith("Service") || generatedName.startsWith("Container"))
            ? "Blob" + generatedName
            : generatedName;
        return prefixed + "Internal";
    }
}
