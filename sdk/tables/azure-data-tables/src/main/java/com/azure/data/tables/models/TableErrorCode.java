// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for error codes returned from the Tables service. For detailed information about what each error
 * code means and their accompanying status codes, visit the following page:
 * <a href="https://docs.microsoft.com/rest/api/storageservices/table-service-error-codes">Table service error codes</a>
 */
public final class TableErrorCode extends ExpandableStringEnum<TableErrorCode> {
    /**
     * Static value {@code AuthorizationResourceTypeMismatch}.
     */
    public static final TableErrorCode AUTHORIZATION_RESOURCE_TYPE_MISMATCH =
        fromString("AuthorizationResourceTypeMismatch");

    /**
     * Static value {@code AuthorizationPermissionMismatch}.
     */
    public static final TableErrorCode AUTHORIZATION_PERMISSION_MISMATCH =
        fromString("AuthorizationPermissionMismatch");

    /**
     * Static value {@code XMethodNotUsingPost}.
     */
    public static final TableErrorCode X_METHOD_NOT_USING_POST = fromString("XMethodNotUsingPost");

    /**
     * Static value {@code XMethodIncorrectValue}.
     */
    public static final TableErrorCode X_METHOD_INCORRECT_VALUE = fromString("XMethodIncorrectValue");

    /**
     * Static value {@code XMethodIncorrectCount}.
     */
    public static final TableErrorCode X_METHOD_INCORRECT_COUNT = fromString("XMethodIncorrectCount");

    /**
     * Static value {@code TableHasNoProperties}.
     */
    public static final TableErrorCode TABLE_HAS_NO_PROPERTIES = fromString("TableHasNoProperties");

    /**
     * Static value {@code DuplicatePropertiesSpecified}.
     */
    public static final TableErrorCode DUPLICATE_PROPERTIES_SPECIFIED =
        fromString("DuplicatePropertiesSpecified");

    /**
     * Static value {@code TableHasNoSuchProperty}.
     */
    public static final TableErrorCode TABLE_HAS_NO_SUCH_PROPERTY = fromString("TableHasNoSuchProperty");

    /**
     * Static value {@code DuplicateKeyPropertySpecified}.
     */
    public static final TableErrorCode DUPLICATE_KEY_PROPERTY_SPECIFIED =
        fromString("DuplicateKeyPropertySpecified");

    /**
     * Static value {@code TableAlreadyExists}.
     */
    public static final TableErrorCode TABLE_ALREADY_EXISTS = fromString("TableAlreadyExists");

    /**
     * Static value {@code TableNotFound}.
     */
    public static final TableErrorCode TABLE_NOT_FOUND = fromString("TableNotFound");

    /**
     * Static value {@code TableNotFound}.
     */
    public static final TableErrorCode RESOURCE_NOT_FOUND = fromString("ResourceNotFound");

    /**
     * Static value {@code EntityNotFound}.
     */
    public static final TableErrorCode ENTITY_NOT_FOUND = fromString("EntityNotFound");

    /**
     * Static value {@code EntityAlreadyExists}.
     */
    public static final TableErrorCode ENTITY_ALREADY_EXISTS = fromString("EntityAlreadyExists");

    /**
     * Static value {@code PartitionKeyNotSpecified}.
     */
    public static final TableErrorCode PARTITION_KEY_NOT_SPECIFIED = fromString("PartitionKeyNotSpecified");

    /**
     * Static value {@code OperatorInvalid}.
     */
    public static final TableErrorCode OPERATOR_INVALID = fromString("OperatorInvalid");

    /**
     * Static value {@code UpdateConditionNotSatisfied}.
     */
    public static final TableErrorCode UPDATE_CONDITION_NOT_SATISFIED = fromString("UpdateConditionNotSatisfied");

    /**
     * Static value {@code PropertiesNeedValue}.
     */
    public static final TableErrorCode PROPERTIES_NEED_VALUE = fromString("PropertiesNeedValue");

    /**
     * Static value {@code PartitionKeyPropertyCannotBeUpdated}.
     */
    public static final TableErrorCode PARTITION_KEY_PROPERTY_CANNOT_BE_UPDATED =
        fromString("PartitionKeyPropertyCannotBeUpdated");

    /**
     * Static value {@code TooManyProperties}.
     */
    public static final TableErrorCode TOO_MANY_PROPERTIES = fromString("TooManyProperties");

    /**
     * Static value {@code EntityTooLarge}.
     */
    public static final TableErrorCode ENTITY_TOO_LARGE = fromString("EntityTooLarge");

    /**
     * Static value {@code PropertyValueTooLarge}.
     */
    public static final TableErrorCode PROPERTY_VALUE_TOO_LARGE = fromString("PropertyValueTooLarge");

    /**
     * Static value {@code KeyValueTooLarge}.
     */
    public static final TableErrorCode KEY_VALUE_TOO_LARGE = fromString("KeyValueTooLarge");

    /**
     * Static value {@code InvalidValueType}.
     */
    public static final TableErrorCode INVALID_VALUE_TYPE = fromString("InvalidValueType");

    /**
     * Static value {@code TableBeingDeleted}.
     */
    public static final TableErrorCode TABLE_BEING_DELETED = fromString("TableBeingDeleted");

