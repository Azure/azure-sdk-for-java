// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
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

/**
 * The {@link KeyVaultEkmClient} provides synchronous methods to create, get, update, delete and check the External Key
 * Manager (EKM) connection of an Azure Key Vault account, as well as to retrieve the EKM proxy client certificate.
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
     * Gets the {@link KeyVaultEkmConnection EKM connection} of the Key Vault account. This operation requires the
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
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Gets the {@link KeyVaultEkmConnection EKM connection} of the Key Vault account. This operation requires the
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
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates the {@link KeyVaultEkmConnection EKM connection} of the Key Vault account. If the EKM connection already
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
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates the {@link KeyVaultEkmConnection EKM connection} of the Key Vault account. If the EKM connection already
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
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Updates the {@link KeyVaultEkmConnection EKM connection} of the Key Vault account. If the EKM connection does not
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
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Updates the {@link KeyVaultEkmConnection EKM connection} of the Key Vault account. If the EKM connection does not
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
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Deletes the {@link KeyVaultEkmConnection EKM connection} of the Key Vault account. If the EKM connection does not
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
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Deletes the {@link KeyVaultEkmConnection EKM connection} of the Key Vault account. If the EKM connection does not
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
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }
}
