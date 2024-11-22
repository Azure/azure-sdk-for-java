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

        // Rename files we will reuse.
        renameFiles(rawEditor);

        // Customize the CertificateClientImpl class.
        PackageCustomization implPackageCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.certificates.implementation");
        ClassCustomization implClientClassCustomization = implPackageCustomization.getClass("CertificateClientImpl");
        customizeClientImpl(implClientClassCustomization);

        ClassCustomization asyncClientClassCustomization = implPackageCustomization.getClass("CertificateAsyncClient");
        String asyncClientClassPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateAsyncClient.java";
        customizeInnerClients(asyncClientClassCustomization, asyncClientClassPath);

        String syncClientClassPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClient.java";
        ClassCustomization syncClientClassCustomization = implPackageCustomization.getClass("CertificateClient");
        customizeInnerClients(syncClientClassCustomization, syncClientClassPath);

        ClassCustomization clientBuilderClassCustomization =
            implPackageCustomization.getClass("CertificateClientBuilder");
        customizeClientBuilder(clientBuilderClassCustomization);

        customizeModuleInfo(libraryCustomization.getRawEditor());
        customizePackageInfos(libraryCustomization.getRawEditor());

        // Change the names of generated
        /*PackageCustomization modelsPackageCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.certificates.models");

        customizeAlgorithms(modelsPackageCustomization);*/
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to CertificateServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile(
            "src/main/java/com/azure/security/keyvault/certificates/implementation/KeyVaultServiceVersion.java");
        //editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientBuilder.java");
        editor.removeFile(
            "src/main/java/com/azure/security/keyvault/certificates/implementation/implementation/package-info.java");
        //editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/implementation/implementation");
        /*editor.removeFile(
            "src/test/java/com/azure/security/keyvault/certificates/implementation/generated/CertificateClientTestBase.java");*/
    }

    private static void renameFiles(Editor editor) {
        // Move CertificatesClientImpl.java to the right package.
        editor.renameFile(
            "src/main/java/com/azure/security/keyvault/certificates/implementation/implementation/CertificateClientImpl.java",
            "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientImpl.java");

        // Uncomment the following line once the TSP spec includes all service versions.
        /*editor.renameFile("src/main/java/com/azure/security/keyvault/certificates/implementation/implementation/KeyVaultServiceVersion.java",
            "src/main/java/com/azure/security/keyvault/certificates/CertificateServiceVersion.java");*/

        // Haven't figured out a way to move files in the resources folder.
        /*editor.renameFile("src/main/resources/azure-security-keyvault-certificates-implementation.properties",
            "src/main/resources/azure-security-keyvault-certificates.properties");
        editor.renameFile("src/main/resources/META-INF/azure-security-keyvault-certificates-implementation_apiview_properties.json",
            "src/main/resources/META-INF/azure-security-keyvault-certificates_apiview_properties.json");*/
    }

    private static void customizeClientImpl(ClassCustomization classCustomization) {
        // Remove the KeyVaultServiceVersion import since we will use CertificateServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast -> {
            ast.getPackageDeclaration().ifPresent(packageDeclaration ->
                packageDeclaration.setName("com.azure.security.keyvault.certificates.implementation"));

            replaceImport(ast, "com.azure.security.keyvault.certificates.implementation.KeyVaultServiceVersion",
                "com.azure.security.keyvault.certificates.CertificateServiceVersion");
        });

        String classPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientImpl.java";

        renameClassInFile(classCustomization, classPath, "KeyVaultServiceVersion", "CertificateServiceVersion");
    }

    private static void customizeInnerClients(ClassCustomization classCustomization, String classPath) {
        // Remove the KeyVaultServiceVersion import since we will use CertificateServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast ->
            removeImport(ast, "com.azure.security.keyvault.certificates.implementation.implementation.CertificateClientImpl"));
    }

    private static void customizeClientBuilder(ClassCustomization classCustomization) {
        // Remove the KeyVaultServiceVersion import since we will use CertificateServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast -> {
            replaceImport(ast, "com.azure.security.keyvault.certificates.implementation.implementation.CertificateClientImpl",
                "com.azure.security.keyvault.certificates.CertificateServiceVersion");
        });

        String classPath = "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientBuilder.java";

        renameClassInFile(classCustomization, classPath, "KeyVaultServiceVersion", "CertificateServiceVersion");
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

    private static void renameClassInFile(ClassCustomization classCustomization, String classPath,
                                          String originalName, String newName) {
        // Replace all instances of KeyVaultServiceVersion with CertificateServiceVersion. We'll remove this once the
        // TSP spec includes all service versions.
        Editor editor = classCustomization.getEditor();
        String fileContent = editor.getFileContent(classPath);
        String newFileContent = fileContent.replace(originalName, newName);
        editor.replaceFile(classPath, newFileContent);

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
