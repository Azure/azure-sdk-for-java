package com.microsoft.windowsazure;

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
    
    public static Object convertStringTo(String input, String type)
    {
        if (type.equals("System.Int32")) {
            return Integer.parseInt(input);
        } else if (type.equals("System.String")) {
            return input;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
