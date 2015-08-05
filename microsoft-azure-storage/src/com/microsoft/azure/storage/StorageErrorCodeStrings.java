/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage;

/**
 * Represents common error code strings for Azure Storage.
 */
public final class StorageErrorCodeStrings {
    /**
     * The specified account already exists.
     */
    public static final String ACCOUNT_ALREADY_EXISTS = "AccountAlreadyExists";

    /**
     * The specified account is in the process of being created.
     */
    public static final String ACCOUNT_BEING_CREATED = "AccountBeingCreated";

    /**
     * The specified account is disabled.
     */
    public static final String ACCOUNT_IS_DISABLED = "AccountIsDisabled";

    /**
     * Authentication failed.
     */
    public static final String AUTHENTICATION_FAILED = "AuthenticationFailed";

    /**
     * The specified blob already exists.
     */
    public static final String BLOB_ALREADY_EXISTS = "BlobAlreadyExists";

    /**
     * The specified blob does not exist.
     */
    public static final String BLOB_NOT_FOUND = "BlobNotFound";

    /**
     * Could not verify the copy source within the specified time. Examine the HTTP status code and message for more
     * information about the failure.
     */
    public static final String CANNOT_VERIFY_COPY_SOURCE = "CannotVerifyCopySource";

    /**
     * The file or directory could not be deleted because it is in use by an SMB client.
     */
    public static final String CANNOT_DELETE_FILE_OR_DIRECTORY = "CannotDeleteFileOrDirectory";

    /**
     * The specified resource state could not be flushed from an SMB client in the specified time.
     */
    public static final String CLIENT_CACHE_FLUSH_DELAY = "ClientCacheFlushDelay";

    /**
     * Condition headers are not supported.
     */
    public static final String CONDITION_HEADERS_NOT_SUPPORTED = "ConditionHeadersNotSupported";

    /**
     * The specified condition was not met.
     */
    public static final String CONDITION_NOT_MET = "ConditionNotMet";

    /**
     * The specified container already exists.
     */
    public static final String CONTAINER_ALREADY_EXISTS = "ContainerAlreadyExists";

    /**
     * The specified container is being deleted.
     */
    public static final String CONTAINER_BEING_DELETED = "ContainerBeingDeleted";

    /**
     * The specified container is disabled.
     */
    public static final String CONTAINER_DISABLED = "ContainerDisabled";

    /**
     * The specified container was not found.
     */
    public static final String CONTAINER_NOT_FOUND = "ContainerNotFound";

    /**
     * The copy source account and destination account must be the same.
     */
    public static final String COPY_ACROSS_ACCOUNTS_NOT_SUPPORTED = "CopyAcrossAccountsNotSupported";

    /**
     * The specified copy ID did not match the copy ID for the pending copy operation.
     */
    public static final String COPY_ID_MISMATCH = "CopyIdMismatch";

    /**
     * The specified resource is marked for deletion by an SMB client.
     */
    public static final String DELETE_PENDING = "DeletePending";

    /**
     * The specified directory already exists.
     */
    public static final String DIRECTORY_ALREADY_EXISTS = "DirectoryAlreadyExists";

    /**
     * The specified directory is not empty.
     */
    public static final String DIRECTORY_NOT_EMPTY = "DirectoryNotEmpty";

    /**
     * A property is specified more than one time.
     */
    public static final String DUPLICATE_PROPERTIES_SPECIFIED = "DuplicatePropertiesSpecified";
    /**
     * The metadata key is empty.
     */
    public static final String EMPTY_METADATA_KEY = "EmptyMetadataKey";

    /**
     * The entity already exists
     */
    public static final String ENTITY_ALREADY_EXISTS = "EntityAlreadyExists";

    /**
     * The entity already exists
     */
    public static final String ENTITY_TOO_LARGE = "EntityTooLarge";

    /**
     * A portion of the specified file is locked by an SMB client.
     */
    public static final String FILE_LOCK_CONFLICT = "FileLockConflict";

