// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import com.azure.v2.security.keyvault.administration.implementation.KeyVaultAdministrationClientImpl;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleAssignment;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleAssignmentCreateParameters;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleDefinition;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.v2.security.keyvault.administration.models.SetRoleDefinitionOptions;
import io.clientcore.core.annotations.ReturnType;
import io.clientcore.core.annotations.ServiceClient;
import io.clientcore.core.annotations.ServiceMethod;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.UUID;

import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.CANNOT_BE_NULL;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.mapPages;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.mapResponse;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.roleAssignmentToKeyVaultRoleAssignment;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.roleDefinitionToKeyVaultRoleDefinition;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.swallowExceptionForStatusCode;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.validateAndGetRoleAssignmentCreateParameters;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.validateAndGetRoleDefinitionCreateParameters;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.validateRoleAssignmentParameters;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.validateRoleDefinitionParameters;
import static io.clientcore.core.utils.CoreUtils.extractSizeFromContentRange;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides methods to view and manage Role-Based Access for a key vault or managed HSM. The client supports
 * creating, listing, updating, and deleting {@link KeyVaultRoleDefinition role definitions} and
 * {@link KeyVaultRoleAssignment role assignments}.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault or Managed HSM service, you will need to create an instance of the
 * {@link KeyVaultAccessControlClient} class, an Azure Key Vault or Managed HSM endpoint and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named {@code DefaultAzureCredential} for
 * authentication, which is appropriate for most scenarios, including local development and production environments.
 * Additionally, we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Access Control Client</strong></p>
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultAccessControlClient}, using the
 * {@link KeyVaultAccessControlClientBuilder} to configure it.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.instantiation -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Set a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to set a role definition in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to create a role definition in the key vault, using the
 * {@link KeyVaultAccessControlClient#setRoleDefinition(KeyVaultRoleScope)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Get a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to retrieve a role definition from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to retrieve a role definition from the key vault, using the
 * {@link KeyVaultAccessControlClient#getRoleDefinition(KeyVaultRoleScope, String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Delete a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to delete a role definition from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to delete a role definition from the key vault, using the
 * {@link KeyVaultAccessControlClient#deleteRoleDefinition(KeyVaultRoleScope, String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Create a Role Assignment</h2>
 * The {@link KeyVaultAccessControlClient} can be used to set a role assignment in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to create a role assignment in the key vault, using the
 * {@link KeyVaultAccessControlClient#createRoleAssignment(KeyVaultRoleScope, String, String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Get a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to retrieve a role assignment from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to retrieve a role assignment from the key vault, using the
 * {@link KeyVaultAccessControlClient#getRoleDefinition(KeyVaultRoleScope, String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Delete a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to delete a role assignment from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to delete a role assignment from the key vault, using the
 * {@link KeyVaultAccessControlClient#deleteRoleDefinition(KeyVaultRoleScope, String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
 *
 * @see com.azure.v2.security.keyvault.administration
 * @see KeyVaultAccessControlClientBuilder
 */
@ServiceClient(
    builder = KeyVaultAccessControlClientBuilder.class,
    serviceInterfaces = KeyVaultAdministrationClientImpl.KeyVaultAdministrationClientService.class)
public final class KeyVaultAccessControlClient {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultAccessControlClient.class);

    private final KeyVaultAdministrationClientImpl clientImpl;

    /**
     * Creates an instance of {@link KeyVaultAccessControlClient} that sends requests to the service using the provided
     * {@link KeyVaultAdministrationClientImpl}.
     *
     * @param clientImpl The implementation client.
     */
    KeyVaultAccessControlClient(KeyVaultAdministrationClientImpl clientImpl) {
        this.clientImpl = clientImpl;
    }

    /**
     * Gets all role definitions that are applicable at the given role scope and above.
     *
     * <p><strong>Iterate through role definitions</strong></p>
     * <p>Lists all role definitions in the key vault and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope -->
     *
     * <p><strong>Iterate through role definitions by page</strong></p>
     * <p>Iterates through the role definitions in the key vault by page and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope -->
     *
     * @param roleScope The role scope of the role definitions. It is required and cannot be {@code null}.
     *
     * @return A {@link PagedIterable} of role definitions for the given role scope.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope) {
        return listRoleDefinitions(roleScope, RequestContext.none());
    }

    /**
     * Gets all role definitions that are applicable at the given role scope and above.
     *
     * <p><strong>Iterate through role definitions</strong></p>
     * <p>Lists all role definitions in the key vault and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-RequestContext -->
     *
     * <p><strong>Iterate through role definitions by page</strong></p>
     * <p>Iterates through the role definitions in the key vault by page and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope-RequestContext -->
     *
     * @param roleScope The role scope of the role definitions. It is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} of role definitions for the given role scope.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope,
        RequestContext requestContext) {

        try {
            Objects.requireNonNull(roleScope, String.format(CANNOT_BE_NULL, "'roleScope'"));

            return mapPages(pagingOptions -> clientImpl.getRoleDefinitions()
                    .listSinglePage(roleScope.getValue(), null, requestContext),
                (pagingOptions, nextLink) -> clientImpl.getRoleDefinitions()
                    .listNextSinglePage(nextLink, requestContext.toBuilder().build()),
                KeyVaultAdministrationUtil::roleDefinitionToKeyVaultRoleDefinition);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a role definition with a randomly generated name.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a role definition with a randomly generated name and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
     *
     * @param roleScope The role scope of the role definition. It is required and cannot be {@code null}. Managed HSM
     * only supports {@code '/'}.
     *
     * @return The created role definition.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition setRoleDefinition(KeyVaultRoleScope roleScope) {
        return setRoleDefinition(roleScope, UUID.randomUUID().toString());
    }

    /**
     * Creates or updates a role definition with a given name. If no name is provided, then a role definition with a
     * randomly generated name will be created.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates or updates a role definition with a given name and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The role scope of the role definition. It is required and cannot be {@code null}. Managed HSM
     * only supports {@code '/'}.
     * @param roleDefinitionName The name of the role definition. It can be any valid UUID. If {@code null} or an empty
     * string are provided, a name will be randomly generated.
     *
     * @return The created or updated role definition.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws NullPointerException If {@code roleDefinitionName} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition setRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        try {
            Objects.requireNonNull(roleScope, String.format(CANNOT_BE_NULL, "'roleScope'"));

            return roleDefinitionToKeyVaultRoleDefinition(
                clientImpl.getRoleDefinitions().createOrUpdate(roleScope.toString(),
                    isNullOrEmpty(roleDefinitionName) ? UUID.randomUUID().toString() : roleDefinitionName, null));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates or updates a role definition.
     *
     * <p>The {@code options} parameter and its {@link SetRoleDefinitionOptions#getRoleScope() role scope} and
     * {@link SetRoleDefinitionOptions#getRoleDefinitionName() role definition name} values are required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates or updates a role definition. Prints out details of the response returned by the service and the
     * created or updated role definition.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions-RequestContext -->
     *
     * @param options The configurable options to create or update a role definition. It is required and cannot be
     * {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return A response object whose {@link Response#getValue() value} contains the created or updated role
     * definition.
     *
     * @throws HttpResponseException If the provided {@code options} object is malformed.
     * @throws IllegalArgumentException If the role definition name in the provided {@code options} is {@code null} or
     * an empty string.
     * @throws NullPointerException If either of the provided {@code options} object or the role scope it contains is
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleDefinition> setRoleDefinitionWithResponse(SetRoleDefinitionOptions options,
        RequestContext requestContext) {

        try {
            return mapResponse(clientImpl.getRoleDefinitions()
                    .createOrUpdateWithResponse(options.getRoleScope().toString(), options.getRoleDefinitionName(),
                        validateAndGetRoleDefinitionCreateParameters(options), requestContext),
                KeyVaultAdministrationUtil::roleDefinitionToKeyVaultRoleDefinition);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets a role definition.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a role definition and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The role scope of the role definition. It is required and cannot be {@code null}. Managed HSM
     * only supports {@code '/'}.
     * @param roleDefinitionName The name of the role definition. It is required and cannot be {@code null} or an empty
     * string.
     *
     * @return The retrieved role definition.
     *
     * @throws HttpResponseException If a role definition with the given {@code roleDefinitionName} cannot be found or
     * if the provided {@code roleScope} is invalid.
     * @throws IllegalArgumentException If the provided {@code roleDefinitionName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition getRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        try {
            validateRoleDefinitionParameters(roleScope, roleDefinitionName);

            return roleDefinitionToKeyVaultRoleDefinition(
                clientImpl.getRoleDefinitions().get(roleScope.toString(), roleDefinitionName));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets a role definition.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a role definition. Prints out details of the response returned by the service and the retrieved role
     * definition.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext -->
     *
     * @param roleScope The role scope of the role definition. It is required and cannot be {@code null}. Managed HSM
     * only supports {@code '/'}.
     * @param roleDefinitionName The name of the role definition. It is required and cannot be {@code null} or an empty
     * string.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return A response object whose {@link Response#getValue() value} contains the retrieved role definition.
     *
     * @throws HttpResponseException If a role definition with the given {@code roleDefinitionName} cannot be found or
     * if the provided {@code roleScope} is invalid.
     * @throws IllegalArgumentException If the provided {@code roleDefinitionName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleDefinition> getRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
        String roleDefinitionName, RequestContext requestContext) {

        try {
            validateRoleDefinitionParameters(roleScope, roleDefinitionName);

            return mapResponse(clientImpl.getRoleDefinitions()
                    .getWithResponse(roleScope.toString(), roleDefinitionName, requestContext),
                KeyVaultAdministrationUtil::roleDefinitionToKeyVaultRoleDefinition);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a role definition.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role definition.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The role scope of the role definition. It is required and cannot be {@code null}. Managed HSM
     * only supports {@code '/'}.
     * @param roleDefinitionName The name of the role definition. It is required and cannot be {@code null} or an empty
     * string.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws IllegalArgumentException If the provided {@code roleDefinitionName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        try {
            validateRoleDefinitionParameters(roleScope, roleDefinitionName);

            clientImpl.getRoleDefinitions().delete(roleScope.toString(), roleDefinitionName);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a role definition.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role definition. Prints out details of the response returned by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext -->
     *
     * @param roleScope The role scope of the role definition. It is required and cannot be {@code null}. Managed HSM
     * only supports {@code '/'}.
     * @param roleDefinitionName The name of the role definition. It is required and cannot be {@code null} or an empty
     * string.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return An empty response object.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws IllegalArgumentException If the provided {@code roleDefinitionName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRoleDefinitionWithResponse(KeyVaultRoleScope roleScope, String roleDefinitionName,
        RequestContext requestContext) {

        try (Response<RoleDefinition> response = clientImpl.getRoleDefinitions()
            .deleteWithResponse(roleScope.toString(), roleDefinitionName, requestContext)) {

            validateRoleDefinitionParameters(roleScope, roleDefinitionName);

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } catch (RuntimeException e) {
            if (e instanceof HttpResponseException) {
                return swallowExceptionForStatusCode(404, (HttpResponseException) e, LOGGER);
            }

            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets all role assignments that are applicable at the given role scope and above.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists all role assignments in the key vault and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope -->
     *
     * @param roleScope The role scope of the role assignment. It is required and cannot be {@code null}. Managed HSM
     * only supports {@code '/'}.
     *
     * @return A {@link PagedIterable} containing the role assignments for the given role scope.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope) {
        return listRoleAssignments(roleScope, RequestContext.none());
    }

    /**
     * Gets all role assignments that are applicable at the given role scope and above.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists all role assignments in the key vault and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope-RequestContext -->
     *
     * @param roleScope The role scope of the role assignment. It is required and cannot be {@code null}. Managed HSM
     * only supports {@code '/'}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the role assignments for the given role scope.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope,
        RequestContext requestContext) {

        try {
            Objects.requireNonNull(roleScope, String.format(CANNOT_BE_NULL, "'roleScope'"));

            return mapPages(pagingOptions -> clientImpl.getRoleAssignments()
                    .listForScopeSinglePage(roleScope.toString(), null, requestContext),
                (pagingOptions, nextLink) -> clientImpl.getRoleAssignments()
                    .listForScopeNextSinglePage(nextLink, requestContext.toBuilder().build()),
                KeyVaultAdministrationUtil::roleAssignmentToKeyVaultRoleAssignment);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a role assignment with a randomly generated name.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a role assignment with a randomly generated name and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
     *
     * @param roleScope The role scope of the role assignment to create. It is required and cannot be {@code null}.
     * Managed HSM only supports {@code '/'}.
     * @param roleDefinitionId The role definition ID for the role assignment. It is required and cannot be {@code null}
     * or an empty string.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory. It is
     * required and cannot be {@code null} or an empty string.
     *
     * @return The created role assignment.
     *
     * @throws HttpResponseException If any of the provided {@code roleScope}, {@code roleDefinitionId}, or
     * {@code principalId} are invalid.
     * @throws IllegalArgumentException If either of the provided {@code roleDefinitionId} or {@code principalId} is
     * {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
        String principalId) {

        return createRoleAssignment(roleScope, roleDefinitionId, principalId, UUID.randomUUID().toString());
    }

    /**
     * Creates a role assignment with a given name. If no name is provided, then a role assignment with a randomly
     * generated name will be created.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a role assignment with a given name and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String-String -->
     *
     * @param roleScope The role scope of the role assignment to create. It is required and cannot be {@code null}.
     * @param roleAssignmentName The name of the role assignment. It can be any valid UUID. If {@code null} or an empty
     * string are provided, a name will be randomly generated.
     * @param roleDefinitionId The role definition ID for the role assignment. It is required and cannot be {@code null}
     * or an empty string.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory. It is
     * required and cannot be {@code null} or an empty string.
     *
     * @return The created role assignment.
     *
     * @throws HttpResponseException If a role assignment with the given name already exists or if any of the provided
     * {@code roleScope}, {@code roleDefinitionId}, or {@code principalId} is invalid.
     * @throws IllegalArgumentException If either of the provided {@code roleDefinitionId} or {@code principalId} is
     * {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
        String principalId, String roleAssignmentName) {

        try {
            RoleAssignmentCreateParameters parameters =
                validateAndGetRoleAssignmentCreateParameters(roleScope, roleDefinitionId, principalId,
                isNullOrEmpty(roleAssignmentName) ? UUID.randomUUID().toString() : roleAssignmentName);

            return roleAssignmentToKeyVaultRoleAssignment(
                clientImpl.getRoleAssignments().create(roleScope.toString(), roleAssignmentName, parameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a role assignment. Prints out details of the response returned by the service and the created role
     * assignment.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-RequestContext -->
     *
     * @param roleScope The role scope of the role assignment to create. It is required and cannot be {@code null}.
     * @param roleAssignmentName The name of the role assignment. It can be any valid UUID. It is required and cannot be
     * {@code null} or an empty string.
     * @param roleDefinitionId The role definition ID for the role assignment. It is required and cannot be {@code null}
     * or an empty string.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory. It is
     * required and cannot be {@code null} or an empty string.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return A response object whose {@link Response#getValue() value} contains the created role assignment.
     *
     * @throws HttpResponseException If a role assignment with the given name already exists or if any of the provided
     * {@code roleScope}, {@code roleDefinitionId}, or {@code principalId} is invalid.
     * @throws IllegalArgumentException If either of the provided {@code roleDefinitionId}, {@code principalId}, or
     * {@code roleAssignmentName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> createRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
        String roleDefinitionId, String principalId, String roleAssignmentName, RequestContext requestContext) {

        try {
            RoleAssignmentCreateParameters parameters = validateAndGetRoleAssignmentCreateParameters(roleScope,
                roleDefinitionId, principalId, roleAssignmentName);

            return mapResponse(clientImpl.getRoleAssignments()
                    .createWithResponse(roleScope.toString(), roleAssignmentName, parameters, requestContext),
                KeyVaultAdministrationUtil::roleAssignmentToKeyVaultRoleAssignment);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role assignment and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
     *
     * @param roleScope The role scope of the role assignment. It is required and cannot be {@code null}.
     * @param roleAssignmentName The name of the role assignment. It is required and cannot be {@code null} or an empty
     * string.
     *
     * @return The role assignment.
     *
     * @throws HttpResponseException If a role assignment with the given {@code roleAssignmentName} cannot be found or
     * if the provided {@code roleScope} is invalid.
     * @throws IllegalArgumentException If the provided {@code roleAssignmentName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment getRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        try {
            validateRoleAssignmentParameters(roleScope, roleAssignmentName);

            return roleAssignmentToKeyVaultRoleAssignment(
                clientImpl.getRoleAssignments().get(roleScope.toString(), roleAssignmentName));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role assignment. Prints out details of the response returned by the service and the retrieved role
     * assignment.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext -->
     *
     * @param roleScope The role scope of the role assignment. It is required and cannot be {@code null}.
     * @param roleAssignmentName The name of the role assignment. It is required and cannot be {@code null} or an empty
     * string.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return A response object whose {@link Response#getValue() value} contains the retrieved role assignment.
     *
     * @throws HttpResponseException If a role assignment with the given {@code roleAssignmentName} cannot be found or
     * if the provided {@code roleScope} is invalid.
     * @throws IllegalArgumentException If the provided {@code roleAssignmentName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> getRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
        String roleAssignmentName, RequestContext requestContext) {

        try {
            validateRoleAssignmentParameters(roleScope, roleAssignmentName);

            return mapResponse(clientImpl.getRoleAssignments()
                    .getWithResponse(roleScope.toString(), roleAssignmentName, requestContext),
                KeyVaultAdministrationUtil::roleAssignmentToKeyVaultRoleAssignment);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role assignment.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
     *
     * @param roleScope The role scope of the role assignment. It is required and cannot be {@code null}.
     * @param roleAssignmentName The name of the role assignment. It is required and cannot be {@code null} or an empty
     * string.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws IllegalArgumentException If the provided {@code roleAssignmentName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        try {
            validateRoleAssignmentParameters(roleScope, roleAssignmentName);

            clientImpl.getRoleAssignments().delete(roleScope.toString(), roleAssignmentName);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role assignment. Prints out details of the response returned by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext -->
     *
     * @param roleScope The role scope of the role assignment. It is required and cannot be {@code null}.
     * @param roleAssignmentName The name of the role assignment. It is required and cannot be {@code null} or an empty
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return An empty response object.
     *
     * @throws HttpResponseException If the provided {@code roleScope} is invalid.
     * @throws IllegalArgumentException If the provided {@code roleAssignmentName} is {@code null} or an empty string.
     * @throws NullPointerException If {@code roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRoleAssignmentWithResponse(KeyVaultRoleScope roleScope, String roleAssignmentName,
        RequestContext requestContext) {

        try (Response<RoleAssignment> response = clientImpl.getRoleAssignments()
            .deleteWithResponse(roleScope.toString(), roleAssignmentName, requestContext)) {

            validateRoleAssignmentParameters(roleScope, roleAssignmentName);

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } catch (RuntimeException e) {
            if (e instanceof HttpResponseException) {
                return swallowExceptionForStatusCode(404, (HttpResponseException) e, LOGGER);
            }

            throw LOGGER.logThrowableAsError(e);
        }
    }
}
