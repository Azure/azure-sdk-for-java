// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
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
        PackageCustomization implPackageCustomization = libraryCustomization.getPackage(
            "com.azure.security.keyvault.administration.implementation");
        String implPath = "src/main/java/com/azure/security/keyvault/administration/implementation/";

        replaceInFile(implPackageCustomization.getClass("KeyVaultClientImpl"), implPath + "KeyVaultClientImpl.java",
            new String[] {
                "KeyVault",
                "private Mono<Response<BinaryData>> fullBackupWithResponseAsync",
                "private Response<BinaryData> fullBackupWithResponse",
                "private Mono<Response<BinaryData>> preFullBackupWithResponseAsync",
                "private Response<BinaryData> preFullBackupWithResponse",
                "private Mono<Response<BinaryData>> preFullRestoreOperationWithResponseAsync",
                "private Response<BinaryData> preFullRestoreOperationWithResponse",
                "private Mono<Response<BinaryData>> fullRestoreOperationWithResponseAsync",
                "private Response<BinaryData> fullRestoreOperationWithResponse",
                "private Mono<Response<BinaryData>> selectiveKeyRestoreOperationWithResponseAsync",
                "private Response<BinaryData> selectiveKeyRestoreOperationWithResponse" }, new String[] {
                "KeyVaultAdministration",
                "public Mono<Response<BinaryData>> fullBackupWithResponseAsync",
                "public Response<BinaryData> fullBackupWithResponse",
                "public Mono<Response<BinaryData>> preFullBackupWithResponseAsync",
                "public Response<BinaryData> preFullBackupWithResponse",
                "public Mono<Response<BinaryData>> preFullRestoreOperationWithResponseAsync",
                "public Response<BinaryData> preFullRestoreOperationWithResponse",
                "public Mono<Response<BinaryData>> fullRestoreOperationWithResponseAsync",
                "public Response<BinaryData> fullRestoreOperationWithResponse",
                "public Mono<Response<BinaryData>> selectiveKeyRestoreOperationWithResponseAsync",
                "public Response<BinaryData> selectiveKeyRestoreOperationWithResponse" });
        replaceInFile(implPackageCustomization.getClass("RoleAssignmentsImpl"), implPath + "RoleAssignmentsImpl.java",
            new String[] {
                "KeyVault",
                "private Mono<PagedResponse<BinaryData>> listForScopeSinglePageAsync",
                "private Mono<PagedResponse<BinaryData>> listForScopeNextSinglePageAsync" }, new String[] {
                "KeyVaultAdministration",
                "public Mono<PagedResponse<BinaryData>> listForScopeSinglePageAsync",
                "public Mono<PagedResponse<BinaryData>> listForScopeNextSinglePageAsync" });
        replaceInFile(implPackageCustomization.getClass("RoleDefinitionsImpl"), implPath + "RoleDefinitionsImpl.java",
            new String[] {
                "KeyVault",
                "private Mono<PagedResponse<BinaryData>> listSinglePageAsync",
                "private Mono<PagedResponse<BinaryData>> listNextSinglePageAsync" }, new String[] {
                "KeyVaultAdministration",
                "public Mono<PagedResponse<BinaryData>> listSinglePageAsync",
                "public Mono<PagedResponse<BinaryData>> listNextSinglePageAsync" });

        // Rename base client impl.
        rawEditor.renameFile(implPath + "KeyVaultClientImpl.java", implPath + "KeyVaultAdministrationClientImpl.java");

        // Change the names of generated
        ClassCustomization keyVaultRoleScopeCustomization = libraryCustomization.getPackage(
            "com.azure.security.keyvault.administration.models").getClass("KeyVaultRoleScope");

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

    private static void customizeKeyVaultRoleScope(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport(IllegalArgumentException.class).addImport(URL.class).addImport(MalformedURLException.class);

            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                .setType("KeyVaultRoleScope")
                .addParameter("String", "url")
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline("/**",
                    " * Creates of finds a {@link KeyVaultRoleScope} from its string representation.", " *",
                    " * @param url A string representing a URL containing the name of the scope to look for.",
                    " * @return The corresponding {@link KeyVaultRoleScope}.",
                    " * @throws IllegalArgumentException If the given {@code url} is malformed.", " */")))
                .setBody(StaticJavaParser.parseBlock(
                    joinWithNewline("{", "try {", "    return fromString(new URL(url).getPath());",
                        "} catch (MalformedURLException e) {", "    throw new IllegalArgumentException(e);", "}",
                        "}")));

            clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                .setType("KeyVaultRoleScope")
                .addParameter("URL", "url")
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline("/**",
                    " * Creates of finds a {@link KeyVaultRoleScope} from its string representation.", " *",
                    " * @param url A URL containing the name of the scope to look for.",
                    " * @return The corresponding {@link KeyVaultRoleScope}.", " */")))
                .setBody(StaticJavaParser.parseBlock("{return fromString(url.getPath());}"));
        });
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
            // Replace all instances of KeyVaultServiceVersion with KeyVaultAdministrationServiceVersion. We'll remove this
            // once the TSP spec includes all service versions.
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

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