    /**
     * The required host information is not present in the request. You must send a non-empty Host header or include the
     * absolute URI in the request line.
     */
    public static final String HOST_INFORMATION_NOT_PRESENT = "HostInformationNotPresent";

    /**
     * An incorrect blob type was specified.
     */
    public static final String INCORRECT_BLOB_TYPE = "IncorrectBlobType";

    /**
     * The lease ID matched, but the specified lease must be an infinite-duration lease.
     */
    public static final String INFINITE_LEASE_DURATION_REQUIRED = "InfiniteLeaseDurationRequired";

    /**
     * The account being accessed does not have sufficient permissions to execute this operation.
     */
    public static final String INSUFFICIENT_ACCOUNT_PERMISSIONS = "InsufficientAccountPermissions";

    /**
     * An internal error occurred.
     */
    public static final String INTERNAL_ERROR = "InternalError";

    /**
     * The authentication information was not provided in the correct format. Verify the value of Authorization header.
     */
    public static final String INVALID_AUTHENTICATION_INFO = "InvalidAuthenticationInfo";

    /**
     * Error code that may be returned when the specified append offset is invalid.
     */
    public static final String INVALID_APPEND_POSITION = "AppendPositionConditionNotMet";
    
    /**
     * An incorrect blob type was specified.
     */
    public static final String INVALID_BLOB_TYPE = "InvalidBlobType";

    /**
     * The specified blob or block content is invalid.
     */
    public static final String INVALID_BLOB_OR_BLOCK = "InvalidBlobOrBlock";

    /**
     * The specified block ID is invalid. The block ID must be Base64-encoded.
     */
    public static final String INVALID_BLOCK_ID = "InvalidBlockId";

    /**
     * The specified block list is invalid.
     */
    public static final String INVALID_BLOCK_LIST = "InvalidBlockList";

    /**
     * One or more header values are invalid.
     */
    public static final String INVALID_HEADER_VALUE = "InvalidHeaderValue";

    /**
     * The HTTP verb is invalid.
     */
    public static final String INVALID_HTTP_VERB = "InvalidHttpVerb";

    /**
     * The input is invalid.
     */
    public static final String INVALID_INPUT = "InvalidInput";

    /**
     * The specified marker is invalid.
     */
    public static final String INVALID_MARKER = "InvalidMarker";

    /**
     * Error code that may be returned when the specified max blob size is exceeded.
     */
    public static final String INVALID_MAX_BLOB_SIZE_CONDITION = "MaxBlobSizeConditionNotMet"; 

    /**
     * The specified MD5 hash is invalid.
     */
    public static final String INVALID_MD5 = "InvalidMd5";

    /**
     * The specified metadata is invalid.
     */
    public static final String INVALID_METADATA = "InvalidMetadata";

    /**
     * The page range specified is invalid.
     */
    public static final String INVALID_PAGE_RANGE = "InvalidPageRange";

    /**
     * One or more query parameters are invalid.
     */
    public static final String INVALID_QUERY_PARAMETER_VALUE = "InvalidQueryParameterValue";

    /**
     * The specified range is invalid.
     */
    public static final String INVALID_RANGE = "InvalidRange";

    /**
     * The specified resource name contains invalid characters.
     */
    public static final String INVALID_RESOURCE_NAME = "InvalidResourceName";

    /**
     * The URI is invalid.
     */
    public static final String INVALID_URI = "InvalidUri";

    /**
     * The value specified is invalid.
     */
    public static final String INVALID_VALUE_TYPE = "InvalidValueType";

    /**
     * All operations on page blobs require at least version 2009-09-19.
     */
    public static final String INVALID_VERSION_FOR_PAGE_BLOB_OPERATION = "InvalidVersionForPageBlobOperation";

    /**
     * The specified XML document is invalid.
     */
    public static final String INVALID_XML_DOCUMENT = "InvalidXmlDocument";

