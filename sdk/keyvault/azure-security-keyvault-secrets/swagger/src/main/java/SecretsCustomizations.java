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
 * Contains customizations for Azure KeyVault's Secrets swagger code generation.
 */
public class SecretsCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        Editor rawEditor = libraryCustomization.getRawEditor();

        // Remove unnecessary files.
        removeFiles(rawEditor);

        // Rename files we will reuse.
        renameFiles(rawEditor);

        // Customize the SecretClientImpl class.
        PackageCustomization packageCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.secrets.implementation");
        ClassCustomization implClientClassCustomization = packageCustomization.getClass("SecretClientImpl");
        customizeClientImpl(implClientClassCustomization);

        ClassCustomization asyncClientClassCustomization = packageCustomization.getClass("SecretAsyncClient");
        String asyncClientClassPath =
            "src/main/java/com/azure/security/keyvault/secrets/implementation/SecretAsyncClient.java";
        customizeInnerClients(asyncClientClassCustomization, asyncClientClassPath);

        String syncClientClassPath =
            "src/main/java/com/azure/security/keyvault/secrets/implementation/SecretClient.java";
        ClassCustomization syncClientClassCustomization = packageCustomization.getClass("SecretClient");
        customizeInnerClients(syncClientClassCustomization, syncClientClassPath);

        ClassCustomization clientBuilderClassCustomization = packageCustomization.getClass("SecretClientBuilder");
        customizeClientBuilder(clientBuilderClassCustomization);

        // Update the module-info.java and package-info.java files to include the right Javadoc.
        customizeModuleInfo(rawEditor);
        customizePackageInfos(rawEditor);
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to SecretServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile(
            "src/main/java/com/azure/security/keyvault/secrets/implementation/KeyVaultServiceVersion.java");
        //editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/implementation/SecretClientBuilder.java");
        editor.removeFile(
            "src/main/java/com/azure/security/keyvault/secrets/implementation/implementation/package-info.java");
        //editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/implementation/implementation");
        editor.removeFile(
            "src/test/java/com/azure/security/keyvault/secrets/implementation/generated/SecretClientTestBase.java");
    }

    private static void renameFiles(Editor editor) {
        // Move SecretClientImpl.java to the right package.
        editor.renameFile(
            "src/main/java/com/azure/security/keyvault/secrets/implementation/implementation/SecretClientImpl.java",
            "src/main/java/com/azure/security/keyvault/secrets/implementation/SecretClientImpl.java");

        // Uncomment the following line once the TSP spec includes all service versions.
        /*editor.renameFile("src/main/java/com/azure/security/keyvault/secrets/implementation/implementation/KeyVaultServiceVersion.java",
            "src/main/java/com/azure/security/keyvault/secrets/SecretServiceVersion.java");*/

        // Haven't figured out a way to move files in the resources folder.
        /*editor.renameFile("src/main/resources/azure-security-keyvault-secrets-implementation.properties",
            "src/main/resources/azure-security-keyvault-secrets.properties");
        editor.renameFile("src/main/resources/META-INF/azure-security-keyvault-secrets-implementation_apiview_properties.json",
            "src/main/resources/META-INF/azure-security-keyvault-secrets_apiview_properties.json");*/
    }

    private static void customizeClientImpl(ClassCustomization classCustomization) {
        // Remove the KeyVaultServiceVersion import since we will use SecretServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast -> {
            ast.getPackageDeclaration().ifPresent(packageDeclaration ->
                packageDeclaration.setName("com.azure.security.keyvault.secrets.implementation"));

            replaceImport(ast, "com.azure.security.keyvault.secrets.implementation.KeyVaultServiceVersion",
                "com.azure.security.keyvault.secrets.SecretServiceVersion");
        });

        String classPath = "src/main/java/com/azure/security/keyvault/secrets/implementation/SecretClientImpl.java";

        renameClassInFile(classCustomization, classPath, "KeyVaultServiceVersion", "SecretServiceVersion");
    }

    private static void customizeInnerClients(ClassCustomization classCustomization, String classPath) {
        // Remove the KeyVaultServiceVersion import since we will use SecretServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast ->
            removeImport(ast, "com.azure.security.keyvault.secrets.implementation.implementation.SecretClientImpl"));
    }

    private static void customizeClientBuilder(ClassCustomization classCustomization) {
        // Remove the KeyVaultServiceVersion import since we will use SecretServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast -> {
            replaceImport(ast, "com.azure.security.keyvault.secrets.implementation.implementation.SecretClientImpl",
                "com.azure.security.keyvault.secrets.SecretServiceVersion");
        });

        String classPath = "src/main/java/com/azure/security/keyvault/secrets/implementation/SecretClientBuilder.java";

        renameClassInFile(classCustomization, classPath, "KeyVaultServiceVersion", "SecretServiceVersion");
    }

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "",
                "module com.azure.security.keyvault.secrets {",
                "    requires transitive com.azure.core;",
                "    requires com.azure.json;",
                "",
                "    exports com.azure.security.keyvault.secrets;",
                "    exports com.azure.security.keyvault.secrets.models;",
                "",
                "    opens com.azure.security.keyvault.secrets to com.azure.core;",
                "    opens com.azure.security.keyvault.secrets.implementation to com.azure.core;",
                "    opens com.azure.security.keyvault.secrets.implementation.models to com.azure.core;",
                "    opens com.azure.security.keyvault.secrets.models to com.azure.core;",
                "}",
                ""
            ));
    }

    private static void customizePackageInfos(Editor editor) {
        editor.replaceFile("src/main/java/com/azure/security/keyvault/secrets/implementation/package-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "// Code generated by Microsoft (R) TypeSpec Code Generator.",
                "",
                "/**",
                " * Package containing the implementations for SecretClient. The key vault client performs "
                    + "cryptographic key operations",
                " * and vault operations against the Key Vault service.",
                " */",
                "package com.azure.security.keyvault.secrets.implementation;",
                ""
            ));

        editor.replaceFile("src/main/java/com/azure/security/keyvault/secrets/implementation/models/package-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "// Code generated by Microsoft (R) TypeSpec Code Generator.",
                "",
                "/**",
                " * Package containing the data models for SecretClient. The key vault client performs cryptographic "
                    + "key operations and",
                " * vault operations against the Key Vault service.",
                " */",
                "package com.azure.security.keyvault.secrets.implementation.models;",
                ""
            ));
    }

    private static void renameClassInFile(ClassCustomization classCustomization, String classPath,
                                          String originalName, String newName) {
        // Replace all instances of KeyVaultServiceVersion with SecretServiceVersion. We'll remove this once the TSP
        // spec includes all service versions.
        Editor editor = classCustomization.getEditor();
        String fileContent = editor.getFileContent(classPath);
        String newFileContent = fileContent.replace(originalName, newName);
        editor.replaceFile(classPath, newFileContent);

        // Uncomment once there's a new version of the AutoRest library out.
        /*List<Range> ranges = editor.searchText(classPath, "KeyVaultServiceVersion");

        for (Range range : ranges) {
            editor.replace(classPath, range.getStart(), range.getEnd(), "SecretServiceVersion");
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
