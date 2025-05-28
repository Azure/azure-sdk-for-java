// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.security.keyvault.administration.implementation.models.FullBackupOperation;
import com.azure.v2.security.keyvault.administration.implementation.models.Permission;
import com.azure.v2.security.keyvault.administration.implementation.models.RestoreOperation;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleAssignment;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleAssignmentCreateParameters;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleAssignmentProperties;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleAssignmentPropertiesWithScope;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleDefinition;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleDefinitionCreateParameters;
import com.azure.v2.security.keyvault.administration.implementation.models.RoleDefinitionProperties;
import com.azure.v2.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultDataAction;
import com.azure.v2.security.keyvault.administration.models.KeyVaultLongRunningOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleDefinitionType;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleType;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.SetRoleDefinitionOptions;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.v2.security.keyvault.administration.implementation.KeyVaultAdministrationUtils.toKeyVaultAdministrationError;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * Internal utility class for KeyVault Administration clients.
 */
class KeyVaultAdministrationUtil {
    /**
     * Synchronously deserializes a given {@link Response HTTP response} including headers to a given class.
     *
     * @param statusCode The status code which will trigger exception swallowing.
     * @param httpResponseException The {@link HttpResponseException} to be swallowed.
     * @param <E> The class of the exception to swallow.
     *
     * @return the deserialized response.
     */
    static <E extends HttpResponseException> Response<Void> swallowExceptionForStatusCode(int statusCode,
        E httpResponseException) {

        Response<BinaryData> httpResponse = httpResponseException.getResponse();

        if (httpResponse.getStatusCode() == statusCode) {
            return new Response<>(httpResponse.getRequest(), httpResponse.getStatusCode(), httpResponse.getHeaders(),
                null);
        }

        throw httpResponseException;
    }

    static RoleAssignmentCreateParameters validateAndGetRoleAssignmentCreateParameters(KeyVaultRoleScope roleScope,
        String roleDefinitionId, String principalId, String roleAssignmentName, ClientLogger logger) {

        validateRoleAssignmentParameters(roleScope, roleAssignmentName, logger);

        Objects.requireNonNull(principalId, "'principalId' cannot be null.");
        Objects.requireNonNull(roleDefinitionId, "'roleDefinitionId' cannot be null.");

        RoleAssignmentProperties roleAssignmentProperties = new RoleAssignmentProperties(roleDefinitionId, principalId);

        return new RoleAssignmentCreateParameters(roleAssignmentProperties);
    }

