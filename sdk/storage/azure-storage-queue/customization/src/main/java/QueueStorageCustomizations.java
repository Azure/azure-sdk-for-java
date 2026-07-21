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
 */
public class QueueStorageCustomizations extends Customization {

    private static final String ROOT_FILE_PATH = "src/main/java/com/azure/storage/queue/";

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

    private static final String IMPL_MODELS_PATH = "src/main/java/com/azure/storage/queue/implementation/models/";

    // Generated XML wrapper models unused by the hand-written clients (they use the hand-authored *Wrapper types).
    private static final String[] UNUSED_GENERATED_MODELS = new String[] {
        "ReceivedMessages.java",
        "PeekedMessages.java",
        "ListOfSentMessage.java",
        "SignedIdentifiers.java"
    };

    private static final List<String> FLUENT_MODELS = Arrays.asList(
        "QueueRetentionPolicy", "QueueMetrics", "QueueCorsRule", "QueueAnalyticsLogging", "QueueSignedIdentifier",
        "GeoReplication", "QueueItem", "QueueServiceStatistics", "SendMessageResult", "UserDelegationKey");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        removeGeneratedPublicClients(editor, logger);
        removeUnusedGeneratedModels(editor, logger);
        preserveHandwrittenModuleInfo(editor, logger);
        retargetServiceVersionReferences(editor, logger);
        restoreFluentModels(customization, logger);
        exposeRawListQueuesResponse(customization.getPackage("com.azure.storage.queue.implementation"), logger);
        updateImplToMapInternalException(customization.getPackage("com.azure.storage.queue.implementation"), logger);
    }

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
            String updated = content.replace("QueuesServiceVersion", "QueueServiceVersion");
            editor.replaceFile(path, updated);
            logger.info("Retargeted QueuesServiceVersion -> QueueServiceVersion in {}.", fileName);
        }
    }

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

    private static void removeUnusedGeneratedModels(Editor editor, Logger logger) {
        for (String fileName : UNUSED_GENERATED_MODELS) {
            String path = IMPL_MODELS_PATH + fileName;
            if (editor.getContents().containsKey(path)) {
                editor.removeFile(path);
                logger.info("Removed unused generated model {}", path);
            } else {
                logger.info("Generated model {} not present; skipping removal.", path);
            }
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
