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
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * TypeSpec customization for azure-storage-queue.
 */
public class QueueStorageCustomizations extends Customization {

    private static final String PKG_ROOT = "src/main/java/com/azure/storage/queue/";

    private static final String MODELS_PACKAGE = "com.azure.storage.queue.models";

    private static final String IMPL_PACKAGE = "com.azure.storage.queue.implementation";

    // Models that shipped as @Fluent (public no-arg ctor + setters) before the TypeSpec migration. With
    // required-fields-as-ctor-args:true these regenerate as @Immutable with a required-args ctor and no setters --
    // a breaking change. restoreFluentModels restores the shipped fluent shape per-model. Expand from the RevApi
    // "method removed" report.
    private static final List<String> FLUENT_MODELS_TO_RESTORE = Arrays.asList(
        "QueueRetentionPolicy", "QueueMetrics", "QueueCorsRule", "QueueAnalyticsLogging", "QueueSignedIdentifier",
        "GeoReplication", "QueueItem", "QueueServiceStatistics", "SendMessageResult", "UserDelegationKey");

    // Generated convenience clients + builder + service version emitted by typespec-java on top of the
    // implementation/*Impl operation layer. The public surface is the hand-written clients, so delete the
    // generated ones (removeFile is a no-op when a name is absent).
    private static final List<String> GENERATED_CLIENTS_TO_REMOVE = Arrays.asList(
        "QueueClient", "QueueAsyncClient",
        "ServiceClient", "ServiceAsyncClient",
        "MessagesClient", "MessagesAsyncClient",
        "MessageIdsClient", "MessageIdsAsyncClient",
        "AzureQueueStorageBuilder",
        "QueuesServiceVersion");

    // The hand-written clients consume the generated XML list-wrapper models (SignedIdentifiers, ReceivedMessages,
    // PeekedMessages, ListOfSentMessage) directly, so none are removed.
    private static final List<String> GENERATED_MODELS_TO_REMOVE = Arrays.asList();

