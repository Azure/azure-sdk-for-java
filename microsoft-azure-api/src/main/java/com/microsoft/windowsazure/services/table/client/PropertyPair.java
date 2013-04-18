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

package com.microsoft.windowsazure.services.table.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Reserved for internal use. A class used internally during the reflection process to determine which properties should
 * be serialized.
 */
class PropertyPair {
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
    protected static HashMap<String, PropertyPair> generatePropertyPairs(final Class<?> clazzType) {
        final Method[] methods = clazzType.getMethods();
        final HashMap<String, PropertyPair> propMap = new HashMap<String, PropertyPair>();

        String propName = null;
        PropertyPair currProperty = null;

        for (final Method m : methods) {
            if (m.getName().length() < 4 || (!m.getName().startsWith("get") && !m.getName().startsWith("set"))) {
                continue;
            }

            // TODO add logging
            // System.out.println(m.getName());

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

            // TODO add logging
            // System.out.println(m.getReturnType());
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
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
                    throw new IllegalArgumentException(String.format(
                            "StoreAs Annotation found for property %s with empty value", currProperty.name));
                }

                if (currProperty.effectiveName != null && !currProperty.effectiveName.equals(currProperty.name)
                        && !currProperty.effectiveName.equals(storeAsInstance.name())) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "StoreAs Annotation found for both getter and setter for property %s with non equal values",
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
    protected void consumeTableProperty(final EntityProperty prop, final Object instance)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (prop.getEdmType() == EdmType.STRING) {
            this.setter.invoke(instance, prop.getValueAsString());
        }
        else if (prop.getEdmType() == EdmType.BINARY) {
            if (this.setter.getParameterTypes()[0].equals(Byte[].class)) {
                this.setter.invoke(instance, (Object) prop.getValueAsByteObjectArray());
            }
            else {
                this.setter.invoke(instance, prop.getValueAsByteArray());
            }
        }
        else if (prop.getEdmType() == EdmType.BOOLEAN) {
            this.setter.invoke(instance, prop.getValueAsBoolean());
        }
        else if (prop.getEdmType() == EdmType.DOUBLE) {
            this.setter.invoke(instance, prop.getValueAsDouble());
        }
        else if (prop.getEdmType() == EdmType.GUID) {
            this.setter.invoke(instance, prop.getValueAsUUID());
        }
        else if (prop.getEdmType() == EdmType.INT32) {
            this.setter.invoke(instance, prop.getValueAsInteger());
        }
        else if (prop.getEdmType() == EdmType.INT64) {
            this.setter.invoke(instance, prop.getValueAsLong());
        }
        else if (prop.getEdmType() == EdmType.DATE_TIME) {
            this.setter.invoke(instance, prop.getValueAsDate());
        }
        else {
            throw new IllegalArgumentException(String.format("Property %s with Edm Type %s cannot be de-serialized.",
                    this.name, prop.getEdmType().toString()));
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
    protected EntityProperty generateTableProperty(final Object instance) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Class<?> getType = this.getter.getReturnType();
        Object val = this.getter.invoke(instance, (Object[]) null);

        if (getType.equals(byte[].class)) {
            return val != null ? new EntityProperty((byte[]) val) : new EntityProperty(EdmType.BINARY);
        }
        else if (getType.equals(Byte[].class)) {
            return val != null ? new EntityProperty((Byte[]) val) : new EntityProperty(EdmType.BINARY);
        }
        else if (getType.equals(String.class)) {
            return val != null ? new EntityProperty((String) val) : new EntityProperty(EdmType.STRING);
        }
        else if (getType.equals(boolean.class) || getType.equals(Boolean.class)) {
            return val != null ? new EntityProperty((Boolean) val) : new EntityProperty(EdmType.BOOLEAN);
        }
        else if (getType.equals(double.class) || getType.equals(Double.class)) {
            return val != null ? new EntityProperty((Double) val) : new EntityProperty(EdmType.DOUBLE);
        }
        else if (getType.equals(UUID.class)) {
            return val != null ? new EntityProperty((UUID) val) : new EntityProperty(EdmType.GUID);
        }
        else if (getType.equals(int.class) || getType.equals(Integer.class)) {
            return val != null ? new EntityProperty((Integer) val) : new EntityProperty(EdmType.INT32);
        }
        else if (getType.equals(long.class) || getType.equals(Long.class)) {
            return val != null ? new EntityProperty((Long) val) : new EntityProperty(EdmType.INT64);
        }
        else if (getType.equals(Date.class)) {
            return val != null ? new EntityProperty((Date) val) : new EntityProperty(EdmType.DATE_TIME);
        }
        else {
            throw new IllegalArgumentException(String.format("Property %s with return type %s cannot be serialized.",
                    this.getter.getName(), this.getter.getReturnType()));
        }
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

        // TODO add logging
        // System.out.println("Valid property " + this.name + " Storing as " + this.effectiveName);
        return true;
    }
}
