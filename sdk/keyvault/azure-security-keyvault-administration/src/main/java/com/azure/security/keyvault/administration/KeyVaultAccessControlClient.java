// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationClientImpl;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignment;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentCreateParameters;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinition;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionCreateParameters;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.SetRoleDefinitionOptions;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.roleAssignmentToKeyVaultRoleAssignment;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.roleDefinitionToKeyVaultRoleDefinition;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.swallowExceptionForStatusCodeSync;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.transformBinaryDataResponse;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.validateAndGetRoleAssignmentCreateParameters;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.validateAndGetRoleDefinitionCreateParameters;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.validateRoleAssignmentParameters;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.validateRoleDefinitionParameters;
import static com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils.toKeyVaultAdministrationException;

/**
 * The {@link KeyVaultAccessControlClient} provides synchronous methods to view and manage Role Based Access for a
 * key vault. The client supports creating, listing, updating, and deleting
 * {@link KeyVaultRoleDefinition role definitions} and {@link KeyVaultRoleAssignment role assignments}.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link KeyVaultAccessControlClient} class, a vault url and a credential object.</p>
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
 * <p><strong>Sample: Construct Synchronous Access Control Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultAccessControlClient}, using the
 * {@link KeyVaultAccessControlClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.instantiation -->
 * <pre>
 * KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.instantiation -->
 * <br/>
 *
 * <hr/>
 *
 * <h2>Set a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to set a role definition in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously create a role definition in the key vault, using the
 * {@link KeyVaultAccessControlClient#setRoleDefinition(KeyVaultRoleScope)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
 * <pre>
 * KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL&#41;;
 *
 * System.out.printf&#40;&quot;Created role definition with randomly generated name '%s' and role name '%s'.%n&quot;,
 *     roleDefinition.getName&#40;&#41;, roleDefinition.getRoleName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultAccessControlAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to retrieve a role definition from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously retrieve a role definition from the key vault, using
 * the {@link KeyVaultAccessControlClient#getRoleDefinition(KeyVaultRoleScope, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
 * <pre>
 * String roleDefinitionName = &quot;de8df120-987e-4477-b9cc-570fd219a62c&quot;;
 * KeyVaultRoleDefinition roleDefinition =
 *     keyVaultAccessControlClient.getRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved role definition with name '%s' and role name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;,
 *     roleDefinition.getRoleName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultAccessControlAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to delete a role definition from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously delete a role definition from the key vault, using
 * the {@link KeyVaultAccessControlClient#deleteRoleDefinition(KeyVaultRoleScope, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
 * <pre>
 * String roleDefinitionName = &quot;6a709e6e-8964-4012-a99b-6b0131e8ce40&quot;;
 *
 * keyVaultAccessControlClient.deleteRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
 *
 * System.out.printf&#40;&quot;Deleted role definition with name '%s'.%n&quot;, roleDefinitionName&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultAccessControlAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Create a Role Assignment</h2>
 * The {@link KeyVaultAccessControlClient} can be used to set a role assignment in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously create a role assignment in the key vault, using the
 * {@link KeyVaultAccessControlClient#createRoleAssignment(KeyVaultRoleScope, String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
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
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultAccessControlAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to retrieve a role assignment from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously retrieve a role assignment from the key vault, using
 * the {@link KeyVaultAccessControlClient#getRoleDefinition(KeyVaultRoleScope, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
 * <pre>
 * String roleAssignmentName = &quot;06d1ae8b-0791-4f02-b976-f631251f5a95&quot;;
 * KeyVaultRoleAssignment roleAssignment =
 *     keyVaultAccessControlClient.getRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultAccessControlAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete a Role Definition</h2>
 * The {@link KeyVaultAccessControlClient} can be used to delete a role assignment from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously delete a role assignment from the key vault, using
 * the {@link KeyVaultAccessControlClient#deleteRoleDefinition(KeyVaultRoleScope, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
 * <pre>
 * String roleAssignmentName = &quot;c3ed874a-64a9-4a87-8581-2a1ad84b9ddb&quot;;
 *
 * keyVaultAccessControlClient.deleteRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
 *
 * System.out.printf&#40;&quot;Deleted role assignment with name '%s'.%n&quot;, roleAssignmentName&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultAccessControlAsyncClient}.</p>
 *
 * @see com.azure.security.keyvault.administration
 * @see KeyVaultAccessControlClientBuilder
 */
