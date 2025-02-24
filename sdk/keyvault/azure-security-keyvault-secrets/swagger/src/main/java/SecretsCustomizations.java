// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
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
        // Customize the SecretClientImpl class.
        customizeClientImpl(libraryCustomization);
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to SecretServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretClientBuilder.java");
    }

    private static void customizeClientImpl(LibraryCustomization libraryCustomization) {
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.secrets.implementation")
            .getClass("SecretClientImpl");
        String classPath = "src/main/java/com/azure/security/keyvault/secrets/implementation/SecretClientImpl.java";
        Editor editor = classCustomization.getEditor();
        String newFileContent = editor.getFileContent(classPath).replace("KeyVault", "Secret");

        editor.replaceFile(classPath, newFileContent);
    }
}
