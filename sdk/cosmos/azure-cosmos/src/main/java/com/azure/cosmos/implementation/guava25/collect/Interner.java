/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.guava25.collect;



/**
 * Provides equivalent behavior to {@link String#intern} for other immutable types. Common
 * implementations are available from the {@link Interners} class.
 *
 * @author Kevin Bourrillion
 * @since 3.0
 */

public interface Interner<E> {
  /**
   * Chooses and returns the representative instance for any of a collection of instances that are
   * equal to each other. If two {@linkplain Object#equals equal} inputs are given to this method,
   * both calls will return the same instance. That is, {@code intern(a).equals(a)} always holds,
   * and {@code intern(a) == intern(b)} if and only if {@code a.equals(b)}. Note that {@code
   * intern(a)} is permitted to return one instance now and a different instance later if the
   * original interned instance was garbage-collected.
   *
   * <p><b>Warning:</b> do not use with mutable objects.
   *
   * @throws NullPointerException if {@code sample} is null
   */
  E intern(E sample);
}
