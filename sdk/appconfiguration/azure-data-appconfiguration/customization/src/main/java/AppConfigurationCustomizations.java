// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Removes generated public client surface so the hand-written ConfigurationClient,
 * ConfigurationAsyncClient, ConfigurationClientBuilder, and ConfigurationServiceVersion
 * in com.azure.data.appconfiguration are not overwritten by tsp-client update.
 * The generated implementation client under com.azure.data.appconfiguration.implementation
 * is preserved and used by the hand-written public classes.
 */
public class AppConfigurationCustomizations extends Customization {

    private static final String ROOT_FILE_PATH = "src/main/java/com/azure/data/appconfiguration/";

    private static final String[] FILES_TO_REMOVE = new String[] {
        "AzureAppConfigurationClient.java",
        "AzureAppConfigurationAsyncClient.java",
        "AzureAppConfigurationBuilder.java",
        "AzureAppConfigurationServiceVersion.java"
    };

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        for (String fileName : FILES_TO_REMOVE) {
            String path = ROOT_FILE_PATH + fileName;
            if (editor.getContents().containsKey(path)) {
                editor.removeFile(path);
                logger.info("Removed generated file {}", path);
            } else {
                logger.info("Generated file {} not present; skipping removal.", path);
            }
        }
    }
}
