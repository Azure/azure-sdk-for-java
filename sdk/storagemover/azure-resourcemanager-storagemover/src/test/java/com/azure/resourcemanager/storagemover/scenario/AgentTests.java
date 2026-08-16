// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Mirrors {@code AgentTests.cs} from the .NET source-of-truth test suite.
 *
 * <p>The single {@code GetExistTest} method is skipped in every language port
 * because Storage Mover agents cannot be created via the RP — they self-register
 * from a real VM. See {@code storage-mover-scenario-tests-cross-language} task
 * note for the rationale shared across .NET, Python, JS, Go, and Java.
 */
public class AgentTests extends StorageMoverManagementTestBase {

    @Test
    @Disabled("Agents cannot be created by the RP; this test requires a registered agent VM.")
    public void getExist() {
        // Body intentionally empty — see @Disabled reason. The .NET reference
        // exercises agent get / list / patch (uploadLimitWeeklyRecurrences) on
        // a pre-existing agent named "testagent1", which is impossible to set
        // up hermetically.
    }
}
