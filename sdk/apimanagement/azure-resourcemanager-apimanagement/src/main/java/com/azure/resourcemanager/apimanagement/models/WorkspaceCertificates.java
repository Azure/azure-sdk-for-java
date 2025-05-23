// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.apimanagement.models;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Resource collection API of WorkspaceCertificates.
 */
public interface WorkspaceCertificates {
    /**
     * Lists a collection of all certificates in the specified workspace.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged Certificates list representation as paginated response with {@link PagedIterable}.
     */
    PagedIterable<CertificateContract> listByWorkspace(String resourceGroupName, String serviceName,
        String workspaceId);

    /**
     * Lists a collection of all certificates in the specified workspace.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param filter | Field | Usage | Supported operators | Supported functions
     * |&lt;/br&gt;|-------------|-------------|-------------|-------------|&lt;/br&gt;| name | filter | ge, le, eq, ne,
     * gt, lt | substringof, contains, startswith, endswith |&lt;/br&gt;| subject | filter | ge, le, eq, ne, gt, lt |
     * substringof, contains, startswith, endswith |&lt;/br&gt;| thumbprint | filter | ge, le, eq, ne, gt, lt |
     * substringof, contains, startswith, endswith |&lt;/br&gt;| expirationDate | filter | ge, le, eq, ne, gt, lt |
     * |&lt;/br&gt;.
     * @param top Number of records to return.
     * @param skip Number of records to skip.
     * @param isKeyVaultRefreshFailed When set to true, the response contains only certificates entities which failed
     * refresh.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged Certificates list representation as paginated response with {@link PagedIterable}.
     */
    PagedIterable<CertificateContract> listByWorkspace(String resourceGroupName, String serviceName, String workspaceId,
        String filter, Integer top, Integer skip, Boolean isKeyVaultRefreshFailed, Context context);

    /**
     * Gets the entity state (Etag) version of the certificate specified by its identifier.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the entity state (Etag) version of the certificate specified by its identifier.
     */
    WorkspaceCertificatesGetEntityTagResponse getEntityTagWithResponse(String resourceGroupName, String serviceName,
        String workspaceId, String certificateId, Context context);

    /**
     * Gets the entity state (Etag) version of the certificate specified by its identifier.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void getEntityTag(String resourceGroupName, String serviceName, String workspaceId, String certificateId);

    /**
     * Gets the details of the certificate specified by its identifier.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the details of the certificate specified by its identifier.
     */
    Response<CertificateContract> getWithResponse(String resourceGroupName, String serviceName, String workspaceId,
        String certificateId, Context context);

    /**
     * Gets the details of the certificate specified by its identifier.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the details of the certificate specified by its identifier.
     */
    CertificateContract get(String resourceGroupName, String serviceName, String workspaceId, String certificateId);

    /**
     * Creates or updates the certificate being used for authentication with the backend.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @param parameters Create or Update parameters.
     * @param ifMatch ETag of the Entity. Not required when creating an entity, but required when updating an entity.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return certificate details.
     */
    Response<CertificateContract> createOrUpdateWithResponse(String resourceGroupName, String serviceName,
        String workspaceId, String certificateId, CertificateCreateOrUpdateParameters parameters, String ifMatch,
        Context context);

    /**
     * Creates or updates the certificate being used for authentication with the backend.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @param parameters Create or Update parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return certificate details.
     */
    CertificateContract createOrUpdate(String resourceGroupName, String serviceName, String workspaceId,
        String certificateId, CertificateCreateOrUpdateParameters parameters);

    /**
     * Deletes specific certificate.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @param ifMatch ETag of the Entity. ETag should match the current entity state from the header response of the GET
     * request or it should be * for unconditional update.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link Response}.
     */
    Response<Void> deleteWithResponse(String resourceGroupName, String serviceName, String workspaceId,
        String certificateId, String ifMatch, Context context);

    /**
     * Deletes specific certificate.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @param ifMatch ETag of the Entity. ETag should match the current entity state from the header response of the GET
     * request or it should be * for unconditional update.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    void delete(String resourceGroupName, String serviceName, String workspaceId, String certificateId, String ifMatch);

    /**
     * From KeyVault, Refresh the certificate being used for authentication with the backend.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return certificate details.
     */
    Response<CertificateContract> refreshSecretWithResponse(String resourceGroupName, String serviceName,
        String workspaceId, String certificateId, Context context);

    /**
     * From KeyVault, Refresh the certificate being used for authentication with the backend.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceName The name of the API Management service.
     * @param workspaceId Workspace identifier. Must be unique in the current API Management service instance.
     * @param certificateId Identifier of the certificate entity. Must be unique in the current API Management service
     * instance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return certificate details.
     */
    CertificateContract refreshSecret(String resourceGroupName, String serviceName, String workspaceId,
        String certificateId);
}
