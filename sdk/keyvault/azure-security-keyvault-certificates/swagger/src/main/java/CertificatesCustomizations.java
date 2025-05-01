// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure KeyVault's Certificates swagger code generation.
 */
public class CertificatesCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        Editor editor = libraryCustomization.getRawEditor();

        removeFiles(editor);
        customizeModuleInfo(editor);
        customizePackageInfos(editor);
        customizeClientImpl(libraryCustomization);
        customizeError(libraryCustomization);
        customizeCertificateKeyUsage(libraryCustomization);
    }

    private static void removeFiles(Editor editor) {
        // Remove the next line in favor of renaming to CertificateServiceVersion once the TSP spec includes all service
        // versions.
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/KeyVaultServiceVersion.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateClientBuilder.java");
    }

    private static void customizeError(LibraryCustomization libraryCustomization) {
        // Rename error class.
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.certificates.implementation.models")
            .getClass("KeyVaultErrorError")
            .rename("CertificateOperationError")
            .customizeAst(ast ->
                ast.getPackageDeclaration().ifPresent(packageDeclaration ->
                    packageDeclaration.setName("com.azure.security.keyvault.certificates.models")));

        String classPath = "src/main/java/com/azure/security/keyvault/certificates/implementation/models/"
            + "CertificateOperationError.java";

        replaceInFile(classCustomization, classPath,
            new String[] { "KeyVaultErrorError" },
            new String[] { "CertificateOperationError" });

        // Move it to public package.
        libraryCustomization
            .getRawEditor()
            .renameFile(classPath,
                "src/main/java/com/azure/security/keyvault/certificates/models/CertificateOperationError.java");

        // Replace instances in impl CertificateOperationError and add import statement.
        classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.certificates.implementation.models")
            .getClass("CertificateOperation")
            .addImports("com.azure.security.keyvault.certificates.models.CertificateOperationError");
        classPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/models/CertificateOperation.java";

        replaceInFile(classCustomization, classPath,
            new String[] { "KeyVaultErrorError" },
            new String[] { "CertificateOperationError" });
    }

    private static void customizeCertificateKeyUsage(LibraryCustomization libraryCustomization) {
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.certificates.models")
            .getClass("CertificateKeyUsage");
        String classPath =
            "src/main/java/com/azure/security/keyvault/certificates/models/CertificateKeyUsage.java";

        replaceInFile(classCustomization, classPath, new String[] { "C_RLSIGN" }, new String[] { "CRL_SIGN" });
    }

    private static void customizeClientImpl(LibraryCustomization libraryCustomization) {
        ClassCustomization classCustomization = libraryCustomization
            .getPackage("com.azure.security.keyvault.certificates.implementation")
            .getClass("CertificateClientImpl");
        String classPath =
            "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientImpl.java";

        replaceInFile(classCustomization, classPath, new String[] { "KeyVault" }, new String[] { "Certificate" });
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

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "module com.azure.security.keyvault.certificates {\n"
                + "    requires transitive com.azure.core;\n"
                + "\n"
                + "    exports com.azure.security.keyvault.certificates;\n"
                + "    exports com.azure.security.keyvault.certificates.models;\n"
                + "\n"
                + "    opens com.azure.security.keyvault.certificates to com.azure.core;\n"
                + "    opens com.azure.security.keyvault.certificates.models to com.azure.core;\n"
                + "    opens com.azure.security.keyvault.certificates.implementation.models to com.azure.core;\n"
                + "}\n");
    }

    private static void customizePackageInfos(Editor editor) {
        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * <p><a href=\"https://learn.microsoft.com/azure/certificate-vault/general/\">Azure Key Vault</a> is a cloud-based service\n"
                + " * provided by Microsoft Azure that allows users to securely store and manage cryptographic certificates used for encrypting\n"
                + " * and decrypting data. It is a part of Azure Key Vault, which is a cloud-based service for managing cryptographic certificates,\n"
                + " * keys, and secrets.</p>\n"
                + " *\n"
                + " * <p>Azure Key Vault Certificates provides a centralized and highly secure location for storing certificates, which\n"
                + " * eliminates the need to store sensitive certificate material in application code or configuration files.\n"
                + " * By leveraging Azure Key Vault, you can better protect your certificates and ensure their availability\n"
                + " * when needed.</p>\n"
                + " *\n"
                + " * <p>Key features of the Azure Key Vault Certificates service include:</p>\n"
                + " *\n"
                + " * <ul>\n"
                + " *  <li>Secure storage: Certificates are stored securely within Azure Key Vault, which provides robust encryption\n"
                + " *  and access control mechanisms to protect against unauthorized access.</li>\n"
                + " *  <li>Certificate lifecycle management: You can create, import, and manage certificates within Azure Key Vault.\n"
                + " *  It supports common certificate formats such as X.509 and PFX.</li>\n"
                + " *  <li>Certificate management operations: Azure Key Vault provides a comprehensive set of management operations,\n"
                + " *  including certificate creation, deletion, retrieval, renewal, and revocation.</li>\n"
                + " *  <li>Integration with Azure services: Key Vault Certificates can be easily integrated with other Azure services,\n"
                + " *  such as Azure App Service, Azure Functions, and Azure Virtual Machines, to enable secure authentication\n"
                + " *  and encryption.</li>\n"
                + " * </ul>\n"
                + " *\n"
                + " * <p>The Azure Key Vault Certificates client library allows developers to securely store and manage certificates\n"
                + " * within Azure Key Vault. The library provides a set of APIs that enable developers to securely create, import,\n"
                + " * retrieve, update, and perform other certificate-related operations.</p>\n"
                + " *\n"
                + " * <p><strong>Key Concepts:</strong></p>\n"
                + " *\n"
                + " * <p>What is a Certificate Client?</p>\n"
                + " *\n"
                + " * <p>The certificate client performs the interactions with the Azure Key Vault service for getting, setting, updating,\n"
                + " * deleting, and listing certificates and its versions. Asynchronous (CertificateAsyncClient) and synchronous (CertificateClient) clients\n"
                + " * exist in the SDK allowing for the selection of a client based on an application's use case. Once you have\n"
                + " * initialized a certificate, you can interact with the primary resource types in Azure Key Vault.</p>\n"
                + " *\n"
                + " * <p>What is an Azure Key Vault Certificate ?</p>\n"
                + " *\n"
                + " * <p>Azure Key Vault supports certificates with secret content types (PKCS12 and PEM). The certificate can be\n"
                + " * backed by keys in Azure Key Vault of types (EC and RSA). In addition to the certificate policy, the following\n"
                + " * attributes may be specified:.</p>\n"
                + " *\n"
                + " * <ul>\n"
                + " * <li>enabled: Specifies whether the certificate is enabled and usable.</li>\n"
                + " * <li>created: Indicates when this version of the certificate was created.</li>\n"
                + " * <li>updated: Indicates when this version of the certificate was updated.</li>\n"
                + " * </ul>\n"
                + " *\n"
                + " * <h2>Getting Started</h2>\n"
                + " *\n"
                + " * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateClient} or {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} class, a vault url and a credential object.</p>\n"
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
                + " * <p><strong>Sample: Construct Synchronous Certificate Client</strong></p>\n"
                + " *\n"
                + " * <p>The following code sample demonstrates the creation of a {@link com.azure.security.keyvault.certificates.CertificateClient},\n"
                + " * using the {@link com.azure.security.keyvault.certificates.CertificateClientBuilder} to configure it.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.instantiation -->\n"
                + " * <pre>\n"
                + " * CertificateClient certificateClient = new CertificateClientBuilder&#40;&#41;\n"
                + " *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;\n"
                + " *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;\n"
                + " *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;\n"
                + " *     .buildClient&#40;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.certificates.CertificateClient.instantiation  -->\n"
                + " *\n"
                + " * <p><strong>Sample: Construct Asynchronous Certificate Client</strong></p>\n"
                + " *\n"
                + " * <p>The following code sample demonstrates the creation of a\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}, using the\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateClientBuilder} to configure it.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation -->\n"
                + " * <pre>\n"
                + " * CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder&#40;&#41;\n"
                + " *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;\n"
                + " *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;\n"
                + " *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;\n"
                + " *     .buildAsyncClient&#40;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation -->\n"
                + " *\n"
                + " * <br/>\n"
                + " *\n"
                + " * <hr/>\n"
                + " *\n"
                + " * <h2>Create a Certificate</h2>\n"
                + " * The {@link com.azure.security.keyvault.certificates.CertificateClient} or\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to create a certificate in\n"
                + " * the key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously create a certificate in the key vault,\n"
                + " * using the {@link com.azure.security.keyvault.certificates.CertificateClient#beginCreateCertificate(java.lang.String, com.azure.security.keyvault.certificates.models.CertificatePolicy)} API.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->\n"
                + " * <pre>\n"
                + " * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;,\n"
                + " *     &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;\n"
                + " * SyncPoller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certPoller = certificateClient\n"
                + " *     .beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;;\n"
                + " * certPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;\n"
                + " * KeyVaultCertificate cert = certPoller.getFinalResult&#40;&#41;;\n"
                + " * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>\n"
                + " *\n"
                + " * <br/>\n"
                + " *\n"
                + " * <hr/>\n"
                + " *\n"
                + " * <h2>Get a Certificate</h2>\n"
                + " * The {@link com.azure.security.keyvault.certificates.CertificateClient} or\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to retrieve a certificate from the\n"
                + " * key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously retrieve a certificate from the key vault, using\n"
                + " * the {@link com.azure.security.keyvault.certificates.CertificateClient#getCertificate(java.lang.String)}.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->\n"
                + " * <pre>\n"
                + " * CertificatePolicy policy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;\n"
                + " * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;, policy.getSubject&#40;&#41;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>\n"
                + " *\n"
                + " * <br/>\n"
                + " *\n"
                + " * <hr/>\n"
                + " *\n"
                + " * <h2>Delete a Certificate</h2>\n"
                + " * The {@link com.azure.security.keyvault.certificates.CertificateClient} or\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to delete a certificate from\n"
                + " * the key vault.\n"
                + " *\n"
                + " * <p><strong>Synchronous Code Sample:</strong></p>\n"
                + " * <p>The following code sample demonstrates how to synchronously delete a certificate from the\n"
                + " * key vault, using the {@link com.azure.security.keyvault.certificates.CertificateClient#beginDeleteCertificate(java.lang.String)} API.</p>\n"
                + " *\n"
                + " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->\n"
                + " * <pre>\n"
                + " * SyncPoller&lt;DeletedCertificate, Void&gt; deleteCertPoller =\n"
                + " *     certificateClient.beginDeleteCertificate&#40;&quot;certificateName&quot;&#41;;\n"
                + " * &#47;&#47; Deleted Certificate is accessible as soon as polling beings.\n"
                + " * PollResponse&lt;DeletedCertificate&gt; deleteCertPollResponse = deleteCertPoller.poll&#40;&#41;;\n"
                + " * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;,\n"
                + " *     deleteCertPollResponse.getValue&#40;&#41;.getName&#40;&#41;, deleteCertPollResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;\n"
                + " * deleteCertPoller.waitForCompletion&#40;&#41;;\n"
                + " * </pre>\n"
                + " * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->\n"
                + " *\n"
                + " * <p><strong>Note:</strong> For the asynchronous sample, refer to\n"
                + " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>\n"
                + " *\n"
                + " * @see com.azure.security.keyvault.certificates.CertificateClient\n"
                + " * @see com.azure.security.keyvault.certificates.CertificateAsyncClient\n"
                + " * @see com.azure.security.keyvault.certificates.CertificateClientBuilder\n"
                + " */\n"
                + "package com.azure.security.keyvault.certificates;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/models/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the data models for Certificates clients. The key vault client performs cryptographic key and\n"
                + " * vault operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.certificates.models;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/implementation/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the implementations for Certificates clients. The key vault client performs cryptographic key\n"
                + " * operations and vault operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.certificates.implementation;\n");

        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/implementation/models/package-info.java",
            "// Copyright (c) Microsoft Corporation. All rights reserved.\n"
                + "// Licensed under the MIT License.\n"
                + "\n"
                + "/**\n"
                + " * <!-- @formatter:off -->\n"
                + " * Package containing the implementation data models for Certificates clients. The key vault client performs\n"
                + " * cryptographic key operations and vault operations against the Key Vault service.\n"
                + " */\n"
                + "package com.azure.security.keyvault.certificates.implementation.models;\n");
    }
}
