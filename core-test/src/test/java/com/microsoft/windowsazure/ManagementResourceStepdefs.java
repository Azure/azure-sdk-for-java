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

package com.microsoft.windowsazure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.junit.Assert;

import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * The Class ManagementResourceStepdefs.
 */
public class ManagementResourceStepdefs {
    /** The objects. */
    private HashMap<String, Object> objects = new HashMap<String, Object>();
    
    /** The random. */
    private static Random random = new Random();
    
    /**
     * I_create_a_character_random_string_with_name.
     *
     * @param length the length
     * @param name the name
     * @param prefix the prefix
     */
    @And("^I create a \"([^\"]*)\" character random String with name \"([^\"]*)\" and prefix \"([^\"]*)\"$")
    public void i_create_a_character_random_string_with_name(int length, String name, String prefix) {
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        String randomString = prefix + stringBuilder.toString();
        objects.put(name, randomString);
    }
    
    /**
     * I_create_a_with_name.
     *
     * @param objectType the object type
     * @param name the name
     * @throws Exception the exception
     */
    @Given("^I create a \"([^\"]*)\" with name \"([^\"]*)\"$")
    public void i_create_a_with_name(String objectType, String name) throws Exception {
        if (objectType.equals("ManagementClient")) {
            Class<?> serviceClass = Class.forName("com.microsoft.windowsazure.management.ManagementService");
            Method method = serviceClass.getMethod("create", Configuration.class);
            objects.put(name, method.invoke(null, createConfiguration()));
        }
        else if (objectType.equals("WebSiteManagementClient")) {
            Class<?> serviceClass = Class.forName("com.microsoft.windowsazure.management.websites.WebSiteManagementService");
            Method method = serviceClass.getMethod("create", Configuration.class);
            objects.put(name, method.invoke(null, createConfiguration()));
        }
        else if (objectType.equals("StorageManagementClient")) {
            Class<?> serviceClass = Class.forName("com.microsoft.windowsazure.management.storage.StorageManagementService");
            Method method = serviceClass.getMethod("create", Configuration.class);
            objects.put(name, method.invoke(null, createConfiguration()));
        }
        else if (objectType.equals("SqlManagementClient")) {
            Class<?> serviceClass = Class.forName("com.microsoft.windowsazure.management.sql.SqlManagementService");
            Method method = serviceClass.getMethod("create", Configuration.class);
            objects.put(name, method.invoke(null, createConfiguration()));   
        }
        else {
            Class<?> objectClass = TextUtility.getJavaType(objectType);
            objects.put(name, objectClass.newInstance());
        }
    }

