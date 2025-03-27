// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure KeyVault's Certificates swagger code generation.
 */
public class CertificatesCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        Editor rawEditor = libraryCustomization.getRawEditor();

        // Remove unnecessary files.
        removeFiles(rawEditor);

        customizeError(libraryCustomization);
        customizeClientImpl(libraryCustomization);
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to CertificateServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateClientBuilder.java");
    }

    private static void customizeError(LibraryCustomization libraryCustomization) {
        // Rename error class.
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.certificates.implementation.models")
            .getClass("KeyVaultErrorError")
            .rename("CertificateOperationError")
            .customizeAst(ast ->
                ast.getPackageDeclaration().ifPresent(packageDeclaration ->
                    packageDeclaration.setName("com.azure.security.keyvault.certificates.models")));

        String classPath = "src/main/java/com/azure/security/keyvault/certificates/implementation/models/"
            + "CertificateOperationError.java";

        replaceInFile(classCustomization, classPath,
            new String[] { "KeyVaultErrorError" },
            new String[] { "CertificateOperationError" });

        // Move it to public package.
        libraryCustomization
            .getRawEditor()
            .renameFile(classPath,
                "src/main/java/com/azure/security/keyvault/certificates/models/CertificateOperationError.java");

        // Replace instances in impl CertificateOperationError and add import statement.
        classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.certificates.implementation.models")
            .getClass("CertificateOperation")
            .addImports("com.azure.security.keyvault.certificates.models.CertificateOperationError");
        classPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/models/CertificateOperation.java";

        replaceInFile(classCustomization, classPath,
            new String[] { "KeyVaultErrorError" },
            new String[] { "CertificateOperationError" });
    }

    private static void customizeClientImpl(LibraryCustomization libraryCustomization) {
        // Rename the class.
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.certificates.implementation")
            .getClass("CertificateClientImpl");
        String classPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientImpl.java";

        // Rename class references and add imports.
        replaceInFile(classCustomization, classPath, new String[] { "KeyVault" }, new String[] { "Certificate" });
    }

    /**
     * This method replaces all the provided strings in the specified file with new strings provided in the latter half
     * of the 'strings' parameter.
     *
     * @param classCustomization The class customization to use to edit the file.
     * @param classPath The path to the file to edit.
     * @param stringsToReplace The strings to replace.
     * @param replacementStrings The strings to replace with.
     */
    private static void replaceInFile(ClassCustomization classCustomization, String classPath,
                                      String[] stringsToReplace, String[] replacementStrings) {

        if (stringsToReplace != null && replacementStrings != null) {
            Editor editor = classCustomization.getEditor();
            String fileContent = editor.getFileContent(classPath);

            // Ensure names has an even length.
            if (stringsToReplace.length != replacementStrings.length) {
                throw new IllegalArgumentException(
                    "'stringsToReplace' must have the same number of elements as 'replacementStrings'.");
            }

            for (int i = 0; i < stringsToReplace.length; i++) {
                fileContent = fileContent.replace(stringsToReplace[i], replacementStrings[i]);
            }

            editor.replaceFile(classPath, fileContent);
        } else if (stringsToReplace != null || replacementStrings != null) {
            throw new IllegalArgumentException(
                "'stringsToReplace' must have the same number of elements as 'replacementStrings'.");
        }
    }
}
