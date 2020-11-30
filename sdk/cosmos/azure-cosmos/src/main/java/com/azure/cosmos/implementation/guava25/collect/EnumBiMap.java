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

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * A {@code BiMap} backed by two {@code EnumMap} instances. Null keys and values are not permitted.
 * An {@code EnumBiMap} and its inverse are both serializable.
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/NewCollectionTypesExplained#bimap"> {@code BiMap}</a>.
 *
 * @author Mike Bostock
 * @since 2.0
 */
public final class EnumBiMap<K extends Enum<K>, V extends Enum<V>> extends AbstractBiMap<K, V> {
  private transient Class<K> keyType;
  private transient Class<V> valueType;

  /**
   * Returns a new, empty {@code EnumBiMap} using the specified key and value types.
   *
   * @param keyType the key type
   * @param valueType the value type
   */
  public static <K extends Enum<K>, V extends Enum<V>> EnumBiMap<K, V> create(
      Class<K> keyType, Class<V> valueType) {
    return new EnumBiMap<>(keyType, valueType);
  }

  /**
   * Returns a new bimap with the same mappings as the specified map. If the specified map is an
   * {@code EnumBiMap}, the new bimap has the same types as the provided map. Otherwise, the
   * specified map must contain at least one mapping, in order to determine the key and value types.
   *
   * @param map the map whose mappings are to be placed in this map
   * @throws IllegalArgumentException if map is not an {@code EnumBiMap} instance and contains no
   *     mappings
   */
  public static <K extends Enum<K>, V extends Enum<V>> EnumBiMap<K, V> create(Map<K, V> map) {
    EnumBiMap<K, V> bimap = create(inferKeyType(map), inferValueType(map));
    bimap.putAll(map);
    return bimap;
  }

  private EnumBiMap(Class<K> keyType, Class<V> valueType) {
    super(
        WellBehavedMap.wrap(new EnumMap<K, V>(keyType)),
        WellBehavedMap.wrap(new EnumMap<V, K>(valueType)));
    this.keyType = keyType;
    this.valueType = valueType;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <K extends Enum<K>> Class<K> inferKeyType(Map<K, ?> map) {
    if (map instanceof EnumBiMap) {
      return ((EnumBiMap<K, ?>) map).keyType();
    }
    if (map instanceof EnumHashBiMap) {
      return ((EnumHashBiMap<K, ?>) map).keyType();
    }
    checkArgument(!map.isEmpty());
    return map.keySet().iterator().next().getDeclaringClass();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <V extends Enum<V>> Class<V> inferValueType(Map<?, V> map) {
    if (map instanceof EnumBiMap) {
      return ((EnumBiMap<?, V>) map).valueType;
    }
    checkArgument(!map.isEmpty());
    return map.values().iterator().next().getDeclaringClass();
  }

  /** Returns the associated key type. */
  public Class<K> keyType() {
    return keyType;
  }

  /** Returns the associated value type. */
  public Class<V> valueType() {
    return valueType;
  }

  @Override
  K checkKey(K key) {
    return checkNotNull(key);
  }

  @Override
  V checkValue(V value) {
    return checkNotNull(value);
  }

  /**
   * @serialData the key class, value class, number of entries, first key, first value, second key,
   *     second value, and so on.
   */
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(keyType);
    stream.writeObject(valueType);
    Serialization.writeMap(this, stream);
  }

  @SuppressWarnings({"unchecked", "rawtypes"}) // reading fields populated by writeObject
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    keyType = (Class<K>) stream.readObject();
    valueType = (Class<V>) stream.readObject();
    setDelegates(
        WellBehavedMap.wrap(new EnumMap<K, V>(keyType)),
        WellBehavedMap.wrap(new EnumMap<V, K>(valueType)));
    Serialization.populateMap(this, stream);
  }

  private static final long serialVersionUID = 0;
}
