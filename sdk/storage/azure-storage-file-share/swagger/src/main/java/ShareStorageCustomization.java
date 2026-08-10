// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Customization class for File Share Storage.
 */
public class ShareStorageCustomization extends Customization {
    private static final String PKG_ROOT = "src/main/java/com/azure/storage/file/share/";

    private static final String MODELS_PACKAGE = "com.azure.storage.file.share.models";

    // Models that shipped as @Fluent (public no-arg ctor + setters) before the TypeSpec migration. With
    // required-fields-as-ctor-args:true (which must stay true so new models keep required params on the ctor) these
    // regenerate as @Immutable with a required-args ctor and no setters -- a breaking change. FluentModelRestorer
    // restores the shipped fluent shape per-model. Expand this list from the RevApi "method removed" report.
    private static final List<String> FLUENT_MODELS_TO_RESTORE = Arrays.asList(
        "FileRange", "ClearRange", "ShareCorsRule", "ShareFileRangeList", "ShareMetrics", "ShareRetentionPolicy",
        "ShareSignedIdentifier", "UserDelegationKey");

    // Generated convenience clients + builders emitted by typespec-java on top of the
    // implementation/*Impl operation layer. The public surface is the hand-written Share*-prefixed
    // clients, so delete the generated ones.
    private static final List<String> GENERATED_CLIENTS_TO_REMOVE = Arrays.asList(
        "ServiceClient", "ServiceAsyncClient",
        "DirectoryClient", "DirectoryAsyncClient",
        "FileClient", "FileAsyncClient",
        "ShareClient", "ShareAsyncClient",
        // Main service-client public surface — the impl (AzureFileStorageImpl) is kept; only the
        // public client/async-client/builder are removed. Both naming variants are listed because
        // removeFile is a no-op when absent.
        "FileClientBuilder",
        "AzureFileStorageClient", "AzureFileStorageAsyncClient", "AzureFileStorageClientBuilder",
        "AzureFileStorageBuilder",
        // The hand-written ShareServiceVersion is authoritative; the generated enum is discarded and new service
        // versions are added by hand. To instead generate it, remove this entry and restore the @clientApiVersions
        // block in client.tsp + the FileServiceVersion->ShareServiceVersion rename customization.
        "FileServiceVersion");

    private static final List<String> GENERATED_DESCRIPTOR_FILES_TO_REMOVE = Arrays.asList(
        "src/main/java/module-info.java",
        "src/main/java/com/azure/storage/file/share/package-info.java",
        "src/main/java/com/azure/storage/file/share/models/package-info.java",
        "src/main/java/com/azure/storage/file/share/implementation/package-info.java");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        removeGeneratedConvenienceClients(customization, logger);

        restoreFluentModels(customization, logger);

        customization.getClass("com.azure.storage.file.share.models", "ShareTokenIntent")
            .customizeAst(ast -> ast.getClassByName("ShareTokenIntent").ifPresent(clazz -> clazz.setJavadocComment(
                "The request intent specifies requests that are intended for backup/admin type operations, meaning "
                    + "that all file/directory ACLs are bypassed and full permissions are granted. User must also have "
                    + "required RBAC permission.")));

        updateImplToMapInternalException(customization.getPackage("com.azure.storage.file.share.implementation"));

