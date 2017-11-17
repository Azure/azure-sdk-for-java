/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;

public class TestResourceNamer extends ResourceNamer {
    private final InterceptorManager interceptorManager;

    public TestResourceNamer(String name, InterceptorManager interceptorManager) {
        super(name);
        this.interceptorManager = interceptorManager;
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the random generated name
     * @return the random name
     */
    @Override
    public String randomName(String prefix, int maxLen) {
        if (interceptorManager.isPlaybackMode()) {
            return interceptorManager.popVariable();
        }
        String randomName = super.randomName(prefix, maxLen);

        interceptorManager.pushVariable(randomName);

        return randomName;
    }

    @Override
    public String randomUuid() {
        if (interceptorManager.isPlaybackMode()) {
            return interceptorManager.popVariable();
        }
        String randomName = super.randomUuid();

        interceptorManager.pushVariable(randomName);

        return randomName;
    }
}
