// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.modules.ModuleDirective;
import com.github.javaparser.ast.modules.ModuleExportsDirective;
import com.github.javaparser.ast.modules.ModuleOpensDirective;
import com.github.javaparser.ast.modules.ModuleRequiresDirective;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    // The generator emits three layers of code:
    //   1. implementation/*Impl: raw REST operations. Kept.
    //   2. Per-resource convenience clients: typed WithResponse methods over the Impl layer. Kept, but moved into the
    //      implementation package (see CONVENIENCE_CLIENTS_TO_RELOCATE).
    //   3. Top-level service client, async client and builders: deleted (this list).
    // The public API is the hand-written Share* clients, so layer 3 would only duplicate it. The list also deletes the
    // generated service version enum, as the hand-written ShareServiceVersion is the public one.
    private static final List<String> GENERATED_CLIENTS_TO_REMOVE = Arrays.asList(
        // Main service-client public surface — the impl (AzureFileStorageImpl) is kept; only the
        // public client/async-client/builder are removed. Both naming variants are listed because
        // removeFile is a no-op when absent.
        "FileClientBuilder",
        "AzureFileStorageClient", "AzureFileStorageAsyncClient", "AzureFileStorageClientBuilder",
        "AzureFileStorageBuilder",
        // The hand-written ShareServiceVersion is authoritative; the generated enum is discarded and new service
        // versions are added by hand. 
        "FileServiceVersion");

    // Per-resource convenience clients emitted by typespec-java. They carry the typed WithResponse
    // methods (ResponseBase<XxxHeaders, Model> / Response<XxxHeaders>) that wrap the protocol
    // *WithResponseInternal methods, so they are RETAINED as the internal typed layer. They are moved
    // out of the public package into implementation so they add no public API, and the hand-written
    // Share* clients delegate to them. The generated ShareClient/ShareAsyncClient names collide with
    // the hand-written public ShareClient/ShareAsyncClient; relocating into implementation resolves it.
    private static final List<String> CONVENIENCE_CLIENTS_TO_RELOCATE = Arrays.asList(
        "ServiceClient", "ServiceAsyncClient",
        "DirectoryClient", "DirectoryAsyncClient",
        "FileClient", "FileAsyncClient",
        "ShareClient", "ShareAsyncClient");

    private static final String MODULE_INFO_PATH = "src/main/java/module-info.java";

    // Directives the hand-written code needs that the generated module-info doesn't declare: the storage-common
    // dependency, the hand-written public packages, and the implementation packages azure-core reflects on.
    private static final List<String> MODULE_INFO_DIRECTIVES = Arrays.asList(
        "requires transitive com.azure.storage.common;",
        "exports com.azure.storage.file.share.options;",
        "exports com.azure.storage.file.share.sas;",
        "exports com.azure.storage.file.share.specialized;",
        "opens com.azure.storage.file.share.implementation to com.azure.core;",
        "opens com.azure.storage.file.share.implementation.util to com.azure.core;");

    private static final List<Class<?>> DIRECTIVE_ORDER
        = Arrays.asList(ModuleRequiresDirective.class, ModuleExportsDirective.class, ModuleOpensDirective.class);

    // Generated implementation classes typed to the generated FileServiceVersion enum, which is deleted (above) in
    // favor of the hand-written public ShareServiceVersion. These are retyped to ShareServiceVersion after generation.
    private static final List<String> IMPLS_USING_SERVICE_VERSION
        = Arrays.asList("AzureFileStorageImpl", "DirectoriesImpl", "FilesImpl", "ServicesImpl", "SharesImpl");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        removeGeneratedConvenienceClients(customization, logger);

        patchModuleInfo(customization);

        relocateConvenienceClientsToImplementation(customization, logger);

        retypeServiceVersionToShareServiceVersion(customization, logger);

        restoreFluentModels(customization, logger);

        restoreShareFileRangeListNextMarker(customization, logger);

        restoreShareStorageErrorJson(customization, logger);

        updateImplToMapInternalException(customization.getPackage("com.azure.storage.file.share.implementation"));
    }

    /**
     * Deletes the generated builders / main service-client surface so the shipped public surface is limited to the
     * hand-written Share*-prefixed clients. The generated {@code implementation/*Impl} operation layer and the
     * relocated convenience clients are retained.
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
    }

    /**
     * Adds the directives the hand-written code needs to the generated {@code module-info.java}, keeping everything
     * the generator declares, and keeps the directives grouped by kind.
     *
     * @param customization The library customization.
     */
    private static void patchModuleInfo(LibraryCustomization customization) {
        Editor editor = customization.getRawEditor();
        CompilationUnit ast = StaticJavaParser.parse(editor.getFileContent(MODULE_INFO_PATH));
        ast.getModule().ifPresent(module -> {
            NodeList<ModuleDirective> directives = module.getDirectives();
            MODULE_INFO_DIRECTIVES.stream()
                .map(StaticJavaParser::parseModuleDirective)
                .filter(directive -> !directives.contains(directive))
                .forEach(directives::add);
            directives.sort(Comparator.comparingInt(directive -> DIRECTIVE_ORDER.indexOf(directive.getClass())));
        });
        // JavaParser doesn't print the comments ahead of a module declaration, so carry the license header over.
        String header = ast.getOrphanComments()
            .stream()
            .map(comment -> comment.toString().trim())
            .collect(Collectors.joining(System.lineSeparator()));
        editor.replaceFile(MODULE_INFO_PATH, header + System.lineSeparator() + System.lineSeparator() + ast);
    }

    /**
     * Moves each generated convenience client into the {@code implementation} package and renames it with an
     * {@code Internal} suffix, e.g. {@code FileClient} becomes {@code ShareFileClientInternal}.
     * <p>
     * These clients provide the typed {@code WithResponse} methods (for example
     * {@code ResponseBase<FilesGetRangeListHeaders, ShareFileRangeList> getRangeListWithResponse(...)}) that the
     * hand-written {@code Share*} clients delegate to. Moving them:
     * <ul>
     * <li>keeps them out of the public API, because {@code implementation} is not exported;</li>
     * <li>avoids a name clash between the generated and hand-written {@code ShareClient}/{@code ShareAsyncClient}.</li>
     * </ul>
     * Each moved class also gets a public constructor, because the hand-written clients now call it from another
     * package, and loses its {@code @ServiceClient} annotation, because the builder it refers to is deleted.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void relocateConvenienceClientsToImplementation(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        for (String className : CONVENIENCE_CLIENTS_TO_RELOCATE) {
            String newName = internalClientName(className);
            String oldPath = PKG_ROOT + className + ".java";
            String content = editor.getFileContent(oldPath);
            // Move to the implementation package.
            content = content.replace("package com.azure.storage.file.share;",
                "package com.azure.storage.file.share.implementation;");
            // Drop the @ServiceClient marker annotation (its builder AzureFileStorageBuilder is deleted). The
            // class literally named ServiceClient carries the annotation fully-qualified to avoid the name clash,
            // so match both @ServiceClient(...) and @com.azure.core.annotation.ServiceClient(...).
            content = content.replaceAll("(?m)^@(com\\.azure\\.core\\.annotation\\.)?ServiceClient\\([^)]*\\)\\r?\\n", "");
            content = content.replace("import com.azure.core.annotation.ServiceClient;" + System.lineSeparator(), "");
            content = content.replace("import com.azure.core.annotation.ServiceClient;\n", "");
            // Make the package-private constructor public; callers now live in a different package.
            content = content.replaceFirst("(?m)^(\\s*)" + className + "\\(", "$1public " + className + "(");
            // Rename the class (declaration, constructor, self-references) to the Share*-Internal name.
            content = content.replaceAll("\\b" + className + "\\b", newName);
            editor.removeFile(oldPath);
            editor.addFile(PKG_ROOT + "implementation/" + newName + ".java", content);
            logger.info("Relocated convenience client {} -> implementation/{}", className, newName);
        }
    }

    /**
     * Maps a generated per-resource convenience client name to its internal name: the hand-written {@code Share*}
     * naming with an {@code Internal} suffix. E.g. {@code FileClient} -> {@code ShareFileClientInternal},
     * {@code DirectoryAsyncClient} -> {@code ShareDirectoryAsyncClientInternal}, {@code ShareClient} ->
     * {@code ShareClientInternal}.
     *
     * @param generatedName The generated convenience client name.
     * @return The internal client name.
     */
    private static String internalClientName(String generatedName) {
        String withSharePrefix = generatedName.startsWith("Share") ? generatedName : "Share" + generatedName;
        return withSharePrefix + "Internal";
    }

    /**
     * Retypes the generated {@code implementation/*Impl} classes from the generated {@code FileServiceVersion} enum
     * (deleted by {@link #removeGeneratedConvenienceClients}) to the hand-written public
     * {@code com.azure.storage.file.share.ShareServiceVersion}. Both enums live in {@code com.azure.storage.file.share}
     * and share the same shape ({@code implements ServiceVersion}, {@code getVersion()}, {@code getLatest()}), so
     * replacing the type token is safe; it also aligns the impl constructors with the {@code ShareServiceVersion} the
     * hand-written builders pass.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void retypeServiceVersionToShareServiceVersion(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        for (String implName : IMPLS_USING_SERVICE_VERSION) {
            String path = PKG_ROOT + "implementation/" + implName + ".java";
            String content = editor.getFileContent(path);
            if (content.contains("FileServiceVersion")) {
                editor.replaceFile(path, content.replace("FileServiceVersion", "ShareServiceVersion"));
                logger.info("Retyped FileServiceVersion -> ShareServiceVersion in {}", path);
            }
        }
    }

    /**
     * Keeps the {@code nextMarker} property on {@code ShareFileRangeList}, which shipped in GA before the TypeSpec
     * migration but is absent from the spec model. The spec splits range listing into {@code getRangeList}
     * ({@code ShareFileRangeList}, no marker) and the paged {@code listAllRanges} ({@code ShareFileRangeListSegment},
     * which carries {@code NextMarker}); the {@code listAllRanges} / {@code listAllRangesDiff} paged iterables use the
     * latter, as the other languages do. The getter and setter are kept only for backward compatibility and are
     * deprecated. The field, getter and setter are added ahead of {@code toXml}, and the XML round trip is extended to
     * write and read {@code NextMarker}.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void restoreShareFileRangeListNextMarker(LibraryCustomization customization, Logger logger) {
        customization.getClass(MODELS_PACKAGE, "ShareFileRangeList").customizeAst(ast -> ast
            .getClassByName("ShareFileRangeList")
            .ifPresent(clazz -> {
                if (clazz.getFieldByName("nextMarker").isPresent()) {
                    return;
                }
                String deprecation = "Use {@code ShareFileClient.listAllRanges} / {@code listAllRangesDiff} (or the "
                    + "async equivalents), which page through ranges and handle the continuation marker.";

                MethodDeclaration getter = new MethodDeclaration();
                getter.setName("getNextMarker");
                getter.setModifiers(Modifier.Keyword.PUBLIC);
                getter.setType("String");
                getter.setBody(StaticJavaParser.parseBlock("{ return this.nextMarker; }"));
                getter.addMarkerAnnotation("Deprecated");
                getter.setJavadocComment(
                    new Javadoc(JavadocDescription.parseText("Get the nextMarker property: The NextMarker property."))
                        .addBlockTag("return", "the nextMarker value.")
                        .addBlockTag("deprecated", deprecation));

                MethodDeclaration setter = new MethodDeclaration();
                setter.setName("setNextMarker");
                setter.setModifiers(Modifier.Keyword.PUBLIC);
                setter.setType("ShareFileRangeList");
                setter.addParameter("String", "nextMarker");
                setter.setBody(StaticJavaParser.parseBlock("{ this.nextMarker = nextMarker; return this; }"));
                setter.addMarkerAnnotation("Deprecated");
                setter.setJavadocComment(
                    new Javadoc(JavadocDescription.parseText("Set the nextMarker property: The NextMarker property."))
                        .addBlockTag("param", "nextMarker", "the nextMarker value to set.")
                        .addBlockTag("return", "the ShareFileRangeList object itself.")
                        .addBlockTag("deprecated", deprecation));

                NodeList<BodyDeclaration<?>> members = clazz.getMembers();
                int toXmlIndex = indexOfFirstMethod(members, "toXml");
                members.add(toXmlIndex, setter);
                members.add(toXmlIndex, getter);
                members.add(toXmlIndex, StaticJavaParser.parseBodyDeclaration("private String nextMarker;"));

                // Write NextMarker last, ahead of the closing element.
                clazz.getMethodsBySignature("toXml", "XmlWriter", "String").forEach(toXml -> {
                    BlockStmt body = toXml.getBody().get();
                    body.addStatement(body.getStatements().size() - 1, StaticJavaParser
                        .parseStatement("xmlWriter.writeStringElement(\"NextMarker\", this.nextMarker);"));
                });

                // Read NextMarker in a new branch after ClearRange, ahead of the branch skipping unknown elements.
                clazz.getMethodsBySignature("fromXml", "XmlReader", "String")
                    .forEach(fromXml -> fromXml
                        .findFirst(IfStmt.class, branch -> branch.getCondition().toString().contains("\"ClearRange\""))
                        .ifPresent(clearRangeBranch -> clearRangeBranch.setElseStmt(new IfStmt(
                            StaticJavaParser.parseExpression("\"NextMarker\".equals(elementName.getLocalPart())"),
                            StaticJavaParser.parseBlock(
                                "{ deserializedShareFileRangeList.nextMarker = reader.getStringElement(); }"),
                            clearRangeBranch.getElseStmt().map(Statement::clone).orElse(null)))));
                logger.info("Restored ShareFileRangeList.nextMarker");
            }));
    }

    /**
     * Restores the "fluent" shape of models that shipped as {@code @Fluent} (public no-arg constructor + setters)
     * before the TypeSpec migration. With {@code required-fields-as-ctor-args: true} (which must stay true so new
     * models keep required parameters on their constructor) these regenerate as {@code @Immutable} with a
     * required-args constructor and no setters -- a breaking change. Each named model is converted back purely via the
     * JavaParser AST (no string replacement): {@code @Immutable} becomes {@code @Fluent}, {@code final} is removed from
     * instance fields, a public no-arg constructor is added, a public fluent setter is added for each field, and the
     * generated required-args constructor is made private (it is not part of the shipped API; {@code fromXml}/
     * {@code fromJson}, which are left untouched, still use it).
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
                // The emitter makes the required-args constructor public on input models; the shipped fluent models
                // only expose the no-arg constructor, so keep it private (fromXml/fromJson still use it).
                clazz.getConstructors().stream()
                    .filter(ctor -> !ctor.getParameters().isEmpty())
                    .forEach(ctor -> ctor.setModifiers(Modifier.Keyword.PRIVATE));
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

    private static int indexOfFirstMethod(NodeList<BodyDeclaration<?>> members, String methodName) {
        for (int i = 0; i < members.size(); i++) {
            BodyDeclaration<?> member = members.get(i);
            if (member instanceof MethodDeclaration
                && ((MethodDeclaration) member).getNameAsString().equals(methodName)) {
                return i;
            }
        }
        return members.size();
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
     * Restores JSON deserialization on the internal {@code ShareStorageError}. The emitter generates a model as either
     * {@code XmlSerializable} or {@code JsonSerializable}, never both, so the XML-only error model would fall back to
     * azure-core's reflective Jackson deserializer for a JSON error body. On the module path Jackson cannot access
     * {@code implementation.models} (it is opened only to {@code com.azure.core}), so the error would surface as a
     * plain {@code HttpResponseException}/{@code RuntimeException} instead of {@code ShareStorageException}. The
     * pre-migration model implemented both interfaces; this restores that, accepting both the bare and the
     * {@code {"error": {...}}}-wrapped forms. Each field is read and written under its own (camel case) name, as the
     * pre-migration model did, so fields the spec adds are picked up automatically.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void restoreShareStorageErrorJson(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.storage.file.share.implementation.models", "ShareStorageError")
            .customizeAst(ast -> ast.getClassByName("ShareStorageError").ifPresent(clazz -> {
                if (!clazz.getMethodsByName("fromJson").isEmpty()) {
                    return;
                }
                ast.addImport("com.azure.json.JsonReader");
                ast.addImport("com.azure.json.JsonSerializable");
                ast.addImport("com.azure.json.JsonToken");
                ast.addImport("com.azure.json.JsonWriter");
                ast.addImport("java.io.IOException");
                clazz.addImplementedType("JsonSerializable<ShareStorageError>");

                List<VariableDeclarator> fields = clazz.getFields()
                    .stream()
                    .filter(field -> !field.isStatic())
                    .map(field -> field.getVariable(0))
                    .collect(Collectors.toList());

                BlockStmt toJsonBody = new BlockStmt().addStatement("jsonWriter.writeStartObject();");
                fields.forEach(field -> toJsonBody.addStatement(jsonWriteStatement(field)));
                toJsonBody.addStatement("return jsonWriter.writeEndObject();");
                MethodDeclaration toJson = clazz.addMethod("toJson", Modifier.Keyword.PUBLIC);
                toJson.setType("JsonWriter");
                toJson.addParameter("JsonWriter", "jsonWriter");
                toJson.addThrownException(StaticJavaParser.parseClassOrInterfaceType("IOException"));
                toJson.addMarkerAnnotation("Override");
                toJson.setBody(toJsonBody);

                MethodDeclaration fromJson = clazz.addMethod("fromJson", Modifier.Keyword.PUBLIC,
                    Modifier.Keyword.STATIC);
                fromJson.setType("ShareStorageError");
                fromJson.addParameter("JsonReader", "jsonReader");
                fromJson.addThrownException(StaticJavaParser.parseClassOrInterfaceType("IOException"));
                fromJson.setBody(StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "    return jsonReader.readObject(reader -> {",
                    "        // The error may be the object itself or wrapped in an \"error\" node.",
                    "        JsonReader bufferedReader = reader.bufferObject();",
                    "        bufferedReader.nextToken();",
                    "        while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {",
                    "            String fieldName = bufferedReader.getFieldName();",
                    "            bufferedReader.nextToken();",
                    "            if (\"error\".equals(fieldName)) {",
                    "                return readShareStorageError(bufferedReader);",
                    "            }",
                    "            bufferedReader.skipChildren();",
                    "        }",
                    "        return readShareStorageError(bufferedReader.reset());",
                    "    });",
                    "}")));
                fromJson.setJavadocComment(new Javadoc(
                    JavadocDescription.parseText("Reads an instance of ShareStorageError from the JsonReader."))
                    .addBlockTag("param", "jsonReader", "The JsonReader being read.")
                    .addBlockTag("return", "An instance of ShareStorageError if the JsonReader was pointing to an "
                        + "instance of it, or null if it was pointing to JSON null.")
                    .addBlockTag("throws", "IOException", "If an error occurs while reading the ShareStorageError."));

                // One branch per field, then skip anything else.
                Statement readField = StaticJavaParser.parseBlock("{ reader.skipChildren(); }");
                for (int i = fields.size() - 1; i >= 0; i--) {
                    String name = fields.get(i).getNameAsString();
                    readField = new IfStmt(StaticJavaParser.parseExpression("\"" + name + "\".equals(fieldName)"),
                        new BlockStmt().addStatement("deserialized." + name + " = " + jsonReadExpression(fields.get(i))
                            + ";"),
                        readField);
                }
                BlockStmt readObject = StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "    ShareStorageError deserialized = new ShareStorageError();",
                    "    while (reader.nextToken() != JsonToken.END_OBJECT) {",
                    "        String fieldName = reader.getFieldName();",
                    "        reader.nextToken();",
                    "    }",
                    "    return deserialized;",
                    "}"));
                readObject.findFirst(WhileStmt.class).get().getBody().asBlockStmt().addStatement(readField);
                MethodDeclaration readShareStorageError = clazz.addMethod("readShareStorageError",
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
                readShareStorageError.setType("ShareStorageError");
                readShareStorageError.addParameter("JsonReader", "jsonReader");
                readShareStorageError.addThrownException(StaticJavaParser.parseClassOrInterfaceType("IOException"));
                readShareStorageError.setBody(new BlockStmt().addStatement(new ReturnStmt(new MethodCallExpr(
                    new NameExpr("jsonReader"), "readObject",
                    new NodeList<>(new LambdaExpr(new Parameter(new UnknownType(), "reader"), readObject))))));
                logger.info("Restored JSON deserialization on ShareStorageError");
            }));
    }

    // Writes a ShareStorageError field: strings and numbers directly, and expandable string enums by their value.
    private static String jsonWriteStatement(VariableDeclarator field) {
        String name = field.getNameAsString();
        switch (field.getTypeAsString()) {
            case "String":
                return "jsonWriter.writeStringField(\"" + name + "\", this." + name + ");";

            case "Integer":
                return "jsonWriter.writeNumberField(\"" + name + "\", this." + name + ");";

            default:
                return "jsonWriter.writeStringField(\"" + name + "\", this." + name + " == null ? null : this." + name
                    + ".toString());";
        }
    }

    // Reads a ShareStorageError field, the counterpart of jsonWriteStatement.
    private static String jsonReadExpression(VariableDeclarator field) {
        switch (field.getTypeAsString()) {
            case "String":
                return "reader.getString()";

            case "Integer":
                return "reader.getNullable(JsonReader::getInt)";

            default:
                return field.getTypeAsString() + ".fromString(reader.getString())";
        }
    }

    /**
     * Customizes the implementation classes that will perform calls to the service. The following logic is used:
     * <p>
     * - Check for the return of the method not equaling to PagedFlux, PagedIterable, PollerFlux, or SyncPoller. Those
     * types wrap other APIs and those APIs being update is the correct change.
     * - For asynchronous methods, add a call to
     * {@code .onErrorMap(ShareStorageErrorException.class, ModelHelper::mapToShareStorageException)} to handle
     * mapping ShareStorageErrorException to ShareStorageException.
     * - For synchronous methods, wrap the return statement in a try-catch block that catches
     * ShareStorageErrorException and rethrows {@code ModelHelper.mapToShareStorageException(e)}. Or, for void
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
                ast.addImport("com.azure.storage.file.share.implementation.models.ShareStorageErrorException");
                ast.getClassByName(implToUpdate).ifPresent(clazz -> {
                    clazz.getMethods().forEach(methodDeclaration -> {
                        // getService() is a plain accessor for the RestProxy service instance; it never does I/O.
                        if (methodDeclaration.getNameAsString().equals("getService")) {
                            return;
                        }

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
            + ".onErrorMap(ShareStorageErrorException.class, ModelHelper::mapToShareStorageException)"
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
        Parameter catchParameter = new Parameter().setType("ShareStorageErrorException")
            .setName("internalException");
        CatchClause catchClause = new CatchClause(catchParameter, catchBlock);
        TryStmt tryCatchMap = new TryStmt(tryBlock, new NodeList<>(catchClause), null);

        // Replace the last statement with the try-catch block.
        method.setBody(new BlockStmt(new NodeList<>(tryCatchMap)));
    }
}
