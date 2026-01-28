// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.security.keyvault.administration.implementation.models.FullBackupOperation;
import com.azure.security.keyvault.administration.implementation.models.Permission;
import com.azure.security.keyvault.administration.implementation.models.RestoreOperation;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignment;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentCreateParameters;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentProperties;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentPropertiesWithScope;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinition;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionCreateParameters;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionProperties;
import com.azure.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultDataAction;
import com.azure.security.keyvault.administration.models.KeyVaultLongRunningOperation;
import com.azure.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinitionType;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleType;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.SetRoleDefinitionOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils.toKeyVaultAdministrationError;

/**
 * Internal utility class for KeyVault Administration clients.
 */
class KeyVaultAdministrationUtil {
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    static final String VAULT_END_POINT_REQUIRED = "Azure Key Vault endpoint url is required.";
    static final String PARAMETER_REQUIRED = "%s cannot be null.";

    public static final RequestOptions EMPTY_OPTIONS = new RequestOptions();

    /**
     * Deserializes a given {@link Response HTTP response} including headers to a given class.
     *
     * @param statusCode The status code which will trigger exception swallowing.
     * @param httpResponseException The {@link HttpResponseException} to be swallowed.
     * @param logger {@link ClientLogger} that will be used to record the exception.
     * @param <E> The class of the exception to swallow.
     *
     * @return A {@link Mono} that contains the deserialized response.
     */
    static <E extends HttpResponseException> Mono<Response<Void>> swallowExceptionForStatusCodeAsync(int statusCode,
        E httpResponseException, ClientLogger logger) {
        try {
            return Mono.just(swallowExceptionForStatusCodeSync(statusCode, httpResponseException, logger));
        } catch (RuntimeException e) {
            return Mono.error(e);
        }
    }

    /**
     * Synchronously deserializes a given {@link Response HTTP response} including headers to a given class.
     *
     * @param statusCode The status code which will trigger exception swallowing.
     * @param httpResponseException The {@link HttpResponseException} to be swallowed.
     * @param logger {@link ClientLogger} that will be used to record the exception.
     * @param <E> The class of the exception to swallow.
     *
     * @return the deserialized response.
     */
    static <E extends HttpResponseException> Response<Void> swallowExceptionForStatusCodeSync(int statusCode,
        E httpResponseException, ClientLogger logger) {

        HttpResponse httpResponse = httpResponseException.getResponse();

        if (httpResponse.getStatusCode() == statusCode) {
            return new SimpleResponse<>(httpResponse.getRequest(), httpResponse.getStatusCode(),
                httpResponse.getHeaders(), null);
        }

        throw logger.logExceptionAsError(httpResponseException);
    }

    static RoleAssignmentCreateParameters validateAndGetRoleAssignmentCreateParameters(KeyVaultRoleScope roleScope,
        String roleDefinitionId, String principalId, String roleAssignmentName) {
        validateRoleAssignmentParameters(roleScope, roleAssignmentName);
        Objects.requireNonNull(principalId, String.format(PARAMETER_REQUIRED, "'principalId'"));
        Objects.requireNonNull(roleDefinitionId, String.format(PARAMETER_REQUIRED, "'roleDefinitionId'"));

        RoleAssignmentProperties roleAssignmentProperties = new RoleAssignmentProperties(roleDefinitionId, principalId);
        return new RoleAssignmentCreateParameters(roleAssignmentProperties);
    }

