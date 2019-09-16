// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class handles loading available HTTP clients
 */
public class HttpClientProviders {
    private static HttpClientProvider defaultProvider;

    static {
//        ModuleFinder finder = ModuleFinder.of(Paths.get("."));
//        ModuleLayer parent = ModuleLayer.boot();
//        Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of("com.azure.core.http"));
//        ClassLoader scl = ClassLoader.getSystemClassLoader();
//        ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);
//
//        Class<?> c = layer.findLoader("myapp").loadClass("app.Main");

//        ModuleLayer.boot().modules().stream()
//            .map(Module::getName)
//            .forEach(System.out::println);
//
//        ModuleFinder finder = ModuleFinder.of(Paths.get(""));
//        Set<ModuleReference> moduleReferences = finder.findAll();

        ServiceLoader<HttpClientProvider> serviceLoader = ServiceLoader.load(HttpClientProvider.class);

        // Use the first provider found in the service loader iterator.

        Iterator<HttpClientProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            defaultProvider = it.next();
        }
    }

    private HttpClientProviders() {
        // no-op
    }

    public static HttpClient createInstance() {
        if (defaultProvider == null) {
            throw new IllegalStateException(
                "Cannot find any HttpClient provider on the classpath - unable to create a default HttpClient instance");
        }

        return defaultProvider.createInstance();
    }
}
