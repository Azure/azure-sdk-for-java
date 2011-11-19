package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents error code strings that are common to all storage services.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class StorageErrorCodeStrings {
    /**
     * Authentication failed.
     */
    public static final String AUTHENTICATION_FAILED = "AuthenticationFailed";

    /**
     * The specified condition was not met.
     */
    public static final String CONDITION_NOT_MET = "ConditionNotMet";

    /**
     * The specified container already exists.
     */
    public static final String CONTAINER_ALREADY_EXISTS = "ContainerAlreadyExists";

    /**
     * The specified queue already exists.
     */
    public static final String QUEUE_ALREADY_EXISTS = "QueueAlreadyExists";

    /**
     * The specified container is being deleted.
     */
    public static final String CONTAINER_BEING_DELETED = "ContainerBeingDeleted";

    /**
     * The specified queue is being deleted.
     */
    public static final String QUEUE_BEING_DELETED = "QueueBeingDeleted";

    /**
     * The specified container is disabled.
     */
    public static final String CONTAINER_DISABLED = "ContainerDisabled";

    /**
     * The specified container was not found.
     */
    public static final String CONTAINER_NOT_FOUND = "ContainerNotFound";

    /**
     * An incorrect blob type was specified.
     */
    public static final String INCORRECT_BLOB_TYPE = "IncorrectBlobType";

    /**
     * An incorrect blob type was specified.
     */
    public static final String INVALID_BLOB_TYPE = "InvalidBlobType";

    /**
     * The metadata key is empty.
     */
    public static final String EMPTY_METADATA_KEY = "EmptyMetadataKey";

    /**
     * An internal error occurred.
     */
    public static final String INTERNAL_ERROR = "InternalError";

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
     * One or more XML node values are invalid.
     */
    public static final String INVALID_XML_NODE_VALUE = "InvalidXmlNodeValue";

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
     * One or more XML nodes are not supported.
     */
    public static final String UNSUPPORTED_XML_NODE = "UnsupportedXmlNode";

    /**
     * The lease is already broken.
     */
    public static final String LEASE_ALREADY_BROKEN = "LeaseAlreadyBroken";

    /**
     * The lease is already present.
     */
    public static final String LEASE_ALREADY_PRESENT = "LeaseAlreadyPresent";

    /**
     * The lease ID is missing.
     */
    public static final String LEASE_ID_MISSING = "LeaseIdMissing";

    /**
     * Private Default Ctor.
     */
    private StorageErrorCodeStrings() {
        // No op
    }
}