    /**
     * Set_property_obj.
     *
     * @param propertyName the property name
     * @param propertyObj the property obj
     * @param propertyType the property type
     * @throws ClassNotFoundException the class not found exception
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Given("^set \"([^\"]*)\" with \"([^\"]*)\" of type \"([^\"]*)\"$")
    public void set_property_obj(String propertyName, String propertyObj, String propertyType) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
        if (parts.length > 1) {
            Method method = object.getClass().getMethod("set" + TextUtility.ToPascalCase(parts[parts.length - 1]), TextUtility.getJavaType(propertyType));
            method.invoke(object, objects.get(propertyObj));
        }
        else {
            objects.put(propertyName, objects.get(propertyObj));
        }
    }
    
    /**
     * Set_property.
     *
     * @param propertyName the property name
     * @param propertyValue the property value
     * @param propertyType the property type
     * @throws ClassNotFoundException the class not found exception
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Given("^set \"([^\"]*)\" with value \"([^\"]*)\" of type \"([^\"]*)\"$")
    public void set_property(String propertyName, String propertyValue, String propertyType) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
        if (parts.length > 1) {
            Method method = object.getClass().getMethod("set" + TextUtility.ToPascalCase(parts[parts.length - 1]), TextUtility.getJavaType(propertyType));
            method.invoke(object, TextUtility.convertStringTo(propertyValue, propertyType));
        }
        else {
            objects.put(propertyName, TextUtility.convertStringTo(propertyValue, propertyType));
        }
    }

    /**
     * Set_property_from_path.
     *
     * @param propertyName the property name
     * @param valuePath the value path
     * @param propertyType the property type
     * @throws ClassNotFoundException the class not found exception
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Given("^set \"([^\"]*)\" with value from path \"([^\"]*)\" of type \"([^\"]*)\"$")
    public void set_property_from_path(String propertyName, String valuePath, String propertyType) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] propertyParts = valuePath.split("\\.");
        Object propertyObject = getObject(propertyParts);
 
        Object propertyValue = getPropertyValue(propertyObject, propertyParts[propertyParts.length - 1]);

        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
        if (parts.length > 1) {
            Method method = object.getClass().getMethod("set" + TextUtility.ToPascalCase(parts[parts.length - 1]), TextUtility.getJavaType(propertyType));
            method.invoke(object, propertyValue);
        }
        else {
            objects.put(propertyName, propertyValue);
        }
    }

    /**
     * Set_value_where_equals.
     *
     * @param objectName the object name
     * @param path the path
     * @param propertyPath the property path
     * @param propertyType the property type
     * @param value the value
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @And("^set \"([^\"]*)\" with value from list \"([^\"]*)\" where \"([^\"]*)\" of type \"([^\"]*)\" equals \"([^\"]*)\"$")
    public void set_value_where_equals(String objectName, String path, String propertyPath, String propertyType, String value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts = path.split("\\.");
        Object parent = getObject(parts);
        ArrayList<?> arrayObject = (ArrayList<?>) getPropertyValue(parent, parts[parts.length - 1]);
        
        for (int i = 0; i < arrayObject.size(); i++) {
            String[] propertyParts = propertyPath.split("\\.");
            Object propertyParent = getObject(arrayObject.get(i), propertyParts);
            Object propertyValue = getPropertyValue(propertyParent, propertyParts[propertyParts.length - 1]);
            if (propertyValue.equals(TextUtility.convertStringTo(value, propertyType))) {
                objects.put(objectName, arrayObject.get(i));
                return;
            }
        }

        throw new NullPointerException();
    }
    
    /**
     * Set_value_where_equals_parameter.
     *
     * @param objectName the object name
     * @param path the path
     * @param propertyPath the property path
     * @param propertyType the property type
     * @param parameterName the parameter name
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @And("^set \"([^\"]*)\" with value from list \"([^\"]*)\" where \"([^\"]*)\" of type \"([^\"]*)\" equals parameter \"([^\"]*)\"$")
    public void set_value_where_equals_parameter(String objectName, String path, String propertyPath, String propertyType, String parameterName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts = path.split("\\.");
        Object parent = getObject(parts);
        ArrayList<?> arrayObject = (ArrayList<?>) getPropertyValue(parent, parts[parts.length - 1]);
        Object value = objects.get(parameterName);
        for (int i = 0; i < arrayObject.size(); i++) {
            String[] propertyParts = propertyPath.split("\\.");
            Object propertyParent = getObject(arrayObject.get(i), propertyParts);
            Object propertyValue = getPropertyValue(propertyParent, propertyParts[propertyParts.length - 1]);
            if (propertyValue.equals(value)) {
                objects.put(objectName, arrayObject.get(i));
                return;
            }
        }

        throw new NullPointerException();
    }

    /**
     * Gets the property value.
     *
     * @param parent the parent
     * @param propertyName the property name
     * @return the property value
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    private Object getPropertyValue(Object parent, String propertyName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method;
        if (propertyName.toLowerCase().equals("length")) {
            method = parent.getClass().getMethod("size");
        }
        else {
            method = parent.getClass().getMethod("get" + TextUtility.ToPascalCase(propertyName));
        }
        
        return method.invoke(parent);
    }
    
    /**
     * And_get_property_equals.
     *
     * @param propertyType the property type
     * @param propertyName the property name
     * @param propertyValue the property value
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @And("^property with type \"([^\"]*)\" and path \"([^\"]*)\" should equal \"([^\"]*)\"$")
    public void and_get_property_equals(String propertyType, String propertyName, String propertyValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
 
        Object result = getPropertyValue(object, parts[parts.length - 1]);

        // Assert
        Assert.assertEquals(TextUtility.convertStringTo(propertyValue, propertyType), result);
    }
    
    @And("^property with type \"([^\"]*)\" and path \"([^\"]*)\" should equal parameter \"([^\"]*)\"$")
    public void and_get_property_equals_parameters(String propertyType, String propertyName, String parameterName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
 
        Object result = getPropertyValue(object, parts[parts.length - 1]);

        Object parameterValue = objects.get(parameterName);
        // Assert
        Assert.assertEquals(parameterValue, result);
    }

    /**
     * And_get_property_not_equals.
     *
     * @param propertyType the property type
     * @param propertyName the property name
     * @param propertyValue the property value
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @And("^property with type \"([^\"]*)\" and path \"([^\"]*)\" should not equal \"([^\"]*)\"$")
    public void and_get_property_not_equals(String propertyType, String propertyName, String propertyValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
 
        Method method = object.getClass().getMethod("get" + TextUtility.ToPascalCase(parts[parts.length - 1]));
        Object result = method.invoke(object);

        // Assert
        Assert.assertNotEquals(TextUtility.convertStringTo(propertyValue, propertyType), result);
    }
    
    /**
     * When_invoke_with_parameter_value_get_result.
     *
     * @param methodName the method name
     * @param parameterValue the parameter value
     * @param parameterType the parameter type
     * @param resultName the result name
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @When("^I invoke \"([^\"]*)\" with parameter value \"([^\"]*)\" of type \"([^\"]*)\" I get the result into \"([^\"]*)\"$")
    public void when_invoke_with_parameter_value_get_result(String methodName, String parameterValue, String parameterType, String resultName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        objects.put(resultName, when_invoke_with_parameter_converted_value(methodName, TextUtility.convertStringTo(parameterValue, parameterType), parameterType));
    }

    /**
     * When_invoke_with_parameter_get_result.
     *
     * @param methodName the method name
     * @param parameterName the parameter name
     * @param resultName the result name
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @When("^I invoke \"([^\"]*)\" with parameter \"([^\"]*)\" I get the result into \"([^\"]*)\"$")
    public void when_invoke_with_parameter_get_result(String methodName, String parameterName, String resultName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Object parameter = objects.get(parameterName);
        objects.put(resultName, when_invoke_with_parameter_converted_value(methodName, parameter, parameter.getClass().getName()));
    }

    /**
     * When_invoke_get_result.
     *
     * @param methodName the method name
     * @param resultName the result name
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    @When("^I invoke \"([^\"]*)\" I get the result into \"([^\"]*)\"$")
    public void when_invoke_get_result(String methodName, String resultName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        objects.put(resultName, when_invoke(methodName));
    }

    /**
     * When_invoke.
     *
     * @param methodName the method name
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    @When("^I invoke \"([^\"]*)\"$")
    public Object when_invoke(String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] parts = methodName.split("\\.");
        Object object = getObject(parts);

        Method method = object.getClass().getMethod(TextUtility.ToCamelCase(parts[parts.length - 1]));
        return method.invoke(object);
    }

    /**
     * When_invoke_with_parameter_values.
     *
     * @param methodName the method name
     * @param parameter1 the parameter1
     * @param parameter1Type the parameter1 type
     * @param parameter2 the parameter2
     * @param parameter2Type the parameter2 type
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    public Object when_invoke_with_parameter_values(String methodName, Object parameter1, String parameter1Type, Object parameter2, String parameter2Type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts = methodName.split("\\.");
        Object object = getObject(parts);

        Method method = object.getClass().getMethod(TextUtility.ToCamelCase(parts[parts.length - 1]), TextUtility.getJavaType(parameter1Type), TextUtility.getJavaType(parameter2Type));
        return method.invoke(object, parameter1, parameter2);
    }

    public Object when_invoke_with_parameter_values(String methodName, Object parameter1, String parameter1Type, Object parameter2, String parameter2Type, Object parameter3, String parameter3Type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts = methodName.split("\\.");
        Object object = getObject(parts);

        Method method = object.getClass().getMethod(TextUtility.ToCamelCase(parts[parts.length - 1]), TextUtility.getJavaType(parameter1Type), TextUtility.getJavaType(parameter2Type), TextUtility.getJavaType(parameter3Type));
        return method.invoke(object, parameter1, parameter2, parameter3);
    }
    
    /**
     * When_invoke_with_parameter_values_string.
     *
     * @param methodName the method name
     * @param parameter1 the parameter1
     * @param parameter1Type the parameter1 type
     * @param parameter2 the parameter2
     * @param parameter2Type the parameter2 type
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @When("^I invoke \"([^\"]*)\" with parameter values \"([^\"]*)\" of type \"([^\"]*)\" and \"([^\"]*)\" of type \"([^\"]*)\"$")
    public Object when_invoke_with_parameter_values_string(String methodName, String parameter1, String parameter1Type, String parameter2, String parameter2Type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        return when_invoke_with_parameter_values(methodName, TextUtility.convertStringTo(parameter1, parameter1Type), parameter1Type, TextUtility.convertStringTo(parameter2, parameter2Type), parameter2Type);
    }

    /**
     * When_invoke_with_parameter_value.
     *
     * @param methodName the method name
     * @param parameterValue the parameter value
     * @param parameterType the parameter type
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @When("^I invoke \"([^\"]*)\" with parameter value \"([^\"]*)\" of type \"([^\"]*)\"$")
    public Object when_invoke_with_parameter_value(String methodName, String parameterValue, String parameterType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        return when_invoke_with_parameter_converted_value(methodName, TextUtility.convertStringTo(parameterValue, parameterType), parameterType);
    }

    /**
     * When_invoke_with_parameter_converted_value.
     *
     * @param methodName the method name
     * @param parameterValue the parameter value
     * @param parameterType the parameter type
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    public Object when_invoke_with_parameter_converted_value(String methodName, Object parameterValue, String parameterType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts = methodName.split("\\.");
        Object object = getObject(parts);
        
        Method method = object.getClass().getMethod(TextUtility.ToCamelCase(parts[parts.length - 1]), TextUtility.getJavaType(parameterType));
        return method.invoke(object, parameterValue);
    }

    /**
     * When_invoke_with_parameter.
     *
     * @param methodName the method name
     * @param parameterName the parameter name
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @When("^I invoke \"([^\"]*)\" with parameter \"([^\"]*)\"$")
    public Object when_invoke_with_parameter(String methodName, String parameterName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Object parameter = objects.get(parameterName);
        return when_invoke_with_parameter_converted_value(methodName, parameter, parameter.getClass().getName());
    }

    /**
     * When_invoke_with_parameter.
     *
     * @param methodName the method name
     * @param parameter1Name the parameter1 name
     * @param parameter2Name the parameter2 name
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @When("^I invoke \"([^\"]*)\" with parameters \"([^\"]*)\" and \"([^\"]*)\"$")
    public Object when_invoke_with_parameter(String methodName, String parameter1Name, String parameter2Name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts1 = parameter1Name.split("\\.");
        Object parameter1 = getObject(parts1);

        String[] parts2 = parameter2Name.split("\\.");
        Object parameter2 = getObject(parts2);

        return when_invoke_with_parameter_values(methodName, parameter1, parameter1.getClass().getName(), parameter2, parameter2.getClass().getName());
    }

    /**
     * When_invoke_with_parameter.
     *
     * @param methodName the method name
     * @param parameter1Name the parameter1 name
     * @param parameter2Name the parameter2 name
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @When("^I invoke \"([^\"]*)\" with parameters \"([^\"]*)\" and \"([^\"]*)\" and \"([^\"]*)\"$")
    public Object when_invoke_with_parameter(String methodName, String parameter1Name, String parameter2Name, String parameter3Name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String[] parts1 = parameter1Name.split("\\.");
        Object parameter1 = getObject(parts1);

        String[] parts2 = parameter2Name.split("\\.");
        Object parameter2 = getObject(parts2);
        
        String[] parts3 = parameter3Name.split("\\.");
        Object parameter3 = getObject(parts3);

        return when_invoke_with_parameter_values(methodName, parameter1, parameter1.getClass().getName(), parameter2, parameter2.getClass().getName(), parameter3, parameter3.getClass().getName());
    }
    
    /**
     * When_invoke_with_parameters_get_result.
     *
     * @param methodName the method name
     * @param parameter1Name the parameter1 name
     * @param parameter2Name the parameter2 name
     * @param resultName the result name
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @When("^I invoke \"([^\"]*)\" with parameters \"([^\"]*)\" and \"([^\"]*)\" I get the result into \"([^\"]*)\"$")
    public void  when_invoke_with_parameters_get_result(String methodName, String parameter1Name, String parameter2Name, String resultName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        objects.put(resultName, when_invoke_with_parameter(methodName, parameter1Name, parameter2Name));
    }
    
    /**
     * Then_invoke_with_parameter_of_type.
     *
     * @param methodName the method name
     * @param parameter the parameter
     * @param parameterType the parameter type
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws SecurityException the security exception
     * @throws ClassNotFoundException the class not found exception
     */
    @Then("^invoke \"([^\"]*)\" with parameter \"([^\"]*)\" of type \"([^\"]*)\"$")
    public Object then_invoke_with_parameter_of_type(String methodName, Object parameter, String parameterType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SecurityException, ClassNotFoundException {
        String[] parts = methodName.split("\\.");
        Object object = getObject(parts);
        Object parameterObject = objects.get(parameter);
        Method method = object.getClass().getMethod(TextUtility.ToCamelCase(parts[parts.length - 1]), TextUtility.getJavaType(parameterType));
        return method.invoke(object, parameterObject);        
    }

    /**
     * Then_invoke_with_parameter_value.
     *
     * @param methodName the method name
     * @param parameter the parameter
     * @param parameterType the parameter type
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @Then("^invoke \"([^\"]*)\" with parameter value \"([^\"]*)\" of type \"([^\"]*)\"$")
    public Object then_invoke_with_parameter_value(String methodName, Object parameter, String parameterType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        return when_invoke_with_parameter_converted_value(methodName, parameter, parameterType);
    }

    /**
     * I_invoke_with_parameter_values_of_type_and_of_type_ i_get_the_result_into.
     *
     * @param methodName the method name
     * @param parameter1 the parameter1
     * @param parameter1Type the parameter1 type
     * @param parameter2 the parameter2
     * @param parameter2Type the parameter2 type
     * @param resultName the result name
     * @throws Throwable the throwable
     */
    @When("^I invoke \"([^\"]*)\" with parameter values \"([^\"]*)\" of type \"([^\"]*)\" and \"([^\"]*)\" of type \"([^\"]*)\" I get the result into \"([^\"]*)\"$")
    public void I_invoke_with_parameter_values_of_type_and_of_type_I_get_the_result_into(String methodName, String parameter1, String parameter1Type, String parameter2, String parameter2Type, String resultName) throws Throwable {
        objects.put(resultName, when_invoke_with_parameter_values(methodName, TextUtility.convertStringTo(parameter1, parameter1Type), parameter1Type, TextUtility.convertStringTo(parameter2, parameter2Type), parameter2Type));
    }

    /**
     * Then_invoke_with_parameter_values.
     *
     * @param methodName the method name
     * @param parameter1 the parameter1
     * @param parameter1Type the parameter1 type
     * @param parameter2 the parameter2
     * @param parameter2Type the parameter2 type
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    @Then("^invoke \"([^\"]*)\" with parameter values \"([^\"]*)\" of type \"([^\"]*)\" and \"([^\"]*)\" of type \"([^\"]*)\"$")
    public Object then_invoke_with_parameter_values(String methodName, Object parameter1, String parameter1Type, Object parameter2, String parameter2Type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        return when_invoke_with_parameter_values(methodName, parameter1, parameter1Type, parameter2, parameter2Type);
    }

    /**
     * Gets the object.
     *
     * @param parts the parts
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    private Object getObject(String[] parts) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (objects.containsKey(parts[0])) {
            Object object = objects.get(parts[0]);

            String[] memberParts = Arrays.copyOfRange(parts, 1, parts.length);
            return getObject(object, memberParts);
        }

        return null;
    }

    /**
     * Gets the object.
     *
     * @param parent the parent
     * @param parts the parts
     * @return the object
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    private Object getObject(Object parent, String[] parts) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < (parts.length - 1); i++) {
            if (parts[i].endsWith("]")) {
                String propertyName = parts[i].substring(0, parts[i].lastIndexOf("["));
                Method method = parent.getClass().getMethod("get" + TextUtility.ToPascalCase(propertyName));
                parent = method.invoke(parent);
                
                int index = Integer.parseInt(parts[i].substring(parts[i].lastIndexOf("[") + 1, parts[i].length() - 1));
                Method indexMethod = parent.getClass().getMethod("get", int.class);
                parent = indexMethod.invoke(parent, index);
            }
            else {
                Method method = parent.getClass().getMethod("get" + TextUtility.ToPascalCase(parts[i]));
                parent = method.invoke(parent);   
            }
        }

        return parent;
    }

    /**
     * Creates the configuration.
     *
     * @return the configuration
     * @throws Exception the exception
     */
    protected static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv(ManagementConfiguration.URI);
        return ManagementConfiguration.configure(
            baseUri != null ? new URI(baseUri) : null,
            System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
            System.getenv(ManagementConfiguration.KEYSTORE_PATH),
            System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
            KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
        );
    }
}