// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
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
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Customization class for Blob Storage.
 */
public class BlobStorageCustomizations extends Customization {
    private static final String PKG_ROOT = "src/main/java/com/azure/storage/blob/";

    private static final String MODELS_PACKAGE = "com.azure.storage.blob.models";

    private static final String IMPL_PACKAGE = "com.azure.storage.blob.implementation";

    // Models that shipped as @Fluent (public no-arg ctor + setters) before the TypeSpec migration. With
    // required-fields-as-ctor-args:true these regenerate as @Immutable with a required-args ctor and no setters -- a
    // breaking change. restoreFluentModels restores the shipped fluent shape per-model. Models the emitter already
    // generates in the shipped shape (BlobAccessPolicy, BlockLookupList) aren't listed.
    private static final List<String> FLUENT_MODELS_TO_RESTORE = Arrays.asList(
        "UserDelegationKey", "BlobCorsRule", "BlobAnalyticsLogging", "BlobRetentionPolicy", "BlobSignedIdentifier",
        "BlobMetrics", "BlobServiceStatistics", "KeyInfo", "Block", "BlockList", "BlobContainerItemProperties",
        "BlobContainerItem", "PageList", "PageRange", "ClearRange", "GeoReplication", "StaticWebsite");

    // Internal list models the hand-written BlobItem, BlobItemProperties and Arrow listing code populate through
    // setters, as they did before the migration.
    private static final List<String> IMPL_FLUENT_MODELS_TO_RESTORE
        = Arrays.asList("BlobItemInternal", "BlobItemPropertiesInternal", "BlobName");

    // Response header classes wrapped by the public BlobDownloadHeaders and BlobQueryHeaders, whose setters delegate
    // to them. They keep their HttpHeaders constructor and only get setters back.
    private static final List<String> HEADER_MODELS_TO_MAKE_SETTABLE
        = Arrays.asList("BlobsDownloadHeaders", "BlobsQueryHeaders");

    // The generator emits three layers of code:
    //   1. implementation/*Impl: raw REST operations. Kept.
    //   2. Per-operation-group convenience clients: typed WithResponse methods over the Impl layer. Kept, but moved
    //      into the implementation package (see CONVENIENCE_CLIENTS_TO_RELOCATE).
    //   3. Top-level builder: deleted (this list).
    // The public API is the hand-written Blob* clients and builders, so layer 3 would only duplicate it. The list also
    // deletes the generated service version enum, as the hand-written BlobServiceVersion is the public one.
    private static final List<String> GENERATED_CLIENTS_TO_REMOVE
        = Arrays.asList("AzureBlobStorageBuilder", "BlobServiceVersion");

    // Per-operation-group convenience clients emitted by typespec-java. See relocateConvenienceClientsToImplementation.
    private static final List<String> CONVENIENCE_CLIENTS_TO_RELOCATE = Arrays.asList(
        "ServiceClient", "ServiceAsyncClient",
        "ContainerClient", "ContainerAsyncClient",
        "BlobClient", "BlobAsyncClient",
        "AppendBlobClient", "AppendBlobAsyncClient",
        "BlockBlobClient", "BlockBlobAsyncClient",
        "PageBlobClient", "PageBlobAsyncClient");

    // Types the generated clients import from com.azure.storage.blob.implementation; once the client itself lives
    // in that package those imports are same-package and Checkstyle rejects them as redundant.
    private static final List<String> IMPL_PACKAGE_TYPES = Arrays.asList(
        "ServicesImpl", "ContainersImpl", "BlobsImpl", "AppendBlobsImpl", "BlockBlobsImpl", "PageBlobsImpl",
        "MultipartFormDataHelper", "XmlSerializerProviders");

    // The generated operation groups that send the service requests.
    private static final List<String> OPERATION_GROUP_CLASSES = Arrays.asList(
        "ServicesImpl", "ContainersImpl", "BlobsImpl", "AppendBlobsImpl", "BlockBlobsImpl", "PageBlobsImpl");

    private static final String MODULE_INFO_PATH = "src/main/java/module-info.java";