@ServiceClient(builder = KeyVaultAccessControlClientBuilder.class)
public final class KeyVaultAccessControlClient {
    /**
     * The logger to be used.
     */
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultAccessControlClient.class);

    /**
     * The underlying AutoRest client used to interact with the Key Vault service.
     */
    private final KeyVaultAdministrationClientImpl implClient;

    /**
     * The Key Vault URL this client is associated to.
     */
    private final String vaultUrl;

    /**
     * Package private constructor to be used by {@link KeyVaultAccessControlClientBuilder}.
     */
    KeyVaultAccessControlClient(URL vaultUrl, HttpPipeline httpPipeline,
        KeyVaultAdministrationServiceVersion serviceVersion) {

        Objects.requireNonNull(vaultUrl, KeyVaultAdministrationUtil.VAULT_END_POINT_REQUIRED);

        this.vaultUrl = vaultUrl.toString();
        implClient = new KeyVaultAdministrationClientImpl(httpPipeline, this.vaultUrl, serviceVersion);
    }

    /**
     * Gets the URL for the Key Vault this client is associated with.
     *
     * @return The Key Vault URL.
     */
    public String getVaultUrl() {
        return this.vaultUrl;
    }

    /**
     * Get all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link KeyVaultRoleDefinition role definitions}. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition role definitions}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope -->
     * <pre>
     * PagedIterable&lt;KeyVaultRoleDefinition&gt; roleDefinitions =
     *     keyVaultAccessControlClient.listRoleDefinitions&#40;KeyVaultRoleScope.GLOBAL&#41;;
     *
     * roleDefinitions.forEach&#40;roleDefinition -&gt;
     *     System.out.printf&#40;&quot;Retrieved role definition with name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope -->
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleDefinition role definitions}.
     *
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleScope roleScope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope) {
        return listRoleDefinitions(roleScope, Context.NONE);
    }

    /**
     * Get all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link KeyVaultRoleDefinition role definitions}. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition role definitions}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-Context -->
     * <pre>
     * PagedIterable&lt;KeyVaultRoleDefinition&gt; keyVaultRoleDefinitions =
     *     keyVaultAccessControlClient.listRoleDefinitions&#40;KeyVaultRoleScope.GLOBAL, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * keyVaultRoleDefinitions.forEach&#40;roleDefinition -&gt;
     *     System.out.printf&#40;&quot;Retrieved role definition with name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-Context -->
     *
     * @param roleScope The {@link KeyVaultRoleScope scope} of the {@link KeyVaultRoleDefinition role definitions}.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleScope roleScope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope, Context context) {
        Objects.requireNonNull(roleScope, String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'roleScope'"));

        try {
            return implClient.getRoleDefinitions()
                .list(roleScope.toString(), new RequestOptions().setContext(context))
                .mapPage(
                    binaryData -> roleDefinitionToKeyVaultRoleDefinition(binaryData.toObject(RoleDefinition.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates a {@link KeyVaultRoleDefinition role definition} with a randomly generated name.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a {@link KeyVaultRoleDefinition role definition} with a randomly generated name. Prints out the
     * details of the created {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
     * <pre>
     * KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL&#41;;
     *
     * System.out.printf&#40;&quot;Created role definition with randomly generated name '%s' and role name '%s'.%n&quot;,
     *     roleDefinition.getName&#40;&#41;, roleDefinition.getRoleName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * Managed HSM only supports '/'.
     *
     * @return The created {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition setRoleDefinition(KeyVaultRoleScope roleScope) {
        return setRoleDefinition(roleScope, UUID.randomUUID().toString());
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition role definition} with a given name. If no name is provided,
     * then a {@link KeyVaultRoleDefinition role definition} will be created with a randomly generated name.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates or updates a {@link KeyVaultRoleDefinition role definition} with a given generated name. Prints out
     * the details of the created {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope-String -->
     * <pre>
     * String myRoleDefinitionName = &quot;b67c3cf4-cbfd-451e-89ab-97c01906a2e0&quot;;
     * KeyVaultRoleDefinition myRoleDefinition =
     *     keyVaultAccessControlClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionName&#41;;
     *
     * System.out.printf&#40;&quot;Set role definition with name '%s' and role name '%s'.%n&quot;, myRoleDefinition.getName&#40;&#41;,
     *     myRoleDefinition.getRoleName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * Managed HSM only supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}. It can be any valid
     * UUID. If {@code null} is provided, a name will be randomly generated.
     *
     * @return The created or updated {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName}
     * are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition setRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return setRoleDefinitionWithResponse(new SetRoleDefinitionOptions(roleScope, roleDefinitionName), Context.NONE)
            .getValue();
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates or updates a {@link KeyVaultRoleDefinition role definition}. Prints out the details of the
     * {@link Response HTTP response} and the created {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions-Context -->
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
     *
     * SetRoleDefinitionOptions setRoleDefinitionOptions =
     *     new SetRoleDefinitionOptions&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;
     *         .setRoleName&#40;&quot;Backup and Restore Role Definition&quot;&#41;
     *         .setDescription&#40;&quot;Can backup and restore a whole Managed HSM, as well as individual keys.&quot;&#41;
     *         .setAssignableScopes&#40;assignableScopes&#41;
     *         .setPermissions&#40;permissions&#41;;
     *
     * Response&lt;KeyVaultRoleDefinition&gt; response =
     *     keyVaultAccessControlClient.setRoleDefinitionWithResponse&#40;setRoleDefinitionOptions,
     *         new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' and role name '%s' &quot;
     *     + &quot;was set.%n&quot;, response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.getRoleName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions-Context -->
     *
     * @param options Object representing the configurable options to create or update a
     * {@link KeyVaultRoleDefinition role definition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the created or updated
     * {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If any parameter in {@code options} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName}
     * in the {@link SetRoleDefinitionOptions options} object are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleDefinition> setRoleDefinitionWithResponse(SetRoleDefinitionOptions options,
        Context context) {

        RoleDefinitionCreateParameters parameters = validateAndGetRoleDefinitionCreateParameters(options);

        try {
            Response<BinaryData> roleDefinitionResponse = implClient.getRoleDefinitions()
                .createOrUpdateWithResponse(options.getRoleScope().toString(), options.getRoleDefinitionName(),
                    BinaryData.fromObject(parameters), new RequestOptions().setContext(context));

            return transformBinaryDataResponse(roleDefinitionResponse,
                binaryData -> roleDefinitionToKeyVaultRoleDefinition(binaryData.toObject(RoleDefinition.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a {@link KeyVaultRoleDefinition role definition}. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
     * <pre>
     * String roleDefinitionName = &quot;de8df120-987e-4477-b9cc-570fd219a62c&quot;;
     * KeyVaultRoleDefinition roleDefinition =
     *     keyVaultAccessControlClient.getRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved role definition with name '%s' and role name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;,
     *     roleDefinition.getRoleName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionName The name used of the {@link KeyVaultRoleDefinition role definition}.
     *
     * @return The retrieved {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition getRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return getRoleDefinitionWithResponse(roleScope, roleDefinitionName, Context.NONE).getValue();
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a {@link KeyVaultRoleDefinition role definition}. Prints out the details of the
     * {@link Response HTTP response} and the retrieved {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-Context -->
     * <pre>
     * String myRoleDefinitionName = &quot;cb15ef18-b32c-4224-b048-3a91cd68acc3&quot;;
     * Response&lt;KeyVaultRoleDefinition&gt; response =
     *     keyVaultAccessControlClient.getRoleDefinitionWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionName,
     *         new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' and role name '%s'&quot;
     *     + &quot; was retrieved.%n&quot;, response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;,
     *     response.getValue&#40;&#41;.getRoleName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-Context -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * retrieved {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleDefinition> getRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
        String roleDefinitionName, Context context) {

        validateRoleDefinitionParameters(roleScope, roleDefinitionName);

        try {
            Response<BinaryData> roleDefinitionResponse = implClient.getRoleDefinitions()
                .getWithResponse(roleScope.toString(), roleDefinitionName, new RequestOptions().setContext(context));

            return transformBinaryDataResponse(roleDefinitionResponse,
                binaryData -> roleDefinitionToKeyVaultRoleDefinition(binaryData.toObject(RoleDefinition.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
     * <pre>
     * String roleDefinitionName = &quot;6a709e6e-8964-4012-a99b-6b0131e8ce40&quot;;
     *
     * keyVaultAccessControlClient.deleteRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
     *
     * System.out.printf&#40;&quot;Deleted role definition with name '%s'.%n&quot;, roleDefinitionName&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * Managed HSM only supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        deleteRoleDefinitionWithResponse(roleScope, roleDefinitionName, Context.NONE);
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleDefinition role definition}. Prints out the details of the
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-Context -->
     * <pre>
     * String myRoleDefinitionName = &quot;6b2d0b58-4108-44d6-b7e0-4fd02f77fe7e&quot;;
     * Response&lt;Void&gt; response =
     *     keyVaultAccessControlClient.deleteRoleDefinitionWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionName,
     *         new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' was deleted.%n&quot;,
     *     response.getStatusCode&#40;&#41;, myRoleDefinitionName&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-Context -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} with a {@link Void} value.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRoleDefinitionWithResponse(KeyVaultRoleScope roleScope, String roleDefinitionName,
        Context context) {

        validateRoleDefinitionParameters(roleScope, roleDefinitionName);

        try {
            Response<BinaryData> roleDefinitionResponse = implClient.getRoleDefinitions()
                .deleteWithResponse(roleScope.toString(), roleDefinitionName, new RequestOptions().setContext(context));

            return new SimpleResponse<>(roleDefinitionResponse, null);
        } catch (HttpResponseException e) {
            KeyVaultAdministrationException mappedException = toKeyVaultAdministrationException(e);
            return swallowExceptionForStatusCodeSync(404, mappedException, LOGGER);
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Get all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link KeyVaultRoleAssignment role assignments}. Prints out the details of the retrieved
     * {@link KeyVaultRoleAssignment role assignments}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope -->
     * <pre>
     * PagedIterable&lt;KeyVaultRoleAssignment&gt; roleAssignments =
     *     keyVaultAccessControlClient.listRoleAssignments&#40;KeyVaultRoleScope.GLOBAL&#41;;
     *
     * roleAssignments.forEach&#40;roleAssignment -&gt;
     *     System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope -->
     *
     * @param roleScope The {@link KeyVaultRoleScope scope} of the {@link KeyVaultRoleAssignment role assignment}.
     *
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleScope roleScope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope) {
        return listRoleAssignments(roleScope, Context.NONE);
    }

    /**
     * Get all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link KeyVaultRoleAssignment role assignments}. Prints out the details of the retrieved
     * {@link KeyVaultRoleAssignment role assignments}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope-Context -->
     * <pre>
     * PagedIterable&lt;KeyVaultRoleAssignment&gt; keyVaultRoleAssignments =
     *     keyVaultAccessControlClient.listRoleAssignments&#40;KeyVaultRoleScope.GLOBAL, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * keyVaultRoleAssignments.forEach&#40;roleAssignment -&gt;
     *     System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope-Context -->
     *
     * @param roleScope The {@link KeyVaultRoleScope scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleScope roleScope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope, Context context) {
        Objects.requireNonNull(roleScope, String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'roleScope'"));

        try {
            return implClient.getRoleAssignments()
                .listForScope(roleScope.toString(), new RequestOptions().setContext(context))
                .mapPage(
                    binaryData -> roleAssignmentToKeyVaultRoleAssignment(binaryData.toObject(RoleAssignment.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment role assignment} with a randomly generated name.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a {@link KeyVaultRoleAssignment role assignment} with a randomly generated name. Prints out the
     * details of the created {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
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
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}
     * to create.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope}, {@code roleDefinitionId} or
     * {@code principalId} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope}, {@link String roleDefinitionId} or
     * {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
        String principalId) {

        return createRoleAssignmentWithResponse(roleScope, roleDefinitionId, principalId, UUID.randomUUID().toString(),
            Context.NONE).getValue();
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a {@link KeyVaultRoleAssignment role assignment}. Prints out the details of the created
     * {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String-String -->
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
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}
     * to create.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment role assignment}. It can be
     * any valid UUID.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     *
     * @return The created {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * already exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
        String principalId, String roleAssignmentName) {

        return createRoleAssignmentWithResponse(roleScope, roleDefinitionId, principalId, roleAssignmentName,
            Context.NONE).getValue();
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a {@link KeyVaultRoleAssignment role assignment}. Prints out details of the
     * {@link Response HTTP response} and the created {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-Context -->
     * <pre>
     * String someRoleDefinitionId = &quot;11385c39-5efa-4e5f-8748-055aa51d4d23&quot;;
     * String someServicePrincipalId = &quot;eab943f7-a204-4434-9681-ef2cc0c85b51&quot;;
     * String someRoleAssignmentName = &quot;4d95e0ea-4808-43a4-b7f9-d9e61dba7ea9&quot;;
     *
     * Response&lt;KeyVaultRoleAssignment&gt; response =
     *     keyVaultAccessControlClient.createRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL, someRoleDefinitionId,
     *         someServicePrincipalId, someRoleAssignmentName, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     * KeyVaultRoleAssignment createdRoleAssignment = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' for principal with&quot;
     *     + &quot;id '%s' was created.%n&quot;, response.getStatusCode&#40;&#41;, createdRoleAssignment.getName&#40;&#41;,
     *     createdRoleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-Context -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}
     * to create.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment role assignment}. It can be
     * any valid UUID.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given
     * name already exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are
     * invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> createRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
        String roleDefinitionId, String principalId, String roleAssignmentName, Context context) {

        RoleAssignmentCreateParameters parameters = validateAndGetRoleAssignmentCreateParameters(roleScope,
            roleDefinitionId, principalId, roleAssignmentName);

        try {
            Response<BinaryData> roleAssignmentResponse = implClient.getRoleAssignments()
                .createWithResponse(roleScope.toString(), roleAssignmentName, BinaryData.fromObject(parameters),
                    new RequestOptions().setContext(context));

            return transformBinaryDataResponse(roleAssignmentResponse,
                binaryData -> roleAssignmentToKeyVaultRoleAssignment(binaryData.toObject(RoleAssignment.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleAssignment role assignment}. Prints out details of the retrieved
     * {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
     * <pre>
     * String roleAssignmentName = &quot;06d1ae8b-0791-4f02-b976-f631251f5a95&quot;;
     * KeyVaultRoleAssignment roleAssignment =
     *     keyVaultAccessControlClient.getRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     *
     * @return The {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment getRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        return getRoleAssignmentWithResponse(roleScope, roleAssignmentName, Context.NONE).getValue();
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleAssignment role assignment}. Prints out details of the
     * {@link Response HTTP response} and the retrieved {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-Context -->
     * <pre>
     * String myRoleAssignmentName = &quot;b4a970d5-c581-4760-bba5-61d3d5aa24f9&quot;;
     * Response&lt;KeyVaultRoleAssignment&gt; response =
     *     keyVaultAccessControlClient.getRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleAssignmentName,
     *         new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' was retrieved.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-Context -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> getRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
        String roleAssignmentName, Context context) {

        validateRoleAssignmentParameters(roleScope, roleAssignmentName);

        try {
            Response<BinaryData> roleAssignmentResponse = implClient.getRoleAssignments()
                .getWithResponse(roleScope.toString(), roleAssignmentName, new RequestOptions().setContext(context));

            return transformBinaryDataResponse(roleAssignmentResponse,
                binaryData -> roleAssignmentToKeyVaultRoleAssignment(binaryData.toObject(RoleAssignment.class)));
        } catch (HttpResponseException e) {
            throw LOGGER.logExceptionAsError(toKeyVaultAdministrationException(e));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
     * <pre>
     * String roleAssignmentName = &quot;c3ed874a-64a9-4a87-8581-2a1ad84b9ddb&quot;;
     *
     * keyVaultAccessControlClient.deleteRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
     *
     * System.out.printf&#40;&quot;Deleted role assignment with name '%s'.%n&quot;, roleAssignmentName&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        deleteRoleAssignmentWithResponse(roleScope, roleAssignmentName, Context.NONE);
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleAssignment role assignment}. Prints out details of the
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-Context -->
     * <pre>
     * String myRoleAssignmentName = &quot;8ac293e1-1ac8-4a71-b254-7caf9f7c2646&quot;;
     * Response&lt;Void&gt; response =
     *     keyVaultAccessControlClient.deleteRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleAssignmentName,
     *         new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' was deleted.%n&quot;,
     *     response.getStatusCode&#40;&#41;, myRoleAssignmentName&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-Context -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} with a {@link Void} value.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRoleAssignmentWithResponse(KeyVaultRoleScope roleScope, String roleAssignmentName,
        Context context) {

        validateRoleAssignmentParameters(roleScope, roleAssignmentName);

        try {
            Response<BinaryData> roleAssignmentResponse = implClient.getRoleAssignments()
                .deleteWithResponse(roleScope.toString(), roleAssignmentName, new RequestOptions().setContext(context));

            return new SimpleResponse<>(roleAssignmentResponse, null);
        } catch (HttpResponseException e) {
            KeyVaultAdministrationException mappedException = toKeyVaultAdministrationException(e);
            return swallowExceptionForStatusCodeSync(404, mappedException, LOGGER);
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }
}
