// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure Key Vault's Certificates code generation.
 */
public class CertificatesCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        // Remove unnecessary files.
        removeFiles(libraryCustomization.getRawEditor());

        // Rename KeyVaultServiceVersion to KeyServiceVersion.
        libraryCustomization.getPackage("com.azure.v2.security.keyvault.certificates")
            .getClass("KeyVaultServiceVersion")
            .rename("CertificateServiceVersion");

        // Move *ListResult files.
        moveListResultFiles(libraryCustomization);

        customizeError(libraryCustomization);

        // Customize the module-info.java file.
        customizeModuleInfo(libraryCustomization.getRawEditor());
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to CertificateServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/v2/security/keyvault/certificates/CertificateClient.java");
        editor.removeFile("src/main/java/com/azure/v2/security/keyvault/certificates/CertificateClientBuilder.java");
        editor.removeFile("src/main/java/com/azure/v2/security/keyvault/certificates/implementation/implementation/models/package-info.java");
    }

    private static void moveListResultFiles(LibraryCustomization libraryCustomization) {
        moveSingleFile(libraryCustomization,
            "com.azure.v2.security.keyvault.certificates.implementation.implementation.models",
            "com.azure.v2.security.keyvault.certificates.implementation.models", "DeletedCertificateListResult");
        moveSingleFile(libraryCustomization,
            "com.azure.v2.security.keyvault.certificates.implementation.implementation.models",
            "com.azure.v2.security.keyvault.certificates.implementation.models", "CertificateListResult");
        moveSingleFile(libraryCustomization,
            "com.azure.v2.security.keyvault.certificates.implementation.implementation.models",
            "com.azure.v2.security.keyvault.certificates.implementation.models", "CertificateIssuerListResult");

        // Update imports statements for moved classes in impl client.
        String classPath = "src/main/java/com/azure/v2/security/keyvault/certificates/implementation/CertificateClientImpl.java";
        Editor editor = libraryCustomization.getRawEditor();
        String newFileContent = editor.getFileContent(classPath)
            .replace("implementation.implementation", "implementation");

        editor.replaceFile(classPath, newFileContent);
    }

    private static void moveSingleFile(LibraryCustomization libraryCustomization, String oldPackage, String newPackage,
        String className) {

        Editor editor = libraryCustomization.getRawEditor();
        String oldClassPath = "src/main/java/" + oldPackage.replace('.', '/') + "/" + className + ".java";
        String newClassPath = "src/main/java/" + newPackage.replace('.', '/') + "/" + className + ".java";

        // Update the package declaration.
        libraryCustomization.getPackage(oldPackage)
            .getClass(className)
            .customizeAst(ast -> ast.getPackageDeclaration()
                .ifPresent(packageDeclaration -> packageDeclaration.setName(newPackage)));

        // Remove unnecessary import statement.
        String newFileContent = editor.getFileContent(oldClassPath)
            .replace("import " + oldPackage + "." + className.replace("ListResult", "Item") + ";\n", "");

        // Replace file contents.
        editor.replaceFile(oldClassPath, newFileContent);

        // Move file to the new path.
        editor.renameFile(oldClassPath, newClassPath);
    }

    private static void customizeError(LibraryCustomization libraryCustomization) {
        String implModelsPackage = "com.azure.v2.security.keyvault.certificates.implementation.models";
        String implModelsDirectory = "src/main/java/com/azure/v2/security/keyvault/certificates/implementation/models/";
        String oldClassName = "KeyVaultErrorError";
        String modelsPackage = "com.azure.v2.security.keyvault.certificates.models";
        String modelsDirectory = "src/main/java/com/azure/v2/security/keyvault/certificates/models/";
        String newClassName = "CertificateOperationError";

        // Rename KeyVaultErrorError to CertificateOperationError.
        ClassCustomization classCustomization = libraryCustomization.getPackage(implModelsPackage)
            .getClass(oldClassName)
            .rename(newClassName)
            .customizeAst(ast -> ast.getPackageDeclaration()
                .ifPresent(packageDeclaration -> packageDeclaration.setName(modelsPackage)));

        String oldClassPath = implModelsDirectory + newClassName + ".java";
        String newClassPath = modelsDirectory + newClassName + ".java";

        replaceInFile(classCustomization, oldClassPath,
            new String[] { oldClassName },
            new String[] { newClassName });

        // Move to the public models package.
        libraryCustomization.getRawEditor().renameFile(oldClassPath, newClassPath);

        // Add import statement in impl CertificateOperation class.
        classCustomization = libraryCustomization.getPackage(implModelsPackage)
            .getClass("CertificateOperation")
            .addImports(modelsPackage + "." + newClassName);

        replaceInFile(classCustomization, implModelsDirectory + "CertificateOperation.java",
            new String[] { oldClassName },
            new String[] { newClassName });

        // Add import statement in impl KeyVaultError class.
        classCustomization = libraryCustomization.getPackage(implModelsPackage)
            .getClass("KeyVaultError")
            .addImports(modelsPackage + "." + newClassName);

        replaceInFile(classCustomization, implModelsDirectory + "KeyVaultError.java",
            new String[] { oldClassName },
            new String[] { newClassName });
    }

    private static void customizeError2(LibraryCustomization libraryCustomization) {
        // Rename error class.
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.v2.security.keyvault.certificates.implementation.models")
            .getClass("KeyVaultErrorError")
            .rename("CertificateOperationError")
            .customizeAst(ast ->
                ast.getPackageDeclaration().ifPresent(packageDeclaration ->
                    packageDeclaration.setName("com.azure.v2.security.keyvault.certificates.models")));

        String classPath =
            "src/main/java/com/azure/v2/security/keyvault/certificates/implementation/models/CertificateOperationError.java";

        replaceInFile(classCustomization, classPath,
            new String[] { "KeyVaultErrorError" },
            new String[] { "CertificateOperationError" });

        // Move it to public package.
        libraryCustomization
            .getRawEditor()
            .renameFile(classPath,
                "src/main/java/com/azure/v2/security/keyvault/certificates/models/CertificateOperationError.java");

        // Replace instances in impl CertificateOperationError and add import statement.
        classCustomization = libraryCustomization
            .getPackage("com.azure.v2.security.keyvault.certificates.implementation.models")
            .getClass("CertificateOperation")
            .addImports("com.azure.v2.security.keyvault.certificates.models.CertificateOperationError");
        classPath =
            "src/main/java/com/azure/v2/security/keyvault/certificates/implementation/models/CertificateOperation.java";

        replaceInFile(classCustomization, classPath,
            new String[] { "KeyVaultErrorError" },
            new String[] { "CertificateOperationError" });

        // Replace instances in impl CertificateOperationError and add import statement.
        classCustomization = libraryCustomization
            .getPackage("com.azure.v2.security.keyvault.certificates.implementation.models")
            .getClass("KeyVaultError")
            .addImports("com.azure.v2.security.keyvault.certificates.models.CertificateOperationError");
        classPath =
            "src/main/java/com/azure/v2/security/keyvault/certificates/implementation/models/KeyVaultError.java";

        replaceInFile(classCustomization, classPath,
            new String[] { "KeyVaultErrorError" },
            new String[] { "CertificateOperationError" });
    }

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

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java", joinWithNewline(
            "// Copyright (c) Microsoft Corporation. All rights reserved.",
            "// Licensed under the MIT License.",
            "// Code generated by Microsoft (R) TypeSpec Code Generator.",
            "",
            "module com.azure.v2.security.keyvault.certificates {",
            "    requires transitive io.clientcore.core;",
            "    requires transitive com.azure.v2.core;",
            "",
            "    exports com.azure.v2.security.keyvault.certificates;",
            "    exports com.azure.v2.security.keyvault.certificates.models;",
            "",
            "    opens com.azure.v2.security.keyvault.certificates.models to io.clientcore.core;",
            "    opens com.azure.v2.security.keyvault.certificates.implementation.models to io.clientcore.core;",
            "}"));
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
