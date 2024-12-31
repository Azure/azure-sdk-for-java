// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
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

        // Customize the CertificateClientImpl class.
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.certificates.implementation")
            .getClass("CertificateClientImpl");
        String classPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientImpl.java";

        replaceInFile(classCustomization, classPath, "KeyVault", "Certificate");
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to CertificateServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateClientBuilder.java");
    }

    /**
     * This method replaces all the provided strings in the specified file with new strings provided in the latter half
     * of the 'strings' parameter.
     *
     * @param classCustomization The class customization to use to edit the file.
     * @param classPath The path to the file to edit.
     * @param strings The strings to replace. The first half of the strings will be replaced with the second half in the
     * order they are provided.
     */
    private static void replaceInFile(ClassCustomization classCustomization, String classPath,
                                      String... strings) {
        // Replace all instances of KeyVaultServiceVersion with CertificateServiceVersion. We'll remove this once the
        // TSP spec includes all service versions.
        Editor editor = classCustomization.getEditor();
        String fileContent = editor.getFileContent(classPath);

        // Ensure names has an even length.
        if (strings.length % 2 != 0) {
            throw new IllegalArgumentException("The 'names' parameter must have an even number of elements.");
        }

        for (int i = 0; i < (strings.length / 2); i++) {
            fileContent = fileContent.replace(strings[i], strings[i + strings.length / 2]);
        }

        editor.replaceFile(classPath, fileContent);

        // Uncomment once there's a new version of the AutoRest library out.
        /*List<Range> ranges = editor.searchText(classPath, "KeyVaultServiceVersion");

        for (Range range : ranges) {
            editor.replace(classPath, range.getStart(), range.getEnd(), "CertificateServiceVersion");
        }*/
    }
}
