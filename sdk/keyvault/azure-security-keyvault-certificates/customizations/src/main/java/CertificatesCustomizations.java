// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;

import java.util.Arrays;

import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * Contains customizations for Azure Key Vault's Certificates code generation.
 */
public class CertificatesCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        removeFiles(libraryCustomization.getRawEditor());
        customizeError(libraryCustomization);
        customizeCertificateKeyType(libraryCustomization);
        customizeCertificateKeyUsage(libraryCustomization);
        customizeServiceVersion(libraryCustomization);
        customizeModuleInfo(libraryCustomization.getRawEditor());
        customizePackageInfos(libraryCustomization.getRawEditor());
    }

    private static void removeFiles(Editor editor) {
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateAsyncClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateClient.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/CertificateClientBuilder.java");
        editor.removeFile("src/main/java/com/azure/security/keyvault/certificates/KeyVaultServiceVersion.java");
    }

    private static void customizeError(LibraryCustomization customization) {
        String implModelsDirectory = "src/main/java/com/azure/security/keyvault/certificates/implementation/models/";
        String oldClassName = "KeyVaultErrorError";
        String modelsDirectory = "src/main/java/com/azure/security/keyvault/certificates/models/";
        String newClassName = "CertificateOperationError";

        // Rename KeyVaultErrorError to CertificateOperationError and move it to the public models package.
        String fileContent = customization.getRawEditor().getFileContent(implModelsDirectory + oldClassName + ".java");

        customization.getRawEditor().removeFile(implModelsDirectory + oldClassName + ".java");

        fileContent = fileContent.replace(oldClassName, newClassName)
            .replace("com.azure.security.keyvault.certificates.implementation.models",
                "com.azure.security.keyvault.certificates.models");
        customization.getRawEditor().addFile(modelsDirectory + newClassName + ".java", fileContent);

        // The class is used in CertificateOperation, which is in the implementation package.
        // Add an import to handle the class moving.
        customization.getClass("com.azure.security.keyvault.certificates.implementation.models", "CertificateOperation")
            .customizeAst(ast ->
                ast.addImport("com.azure.security.keyvault.certificates.models." + newClassName)
                    .getClassByName("CertificateOperation")
                    .ifPresent(clazz -> {
                        clazz.getFieldByName("error").ifPresent(field -> field.getVariable(0).setType(newClassName));
                        clazz.getMethodsByName("getError").forEach(method -> method.setType(newClassName));
                        clazz.getMethodsByName("setError").forEach(method -> method.getParameter(0).setType(newClassName));
                        clazz.getMethodsByName("fromJson").forEach(method -> method.getBody().ifPresent(body ->
                            method.setBody(StaticJavaParser.parseBlock(body.toString().replace(oldClassName, newClassName)))));
                    }));
    }

    private static void customizeCertificateKeyUsage(LibraryCustomization customization) {
        customization.getClass("com.azure.security.keyvault.certificates.models", "CertificateKeyUsage")
            .customizeAst(ast ->
                ast.getClassByName("CertificateKeyUsage").ifPresent(clazz ->
                    clazz.getFieldByName("C_RLSIGN").ifPresent(field ->
                        field.getVariable(0).setName("CRL_SIGN"))));
    }

    private static void customizeCertificateKeyType(LibraryCustomization customization) {
        customization.getClass("com.azure.security.keyvault.certificates.models", "CertificateKeyType")
            .customizeAst(ast ->
                ast.getClassByName("CertificateKeyType").ifPresent(clazz -> {
                    clazz.getFieldByName("OCT").ifPresent(field ->
                        field.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL));
                    clazz.getFieldByName("OCT_HSM").ifPresent(field ->
                        field.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL));
                }));
    }

    private static void customizeServiceVersion(LibraryCustomization customization) {
        CompilationUnit compilationUnit = new CompilationUnit();

        compilationUnit.addOrphanComment(new LineComment(" Copyright (c) Microsoft Corporation. All rights reserved."));
        compilationUnit.addOrphanComment(new LineComment(" Licensed under the MIT License."));
        compilationUnit.addOrphanComment(new LineComment(" Code generated by Microsoft (R) TypeSpec Code Generator."));

        compilationUnit.setPackageDeclaration("com.azure.security.keyvault.certificates")
            .addImport("com.azure.core.util.ServiceVersion");

        EnumDeclaration enumDeclaration = compilationUnit.addEnum("CertificateServiceVersion", Modifier.Keyword.PUBLIC)
            .addImplementedType("ServiceVersion")
            .setJavadocComment("The versions of Azure Key Vault Certificates supported by this client library.");

        for (String version : Arrays.asList("7.0", "7.1", "7.2", "7.3", "7.4", "7.5", "7.6")) {
            enumDeclaration.addEnumConstant("V" + version.replace('.', '_').replace('-', '_').toUpperCase())
                .setJavadocComment("Service version {@code " + version + "}.")
                .addArgument(new StringLiteralExpr(version));
        }

        enumDeclaration.addField("String", "version", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);

        enumDeclaration.addConstructor().addParameter("String", "version")
            .setBody(StaticJavaParser.parseBlock("{ this.version = version; }"));

        enumDeclaration.addMethod("getVersion", Modifier.Keyword.PUBLIC)
            .setType("String")
            .setJavadocComment("{@inheritDoc}")
            .addMarkerAnnotation("Override")
            .setBody(StaticJavaParser.parseBlock("{ return this.version; }"));

        enumDeclaration.addMethod("getLatest", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            .setType("CertificateServiceVersion")
            .setJavadocComment(new Javadoc(parseText("Gets the latest service version supported by this client library."))
                .addBlockTag("return", "The latest {@link CertificateServiceVersion}."))
            .setBody(StaticJavaParser.parseBlock("{ return V7_6; }"));

        customization.getRawEditor()
            .addFile("src/main/java/com/azure/security/keyvault/certificates/CertificateServiceVersion.java",
                compilationUnit.toString());

        String fileName = "src/main/java/com/azure/security/keyvault/certificates/implementation/CertificateClientImpl.java";
        String fileContent = customization.getRawEditor().getFileContent(fileName);
        fileContent = fileContent.replace("KeyVaultServiceVersion", "CertificateServiceVersion");
        customization.getRawEditor().replaceFile(fileName, fileContent);
    }

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java", joinWithNewline(
            "// Copyright (c) Microsoft Corporation. All rights reserved.",
            "// Licensed under the MIT License.",
            "",
            "module com.azure.security.keyvault.certificates {",
            "    requires transitive com.azure.core;",
            "",
            "    exports com.azure.security.keyvault.certificates;",
            "    exports com.azure.security.keyvault.certificates.models;",
            "",
            "    opens com.azure.security.keyvault.certificates to com.azure.core;",
            "    opens com.azure.security.keyvault.certificates.models to com.azure.core;",
            "    opens com.azure.security.keyvault.certificates.implementation.models to com.azure.core;",
            "}"));
    }

    private static void customizePackageInfos(Editor editor) {
        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/package-info.java", joinWithNewline(
            "// Copyright (c) Microsoft Corporation. All rights reserved.",
            "// Licensed under the MIT License.",
            "",
            "/**",
            " * <!-- @formatter:off -->",
            " * <p><a href=\"https://learn.microsoft.com/azure/certificate-vault/general/\">Azure Key Vault</a> is a cloud-based service",
            " * provided by Microsoft Azure that allows users to securely store and manage cryptographic certificates used for encrypting",
            " * and decrypting data. It is a part of Azure Key Vault, which is a cloud-based service for managing cryptographic certificates,",
            " * keys, and secrets.</p>",
            " *",
            " * <p>Azure Key Vault Certificates provides a centralized and highly secure location for storing certificates, which",
            " * eliminates the need to store sensitive certificate material in application code or configuration files.",
            " * By leveraging Azure Key Vault, you can better protect your certificates and ensure their availability",
            " * when needed.</p>",
            " *",
            " * <p>Key features of the Azure Key Vault Certificates service include:</p>",
            " *",
            " * <ul>",
            " *  <li>Secure storage: Certificates are stored securely within Azure Key Vault, which provides robust encryption",
            " *  and access control mechanisms to protect against unauthorized access.</li>",
            " *  <li>Certificate lifecycle management: You can create, import, and manage certificates within Azure Key Vault.",
            " *  It supports common certificate formats such as X.509 and PFX.</li>",
            " *  <li>Certificate management operations: Azure Key Vault provides a comprehensive set of management operations,",
            " *  including certificate creation, deletion, retrieval, renewal, and revocation.</li>",
            " *  <li>Integration with Azure services: Key Vault Certificates can be easily integrated with other Azure services,",
            " *  such as Azure App Service, Azure Functions, and Azure Virtual Machines, to enable secure authentication",
            " *  and encryption.</li>",
            " * </ul>",
            " *",
            " * <p>The Azure Key Vault Certificates client library allows developers to securely store and manage certificates",
            " * within Azure Key Vault. The library provides a set of APIs that enable developers to securely create, import,",
            " * retrieve, update, and perform other certificate-related operations.</p>",
            " *",
            " * <p><strong>Key Concepts:</strong></p>",
            " *",
            " * <p>What is a Certificate Client?</p>",
            " *",
            " * <p>The certificate client performs the interactions with the Azure Key Vault service for getting, setting, updating,",
            " * deleting, and listing certificates and its versions. Asynchronous (CertificateAsyncClient) and synchronous (CertificateClient) clients",
            " * exist in the SDK allowing for the selection of a client based on an application's use case. Once you have",
            " * initialized a certificate, you can interact with the primary resource types in Azure Key Vault.</p>",
            " *",
            " * <p>What is an Azure Key Vault Certificate ?</p>",
            " *",
            " * <p>Azure Key Vault supports certificates with secret content types (PKCS12 and PEM). The certificate can be",
            " * backed by keys in Azure Key Vault of types (EC and RSA). In addition to the certificate policy, the following",
            " * attributes may be specified:.</p>",
            " *",
            " * <ul>",
            " * <li>enabled: Specifies whether the certificate is enabled and usable.</li>",
            " * <li>created: Indicates when this version of the certificate was created.</li>",
            " * <li>updated: Indicates when this version of the certificate was updated.</li>",
            " * </ul>",
            " *",
            " * <h2>Getting Started</h2>",
            " *",
            " * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the",
            " * {@link com.azure.security.keyvault.certificates.CertificateClient} or {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} class, a vault url and a credential object.</p>",
            " *",
            " * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,",
            " * which is appropriate for most scenarios, including local development and production environments. Additionally,",
            " * we recommend using a",
            " * <a href=\"https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/\">",
            " * managed identity</a> for authentication in production environments.",
            " * You can find more information on different ways of authenticating and their corresponding credential types in the",
            " * <a href=\"https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable\">",
            " * Azure Identity documentation\"</a>.</p>",
            " *",
            " * <p><strong>Sample: Construct Synchronous Certificate Client</strong></p>",
            " *",
            " * <p>The following code sample demonstrates the creation of a {@link com.azure.security.keyvault.certificates.CertificateClient},",
            " * using the {@link com.azure.security.keyvault.certificates.CertificateClientBuilder} to configure it.</p>",
            " *",
            " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.instantiation -->",
            " * <pre>",
            " * CertificateClient certificateClient = new CertificateClientBuilder&#40;&#41;",
            " *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;",
            " *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;",
            " *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;",
            " *     .buildClient&#40;&#41;;",
            " * </pre>",
            " * <!-- end com.azure.security.keyvault.certificates.CertificateClient.instantiation  -->",
            " *",
            " * <p><strong>Sample: Construct Asynchronous Certificate Client</strong></p>",
            " *",
            " * <p>The following code sample demonstrates the creation of a",
            " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}, using the",
            " * {@link com.azure.security.keyvault.certificates.CertificateClientBuilder} to configure it.</p>",
            " *",
            " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation -->",
            " * <pre>",
            " * CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder&#40;&#41;",
            " *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;",
            " *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;",
            " *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;",
            " *     .buildAsyncClient&#40;&#41;;",
            " * </pre>",
            " * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation -->",
            " *",
            " * <br/>",
            " *",
            " * <hr/>",
            " *",
            " * <h2>Create a Certificate</h2>",
            " * The {@link com.azure.security.keyvault.certificates.CertificateClient} or",
            " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to create a certificate in",
            " * the key vault.",
            " *",
            " * <p><strong>Synchronous Code Sample:</strong></p>",
            " * <p>The following code sample demonstrates how to synchronously create a certificate in the key vault,",
            " * using the {@link com.azure.security.keyvault.certificates.CertificateClient#beginCreateCertificate(java.lang.String, com.azure.security.keyvault.certificates.models.CertificatePolicy)} API.</p>",
            " *",
            " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->",
            " * <pre>",
            " * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;",
            " *",
            " * SyncPoller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certPoller =",
            " *     certificateClient.beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;;",
            " *",
            " * certPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;",
            " *",
            " * KeyVaultCertificate cert = certPoller.getFinalResult&#40;&#41;;",
            " *",
            " * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;",
            " * </pre>",
            " * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->",
            " *",
            " * <p><strong>Note:</strong> For the asynchronous sample, refer to",
            " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>",
            " *",
            " * <br/>",
            " *",
            " * <hr/>",
            " *",
            " * <h2>Get a Certificate</h2>",
            " * The {@link com.azure.security.keyvault.certificates.CertificateClient} or",
            " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to retrieve a certificate from the",
            " * key vault.",
            " *",
            " * <p><strong>Synchronous Code Sample:</strong></p>",
            " * <p>The following code sample demonstrates how to synchronously retrieve a certificate from the key vault, using",
            " * the {@link com.azure.security.keyvault.certificates.CertificateClient#getCertificate(java.lang.String)}.</p>",
            " *",
            " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->",
            " * <pre>",
            " * CertificatePolicy policy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;",
            " * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;, policy.getSubject&#40;&#41;&#41;;",
            " * </pre>",
            " * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->",
            " *",
            " * <p><strong>Note:</strong> For the asynchronous sample, refer to",
            " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>",
            " *",
            " * <br/>",
            " *",
            " * <hr/>",
            " *",
            " * <h2>Delete a Certificate</h2>",
            " * The {@link com.azure.security.keyvault.certificates.CertificateClient} or",
            " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to delete a certificate from",
            " * the key vault.",
            " *",
            " * <p><strong>Synchronous Code Sample:</strong></p>",
            " * <p>The following code sample demonstrates how to synchronously delete a certificate from the",
            " * key vault, using the {@link com.azure.security.keyvault.certificates.CertificateClient#beginDeleteCertificate(java.lang.String)} API.</p>",
            " *",
            " * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->",
            " * <pre>",
            " * SyncPoller&lt;DeletedCertificate, Void&gt; deleteCertPoller =",
            " *     certificateClient.beginDeleteCertificate&#40;&quot;certificateName&quot;&#41;;",
            " * &#47;&#47; Deleted Certificate is accessible as soon as polling beings.",
            " * PollResponse&lt;DeletedCertificate&gt; deleteCertPollResponse = deleteCertPoller.poll&#40;&#41;;",
            " * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;,",
            " *     deleteCertPollResponse.getValue&#40;&#41;.getName&#40;&#41;, deleteCertPollResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;",
            " * deleteCertPoller.waitForCompletion&#40;&#41;;",
            " * </pre>",
            " * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->",
            " *",
            " * <p><strong>Note:</strong> For the asynchronous sample, refer to",
            " * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>",
            " *",
            " * @see com.azure.security.keyvault.certificates.CertificateClient",
            " * @see com.azure.security.keyvault.certificates.CertificateAsyncClient",
            " * @see com.azure.security.keyvault.certificates.CertificateClientBuilder",
            " */",
            "package com.azure.security.keyvault.certificates;",
            ""));

        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/models/package-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "",
                "/**",
                " * <!-- @formatter:off -->",
                " * Package containing the data models for Certificates clients. The key vault client performs cryptographic key and",
                " * vault operations against the Key Vault service.",
                " */",
                "package com.azure.security.keyvault.certificates.models;",
                ""));

        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/implementation/package-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "",
                "/**",
                " * <!-- @formatter:off -->",
                " * Package containing the implementations for Certificates clients. The key vault client performs cryptographic key",
                " * operations and vault operations against the Key Vault service.",
                " */",
                "package com.azure.security.keyvault.certificates.implementation;",
                ""));

        editor.replaceFile("src/main/java/com/azure/security/keyvault/certificates/implementation/models/package-info.java",
            joinWithNewline(
                "// Copyright (c) Microsoft Corporation. All rights reserved.",
                "// Licensed under the MIT License.",
                "",
                "/**",
                " * <!-- @formatter:off -->",
                " * Package containing the implementation data models for Certificates clients. The key vault client performs",
                " * cryptographic key operations and vault operations against the Key Vault service.",
                " */",
                "package com.azure.security.keyvault.certificates.implementation.models;",
                ""));
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
