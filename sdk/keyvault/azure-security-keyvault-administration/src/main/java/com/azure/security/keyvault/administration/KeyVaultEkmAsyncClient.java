// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils;
import com.azure.security.keyvault.administration.implementation.models.EkmConnection;
import com.azure.security.keyvault.administration.implementation.models.EkmProxyClientCertificateInfo;
import com.azure.security.keyvault.administration.implementation.models.EkmProxyInfo;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyClientCertificateInfo;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyInfo;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.EMPTY_OPTIONS;

/**
 * The {@link KeyVaultEkmAsyncClient} provides asynchronous methods to manage External Key Manager (EKM) connections
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link KeyVaultEkmAsyncClient} class, a vault url and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally,
 * we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">
 * managed identity</a> for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">
 * Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Asynchronous EKM Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultEkmAsyncClient}, using the
 * {@link KeyVaultEkmClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.instantiation -->
 * <pre>
 * KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = new KeyVaultEkmClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get an EKM Connection</h2>
 * The {@link KeyVaultEkmAsyncClient} can be used to retrieve the EKM connection from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously retrieve the EKM connection from the key vault,
 * using the {@link KeyVaultEkmAsyncClient#getEkmConnection()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmConnection -->
 * <pre>
 * keyVaultEkmAsyncClient.getEkmConnection&#40;&#41;
 *     .subscribe&#40;ekmConnection -&gt;
 *         System.out.printf&#40;&quot;Retrieved EKM connection with host '%s'.%n&quot;, ekmConnection.getHost&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmConnection -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultEkmClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Create an EKM Connection</h2>
 * The {@link KeyVaultEkmAsyncClient} can be used to create an EKM connection in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously create an EKM connection in the key vault, using the
 * {@link KeyVaultEkmAsyncClient#createEkmConnection(KeyVaultEkmConnection)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.createEkmConnection#KeyVaultEkmConnection -->
 * <pre>
 * KeyVaultEkmConnection ekmConnectionToCreate =
 *     new KeyVaultEkmConnection&#40;&quot;&lt;ekm-proxy-host&gt;&quot;,
 *         Collections.singletonList&#40;&quot;&lt;server-ca-certificate&gt;&quot;.getBytes&#40;&#41;&#41;&#41;
 *         .setPathPrefix&#40;&quot;&lt;path-prefix&gt;&quot;&#41;
 *         .setServerSubjectCommonName&#40;&quot;&lt;server-subject-common-name&gt;&quot;&#41;;
 *
 * keyVaultEkmAsyncClient.createEkmConnection&#40;ekmConnectionToCreate&#41;
 *     .subscribe&#40;createdEkmConnection -&gt;
 *         System.out.printf&#40;&quot;Created EKM connection with host '%s'.%n&quot;, createdEkmConnection.getHost&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.createEkmConnection#KeyVaultEkmConnection -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultEkmClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Update an EKM Connection</h2>
 * The {@link KeyVaultEkmAsyncClient} can be used to update an existing EKM connection in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously update an EKM connection in the key vault, using the
 * {@link KeyVaultEkmAsyncClient#updateEkmConnection(KeyVaultEkmConnection)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.updateEkmConnection#KeyVaultEkmConnection -->
 * <pre>
 * KeyVaultEkmConnection ekmConnectionToUpdate =
 *     new KeyVaultEkmConnection&#40;&quot;&lt;ekm-proxy-host&gt;&quot;,
 *         Collections.singletonList&#40;&quot;&lt;server-ca-certificate&gt;&quot;.getBytes&#40;&#41;&#41;&#41;
 *         .setPathPrefix&#40;&quot;&lt;path-prefix&gt;&quot;&#41;
 *         .setServerSubjectCommonName&#40;&quot;&lt;server-subject-common-name&gt;&quot;&#41;;
 *
 * keyVaultEkmAsyncClient.updateEkmConnection&#40;ekmConnectionToUpdate&#41;
 *     .subscribe&#40;updatedEkmConnection -&gt;
 *         System.out.printf&#40;&quot;Updated EKM connection with host '%s'.%n&quot;, updatedEkmConnection.getHost&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.updateEkmConnection#KeyVaultEkmConnection -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultEkmClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete an EKM Connection</h2>
 * The {@link KeyVaultEkmAsyncClient} can be used to delete the EKM connection from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously delete the EKM connection from the key vault, using
 * the {@link KeyVaultEkmAsyncClient#deleteEkmConnection()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.deleteEkmConnection -->
 * <pre>
 * keyVaultEkmAsyncClient.deleteEkmConnection&#40;&#41;
 *     .subscribe&#40;deletedEkmConnection -&gt;
 *         System.out.printf&#40;&quot;Deleted EKM connection with host '%s'.%n&quot;, deletedEkmConnection.getHost&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.deleteEkmConnection -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultEkmClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Check an EKM Connection</h2>
 * The {@link KeyVaultEkmAsyncClient} can be used to check the connectivity and authentication with the EKM proxy.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously check the EKM connection, using the
 * {@link KeyVaultEkmAsyncClient#checkEkmConnection()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.checkEkmConnection -->
 * <pre>
 * keyVaultEkmAsyncClient.checkEkmConnection&#40;&#41;
 *     .subscribe&#40;ekmProxyInfo -&gt;
 *         System.out.printf&#40;&quot;Checked EKM connection. Proxy vendor: '%s', proxy name: '%s'.%n&quot;,
 *             ekmProxyInfo.getProxyVendor&#40;&#41;, ekmProxyInfo.getProxyName&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.checkEkmConnection -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultEkmClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get the EKM Proxy Client Certificate</h2>
 * The {@link KeyVaultEkmAsyncClient} can be used to retrieve the EKM proxy client certificate from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously retrieve the EKM proxy client certificate, using the
 * {@link KeyVaultEkmAsyncClient#getEkmCertificate()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmCertificate -->
 * <pre>
 * keyVaultEkmAsyncClient.getEkmCertificate&#40;&#41;
 *     .subscribe&#40;certificateInfo -&gt;
 *         System.out.printf&#40;&quot;Retrieved EKM proxy client certificate with subject common name '%s'.%n&quot;,
 *             certificateInfo.getSubjectCommonName&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmCertificate -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultEkmClient}.</p>
 *
 * @see com.azure.security.keyvault.administration
 * @see KeyVaultEkmClientBuilder
 */