    static RoleDefinitionCreateParameters validateAndGetRoleDefinitionCreateParameters(SetRoleDefinitionOptions options,
        ClientLogger logger) {

        Objects.requireNonNull(options, "'options' cannot be null.");
        Objects.requireNonNull(options.getRoleScope(), "'options.getRoleScope()' cannot be null.");

        if (isNullOrEmpty(options.getRoleDefinitionName())) {
            throw logger.throwableAtError()
                .log("'options.getRoleDefinitionName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        List<Permission> permissions = null;

        if (options.getPermissions() != null) {
            permissions = options.getPermissions()
                .stream()
                .map(keyVaultPermission -> new Permission().setActions(keyVaultPermission.getActions())
                    .setNotActions(keyVaultPermission.getNotActions())
                    .setDataActions(keyVaultPermission.getDataActions())
                    .setNotDataActions(keyVaultPermission.getNotDataActions()))
                .collect(Collectors.toList());
        }

        RoleDefinitionProperties roleDefinitionProperties
            = new RoleDefinitionProperties().setRoleName(options.getRoleDefinitionName())
                .setAssignableScopes(options.getAssignableScopes())
                .setDescription(options.getDescription())
                .setPermissions(permissions);

        return new RoleDefinitionCreateParameters(roleDefinitionProperties);
    }

    static void validateRoleAssignmentParameters(KeyVaultRoleScope roleScope, String roleAssignmentName,
        ClientLogger logger) {
        Objects.requireNonNull(roleScope, "'roleScope' cannot be null.");

        if (isNullOrEmpty(roleAssignmentName)) {
            throw logger.throwableAtError()
                .log("'roleDefinitionName' cannot be null or empty.", IllegalArgumentException::new);
        }
    }

    static void validateRoleDefinitionParameters(KeyVaultRoleScope roleScope, String roleDefinitionName,
        ClientLogger logger) {
        Objects.requireNonNull(roleScope, "'roleScope' cannot be null.");

        if (isNullOrEmpty(roleDefinitionName)) {
            throw logger.throwableAtError()
                .log("'roleDefinitionName' cannot be null or empty.", IllegalArgumentException::new);
        }
    }

    static KeyVaultRoleDefinition roleDefinitionToKeyVaultRoleDefinition(RoleDefinition roleDefinition) {
        List<KeyVaultPermission> keyVaultPermissions = new ArrayList<>();

        for (Permission permission : roleDefinition.getProperties().getPermissions()) {
            keyVaultPermissions.add(new KeyVaultPermission(permission.getActions(), permission.getNotActions(),
                permission.getDataActions()
                    .stream()
                    .map(dataAction -> KeyVaultDataAction.fromValue(dataAction.toString()))
                    .collect(Collectors.toList()),
                permission.getNotDataActions()
                    .stream()
                    .map(notDataAction -> KeyVaultDataAction.fromValue(notDataAction.toString()))
                    .collect(Collectors.toList())));
        }

        return new KeyVaultRoleDefinition(roleDefinition.getId(), roleDefinition.getName(),
            KeyVaultRoleDefinitionType.fromValue(roleDefinition.getType().toString()),
            roleDefinition.getProperties().getRoleName(), roleDefinition.getProperties().getDescription(),
            KeyVaultRoleType.fromValue(roleDefinition.getProperties().getRoleType().toString()), keyVaultPermissions,
            roleDefinition.getProperties()
                .getAssignableScopes()
                .stream()
                .map(roleScope -> KeyVaultRoleScope.fromValue(roleScope.toString()))
                .collect(Collectors.toList()));
    }

    static KeyVaultRoleAssignment roleAssignmentToKeyVaultRoleAssignment(RoleAssignment roleAssignment) {
        RoleAssignmentPropertiesWithScope propertiesWithScope = roleAssignment.getProperties();

        return new KeyVaultRoleAssignment(roleAssignment.getId(), roleAssignment.getName(), roleAssignment.getType(),
            new KeyVaultRoleAssignmentProperties(propertiesWithScope.getRoleDefinitionId(),
                propertiesWithScope.getPrincipalId(),
                KeyVaultRoleScope.fromValue(propertiesWithScope.getScope().toString())));
    }

    static LongRunningOperationStatus toLongRunningOperationStatus(String operationStatus) {
        switch (operationStatus) {
            case "inprogress":
                return LongRunningOperationStatus.IN_PROGRESS;

            case "succeeded":
                return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

            case "failed":
                return LongRunningOperationStatus.FAILED;

            default:
                // Should not reach here
                return LongRunningOperationStatus.fromString("POLLING_FAILED", true);
        }
    }

    static <O> KeyVaultLongRunningOperation transformToLongRunningOperation(O operation, ClientLogger logger) {
        if (operation instanceof RestoreOperation) {
            RestoreOperation restoreOperation = (RestoreOperation) operation;

            return new KeyVaultRestoreOperation(restoreOperation.getStatus().getValue(),
                restoreOperation.getStatusDetails(), toKeyVaultAdministrationError(restoreOperation.getError()),
                restoreOperation.getJobId(), restoreOperation.getStartTime(), restoreOperation.getEndTime());
        } else if (operation instanceof SelectiveKeyRestoreOperation) {
            SelectiveKeyRestoreOperation selectiveKeyRestoreOperation = (SelectiveKeyRestoreOperation) operation;

            return new KeyVaultSelectiveKeyRestoreOperation(selectiveKeyRestoreOperation.getStatus().getValue(),
                selectiveKeyRestoreOperation.getStatusDetails(),
                toKeyVaultAdministrationError(selectiveKeyRestoreOperation.getError()),
                selectiveKeyRestoreOperation.getJobId(), selectiveKeyRestoreOperation.getStartTime(),
                selectiveKeyRestoreOperation.getEndTime());
        } else if (operation instanceof FullBackupOperation) {
            FullBackupOperation fullBackupOperation = (FullBackupOperation) operation;

            return new KeyVaultBackupOperation(fullBackupOperation.getStatus().getValue(),
                fullBackupOperation.getStatusDetails(), toKeyVaultAdministrationError(fullBackupOperation.getError()),
                fullBackupOperation.getJobId(), fullBackupOperation.getStartTime(), fullBackupOperation.getEndTime(),
                fullBackupOperation.getAzureStorageBlobContainerUri());
        } else {
            throw logger.throwableAtError()
                .addKeyValue("operationType", operation == null ? null : operation.getClass().getCanonicalName())
                .log(UnsupportedOperationException::new);
        }
    }

    static <T, S> Response<S> mapResponse(Response<T> response, Function<T, S> mapper) {
        if (response == null) {
            return null;
        }

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            mapper.apply(response.getValue()));
    }

    static <T, S> PagedIterable<S> mapPages(Function<PagingOptions, PagedResponse<T>> firstPageRetriever,
        BiFunction<PagingOptions, String, PagedResponse<T>> nextPageRetriever, Function<T, S> mapper) {

        return new PagedIterable<>(pageSize -> mapPagedResponse(firstPageRetriever.apply(pageSize), mapper),
            (continuationToken, pageSize) -> mapPagedResponse(nextPageRetriever.apply(continuationToken, pageSize),
                mapper));
    }

    static <T, S> PagedResponse<S> mapPagedResponse(PagedResponse<T> pagedResponse, Function<T, S> mapper) {
        if (pagedResponse == null) {
            return null;
        }

        return new PagedResponse<>(pagedResponse.getRequest(), pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue()
                .stream()
                .map(mapper)
                .collect(Collectors.toCollection(() -> new ArrayList<>(pagedResponse.getValue().size()))),
            pagedResponse.getContinuationToken(), null, null, null, null);
    }
}
