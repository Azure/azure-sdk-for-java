/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.search.QueryKey;

/**
 * Describes an API key for a given Azure Search service that has permissions for query operations only.
 */
@LangDefinition
class QueryKeyImpl extends WrapperImpl<QueryKeyInner> implements QueryKey {

  protected QueryKeyImpl(QueryKeyInner innerObject) {
    super(innerObject);
  }

  @Override
  public String name() {
    return this.inner().name();
  }

  @Override
  public String key() {
    return this.inner().key();
  }

}
