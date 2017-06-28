/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.search.implementation.QueryKeyInner;

/**
 * Describes an API key for a given Azure Search service that has permissions
 * for query operations only.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_1_0) // TODO: change to Beta.SinceVersion.V1_2_0
public interface QueryKey {
  /**
   * Get the name value.
   *
   * @return the name value
   */
  String name();

  /**
   * Get the key value.
   *
   * @return the key value
   */
  String key();
}
