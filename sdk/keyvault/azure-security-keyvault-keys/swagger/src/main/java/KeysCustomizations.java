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
 * Contains customizations for Azure KeyVault's Keys swagger code generation.
 */
public class KeysCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        Editor rawEditor = libraryCustomization.getRawEditor();

        // Remove unnecessary files.
        removeFiles(rawEditor);

        // Customize the KeyClientImpl class.
        PackageCustomization implPackageCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.keys.implementation");
        ClassCustomization implClientClassCustomization = implPackageCustomization.getClass("KeyClientImpl");
        customizeClientImpl(implClientClassCustomization);

        // Change the names of the generated enum values.
        ClassCustomization keyCurveNameCustomization =
            libraryCustomization.getPackage("com.azure.security.keyvault.keys.models")
                .getClass("KeyCurveName");

        customizeKeyCurveName(keyCurveNameCustomization);
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to KeyServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyClientBuilder.java");
    }

    private static void customizeClientImpl(ClassCustomization classCustomization) {
        // Remove the KeyVaultServiceVersion import since we will use KeyServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast ->
            replaceImport(ast, "com.azure.security.keyvault.keys.KeyVaultServiceVersion",
                "com.azure.security.keyvault.keys.KeyServiceVersion"));

        String classPath =
            "src/main/java/com/azure/security/keyvault/keys/implementation/KeyClientImpl.java";

        replaceInFile(classCustomization, classPath, new String[] { "KeyVault" }, new String[] { "Key" });
    }

    private static void customizeKeyCurveName(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast ->
            ast.getClassByName("KeyCurveName")
                .ifPresent(clazz -> {
                    clazz.getFieldByName("P256").ifPresent(field -> field.getVariable(0).setName("P_256"));
                    clazz.getFieldByName("P384").ifPresent(field -> field.getVariable(0).setName("P_384"));
                    clazz.getFieldByName("P521").ifPresent(field -> field.getVariable(0).setName("P_521"));
                    clazz.getFieldByName("P256_K").ifPresent(field -> field.getVariable(0).setName("P_256K"));
                })
        );

        String classPath =
            "src/main/java/com/azure/security/keyvault/keys/models/KeyCurveName.java";

        replaceInFile(classCustomization, classPath,
            new String[] { " For valid values, see JsonWebKeyCurveName." },
            new String[] { "" });
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

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
