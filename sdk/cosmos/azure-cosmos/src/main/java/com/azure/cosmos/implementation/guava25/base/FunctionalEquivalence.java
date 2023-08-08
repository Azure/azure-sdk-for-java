/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.guava25.base;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;


import java.io.Serializable;


/**
 * Equivalence applied on functional result.
 *
 * @author Bob Lee
 * @since 10.0
 */

final class FunctionalEquivalence<F, T> extends Equivalence<F> implements Serializable {

  private static final long serialVersionUID = 0;

  private final Function<F, ? extends T> function;
  private final Equivalence<T> resultEquivalence;

  FunctionalEquivalence(Function<F, ? extends T> function, Equivalence<T> resultEquivalence) {
    this.function = checkNotNull(function);
    this.resultEquivalence = checkNotNull(resultEquivalence);
  }

  @Override
  protected boolean doEquivalent(F a, F b) {
    return resultEquivalence.equivalent(function.apply(a), function.apply(b));
  }

  @Override
  protected int doHash(F a) {
    return resultEquivalence.hash(function.apply(a));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof FunctionalEquivalence) {
      FunctionalEquivalence<?, ?> that = (FunctionalEquivalence<?, ?>) obj;
      return function.equals(that.function) && resultEquivalence.equals(that.resultEquivalence);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(function, resultEquivalence);
  }

  @Override
  public String toString() {
    return resultEquivalence + ".onResultOf(" + function + ")";
  }
}
