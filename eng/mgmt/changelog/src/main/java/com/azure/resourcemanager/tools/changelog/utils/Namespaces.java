// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog.utils;

import japicmp.model.JApiClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Namespaces {
    private final Set<String> namespaces;
    private String baseNamespace;

    public Namespaces(List<JApiClass> jApiClasses) {
        namespaces = new HashSet<>();
        baseNamespace = null;
        jApiClasses.forEach(jApiClass -> {
            String namespace = ClassName.namespace(jApiClass);
            namespaces.add(namespace);
            if (baseNamespace == null || baseNamespace.length() > namespace.length()) {
                baseNamespace = namespace;
            }
        });
    }

    public String getBase() {
        return baseNamespace;
    }

    public Set<String> getNamespaces() {
        return namespaces;
    }
}
