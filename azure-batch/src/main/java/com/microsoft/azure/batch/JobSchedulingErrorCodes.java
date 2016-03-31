package com.microsoft.azure.batch;

// <summary>
// Contains error codes specific to job scheduling errors.
// </summary>
public final class JobSchedulingErrorCodes
{
    // <summary>
    // The Batch service could not create an auto pool to run the job on, because the account
    // has reached its quota of compute nodes.
    // </summary>
    public static final String AutoPoolCreationFailedWithQuotaReached = "AutoPoolCreationFailedWithQuotaReached";

    // <summary>
    // The auto pool specification for the job has one or more application package references which could not be satisfied.
    // This occurs if the application id or version does not exist or is not active, or if the reference did not specify a
    // version and there is no default version configured.
    // </summary>
    public static final String InvalidApplicationPackageReferencesInAutoPool = "InvalidApplicationPackageReferencesInAutoPool";

    // <summary>
    // The auto pool specification for the job has an invalid automatic scaling formula.
    // </summary>
    public static final String InvalidAutoScaleFormulaInAutoPool = "InvalidAutoScaleFormulaInAutoPool";

    // <summary>
    // The auto pool specification for the job has an invalid certificate reference (for example, to a
    // certificate that does not exist).
    // </summary>
    public static final String InvalidCertificatesInAutoPool = "InvalidCertificatesInAutoPool";

    // <summary>
    // The reason for the scheduling error is unknown.
    // </summary>
    public static final String Unknown = "Unknown";
}