        // Must run AFTER updateImplToMapInternalException so the typed wrappers added here are not walked
        // (and double-wrapped) by that pass. Wrappers embed their own error mapping.
        addTypedImplWrappers(customization, logger);
    }

    /**
     * Deletes the generated convenience clients / builder / service version so the shipped public
     * surface is limited to the hand-written Share*-prefixed clients. The generated
     * {@code implementation/*Impl} operation layer is retained and continues to be wrapped by the
     * hand-written clients.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void removeGeneratedConvenienceClients(LibraryCustomization customization, Logger logger) {
        for (String className : GENERATED_CLIENTS_TO_REMOVE) {
            String path = PKG_ROOT + className + ".java";
            customization.getRawEditor().removeFile(path);
            logger.info("Removed generated client file: {}", path);
        }
        for (String path : GENERATED_DESCRIPTOR_FILES_TO_REMOVE) {
            customization.getRawEditor().removeFile(path);
            logger.info("Removed generated descriptor file (hand-written version preserved): {}", path);
        }
    }

    /**
     * Restores the "fluent" shape of models that shipped as {@code @Fluent} (public no-arg constructor + setters)
     * before the TypeSpec migration. With {@code required-fields-as-ctor-args: true} (which must stay true so new
     * models keep required parameters on their constructor) these regenerate as {@code @Immutable} with a
     * required-args constructor and no setters -- a breaking change. Each named model is converted back purely via the
     * JavaParser AST (no string replacement): {@code @Immutable} becomes {@code @Fluent}, {@code final} is removed from
     * instance fields, a public no-arg constructor is added, and a public fluent setter is added for each field. The
     * generated required-args constructor and {@code fromXml}/{@code fromJson} deserialization are left untouched.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void restoreFluentModels(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage(MODELS_PACKAGE);
        for (String modelName : FLUENT_MODELS_TO_RESTORE) {
            restoreFluentModel(models, modelName, logger);
        }
    }

    private static void restoreFluentModel(PackageCustomization models, String modelName, Logger logger) {
        models.getClass(modelName).customizeAst(ast -> {
            ast.addImport("com.azure.core.annotation.Fluent");
            ast.addImport("com.azure.core.annotation.Generated");
            ast.getClassByName(modelName).ifPresent(clazz -> {
                // @Immutable -> @Fluent
                clazz.getAnnotationByName("Immutable").ifPresent(annotation -> annotation.remove());
                if (!clazz.isAnnotationPresent("Fluent")) {
                    clazz.addMarkerAnnotation("Fluent");
                }

                NodeList<BodyDeclaration<?>> members = clazz.getMembers();

                // Ensure a public no-arg constructor exists, positioned as the first constructor (right after the
                // fields) to match the shipped layout. Widen an existing private no-arg ctor in place, otherwise
                // insert a new one ahead of the generated required-args constructor.
                boolean hasNoArgConstructor = clazz.getConstructors().stream()
                    .anyMatch(ctor -> ctor.getParameters().isEmpty());
                if (hasNoArgConstructor) {
                    clazz.getConstructors().stream()
                        .filter(ctor -> ctor.getParameters().isEmpty())
                        .forEach(ctor -> ctor.setModifiers(Modifier.Keyword.PUBLIC));
                } else {
                    ConstructorDeclaration noArgConstructor = new ConstructorDeclaration();
                    noArgConstructor.setName(modelName);
                    noArgConstructor.setModifiers(Modifier.Keyword.PUBLIC);
                    noArgConstructor.setBody(StaticJavaParser.parseBlock("{}"));
                    noArgConstructor.addMarkerAnnotation("Generated");
                    noArgConstructor.setJavadocComment(new Javadoc(
                        JavadocDescription.parseText("Creates an instance of " + modelName + " class.")));
                    int firstConstructorIndex = indexOfFirstConstructor(members);
                    if (firstConstructorIndex >= 0) {
                        members.add(firstConstructorIndex, noArgConstructor);
                    } else {
                        members.add(noArgConstructor);
                    }
                }

                // Un-finalize instance fields and add a fluent setter immediately after each field's getter, so the
                // getter/setter pairs sit together as they did in the shipped model.
                clazz.getFields().stream()
                    .filter(field -> !field.isStatic())
                    .forEach(field -> {
                        field.removeModifier(Modifier.Keyword.FINAL);
                        VariableDeclarator variable = field.getVariable(0);
                        String fieldName = variable.getNameAsString();
                        String setterName
                            = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                        if (!clazz.getMethodsByName(setterName).isEmpty()) {
                            return;
                        }

                        MethodDeclaration setter = new MethodDeclaration();
                        setter.setName(setterName);
                        setter.setModifiers(Modifier.Keyword.PUBLIC);
                        setter.setType(modelName);
                        setter.addParameter(variable.getTypeAsString(), fieldName);
                        setter.setBody(StaticJavaParser.parseBlock(
                            "{ this." + fieldName + " = " + fieldName + "; return this; }"));
                        setter.addMarkerAnnotation("Generated");
                        setter.setJavadocComment(new Javadoc(
                            JavadocDescription.parseText("Set the " + fieldName + " property."))
                            .addBlockTag("param", fieldName, "the " + fieldName + " value to set.")
                            .addBlockTag("return", "the " + modelName + " object itself."));

                        int getterIndex = indexOfGetter(members, fieldName);
                        if (getterIndex >= 0) {
                            members.add(getterIndex + 1, setter);
                        } else {
                            members.add(setter);
                        }
                    });
            });
            logger.info("Restored fluent shape for model: {}", modelName);
        });
    }

    private static int indexOfFirstConstructor(NodeList<BodyDeclaration<?>> members) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i) instanceof ConstructorDeclaration) {
                return i;
            }
        }
        return -1;
    }

    private static int indexOfGetter(NodeList<BodyDeclaration<?>> members, String fieldName) {
        String capitalized = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String getterName = "get" + capitalized;
        String booleanGetterName = "is" + capitalized;
        for (int i = 0; i < members.size(); i++) {
            BodyDeclaration<?> member = members.get(i);
            if (member instanceof MethodDeclaration) {
                String name = ((MethodDeclaration) member).getNameAsString();
                if (name.equals(getterName) || name.equals(booleanGetterName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Adds typed convenience overloads on the generated {@code *Impl} classes that mirror the shape emitted by the
     * pre-migration swagger {@code generate-client-as-impl} code (typed parameters + {@code ResponseBase<XxxHeaders,T>}
     * return type). typespec-java only emits protocol methods
     * ({@code xxxWithResponse[Async](RequestOptions)&nbsp;-&gt;&nbsp;Response<Void>/BinaryData}) which discard typed
     * response headers and drop the typed request parameters. The shipped hand-written {@code Share*} clients rely on
     * the swagger-shaped signatures, so this method reinstates them per the TypeSpec team's interim guidance:
     * "a manual mapping can be added from the implementation layer {@code Response<Void>} to
     * {@code ResponseBase<SharesCreateSnapshotHeaders, Void>}." Each wrapper builds a {@link RequestOptions} from its
     * typed parameters, calls the generated {@code service} proxy directly with a share-scoped URL, maps the raw
     * response into a typed {@code ResponseBase} using the {@code *Headers} constructor that consumes the raw
     * {@link com.azure.core.http.HttpHeaders}, and applies {@code ShareStorageExceptionInternal} error mapping.
     * <p>
     * The {@code *Headers} classes in {@code implementation/models} are the pre-migration AutoRest-generated files
     * that typespec-java does not yet emit; they are kept as-is (treated as hand-written) until the emitter grows an
     * equivalent of the TypeScript emitter's {@code include-headers-in-response} option.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void addTypedImplWrappers(LibraryCustomization customization, Logger logger) {
        // createSnapshot (SharesImpl) -- template implementation, extend to the remaining operations.
        customization.getClass("com.azure.storage.file.share.implementation", "SharesImpl").customizeAst(ast -> {
            // ResponseBase / SharesCreateSnapshotHeaders / Map are not present in the emitted file; the rest
            // (RequestOptions, Response, HttpHeaderName, Context, Mono, ShareStorageExceptionInternal, ModelHelper)
            // are already imported by typespec-java's emit + updateImplToMapInternalException.
            ast.addImport("com.azure.core.http.rest.ResponseBase");
            ast.addImport("com.azure.storage.file.share.implementation.models.SharesCreateSnapshotHeaders");
            ast.addImport("java.util.Map");

            ast.getClassByName("SharesImpl").ifPresent(clazz -> {
                addBodyDeclaration(clazz,
                    "public Mono<ResponseBase<SharesCreateSnapshotHeaders, Void>>"
                        + " createSnapshotWithResponseAsync(String shareName, Integer timeout,"
                        + " Map<String, String> metadata, Context context) {\n"
                        + "    RequestOptions requestOptions = buildCreateSnapshotRequestOptions(timeout, metadata);\n"
                        + "    String url = this.client.getUrl() + \"/\" + shareName;\n"
                        + "    return service.createSnapshot(url, this.client.getServiceVersion().getVersion(),\n"
                        + "            this.client.getFileRequestIntent(), requestOptions, context)\n"
                        + "        .onErrorMap(ShareStorageExceptionInternal.class,"
                        + " ModelHelper::mapToShareStorageException)\n"
                        + "        .map(response -> new ResponseBase<>(response.getRequest(),"
                        + " response.getStatusCode(),\n"
                        + "            response.getHeaders(), response.getValue(),\n"
                        + "            new SharesCreateSnapshotHeaders(response.getHeaders())));\n"
                        + "}\n");

                addBodyDeclaration(clazz,
                    "public ResponseBase<SharesCreateSnapshotHeaders, Void>"
                        + " createSnapshotWithResponse(String shareName, Integer timeout,"
                        + " Map<String, String> metadata, Context context) {\n"
                        + "    RequestOptions requestOptions = buildCreateSnapshotRequestOptions(timeout, metadata);\n"
                        + "    String url = this.client.getUrl() + \"/\" + shareName;\n"
                        + "    try {\n"
                        + "        Response<Void> response = service.createSnapshotSync(url,"
                        + " this.client.getServiceVersion().getVersion(),\n"
                        + "            this.client.getFileRequestIntent(), requestOptions, context);\n"
                        + "        return new ResponseBase<>(response.getRequest(), response.getStatusCode(),\n"
                        + "            response.getHeaders(), response.getValue(),\n"
                        + "            new SharesCreateSnapshotHeaders(response.getHeaders()));\n"
                        + "    } catch (ShareStorageExceptionInternal internalException) {\n"
                        + "        throw ModelHelper.mapToShareStorageException(internalException);\n"
                        + "    }\n"
                        + "}\n");

                // Shared RequestOptions builder for both overloads. Kept private + static so it does not extend the
                // impl's public surface. timeout -> ?timeout=N query param; metadata -> x-ms-meta-<key> headers.
                addBodyDeclaration(clazz,
                    "private static RequestOptions buildCreateSnapshotRequestOptions("
                        + "Integer timeout, Map<String, String> metadata) {\n"
                        + "    RequestOptions requestOptions = new RequestOptions();\n"
                        + "    if (timeout != null) {\n"
                        + "        requestOptions.addQueryParam(\"timeout\", String.valueOf(timeout));\n"
                        + "    }\n"
                        + "    if (metadata != null) {\n"
                        + "        for (Map.Entry<String, String> entry : metadata.entrySet()) {\n"
                        + "            requestOptions.setHeader("
                        + "HttpHeaderName.fromString(\"x-ms-meta-\" + entry.getKey()),\n"
                        + "                entry.getValue());\n"
                        + "        }\n"
                        + "    }\n"
                        + "    return requestOptions;\n"
                        + "}\n");
            });
            logger.info("Added typed createSnapshot wrappers to SharesImpl");
        });
    }

    /**
     * Parses {@code source} as a body declaration (method/constructor/field) and appends it to {@code clazz}.
     */
    private static void addBodyDeclaration(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration clazz,
        String source) {
        clazz.addMember(StaticJavaParser.parseBodyDeclaration(source));
    }

    /**
     * Customizes the implementation classes that will perform calls to the service. The following logic is used:
     * <p>
     * - Check for the return of the method not equaling to PagedFlux, PagedIterable, PollerFlux, or SyncPoller. Those
     * types wrap other APIs and those APIs being update is the correct change.
     * - For asynchronous methods, add a call to
     * {@code .onErrorMap(ShareStorageExceptionInternal.class, ModelHelper::mapToShareStorageException)} to handle
     * mapping ShareStorageExceptionInternal to ShareStorageException.
     * - For synchronous methods, wrap the return statement in a try-catch block that catches
     * ShareStorageExceptionInternal and rethrows {@code ModelHelper.mapToShareStorageException(e)}. Or, for void
     * methods wrap the last statement.
     *
     * @param implPackage The implementation package.
     */
    private static void updateImplToMapInternalException(PackageCustomization implPackage) {
        List<String> implsToUpdate = Arrays.asList("DirectoriesImpl", "FilesImpl", "ServicesImpl", "SharesImpl");
        for (String implToUpdate : implsToUpdate) {
            implPackage.getClass(implToUpdate).customizeAst(ast -> {
                ast.addImport("com.azure.storage.file.share.implementation.util.ModelHelper");
                ast.addImport("com.azure.storage.file.share.models.ShareStorageException");
                ast.addImport("com.azure.storage.file.share.implementation.models.ShareStorageExceptionInternal");
                ast.getClassByName(implToUpdate).ifPresent(clazz -> {
                    clazz.getFields();

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
            + ".onErrorMap(ShareStorageExceptionInternal.class, ModelHelper::mapToShareStorageException)"
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
            "throw ModelHelper.mapToShareStorageException(internalException);")));
        Parameter catchParameter = new Parameter().setType("ShareStorageExceptionInternal")
            .setName("internalException");
        CatchClause catchClause = new CatchClause(catchParameter, catchBlock);
        TryStmt tryCatchMap = new TryStmt(tryBlock, new NodeList<>(catchClause), null);

        // Replace the last statement with the try-catch block.
        method.setBody(new BlockStmt(new NodeList<>(tryCatchMap)));
    }
}
