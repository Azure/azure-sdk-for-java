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
        PackageCustomization implPackageCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.certificates.implementation");
        ClassCustomization implClientClassCustomization = implPackageCustomization.getClass("KeyVaultClientImpl");
        customizeClientImpl(implClientClassCustomization);

        // Rename files we will reuse.
        renameFiles(rawEditor);

        customizeModuleInfo(rawEditor);
        customizePackageInfos(rawEditor);

        // Change the names of generated
        /*PackageCustomization modelsPackageCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.certificates.models");

        customizeAlgorithms(modelsPackageCustomization);*/
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to CertificateServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/KeyVaultAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/KeyVaultClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/KeyVaultClientBuilder.java");
    }

    private static void renameFiles(Editor editor) {
        editor.renameFile(
            "src/main/java/com/azure/security/keyvault/certificates/implementation/KeyVaultClientImpl.java",
            "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientImpl.java");

        // Uncomment the following line once the TSP spec includes all service versions.
        /*editor.renameFile("src/main/java/com/azure/security/keyvault/certificates/implementation/KeyVaultServiceVersion.java",
            "src/main/java/com/azure/security/keyvault/certificates/CertificateServiceVersion.java");*/
    }

    private static void customizeClientImpl(ClassCustomization classCustomization) {
        // Remove the KeyVaultServiceVersion import since we will use CertificateServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast ->
            replaceImport(ast, "com.azure.security.keyvault.certificates.KeyVaultServiceVersion",
            "com.azure.security.keyvault.certificates.CertificateServiceVersion"));

        String classPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/KeyVaultClientImpl.java";

        replaceInFile(classCustomization, classPath, "KeyVault", "Certificate");
    }

    /*private static void customizeAlgorithms(PackageCustomization packageCustomization) {
        packageCustomization.getClass("CertificateKeyCurveName")
            .customizeAst(ast -> ast.getClassByName("CertificateKeyCurveName")
                .ifPresent(clazz -> {
                    clazz.getFieldByName("P256K").ifPresent(field -> field.getVariable(0).setName("P_256K"));
                    clazz.getFieldByName("P256").ifPresent(field -> field.getVariable(0).setName("P_256"));
                    clazz.getFieldByName("P384").ifPresent(field -> field.getVariable(0).setName("P_384"));
                    clazz.getFieldByName("P521").ifPresent(field -> field.getVariable(0).setName("P_521"));
                }));
    }*/

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "",
                "module com.azure.security.keyvault.certificates {",
                "    requires transitive com.azure.core;",
                "    requires com.azure.json;",
                "",
                "    exports com.azure.security.keyvault.certificates;",
                "    exports com.azure.security.keyvault.certificates.models;",
                "",
                "    opens com.azure.security.keyvault.certificates to com.azure.core;",
                "    opens com.azure.security.keyvault.certificates.implementation to com.azure.core;",
                "    opens com.azure.security.keyvault.certificates.implementation.models to com.azure.core;",
                "    opens com.azure.security.keyvault.certificates.models to com.azure.core;",
                "}",
                ""
            ));
    }

    private static void customizePackageInfos(Editor editor) {
        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/implementation/package-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "// Code generated by Microsoft (R) TypeSpec Code Generator.",
                "",
                "/**",
                " * Package containing the implementations for CertificateClient. The key vault client performs "
                    + "cryptographic key operations",
                " * and vault operations against the Key Vault service.",
                " */",
                "package com.azure.security.keyvault.certificates.implementation;",
                ""
            ));

        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/implementation/models/package-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "// Code generated by Microsoft (R) TypeSpec Code Generator.",
                "",
                "/**",
                " * Package containing the data models for CertificateClient. The key vault client performs "
                    + "cryptographic key operations and",
                " * vault operations against the Key Vault service.",
                " */",
                "package com.azure.security.keyvault.certificates.implementation.models;",
                ""
            ));
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

    private static void replaceImport(CompilationUnit ast, String originalImport, String newImport) {
        NodeList<ImportDeclaration> nodeList = ast.getImports();

        for (ImportDeclaration importDeclaration : nodeList) {
            if (importDeclaration.getNameAsString().equals(originalImport)) {
                importDeclaration.setName(newImport);

                break;
            }
        }

        ast.setImports(nodeList);
    }

    private static void removeImport(CompilationUnit ast, String importStatement) {
        NodeList<ImportDeclaration> nodeList = ast.getImports();

        for (ImportDeclaration importDeclaration : nodeList) {
            if (importDeclaration.getNameAsString().equals(importStatement)) {
                nodeList.remove(importDeclaration);

                break;
            }
        }

        ast.setImports(nodeList);
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
