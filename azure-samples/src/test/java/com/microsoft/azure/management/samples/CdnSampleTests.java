/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.cdn.samples.ManageCdn;
import org.junit.Assert;
import org.junit.Test;

public class CdnSampleTests extends SamplesTestBase {
    @Test
    public void testManageCdn() {
        Assert.assertTrue(ManageCdn.runSample(azure));
    }
}
