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

import java.util.Objects;
import java.util.UUID;

import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.mapPages;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.mapResponse;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.roleAssignmentToKeyVaultRoleAssignment;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.roleDefinitionToKeyVaultRoleDefinition;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.swallowExceptionForStatusCode;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.validateAndGetRoleAssignmentCreateParameters;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.validateAndGetRoleDefinitionCreateParameters;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.validateRoleAssignmentParameters;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.validateRoleDefinitionParameters;
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
 * <pre>
 * KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
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
 * <pre>
 * KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL&#41;;
 *
 * System.out.printf&#40;&quot;Created role definition with randomly generated name '%s' and role name '%s'.%n&quot;,
 *     roleDefinition.getName&#40;&#41;, roleDefinition.getRoleName&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * String roleDefinitionName = &quot;de8df120-987e-4477-b9cc-570fd219a62c&quot;;
 * KeyVaultRoleDefinition roleDefinition =
 *     keyVaultAccessControlClient.getRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved role definition with name '%s' and role name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;,
 *     roleDefinition.getRoleName&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * String roleDefinitionName = &quot;6a709e6e-8964-4012-a99b-6b0131e8ce40&quot;;
 *
 * keyVaultAccessControlClient.deleteRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
 *
 * System.out.printf&#40;&quot;Deleted role definition with name '%s'.%n&quot;, roleDefinitionName&#41;;
 * </pre>
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
 * <pre>
 * String roleDefinitionId = &quot;b0b43a39-920c-475b-b34c-32ecc2bbb0ea&quot;;
 * String servicePrincipalId = &quot;169d6a86-61b3-4615-ac7e-2da09edfeed4&quot;;
 * KeyVaultRoleAssignment roleAssignment =
 *     keyVaultAccessControlClient.createRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionId,
 *         servicePrincipalId&#41;;
 *
 * System.out.printf&#40;&quot;Created role assignment with randomly generated name '%s' for principal with id '%s'.%n&quot;,
 *     roleAssignment.getName&#40;&#41;, roleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * String roleAssignmentName = &quot;06d1ae8b-0791-4f02-b976-f631251f5a95&quot;;
 * KeyVaultRoleAssignment roleAssignment =
 *     keyVaultAccessControlClient.getRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * String roleAssignmentName = &quot;c3ed874a-64a9-4a87-8581-2a1ad84b9ddb&quot;;
 *
 * keyVaultAccessControlClient.deleteRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
 *
 * System.out.printf&#40;&quot;Deleted role assignment with name '%s'.%n&quot;, roleAssignmentName&#41;;
 * </pre>
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
     * <pre>
     * keyVaultAccessControlClient.listRoleDefinitions&#40;KeyVaultRoleScope.GLOBAL&#41;.forEach&#40;roleDefinition -&gt;
     *     System.out.printf&#40;&quot;Retrieved role definition with name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope -->
     *
     * <p><strong>Iterate through role definitions by page</strong></p>
     * <p>Iterates through the role definitions in the key vault by page and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope -->
     * <pre>
     * keyVaultAccessControlClient.listRoleDefinitions&#40;KeyVaultRoleScope.GLOBAL&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;roleDefinition -&gt;
     *         System.out.printf&#40;&quot;Retrieved role definition with name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
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
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * keyVaultAccessControlClient.listRoleDefinitions&#40;KeyVaultRoleScope.GLOBAL, requestContext&#41;
     *     .forEach&#40;roleDefinition -&gt;
     *         System.out.printf&#40;&quot;Retrieved role definition with name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-RequestContext -->
     *
     * <p><strong>Iterate through role definitions by page</strong></p>
     * <p>Iterates through the role definitions in the key vault by page and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope-RequestContext -->
     * <pre>
     * keyVaultAccessControlClient.listRoleDefinitions&#40;KeyVaultRoleScope.GLOBAL, requestContext&#41;.iterableByPage&#40;&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;roleDefinition -&gt;
     *             System.out.printf&#40;&quot;Retrieved role definition with name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
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

        Objects.requireNonNull(roleScope, "'roleScope' cannot be null.");

        return mapPages(
            pagingOptions -> clientImpl.getRoleDefinitions().listSinglePage(roleScope.getValue(), null, requestContext),
            (pagingOptions, nextLink) -> clientImpl.getRoleDefinitions().listNextSinglePage(nextLink, requestContext),
            KeyVaultAdministrationUtil::roleDefinitionToKeyVaultRoleDefinition);
    }

    /**
     * Creates a role definition with a randomly generated name.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a role definition with a randomly generated name and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
     * <pre>
     * KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL&#41;;
     *
     * System.out.printf&#40;&quot;Created role definition with randomly generated name '%s' and role name '%s'.%n&quot;,
     *     roleDefinition.getName&#40;&#41;, roleDefinition.getRoleName&#40;&#41;&#41;;
     * </pre>
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
     * <pre>
     * String myRoleDefinitionName = &quot;b67c3cf4-cbfd-451e-89ab-97c01906a2e0&quot;;
     * KeyVaultRoleDefinition myRoleDefinition =
     *     keyVaultAccessControlClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionName&#41;;
     *
     * System.out.printf&#40;&quot;Set role definition with name '%s' and role name '%s'.%n&quot;, myRoleDefinition.getName&#40;&#41;,
     *     myRoleDefinition.getRoleName&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(roleScope, "'roleScope' cannot be null.");

        return roleDefinitionToKeyVaultRoleDefinition(clientImpl.getRoleDefinitions()
            .createOrUpdateWithResponse(roleScope.toString(),
                isNullOrEmpty(roleDefinitionName) ? UUID.randomUUID().toString() : roleDefinitionName, null,
                RequestContext.none())
            .getValue());
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
     * <pre>
     * String roleDefinitionName = &quot;a86990e4-2080-4666-bd36-6e1664d3706f&quot;;
     *
     * List&lt;KeyVaultRoleScope&gt; assignableScopes = new ArrayList&lt;&gt;&#40;&#41;;
     * assignableScopes.add&#40;KeyVaultRoleScope.GLOBAL&#41;;
     * assignableScopes.add&#40;KeyVaultRoleScope.KEYS&#41;;
     *
     * List&lt;KeyVaultDataAction&gt; dataActions = new ArrayList&lt;&gt;&#40;&#41;;
     * dataActions.add&#40;KeyVaultDataAction.START_HSM_RESTORE&#41;;
     * dataActions.add&#40;KeyVaultDataAction.START_HSM_BACKUP&#41;;
     * dataActions.add&#40;KeyVaultDataAction.READ_HSM_BACKUP_STATUS&#41;;
     * dataActions.add&#40;KeyVaultDataAction.READ_HSM_RESTORE_STATUS&#41;;
     * dataActions.add&#40;KeyVaultDataAction.BACKUP_HSM_KEYS&#41;;
     * dataActions.add&#40;KeyVaultDataAction.RESTORE_HSM_KEYS&#41;;
     *
     * List&lt;KeyVaultPermission&gt; permissions = new ArrayList&lt;&gt;&#40;&#41;;
     * permissions.add&#40;new KeyVaultPermission&#40;null, null, dataActions, null&#41;&#41;;
     * SetRoleDefinitionOptions setRoleDefinitionOptions =
     *     new SetRoleDefinitionOptions&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;
     *         .setRoleName&#40;&quot;Backup and Restore Role Definition&quot;&#41;
     *         .setDescription&#40;&quot;Can backup and restore a whole Managed HSM, as well as individual keys.&quot;&#41;
     *         .setAssignableScopes&#40;assignableScopes&#41;
     *         .setPermissions&#40;permissions&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultRoleDefinition&gt; response =
     *     keyVaultAccessControlClient.setRoleDefinitionWithResponse&#40;setRoleDefinitionOptions,
     *         requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' and role name '%s' &quot;
     *     + &quot;was set.%n&quot;, response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.getRoleName&#40;&#41;&#41;;
     * </pre>
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

        return mapResponse(
            clientImpl.getRoleDefinitions()
                .createOrUpdateWithResponse(options.getRoleScope().toString(), options.getRoleDefinitionName(),
                    validateAndGetRoleDefinitionCreateParameters(options, LOGGER), requestContext),
            KeyVaultAdministrationUtil::roleDefinitionToKeyVaultRoleDefinition);
    }

    /**
     * Gets a role definition.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a role definition and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
     * <pre>
     * String roleDefinitionName = &quot;de8df120-987e-4477-b9cc-570fd219a62c&quot;;
     * KeyVaultRoleDefinition roleDefinition =
     *     keyVaultAccessControlClient.getRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved role definition with name '%s' and role name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;,
     *     roleDefinition.getRoleName&#40;&#41;&#41;;
     * </pre>
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
        validateRoleDefinitionParameters(roleScope, roleDefinitionName, LOGGER);

        return roleDefinitionToKeyVaultRoleDefinition(clientImpl.getRoleDefinitions()
            .getWithResponse(roleScope.toString(), roleDefinitionName, RequestContext.none())
            .getValue());
    }

    /**
     * Gets a role definition.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a role definition. Prints out details of the response returned by the service and the retrieved role
     * definition.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext -->
     * <pre>
     * String myRoleDefinitionName = &quot;cb15ef18-b32c-4224-b048-3a91cd68acc3&quot;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultRoleDefinition&gt; response =
     *     keyVaultAccessControlClient.getRoleDefinitionWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionName,
     *         requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' and role name '%s'&quot;
     *     + &quot; was retrieved.%n&quot;, response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;,
     *     response.getValue&#40;&#41;.getRoleName&#40;&#41;&#41;;
     * </pre>
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

        validateRoleDefinitionParameters(roleScope, roleDefinitionName, LOGGER);

        return mapResponse(
            clientImpl.getRoleDefinitions().getWithResponse(roleScope.toString(), roleDefinitionName, requestContext),
            KeyVaultAdministrationUtil::roleDefinitionToKeyVaultRoleDefinition);
    }

    /**
     * Deletes a role definition.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role definition.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
     * <pre>
     * String roleDefinitionName = &quot;6a709e6e-8964-4012-a99b-6b0131e8ce40&quot;;
     *
     * keyVaultAccessControlClient.deleteRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
     *
     * System.out.printf&#40;&quot;Deleted role definition with name '%s'.%n&quot;, roleDefinitionName&#41;;
     * </pre>
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
        validateRoleDefinitionParameters(roleScope, roleDefinitionName, LOGGER);

        clientImpl.getRoleDefinitions()
            .deleteWithResponse(roleScope.toString(), roleDefinitionName, RequestContext.none());
    }

    /**
     * Deletes a role definition.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role definition. Prints out details of the response returned by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext -->
     * <pre>
     * String myRoleDefinitionName = &quot;6b2d0b58-4108-44d6-b7e0-4fd02f77fe7e&quot;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     * Response&lt;Void&gt; response =
     *     keyVaultAccessControlClient.deleteRoleDefinitionWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionName,
     *         requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' was deleted.%n&quot;,
     *     response.getStatusCode&#40;&#41;, myRoleDefinitionName&#41;;
     * </pre>
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

            validateRoleDefinitionParameters(roleScope, roleDefinitionName, LOGGER);

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } catch (HttpResponseException e) {
            return swallowExceptionForStatusCode(404, e);
        }
    }

    /**
     * Gets all role assignments that are applicable at the given role scope and above.
     *
     * <p><strong>Iterate through role definitions</strong></p>
     * <p>Lists all role assignments in the key vault and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope -->
     * <pre>
     * keyVaultAccessControlClient.listRoleAssignments&#40;KeyVaultRoleScope.GLOBAL&#41;.forEach&#40;roleAssignment -&gt;
     *     System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope -->
     *
     * <p><strong>Iterate through role definitions by page</strong></p>
     * <p>Iterates through the role definitions in the key vault by page and prints out their details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope -->
     * <pre>
     * keyVaultAccessControlClient.listRoleAssignments&#40;KeyVaultRoleScope.GLOBAL&#41;.forEach&#40;roleAssignment -&gt;
     *     System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope -->
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
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * keyVaultAccessControlClient.listRoleAssignments&#40;KeyVaultRoleScope.GLOBAL, requestContext&#41;
     *     .forEach&#40;roleAssignment -&gt;
     *         System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;&#41;;
     * </pre>
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

        Objects.requireNonNull(roleScope, "'roleScope' cannot be null.");

        return mapPages(
            pagingOptions -> clientImpl.getRoleAssignments()
                .listForScopeSinglePage(roleScope.toString(), null, requestContext),
            (pagingOptions, nextLink) -> clientImpl.getRoleAssignments()
                .listForScopeNextSinglePage(nextLink, requestContext),
            KeyVaultAdministrationUtil::roleAssignmentToKeyVaultRoleAssignment);
    }

    /**
     * Creates a role assignment with a randomly generated name.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a role assignment with a randomly generated name and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
     * <pre>
     * String roleDefinitionId = &quot;b0b43a39-920c-475b-b34c-32ecc2bbb0ea&quot;;
     * String servicePrincipalId = &quot;169d6a86-61b3-4615-ac7e-2da09edfeed4&quot;;
     * KeyVaultRoleAssignment roleAssignment =
     *     keyVaultAccessControlClient.createRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionId,
     *         servicePrincipalId&#41;;
     *
     * System.out.printf&#40;&quot;Created role assignment with randomly generated name '%s' for principal with id '%s'.%n&quot;,
     *     roleAssignment.getName&#40;&#41;, roleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;;
     * </pre>
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
     * <pre>
     * String myRoleDefinitionId = &quot;c7d4f70f-944d-494a-a73e-ff62fe7f04da&quot;;
     * String myServicePrincipalId = &quot;4196fc8f-7312-46b9-9a08-05bf44fdff37&quot;;
     * String myRoleAssignmentName = &quot;d80e9366-47a6-4f42-ba84-f2eefb084972&quot;;
     * KeyVaultRoleAssignment myRoleAssignment =
     *     keyVaultAccessControlClient.createRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionId,
     *         myServicePrincipalId, myRoleAssignmentName&#41;;
     *
     * System.out.printf&#40;&quot;Created role assignment with name '%s' for principal with id '%s'.%n&quot;,
     *     myRoleAssignment.getName&#40;&#41;, myRoleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;;
     * </pre>
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

        RoleAssignmentCreateParameters parameters
            = validateAndGetRoleAssignmentCreateParameters(roleScope, roleDefinitionId, principalId,
                isNullOrEmpty(roleAssignmentName) ? UUID.randomUUID().toString() : roleAssignmentName, LOGGER);

        return roleAssignmentToKeyVaultRoleAssignment(clientImpl.getRoleAssignments()
            .createWithResponse(roleScope.toString(), roleAssignmentName, parameters, RequestContext.none())
            .getValue());
    }

    /**
     * Creates a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a role assignment. Prints out details of the response returned by the service and the created role
     * assignment.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-RequestContext -->
     * <pre>
     * String someRoleDefinitionId = &quot;11385c39-5efa-4e5f-8748-055aa51d4d23&quot;;
     * String someServicePrincipalId = &quot;eab943f7-a204-4434-9681-ef2cc0c85b51&quot;;
     * String someRoleAssignmentName = &quot;4d95e0ea-4808-43a4-b7f9-d9e61dba7ea9&quot;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultRoleAssignment&gt; response =
     *     keyVaultAccessControlClient.createRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL, someRoleDefinitionId,
     *         someServicePrincipalId, someRoleAssignmentName, requestContext&#41;;
     * KeyVaultRoleAssignment createdRoleAssignment = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' for principal with&quot;
     *     + &quot;id '%s' was created.%n&quot;, response.getStatusCode&#40;&#41;, createdRoleAssignment.getName&#40;&#41;,
     *     createdRoleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;;
     * </pre>
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

        RoleAssignmentCreateParameters parameters = validateAndGetRoleAssignmentCreateParameters(roleScope,
            roleDefinitionId, principalId, roleAssignmentName, LOGGER);

        return mapResponse(
            clientImpl.getRoleAssignments()
                .createWithResponse(roleScope.toString(), roleAssignmentName, parameters, requestContext),
            KeyVaultAdministrationUtil::roleAssignmentToKeyVaultRoleAssignment);
    }

    /**
     * Gets a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role assignment and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
     * <pre>
     * String roleAssignmentName = &quot;06d1ae8b-0791-4f02-b976-f631251f5a95&quot;;
     * KeyVaultRoleAssignment roleAssignment =
     *     keyVaultAccessControlClient.getRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;;
     * </pre>
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
        validateRoleAssignmentParameters(roleScope, roleAssignmentName, LOGGER);

        return roleAssignmentToKeyVaultRoleAssignment(clientImpl.getRoleAssignments()
            .getWithResponse(roleScope.toString(), roleAssignmentName, RequestContext.none())
            .getValue());
    }

    /**
     * Gets a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role assignment. Prints out details of the response returned by the service and the retrieved role
     * assignment.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext -->
     * <pre>
     * String myRoleAssignmentName = &quot;b4a970d5-c581-4760-bba5-61d3d5aa24f9&quot;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultRoleAssignment&gt; response =
     *     keyVaultAccessControlClient.getRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleAssignmentName,
     *         requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' was retrieved.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
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

        validateRoleAssignmentParameters(roleScope, roleAssignmentName, LOGGER);

        return mapResponse(
            clientImpl.getRoleAssignments().getWithResponse(roleScope.toString(), roleAssignmentName, requestContext),
            KeyVaultAdministrationUtil::roleAssignmentToKeyVaultRoleAssignment);
    }

    /**
     * Deletes a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role assignment.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
     * <pre>
     * String roleAssignmentName = &quot;c3ed874a-64a9-4a87-8581-2a1ad84b9ddb&quot;;
     *
     * keyVaultAccessControlClient.deleteRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
     *
     * System.out.printf&#40;&quot;Deleted role assignment with name '%s'.%n&quot;, roleAssignmentName&#41;;
     * </pre>
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
        validateRoleAssignmentParameters(roleScope, roleAssignmentName, LOGGER);

        clientImpl.getRoleAssignments().deleteWithResponse(roleScope.toString(), roleAssignmentName, null);
    }

    /**
     * Deletes a role assignment.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a role assignment. Prints out details of the response returned by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext -->
     * <pre>
     * String myRoleAssignmentName = &quot;8ac293e1-1ac8-4a71-b254-7caf9f7c2646&quot;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;Void&gt; response =
     *     keyVaultAccessControlClient.deleteRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleAssignmentName,
     *         requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' was deleted.%n&quot;,
     *     response.getStatusCode&#40;&#41;, myRoleAssignmentName&#41;;
     * </pre>
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

            validateRoleAssignmentParameters(roleScope, roleAssignmentName, LOGGER);

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } catch (HttpResponseException e) {
            return swallowExceptionForStatusCode(404, e);
        }
    }
}