@ServiceClient(
    builder = KeyVaultEkmClientBuilder.class,
    isAsync = true,
    serviceInterfaces = KeyVaultAdministrationClientImpl.KeyVaultAdministrationClientService.class)
public final class KeyVaultEkmAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultEkmAsyncClient.class);
    private final KeyVaultAdministrationClientImpl implClient;

    /**
     * Creates a {@link KeyVaultEkmAsyncClient} that uses a {@link KeyVaultAdministrationClientImpl} to service requests.
     *
     * @param implClient The implementation client used to service requests.
     */
    KeyVaultEkmAsyncClient(KeyVaultAdministrationClientImpl implClient) {
        this.implClient = implClient;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.implClient.getHttpPipeline();
    }

    /**
     * Gets the {@link KeyVaultEkmConnection EKM connection}. This operation requires the
     * {@code ekm/read} permission.
     *
     * @return A {@link Mono} containing the {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultEkmConnection> getEkmConnection() {
        try {
            return implClient.getEkmConnectionWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Gets the {@link KeyVaultEkmConnection EKM connection}. This operation requires the
     * {@code ekm/read} permission.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultEkmConnection>> getEkmConnectionWithResponse() {
        try {
            return implClient.getEkmConnectionWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response,
                    transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Creates the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection already
     * exists, this operation fails. This operation requires the {@code ekm/write} permission.
     *
     * @param ekmConnection The {@link KeyVaultEkmConnection EKM connection} to create.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws NullPointerException if {@code ekmConnection} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultEkmConnection> createEkmConnection(KeyVaultEkmConnection ekmConnection) {
        Objects.requireNonNull(ekmConnection,
            String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'ekmConnection'"));

        try {
            return implClient
                .createEkmConnectionWithResponseAsync(BinaryData.fromObject(transformToEkmConnection(ekmConnection)),
                    EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Creates the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection already
     * exists, this operation fails. This operation requires the {@code ekm/write} permission.
     *
     * @param ekmConnection The {@link KeyVaultEkmConnection EKM connection} to create.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws NullPointerException if {@code ekmConnection} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultEkmConnection>> createEkmConnectionWithResponse(KeyVaultEkmConnection ekmConnection) {
        Objects.requireNonNull(ekmConnection,
            String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'ekmConnection'"));

        try {
            return implClient
                .createEkmConnectionWithResponseAsync(BinaryData.fromObject(transformToEkmConnection(ekmConnection)),
                    EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response,
                    transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Updates the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection does not
     * exist, this operation fails. This operation requires the {@code ekm/write} permission.
     *
     * @param ekmConnection The {@link KeyVaultEkmConnection EKM connection} to update.
     *
     * @return A {@link Mono} containing the updated {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws NullPointerException if {@code ekmConnection} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultEkmConnection> updateEkmConnection(KeyVaultEkmConnection ekmConnection) {
        Objects.requireNonNull(ekmConnection,
            String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'ekmConnection'"));

        try {
            return implClient
                .updateEkmConnectionWithResponseAsync(BinaryData.fromObject(transformToEkmConnection(ekmConnection)),
                    EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Updates the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection does not
     * exist, this operation fails. This operation requires the {@code ekm/write} permission.
     *
     * @param ekmConnection The {@link KeyVaultEkmConnection EKM connection} to update.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws NullPointerException if {@code ekmConnection} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultEkmConnection>> updateEkmConnectionWithResponse(KeyVaultEkmConnection ekmConnection) {
        Objects.requireNonNull(ekmConnection,
            String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'ekmConnection'"));

        try {
            return implClient
                .updateEkmConnectionWithResponseAsync(BinaryData.fromObject(transformToEkmConnection(ekmConnection)),
                    EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response,
                    transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Deletes the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection does not
     * exist, this operation fails. This operation requires the {@code ekm/delete} permission.
     *
     * @return A {@link Mono} containing the deleted {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultEkmConnection> deleteEkmConnection() {
        try {
            return implClient.deleteEkmConnectionWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Deletes the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection does not
     * exist, this operation fails. This operation requires the {@code ekm/delete} permission.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the deleted
     * {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultEkmConnection>> deleteEkmConnectionWithResponse() {
        try {
            return implClient.deleteEkmConnectionWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response,
                    transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Checks the connectivity and authentication with the EKM proxy. This operation requires the {@code ekm/read}
     * permission.
     *
     * @return A {@link Mono} containing the {@link KeyVaultEkmProxyInfo EKM proxy information}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultEkmProxyInfo> checkEkmConnection() {
        try {
            return implClient.checkEkmConnectionWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> transformToKeyVaultEkmProxyInfo(response.getValue().toObject(EkmProxyInfo.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Checks the connectivity and authentication with the EKM proxy. This operation requires the {@code ekm/read}
     * permission.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultEkmProxyInfo EKM proxy information}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultEkmProxyInfo>> checkEkmConnectionWithResponse() {
        try {
            return implClient.checkEkmConnectionWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response,
                    transformToKeyVaultEkmProxyInfo(response.getValue().toObject(EkmProxyInfo.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Gets the EKM proxy client certificate used to authenticate to the EKM proxy. This operation requires the
     * {@code ekm/read} permission.
     *
     * @return A {@link Mono} containing the {@link KeyVaultEkmProxyClientCertificateInfo EKM proxy client certificate
     * information}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultEkmProxyClientCertificateInfo> getEkmCertificate() {
        try {
            return implClient.getEkmCertificateWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> transformToKeyVaultEkmProxyClientCertificateInfo(
                    response.getValue().toObject(EkmProxyClientCertificateInfo.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Gets the EKM proxy client certificate used to authenticate to the EKM proxy. This operation requires the
     * {@code ekm/read} permission.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultEkmProxyClientCertificateInfo EKM proxy client certificate information}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultEkmProxyClientCertificateInfo>> getEkmCertificateWithResponse() {
        try {
            return implClient.getEkmCertificateWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response, transformToKeyVaultEkmProxyClientCertificateInfo(
                    response.getValue().toObject(EkmProxyClientCertificateInfo.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    static KeyVaultEkmConnection transformToKeyVaultEkmConnection(EkmConnection ekmConnection) {
        return new KeyVaultEkmConnection(ekmConnection.getHost(), ekmConnection.getServerCaCertificates())
            .setPathPrefix(ekmConnection.getPathPrefix())
            .setServerSubjectCommonName(ekmConnection.getServerSubjectCommonName());
    }

    static EkmConnection transformToEkmConnection(KeyVaultEkmConnection ekmConnection) {
        return new EkmConnection(ekmConnection.getHost(), ekmConnection.getServerCaCertificates())
            .setPathPrefix(ekmConnection.getPathPrefix())
            .setServerSubjectCommonName(ekmConnection.getServerSubjectCommonName());
    }

    static KeyVaultEkmProxyInfo transformToKeyVaultEkmProxyInfo(EkmProxyInfo ekmProxyInfo) {
        return new KeyVaultEkmProxyInfo(ekmProxyInfo.getApiVersion(), ekmProxyInfo.getProxyVendor(),
            ekmProxyInfo.getProxyName(), ekmProxyInfo.getEkmVendor(), ekmProxyInfo.getEkmProduct());
    }

    static KeyVaultEkmProxyClientCertificateInfo
        transformToKeyVaultEkmProxyClientCertificateInfo(EkmProxyClientCertificateInfo certificateInfo) {
        return new KeyVaultEkmProxyClientCertificateInfo(certificateInfo.getCaCertificates(),
            certificateInfo.getSubjectCommonName());
    }
}