    // module-info.java is hand-authored: the module descriptor carries the full requires/exports/opens (incl. the
    // transitive com.azure.storage.common visibility). typespec-java regenerates a minimal version that overwrites
    // it, so drop the generated copy and keep the hand-written descriptor.
    private static final List<String> GENERATED_DESCRIPTOR_FILES_TO_REMOVE = Arrays.asList(
        "src/main/java/module-info.java");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        removeGeneratedFiles(editor, logger);
        fixXmlSerializerRedundantCast(editor, logger);
        retargetServiceVersionReferences(editor, logger);
        restoreFluentModels(customization, logger);
        restoreMetadataHeaderCollection(customization.getPackage(IMPL_PACKAGE + ".models"), logger);
        exposeRawListQueuesResponse(customization.getPackage(IMPL_PACKAGE), logger);
        removeUnusedXmlNextLinkHelpers(customization.getPackage(IMPL_PACKAGE), logger);
        updateImplToMapInternalException(customization.getPackage(IMPL_PACKAGE), logger);
    }

    // typespec-java's response header model reads the metadata header as a single x-ms-meta value; on the wire queue
    // metadata is a dynamic x-ms-meta-<key> collection, so responseHeadersAsModel cannot represent it. Reshape
    // QueuesGetPropertiesHeaders so getMetadata() returns the assembled Map<String, String> (the shape the shipped
    // public API and the hand-written ModelHelper consume), matching what AutoRest generated.
    private static void restoreMetadataHeaderCollection(PackageCustomization implModelsPackage, Logger logger) {
        if (implModelsPackage.getClass("QueuesGetPropertiesHeaders") == null) {
            logger.info("QueuesGetPropertiesHeaders not present; skipping metadata header-collection restore.");
            return;
        }
        implModelsPackage.getClass("QueuesGetPropertiesHeaders").customizeAst(ast -> {
            ast.addImport("com.azure.core.http.HttpHeader");
            ast.addImport("java.util.LinkedHashMap");
            ast.addImport("java.util.Map");
            ast.getClassByName("QueuesGetPropertiesHeaders").ifPresent(clazz -> {
                clazz.getFieldByName("metadata")
                    .ifPresent(field -> field.getVariable(0).setType("Map<String, String>"));
                clazz.getMethodsByName("getMetadata").forEach(method -> method.setType("Map<String, String>"));
                clazz.getFieldByName("X_MS_META").ifPresent(FieldDeclaration::remove);
                if (!clazz.getFieldByName("X_MS_META_PREFIX").isPresent()) {
                    clazz.getMembers().add(0,
                        StaticJavaParser.parseBodyDeclaration("private static final String X_MS_META_PREFIX = \"x-ms-meta-\";"));
                }
                clazz.getConstructors().forEach(ctor -> {
                    NodeList<Statement> statements = ctor.getBody().getStatements();
                    for (int i = 0; i < statements.size(); i++) {
                        if (statements.get(i).toString().contains("this.metadata = ")) {
                            statements.remove(i);
                            statements.add(i, StaticJavaParser.parseStatement("this.metadata = metadataHeaderCollection;"));
                            statements.add(i, StaticJavaParser.parseStatement("for (HttpHeader header : rawHeaders) {"
                                + " String headerName = header.getName();"
                                + " if (headerName.regionMatches(true, 0, X_MS_META_PREFIX, 0, X_MS_META_PREFIX.length())) {"
                                + " metadataHeaderCollection.put(headerName.substring(X_MS_META_PREFIX.length()),"
                                + " header.getValue()); } }"));
                            statements.add(i,
                                StaticJavaParser.parseStatement("Map<String, String> metadataHeaderCollection = new LinkedHashMap<>();"));
                            break;
                        }
                    }
                });
            });
        });
        logger.info("Restored x-ms-meta-* header-collection Map on QueuesGetPropertiesHeaders.");
    }

    private static void restoreFluentModels(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage(MODELS_PACKAGE);
        for (String className : FLUENT_MODELS_TO_RESTORE) {
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

    // exposeRawListQueuesResponse replaces the emitter's paginated path, leaving the generated getXmlNextLink
    // helpers uncalled and tripping SpotBugs UPM_UNCALLED_PRIVATE_METHOD.
    private static void removeUnusedXmlNextLinkHelpers(PackageCustomization implPackage, Logger logger) {
        if (implPackage.getClass("ServicesImpl") == null) {
            logger.info("ServicesImpl not present; skipping getXmlNextLink removal.");
            return;
        }
        implPackage.getClass("ServicesImpl").customizeAst(ast -> ast.getClassByName("ServicesImpl").ifPresent(clazz -> {
            List<MethodDeclaration> unused = clazz.getMethodsByName("getXmlNextLink");
            if (unused.isEmpty()) {
                logger.info("No getXmlNextLink methods found in ServicesImpl; skipping removal.");
                return;
            }
            new ArrayList<>(unused).forEach(MethodDeclaration::remove);
            logger.info("Removed {} unused getXmlNextLink method(s) from ServicesImpl.", unused.size());
        }));
    }

    private static void retargetServiceVersionReferences(Editor editor, Logger logger) {
        String implDir = PKG_ROOT + "implementation/";
        for (String fileName : new String[] {
            "AzureQueueStorageImpl.java", "ServicesImpl.java", "QueuesImpl.java",
            "MessagesImpl.java", "MessageIdsImpl.java" }) {
            String path = implDir + fileName;
            String content = editor.getContents().get(path);
            if (content == null || !content.contains("QueuesServiceVersion")) {
                continue;
            }
            String updated = content.replace("QueuesServiceVersion", "QueueServiceVersion");
            editor.replaceFile(path, updated);
            logger.info("Retargeted QueuesServiceVersion -> QueueServiceVersion in {}.", fileName);
        }
    }

    private static void removeGeneratedFiles(Editor editor, Logger logger) {
        for (String className : GENERATED_CLIENTS_TO_REMOVE) {
            removeFileIfPresent(editor, PKG_ROOT + className + ".java", logger);
        }
        for (String modelName : GENERATED_MODELS_TO_REMOVE) {
            removeFileIfPresent(editor, PKG_ROOT + "implementation/models/" + modelName + ".java", logger);
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

    // The generated XmlSerializer casts typeReference.getJavaClass() (already Class<T>) to Class<T> -- a redundant
    // cast that trips the module's -Werror build. The file is emitter-generated and wired into ServicesImpl (XML
    // pageable responses), so it can't be removed; drop the redundant cast here instead.
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

    private static void updateImplToMapInternalException(PackageCustomization implPackage, Logger logger) {
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
                ast.findAll(NormalAnnotationExpr.class).stream()
                    .filter(anno -> anno.getNameAsString().equals("UnexpectedResponseExceptionType")
                        && anno.getPairs().stream().anyMatch(p -> p.getNameAsString().equals("code")))
                    .collect(java.util.stream.Collectors.toList())
                    .forEach(Node::remove);
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
