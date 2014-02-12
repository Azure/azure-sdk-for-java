package com.microsoft.windowsazure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import com.microsoft.windowsazure.management.websites.models.ServerFarmWorkerSize;
import com.microsoft.windowsazure.management.websites.models.WebSiteCreateParameters;

public final class TextUtility
{
    public static String ToPascalCase(String input)
    {
        return input.substring(0, 1).toUpperCase() +
                input.substring(1);
    }
    
    public static String ToCamelCase(String input)
    {
        return input.substring(0, 1).toLowerCase() +
                input.substring(1);
    }
    
    public static Object convertStringTo(String input, String type) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        if (input.equals("null"))
        {
            return null;
        }

        if (type.equals("System.Int32"))
        {
            return DatatypeConverter.parseInt(input);
        }
        else if (type.equals("System.String"))
        {
            return input;
        }
        else if (type.equals("System.DateTime"))
        {
            if (input.equals("DateTime.Now"))
            {
            	return Calendar.getInstance();
            }

            return DatatypeConverter.parseDateTime(input);
        }
        else if (Enum.class.isAssignableFrom(getJavaType(type)))
        {
            Class<?> enumType = getJavaType(type);
            Method method = enumType.getMethod("valueOf", String.class);
            return method.invoke(null, input);
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }
    
    public static Class<?> getJavaType(String csharpType) throws ClassNotFoundException
    {
        if (csharpType.equals("System.Int32"))
        {
            return int.class;
        }
        else if (csharpType.equals("System.String"))
        {
            return String.class;
        }
        else if (csharpType.equals("System.DateTime"))
        {
            return Calendar.class;
        }
        else if (csharpType.equals("Microsoft.WindowsAzure.Management.WebSites.Models.WebSiteCreateParameters.WebSpaceDetails"))
        {
            return WebSiteCreateParameters.WebSpaceDetails.class;   
        }
        else
        {
            return Class.forName(csharpType
                    .replace("Microsoft.WindowsAzure.Management",
                            "com.microsoft.windowsazure.management")
                    .replace(".Models", ".models")
                    .replace(".WebSites.", ".websites.")
                    .replace(".Storage.", ".storage."));
        }
    }
}
