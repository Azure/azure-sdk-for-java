// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure KeyVault's Secrets swagger code generation.
 */
public class SecretsCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        removeFiles(libraryCustomization.getRawEditor());
        renameFiles(libraryCustomization.getRawEditor());

        PackageCustomization packageCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.secrets.implementation.implementation");

        ClassCustomization classCustomization = packageCustomization.getClass("SecretClientImpl");

        customizeClientImpl(classCustomization);
        customizeModuleInfo(libraryCustomization.getRawEditor());
        customizePackageInfos(libraryCustomization.getRawEditor());
    }

    private static void customizeClientImpl(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.getPackageDeclaration().ifPresent(packageDeclaration -> {
                packageDeclaration.setName("com.azure.security.keyvault.secrets.implementation");
            });

            NodeList<ImportDeclaration> nodeList = ast.getImports();

            for (ImportDeclaration importDeclaration : nodeList) {
                if (importDeclaration.getNameAsString().equals("com.azure.security.keyvault.secrets.implementation.implementation.KeyVaultServiceVersion")) {
                    importDeclaration.setName("com.azure.security.keyvault.secrets.SecretServiceVersion");
                }
            }

            ast.setImports(nodeList);
        });
    }

    private static void renameFiles(Editor editor) {
        editor.renameFile("src/main/java/com/azure/security/keyvault/secrets/implementation/implementation/SecretClientImpl.java",
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

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to SecretServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/implementation/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/implementation/SecretClientBuilder.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/implementation/implementation/package-info.java");
        //editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/implementation/implementation");
        editor.removeFile("src/test/java/com/azure/security/keyvault/secrets/implementation/generated/SecretClientTestBase.java");
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
                " * Package containing the implementations for SecretClient. The key vault client performs cryptographic key operations",
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
                " * Package containing the data models for SecretClient. The key vault client performs cryptographic key operations and",
                " * vault operations against the Key Vault service.",
                " */",
                "package com.azure.security.keyvault.secrets.implementation.models;",
                ""
            ));
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
