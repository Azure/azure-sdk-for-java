// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure KeyVault's Secrets swagger code generation.
 */
public class SecretsCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        // Remove unnecessary files.
        removeFiles(libraryCustomization.getRawEditor());
        // Rename KeyVaultServiceVersion to SecretServiceVersion.
        libraryCustomization.getPackage("com.azure.v2.security.keyvault.secrets")
            .getClass("KeyVaultServiceVersion")
            .rename("SecretServiceVersion");
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to SecretServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretClientBuilder.java");
    }
}
