/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.search.implementation.CheckNameAvailabilityOutputInner;

/**
 * The result of checking for Search service name availability.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_2_0)
public interface CheckNameAvailabilityResult extends HasInner<CheckNameAvailabilityOutputInner> {
  /**
   * @return true if the specified name is valid and available for use, otherwise false
   */
  boolean isAvailable();
  /**
   * @return the reason why the user-provided name for the search service could not be used, if any. The
   * Reason element is only returned if NameAvailable is false.
   */
  UnavailableNameReason unavailabilityReason();
  /**
   * @return an error message explaining the Reason value in more detail
   */
  String unavailabilityMessage();
}
