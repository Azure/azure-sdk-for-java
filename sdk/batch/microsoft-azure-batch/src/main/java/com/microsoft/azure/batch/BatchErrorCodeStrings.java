// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

/**
 * Error code strings returned from the Batch service.
 */
public final class BatchErrorCodeStrings {
    // Batch Service

    /**
     * The specified account is disabled.
     */
    public static final String AccountIsDisabled = "AccountIsDisabled";

    /**
     * The account has reached its quota of active jobs and job schedules.
     */
    public static final String ActiveJobAndScheduleQuotaReached = "ActiveJobAndScheduleQuotaReached";

    /**
     * The specified application does not exist.
     */
    public static final String ApplicationNotFound = "ApplicationNotFound";

    /**
     * An automatic scaling formula has a syntax error.
     */
    public static final String AutoScalingFormulaSyntaxError = "AutoScalingFormulaSyntaxError";

    /**
     * An automatic scaling formula is too long. The maximum length is 8192 characters.
     */
    public static final String AutoScaleFormulaTooLong = "AutoScaleFormulaTooLong";

    /**
     * Enable AutoScale requests for the same pool must be separated by at least 30 seconds.
     */
    public static final String AutoScaleTooManyRequestsToEnable = "TooManyEnableAutoScaleRequests";

    /**
     * A certificate operation was attempted which is not permitted when the certificate is in the process of being deleted.
     */
    public static final String CertificateBeingDeleted = "CertificateBeingDeleted";

    /**
     * The certificate you are attempting to add already exists.
     */
    public static final String CertificateExists = "CertificateExists";

    /**
     * The certificate on which an operation was attempted is not present in the Batch account.
     */
    public static final String CertificateNotFound = "CertificateNotFound";

    /**
     * A certificate operation was attempted which is not permitted when the certificate is active.
     */
    public static final String CertificateStateActive = "CertificateStateActive";

    /**
     * A certificate could not be deleted because it is still in use.
     */
    public static final String CertificateStateDeleteFailed = "CertificateDeleteFailed";

    /**
     * A node file requested from a task or compute node was not found.
     */
    public static final String FileNotFound = "FileNotFound";

    /**
     * One or more application package references could not be satisfied. This occurs if the application
     * ID or version does not exist or is not active, or if the reference did not specify a version and
     * there is no default version configured.
     */
    public static final String InvalidApplicationPackageReferences = "InvalidApplicationPackageReferences";

    /**
     * A pool specification specifies one or more invalid certificates (for example, certificates that are
     * not present in the Batch account).
     */
    public static final String InvalidCertificateReferences = "InvalidCertificateReferences";

    /**
     * A value in a job or task constraint is out of range.
     */
    public static final String InvalidConstraintValue = "InvalidConstraintValue";

    /**
     * There is a conflict between the REST API being used and the account.
     */
    public static final String InvalidRestAPIForAccountSetting = "InvalidRestAPIForAccountSetting";

    /**
     * A job operation was attempted which is not permitted when the job is in the process of being deleted.
     */
    public static final String JobBeingDeleted = "JobBeingDeleted";

    /**
     * A job operation was attempted which is not permitted when the job is in the process of being terminated.
     */
    public static final String JobBeingTerminated = "JobBeingTerminated";

    /**
     * A job operation was attempted which is not permitted when the job has been completed.
     */
    public static final String JobCompleted = "JobCompleted";

    /**
     * A job operation was attempted which is not permitted when the job is not active.
     */
    public static final String JobNotActive = "JobNotActive";

    /**
     * The specified job exists.
     */
    public static final String JobExists = "JobExists";

    /**
     * A Job Preparation task was not run on a compute node.
     */
    public static final String JobPreparationTaskNotRunOnNode = "JobPreparationTaskNotRunOnNode";

    /**
     * The specified job does not have a Job Preparation task.
     */
    public static final String JobPreparationTaskNotSpecified = "JobPreparationTaskNotSpecified";

    /**
     * A Job Release task was not run on a compute node.
     */
    public static final String JobReleaseTaskNotRunOnNode = "JobReleaseTaskNotRunOnNode";

    /**
     * The specified job does not have a Job Release task.
     */
    public static final String JobReleaseTaskNotSpecified = "JobReleaseTaskNotSpecified";

