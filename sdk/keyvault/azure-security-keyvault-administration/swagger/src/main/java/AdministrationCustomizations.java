// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Contains customizations for Azure Key Vault Administration code generation.
 */
public class AdministrationCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        Editor rawEditor = libraryCustomization.getRawEditor();

        // Remove unnecessary files.
        removeFiles(rawEditor);

        // Customize the client impl classes.
        PackageCustomization implPackageCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.administration.implementation");
        customizeClientImpl(implPackageCustomization.getClass("KeyVaultClientImpl"), "KeyVaultClientImpl", "KeyVault",
            "KeyVaultAdministration");
        customizeClientImpl(implPackageCustomization.getClass("RoleAssignmentsImpl"), "RoleAssignmentsImpl", "KeyVault",
            "KeyVaultAdministration");
        customizeClientImpl(implPackageCustomization.getClass("RoleDefinitionsImpl"), "RoleDefinitionsImpl", "KeyVault",
            "KeyVaultAdministration");

        // Rename base client impl.
        rawEditor.renameFile(
            "src/main/java/com/azure/security/keyvault/administration/implementation/KeyVaultClientImpl.java",
            "src/main/java/com/azure/security/keyvault/administration/implementation/KeyVaultAdministrationClientImpl.java");

        // Change the names of generated
        ClassCustomization keyVaultRoleScopeCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.administration.models")
                .getClass("KeyVaultRoleScope");

        customizeKeyVaultRoleScope(keyVaultRoleScopeCustomization);
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to KeyServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/administration/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/administration/KeyVaultAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/administration/KeyVaultClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/administration/KeyVaultClientBuilder.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/administration/RoleAssignmentsAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/administration/RoleAssignmentsClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/administration/RoleDefinitionsAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/administration/RoleDefinitionsClient.java");
    }

    private static void customizeClientImpl(ClassCustomization classCustomization, String className, String toReplace,
        String replacement) {

        // Remove the KeyVaultServiceVersion import since we will use KeyVaultAdministrationServiceVersion for now.
        // We'll remove this once the TSP spec includes all service versions.
        replaceInFile(classCustomization,
            "src/main/java/com/azure/security/keyvault/administration/implementation/" + className + ".java",
            toReplace, replacement);
    }

    private static void customizeKeyVaultRoleScope(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport(IllegalArgumentException.class)
                .addImport(URL.class)
                .addImport(MalformedURLException.class);

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                .setType("KeyVaultRoleScope")
                .addParameter("String", "url")
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "/**",
                    " * Creates of finds a {@link KeyVaultRoleScope} from its string representation.",
                    " *",
                    " * @param url A string representing a URL containing the name of the scope to look for.",
                    " * @return The corresponding {@link KeyVaultRoleScope}.",
                    " * @throws IllegalArgumentException If the given {@code url} is malformed.",
                    " */"
                )))
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "try {",
                    "    return fromString(new URL(url).getPath());",
                    "} catch (MalformedURLException e) {",
                    "    throw new IllegalArgumentException(e);",
                    "}",
                    "}"
                )));

            clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                .setType("KeyVaultRoleScope")
                .addParameter("URL", "url")
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "/**",
                    " * Creates of finds a {@link KeyVaultRoleScope} from its string representation.",
                    " *",
                    " * @param url A URL containing the name of the scope to look for.",
                    " * @return The corresponding {@link KeyVaultRoleScope}.",
                    " */"
                )))
                .setBody(StaticJavaParser.parseBlock("{return fromString(url.getPath());}"));
        });
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

        // Replace all instances of KeyVaultServiceVersion with KeyVaultAdministrationServiceVersion. We'll remove this
        // once the TSP spec includes all service versions.
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
            editor.replace(classPath, range.getStart(), range.getEnd(), "KeyVaultAdministrationServiceVersion");
        }*/
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
