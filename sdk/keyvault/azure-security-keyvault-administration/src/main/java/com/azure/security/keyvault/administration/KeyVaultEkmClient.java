// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationClientImpl;
import com.azure.security.keyvault.administration.implementation.models.EkmConnection;
import com.azure.security.keyvault.administration.implementation.models.EkmProxyClientCertificateInfo;
import com.azure.security.keyvault.administration.implementation.models.EkmProxyInfo;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyClientCertificateInfo;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyInfo;

import java.util.Objects;

import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.EMPTY_OPTIONS;
import static com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.transformToEkmConnection;
import static com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.transformToKeyVaultEkmConnection;
import static com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.transformToKeyVaultEkmProxyClientCertificateInfo;
import static com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.transformToKeyVaultEkmProxyInfo;
import static com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils.toKeyVaultAdministrationException;

/**
 * The {@link KeyVaultEkmClient} provides synchronous methods to manage External Key Manager (EKM) connections
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link KeyVaultEkmClient} class, a vault url and a credential object.</p>
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
 * <p><strong>Sample: Construct Synchronous EKM Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultEkmClient}, using the
 * {@link KeyVaultEkmClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmClient.instantiation -->
 * <pre>
 * KeyVaultEkmClient keyVaultEkmClient = new KeyVaultEkmClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get an EKM Connection</h2>
 * The {@link KeyVaultEkmClient} can be used to retrieve the EKM connection from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously retrieve the EKM connection from the key vault, using
 * the {@link KeyVaultEkmClient#getEkmConnection()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmConnection -->
 * <pre>
 * KeyVaultEkmConnection ekmConnection = keyVaultEkmClient.getEkmConnection&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved EKM connection with host '%s'.%n&quot;, ekmConnection.getHost&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmConnection -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultEkmAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Create an EKM Connection</h2>
 * The {@link KeyVaultEkmClient} can be used to create an EKM connection in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously create an EKM connection in the key vault, using the
 * {@link KeyVaultEkmClient#createEkmConnection(KeyVaultEkmConnection)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmClient.createEkmConnection#KeyVaultEkmConnection -->
 * <pre>
 * KeyVaultEkmConnection ekmConnectionToCreate =
 *     new KeyVaultEkmConnection&#40;&quot;&lt;ekm-proxy-host&gt;&quot;,
 *         Collections.singletonList&#40;&quot;&lt;server-ca-certificate&gt;&quot;.getBytes&#40;&#41;&#41;&#41;
 *         .setPathPrefix&#40;&quot;&lt;path-prefix&gt;&quot;&#41;
 *         .setServerSubjectCommonName&#40;&quot;&lt;server-subject-common-name&gt;&quot;&#41;;
 *
 * KeyVaultEkmConnection createdEkmConnection = keyVaultEkmClient.createEkmConnection&#40;ekmConnectionToCreate&#41;;
 *
 * System.out.printf&#40;&quot;Created EKM connection with host '%s'.%n&quot;, createdEkmConnection.getHost&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmClient.createEkmConnection#KeyVaultEkmConnection -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultEkmAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Update an EKM Connection</h2>
 * The {@link KeyVaultEkmClient} can be used to update an existing EKM connection in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously update an EKM connection in the key vault, using the
 * {@link KeyVaultEkmClient#updateEkmConnection(KeyVaultEkmConnection)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmClient.updateEkmConnection#KeyVaultEkmConnection -->
 * <pre>
 * KeyVaultEkmConnection ekmConnectionToUpdate =
 *     new KeyVaultEkmConnection&#40;&quot;&lt;ekm-proxy-host&gt;&quot;,
 *         Collections.singletonList&#40;&quot;&lt;server-ca-certificate&gt;&quot;.getBytes&#40;&#41;&#41;&#41;
 *         .setPathPrefix&#40;&quot;&lt;path-prefix&gt;&quot;&#41;
 *         .setServerSubjectCommonName&#40;&quot;&lt;server-subject-common-name&gt;&quot;&#41;;
 *
 * KeyVaultEkmConnection updatedEkmConnection = keyVaultEkmClient.updateEkmConnection&#40;ekmConnectionToUpdate&#41;;
 *
 * System.out.printf&#40;&quot;Updated EKM connection with host '%s'.%n&quot;, updatedEkmConnection.getHost&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmClient.updateEkmConnection#KeyVaultEkmConnection -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultEkmAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete an EKM Connection</h2>
 * The {@link KeyVaultEkmClient} can be used to delete the EKM connection from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously delete the EKM connection from the key vault, using
 * the {@link KeyVaultEkmClient#deleteEkmConnection()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmClient.deleteEkmConnection -->
 * <pre>
 * KeyVaultEkmConnection deletedEkmConnection = keyVaultEkmClient.deleteEkmConnection&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Deleted EKM connection with host '%s'.%n&quot;, deletedEkmConnection.getHost&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmClient.deleteEkmConnection -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultEkmAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Check an EKM Connection</h2>
 * The {@link KeyVaultEkmClient} can be used to check the connectivity and authentication with the EKM proxy.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously check the EKM connection, using the
 * {@link KeyVaultEkmClient#checkEkmConnection()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmClient.checkEkmConnection -->
 * <pre>
 * KeyVaultEkmProxyInfo ekmProxyInfo = keyVaultEkmClient.checkEkmConnection&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Checked EKM connection. Proxy vendor: '%s', proxy name: '%s'.%n&quot;,
 *     ekmProxyInfo.getProxyVendor&#40;&#41;, ekmProxyInfo.getProxyName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmClient.checkEkmConnection -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultEkmAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get the EKM Proxy Client Certificate</h2>
 * The {@link KeyVaultEkmClient} can be used to retrieve the EKM proxy client certificate from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously retrieve the EKM proxy client certificate, using the
 * {@link KeyVaultEkmClient#getEkmCertificate()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmCertificate -->
 * <pre>
 * KeyVaultEkmProxyClientCertificateInfo certificateInfo = keyVaultEkmClient.getEkmCertificate&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved EKM proxy client certificate with subject common name '%s'.%n&quot;,
 *     certificateInfo.getSubjectCommonName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmCertificate -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultEkmAsyncClient}.</p>
 *
 * @see com.azure.security.keyvault.administration
 * @see KeyVaultEkmClientBuilder
 */