    /**
     * The job on which an operation was attempted is not present in the Batch account.
     */
    public static final String JobNotFound = "JobNotFound";

    /**
     * An I/O error occurred while trying to access a resource within the Batch account.
     */
    public static final String IOError = "IOError";

    /**
     * The specified operation is not valid for the current state of the resource.
     */
    public static final String OperationInvalidForCurrentState = "OperationInvalidForCurrentState";

    /**
     * The specified Azure Guest OS version is disabled.
     */
    public static final String OSVersionDisabled = "OSVersionDisabled";

    /**
     * The specified Azure Guest OS version is expired.
     */
    public static final String OSVersionExpired = "OSVersionExpired";

    /**
     * The specified Azure Guest OS version was not found.
     */
    public static final String OSVersionNotFound = "OSVersionNotFound";

    /**
     * A job priority was specified which was outside the permitted range of -1000 to 1000.
     */
    public static final String OutOfRangePriority = "OutOfRangePriority";

    /**
     * A file path was not found on a compute node.
     */
    public static final String PathNotFound = "PathNotFound";

    /**
     * A pool operation was attempted which is not permitted when the pool is in the process of being deleted.
     */
    public static final String PoolBeingDeleted = "PoolBeingDeleted";

    /**
     * A pool operation was attempted which is not permitted when the pool is in the process of being resized.
     */
    public static final String PoolBeingResized = "PoolBeingResized";

    /**
     * A pool operation was attempted which is not permitted when the pool is in the process of being created.
     */
    public static final String PoolBeingCreated = "PoolBeingCreated";

    /**
     * The pool you are attempting to add already exists.
     */
    public static final String PoolExists = "PoolExists";

    /**
     * The specified pool is not eligible for an operating system version upgrade.
     */
    public static final String PoolNotEligibleForOSVersionUpgrade = "PoolNotEligibleForOSVersionUpgrade";

    /**
     * The pool on which an operation was attempted is not present in the Batch account.
     */
    public static final String PoolNotFound = "PoolNotFound";

    /**
     * The account has reached its quota of pools.
     */
    public static final String PoolQuotaReached = "PoolQuotaReached";

    /**
     * The pool is already on the operating system version to which it was asked to upgrade.
     */
    public static final String PoolVersionEqualsUpgradeVersion = "PoolVersionEqualsUpgradeVersion";

    /**
     * A requested storage account was not found.
     */
    public static final String StorageAccountNotFound = "StorageAccountNotFound";

    /**
     * A task operation was attempted which is not permitted when the task has been completed.
     */
    public static final String TaskCompleted = "TaskCompleted";

    /**
     * A task was specified as depending on other tasks, but the job did not specify that it would use task dependencies.
     */
    public static final String TaskDependenciesNotSpecifiedOnJob = "TaskDependenciesNotSpecifiedOnJob";

    /**
     * A task was specified as depending on other tasks, but the list of dependencies was too long to be stored.
     */
    public static final String TaskDependencyListTooLong = "TaskDependencyListTooLong";

    /**
     * A task was specified as depending on other tasks, but the list of task ID ranges was too long to be stored.
     */
    public static final String TaskDependencyRangesTooLong = "TaskDependencyRangesTooLong";

    /**
     * The node files for a task are not available, for example because the retention period has expired.
     */
    public static final String TaskFilesUnavailable = "TaskFilesUnavailable";

    /**
     * The files of the specified task are cleaned up.
     */
    public static final String TaskFilesCleanedup = "TaskFilesCleanedup";

    /**
     * The task you are attempting to add already exists.
     */
    public static final String TaskExists = "TaskExists";

    /**
     * The task ID is the same as that of the Job Preparation task.
     */
    public static final String TaskIdSameAsJobPreparationTask = "TaskIdSameAsJobPreparationTask";

    /**
     * The task ID is the same as that of the Job Release task.
     */
    public static final String TaskIdSameAsJobReleaseTask = "TaskIdSameAsJobReleaseTask";

    /**
     * The task on which an operation was attempted is not present in the job.
     */
    public static final String TaskNotFound = "TaskNotFound";

    /**
     * A compute node operation was attempted which is not permitted when the node is in the process of being created.
     */
    public static final String NodeBeingCreated = "NodeBeingCreated";

