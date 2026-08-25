// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * TypeSpec customization for azure-storage-blob.
 * <p>
 * Minimal stub for the first measurement regen. The full customization (client removal, fluent-model
 * restoration, exception mapping, metadata header collections) is built out stage-by-stage once the raw
 * generated output is measured against the shipped public API.
 */
public class BlobStorageCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        logger.info("azure-storage-blob TypeSpec customization: stub (measurement pass, no transforms applied)");
    }
}
