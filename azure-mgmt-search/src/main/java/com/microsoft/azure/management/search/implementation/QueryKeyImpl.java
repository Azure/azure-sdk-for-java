package com.microsoft.azure.management.search.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.search.QueryKey;

/**
 * Describes an API key for a given Azure Search service that has permissions
 * for query operations only.
 */
class QueryKeyImpl extends WrapperImpl<QueryKeyInner> implements QueryKey {
  /**
   * Create QueryKey
   */
  protected QueryKeyImpl(QueryKeyInner innerObject) {
    super(innerObject);
  }

  /**
   * Get the name value.
   *
   * @return the name value
   */
  public String name() {
    return this.inner().name();
  }

  /**
   * Get the key value.
   *
   * @return the key value
   */
  public String key() {
    return this.inner().key();
  }

}