    /**
     * Static value {@code PrimaryKeyPropertyIsInvalidType}.
     */
    public static final TableErrorCode PRIMARY_KEY_PROPERTY_IS_INVALID_TYPE =
        fromString("PrimaryKeyPropertyIsInvalidType");

    /**
     * Static value {@code PropertyNameTooLong}.
     */
    public static final TableErrorCode PROPERTY_NAME_TOO_LONG = fromString("PropertyNameTooLong");

    /**
     * Static value {@code PropertyNameInvalid}.
     */
    public static final TableErrorCode PROPERTY_NAME_INVALID = fromString("PropertyNameInvalid");

    /**
     * Static value {@code InvalidDuplicateRow}.
     */
    public static final TableErrorCode INVALID_DUPLICATE_ROW = fromString("InvalidDuplicateRow");

    /**
     * Static value {@code CommandsInBatchActOnDifferentPartitions}.
     */
    public static final TableErrorCode COMMANDS_IN_BATCH_ACT_ON_DIFFERENT_PARTITIONS =
        fromString("CommandsInBatchActOnDifferentPartitions");

    /**
     * Static value {@code JsonFormatNotSupported}.
     */
    public static final TableErrorCode JSON_FORMAT_NOT_SUPPORTED = fromString("JsonFormatNotSupported");

    /**
     * Static value {@code AtomFormatNotSupported}.
     */
    public static final TableErrorCode ATOM_FORMAT_NOT_SUPPORTED = fromString("AtomFormatNotSupported");

    /**
     * Static value {@code JsonVerboseFormatNotSupported}.
     */
    public static final TableErrorCode JSON_VERBOSE_FORMAT_NOT_SUPPORTED =
        fromString("JsonVerboseFormatNotSupported");

    /**
     * Static value {@code MediaTypeNotSupported}.
     */
    public static final TableErrorCode MEDIA_TYPE_NOT_SUPPORTED = fromString("MediaTypeNotSupported");

    /**
     * Static value {@code MethodNotAllowed}.
     */
    public static final TableErrorCode METHOD_NOT_ALLOWED = fromString("MethodNotAllowed");

    /**
     * Static value {@code ContentLengthExceeded}.
     */
    public static final TableErrorCode CONTENT_LENGTH_EXCEEDED = fromString("ContentLengthExceeded");

    /**
     * Static value {@code AccountIOPSLimitExceeded}.
     */
    public static final TableErrorCode ACCOUNT_IOPS_LIMIT_EXCEEDED = fromString("AccountIOPSLimitExceeded");

    /**
     * Static value {@code CannotCreateTableWithIOPSGreaterThanMaxAllowedPerTable}.
     */
    public static final TableErrorCode CANNOT_CREATE_TABLE_WITH_IOPS_GREATER_THAN_MAX_ALLOWED_PER_TABLE =
        fromString("CannotCreateTableWithIOPSGreaterThanMaxAllowedPerTable");

    /**
     * Static value {@code PerTableIOPSIncrementLimitReached}.
     */
    public static final TableErrorCode PER_TABLE_IOPS_INCREMENT_LIMIT_REACHED =
        fromString("PerTableIOPSIncrementLimitReached");

    /**
     * Static value {@code PerTableIOPSDecrementLimitReached}.
     */
    public static final TableErrorCode PER_TABLE_IOPS_DECREMENT_LIMIT_REACHED =
        fromString("PerTableIOPSDecrementLimitReached");

    /**
     * Static value {@code SettingIOPSForATableInProvisioningNotAllowed}.
     */
    public static final TableErrorCode SETTING_IOPS_FOR_A_TABLE_IN_PROVISIONING_NOT_ALLOWED =
        fromString("SettingIOPSForATableInProvisioningNotAllowed");

    /**
     * Static value {@code PartitionKeyEqualityComparisonExpected}.
     */
    public static final TableErrorCode PARTITION_KEY_EQUALITY_COMPARISON_EXPECTED =
        fromString("PartitionKeyEqualityComparisonExpected");

    /**
     * Static value {@code PartitionKeySpecifiedMoreThanOnce}.
     */
    public static final TableErrorCode PARTITION_KEY_SPECIFIED_MORE_THAN_ONCE =
        fromString("PartitionKeySpecifiedMoreThanOnce");

    /**
     * Static value {@code InvalidInput}.
     */
    public static final TableErrorCode INVALID_INPUT = fromString("InvalidInput");

    /**
     * Static value {@code NotImplemented}.
     */
    public static final TableErrorCode NOT_IMPLEMENTED = fromString("NotImplemented");

    /**
     * Static value {@code OperationTimedOut}.
     */
    public static final TableErrorCode OPERATION_TIMED_OUT = fromString("OperationTimedOut");

    /**
     * Static value {@code OutOfRangeInput}.
     */
    public static final TableErrorCode OUT_OF_RANGE_INPUT = fromString("OutOfRangeInput");

    /**
     * Static value {@code Forbidden}.
     */
    public static final TableErrorCode FORBIDDEN = fromString("Forbidden");

    /**
     * Returns the {@code TableErrorCode} constant with the provided name, or {@code null} if no {@code TableErrorCode}
     * has the provided name.
     *
     * @param name The name of the error.
     *
     * @return The {@code TableErrorCode} value having the provided name.
     */
    public static TableErrorCode fromString(String name) {
        return fromString(name, TableErrorCode.class);
    }
}
