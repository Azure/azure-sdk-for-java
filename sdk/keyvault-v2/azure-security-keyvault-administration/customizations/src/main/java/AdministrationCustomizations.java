// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Contains customizations for Azure Key Vault Administration code generation.
 */
public class AdministrationCustomizations extends Customization {
    private static final String ROOT_FILE_PATH = "src/main/java/com/azure/v2/security/keyvault/administration/";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        // Remove unnecessary files.
        removeFiles(libraryCustomization.getRawEditor());
        customizeKeyVaultRoleScope(libraryCustomization);
        customizeServiceVersion(libraryCustomization);
        customizeModuleInfo(libraryCustomization.getRawEditor());
    }

    private static void removeFiles(Editor editor) {
        editor.removeFile(ROOT_FILE_PATH + "KeyVaultAdministrationClient.java");
        editor.removeFile(ROOT_FILE_PATH + "KeyVaultAdministrationClientBuilder.java");
        editor.removeFile(ROOT_FILE_PATH + "RoleAssignmentsClient.java");
        editor.removeFile(ROOT_FILE_PATH + "RoleDefinitionsClient.java");
        editor.removeFile(ROOT_FILE_PATH + "implementation/models/package-info.java");
    }

    private static void customizeKeyVaultRoleScope(LibraryCustomization customization) {
        customization.getClass("com.azure.v2.security.keyvault.administration.models", "KeyVaultRoleScope")
            .customizeAst(ast -> {
                ast.addImport(IllegalArgumentException.class)
                    .addImport(URL.class)
                    .addImport(MalformedURLException.class);

                ast.getClassByName("KeyVaultRoleScope").ifPresent(clazz -> {
                    clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                        .setType("KeyVaultRoleScope")
                        .addParameter("String", "url")
                        .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                            "Creates of finds a {@link KeyVaultRoleScope} from its string representation."))
                            .addBlockTag("param", "url A string representing a URL containing the name of the scope to look for.")
                            .addBlockTag("return", "The corresponding {@link KeyVaultRoleScope}.")
                            .addBlockTag("throws", "IllegalArgumentException If the given {@code url} is malformed."))
                        .setBody(StaticJavaParser.parseBlock("{ try { return fromValue(new URL(url).getPath()); }"
                                + "catch (MalformedURLException e) { throw new IllegalArgumentException(e); } }"));

                    clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                        .setType("KeyVaultRoleScope")
                        .addParameter("URL", "url")
                        .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                            "Creates of finds a {@link KeyVaultRoleScope} from its string representation."))
                            .addBlockTag("param", "url", "A URL containing the name of the scope to look for.")
                            .addBlockTag("return", "The corresponding {@link KeyVaultRoleScope}.", " */"))
                        .setBody(StaticJavaParser.parseBlock("{ return fromValue(url.getPath()); }"));
                });
            });
    }

    private static void customizeServiceVersion(LibraryCustomization customization) {
        Editor editor = customization.getRawEditor();
        String serviceVersion = editor.getFileContent(ROOT_FILE_PATH + "KeyVaultServiceVersion.java")
            .replace("KeyVaultServiceVersion", "KeyVaultAdministrationServiceVersion");

        editor.addFile(ROOT_FILE_PATH + "KeyVaultAdministrationServiceVersion.java", serviceVersion);
        editor.removeFile(ROOT_FILE_PATH + "KeyVaultServiceVersion.java");

        for (String impl : List.of("KeyVaultAdministrationClientImpl", "RoleAssignmentsImpl", "RoleDefinitionsImpl")) {
            String fileName = ROOT_FILE_PATH + "implementation/" + impl + ".java";
            String fileContent = customization.getRawEditor().getFileContent(fileName);
            fileContent = fileContent.replace("KeyVaultServiceVersion", "KeyVaultAdministrationServiceVersion");
            customization.getRawEditor().replaceFile(fileName, fileContent);
        }
    }

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java", String.join("\n",
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
}
