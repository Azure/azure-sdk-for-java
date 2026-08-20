// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation;

import com.azure.resourcemanager.platformvalidation.models.CertificationPackageDiskImage;
import com.azure.resourcemanager.platformvalidation.models.CertificationPackageReference;
import com.azure.resourcemanager.platformvalidation.models.CertificationPackageStorageProfile;
import com.azure.resourcemanager.platformvalidation.models.ExecutionPlanConfiguration;
import com.azure.resourcemanager.platformvalidation.models.ValidationExecutionPlanProperties;
import com.azure.resourcemanager.platformvalidation.models.ValidationStep;
import java.util.Collections;

/**
 * Samples for authoring an execution plan configuration.
 */
public final class ExecutionPlanConfigurationSamples {
    private ExecutionPlanConfigurationSamples() {
    }

    /**
     * Creates properties containing an inline execution plan configuration.
     *
     * @return the execution plan properties.
     */
    public static ValidationExecutionPlanProperties createProperties() {
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration()
            .withName("certification-plan")
            .withCertificationPackageReference(new CertificationPackageReference()
                .withOsType("Linux")
                .withVmGenerationType("V2")
                .withArchitectureType("x64")
                .withRecommendedVMSizes(Collections.singletonList("Standard_D2s_v5"))
                .withStorageProfile(new CertificationPackageStorageProfile()
                    .withOsDiskImage(new CertificationPackageDiskImage()
                        .withSourceVhdUri("https://storage.example/os.vhd"))))
            .addStep(ValidationStep.test("run validation", "complete-test-reference"));

        return new ValidationExecutionPlanProperties().withPlanConfigurationJson(configuration.toJsonString());
    }
}
