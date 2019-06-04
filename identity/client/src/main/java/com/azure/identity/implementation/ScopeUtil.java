package com.azure.identity.implementation;

import java.util.Objects;

public class ScopeUtil {

    private static final String DEFAULT_SUFFIX = "/.defualt";


    public static String scopesToResource(String[] scopes)
    {
        Objects.nonNull(scopes);
        if (scopes.length != 1) throw new IllegalArgumentException("To convert to a resource string the specified array must be exactly length 1");

        if (!scopes[0].endsWith(DEFAULT_SUFFIX))
        {
            return scopes[0];
        }

        return scopes[0].substring(0, scopes[0].lastIndexOf(DEFAULT_SUFFIX));
    }

    public static String[] resourceToScopes(String resource)
    {
        return new String[] { resource + "/.default" };
    }
}
