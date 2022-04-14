package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

public class ClassNameResolverPredicate {

    boolean resolve(String fullyQuallifiedClassName) {
        try {
            if (fullyQuallifiedClassName == null) {
                return false;
            }

            Class.forName(fullyQuallifiedClassName);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
