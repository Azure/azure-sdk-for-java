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
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
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
        "ShareSignedIdentifier", "UserDelegationKey", "ShareFileDownloadHeaders");

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

    private static final List<String> GENERATED_DESCRIPTOR_FILES_TO_REMOVE = Arrays.asList(
        "src/main/java/com/azure/storage/file/share/package-info.java",
        "src/main/java/com/azure/storage/file/share/models/package-info.java",
        "src/main/java/com/azure/storage/file/share/implementation/package-info.java");

    // Generated implementation classes typed to the generated FileServiceVersion enum, which is deleted (above) in
    // favor of the hand-written public ShareServiceVersion. These are retyped to ShareServiceVersion after generation.
    private static final List<String> IMPLS_USING_SERVICE_VERSION
        = Arrays.asList("AzureFileStorageImpl", "DirectoriesImpl", "FilesImpl", "ServicesImpl", "SharesImpl");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        removeGeneratedConvenienceClients(customization, logger);

        relocateConvenienceClientsToImplementation(customization, logger);

        retypeServiceVersionToShareServiceVersion(customization, logger);

        exposeRawListSharesSegment(customization, logger);

        exposeRawListHandles(customization, logger);

        addProxyReuseForResourceScoping(customization, logger);

        fixMetadataHeaderSerialization(customization, logger);

        fixXmlSerializerRedundantCast(customization, logger);

        relocateDownloadHeadersToModels(customization, logger);

        restoreFluentModels(customization, logger);

        renameDownloadHeaderMethods(customization, logger);

        customization.getClass("com.azure.storage.file.share.models", "ShareTokenIntent")
            .customizeAst(ast -> ast.getClassByName("ShareTokenIntent").ifPresent(clazz -> clazz.setJavadocComment(
                "The request intent specifies requests that are intended for backup/admin type operations, meaning "
                    + "that all file/directory ACLs are bypassed and full permissions are granted. User must also have "
                    + "required RBAC permission.")));

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
     * Relocates the generated {@code implementation.models.FilesDownloadHeaders} response-header model into the public
     * {@code models} package as {@code ShareFileDownloadHeaders}, the shipped public type for the File download
     * operation. The synthesized response-header model cannot be named or placed via spec decorators, so the move and
     * rename are done here. Two members that shipped on the hand-written wrapper but are not modeled on the response
     * headers -- {@code errorCode} and {@code contentType} -- are added back. References in the relocated convenience
     * clients ({@code ShareFileClientInternal}/{@code ShareFileAsyncClientInternal}) are repointed to the new type.
     * Runs before {@code restoreFluentModels}/{@code renameDownloadHeaderMethods}.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void relocateDownloadHeadersToModels(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        String oldPath = PKG_ROOT + "implementation/models/FilesDownloadHeaders.java";
        String content = editor.getFileContent(oldPath);
        content = content.replace("package com.azure.storage.file.share.implementation.models;",
            "package com.azure.storage.file.share.models;");
        content = content.replaceAll("\\bFilesDownloadHeaders\\b", "ShareFileDownloadHeaders");
        // Add the errorCode and contentType members that shipped on the public headers type but are not part of the
        // generated response-header model. restoreFluentModels (run next) skips fields that already have a setter.
        String extraMembers = "\n"
            + "    private String errorCode;\n\n"
            + "    /**\n     * Get the errorCode property.\n     *\n     * @return the errorCode value.\n     */\n"
            + "    public String getErrorCode() {\n        return this.errorCode;\n    }\n\n"
            + "    /**\n     * Set the errorCode property.\n     *\n     * @param errorCode the errorCode value to set.\n"
            + "     * @return the ShareFileDownloadHeaders object itself.\n     */\n"
            + "    public ShareFileDownloadHeaders setErrorCode(String errorCode) {\n"
            + "        this.errorCode = errorCode;\n        return this;\n    }\n\n"
            + "    private String contentType;\n\n"
            + "    /**\n     * Get the contentType property.\n     *\n     * @return the contentType value.\n     */\n"
            + "    public String getContentType() {\n        return this.contentType;\n    }\n\n"
            + "    /**\n     * Set the contentType property.\n     *\n     * @param contentType the contentType value to set.\n"
            + "     * @return the ShareFileDownloadHeaders object itself.\n     */\n"
            + "    public ShareFileDownloadHeaders setContentType(String contentType) {\n"
            + "        this.contentType = contentType;\n        return this;\n    }\n";
        int lastBrace = content.lastIndexOf('}');
        content = content.substring(0, lastBrace) + extraMembers + "}\n";
        editor.removeFile(oldPath);
        // Remove the hand-written wrapper (now replaced by this relocated model) and its now-dead constructor proxy.
        editor.removeFile(PKG_ROOT + "models/ShareFileDownloadHeaders.java");
        editor.removeFile(PKG_ROOT + "implementation/accesshelpers/ShareFileDownloadHeadersConstructorProxy.java");
        editor.addFile(PKG_ROOT + "models/ShareFileDownloadHeaders.java", content);
        logger.info("Relocated FilesDownloadHeaders -> models.ShareFileDownloadHeaders");

        for (String clientName : Arrays.asList("ShareFileClientInternal", "ShareFileAsyncClientInternal")) {
            String clientPath = PKG_ROOT + "implementation/" + clientName + ".java";
            String c = editor.getFileContent(clientPath);
            c = c.replace("com.azure.storage.file.share.implementation.models.FilesDownloadHeaders",
                "com.azure.storage.file.share.models.ShareFileDownloadHeaders");
            c = c.replaceAll("\\bFilesDownloadHeaders\\b", "ShareFileDownloadHeaders");
            editor.removeFile(clientPath);
            editor.addFile(clientPath, c);
            logger.info("Repointed {} to models.ShareFileDownloadHeaders", clientName);
        }
    }

    /**
     * Renames the generated getters/setters on {@code ShareFileDownloadHeaders} to the shipped public names. The
     * emitter derives them from the response-header property names, which differ in casing/spelling from the historical
     * public API: {@code getEtag}->{@code getETag}, {@code getContentMD5}->{@code getContentMd5},
     * {@code getFileContentMD5}->{@code getFileContentMd5}, {@code getDate}->{@code getDateProperty} (and matching
     * setters). Runs after {@code restoreFluentModels} so the setters exist to be renamed.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void renameDownloadHeaderMethods(LibraryCustomization customization, Logger logger) {
        customization.getClass(MODELS_PACKAGE, "ShareFileDownloadHeaders")
            .customizeAst(ast -> ast.getClassByName("ShareFileDownloadHeaders").ifPresent(clazz -> {
                clazz.getMethodsByName("getEtag").forEach(m -> m.setName("getETag"));
                clazz.getMethodsByName("setEtag").forEach(m -> m.setName("setETag"));
                clazz.getMethodsByName("getContentMD5").forEach(m -> m.setName("getContentMd5"));
                clazz.getMethodsByName("setContentMD5").forEach(m -> m.setName("setContentMd5"));
                clazz.getMethodsByName("getFileContentMD5").forEach(m -> m.setName("getFileContentMd5"));
                clazz.getMethodsByName("setFileContentMD5").forEach(m -> m.setName("setFileContentMd5"));
                clazz.getMethodsByName("getDate").forEach(m -> m.setName("getDateProperty"));
                clazz.getMethodsByName("setDate").forEach(m -> m.setName("setDateProperty"));
                logger.info("Renamed ShareFileDownloadHeaders getters/setters to shipped public names");
            }));
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
     * Adds a URL-rebasing capability to the generated implementation so that resource-URL-scoped clients can reuse the
     * account-scoped client's operation proxies instead of re-creating {@code RestProxy} for every sub-client. The
     * service URL is a per-call {@code @HostParam}, so the operation proxies are URL-independent and safe to share.
     * Each sub-impl ({@code DirectoriesImpl}/{@code FilesImpl}/{@code ServicesImpl}/{@code SharesImpl}) gains a
     * package-private {@code (AzureFileStorageImpl, <Service>)} constructor plus a {@code getService()} accessor, and
     * {@code AzureFileStorageImpl} gains a public {@code withUrl(String)} plus a private rebasing constructor.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void addProxyReuseForResourceScoping(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        String implRoot = PKG_ROOT + "implementation/";

        String[][] subImpls = {
            { "DirectoriesImpl", "DirectoriesService" },
            { "FilesImpl", "FilesService" },
            { "ServicesImpl", "ServicesService" },
            { "SharesImpl", "SharesService" } };
        String ctorEnd = "        this.client = client;\n    }";
        for (String[] pair : subImpls) {
            String implName = pair[0];
            String svc = pair[1];
            String path = implRoot + implName + ".java";
            String content = editor.getFileContent(path);
            String injected = ctorEnd + "\n\n"
                + "    // Reuses an existing (URL-independent) proxy so URL-rebased clients avoid re-creating RestProxy.\n"
                + "    " + implName + "(AzureFileStorageImpl client, " + svc + " service) {\n"
                + "        this.service = service;\n"
                + "        this.client = client;\n"
                + "    }\n\n"
                + "    " + svc + " getService() {\n"
                + "        return this.service;\n"
                + "    }";
            content = content.replace(ctorEnd, injected);
            editor.replaceFile(path, content);
            logger.info("Added proxy-reuse constructor + getService() to {}", implName);
        }

        // AzureFileStorageImpl: add the private rebasing constructor + public withUrl(String).
        String afsPath = implRoot + "AzureFileStorageImpl.java";
        String afs = editor.getFileContent(afsPath);
        String sharesInit = "        this.shares = new SharesImpl(this);\n    }";
        String rebase = sharesInit + "\n\n"
            + "    // Resource-URL-scoped view that reuses this client's operation proxies. The service URL is a per-call @HostParam,\n"
            + "    // so proxies are URL-independent and safe to share; this avoids re-creating RestProxy for every sub-client.\n"
            + "    private AzureFileStorageImpl(AzureFileStorageImpl parent, String url) {\n"
            + "        this.httpPipeline = parent.httpPipeline;\n"
            + "        this.serializerAdapter = parent.serializerAdapter;\n"
            + "        this.url = url;\n"
            + "        this.fileRequestIntent = parent.fileRequestIntent;\n"
            + "        this.allowTrailingDot = parent.allowTrailingDot;\n"
            + "        this.allowSourceTrailingDot = parent.allowSourceTrailingDot;\n"
            + "        this.serviceVersion = parent.serviceVersion;\n"
            + "        this.directories = new DirectoriesImpl(this, parent.directories.getService());\n"
            + "        this.files = new FilesImpl(this, parent.files.getService());\n"
            + "        this.services = new ServicesImpl(this, parent.services.getService());\n"
            + "        this.shares = new SharesImpl(this, parent.shares.getService());\n"
            + "    }\n\n"
            + "    /**\n"
            + "     * Creates a resource-URL-scoped view of this client that shares this client's operation proxies.\n"
            + "     *\n"
            + "     * @param url the resource URL to target.\n"
            + "     * @return an AzureFileStorageImpl targeting {@code url} and reusing this client's proxies.\n"
            + "     */\n"
            + "    public AzureFileStorageImpl withUrl(String url) {\n"
            + "        return new AzureFileStorageImpl(this, url);\n"
            + "    }";
        afs = afs.replace(sharesInit, rebase);
        editor.replaceFile(afsPath, afs);
        logger.info("Added withUrl(String) + rebasing constructor to AzureFileStorageImpl");
    }

    /**
     * Corrects the metadata request-header serialization in the relocated {@code Share*Internal} convenience clients.
     * The emitter writes the {@code Record<string>} metadata as a single {@code x-ms-meta} header via
     * {@code String.valueOf(metadata)} (Java {@code Map.toString()}), instead of the per-entry
     * {@code x-ms-meta-<name>: <value>} headers the service expects. This rewrites each occurrence to emit one header
     * per map entry, preserving the behavior of the retired AutoRest client. Remove once the emitter honors the
     * {@code collectionHeaderPrefix}/{@code x-ms-meta-} client option for Java.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void fixMetadataHeaderSerialization(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        String implRoot = PKG_ROOT + "implementation/";
        String[] internalClients = { "ShareAsyncClientInternal", "ShareClientInternal",
            "ShareDirectoryAsyncClientInternal", "ShareDirectoryClientInternal", "ShareFileAsyncClientInternal",
            "ShareFileClientInternal" };
        String broken
            = "requestOptions.setHeader(HttpHeaderName.fromString(\"x-ms-meta\"), String.valueOf(metadata));";
        String fixed = "for (Map.Entry<String, String> entry : metadata.entrySet()) {\n"
            + "                requestOptions.setHeader(HttpHeaderName.fromString(\"x-ms-meta-\" + entry.getKey()),\n"
            + "                    entry.getValue());\n"
            + "            }";
        for (String name : internalClients) {
            String path = implRoot + name + ".java";
            String content = editor.getFileContent(path);
            if (content.contains(broken)) {
                editor.replaceFile(path, content.replace(broken, fixed));
                logger.info("Fixed metadata header serialization in {}", name);
            }
        }
    }

    /**
     * Relaxes the javac "redundant cast" lint for the single {@code (Class<T>)} cast the emitter generates in
     * {@code XmlSerializer.deserialize}. The current azure-core {@code TypeReference#getJavaClass()} already returns
     * {@code Class<T>}, so the cast is redundant and fails the {@code -Werror} build; this adds {@code "cast"} to the
     * method's existing {@code @SuppressWarnings}.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void fixXmlSerializerRedundantCast(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.storage.file.share.implementation", "XmlSerializer")
            .customizeAst(ast -> ast.getClassByName("XmlSerializer").ifPresent(clazz -> {
                clazz.getMethodsByName("deserialize")
                    .forEach(method -> method.getAnnotationByName("SuppressWarnings")
                        .filter(annotation -> annotation.isSingleMemberAnnotationExpr())
                        .ifPresent(annotation -> annotation.asSingleMemberAnnotationExpr()
                            .setMemberValue(new ArrayInitializerExpr(new NodeList<>(
                                new StringLiteralExpr("unchecked"), new StringLiteralExpr("cast"))))));
                logger.info("Suppressed redundant-cast warning on XmlSerializer.deserialize");
            }));
    }

    /**
     * Exposes raw {@code Response<BinaryData>} accessors for the List Shares Segment operation. The generated
     * {@code listSharesSegmentSinglePage} paging helpers deserialize only the per-item {@code ShareItemInternal}
     * elements and discard the {@code NextMarker} from the XML envelope, but the hand-written
     * {@code ShareServiceClient#listShares} manages continuation itself. These methods return the full response body so
     * the client can deserialize {@code ListSharesResponse} (items + {@code NextMarker}).
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void exposeRawListSharesSegment(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.storage.file.share.implementation", "ServicesImpl")
            .customizeAst(ast -> ast.getClassByName("ServicesImpl").ifPresent(clazz -> {
                if (!clazz.getMethodsByName("listSharesSegmentWithResponse").isEmpty()) {
                    return;
                }
                // Bodies are intentionally left without ShareStorageExceptionInternal mapping;
                // updateImplToMapInternalException (run later) wraps every class-returning method exactly once.
                clazz.addMember(StaticJavaParser.parseMethodDeclaration(
                    "public Response<BinaryData> listSharesSegmentWithResponse(RequestOptions requestOptions) {\n"
                        + "    final String accept = \"application/xml\";\n"
                        + "    return service.listSharesSegmentSync(this.client.getUrl(), this.client.getServiceVersion().getVersion(),\n"
                        + "        this.client.getFileRequestIntent(), accept, requestOptions, Context.NONE);\n"
                        + "}"));
                clazz.addMember(StaticJavaParser.parseMethodDeclaration(
                    "public Mono<Response<BinaryData>> listSharesSegmentWithResponseAsync(RequestOptions requestOptions) {\n"
                        + "    final String accept = \"application/xml\";\n"
                        + "    return FluxUtil.withContext(context -> service.listSharesSegment(this.client.getUrl(),\n"
                        + "        this.client.getServiceVersion().getVersion(), this.client.getFileRequestIntent(), accept,\n"
                        + "        requestOptions, context));\n"
                        + "}"));
                logger.info("Exposed raw listSharesSegmentWithResponse methods in ServicesImpl");
            }));
    }

    /**
     * Exposes raw {@code Response<BinaryData>} accessors for the Directory List Handles operation. Like
     * {@code listSharesSegment}, the generated paging helpers discard the {@code NextMarker} from the XML envelope,
     * but the hand-written {@code ShareDirectoryClient#listHandles} manages continuation itself, so these methods
     * return the full response body for the client to deserialize {@code ListHandlesResponse}.
     *
     * @param customization The library customization.
     * @param logger The logger.
     */
    private static void exposeRawListHandles(LibraryCustomization customization, Logger logger) {
        for (String implName : Arrays.asList("DirectoriesImpl", "FilesImpl")) {
            customization.getClass("com.azure.storage.file.share.implementation", implName)
                .customizeAst(ast -> ast.getClassByName(implName).ifPresent(clazz -> {
                    if (!clazz.getMethodsByName("listHandlesWithResponse").isEmpty()) {
                        return;
                    }
                    // Bodies are intentionally left without ShareStorageExceptionInternal mapping;
                    // updateImplToMapInternalException (run later) wraps every class-returning method exactly once.
                    clazz.addMember(StaticJavaParser.parseMethodDeclaration(
                        "public Response<BinaryData> listHandlesWithResponse(RequestOptions requestOptions) {\n"
                            + "    final String accept = \"application/xml\";\n"
                            + "    return service.listHandlesSync(this.client.getUrl(), this.client.getServiceVersion().getVersion(),\n"
                            + "        this.client.isAllowTrailingDot(), this.client.getFileRequestIntent(), accept, requestOptions,\n"
                            + "        Context.NONE);\n"
                            + "}"));
                    clazz.addMember(StaticJavaParser.parseMethodDeclaration(
                        "public Mono<Response<BinaryData>> listHandlesWithResponseAsync(RequestOptions requestOptions) {\n"
                            + "    final String accept = \"application/xml\";\n"
                            + "    return FluxUtil.withContext(context -> service.listHandles(this.client.getUrl(),\n"
                            + "        this.client.getServiceVersion().getVersion(), this.client.isAllowTrailingDot(),\n"
                            + "        this.client.getFileRequestIntent(), accept, requestOptions, context));\n"
                            + "}"));
                    logger.info("Exposed raw listHandlesWithResponse methods in {}", implName);
                }));
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
                        // Header models back RFC1123 date headers with a DateTimeRfc1123 field but expose an
                        // OffsetDateTime getter; the public setter must take OffsetDateTime and wrap, otherwise the
                        // shipped setter signature changes (breaking) and leaks the internal DateTimeRfc1123 type.
                        if ("DateTimeRfc1123".equals(variable.getTypeAsString())) {
                            ast.addImport("java.time.OffsetDateTime");
                            ast.addImport("com.azure.core.util.DateTimeRfc1123");
                            setter.addParameter("OffsetDateTime", fieldName);
                            setter.setBody(StaticJavaParser.parseBlock("{ this." + fieldName + " = " + fieldName
                                + " == null ? null : new DateTimeRfc1123(" + fieldName + "); return this; }"));
                        } else {
                            setter.addParameter(variable.getTypeAsString(), fieldName);
                            setter.setBody(StaticJavaParser.parseBlock(
                                "{ this." + fieldName + " = " + fieldName + "; return this; }"));
                        }
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
