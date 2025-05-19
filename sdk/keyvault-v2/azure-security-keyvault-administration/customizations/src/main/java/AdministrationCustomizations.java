// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.microsoft.typespec.http.client.generator.core.customization.Customization;
import com.microsoft.typespec.http.client.generator.core.customization.Editor;
import com.microsoft.typespec.http.client.generator.core.customization.LibraryCustomization;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Contains customizations for Azure Key Vault Administration code generation.
 */
public class AdministrationCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        // Remove unnecessary files.
        removeFiles(libraryCustomization.getRawEditor());
        moveListResultFiles(libraryCustomization);
        customizeKeyVaultRoleScope(libraryCustomization);
        customizeServiceVersion(libraryCustomization);
        customizeModuleInfo(libraryCustomization.getRawEditor());
    }

    private static void removeFiles(Editor editor) {
        editor.removeFile("src/main/java/com/azure/v2/security/keyvault/administration/KeyVaultAdministrationClient.java");
        editor.removeFile("src/main/java/com/azure/v2/security/keyvault/administration/KeyVaultAdministrationClientBuilder.java");
        editor.removeFile("src/main/java/com/azure/v2/security/keyvault/administration/RoleAssignmentsClient.java");
        editor.removeFile("src/main/java/com/azure/v2/security/keyvault/administration/RoleDefinitionsClient.java");
        editor.removeFile("src/main/java/com/azure/v2/security/keyvault/administration/implementation/implementation/models/package-info.java");
    }

    private static void moveListResultFiles(LibraryCustomization libraryCustomization) {
        moveSingleFile(libraryCustomization,
            "com.azure.v2.security.keyvault.administration.implementation.implementation.models",
            "com.azure.v2.security.keyvault.administration.implementation.models", "RoleAssignmentListResult");
        moveSingleFile(libraryCustomization,
            "com.azure.v2.security.keyvault.administration.implementation.implementation.models",
            "com.azure.v2.security.keyvault.administration.implementation.models", "RoleDefinitionListResult");

        // Update imports statements for moved classes in impl client.
        String classPath = "src/main/java/com/azure/v2/security/keyvault/administration/implementation/KeyVaultAdministrationClientImpl.java";
        Editor editor = libraryCustomization.getRawEditor();
        String newFileContent = editor.getFileContent(classPath)
            .replace("implementation.implementation", "implementation");

        editor.replaceFile(classPath, newFileContent);

        // Update imports statements for moved classes in impl client.
        classPath = "src/main/java/com/azure/v2/security/keyvault/administration/implementation/RoleAssignmentsImpl.java";
        newFileContent = editor.getFileContent(classPath)
            .replace("implementation.implementation", "implementation");

        editor.replaceFile(classPath, newFileContent);

        // Update imports statements for moved classes in impl client.
        classPath = "src/main/java/com/azure/v2/security/keyvault/administration/implementation/RoleDefinitionsImpl.java";
        newFileContent = editor.getFileContent(classPath)
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

    private static void customizeKeyVaultRoleScope(LibraryCustomization libraryCustomization) {
        libraryCustomization
            .getPackage("com.azure.v2.security.keyvault.administration.models")
            .getClass("KeyVaultRoleScope")
            .customizeAst(ast -> {
                ast.addImport(IllegalArgumentException.class)
                    .addImport(URL.class)
                    .addImport(MalformedURLException.class);

                ClassOrInterfaceDeclaration clazz = ast.getClassByName("KeyVaultRoleScope").get();

                clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                    .setType("KeyVaultRoleScope")
                    .addParameter("String", "url")
                    .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline("/**",
                        " * Creates of finds a {@link KeyVaultRoleScope} from its string representation.", " *",
                        " * @param url A string representing a URL containing the name of the scope to look for.",
                        " * @return The corresponding {@link KeyVaultRoleScope}.",
                        " * @throws IllegalArgumentException If the given {@code url} is malformed.", " */")))
                    .setBody(StaticJavaParser.parseBlock(
                        joinWithNewline("{", "try {", "    return fromValue(new URL(url).getPath());",
                            "} catch (MalformedURLException e) {", "    throw new IllegalArgumentException(e);", "}",
                            "}")));

                clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                    .setType("KeyVaultRoleScope")
                    .addParameter("URL", "url")
                    .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline("/**",
                        " * Creates of finds a {@link KeyVaultRoleScope} from its string representation.", " *",
                        " * @param url A URL containing the name of the scope to look for.",
                        " * @return The corresponding {@link KeyVaultRoleScope}.", " */")))
                    .setBody(StaticJavaParser.parseBlock("{return fromValue(url.getPath());}"));
            });
    }

    private static void customizeServiceVersion(LibraryCustomization libraryCustomization) {
        libraryCustomization.getPackage("com.azure.v2.security.keyvault.administration")
            .getClass("KeyVaultServiceVersion")
            .rename("KeyVaultAdministrationServiceVersion");
    }

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java", joinWithNewline(
            "// Copyright (c) Microsoft Corporation. All rights reserved.",
            "// Licensed under the MIT License.",
            "// Code generated by Microsoft (R) TypeSpec Code Generator.",
            "",
            "module com.azure.v2.security.keyvault.administration {",
            "    requires transitive com.azure.v2.core;",
            "",
            "    exports com.azure.v2.security.keyvault.administration;",
            "    exports com.azure.v2.security.keyvault.administration.models;",
            "}"));
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
