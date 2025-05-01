// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure KeyVault's Secrets swagger code generation.
 */
public class SecretsCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        Editor editor = libraryCustomization.getRawEditor();

        removeFiles(editor);
        customizeModuleInfo(editor);
        customizePackageInfos(editor);
        customizeClientImpl(editor);
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to SecretServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/secrets/SecretClientBuilder.java");
    }

    private static void customizeClientImpl(Editor editor) {
        String classPath = "src/main/java/com/azure/security/keyvault/secrets/implementation/SecretClientImpl.java";
        String newFileContent = editor.getFileContent(classPath).replace("KeyVault", "Secret");

        editor.replaceFile(classPath, newFileContent);
    }

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "module com.azure.security.keyvault.secrets {\n"
                + "    requires transitive com.azure.core;\n"
                + "\n"
                + "    exports com.azure.security.keyvault.secrets;\n"
                + "    exports com.azure.security.keyvault.secrets.models;\n"
                + "\n"
                + "    opens com.azure.security.keyvault.secrets to com.azure.core;\n"
                + "    opens com.azure.security.keyvault.secrets.models to com.azure.core;\n"
                + "    opens com.azure.security.keyvault.secrets.implementation.models to com.azure.core;\n"
                + "}\n");
    }

    private static void customizePackageInfos(Editor editor) {
        editor.replaceFile("src/main/java/com/azure/security/keyvault/secrets/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * <p><a href=\"https://learn.microsoft.com/azure/key-vault/general/\">Azure Key Vault</a> is a cloud-based service\n"
                + " * provided by Microsoft Azure that allows users to store, manage, and access secrets, such as passwords, certificates,\n"
                + " * and other sensitive information, securely in the cloud. The service provides a centralized and secure location for\n"
                + " * storing secrets, which can be accessed by authorized applications and users with appropriate permissions.\n"
                + " * Azure Key Vault Secrets offers several key features, including:</p>\n"
                + " * <ul>\n"
                + " * <li>Secret management: It allows users to store, manage, and access secrets securely, and provides features such\n"
                + " * as versioning, backup, and restoration.</li>\n"
                + " * <li>Access control: It offers\n"
                + " * <a href = \"https://learn.microsoft.com/azure/key-vault/general/rbac-guide?tabs=azure-cli\">\n"
                + " * role-based access control (RBAC)</a> and enables users to grant specific permissions to access secrets to\n"
                + " * other users, applications, or services.</li>\n"
                + " * <li>Integration with other Azure services: Azure Key Vault Secrets can be integrated with other Azure services,\n"
                + " * such as Azure App Service, Azure Functions, and Azure Virtual Machines, to simplify the process of securing\n"
                + " * sensitive information.</li>\n"
                + " * <li>High availability and scalability: The service is designed to provide high availability and scalability,\n"
                + " * with the ability to handle large volumes of secrets and requests.</li>\n"
                + " * </ul>\n"
                + " *\n"
                + " * <p>The Azure Key Vault Secrets client library allows developers to interact with the Azure Key Vault service\n"
                + " * from their applications. The library provides a set of APIs that enable developers to securely store, manage, and\n"
                + " * retrieve secrets in a key vault, and supports operations such as creating, updating, deleting, and retrieving\n"
                + " * secrets.</p>\n"
                + " *\n"
                + " * <p><strong>Key Concepts:</strong></p>\n"
                + " *\n"
                + " * <p>What is a Secret Client?</p>\n"
                + " * <p>The secret client performs the interactions with the Azure Key Vault service for getting, setting, updating,\n"
                + " * deleting, and listing secrets and its versions. Asynchronous (SecretAsyncClient) and synchronous (SecretClient)\n"
                + " * clients exist in the SDK allowing for selection of a client based on an application's use case.\n"
                + " * Once you've initialized a secret, you can interact with the primary resource types in Key Vault.</p>\n"
                + " *\n"
                + " * <p>What is an Azure Key Vault Secret ?</p>\n"
                + " * <p>A secret is the fundamental resource within Azure Key Vault. From a developer's perspective, Key Vault APIs\n"
                + " * accept and return secret values as strings. In addition to the secret data, the following attributes may be\n"
                + " * specified:</p>\n"
                + " *\n"
                + " * <ol>\n"
                + " * <li>enabled: Specifies whether the secret data can be retrieved.</li>\n"
                + " * <li>notBefore: Identifies the time after which the secret will be active.</li>\n"
                + " * <li>expires: Identifies the expiration time on or after which the secret data should not be retrieved.</li>\n"
                + " * <li>created: Indicates when this version of the secret was created.</li>\n"
                + " * <li>updated: Indicates when this version of the secret was updated.</li>\n"
                + " * </ol>\n"
                + " *\n"
                + " * <h2>Getting Started</h2>\n"
                + " *\n"
                + " * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretClient} or\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretAsyncClient} class, a vault url and a credential object.</p>\n"
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
                + " * <p><strong>Sample: Construct Synchronous Secret Client</strong></p>\n"
                + " *\n"
                + " * <p>The following code sample demonstrates the creation of a {@link com.azure.security.keyvault.secrets.SecretClient},\n"
                + " * using the {@link com.azure.security.keyvault.secrets.SecretClientBuilder} to configure it.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.SecretClient.instantiation -->\n"
                + " * <pre>\n"
                + " * SecretClient secretClient = new SecretClientBuilder&#40;&#41;\n"
                + " *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;\n"
                + " *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;\n"
                + " *     .buildClient&#40;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.SecretClient.instantiation -->\n"
                + " *\n"
                + " * <p><strong>Sample: Construct Asynchronous Secret Client</strong></p>\n"
                + " *\n"
                + " * <p>The following code sample demonstrates the creation of a\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretAsyncClient}, using the\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretClientBuilder} to configure it.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.secrets.SecretAsyncClient.instantiation -->\n"
                + " * <pre>\n"
                + " * SecretAsyncClient secretAsyncClient = new SecretClientBuilder&#40;&#41;\n"
                + " *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;\n"
                + " *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;\n"
                + " *     .buildAsyncClient&#40;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.secrets.SecretAsyncClient.instantiation -->\n"
                + " *\n"
                + " * <hr/>\n"
                + " *\n"
                + " * <h2>Create a Secret</h2>\n"
                + " * The {@link com.azure.security.keyvault.secrets.SecretClient} or\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretAsyncClient} can be used to create a secret in the key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously create and store a secret in the key vault,\n"
                + " * using the {@link com.azure.security.keyvault.secrets.SecretClient#setSecret(java.lang.String, java.lang.String)} API.\n"
                + " * </p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.SecretClient.setSecret#string-string -->\n"
                + " * <pre>\n"
                + " * KeyVaultSecret secret = secretClient.setSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;;\n"
                + " * System.out.printf&#40;&quot;Secret is created with name %s and value %s%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.SecretClient.setSecret#string-string -->\n"
                + " *\n"
                + " * <p><strong>Asynchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to asynchronously create and store a secret in the key vault,\n"
                + " * using the {@link com.azure.security.keyvault.secrets.SecretAsyncClient}.</p>\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretAsyncClient}.</p>\n"
                + " *\n"
                + " * <hr/>\n"
                + " *\n"
                + " * <h2>Get a Secret</h2>\n"
                + " * The {@link com.azure.security.keyvault.secrets.SecretClient} or\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretAsyncClient} can be used to retrieve a secret from the\n"
                + " * key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously retrieve a previously stored secret from the\n"
                + " * key vault, using the {@link com.azure.security.keyvault.secrets.SecretClient#getSecret(java.lang.String)} API.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.SecretClient.getSecret#string -->\n"
                + " * <pre>\n"
                + " * KeyVaultSecret secret = secretClient.getSecret&#40;&quot;secretName&quot;&#41;;\n"
                + " * System.out.printf&#40;&quot;Secret is returned with name %s and value %s%n&quot;,\n"
                + " *     secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.SecretClient.getSecret#string -->\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretAsyncClient}.</p>\n"
                + " *\n"
                + " * <hr/>\n"
                + " *\n"
                + " * <h2>Delete a Secret</h2>\n"
                + " * The {@link com.azure.security.keyvault.secrets.SecretClient} or\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretAsyncClient} can be used to delete a secret from the\n"
                + " * key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously delete a secret from the\n"
                + " * key vault, using the {@link com.azure.security.keyvault.secrets.SecretClient#beginDeleteSecret(java.lang.String)}\n"
                + " * API.\n"
                + " * </p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.SecretClient.deleteSecret#String -->\n"
                + " * <pre>\n"
                + " * SyncPoller&lt;DeletedSecret, Void&gt; deleteSecretPoller = secretClient.beginDeleteSecret&#40;&quot;secretName&quot;&#41;;\n"
                + " *\n"
                + " * &#47;&#47; Deleted Secret is accessible as soon as polling begins.\n"
                + " * PollResponse&lt;DeletedSecret&gt; deleteSecretPollResponse = deleteSecretPoller.poll&#40;&#41;;\n"
                + " *\n"
                + " * &#47;&#47; Deletion date only works for a SoftDelete-enabled Key Vault.\n"
                + " * System.out.println&#40;&quot;Deleted Date  %s&quot; + deleteSecretPollResponse.getValue&#40;&#41;\n"
                + " *     .getDeletedOn&#40;&#41;.toString&#40;&#41;&#41;;\n"
                + " * System.out.printf&#40;&quot;Deleted Secret's Recovery Id %s&quot;, deleteSecretPollResponse.getValue&#40;&#41;\n"
                + " *     .getRecoveryId&#40;&#41;&#41;;\n"
                + " *\n"
                + " * &#47;&#47; Secret is being deleted on server.\n"
                + " * deleteSecretPoller.waitForCompletion&#40;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.SecretClient.deleteSecret#String -->\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.secrets.SecretAsyncClient}.</p>\n"
                + " *\n"
                + " * @see com.azure.security.keyvault.secrets.SecretClient\n"
                + " * @see com.azure.security.keyvault.secrets.SecretAsyncClient\n"
                + " * @see com.azure.security.keyvault.secrets.SecretClientBuilder\n"
                + " * @see com.azure.security.keyvault.secrets.models.KeyVaultSecret\n"
                + " */\n"
                + "package com.azure.security.keyvault.secrets;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/secrets/models/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the data models for Secrets clients. The Key Vault clients perform cryptographic key and vault\n"
                + " * operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.secrets.models;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/secrets/implementation/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the implementations for Secrets clients. The Key Vault clients perform cryptographic key\n"
                + " * operations and vault operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.secrets.implementation;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/secrets/implementation/models/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the implementation data models for Secrets clients. The Key Vault clients perform cryptographic\n"
                + " * key operations and vault operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.secrets.implementation.models;\n");
    }
}
