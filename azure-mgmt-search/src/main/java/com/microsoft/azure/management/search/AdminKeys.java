/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.search.implementation.AdminKeyResultInner;

/**
 * Response containing the primary and secondary admin API keys for a given Azure Search service.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_1_0) // TODO: change to Beta.SinceVersion.V1_2_0
public interface AdminKeys {
  /**
   * Get the primaryKey value.
   *
   * @return the primaryKey value
   */
  String primaryKey();

  /**
   * Get the secondaryKey value.
   *
   * @return the secondaryKey value
   */
  String secondaryKey();
}
