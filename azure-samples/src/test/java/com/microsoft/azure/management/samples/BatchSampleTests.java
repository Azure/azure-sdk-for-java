/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.batch.samples.ManageBatchAccount;
import org.junit.Assert;
import org.junit.Test;

public class BatchSampleTests extends SamplesTestBase {
    @Test
    public void testManageBatchAccount() {
        Assert.assertTrue(ManageBatchAccount.runSample(azure));
    }
}
