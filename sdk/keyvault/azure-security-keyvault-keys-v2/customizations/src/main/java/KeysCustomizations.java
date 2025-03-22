// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure KeyVault's Keys swagger code generation.
 */
public class KeysCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        // Remove unnecessary files.
        removeFiles(libraryCustomization.getRawEditor());

        // Rename KeyVaultServiceVersion to KeyServiceVersion.
        libraryCustomization.getPackage("com.azure.v2.security.keyvault.keys")
            .getClass("KeyVaultServiceVersion")
            .rename("KeyServiceVersion");
    }

    private static void removeFiles(Editor editor) {
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyClientBuilder.java");
    }
}
