/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.search.CheckNameAvailabilityResult;
import com.microsoft.azure.management.search.UnavailableNameReason;

/**
 * Implementation for CheckNameAvailabilityResult.
 */
@LangDefinition
class CheckNameAvailabilityResultImpl
    extends WrapperImpl<CheckNameAvailabilityOutputInner>
    implements CheckNameAvailabilityResult {
  /**
   * Creates an instance of the check name availability result object.
   *
   * @param inner the inner object
   */
  CheckNameAvailabilityResultImpl(CheckNameAvailabilityOutputInner inner) {
    super(inner);
  }

  @Override
  public boolean isAvailable() {
    return inner().isNameAvailable();
  }

  @Override
  public UnavailableNameReason unavailabilityReason() {
    return inner().reason();
  }

  @Override
  public String unavailabilityMessage() {
    return inner().message();
  }
}