    /**
     * The value provided for one of the XML nodes in the request body was not in the correct format.
     */
    public static final String INVALID_XML_NODE_VALUE = "InvalidXmlNodeValue";

    /**
     * The specified XML or Json document is invalid. Used for tables only.
     */
    public static final String INVALID_DOCUMENT = "InvalidDocument";

    /**
     * File or directory path is too long or file or directory path has too many subdirectories.
     */
    public static final String INVALID_FILE_OR_DIRECTORY_PATH_NAME = "InvalidFileOrDirectoryPathName";

    /**
     * The incorrect type was given. Used for tables only.
     */
    public static final String INVALID_TYPE = "InvalidType";

    /**
     * JSON format is not supported.
     */
    public static final String JSON_FORMAT_NOT_SUPPORTED = "JsonFormatNotSupported";

    /**
     * The lease is already broken.
     */
    public static final String LEASE_ALREADY_BROKEN = "LeaseAlreadyBroken";

    /**
     * The lease is already present.
     */
    public static final String LEASE_ALREADY_PRESENT = "LeaseAlreadyPresent";

    /**
     * The lease ID is incorrect with a blob operation.
     */
    public static final String LEASE_ID_MISMATCH_WITH_BLOB_OPERATION = "LeaseIdMismatchWithBlobOperation";

    /**
     * The lease ID is incorrect with a container operation.
     */
    public static final String LEASE_ID_MISMATCH_WITH_CONTAINER_OPERATION = "LeaseIdMismatchWithContainerOperation";

    /**
     * The lease ID is incorrect with a lease operation.
     */
    public static final String LEASE_ID_MISMATCH_WITH_LEASE_OPERATION = "LeaseIdMismatchWithLeaseOperation";

    /**
     * The lease ID is missing.
     */
    public static final String LEASE_ID_MISSING = "LeaseIdMissing";

    /**
     * The lease ID matched, but the lease has been broken explicitly and cannot be renewed.
     */
    public static final String LEASE_IS_BROKEN_AND_CANNOT_BE_RENEWED = "LeaseIsBrokenAndCannotBeRenewed";

    /**
     * The lease ID matched, but the lease is currently in breaking state and cannot be acquired until it is broken.
     */
    public static final String LEASE_IS_BREAKING_AND_CANNOT_BE_ACQUIRED = "LeaseIsBreakingAndCannotBeAcquired";

    /**
     * The lease ID matched, but the lease is currently in breaking state and cannot be changed.
     */
    public static final String LEASE_IS_BREAKING_AND_CANNOT_BE_CHANGED = "LeaseIsBreakingAndCannotBeChanged";

    /**
     * A lease ID was specified, but the lease for the blob/container has expired.
     */
    public static final String LEASE_LOST = "LeaseLost";

    /**
     * There is currently no lease on the blob.
     */
    public static final String LEASE_NOT_PRESENT_WITH_BLOB_OPERATION = "LeaseNotPresentWithBlobOperation";

    /**
     * There is currently no lease on the container.
     */
    public static final String LEASE_NOT_PRESENT_WITH_CONTAINER_OPERATION = "LeaseNotPresentWithContainerOperation";

    /**
     * There is currently no lease on the blob/container.
     */
    public static final String LEASE_NOT_PRESENT_WITH_LEASE_OPERATION = "LeaseNotPresentWithLeaseOperation";

    /**
     * The specified MD5 hash does not match the server value.
     */
    public static final String MD5_MISMATCH = "Md5Mismatch";

    /**
     * The message exceeds the maximum allowed size.
     */
    public static final String MESSAGE_TOO_LARGE = "MessageTooLarge";

    /**
     * The specified message does not exist.
     */
    public static final String MESSAGE_NOT_FOUND = "MessageNotFound";

    /**
     * The specified metadata is too large.
     */
    public static final String METADATA_TOO_LARGE = "MetadataTooLarge";

    /**
     * The requested method is not allowed on the specified resource.
     */
    public static final String METHOD_NOT_ALLOWED = "MethodNotAllowed";

