/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.search.samples.ManageSearchService;
import org.junit.Assert;
import org.junit.Test;

public class SearchServiceTests extends SamplesTestBase {
  @Test
  public void testManageSearchService() {
    Assert.assertTrue(ManageSearchService.runSample(azure));
  }
}
