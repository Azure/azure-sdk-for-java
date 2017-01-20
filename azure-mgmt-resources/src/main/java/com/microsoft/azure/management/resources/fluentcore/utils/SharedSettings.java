/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

/**
 * The class to contain the common factory methods required for SDK framework.
 */
public class SharedSettings {
    private static ResourceNamerFactory resourceNamerFactory = new ResourceNamerFactory();

    /**
     * Function to override the ResourceNamerFactory.
     * @param resourceNamerFactory factory to override.
     */
    public static void setResourceNamerFactory(ResourceNamerFactory resourceNamerFactory) {
        SharedSettings.resourceNamerFactory = resourceNamerFactory;
    }

    /**
     * Gets the current factory for ResourceNamer.
     * @return resourceNamer factory.
     */
    public static ResourceNamerFactory getResourceNamerFactory() {
        return SharedSettings.resourceNamerFactory;
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @return the random name
     */
    public static String randomResourceName(String prefix, int maxLen) {
        ResourceNamer resourceNamer = SharedSettings.getResourceNamerFactory().createResourceNamer("");
        return resourceNamer.randomName(prefix, maxLen);
    }
}
