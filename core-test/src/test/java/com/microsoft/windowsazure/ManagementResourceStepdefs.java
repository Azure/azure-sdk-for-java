/*
 * Copyright 2013 Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;

import com.microsoft.windowsazure.management.ManagementConfiguration;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

public class ManagementResourceStepdefs
{
    private HashMap<String, Object> objects = new HashMap<String, Object>();
    
    @Given("^I create a \"([^\"]*)\" with name \"([^\"]*)\"$")
    public void i_create_a_with_name(String objectType, String name) throws Exception
    {
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
        else
        {
            Class<?> objectClass = Class.forName(getJavaType(objectType));
            objects.put(name, objectClass.newInstance());
        }
    }
    
    @Given("^set \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void set_property(String propertyName, String propertyValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
        
        Method method = object.getClass().getMethod("set" + TextUtility.ToPascalCase(parts[parts.length - 1]), propertyValue.getClass());
        method.invoke(object, propertyValue);
    }

    @And("^set \"([^\"]*)\" with value from list \"([^\"]*)\" where \"([^\"]*)\" of type \"([^\"]*)\" equals \"([^\"]*)\"$")
    public void set_value_where_equals(String objectName, String path, String propertyPath, String propertyType, String value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        String[] parts = path.split("\\.");
        Object parent = getObject(parts);
        ArrayList<?> arrayObject = (ArrayList<?>) getPropertyValue(parent, parts[parts.length - 1]);
        
        for (int i = 0; i < arrayObject.size(); i++)
        {
        	String[] propertyParts = propertyPath.split("\\.");
        	Object propertyParent = getObject(arrayObject.get(i), propertyParts);
            Object propertyValue = getPropertyValue(propertyParent, propertyParts[propertyParts.length - 1]);
        	if (propertyValue.equals(TextUtility.convertStringTo(value, propertyType)))
        	{
        		objects.put(objectName, arrayObject.get(i));
        		return;
        	}
        }
        
        throw new NullPointerException();
    }
    
    private Object getPropertyValue(Object parent, String propertyName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Method method;
        if (propertyName.toLowerCase().equals("length"))
        {
            method = parent.getClass().getMethod("size");
        }
        else
        {
            method = parent.getClass().getMethod("get" + TextUtility.ToPascalCase(propertyName));
        }
        
        return method.invoke(parent);
    }
    
    @And("^property with type \"([^\"]*)\" and path \"([^\"]*)\" should equal \"([^\"]*)\"$")
    public void and_get_property_equals(String propertyType, String propertyName, String propertyValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
 
        Object result = getPropertyValue(object, parts[parts.length - 1]);
        
        // Assert
        Assert.assertEquals(TextUtility.convertStringTo(propertyValue, propertyType), result);
    }

    @And("^property with type \"([^\"]*)\" and path \"([^\"]*)\" should not equal \"([^\"]*)\"$")
    public void and_get_property_not_equals(String propertyType, String propertyName, String propertyValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
 
        Method method = object.getClass().getMethod("get" + TextUtility.ToPascalCase(parts[parts.length - 1]));
        Object result = method.invoke(object);

        // Assert
        Assert.assertNotEquals(TextUtility.convertStringTo(propertyValue, propertyType), result);
    }
    
    @When("^I invoke \"([^\"]*)\" with parameter value \"([^\"]*)\" of type \"([^\"]*)\" I get the result into \"([^\"]*)\"$")
    public void when_invoke_with_parameter_value_get_result(String methodName, Object parameter, String parameterType, String resultName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException
    {
        objects.put(resultName, when_invoke_with_parameter_value(methodName, parameter, parameterType));
    }
    
    @When("^I invoke \"([^\"]*)\" with parameter \"([^\"]*)\" I get the result into \"([^\"]*)\"$")
    public void when_invoke_with_parameter_get_result(String methodName, String parameterName, String resultName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException
    {
        Object parameter = objects.get(parameterName);
        objects.put(resultName, when_invoke_with_parameter_value(methodName, parameter, parameter.getClass().getName()));
    }

    @When("^I invoke \"([^\"]*)\" I get the result into \"([^\"]*)\"$")
    public void when_invoke_get_result(String methodName, String resultName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        objects.put(resultName, when_invoke(methodName));
    }
    
    @When("^I invoke \"([^\"]*)\"$")
    public Object when_invoke(String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        String[] parts = methodName.split("\\.");
        Object object = getObject(parts);
        
        Method method = object.getClass().getMethod(TextUtility.ToCamelCase(parts[parts.length - 1]));
        return method.invoke(object);
    }
    
    @When("^I invoke \"([^\"]*)\" with parameter value \"([^\"]*)\" of type \"([^\"]*)\"$")
    public Object when_invoke_with_parameter_value(String methodName, Object parameter, String parameterType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException
    {
        String[] parts = methodName.split("\\.");
        Object object = getObject(parts);
        
        Method method = object.getClass().getMethod(TextUtility.ToCamelCase(parts[parts.length - 1]), Class.forName(getJavaType(parameterType)));
        return method.invoke(object, parameter);
    }

    @When("^I invoke \"([^\"]*)\" with parameter \"([^\"]*)\"$")
    public Object when_invoke_with_parameter(String methodName, String parameterName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException
    {
        Object parameter = objects.get(parameterName);
        return when_invoke_with_parameter_value(methodName, parameter, parameter.getClass().getName());
    }
    
    @When("^invoke \"([^\"]*)\" with parameter value \"([^\"]*)\" of type \"([^\"]*)\"$")
    public Object then_invoke_with_parameter_value(String methodName, Object parameter, String parameterType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException
    {
        return when_invoke_with_parameter_value(methodName, parameter, parameterType);
    }
    
    private Object getObject(String[] parts) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Object object = objects.get(parts[0]);
        
        String[] memberParts = Arrays.copyOfRange(parts, 1, parts.length);
        return getObject(object, memberParts);
    }
    
    private Object getObject(Object parent, String[] parts) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        for (int i = 0; i < (parts.length - 1); i++)
        {
            if (parts[i].endsWith("]"))
            {
                String propertyName = parts[i].substring(0, parts[i].lastIndexOf("["));
                Method method = parent.getClass().getMethod("get" + TextUtility.ToPascalCase(propertyName));
                parent = method.invoke(parent);
                
                int index = Integer.parseInt(parts[i].substring(parts[i].lastIndexOf("[") + 1, parts[i].length() - 1));
                Method indexMethod = parent.getClass().getMethod("get", int.class);
                parent = indexMethod.invoke(parent, index);
            }
            else
            {
                Method method = parent.getClass().getMethod("get" + TextUtility.ToPascalCase(parts[i]));
                parent = method.invoke(parent);   
            }
        }
        
        return parent;
    }
    
    private String getJavaType(String csharpType)
    {
        if (csharpType.equals("System.Int32"))
        {
            return "java.lang.Integer";
        }
        else if (csharpType.equals("System.String"))
        {
            return "java.lang.String";
        }
        else
        {
            return csharpType
                    .replace("Microsoft.WindowsAzure.Management.Models",
                            "com.microsoft.windowsazure.management.models");
        }
    }
    
    protected static Configuration createConfiguration() throws Exception
    {
        return ManagementConfiguration.configure(
                "db1ab6f0-4769-4b27-930e-01e2ef9c123c",
                "C:\\sources\\certificates\\WindowsAzureKeyStore.jks",
                "test123"
        );
    }
}