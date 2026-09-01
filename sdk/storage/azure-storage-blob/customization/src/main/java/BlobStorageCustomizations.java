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
        "BlobClient", "BlobAsyncClient",
        "ContainerClient", "ContainerAsyncClient",
        "ServiceClient", "ServiceAsyncClient",
        "AppendBlobClient", "AppendBlobAsyncClient",
        "BlockBlobClient", "BlockBlobAsyncClient",
        "PageBlobClient", "PageBlobAsyncClient",
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
        removeGeneratedFiles(editor, logger);
        addSdkOnlyIsPrefix(customization.getPackage(IMPL_PACKAGE + ".models"), logger);
        restoreFluentModels(customization, logger);
        exposeSinglePageAccessors(customization.getPackage(IMPL_PACKAGE), logger);
        // Follow-up stages (ported from the queue customization) build on this removal pass:
        //   - fixXmlSerializerRedundantCast (generated XmlSerializer -Werror cast)
        //   - retargetServiceVersionReferences (impl -> hand-written BlobServiceVersion)
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
        "QuerySerialization", "BlobTag", "BlobTags", "BlobName", "BlobHierarchyListSegment");

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

    // The hand-written BlobServiceClient/BlobContainerClient build their own PagedFlux/PagedIterable so they can
    // map the generated XML segment responses onto the shipped public item types and apply blob-specific paging
    // behaviour. The emitter already generates exactly the single-page helpers they need, but keeps them private
    // behind its own PagedFlux convenience methods, so widen them to public rather than injecting duplicates.
    private static void exposeSinglePageAccessors(PackageCustomization implPackage, Logger logger) {
        exposeSinglePageAccessors(implPackage, "ServicesImpl",
            Arrays.asList("listContainersSegmentSinglePageAsync", "listContainersSegmentSinglePage",
                "filterBlobsSinglePageAsync", "filterBlobsSinglePage"),
            logger);
        exposeSinglePageAccessors(implPackage, "ContainersImpl",
            Arrays.asList("filterBlobsSinglePageAsync", "filterBlobsSinglePage",
                "listBlobFlatSegmentSinglePageAsync", "listBlobFlatSegmentSinglePage",
                "listBlobHierarchySegmentSinglePageAsync", "listBlobHierarchySegmentSinglePage"),
            logger);
    }

    private static void exposeSinglePageAccessors(PackageCustomization implPackage, String className,
        List<String> methodNames, Logger logger) {
        if (implPackage.getClass(className) == null) {
            logger.info("{} not present; skipping single-page accessor exposure.", className);
            return;
        }
        implPackage.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
            for (String methodName : methodNames) {
                List<MethodDeclaration> methods = clazz.getMethodsByName(methodName);
                if (methods.isEmpty()) {
                    logger.info("{}.{} not found; skipping.", className, methodName);
                    continue;
                }
                for (MethodDeclaration method : methods) {
                    method.setPrivate(false);
                    method.setPublic(true);
                }
                logger.info("Exposed {}.{} as public.", className, methodName);
            }
        }));
    }
}
