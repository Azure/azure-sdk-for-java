// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

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
        // Follow-up stages (ported from the queue customization) build on this removal pass:
        //   - fixXmlSerializerRedundantCast (generated XmlSerializer -Werror cast)
        //   - retargetServiceVersionReferences (impl -> hand-written BlobServiceVersion)
        //   - restoreFluentModels (public models that shipped @Fluent regenerate as @Immutable)
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
}
