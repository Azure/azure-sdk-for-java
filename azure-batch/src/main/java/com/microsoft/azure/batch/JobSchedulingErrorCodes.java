/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

/**
 * Contains error codes specific to job scheduling errors.
 */
public final class JobSchedulingErrorCodes
{
    /**
     * The Batch service could not create an auto pool to run the job on, because the account
     * has reached its quota of compute nodes.
     */
    public static final String AutoPoolCreationFailedWithQuotaReached = "AutoPoolCreationFailedWithQuotaReached";

    /**
     * The auto pool specification for the job has one or more application package references which could not be satisfied.
     * This occurs if the application ID or version does not exist or is not active, or if the reference did not specify a
     * version and there is no default version configured.
     */
    public static final String InvalidApplicationPackageReferencesInAutoPool = "InvalidApplicationPackageReferencesInAutoPool";

    /**
     * The auto pool specification for the job has an invalid automatic scaling formula.
     */
    public static final String InvalidAutoScaleFormulaInAutoPool = "InvalidAutoScaleFormulaInAutoPool";

    /**
     * The auto pool specification for the job has an invalid certificate reference (for example, to a
     * certificate that does not exist).
     */
    public static final String InvalidCertificatesInAutoPool = "InvalidCertificatesInAutoPool";

    /**
     * The reason for the scheduling error is unknown.
     */
    public static final String Unknown = "Unknown";
}