    /**
     * A compute node operation was attempted which is not permitted when the node is in the process of being started.
     */
    public static final String NodeBeingStarted = "NodeBeingStarted";

    /**
     * A compute node operation was attempted which is not permitted when the node is in the process of being rebooted.
     */
    public static final String NodeBeingRebooted = "NodeBeingRebooted";

    /**
     * A compute node operation was attempted which is not permitted when the node is in the process of being reimaged.
     */
    public static final String NodeBeingReimaged = "NodeBeingReimaged";

    /**
     * The node counts do not match.
     */
    public static final String NodeCountsMismatch = "NodeCountsMismatch";

    /**
     * The compute node on which an operation was attempted is not present in the given pool.
     */
    public static final String NodeNotFound = "NodeNotFound";

    /**
     * A compute node operation was attempted which is not permitted when the node is unusable.
     */
    public static final String NodeStateUnusable = "NodeStateUnusable";

    /**
     * The compute node user account you are attempting to add already exists.
     */
    public static final String NodeUserExists = "NodeUserExists";

    /**
     * The compute node user account on which an operation was attempted does not exist.
     */
    public static final String NodeUserNotFound = "NodeUserNotFound";

    /**
     * The specified compute node is already in the target scheduling state.
     */
    public static final String NodeAlreadyInTargetSchedulingState = "NodeAlreadyInTargetSchedulingState";

    /**
     * The pool is already upgrading to a different operating system version.
     */
    public static final String UpgradePoolVersionConflict = "UpgradePoolVersionConflict";

    /**
     * A requested job or task constraint is not supported.
     */
    public static final String UnsupportedConstraint = "UnsupportedConstraint";

    /**
     * The specified version of the Batch REST API is not supported.
     */
    public static final String UnsupportedRequestVersion = "UnsupportedRequestVersion";

    /**
     * A job schedule operation was attempted which is not permitted when the schedule is in the process of being deleted.
     */
    public static final String JobScheduleBeingDeleted = "JobScheduleBeingDeleted";

    /**
     * A job schedule operation was attempted which is not permitted when the schedule is in the process of being terminated.
     */
    public static final String JobScheduleBeingTerminated = "JobScheduleBeingTerminated";

    /**
     * A job schedule operation was attempted which is not permitted when the schedule has completed.
     */
    public static final String JobScheduleCompleted = "JobScheduleCompleted";

    /**
     * A job schedule operation was attempted which is not permitted when the schedule is disabled.
     */
    public static final String JobScheduleDisabled = "JobScheduleDisabled";

    /**
     * The job schedule you are attempting to add already exists in the Batch account.
     */
    public static final String JobScheduleExists = "JobScheduleExists";

    /**
     * The job schedule on which an operation was attempted does not exist.
     */
    public static final String JobScheduleNotFound = "JobScheduleNotFound";

    /**
     * The specified job is disabled.
     */
    public static final String JobDisabled = "JobDisabled";

    /**
     * A job with the specified job schedule ID exists. Job and job schedule cannot have the same ID.
     */
    public static final String JobWithSameIdExists = "JobWithSameIdExists";

    /**
     * A job schedule with the specified job ID exists. Job and job schedule cannot have the same ID.
     */
    public static final String JobScheduleWithSameIdExists = "JobScheduleWithSameIdExists";

    // General

    /**
     * The Batch service failed to authenticate the request.
     */
    public static final String AuthenticationFailed = "AuthenticationFailed";

    /**
     * A read operation included a HTTP conditional header, and the condition was not met.
     */
    public static final String ConditionNotMet = "ConditionNotMet";

    /**
     * An add or update request specified a metadata item whose key is an empty string.
     */
    public static final String EmptyMetadataKey = "EmptyMetadataKey";

    /**
     * The host information was missing from the HTTP request.
     */
    public static final String HostInformationNotPresent = "HostInformationNotPresent";

    /**
     * The account being accessed does not have sufficient permissions to execute this operation.
     */
    public static final String InsufficientAccountPermissions = "InsufficientAccountPermissions";

    /**
     * An internal error occurred in the Batch service.
     */
    public static final String InternalError = "InternalError";

    /**
     * The authentication credentials were not provided in the correct format.
     */
    public static final String InvalidAuthenticationInfo = "InvalidAuthenticationInfo";

