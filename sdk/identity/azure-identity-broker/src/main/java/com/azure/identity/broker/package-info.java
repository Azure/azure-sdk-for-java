// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>The Azure Identity Brokered Authentication library offers support for using various support brokers from the
 * MSAL4j library. Brokered authentication utilizes a secure component for token acquisition and management. This enables
 * many useful scenarios, such as using hardware authentication keys or accessing resources that require additional security
 * such as device bound tokens.</p>
 *
 * <p>Currently supported brokers:</p>
 * <ul>
 *     <li>Web Application Manager on Windows OS</li>
 * </ul>
 * <h2>Getting Started</h2>
 * <p>Brokered authentication support is offered through the {@link com.azure.identity.InteractiveBrowserCredential}.
 * To construct a credential which will use a broker, use the {@link com.azure.identity.broker.InteractiveBrowserBrokerCredentialBuilder}
 * from this package.</p>
 *
 * <!-- src_embed com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.construct -->
 * <pre>
 * InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder&#40;&#41;;
 * InteractiveBrowserCredential credential = builder.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.construct -->
 */
package com.azure.identity.broker;