    /**
     * The Content-Length header is required for this request.
     */
    public static final String MISSING_CONTENT_LENGTH_HEADER = "MissingContentLengthHeader";

    /**
     * A required header was missing.
     */
    public static final String MISSING_REQUIRED_HEADER = "MissingRequiredHeader";

    /**
     * A required query parameter is missing.
     */
    public static final String MISSING_REQUIRED_QUERY_PARAMETER = "MissingRequiredQueryParameter";

    /**
     * A required XML node was missing.
     */
    public static final String MISSING_REQUIRED_XML_NODE = "MissingRequiredXmlNode";

    /**
     * The MD5 hash is missing.
     */
    public static final String MISSING_MD5_HEADER = "MissingContentMD5Header";

    /**
     * Multiple condition headers are not supported.
     */
    public static final String MULTIPLE_CONDITION_HEADERS_NOT_SUPPORTED = "MultipleConditionHeadersNotSupported";

    /**
     * There is currently no pending copy operation.
     */
    public static final String NO_PENDING_COPY_OPERATION = "NoPendingCopyOperation";

    /**
     * The requested operation is not implemented on the specified resource.
     */
    public static final String NOT_IMPLEMENTED = "NotImplemented";

    /**
     * The operation timed out.
     */
    public static final String OPERATION_TIMED_OUT = "OperationTimedOut";

    /**
     * The input is out of range.
     */
    public static final String OUT_OF_RANGE_INPUT = "OutOfRangeInput";

    /**
     * One or more query parameters are out of range.
     */
    public static final String OUT_OF_RANGE_QUERY_PARAMETER_VALUE = "OutOfRangeQueryParameterValue";

    /**
     * The specified parent path does not exist.
     */
    public static final String PARENT_NOT_FOUND = "ParentNotFound";

    /**
     * There is currently a pending copy operation.
     */
    public static final String PENDING_COPY_OPERATION = "PendingCopyOperation";

    /**
     * The specified pop receipt did not match the pop receipt for a dequeued message.
     */
    public static final String POP_RECEIPT_MISMATCH = "PopReceiptMismatch";

    /**
     * Values have not been specified for all properties in the entity.
     */
    public static final String PROPERTIES_NEED_VALUE = "PropertiesNeedValue";

    /**
     * The property name is invalid.
     */
    public static final String PROPERTY_NAME_INVALID = "PropertyNameInvalid";

    /**
     * The property name exceeds the maximum allowed length.
     */
    public static final String PROPERTY_NAME_TOO_LONG = "PropertyNameTooLong";

    /**
     * The property value is larger than the maximum size permitted.
     */
    public static final String PROPERTY_VALUE_TOO_LARGE = "PropertyValueTooLarge";

    /**
     * The specified queue already exists.
     */
    public static final String QUEUE_ALREADY_EXISTS = "QueueAlreadyExists";

    /**
     * The specified queue is being deleted.
     */
    public static final String QUEUE_BEING_DELETED = "QueueBeingDeleted";

    /**
     * The specified queue has been disabled by the administrator.
     */
    public static final String QUEUE_DISABLED = "QueueDisabled";

    /**
     * The specified queue is not empty.
     */
    public static final String QUEUE_NOT_EMPTY = "QueueNotEmpty";

    /**
     * The specified queue does not exist.
     */
    public static final String QUEUE_NOT_FOUND = "QueueNotFound";

    /**
     * The specified resource is read-only and cannot be modified at this time.
     */
    public static final String READ_ONLY_ATTRIBUTE = "ReadOnlyAttribute";

    /**
     * The request body is too large.
     */
    public static final String REQUEST_BODY_TOO_LARGE = "RequestBodyTooLarge";

    /**
     * The url in the request could not be parsed.
     */
    public static final String REQUEST_URL_FAILED_TO_PARSE = "RequestUrlFailedToParse";

    /**
     * The specified resource was not found.
     */
    public static final String RESOURCE_NOT_FOUND = "ResourceNotFound";

