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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Reserved for internal use. A class used internally during the reflection process to determine which properties should
 * be serialized.
 */
final class PropertyPair {

    /**
     * Reserved for internal use. A static factory method to generate a map of property names to {@link PropertyPair}
     * instances for the specified class type. Checks if the cache is enabled and if so tries to get the property pairs
     * from it. Otherwise, uses reflection to find pairs of getter and setter methods that are annotated with
     * {@link StoreAs} with a common property name, or of the form <code>get<em>PropertyName</em></code> and
     * <code>set<em>PropertyName</em></code>, with a common type for the getter return value and the setter parameter,
     * and stores the methods and the property name for each pair found in a map for use in serializing and
     * deserializing entity data. If the cache is enabled and the mapping was not present, adds it to the cache.
     * 
     * @param clazzType
     *            The class type to check for matching getter and setter methods with a common return and parameter
     *            type, respectively.
     */
    protected static HashMap<String, PropertyPair> generatePropertyPairs(final Class<?> clazzType) {
        if (!TableServiceEntity.isReflectedEntityCacheDisabled()) {
            HashMap<String, PropertyPair> props = new HashMap<String, PropertyPair>();
            props = TableServiceEntity.getReflectedEntityCache().get(clazzType);

            if (props == null) {
                props = PropertyPair.generatePropertyPairsHelper(clazzType);
                TableServiceEntity.getReflectedEntityCache().put(clazzType, props);
            }

            return props;
        }
        else {
            return PropertyPair.generatePropertyPairsHelper(clazzType);
        }
    }

    /**
     * Reserved for internal use. A static factory method to generate a map of property names to {@link PropertyPair}
     * instances for the specified class type. Uses reflection to find pairs of getter and setter methods that are
     * annotated with {@link StoreAs} with a common property name, or of the form <code>get<em>PropertyName</em></code>
     * and <code>set<em>PropertyName</em></code>, with a common type for the getter return value and the
     * setter parameter, and stores the methods and the property name for each pair found in a map for use in
     * serializing and deserializing entity data.
     * 
     * @param clazzType
     *            The class type to check for matching getter and setter methods with a common return and parameter
     *            type, respectively.
     */
    private static HashMap<String, PropertyPair> generatePropertyPairsHelper(final Class<?> clazzType) {
        final Method[] methods = clazzType.getMethods();
        final HashMap<String, PropertyPair> propMap = new HashMap<String, PropertyPair>();

        String propName = null;
        PropertyPair currProperty = null;

        for (final Method m : methods) {
            if (m.getName().length() < 4 || (!m.getName().startsWith("get") && !m.getName().startsWith("set"))) {
                continue;
            }

            propName = m.getName().substring(3);

            // Skip interface methods, these will be called explicitly
            if (propName.equals(TableConstants.PARTITION_KEY) || propName.equals(TableConstants.ROW_KEY)
                    || propName.equals(TableConstants.TIMESTAMP) || propName.equals("Etag")
                    || propName.equals("LastModified")) {
                continue;
            }

            if (propMap.containsKey(propName)) {
                currProperty = propMap.get(propName);
            }
            else {
                currProperty = new PropertyPair();
                currProperty.name = propName;
                propMap.put(propName, currProperty);
            }

            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                currProperty.type = m.getReturnType();
                currProperty.getter = m;
            }
            else if (m.getName().startsWith("set") && m.getParameterTypes().length == 1
                    && void.class.equals(m.getReturnType())) {
                currProperty.setter = m;
            }

            // Check for StoreAs Annotation
            final StoreAs storeAsInstance = m.getAnnotation(StoreAs.class);
            if (storeAsInstance != null) {
                if (Utility.isNullOrEmpty(storeAsInstance.name())) {
                    throw new IllegalArgumentException(String.format(SR.STOREAS_USED_ON_EMPTY_PROPERTY,
                            currProperty.name));
                }

                if (currProperty.effectiveName != null && !currProperty.effectiveName.equals(currProperty.name)
                        && !currProperty.effectiveName.equals(storeAsInstance.name())) {
                    throw new IllegalArgumentException(String.format(SR.STOREAS_DIFFERENT_FOR_GETTER_AND_SETTER,
                            currProperty.name));
                }

                if (!currProperty.name.equals(storeAsInstance.name())) {
                    currProperty.effectiveName = storeAsInstance.name();
                }
            }
        }

        // Return only processable pairs
        final ArrayList<String> keysToRemove = new ArrayList<String>();
        final ArrayList<String> keysToAlter = new ArrayList<String>();

        for (final Entry<String, PropertyPair> e : propMap.entrySet()) {
            if (!e.getValue().shouldProcess()) {
                keysToRemove.add(e.getKey());
                continue;
            }

            if (!Utility.isNullOrEmpty(e.getValue().effectiveName)) {
                keysToAlter.add(e.getKey());
            }
            else {
                e.getValue().effectiveName = e.getValue().name;
            }
        }

        // remove all entries for keys that should not process
        for (final String key : keysToRemove) {
            propMap.remove(key);
        }

        // Any store as properties should be re-stored into the hash under the efective name.
        for (final String key : keysToAlter) {
            final PropertyPair p = propMap.get(key);
            propMap.remove(key);
            propMap.put(p.effectiveName, p);
        }

