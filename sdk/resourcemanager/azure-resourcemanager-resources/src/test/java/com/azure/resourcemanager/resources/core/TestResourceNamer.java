// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.core;

import com.azure.resourcemanager.resources.fluentcore.utils.ResourceNamer;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

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
            try {
                return interceptorManager.popVariable();
            } catch (NoSuchElementException e) {
                return super.randomName(prefix, maxLen);
            }
        }
        String randomName = super.randomName(prefix, maxLen);

        interceptorManager.pushVariable(randomName);

        return randomName;
    }

    @Override
    public String randomUuid() {
        if (interceptorManager.isPlaybackMode()) {
            try {
                return interceptorManager.popVariable();
            } catch (NoSuchElementException e) {
                return super.randomUuid();
            }
        }
        String randomName = super.randomUuid();

        interceptorManager.pushVariable(randomName);

        return randomName;
    }

    @Override
    public OffsetDateTime dateTimeNow() {
        if (interceptorManager.isPlaybackMode()) {
            try {
                return OffsetDateTime.parse(interceptorManager.popVariable());
            } catch (NoSuchElementException e) {
                return super.dateTimeNow();
            }
        }
        OffsetDateTime dateTime = super.dateTimeNow();
        interceptorManager.pushVariable(dateTime.toString());

        return dateTime;
    }
}