    /**
     * The specified auto-scale settings are not valid.
     */
    public static final String InvalidAutoScalingSettings = "InvalidAutoScalingSettings";

    /**
     * The value of one of the HTTP headers was in an incorrect format.
     */
    public static final String InvalidHeaderValue = "InvalidHeaderValue";

    /**
     * The Batch service did not recognize the HTTP verb used for the request.
     */
    public static final String InvalidHttpVerb = "InvalidHttpVerb";

    /**
     * One of the request inputs is not valid.
     */
    public static final String InvalidInput = "InvalidInput";

    /**
     * An add or update request specified a metadata item which contains characters that are not permitted.
     */
    public static final String InvalidMetadata = "InvalidMetadata";

    /**
     * The value of a property in the HTTP request body is invalid.
     */
    public static final String InvalidPropertyValue = "InvalidPropertyValue";

    /**
     * The HTTP request body is not syntactically valid.
     */
    public static final String InvalidRequestBody = "InvalidRequestBody";

    /**
     * The HTTP request URI contained invalid value for one of the query parameters.
     */
    public static final String InvalidQueryParameterValue = "InvalidQueryParameterValue";

    /**
     * The specified byte range is invalid for the given resource.
     */
    public static final String InvalidRange = "InvalidRange";

    /**
     * The HTTP request URI was invalid.
     */
    public static final String InvalidUri = "InvalidUri";

    /**
     * The size of the metadata exceeds the maximum permitted.
     */
    public static final String MetadataTooLarge = "MetadataTooLarge";

    /**
     * The HTTP content-length header was not specified.
     */
    public static final String MissingContentLengthHeader = "MissingContentLengthHeader";

    /**
     * A required HTTP header was not specified.
     */
    public static final String MissingRequiredHeader = "MissingRequiredHeader";

    /**
     * A required property was not specified in the HTTP request body.
     */
    public static final String MissingRequiredProperty = "MissingRequiredProperty";

    /**
     * A required query parameter was not specified in the URL.
     */
    public static final String MissingRequiredQueryParameter = "MissingRequiredQueryParameter";

    /**
     * Multiple condition headers are not supported.
     */
    public static final String MultipleConditionHeadersNotSupported = "MultipleConditionHeadersNotSupported";

    /**
     * The operation is not implemented.
     */
    public static final String NotImplemented = "NotImplemented";

    /**
     * One of the request inputs is out of range.
     */
    public static final String OutOfRangeInput = "OutOfRangeInput";

    /**
     * A query parameter in the request URL is out of range.
     */
    public static final String OutOfRangeQueryParameterValue = "OutOfRangeQueryParameterValue";

    /**
     * The operation could not be completed within the permitted time.
     */
    public static final String OperationTimedOut = "OperationTimedOut";

    /**
     * The size of the HTTP request body exceeds the maximum permitted.
     */
    public static final String RequestBodyTooLarge = "RequestBodyTooLarge";

    /**
     * The Batch service could not parse the request URL.
     */
    public static final String RequestUrlFailedToParse = "RequestUrlFailedToParse";

    /**
     * The specified resource does not exist.
     */
    public static final String ResourceNotFound = "ResourceNotFound";

    /**
     * The specified resource already exists.
     */
    public static final String ResourceAlreadyExists = "ResourceAlreadyExists";

    /**
     * The resource does not match the expected type.
     */
    public static final String ResourceTypeMismatch = "ResourceTypeMismatch";

    /**
     * The Batch service is currently unable to receive requests.
     */
    public static final String ServerBusy = "ServerBusy";

    /**
     * One of the HTTP headers specified in the request is not supported.
     */
    public static final String UnsupportedHeader = "UnsupportedHeader";

    /**
     * The resource does not support the specified HTTP verb.
     */
    public static final String UnsupportedHttpVerb = "UnsupportedHttpVerb";

    /**
     * The Batch service does not support the specified version of the HTTP protocol.
     */
    public static final String UnsupportedHttpVersion = "UnsupportedHttpVersion";

    /**
     * One of the properties specified in the HTTP request body is not supported.
     */
    public static final String UnsupportedProperty = "UnsupportedProperty";

    /**
     * One of the query parameters specified in the URL is not supported.
     */
    public static final String UnsupportedQueryParameter = "UnsupportedQueryParameter";
}

