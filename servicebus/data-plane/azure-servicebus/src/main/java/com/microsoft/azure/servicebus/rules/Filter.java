// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus.rules;

/**
 * Represents a filter expression that is evaluated against a message on a topic. This client library provides support for creating only limited types of filters.
 * This is an empty interface to serve as root interface for all supported filter types.
 *
 * @since 1.0
 */
public abstract class Filter {
    // No methods. Just a skeleton root class for filters
    // Filter execution happens in the cloud on .net runtime. There is no point implementing custom filters in Java.
}
