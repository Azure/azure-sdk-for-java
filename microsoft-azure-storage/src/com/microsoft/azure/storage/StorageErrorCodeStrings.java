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
 * Represents error code strings that are common to all storage services.
 */
public final class StorageErrorCodeStrings {
    /**
     * Authentication failed.
     */
    public static final String AUTHENTICATION_FAILED = "AuthenticationFailed";

    /**
     * The specified blob does not exist.
     */
    public static final String BLOB_NOT_FOUND = "BlobNotFound";

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
     * An incorrect blob type was specified.
     */
    public static final String INCORRECT_BLOB_TYPE = "IncorrectBlobType";

    /**
     * An internal error occurred.
     */
    public static final String INTERNAL_ERROR = "InternalError";

    /**
     * An incorrect blob type was specified.
     */
    public static final String INVALID_BLOB_TYPE = "InvalidBlobType";

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
     * The specified MD5 hash is invalid.
     */
    public static final String INVALID_MD5 = "InvalidMd5";

    /**
     * The specified metadata is invalid.
     */
    public static final String INVALID_METADATA = "InvalidMetadata";

    /**
     * One or more query parameters are invalid.
     */
    public static final String INVALID_QUERY_PARAMETER_VALUE = "InvalidQueryParameterValue";

    /**
     * The specified range is invalid.
     */
    public static final String INVALID_RANGE = "InvalidRange";

    /**
     * The URI is invalid.
     */
    public static final String INVALID_URI = "InvalidUri";

    /**
     * The specified XML document is invalid.
     */
    public static final String INVALID_XML_DOCUMENT = "InvalidXmlDocument";

    /**
     * The specified XML or Json document is invalid. Used for tables only.
     */
    public static final String INVALID_DOCUMENT = "InvalidDocument";

    /**
     * The incorrect type was given. Used for tables only.
     */
    public static final String INVALID_TYPE = "InvalidType";

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
     * The lease ID is incorrect with a lease operation.
     */
    public static final String LEASE_ID_MISMATCH_WITH_LEASE_OPERATION = "LeaseIdMismatchWithLeaseOperation";

    /**
     * The lease ID is missing.
     */
    public static final String LEASE_ID_MISSING = "LeaseIdMissing";

    /**
     * The specified MD5 hash does not match the server value.
     */
    public static final String MD5_MISMATCH = "Md5Mismatch";

    /**
     * The specified metadata is too large.
     */
    public static final String METADATA_TOO_LARGE = "MetadataTooLarge";

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
     * The specified queue already exists.
     */
    public static final String QUEUE_ALREADY_EXISTS = "QueueAlreadyExists";

    /**
     * The specified queue is being deleted.
     */
    public static final String QUEUE_BEING_DELETED = "QueueBeingDeleted";

    /**
     * The specified queue does not exist.
     */
    public static final String QUEUE_NOT_FOUND = "QueueNotFound";

    /**
     * The request body is too large.
     */
    public static final String REQUEST_BODY_TOO_LARGE = "RequestBodyTooLarge";

    /**
     * The specified resource was not found.
     */
    public static final String RESOURCE_NOT_FOUND = "ResourceNotFound";

    /**
     * The server is busy.
     */
    public static final String SERVER_BUSY = "ServerBusy";

    /**
     * Table Already Exists
     */
    public static final String TABLE_ALREADY_EXISTS = "TableAlreadyExists";

    /**
     * The update condition was not satisfied
     */
    public static final String UPDATE_CONDITION_NOT_SATISFIED = "UpdateConditionNotSatisfied";

    /**
     * One or more header values are not supported.
     */
    public static final String UNSUPPORTED_HEADER = "UnsupportedHeader";

    /**
     * The specified HTTP verb is not supported.
     */
    public static final String UNSUPPORTED_HTTP_VERB = "UnsupportedHttpVerb";

    /**
     * One or more query parameters is not supported.
     */
    public static final String UNSUPPORTED_QUERY_PARAMETER = "UnsupportedQueryParameter";

    /**
     * Private Default Constructor.
     */
    private StorageErrorCodeStrings() {
        // No op
    }
}
