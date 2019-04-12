// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.utils;

/**
 * The class to contain the common factory methods required for SDK framework.
 */
public class SdkContext {
    private static ResourceNamerFactory resourceNamerFactory = new ResourceNamerFactory();

    /**
     * Function to override the ResourceNamerFactory.
     *
     * @param resourceNamerFactory factory to override.
     */
    public static void setResourceNamerFactory(ResourceNamerFactory resourceNamerFactory) {
        SdkContext.resourceNamerFactory = resourceNamerFactory;
    }

    /**
     * Gets the current factory for ResourceNamer.
     * @return resourceNamer factory.
     */
    public static ResourceNamerFactory getResourceNamerFactory() {
        return SdkContext.resourceNamerFactory;
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @return the random name
     */
    public static String randomResourceName(String prefix, int maxLen) {
        ResourceNamer resourceNamer = SdkContext.getResourceNamerFactory().createResourceNamer("");
        return resourceNamer.randomName(prefix, maxLen);
    }

    /**
     * Generates the specified number of random resource names with the same prefix.
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @param count the number of names to generate
     * @return random names
     */
    public static String[] randomResourceNames(String prefix, int maxLen, int count) {
        String[] names = new String[count];
        ResourceNamer resourceNamer = SdkContext.getResourceNamerFactory().createResourceNamer("");
        for (int i = 0; i < count; i++) {
            names[i] = resourceNamer.randomName(prefix, maxLen);
        }
        return names;
    }

    /**
     * Gets a random UUID.
     *
     * @return the random UUID.
     */
    public static String randomUuid() {
        ResourceNamer resourceNamer = SdkContext.getResourceNamerFactory().createResourceNamer("");
        return resourceNamer.randomUuid();
    }
}