        return propMap;
    }

    private Method getter = null;
    private Method setter = null;
    private String name = null;
    Class<?> type = null;
    String effectiveName = null;

    /**
     * Reserved for internal use. Invokes the setter method on the specified instance parameter with the value of the
     * {@link EntityProperty} deserialized as the appropriate type.
     * 
     * @param prop
     *            The {@link EntityProperty} containing the value to pass to the setter on the instance.
     * @param instance
     *            An instance of a class supporting this property with getter and setter methods of the
     *            appropriate name and parameter or return type.
     * 
     * @throws IllegalArgumentException
     *             if the specified instance parameter is not an instance of the class
     *             or interface declaring the setter method (or of a subclass or implementor thereof).
     * @throws IllegalAccessException
     *             if the setter method is inaccessible.
     * @throws InvocationTargetException
     *             if the setter method throws an exception.
     */
    protected void consumeEntityProperty(final EntityProperty prop, final Object instance)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> paramType = this.setter.getParameterTypes()[0];
        if (prop.getIsNull()) {
            if (!paramType.isPrimitive()) {
                this.setter.invoke(instance, (Object) null);
            }
        }
        else {
            if (prop.getEdmType() == EdmType.STRING) {
                if (paramType.equals(String.class)) {
                    this.setter.invoke(instance, prop.getValueAsString());
                }
            }
            else if (prop.getEdmType() == EdmType.BINARY) {
                if (paramType.equals(Byte[].class)) {
                    this.setter.invoke(instance, (Object) prop.getValueAsByteObjectArray());
                }
                else if (paramType.equals(byte[].class)) {
                    this.setter.invoke(instance, prop.getValueAsByteArray());
                }
            }
            else if (prop.getEdmType() == EdmType.BOOLEAN) {
                if (paramType.equals(Boolean.class)) {
                    this.setter.invoke(instance, prop.getValueAsBooleanObject());
                }
                else if (paramType.equals(boolean.class)) {
                    this.setter.invoke(instance, prop.getValueAsBoolean());
                }
            }
            else if (prop.getEdmType() == EdmType.DOUBLE) {
                if (paramType.equals(Double.class)) {
                    this.setter.invoke(instance, prop.getValueAsDoubleObject());
                }
                else if (paramType.equals(double.class)) {
                    this.setter.invoke(instance, prop.getValueAsDouble());
                }
            }
            else if (prop.getEdmType() == EdmType.GUID) {                
                if (paramType.equals(UUID.class)) {
                    this.setter.invoke(instance, prop.getValueAsUUID());
                }
            }
            else if (prop.getEdmType() == EdmType.INT32) {
                if (paramType.equals(Integer.class)) {
                    this.setter.invoke(instance, prop.getValueAsIntegerObject());
                }
                else if (paramType.equals(int.class)) {
                    this.setter.invoke(instance, prop.getValueAsInteger());
                }
            }
            else if (prop.getEdmType() == EdmType.INT64) {
                if (paramType.equals(Long.class)) {
                    this.setter.invoke(instance, prop.getValueAsLongObject());
                }
                else if (paramType.equals(long.class)) {
                    this.setter.invoke(instance, prop.getValueAsLong());
                }
            }
            else if (prop.getEdmType() == EdmType.DATE_TIME) {
                if (paramType.equals(Date.class)) {
                    this.setter.invoke(instance, prop.getValueAsDate());
                }
            }
            else {
                throw new IllegalArgumentException(String.format(SR.PROPERTY_CANNOT_BE_SERIALIZED_AS_GIVEN_EDMTYPE,
                        this.name, prop.getEdmType().toString()));
            }
        }
    }

    /**
     * Reserved for internal use. Generates an {@link EntityProperty} from the result of invoking the getter method for
     * this property on the specified instance parameter.
     * 
     * @param instance
     *            An instance of a class supporting this property with getter and setter methods of the
     *            appropriate name and parameter or return type.
     * 
     * @return
     *         An {@link EntityProperty} with the data type and value returned by the invoked getter on the instance.
     * 
     * @throws IllegalArgumentException
     *             if the specified instance parameter is not an instance of the class
     *             or interface declaring the getter method (or of a subclass or implementor thereof).
     * @throws IllegalAccessException
     *             if the getter method is inaccessible.
     * @throws InvocationTargetException
     *             if the getter method throws an exception.
     */
    protected EntityProperty generateEntityProperty(final Object instance) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Class<?> getType = this.getter.getReturnType();
        Object val = this.getter.invoke(instance, (Object[]) null);
        return new EntityProperty(val, getType);
    }

    /**
     * Reserved for internal use. A utility function that returns <code>true</code> if this property is accessible
     * through reflection.
     * 
     * @return
     */
    protected boolean shouldProcess() {
        if (Utility.isNullOrEmpty(this.name) || this.getter == null || this.getter.isAnnotationPresent(Ignore.class)
                || this.setter == null || this.setter.isAnnotationPresent(Ignore.class)
                || (!this.getter.getReturnType().equals(this.setter.getParameterTypes()[0]))) {
            return false;
        }

        return true;
    }
}