    // Directives the hand-written code needs that the generated module-info doesn't declare: the storage-common and
    // avro dependencies, the hand-written public packages, the implementation packages the other storage libraries
    // build on, and the implementation package azure-core reflects on.
    private static final List<String> MODULE_INFO_DIRECTIVES = Arrays.asList(
        "requires transitive com.azure.storage.common;",
        "requires com.azure.storage.internal.avro;",
        "exports com.azure.storage.blob.options;",
        "exports com.azure.storage.blob.sas;",
        "exports com.azure.storage.blob.specialized;",
        "exports com.azure.storage.blob.implementation to com.azure.storage.blob.cryptography,"
            + " com.azure.storage.blob.batch, com.azure.storage.file.datalake;",
        "exports com.azure.storage.blob.implementation.accesshelpers to com.azure.storage.file.datalake;",
        "exports com.azure.storage.blob.implementation.models to com.azure.storage.blob.batch,"
            + " com.azure.storage.blob.cryptography;",
        "exports com.azure.storage.blob.implementation.util to com.azure.storage.blob.cryptography,"
            + " com.azure.storage.file.datalake, com.azure.storage.blob.changefeed, com.azure.core,"
            + " com.azure.storage.blob.batch, com.azure.storage.blob.nio;",
        "opens com.azure.storage.blob.implementation to com.azure.core;");

    private static final List<Class<?>> DIRECTIVE_ORDER
        = Arrays.asList(ModuleRequiresDirective.class, ModuleExportsDirective.class, ModuleOpensDirective.class);

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        removeGeneratedConvenienceClients(customization, logger);

        patchModuleInfo(customization);

        relocateConvenienceClientsToImplementation(customization, logger);

        restoreFluentModels(customization, logger);

        restoreBlobStorageErrorJson(customization, logger);

        restoreShippedEnumOrder(customization.getPackage(MODELS_PACKAGE), logger);

        restoreShippedModelMembers(customization.getPackage(MODELS_PACKAGE), logger);

        allowNullBlockListType(customization.getPackage(IMPL_PACKAGE));

        sendRangesInXMsRangeHeader(customization);

