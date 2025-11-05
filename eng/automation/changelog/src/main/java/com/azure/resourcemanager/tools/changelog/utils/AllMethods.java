// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog.utils;

import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllMethods {
    public JApiClass getJApiClass() {
        return jApiClass;
    }

    public List<JApiMethod> getMethods() {
        return methods;
    }

    public List<JApiConstructor> getConstructors() {
        return constructors;
    }

    private JApiClass jApiClass;
    private List<JApiMethod> methods;
    private List<JApiConstructor> constructors;

    private AllMethods(JApiClass jApiClass, List<JApiMethod> methods, List<JApiConstructor> constructors) {
        this.jApiClass = jApiClass;
        this.methods = methods;
        this.constructors = constructors;
    }

    public static void fromClasses(Map<String, JApiClass> classes, Map<String, AllMethods> results) {
        classes.forEach((name, apiClass) -> {
            if (!results.containsKey(name)) {
                getAllMethods(apiClass, classes, results);
            }
        });
    }

    private static void getAllMethods(JApiClass apiClass, Map<String, JApiClass> classes, Map<String, AllMethods> results) {
        Set<JApiMethod> methods = new HashSet<>(apiClass.getMethods());
        apiClass.getInterfaces().forEach(aInterface -> {
            if (classes.containsKey(aInterface.getFullyQualifiedName())) {
                if (!results.containsKey(aInterface.getFullyQualifiedName())) {
                    getAllMethods(classes.get(aInterface.getFullyQualifiedName()), classes, results);
                }
                methods.addAll(results.get(aInterface.getFullyQualifiedName()).getMethods());
            }
        });
        Set<JApiConstructor> contructors = new HashSet<>(apiClass.getConstructors());
        results.put(apiClass.getFullyQualifiedName(), new AllMethods(apiClass, new ArrayList<>(methods), new ArrayList<>(contructors)));
    }
}
