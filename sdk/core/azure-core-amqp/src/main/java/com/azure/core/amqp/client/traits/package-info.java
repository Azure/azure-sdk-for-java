// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This package contains interfaces that represent common cross-cutting (and AMQP-related) aspects of functionality
 * offered by libraries in the Azure SDK for Java. Each interface is referred to as a 'trait', and classes that
 * implement the interface are said to have that trait. There are additional traits that are more general
 * purpose in the {@link com.azure.core.client.traits} package.
 *
 * <p>The particular focus of traits in the Azure SDK for Java is to enable higher-level
 * libraries the ability to more abstractly configure client libraries as part of their builders, prior to the client
 * itself being instantiated. By doing this, these high-level libraries are able to reason about functionality more
 * simply. It is important to appreciate that despite the availability of these cross-cutting traits, there is no
 * promise that configuration of each builder can simply be a matter of providing the same arguments for all builders!
 * Each builder must be configured appropriately for its requirements, or else runtime failures may occur when the
 * builder is asked to create the associated client.</p>
 *
 * @see com.azure.core.client.traits
 */
package com.azure.core.amqp.client.traits;
