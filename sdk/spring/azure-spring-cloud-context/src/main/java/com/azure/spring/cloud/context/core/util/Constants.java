// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core.util;

import java.util.Optional;

/**
 * The User Agent version constants.
 */
public class Constants {

    public static final String VERSION = Optional.of(Constants.class)
        .map(Class::getPackage)
        .map(Package::getImplementationVersion)
        .orElse("unknown");

}
