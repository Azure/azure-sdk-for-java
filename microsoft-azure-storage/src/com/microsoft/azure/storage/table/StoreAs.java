/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.table;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to override the name a property is serialized and deserialized with using reflection. Use this
 * annotation to specify the property name to associate with the data stored by a setter method or retrieved by a getter
 * method in a class implementing {@link TableEntity} that uses reflection-based serialization and deserialization. Note
 * that the names "PartitionKey", "RowKey", "Timestamp", and "Etag" are reserved and will be ignored if set with the
 * <code>@StoreAs</code> annotation.
 * <p>
 * Example:
 * <p>
 * <code>@StoreAs(name = "EntityPropertyName")<br>public String getObjectPropertyName() { ... }</code>
 * <p>
 * <code>@StoreAs(name = "EntityPropertyName")<br>public void setObjectPropertyName(String name) { ... }</code>
 * <p>
 * This example shows how the methods that would get and set an entity property named <em>ObjectPropertyName</em> in the
 * default case can be annotated to get and set an entity property named <em>EntityPropertyName</em>. See the
 * documentation for {@link TableServiceEntity} for more information on using reflection-based serialization and
 * deserialization.
 * 
 * @see Ignore
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface StoreAs {
    public String name();
}