@ServiceClient(
    builder = KeyVaultEkmClientBuilder.class,
    serviceInterfaces = KeyVaultAdministrationClientImpl.KeyVaultAdministrationClientService.class)
public final class KeyVaultEkmClient {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultEkmClient.class);
    private final KeyVaultAdministrationClientImpl implClient;

    /**
     * Initializes an instance of {@link KeyVaultEkmClient} class.
     *
     * @param implClient The implementation client used to service requests.
     */
    KeyVaultEkmClient(KeyVaultAdministrationClientImpl implClient) {
        this.implClient = implClient;
    }

    /**
     * Gets the {@link KeyVaultEkmConnection EKM connection}. This operation requires the
     * {@code ekm/read} permission.
     *
     * @return The {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultEkmConnection getEkmConnection() {
        try {
            return transformToKeyVaultEkmConnection(
                implClient.getEkmConnectionWithResponse(EMPTY_OPTIONS).getValue().toObject(EkmConnection.class));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Gets the {@link KeyVaultEkmConnection EKM connection}. This operation requires the
     * {@code ekm/read} permission.
     *
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultEkmConnection> getEkmConnectionWithResponse(Context context) {
        try {
            Response<BinaryData> response
                = implClient.getEkmConnectionWithResponse(new RequestOptions().setContext(context));

            return new SimpleResponse<>(response,
                transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection already
     * exists, this operation fails. This operation requires the {@code ekm/write} permission.
     *
     * @param ekmConnection The {@link KeyVaultEkmConnection EKM connection} to create.
     *
     * @return The created {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws NullPointerException if {@code ekmConnection} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultEkmConnection createEkmConnection(KeyVaultEkmConnection ekmConnection) {
        Objects.requireNonNull(ekmConnection,
            String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'ekmConnection'"));

        try {
            return transformToKeyVaultEkmConnection(
                implClient
                    .createEkmConnectionWithResponse(BinaryData.fromObject(transformToEkmConnection(ekmConnection)),
                        EMPTY_OPTIONS)
                    .getValue()
                    .toObject(EkmConnection.class));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection already
     * exists, this operation fails. This operation requires the {@code ekm/write} permission.
     *
     * @param ekmConnection The {@link KeyVaultEkmConnection EKM connection} to create.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws NullPointerException if {@code ekmConnection} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultEkmConnection> createEkmConnectionWithResponse(KeyVaultEkmConnection ekmConnection,
        Context context) {
        Objects.requireNonNull(ekmConnection,
            String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'ekmConnection'"));

        try {
            Response<BinaryData> response = implClient.createEkmConnectionWithResponse(
                BinaryData.fromObject(transformToEkmConnection(ekmConnection)),
                new RequestOptions().setContext(context));

            return new SimpleResponse<>(response,
                transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Updates the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection does not
     * exist, this operation fails. This operation requires the {@code ekm/write} permission.
     *
     * @param ekmConnection The {@link KeyVaultEkmConnection EKM connection} to update.
     *
     * @return The updated {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws NullPointerException if {@code ekmConnection} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultEkmConnection updateEkmConnection(KeyVaultEkmConnection ekmConnection) {
        Objects.requireNonNull(ekmConnection,
            String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'ekmConnection'"));

        try {
            return transformToKeyVaultEkmConnection(
                implClient
                    .updateEkmConnectionWithResponse(BinaryData.fromObject(transformToEkmConnection(ekmConnection)),
                        EMPTY_OPTIONS)
                    .getValue()
                    .toObject(EkmConnection.class));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Updates the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection does not
     * exist, this operation fails. This operation requires the {@code ekm/write} permission.
     *
     * @param ekmConnection The {@link KeyVaultEkmConnection EKM connection} to update.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws NullPointerException if {@code ekmConnection} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultEkmConnection> updateEkmConnectionWithResponse(KeyVaultEkmConnection ekmConnection,
        Context context) {
        Objects.requireNonNull(ekmConnection,
            String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'ekmConnection'"));

        try {
            Response<BinaryData> response = implClient.updateEkmConnectionWithResponse(
                BinaryData.fromObject(transformToEkmConnection(ekmConnection)),
                new RequestOptions().setContext(context));

            return new SimpleResponse<>(response,
                transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Deletes the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection does not
     * exist, this operation fails. This operation requires the {@code ekm/delete} permission.
     *
     * @return The deleted {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultEkmConnection deleteEkmConnection() {
        try {
            return transformToKeyVaultEkmConnection(
                implClient.deleteEkmConnectionWithResponse(EMPTY_OPTIONS).getValue().toObject(EkmConnection.class));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Deletes the {@link KeyVaultEkmConnection EKM connection}. If the EKM connection does not
     * exist, this operation fails. This operation requires the {@code ekm/delete} permission.
     *
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the deleted
     * {@link KeyVaultEkmConnection EKM connection}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultEkmConnection> deleteEkmConnectionWithResponse(Context context) {
        try {
            Response<BinaryData> response
                = implClient.deleteEkmConnectionWithResponse(new RequestOptions().setContext(context));

            return new SimpleResponse<>(response,
                transformToKeyVaultEkmConnection(response.getValue().toObject(EkmConnection.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Checks the connectivity and authentication with the EKM proxy. This operation requires the {@code ekm/read}
     * permission.
     *
     * @return The {@link KeyVaultEkmProxyInfo EKM proxy information}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultEkmProxyInfo checkEkmConnection() {
        try {
            return transformToKeyVaultEkmProxyInfo(
                implClient.checkEkmConnectionWithResponse(EMPTY_OPTIONS).getValue().toObject(EkmProxyInfo.class));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Checks the connectivity and authentication with the EKM proxy. This operation requires the {@code ekm/read}
     * permission.
     *
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultEkmProxyInfo EKM proxy information}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultEkmProxyInfo> checkEkmConnectionWithResponse(Context context) {
        try {
            Response<BinaryData> response
                = implClient.checkEkmConnectionWithResponse(new RequestOptions().setContext(context));

            return new SimpleResponse<>(response,
                transformToKeyVaultEkmProxyInfo(response.getValue().toObject(EkmProxyInfo.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Gets the EKM proxy client certificate used to authenticate to the EKM proxy. This operation requires the
     * {@code ekm/read} permission.
     *
     * @return The {@link KeyVaultEkmProxyClientCertificateInfo EKM proxy client certificate information}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultEkmProxyClientCertificateInfo getEkmCertificate() {
        try {
            return transformToKeyVaultEkmProxyClientCertificateInfo(
                implClient.getEkmCertificateWithResponse(EMPTY_OPTIONS)
                    .getValue()
                    .toObject(EkmProxyClientCertificateInfo.class));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Gets the EKM proxy client certificate used to authenticate to the EKM proxy. This operation requires the
     * {@code ekm/read} permission.
     *
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultEkmProxyClientCertificateInfo EKM proxy client certificate information}.
     *
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultEkmProxyClientCertificateInfo> getEkmCertificateWithResponse(Context context) {
        try {
            Response<BinaryData> response
                = implClient.getEkmCertificateWithResponse(new RequestOptions().setContext(context));

            return new SimpleResponse<>(response, transformToKeyVaultEkmProxyClientCertificateInfo(
                response.getValue().toObject(EkmProxyClientCertificateInfo.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }
}
