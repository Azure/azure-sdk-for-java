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
        Editor editor = libraryCustomization.getRawEditor();

        removeFiles(editor);
        customizeModuleInfo(editor);
        customizePackageInfos(editor);
        customizeClientImpl(libraryCustomization);
        customizeKeyCurveName(libraryCustomization);
        customizeReleaseKeyResult(libraryCustomization);
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to KeyServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/keys/KeyClientBuilder.java");
    }

    private static void customizeClientImpl(LibraryCustomization libraryCustomization) {
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.keys.implementation")
            .getClass("KeyClientImpl");

        // Remove the KeyVaultServiceVersion import since we will use KeyServiceVersion for now. We'll remove
        // this once the TSP spec includes all service versions.
        classCustomization.customizeAst(ast ->
            replaceImport(ast, "com.azure.security.keyvault.keys.KeyVaultServiceVersion",
                "com.azure.security.keyvault.keys.KeyServiceVersion"));

        String classPath =
            "src/main/java/com/azure/security/keyvault/keys/implementation/KeyClientImpl.java";

        replaceInFile(classCustomization, classPath, new String[] { "KeyVault" }, new String[] { "Key" });
    }

    private static void customizeKeyCurveName(LibraryCustomization libraryCustomization) {
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.keys.models")
            .getClass("KeyCurveName");

        classCustomization.customizeAst(ast ->
            ast.getClassByName("KeyCurveName")
                .ifPresent(clazz -> {
                    clazz.getFieldByName("P256").ifPresent(field -> field.getVariable(0).setName("P_256"));
                    clazz.getFieldByName("P384").ifPresent(field -> field.getVariable(0).setName("P_384"));
                    clazz.getFieldByName("P521").ifPresent(field -> field.getVariable(0).setName("P_521"));
                    clazz.getFieldByName("P256_K").ifPresent(field -> field.getVariable(0).setName("P_256K"));
                })
        );

        String classPath = "src/main/java/com/azure/security/keyvault/keys/models/KeyCurveName.java";

        replaceInFile(classCustomization, classPath,
            new String[] { " For valid values, see JsonWebKeyCurveName." },
            new String[] { "" });
    }

    private static void customizeReleaseKeyResult(LibraryCustomization libraryCustomization) {
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.keys.models")
            .getClass("ReleaseKeyResult");
        String classPath = "src/main/java/com/azure/security/keyvault/keys/models/ReleaseKeyResult.java";

        replaceInFile(classCustomization, classPath,
            new String[] { "private ReleaseKeyResult()" }, new String[] { "public ReleaseKeyResult()" });
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

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "// Code generated by Microsoft (R) TypeSpec Code Generator.\n"
                + "\n"
                + "module com.azure.security.keyvault.keys {\n"
                + "    requires transitive com.azure.core;\n"
                + "\n"
                + "    exports com.azure.security.keyvault.keys;\n"
                + "    exports com.azure.security.keyvault.keys.models;\n"
                + "    exports com.azure.security.keyvault.keys.cryptography;\n"
                + "    exports com.azure.security.keyvault.keys.cryptography.models;\n"
                + "\n"
                + "    opens com.azure.security.keyvault.keys to com.azure.core;\n"
                + "    opens com.azure.security.keyvault.keys.models to com.azure.core;\n"
                + "    opens com.azure.security.keyvault.keys.implementation.models to com.azure.core;\n"
                + "    opens com.azure.security.keyvault.keys.cryptography.models to com.azure.core;\n"
                + "}\n");
    }

    private static void customizePackageInfos(Editor editor) {
        editor.replaceFile("src/main/java/com/azure/security/keyvault/keys/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * <p><a href=\"https://learn.microsoft.com/azure/key-vault/general/\">Azure Key Vault</a> is a cloud-based service\n"
                + " * provided by Microsoft Azure that allows users to securely store and manage cryptographic keys used for encrypting\n"
                + " * and decrypting data. It is a part of Azure Key Vault, which is a cloud-based service for managing cryptographic keys,\n"
                + " * secrets, and certificates.</p>\n"
                + " *\n"
                + " * <p>Azure Key Vault Keys provides a centralized and highly secure key management solution, allowing you to protect\n"
                + " * your keys and control access to them. It eliminates the need for storing keys in code or configuration files,\n"
                + " * reducing the risk of exposure and unauthorized access.</p>\n"
                + " *\n"
                + " * <p>With Azure Key Vault Keys, you can perform various operations on cryptographic keys, such as creating keys,\n"
                + " * importing existing keys, generating key pairs, encrypting data using keys, and decrypting data using keys.\n"
                + " * The service supports various key types and algorithms, including symmetric keys, asymmetric keys, and\n"
                + " * Elliptic Curve Cryptography (ECC) keys.</p>\n"
                + " *\n"
                + " * <p>The Azure Key Vault Keys client library allows developers to interact with the Azure Key Vault service\n"
                + " * from their applications. The library provides a set of APIs that enable developers to securely create keys,\n"
                + " * import existing keys, delete keys, retrieving key metadata, encrypting and decrypting data using keys,\n"
                + " * and signing and verifying signatures using keys.</p>\n"
                + " *\n"
                + " * <p><strong>Key Concepts:</strong></p>\n"
                + " *\n"
                + " * <p>What is a Key Client?</p>\n"
                + " * <p>The key client performs the interactions with the Azure Key Vault service for getting, setting, updating,\n"
                + " * deleting, and listing keys and its versions. Asynchronous (`KeyAsyncClient`) and synchronous (`KeyClient`) clients\n"
                + " * exist in the SDK allowing for the selection of a client based on an application's use case. Once you have\n"
                + " * initialized a key, you can interact with the primary resource types in Key Vault.</p>\n"
                + " *\n"
                + " * <p>What is an Azure Key Vault Key ?</p>\n"
                + " * <p>Azure Key Vault supports multiple key types (RSA and EC) and algorithms, and enables the use of\n"
                + " * Hardware Security Modules (HSM) for high value keys. In addition to the key material, the following attributes may\n"
                + " * be specified:</p>\n"
                + " *\n"
                + " * <ul>\n"
                + " *     <li>enabled: Specifies whether the key is enabled and usable for cryptographic operations.</li>\n"
                + " *     <li>notBefore: Identifies the time before which the key must not be used for cryptographic operations.</li>\n"
                + " *     <li>expires: Identifies the expiration time on or after which the key MUST NOT be used for cryptographic operations.</li>\n"
                + " *     <li>created: Indicates when this version of the key was created.</li>\n"
                + " *     <li>updated: Indicates when this version of the key was updated.</li>\n"
                + " * </ul>\n"
                + " *\n"
                + " * <h2>Getting Started</h2>\n"
                + " *\n"
                + " * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the\n"
                + " * {@link com.azure.security.keyvault.keys.KeyClient} class, a vault url and a credential object.</p>\n"
                + " *\n"
                + " * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,\n"
                + " * which is appropriate for most scenarios, including local development and production environments. Additionally,\n"
                + " * we recommend using a\n"
                + " * <a href=\"https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/\">\n"
                + " * managed identity</a> for authentication in production environments.\n"
                + " * You can find more information on different ways of authenticating and their corresponding credential types in the\n"
                + " * <a href=\"https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable\">\n"
                + " * Azure Identity documentation\"</a>.</p>\n"
                + " *\n"
                + " * <p><strong>Sample: Construct Synchronous Key Client</strong></p>\n"
                + " *\n"
                + " * <p>The following code sample demonstrates the creation of a {@link com.azure.security.keyvault.keys.KeyClient},\n"
                + " * using the {@link com.azure.security.keyvault.keys.KeyClientBuilder} to configure it.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.instantiation -->\n"
                + " * <pre>\n"
                + " * KeyClient keyClient = new KeyClientBuilder&#40;&#41;\n"
                + " *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;\n"
                + " *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;\n"
                + " *     .buildClient&#40;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.keys.KeyClient.instantiation -->\n"
                + " *\n"
                + " * <p><strong>Sample: Construct Asynchronous Key Client</strong></p>\n"
                + " *\n"
                + " * <p>The following code sample demonstrates the creation of a\n"
                + " * {@link com.azure.security.keyvault.keys.KeyClient}, using the\n"
                + " * {@link com.azure.security.keyvault.keys.KeyClientBuilder} to configure it.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.keys.KeyAsyncClient.instantiation -->\n"
                + " * <pre>\n"
                + " * KeyAsyncClient keyAsyncClient = new KeyClientBuilder&#40;&#41;\n"
                + " *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;\n"
                + " *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;\n"
                + " *     .buildAsyncClient&#40;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.keys.KeyAsyncClient.instantiation -->\n"
                + " *\n"
                + " * <br>\n"
                + " *\n"
                + " * <hr>\n"
                + " *\n"
                + " * <h2>Create a Cryptographic Key</h2>\n"
                + " * The {@link com.azure.security.keyvault.keys.KeyClient} or\n"
                + " * {@link com.azure.security.keyvault.keys.KeyAsyncClient} can be used to create a key in the key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously create a cryptographic key in the key vault,\n"
                + " * using the {@link com.azure.security.keyvault.keys.KeyClient#createKey(java.lang.String, com.azure.security.keyvault.keys.models.KeyType)} API.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.createKey#String-KeyType -->\n"
                + " * <pre>\n"
                + " * KeyVaultKey key = keyClient.createKey&#40;&quot;keyName&quot;, KeyType.EC&#41;;\n"
                + " * System.out.printf&#40;&quot;Created key with name: %s and id: %s%n&quot;, key.getName&#40;&#41;, key.getId&#40;&#41;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.keys.KeyClient.createKey#String-KeyType -->\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.keys.KeyAsyncClient}.</p>\n"
                + " *\n"
                + " * <br>\n"
                + " *\n"
                + " * <hr>\n"
                + " *\n"
                + " * <h2>Get a Cryptographic Key</h2>\n"
                + " * The {@link com.azure.security.keyvault.keys.KeyClient} or\n"
                + " * {@link com.azure.security.keyvault.keys.KeyAsyncClient} can be used to retrieve a key from the\n"
                + " * key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously retrieve a key from the key vault, using\n"
                + " * the {@link com.azure.security.keyvault.keys.KeyClient#getKey(java.lang.String)} API.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.getKey#String -->\n"
                + " * <pre>\n"
                + " * KeyVaultKey keyWithVersionValue = keyClient.getKey&#40;&quot;keyName&quot;&#41;;\n"
                + " *\n"
                + " * System.out.printf&#40;&quot;Retrieved key with name: %s and: id %s%n&quot;, keyWithVersionValue.getName&#40;&#41;,\n"
                + " *     keyWithVersionValue.getId&#40;&#41;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.keys.KeyClient.getKey#String -->\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.keys.KeyAsyncClient}.</p>\n"
                + " *\n"
                + " * <br>\n"
                + " *\n"
                + " * <hr>\n"
                + " *\n"
                + " * <h2>Delete a Cryptographic Key</h2>\n"
                + " * The {@link com.azure.security.keyvault.keys.KeyClient} or\n"
                + " * {@link com.azure.security.keyvault.keys.KeyAsyncClient} can be used to delete a key from the key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously delete a key from the\n"
                + " * key vault, using the {@link com.azure.security.keyvault.keys.KeyClient#beginDeleteKey(java.lang.String)} API.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.deleteKey#String -->\n"
                + " * <pre>\n"
                + " * SyncPoller&lt;DeletedKey, Void&gt; deleteKeyPoller = keyClient.beginDeleteKey&#40;&quot;keyName&quot;&#41;;\n"
                + " * PollResponse&lt;DeletedKey&gt; deleteKeyPollResponse = deleteKeyPoller.poll&#40;&#41;;\n"
                + " *\n"
                + " * &#47;&#47; Deleted date only works for SoftDelete Enabled Key Vault.\n"
                + " * DeletedKey deletedKey = deleteKeyPollResponse.getValue&#40;&#41;;\n"
                + " *\n"
                + " * System.out.printf&#40;&quot;Key delete date: %s%n&quot;, deletedKey.getDeletedOn&#40;&#41;&#41;;\n"
                + " * System.out.printf&#40;&quot;Deleted key's recovery id: %s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;;\n"
                + " *\n"
                + " * &#47;&#47; Key is being deleted on the server.\n"
                + " * deleteKeyPoller.waitForCompletion&#40;&#41;;\n"
                + " * &#47;&#47; Key is deleted\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.keys.KeyClient.deleteKey#String -->\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.keys.KeyAsyncClient}.</p>\n"
                + " *\n"
                + " * @see com.azure.security.keyvault.keys.KeyClient\n"
                + " * @see com.azure.security.keyvault.keys.KeyAsyncClient\n"
                + " * @see com.azure.security.keyvault.keys.KeyClientBuilder\n"
                + " */\n"
                + "package com.azure.security.keyvault.keys;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/keys/models/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the data models for Keys clients. The key vault client performs cryptographic key operations and\n"
                + " * vault operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.keys.models;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/keys/implementation/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the implementations for Keys clients. The key vault clients perform cryptographic key operations\n"
                + " * and vault operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.keys.implementation;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/keys/implementation/models/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the implementation data models for Keys clients. The Key Vault clients perform cryptographic key\n"
                + " * operations and vault operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.keys.implementation.models;\n");
    }
}
