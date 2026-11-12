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
import java.util.Set;

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

    // Generated builders / main service-client surface emitted by typespec-java on top of the
    // implementation/*Impl operation layer. These are deleted; the shipped public surface is the
    // hand-written Share*-prefixed clients. (The per-resource convenience clients are NOT deleted —
    // they are relocated into the implementation package as the internal typed layer; see
    // CONVENIENCE_CLIENTS_TO_RELOCATE / relocateConvenienceClientsToImplementation.)
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

    private static final List<String> GENERATED_DESCRIPTOR_FILES_TO_REMOVE = Arrays.asList("src/main/java/module-info.java");

    // Generated implementation classes typed to the generated FileServiceVersion enum, which is deleted (above) in
    // favor of the hand-written public ShareServiceVersion. These are retyped to ShareServiceVersion after generation.
    private static final List<String> IMPLS_USING_SERVICE_VERSION
        = Arrays.asList("AzureFileStorageImpl", "DirectoriesImpl", "FilesImpl", "ServicesImpl", "SharesImpl");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        removeGeneratedConvenienceClients(customization, logger);

        relocateConvenienceClientsToImplementation(customization, logger);

        retypeServiceVersionToShareServiceVersion(customization, logger);

        restoreFluentModels(customization, logger);

        restoreShareFileRangeListNextMarker(customization, logger);

        restoreShareStorageErrorJson(customization, logger);

        updateImplToMapInternalException(customization.getPackage("com.azure.storage.file.share.implementation"));
    }

    /**
     * Deletes the generated builders / main service-client surface and descriptor files so the shipped
     * public surface is limited to the hand-written Share*-prefixed clients. The generated
     * {@code implementation/*Impl} operation layer and the relocated convenience clients are retained.
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
     * Moves the generated per-resource convenience clients from the public
     * {@code com.azure.storage.file.share} package into {@code com.azure.storage.file.share.implementation} and
     * renames them to the hand-written {@code Share*} naming with an {@code Internal} suffix (e.g. {@code FileClient}
     * -> {@code ShareFileClientInternal}, {@code ShareClient} -> {@code ShareClientInternal}).
     * They carry the typed {@code WithResponse} methods (e.g.
     * {@code ResponseBase<FilesGetRangeListHeaders, ShareFileRangeList> getRangeListWithResponse(...)}) that wrap the
     * protocol {@code *WithResponseInternal} methods, so they are kept as the internal typed layer instead of being
     * hand-written. Relocating them (1) keeps them off the public API surface (implementation is not exported) and
     * (2) resolves the name collision between the generated {@code ShareClient}/{@code ShareAsyncClient} and the
     * hand-written public ones. The package-private constructors are made public so the hand-written Share* clients
     * (now in a different package) can construct them from {@code AzureFileStorageImpl.get*()}. The
     * {@code @ServiceClient} marker annotation is dropped because its {@code AzureFileStorageBuilder} is deleted.
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
     * latter, as the other languages do. The getter/setter are retained only for backward compatibility and are
     * deprecated. Field, getter, fluent setter, and XML round-trip (matching the shipped shape) are added here;
     * {@code restoreFluentModels} skips the field because it already has a setter.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void restoreShareFileRangeListNextMarker(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        String path = PKG_ROOT + "models/ShareFileRangeList.java";
        String content = editor.getFileContent(path);
        if (content.contains("getNextMarker")) {
            return;
        }
        content = content.replace(
            "    @Generated\n    @Override\n    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {\n"
                + "        return toXml(xmlWriter, null);\n    }\n",
            "    private String nextMarker;\n\n"
                + "    /**\n     * Get the nextMarker property: The NextMarker property.\n     *\n"
                + "     * @return the nextMarker value.\n"
                + "     * @deprecated Use {@code ShareFileClient.listAllRanges} / {@code listAllRangesDiff} (or the async\n"
                + "     * equivalents), which page through ranges and handle the continuation marker.\n     */\n"
                + "    @Deprecated\n"
                + "    public String getNextMarker() {\n        return this.nextMarker;\n    }\n\n"
                + "    /**\n     * Set the nextMarker property: The NextMarker property.\n     *\n"
                + "     * @param nextMarker the nextMarker value to set.\n"
                + "     * @return the ShareFileRangeList object itself.\n"
                + "     * @deprecated Use {@code ShareFileClient.listAllRanges} / {@code listAllRangesDiff} (or the async\n"
                + "     * equivalents), which page through ranges and handle the continuation marker.\n     */\n"
                + "    @Deprecated\n"
                + "    public ShareFileRangeList setNextMarker(String nextMarker) {\n"
                + "        this.nextMarker = nextMarker;\n        return this;\n    }\n\n"
                + "    @Generated\n    @Override\n    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {\n"
                + "        return toXml(xmlWriter, null);\n    }\n");
        content = content.replace("        }\n        return xmlWriter.writeEndElement();",
            "        }\n        xmlWriter.writeStringElement(\"NextMarker\", this.nextMarker);\n"
                + "        return xmlWriter.writeEndElement();");
        content = content.replace(
            "                    deserializedShareFileRangeList.clearRanges.add(ClearRange.fromXml(reader, \"ClearRange\"));\n"
                + "                } else {",
            "                    deserializedShareFileRangeList.clearRanges.add(ClearRange.fromXml(reader, \"ClearRange\"));\n"
                + "                } else if (\"NextMarker\".equals(elementName.getLocalPart())) {\n"
                + "                    deserializedShareFileRangeList.nextMarker = reader.getStringElement();\n"
                + "                } else {");
        editor.replaceFile(path, content);
        logger.info("Restored ShareFileRangeList.nextMarker (getter/setter + XML round-trip)");
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
     * {@code {"error": {...}}}-wrapped forms.
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

                clazz.addMember(StaticJavaParser.parseBodyDeclaration(String.join("\n",
                    "@Override",
                    "public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {",
                    "    jsonWriter.writeStartObject();",
                    "    jsonWriter.writeStringField(\"code\", this.code == null ? null : this.code.toString());",
                    "    jsonWriter.writeStringField(\"message\", this.message);",
                    "    jsonWriter.writeNumberField(\"copySourceStatusCode\", this.copySourceStatusCode);",
                    "    jsonWriter.writeStringField(\"copySourceErrorCode\", this.copySourceErrorCode);",
                    "    jsonWriter.writeStringField(\"copySourceErrorMessage\", this.copySourceErrorMessage);",
                    "    jsonWriter.writeStringField(\"headerName\", this.headerName);",
                    "    jsonWriter.writeStringField(\"headerValue\", this.headerValue);",
                    "    jsonWriter.writeStringField(\"queryParameterName\", this.queryParameterName);",
                    "    jsonWriter.writeStringField(\"queryParameterValue\", this.queryParameterValue);",
                    "    jsonWriter.writeStringField(\"reason\", this.reason);",
                    "    jsonWriter.writeStringField(\"extendedErrorDetail\", this.extendedErrorDetail);",
                    "    return jsonWriter.writeEndObject();",
                    "}")));

                MethodDeclaration fromJson = StaticJavaParser.parseMethodDeclaration(String.join("\n",
                    "public static ShareStorageError fromJson(JsonReader jsonReader) throws IOException {",
                    "    return jsonReader.readObject(reader -> {",
                    "        // The error may be the object itself or wrapped in an \"error\" node.",
                    "        JsonReader bufferedReader = reader.bufferObject();",
                    "        bufferedReader.nextToken();",
                    "        while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {",
                    "            String fieldName = bufferedReader.getFieldName();",
                    "            bufferedReader.nextToken();",
                    "            if (\"error\".equals(fieldName)) {",
                    "                return readShareStorageError(bufferedReader);",
                    "            } else {",
                    "                bufferedReader.skipChildren();",
                    "            }",
                    "        }",
                    "        return readShareStorageError(bufferedReader.reset());",
                    "    });",
                    "}"));
                fromJson.setJavadocComment(String.join("\n",
                    "Reads an instance of ShareStorageError from the JsonReader.",
                    "",
                    "@param jsonReader The JsonReader being read.",
                    "@return An instance of ShareStorageError if the JsonReader was pointing to an instance of it, or "
                        + "null if it was pointing to JSON null.",
                    "@throws IOException If an error occurs while reading the ShareStorageError."));
                clazz.addMember(fromJson);

                clazz.addMember(StaticJavaParser.parseBodyDeclaration(String.join("\n",
                    "private static ShareStorageError readShareStorageError(JsonReader jsonReader) throws IOException {",
                    "    return jsonReader.readObject(reader -> {",
                    "        ShareStorageError deserialized = new ShareStorageError();",
                    "        while (reader.nextToken() != JsonToken.END_OBJECT) {",
                    "            String fieldName = reader.getFieldName();",
                    "            reader.nextToken();",
                    "            if (\"code\".equals(fieldName)) {",
                    "                deserialized.code = ShareErrorCode.fromString(reader.getString());",
                    "            } else if (\"message\".equals(fieldName)) {",
                    "                deserialized.message = reader.getString();",
                    "            } else if (\"copySourceStatusCode\".equals(fieldName)) {",
                    "                deserialized.copySourceStatusCode = reader.getNullable(JsonReader::getInt);",
                    "            } else if (\"copySourceErrorCode\".equals(fieldName)) {",
                    "                deserialized.copySourceErrorCode = reader.getString();",
                    "            } else if (\"copySourceErrorMessage\".equals(fieldName)) {",
                    "                deserialized.copySourceErrorMessage = reader.getString();",
                    "            } else if (\"headerName\".equals(fieldName)) {",
                    "                deserialized.headerName = reader.getString();",
                    "            } else if (\"headerValue\".equals(fieldName)) {",
                    "                deserialized.headerValue = reader.getString();",
                    "            } else if (\"queryParameterName\".equals(fieldName)) {",
                    "                deserialized.queryParameterName = reader.getString();",
                    "            } else if (\"queryParameterValue\".equals(fieldName)) {",
                    "                deserialized.queryParameterValue = reader.getString();",
                    "            } else if (\"reason\".equals(fieldName)) {",
                    "                deserialized.reason = reader.getString();",
                    "            } else if (\"extendedErrorDetail\".equals(fieldName)) {",
                    "                deserialized.extendedErrorDetail = reader.getString();",
                    "            } else {",
                    "                reader.skipChildren();",
                    "            }",
                    "        }",
                    "        return deserialized;",
                    "    });",
                    "}")));
                logger.info("Restored JSON deserialization on ShareStorageError");
            }));
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
