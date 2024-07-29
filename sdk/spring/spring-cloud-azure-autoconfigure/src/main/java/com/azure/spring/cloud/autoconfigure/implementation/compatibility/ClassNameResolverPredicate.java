// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

public class ClassNameResolverPredicate {

    @SuppressWarnings("rawtypes")
    boolean resolve(String fullyQualifiedClassName) {
        try {
            if (fullyQualifiedClassName == null) {
                return false;
            }
            if (fullyQualifiedClassName.contains(",")) {
                String[] stringNames = fullyQualifiedClassName.split(",");
                if (stringNames.length > 2) {
                    Class[] params = new Class[stringNames.length - 2];
                    for (int i = 2; i < stringNames.length; i++) {
                        if (stringNames[i].equals("int")) {
                            params[i - 2] = int.class;
                            continue;
                        }
                        if (stringNames[i].equals("boolean")) {
                            params[i - 2] = boolean.class;
                            continue;
                        }
                        params[i - 2] = Class.forName(stringNames[i]);
                    }
                    Class.forName(stringNames[0]).getMethod(stringNames[1], params);
                } else {
                    Class.forName(stringNames[0]).getMethod(stringNames[1]);
                }
            } else {
                Class.forName(fullyQualifiedClassName);
            }
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }
}
