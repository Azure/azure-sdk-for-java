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
import java.util.HashMap;

import org.junit.Assert;

import com.microsoft.windowsazure.management.ManagementConfiguration;
import com.microsoft.windowsazure.management.ManagementService;

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
            objects.put(name, ManagementService.create(createConfiguration()));   
        } else {
            Class<?> objectClass = Class.forName(getJavaType(objectType));
            objects.put(name, objectClass.newInstance());
        }
    }
    
    @Given("^set property \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void set_property(String propertyName, String propertyValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
        
        Method method = object.getClass().getMethod("set" + TextUtility.ToPascalCase(parts[parts.length - 1]), propertyValue.getClass());
        method.invoke(object, propertyValue);
    }

    @And("^property with type \"([^\"]*)\" and path \"([^\"]*)\" should equal \"([^\"]*)\"$")
    public void and_get_property_equals(String propertyType, String propertyName, String propertyValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        String[] parts = propertyName.split("\\.");
        Object object = getObject(parts);
 
        Method method;
        if (parts[parts.length - 1].toLowerCase().equals("length"))
        {
            method = object.getClass().getMethod("length");
        }
        else
        {
            method = object.getClass().getMethod("get" + TextUtility.ToPascalCase(parts[parts.length - 1]));
        }
        
        Object result = method.invoke(object);
        
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
        for (int i = 1; i < (parts.length - 1); i++)
        {
            if (parts[i].endsWith("]"))
            {
                String propertyName = parts[i].substring(0, parts[i].lastIndexOf("["));
                Method method = object.getClass().getMethod("get" + TextUtility.ToPascalCase(propertyName));
                object = method.invoke(object);
                
                int index = Integer.parseInt(parts[i].substring(parts[i].lastIndexOf("[") + 1, parts[i].length() - 1));
                Method indexMethod = object.getClass().getMethod("get", int.class);
                object = indexMethod.invoke(object, index);
            }
            else
            {
                Method method = object.getClass().getMethod("get" + TextUtility.ToPascalCase(parts[i]));
                object = method.invoke(object);   
            }
        }
        
        return object;
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
                System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                System.getenv(ManagementConfiguration.KEYSTORE_PATH),
                System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD)
        );
    }
}