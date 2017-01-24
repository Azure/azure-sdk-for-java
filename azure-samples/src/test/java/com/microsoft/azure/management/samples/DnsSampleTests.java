/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.dns.samples.ManageDns;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class DnsSampleTests extends SamplesTestBase {

    @Test
    @Ignore("The domain name 'the custom domain that you own (e.g. contoso.com)' is invalid.")
    public void testManageDns() {
        Assert.assertTrue(ManageDns.runSample(azure));
    }

}
