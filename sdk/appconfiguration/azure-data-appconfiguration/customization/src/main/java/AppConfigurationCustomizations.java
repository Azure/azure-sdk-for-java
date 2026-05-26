// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Removes generated public client surface so the hand-written ConfigurationClient,
 * ConfigurationAsyncClient, ConfigurationClientBuilder, and ConfigurationServiceVersion
 * in com.azure.data.appconfiguration are not overwritten by tsp-client update.
 * The generated implementation client under com.azure.data.appconfiguration.implementation
 * is preserved and used by the hand-written public classes.
 */
public class AppConfigurationCustomizations extends Customization {

    private static final String ROOT_PACKAGE = "com.azure.data.appconfiguration";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization rootPackage = customization.getPackage(ROOT_PACKAGE);
        removeIfPresent(rootPackage, "ConfigurationClient", logger);
        removeIfPresent(rootPackage, "ConfigurationAsyncClient", logger);
        removeIfPresent(rootPackage, "ConfigurationClientBuilder", logger);
        removeIfPresent(rootPackage, "ConfigurationServiceVersion", logger);
    }

    private static void removeIfPresent(PackageCustomization pkg, String className, Logger logger) {
        try {
            pkg.getClass(className).remove();
            logger.info("Removed generated class {}.{}", pkg.getPackageName(), className);
        } catch (Exception ex) {
            logger.info("Generated class {}.{} not present; skipping removal.", pkg.getPackageName(), className);
        }
    }
}