        updateImplToMapInternalException(customization.getPackage(IMPL_PACKAGE));
    }

    /**
     * Deletes the generated builder and service version so the shipped public surface is limited to the hand-written
     * Blob* clients. The generated {@code implementation/*Impl} operation layer and the relocated convenience clients
     * are retained.
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
     * {@code Internal} suffix, e.g. {@code ContainerClient} becomes {@code BlobContainerClientInternal}.
     * <p>
     * These clients provide the typed {@code WithResponse} methods (for example
     * {@code ResponseBase<ContainersGetAccessPolicyHeaders, BlobSignedIdentifierWrapper> getAccessPolicyWithResponse(...)})
     * that the hand-written {@code Blob*} clients delegate to. Moving them:
     * <ul>
     * <li>keeps them out of the public API, because {@code implementation} is not exported;</li>
     * <li>avoids a name clash between the generated and hand-written {@code BlobClient}/{@code BlobAsyncClient}.</li>
     * </ul>
     * Each moved class also gets a public constructor, because the hand-written clients now call it from another
     * package, and loses its {@code @ServiceClient} annotation, because the builder it refers to is deleted. Its
     * imports of the {@code *Impl} classes become same-package, so they're removed.
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
            content = content.replace("package com.azure.storage.blob;",
                "package com.azure.storage.blob.implementation;");
            // Drop the @ServiceClient marker annotation (its builder AzureBlobStorageBuilder is deleted). The class
            // literally named ServiceClient carries the annotation fully-qualified to avoid the name clash, so match
            // both @ServiceClient(...) and @com.azure.core.annotation.ServiceClient(...).
            content = content.replaceAll("(?m)^@(com\\.azure\\.core\\.annotation\\.)?ServiceClient\\([^)]*\\)\\r?\\n", "");
            content = content.replace("import com.azure.core.annotation.ServiceClient;\n", "");
            for (String implType : IMPL_PACKAGE_TYPES) {
                content = content.replace("import " + IMPL_PACKAGE + "." + implType + ";\n", "");
            }
            // Make the package-private constructor public; callers now live in a different package.
            content = content.replaceFirst("(?m)^(\\s*)" + className + "\\(", "$1public " + className + "(");
            // Rename the class (declaration, constructor, self-references) to the Blob*-Internal name.
            content = content.replaceAll("\\b" + className + "\\b", newName);
            editor.removeFile(oldPath);
            editor.addFile(PKG_ROOT + "implementation/" + newName + ".java", content);
            logger.info("Relocated convenience client {} -> implementation/{}", className, newName);
        }
    }

    /**
     * Maps a generated convenience client name to its internal name: the hand-written {@code Blob*} naming with an
     * {@code Internal} suffix. The service and container clients take a {@code Blob} prefix, while the blob-scoped
     * ones already carry it. E.g. {@code ServiceClient} -> {@code BlobServiceClientInternal},
     * {@code ContainerAsyncClient} -> {@code BlobContainerAsyncClientInternal}, {@code PageBlobClient} ->
     * {@code PageBlobClientInternal}.
     *
     * @param generatedName The generated convenience client name.
     * @return The internal client name.
     */
    private static String internalClientName(String generatedName) {
        String prefixed = generatedName.startsWith("Service") || generatedName.startsWith("Container")
            ? "Blob" + generatedName
            : generatedName;
        return prefixed + "Internal";
    }


    /**
     * Restores the "fluent" shape of models that shipped as {@code @Fluent} (public no-arg constructor + setters)
     * before the TypeSpec migration. With {@code required-fields-as-ctor-args: true} these regenerate as
     * {@code @Immutable} with a required-args constructor and no setters -- a breaking change. Each named model is
     * converted back through the JavaParser AST: {@code @Immutable} becomes {@code @Fluent}, {@code final} is removed
     * from instance fields, a public no-arg constructor is added, a public fluent setter is added for each field, and
     * the generated required-args constructor is made private (it is not part of the shipped API; {@code fromXml},
     * which is left untouched, still uses it).
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void restoreFluentModels(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage(MODELS_PACKAGE);
        for (String modelName : FLUENT_MODELS_TO_RESTORE) {
            restoreFluentModel(models, modelName, true, logger);
        }
        PackageCustomization implModels = customization.getPackage(IMPL_PACKAGE + ".models");
        for (String modelName : IMPL_FLUENT_MODELS_TO_RESTORE) {
            restoreFluentModel(implModels, modelName, true, logger);
        }
        for (String modelName : HEADER_MODELS_TO_MAKE_SETTABLE) {
            restoreFluentModel(implModels, modelName, false, logger);
        }
    }

    private static void restoreFluentModel(PackageCustomization models, String modelName, boolean restoreConstructors,
        Logger logger) {
        models.getClass(modelName).customizeAst(ast -> {
            ast.addImport("com.azure.core.annotation.Fluent");
            ast.addImport("com.azure.core.annotation.Generated");
            ast.getImports().removeIf(i -> i.getNameAsString().equals("com.azure.core.annotation.Immutable"));
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
                if (restoreConstructors) {
                    boolean hasNoArgConstructor = clazz.getConstructors().stream()
                        .anyMatch(ctor -> ctor.getParameters().isEmpty());
                    // The emitter makes the required-args constructor public on input models; the shipped fluent models
                    // only expose the no-arg constructor, so keep it private (fromXml still uses it).
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

                }

                // Un-finalize instance fields and add a fluent setter immediately after each field's getter, so the
                // getter/setter pairs sit together as they did in the shipped model.
                clazz.getFields().stream()
                    .filter(field -> !field.isStatic())
                    .forEach(field -> {
                        field.removeModifier(Modifier.Keyword.FINAL);
                        VariableDeclarator variable = field.getVariable(0);
                        String fieldName = variable.getNameAsString();
                        // The setter is named after the getter, so isX() pairs with setX(), as in the shipped models.
                        int getterIndex = indexOfGetter(members, fieldName);
                        String getterName = getterIndex >= 0
                            ? ((MethodDeclaration) members.get(getterIndex)).getNameAsString()
                            : "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                        String setterName = "set" + getterName.substring(getterName.startsWith("is") ? 2 : 3);
                        if (!clazz.getMethodsByName(setterName).isEmpty()) {
                            return;
                        }

                        // The setter takes the getter's type. For dates the service sends in RFC 1123 format the
                        // field is a DateTimeRfc1123 while the getter returns an OffsetDateTime, so convert.
                        Type setterType = getterIndex >= 0
                            ? ((MethodDeclaration) members.get(getterIndex)).getType().clone()
                            : variable.getType().clone();
                        String value = "DateTimeRfc1123".equals(variable.getTypeAsString())
                            && !"DateTimeRfc1123".equals(setterType.asString())
                                ? fieldName + " == null ? null : new DateTimeRfc1123(" + fieldName + ")"
                                : fieldName;

                        MethodDeclaration setter = new MethodDeclaration();
                        setter.setName(setterName);
                        setter.setModifiers(Modifier.Keyword.PUBLIC);
                        setter.setType(modelName);
                        setter.addParameter(setterType, fieldName);
                        setter.setBody(StaticJavaParser.parseBlock(
                            "{ this." + fieldName + " = " + value + "; return this; }"));
                        setter.addMarkerAnnotation("Generated");
                        setter.setJavadocComment(new Javadoc(
                            JavadocDescription.parseText("Set the " + fieldName + " property."))
                            .addBlockTag("param", fieldName, "the " + fieldName + " value to set.")
                            .addBlockTag("return", "the " + modelName + " object itself."));

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

    // Constant order of the fixed enums as shipped; ordinals are part of the public contract.
    private static final Map<String, List<String>> SHIPPED_ENUM_ORDER = new LinkedHashMap<>();

    static {
        SHIPPED_ENUM_ORDER.put("CopyStatusType", Arrays.asList("PENDING", "SUCCESS", "ABORTED", "FAILED"));
        SHIPPED_ENUM_ORDER.put("DeleteSnapshotsOptionType", Arrays.asList("INCLUDE", "ONLY"));
        SHIPPED_ENUM_ORDER.put("LeaseStatusType", Arrays.asList("LOCKED", "UNLOCKED"));
        SHIPPED_ENUM_ORDER.put("SequenceNumberActionType", Arrays.asList("MAX", "UPDATE", "INCREMENT"));
    }

    /**
     * Restores the shipped constant order of the fixed enums. The emitter orders constants as the spec lists them,
     * which differs from the AutoRest order, and Java exposes the order through {@code ordinal()} and
     * {@code values()}.
     *
     * @param models The models package.
     * @param logger The logger.
     */
    private static void restoreShippedEnumOrder(PackageCustomization models, Logger logger) {
        SHIPPED_ENUM_ORDER.forEach((enumName, order) -> models.getClass(enumName)
            .customizeAst(ast -> ast.getEnumByName(enumName)
                .ifPresent(declaration -> declaration.getEntries()
                    .sort(Comparator.comparingInt(entry -> order.indexOf(entry.getNameAsString()))))));
        logger.info("Restored shipped enum constant order for {}", SHIPPED_ENUM_ORDER.keySet());
    }

    /**
     * Sends ranges in the {@code x-ms-range} header, as the shipped client did. The spec uses the standard
     * {@code Range} header, which the service treats the same, but custom pipeline policies, recorded tests and HTTP
     * intermediaries all see the header name. Renaming it where the request is built covers every pipeline, including
     * the ones Data Lake, Cryptography and callers supply.
     *
     * @param customization The library customization.
     */
    private static void sendRangesInXMsRangeHeader(LibraryCustomization customization) {
        PackageCustomization implPackage = customization.getPackage(IMPL_PACKAGE);
        Editor editor = customization.getRawEditor();
        Stream.concat(OPERATION_GROUP_CLASSES.stream(),
            CONVENIENCE_CLIENTS_TO_RELOCATE.stream().map(BlobStorageCustomizations::internalClientName))
            // Only reprint the classes that send a range, so the others keep the generator's formatting.
            .filter(className -> {
                String content = editor.getFileContent(PKG_ROOT + "implementation/" + className + ".java");
                return content.contains("@HeaderParam(\"Range\")") || content.contains("HttpHeaderName.RANGE");
            })
            .forEach(className -> implPackage.getClass(className)
                .customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
                    // @HeaderParam("Range") on the RestProxy service interface methods.
                    clazz.findAll(SingleMemberAnnotationExpr.class,
                        annotation -> "HeaderParam".equals(annotation.getNameAsString())
                            && annotation.getMemberValue().isStringLiteralExpr()
                            && "Range".equals(annotation.getMemberValue().asStringLiteralExpr().getValue()))
                        .forEach(annotation -> annotation.setMemberValue(new StringLiteralExpr("x-ms-range")));
                    // requestOptions.setHeader(HttpHeaderName.RANGE, ...) for optional ranges.
                    clazz.findAll(FieldAccessExpr.class,
                        field -> "RANGE".equals(field.getNameAsString()) && field.getScope().isNameExpr()
                            && "HttpHeaderName".equals(field.getScope().asNameExpr().getNameAsString()))
                        .forEach(field -> field.replace(new MethodCallExpr(new NameExpr("HttpHeaderName"),
                            "fromString", new NodeList<>(new StringLiteralExpr("x-ms-range")))));
                })));
    }

    /**
     * The service defaults Get Block List to committed blocks when {@code blocklisttype} is omitted, and the shipped
     * {@code listBlocks} accepted a null type for that. The spec declares the parameter required, so the generated
     * convenience methods call {@code listType.toString()}. A null type is now passed through as null, which the
     * protocol layer omits from the query, as the shipped client did.
     *
     * @param implPackage The implementation package holding the relocated convenience clients.
     */
    private static void allowNullBlockListType(PackageCustomization implPackage) {
        for (String className : Arrays.asList("BlockBlobAsyncClientInternal", "BlockBlobClientInternal")) {
            implPackage.getClass(className)
                .customizeAst(ast -> ast.getClassByName(className)
                    .ifPresent(clazz -> clazz.getMethods()
                        .stream()
                        .filter(method -> method.getNameAsString().startsWith("getBlockList"))
                        .forEach(method -> method
                            .findAll(MethodCallExpr.class, call -> "toString".equals(call.getNameAsString())
                                && call.getScope().filter(scope -> scope.isNameExpr()
                                    && "listType".equals(scope.asNameExpr().getNameAsString())).isPresent())
                            .forEach(call -> call.replace(new ConditionalExpr(
                                new BinaryExpr(new NameExpr("listType"), new NullLiteralExpr(),
                                    BinaryExpr.Operator.EQUALS),
                                new NullLiteralExpr(), call.clone()))))));
        }
    }

    /**
     * Restores public model members the AutoRest layer shipped that the spec doesn't produce:
     * <ul>
     * <li>{@code Block}'s deprecated {@code int} size accessors, alongside the {@code long} ones;</li>
     * <li>{@code BlobContainerItemProperties}' primitive {@code boolean} accessors for the encryption scope override,
     * which treat an absent value as {@code false};</li>
     * <li>two deprecated, misspelled {@code BlobErrorCode} constants kept for compatibility;</li>
     * <li>{@code PageList}'s continuation marker, which stays private and is reached by the SDK through
     * {@code PageListHelper}.</li>
     * </ul>
     *
     * @param models The models package.
     * @param logger The logger.
     */
    private static void restoreShippedModelMembers(PackageCustomization models, Logger logger) {
        models.getClass("Block").customizeAst(ast -> ast.getClassByName("Block").ifPresent(clazz -> {
            MethodDeclaration getSize = clazz.addMethod("getSize", Modifier.Keyword.PUBLIC);
            getSize.setType(PrimitiveType.intType());
            getSize.setBody(StaticJavaParser.parseBlock("{ return (int) this.sizeLong; }"));
            getSize.addMarkerAnnotation("Deprecated");
            getSize.setJavadocComment(
                new Javadoc(JavadocDescription.parseText("Get the size property: The block size in bytes."))
                    .addBlockTag("return", "the size value.")
                    .addBlockTag("deprecated", "Use {@link #getSizeLong()}"));

            MethodDeclaration setSize = clazz.addMethod("setSize", Modifier.Keyword.PUBLIC);
            setSize.setType("Block");
            setSize.addParameter(PrimitiveType.intType(), "size");
            setSize.setBody(StaticJavaParser.parseBlock("{ return this.setSizeLong(size); }"));
            setSize.addMarkerAnnotation("Deprecated");
            setSize.setJavadocComment(
                new Javadoc(JavadocDescription.parseText("Set the size property: The block size in bytes."))
                    .addBlockTag("param", "size", "the size value to set.")
                    .addBlockTag("return", "the Block object itself.")
                    .addBlockTag("deprecated", "Use {@link #setSizeLong(long)}"));
        }));

        models.getClass("BlobContainerItemProperties")
            .customizeAst(ast -> ast.getClassByName("BlobContainerItemProperties").ifPresent(clazz -> {
                clazz.getMethodsByName("isEncryptionScopeOverridePrevented").forEach(getter -> getter
                    .setType(PrimitiveType.booleanType())
                    .setBody(StaticJavaParser.parseBlock(
                        "{ return Boolean.TRUE.equals(this.encryptionScopeOverridePrevented); }")));
                clazz.getMethodsByName("setEncryptionScopeOverridePrevented")
                    .forEach(setter -> setter.getParameter(0).setType(PrimitiveType.booleanType()));
            }));

        models.getClass("BlobErrorCode").customizeAst(ast -> ast.getClassByName("BlobErrorCode").ifPresent(clazz -> {
            addDeprecatedErrorCode(clazz, "SNAPHOT_OPERATION_RATE_EXCEEDED", "SnapshotOperationRateExceeded",
                "SNAPSHOT_OPERATION_RATE_EXCEEDED");
            addDeprecatedErrorCode(clazz, "INCREMENTAL_COPY_OF_ERALIER_VERSION_SNAPSHOT_NOT_ALLOWED",
                "IncrementalCopyOfEralierVersionSnapshotNotAllowed", "INCREMENTAL_COPY_OF_EARLIER_SNAPSHOT_NOT_ALLOWED");
        }));

        models.getClass("PageList").customizeAst(ast -> ast.getClassByName("PageList").ifPresent(clazz -> {
            ast.addImport("com.azure.storage.blob.implementation.models.PageListHelper");
            clazz.getMethodsByName("getNextMarker").forEach(method -> method.setModifiers(Modifier.Keyword.PRIVATE));
            clazz.getMethodsByName("setNextMarker").forEach(method -> method.setModifiers(Modifier.Keyword.PRIVATE));

            // static { PageListHelper.setAccessor(new PageListHelper.PageListAccessor() { ... }); }
            MethodDeclaration getNextMarker = new MethodDeclaration();
            getNextMarker.setName("getNextMarker");
            getNextMarker.setModifiers(Modifier.Keyword.PUBLIC);
            getNextMarker.setType("String");
            getNextMarker.addParameter("PageList", "pageList");
            getNextMarker.setBody(StaticJavaParser.parseBlock("{ return pageList.getNextMarker(); }"));
            getNextMarker.addMarkerAnnotation("Override");

            MethodDeclaration setNextMarker = new MethodDeclaration();
            setNextMarker.setName("setNextMarker");
            setNextMarker.setModifiers(Modifier.Keyword.PUBLIC);
            setNextMarker.setType("PageList");
            setNextMarker.addParameter("PageList", "pageList");
            setNextMarker.addParameter("String", "marker");
            setNextMarker.setBody(StaticJavaParser.parseBlock("{ return pageList.setNextMarker(marker); }"));
            setNextMarker.addMarkerAnnotation("Override");

            ObjectCreationExpr accessor = new ObjectCreationExpr(null,
                StaticJavaParser.parseClassOrInterfaceType("PageListHelper.PageListAccessor"), new NodeList<>());
            accessor.setAnonymousClassBody(new NodeList<>(getNextMarker, setNextMarker));
            BlockStmt setAccessor = new BlockStmt()
                .addStatement(new MethodCallExpr(new NameExpr("PageListHelper"), "setAccessor", new NodeList<>(accessor)));
            clazz.getMembers().addFirst(new InitializerDeclaration(true, setAccessor));
        }));
        logger.info("Restored shipped members of Block, BlobContainerItemProperties, BlobErrorCode and PageList");
    }

    /**
     * Adds a deprecated {@code BlobErrorCode} constant after the constant that replaces it.
     *
     * @param clazz The BlobErrorCode class.
     * @param constant The deprecated constant name.
     * @param wireValue The error code the constant wraps.
     * @param replacement The constant to use instead.
     */
    private static void addDeprecatedErrorCode(ClassOrInterfaceDeclaration clazz, String constant, String wireValue,
        String replacement) {
        if (clazz.getFieldByName(constant).isPresent()) {
            return;
        }
        FieldDeclaration field = new FieldDeclaration(
            new NodeList<>(Modifier.publicModifier(), Modifier.staticModifier(), Modifier.finalModifier()),
            new VariableDeclarator(new ClassOrInterfaceType(null, "BlobErrorCode"), constant,
                new MethodCallExpr("fromString", new StringLiteralExpr(wireValue))));
        field.addMarkerAnnotation("Deprecated");
        field.setJavadocComment(
            new Javadoc(JavadocDescription.parseText("Static value " + wireValue + " for BlobErrorCode."))
                .addBlockTag("deprecated", "Please use {@link BlobErrorCode#" + replacement + "}"));

        NodeList<BodyDeclaration<?>> members = clazz.getMembers();
        int replacementIndex = clazz.getFieldByName(replacement).map(members::indexOf).orElse(members.size() - 1);
        members.add(replacementIndex + 1, field);
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
                if (name.equals(getterName) || name.equals(booleanGetterName) || name.equals(fieldName)) {
                    return i;
                }
            }
        }
        return -1;
    }


    /**
     * Restores JSON deserialization on the internal {@code BlobStorageError}. The emitter generates a model as either
     * {@code XmlSerializable} or {@code JsonSerializable}, never both, so the XML-only error model would fall back to
     * azure-core's reflective Jackson deserializer for a JSON error body. On the module path Jackson cannot access
     * {@code implementation.models} (it is opened only to {@code com.azure.core}), so the error would surface as a
     * plain {@code HttpResponseException}/{@code RuntimeException} instead of {@code BlobStorageException}. The
     * pre-migration model implemented both interfaces; this restores that, accepting both the bare and the
     * {@code {"error": {...}}}-wrapped forms. Each field is read and written under its own (camel case) name, as the
     * pre-migration model did, so fields the spec adds are picked up automatically.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void restoreBlobStorageErrorJson(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.storage.blob.implementation.models", "BlobStorageError")
            .customizeAst(ast -> ast.getClassByName("BlobStorageError").ifPresent(clazz -> {
                if (!clazz.getMethodsByName("fromJson").isEmpty()) {
                    return;
                }
                ast.addImport("com.azure.json.JsonReader");
                ast.addImport("com.azure.json.JsonSerializable");
                ast.addImport("com.azure.json.JsonToken");
                ast.addImport("com.azure.json.JsonWriter");
                ast.addImport("java.io.IOException");
                clazz.addImplementedType("JsonSerializable<BlobStorageError>");

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
                fromJson.setType("BlobStorageError");
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
                    "                return readBlobStorageError(bufferedReader);",
                    "            }",
                    "            bufferedReader.skipChildren();",
                    "        }",
                    "        return readBlobStorageError(bufferedReader.reset());",
                    "    });",
                    "}")));
                fromJson.setJavadocComment(new Javadoc(
                    JavadocDescription.parseText("Reads an instance of BlobStorageError from the JsonReader."))
                    .addBlockTag("param", "jsonReader", "The JsonReader being read.")
                    .addBlockTag("return", "An instance of BlobStorageError if the JsonReader was pointing to an "
                        + "instance of it, or null if it was pointing to JSON null.")
                    .addBlockTag("throws", "IOException", "If an error occurs while reading the BlobStorageError."));

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
                    "    BlobStorageError deserialized = new BlobStorageError();",
                    "    while (reader.nextToken() != JsonToken.END_OBJECT) {",
                    "        String fieldName = reader.getFieldName();",
                    "        reader.nextToken();",
                    "    }",
                    "    return deserialized;",
                    "}"));
                readObject.findFirst(WhileStmt.class).get().getBody().asBlockStmt().addStatement(readField);
                MethodDeclaration readBlobStorageError = clazz.addMethod("readBlobStorageError",
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
                readBlobStorageError.setType("BlobStorageError");
                readBlobStorageError.addParameter("JsonReader", "jsonReader");
                readBlobStorageError.addThrownException(StaticJavaParser.parseClassOrInterfaceType("IOException"));
                readBlobStorageError.setBody(new BlockStmt().addStatement(new ReturnStmt(new MethodCallExpr(
                    new NameExpr("jsonReader"), "readObject",
                    new NodeList<>(new LambdaExpr(new Parameter(new UnknownType(), "reader"), readObject))))));
                logger.info("Restored JSON deserialization on BlobStorageError");
            }));
    }

    // Writes a BlobStorageError field: strings and numbers directly, and expandable string enums by their value.
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

    // Reads a BlobStorageError field, the counterpart of jsonWriteStatement.
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
     * {@code .onErrorMap(BlobStorageErrorException.class, ModelHelper::mapToBlobStorageException)} to handle
     * mapping BlobStorageErrorException to BlobStorageException.
     * - For synchronous methods, wrap the return statement in a try-catch block that catches
     * BlobStorageErrorException and rethrows {@code ModelHelper.mapToBlobStorageException(e)}. Or, for void
     * methods wrap the last statement.
     *
     * @param implPackage The implementation package.
     */
    private static void updateImplToMapInternalException(PackageCustomization implPackage) {
        for (String implToUpdate : OPERATION_GROUP_CLASSES) {
            implPackage.getClass(implToUpdate).customizeAst(ast -> {
                ast.addImport("com.azure.storage.blob.implementation.util.ModelHelper");
                ast.addImport("com.azure.storage.blob.models.BlobStorageException");
                ast.addImport("com.azure.storage.blob.implementation.models.BlobStorageErrorException");
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
        ReturnStmt returnStmt = body.getStatement(body.getStatements().size() - 1).asReturnStmt();
        // Map errors on the service call itself, ahead of any .map(...): onErrorMap returns the publisher's own type,
        // so placing it after a map that builds, say, a PagedResponseBase breaks the generic inference.
        Expression serviceCall = returnStmt.getExpression().get();
        while (serviceCall.isMethodCallExpr() && serviceCall.asMethodCallExpr().getScope()
            .filter(Expression::isMethodCallExpr)
            .isPresent()) {
            serviceCall = serviceCall.asMethodCallExpr().getScope().get();
        }
        MethodCallExpr onErrorMap = StaticJavaParser
            .parseExpression("x.onErrorMap(BlobStorageErrorException.class, ModelHelper::mapToBlobStorageException)")
            .asMethodCallExpr();
        if (serviceCall == returnStmt.getExpression().get()) {
            onErrorMap.setScope(serviceCall);
            returnStmt.setExpression(onErrorMap);
        } else {
            serviceCall.replace(onErrorMap);
            onErrorMap.setScope(serviceCall);
        }
    }

    private static void addErrorMappingToSyncMethod(MethodDeclaration method) {
        // Turn the entire method into a BlockStmt that will be used as the try block.
        BlockStmt tryBlock = method.getBody().get();
        BlockStmt catchBlock = new BlockStmt(new NodeList<>(StaticJavaParser.parseStatement(
            "throw ModelHelper.mapToBlobStorageException(internalException);")));
        Parameter catchParameter = new Parameter().setType("BlobStorageErrorException")
            .setName("internalException");
        CatchClause catchClause = new CatchClause(catchParameter, catchBlock);
        TryStmt tryCatchMap = new TryStmt(tryBlock, new NodeList<>(catchClause), null);

        // Replace the last statement with the try-catch block.
        method.setBody(new BlockStmt(new NodeList<>(tryCatchMap)));
    }
}