    /**
     * The specified resource already exists.
     */
    public static final String RESOURCE_ALREADY_EXISTS = "ResourceAlreadyExists";

    /**
     * The specified resource type does not match the type of the existing resource.
     */
    public static final String RESOURCE_TYPE_MISMATCH = "ResourceTypeMismatch";

    /**
     * The sequence number condition specified was not met.
     */
    public static final String SEQUENCE_NUMBER_CONDITION_NOT_MET = "SequenceNumberConditionNotMet";

    /**
     * The sequence number increment cannot be performed because it would result in overflow of the sequence number.
     */
    public static final String SEQUENCE_NUMBER_INCREMENT_TOO_LARGE = "SequenceNumberIncrementTooLarge";

    /**
     * The server is busy.
     */
    public static final String SERVER_BUSY = "ServerBusy";

    /**
     * The specified share already exists.
     */
    public static final String SHARE_ALREADY_EXISTS = "ShareAlreadyExists";

    /**
     * The specified share is being deleted. Try operation later.
     */
    public static final String SHARE_BEING_DELETED = "ShareBeingDeleted";

    /**
     * The specified share is disabled by the administrator.
     */
    public static final String SHARE_DISABLED = "ShareDisabled";

    /**
     * The specified share was not found.
     */
    public static final String SHARE_NOT_FOUND = "ShareNotFound";

    /**
     * The specified resource may be in use by an SMB client.
     */
    public static final String SHARING_VIOLATION = "SharingViolation";

    /**
     * This operation is not permitted because the blob has snapshots.
     */
    public static final String SNAPSHOTS_PRESENT = "SnapshotsPresent";

    /**
     * The source condition specified using HTTP conditional header(s) is not met.
     */
    public static final String SOURCE_CONDITION_NOT_MET = "SourceConditionNotMet";

    /**
     * The target condition specified using HTTP conditional header(s) is not met.
     */
    public static final String TARGET_CONDITION_NOT_MET = "TargetConditionNotMet";

    /**
     * The table specified already exists.
     */
    public static final String TABLE_ALREADY_EXISTS = "TableAlreadyExists";

    /**
     * The specified table is being deleted.
     */
    public static final String TABLE_BEING_DELETED = "TableBeingDeleted";

    /**
     * The table specified does not exist.
     */
    public static final String TABLE_NOT_FOUND = "TableNotFound";

    /**
     * The entity contains more properties than allowed.
     */
    public static final String TOO_MANY_PROPERTIES = "TooManyProperties";

    /**
     * The update condition was not satisfied
     */
    public static final String UPDATE_CONDITION_NOT_SATISFIED = "UpdateConditionNotSatisfied";

    /**
     * One or more header values are not supported.
     */
    public static final String UNSUPPORTED_HEADER = "UnsupportedHeader";

    /**
     * One of the XML nodes specified in the request body is not supported.
     */
    public static final String UNSUPPORTED_XML_NODE = "UnsupportedXmlNode";

    /**
     * The specified HTTP verb is not supported.
     */
    public static final String UNSUPPORTED_HTTP_VERB = "UnsupportedHttpVerb";

    /**
     * One or more query parameters is not supported.
     */
    public static final String UNSUPPORTED_QUERY_PARAMETER = "UnsupportedQueryParameter";

    /**
     * More than one X-HTTP-Method is specified.
     */
    public static final String X_METHOD_INCORRECT_COUNT = "XMethodIncorrectCount";

    /**
     * The specified X-HTTP-Method is invalid.
     */
    public static final String X_METHOD_INCORRECT_VALUE = "XMethodIncorrectValue";

    /**
     * The request uses X-HTTP-Method with an HTTP verb other than POST.
     */
    public static final String X_METHOD_NOT_USING_POST = "XMethodNotUsingPost";

    /**
     * Private Default Constructor.
     */
    private StorageErrorCodeStrings() {
        // No op
    }
}