    static RoleDefinitionCreateParameters
        validateAndGetRoleDefinitionCreateParameters(SetRoleDefinitionOptions options) {
        Objects.requireNonNull(options, String.format(PARAMETER_REQUIRED, "'options'"));
        Objects.requireNonNull(options.getRoleScope(), String.format(PARAMETER_REQUIRED, "'options.getRoleScope()'"));
        Objects.requireNonNull(options.getRoleDefinitionName(),
            String.format(PARAMETER_REQUIRED, "'options.getRoleDefinitionName()'"));

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

    static void validateRoleAssignmentParameters(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        Objects.requireNonNull(roleScope, String.format(PARAMETER_REQUIRED, "'roleScope'"));
        Objects.requireNonNull(roleAssignmentName, String.format(PARAMETER_REQUIRED, "'roleAssignmentName'"));
    }

    static void validateRoleDefinitionParameters(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        Objects.requireNonNull(roleScope, String.format(PARAMETER_REQUIRED, "'roleScope'"));
        Objects.requireNonNull(roleDefinitionName, String.format(PARAMETER_REQUIRED, "'roleDefinitionName'"));
    }

    @SuppressWarnings("BoundedWildcard")
    static PagedResponse<KeyVaultRoleDefinition>
        transformRoleDefinitionsPagedResponse(PagedResponse<RoleDefinition> pagedResponse) {

        List<KeyVaultRoleDefinition> keyVaultRoleDefinitions = new ArrayList<>();

        for (RoleDefinition roleDefinition : pagedResponse.getValue()) {
            keyVaultRoleDefinitions.add(roleDefinitionToKeyVaultRoleDefinition(roleDefinition));
        }

        return new TransformedPagedResponse<>(keyVaultRoleDefinitions, pagedResponse);
    }

    static <T> Response<T> transformBinaryDataResponse(Response<BinaryData> response, Class<T> clazz) {
        if (response == null) {
            return null;
        }

        BinaryData binaryData = response.getValue();

        if (binaryData == null) {
            return new SimpleResponse<>(response, null);
        }

        return new SimpleResponse<>(response, BinaryData.fromObject(response.getValue()).toObject(clazz));
    }

    static <T> Response<T> transformBinaryDataResponse(Response<BinaryData> response,
        Function<BinaryData, T> transformationFunction) {
        if (response == null) {
            return null;
        }

        BinaryData binaryData = response.getValue();

        if (binaryData == null) {
            return new SimpleResponse<>(response, null);
        }

        return new SimpleResponse<>(response, transformationFunction.apply(binaryData));
    }

    static KeyVaultRoleDefinition roleDefinitionToKeyVaultRoleDefinition(RoleDefinition roleDefinition) {
        List<KeyVaultPermission> keyVaultPermissions = new ArrayList<>();

        for (Permission permission : roleDefinition.getProperties().getPermissions()) {
            keyVaultPermissions.add(new KeyVaultPermission(permission.getActions(), permission.getNotActions(),
                permission.getDataActions()
                    .stream()
                    .map(dataAction -> KeyVaultDataAction.fromString(dataAction.toString()))
                    .collect(Collectors.toList()),
                permission.getNotDataActions()
                    .stream()
                    .map(notDataAction -> KeyVaultDataAction.fromString(notDataAction.toString()))
                    .collect(Collectors.toList())));
        }

        return new KeyVaultRoleDefinition(roleDefinition.getId(), roleDefinition.getName(),
            KeyVaultRoleDefinitionType.fromString(roleDefinition.getType().toString()),
            roleDefinition.getProperties().getRoleName(), roleDefinition.getProperties().getDescription(),
            KeyVaultRoleType.fromString(roleDefinition.getProperties().getRoleType().toString()), keyVaultPermissions,
            roleDefinition.getProperties()
                .getAssignableScopes()
                .stream()
                .map(roleScope -> KeyVaultRoleScope.fromString(roleScope.toString()))
                .collect(Collectors.toList()));
    }

    static KeyVaultRoleAssignment roleAssignmentToKeyVaultRoleAssignment(RoleAssignment roleAssignment) {
        RoleAssignmentPropertiesWithScope propertiesWithScope = roleAssignment.getProperties();

        return new KeyVaultRoleAssignment(roleAssignment.getId(), roleAssignment.getName(), roleAssignment.getType(),
            new KeyVaultRoleAssignmentProperties(propertiesWithScope.getRoleDefinitionId(),
                propertiesWithScope.getPrincipalId(),
                KeyVaultRoleScope.fromString(propertiesWithScope.getScope().toString())));
    }

    private static final class TransformedPagedResponse<L extends List<T>, T, U> implements PagedResponse<T> {
        private final L output;
        private final PagedResponse<U> pagedResponse;

        TransformedPagedResponse(L output, PagedResponse<U> inputPagedResponse) {
            this.output = output;
            this.pagedResponse = inputPagedResponse;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public IterableStream<T> getElements() {
            return new IterableStream<>(output);
        }

        @Override
        public String getContinuationToken() {
            return pagedResponse.getContinuationToken();
        }

        @Override
        public int getStatusCode() {
            return pagedResponse.getStatusCode();
        }

        @Override
        public HttpHeaders getHeaders() {
            return pagedResponse.getHeaders();
        }

        @Override
        public HttpRequest getRequest() {
            return pagedResponse.getRequest();
        }

        @Override
        public List<T> getValue() {
            return output;
        }
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

    static <O> KeyVaultLongRunningOperation transformToLongRunningOperation(O operation) {
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
            throw new UnsupportedOperationException();
        }
    }

    static <T, R> PagedResponse<R> mapPagedResponse(PagedResponse<T> page, Function<T, R> itemMapper) {
        List<R> mappedValues = new ArrayList<>(page.getValue().size());

        for (T item : page.getValue()) {
            mappedValues.add(itemMapper.apply(item));
        }

        return new PagedResponseBase<>(page.getRequest(), page.getStatusCode(), page.getHeaders(), mappedValues,
            page.getContinuationToken(), null);
    }
}
