// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Authentication strategies for Azure Event Hubs Kafka support in Spring Cloud Azure.
 * 
 * <h2>Overview</h2>
 * This package contains authentication strategy implementations for configuring Kafka clients
 * to connect to Azure Event Hubs using different authentication methods.
 * 
 * <h2>Architecture</h2>
 * The authentication configuration uses the Strategy pattern to support different authentication methods:
 * <ul>
 *   <li>{@link com.azure.spring.cloud.autoconfigure.implementation.kafka.authentication.KafkaAuthenticationStrategy} - 
 *       The strategy interface that defines how authentication should be applied</li>
 *   <li>{@link com.azure.spring.cloud.autoconfigure.implementation.kafka.authentication.KafkaOAuth2AuthenticationStrategy} - 
 *       Implementation for OAuth2 authentication using Microsoft Entra ID</li>
 * </ul>
 * 
 * <h2>Supported Authentication Methods</h2>
 * 
 * <h3>OAuth2 Authentication (Microsoft Entra ID)</h3>
 * The {@code KafkaOAuth2AuthenticationStrategy} configures SASL/OAUTHBEARER authentication
 * for connecting to Azure Event Hubs using Microsoft Entra ID credentials.
 * 
 * <p><b>Configuration Requirements:</b></p>
 * <ul>
 *   <li>Bootstrap server must be an Event Hubs namespace endpoint (ends with :9093)</li>
 *   <li>Security protocol should be SASL_SSL (or not configured)</li>
 *   <li>SASL mechanism should be OAUTHBEARER (or not configured)</li>
 * </ul>
 * 
 * <p><b>Properties Configured:</b></p>
 * <ul>
 *   <li>{@code security.protocol} = SASL_SSL</li>
 *   <li>{@code sasl.mechanism} = OAUTHBEARER</li>
 *   <li>{@code sasl.jaas.config} = JAAS configuration with Azure credentials</li>
 *   <li>{@code sasl.login.callback.handler.class} = KafkaOAuth2AuthenticateCallbackHandler</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * The authentication strategies are used automatically by the Kafka bean post processors:
 * <ul>
 *   <li>{@code KafkaPropertiesBeanPostProcessor} - For Spring Boot Kafka auto-configuration</li>
 *   <li>{@code KafkaBinderConfigurationPropertiesBeanPostProcessor} - For Spring Cloud Stream Kafka binder</li>
 * </ul>
 * 
 * <h2>Example Configuration</h2>
 * <pre>{@code
 * spring.kafka.bootstrap-servers=mynamespace.servicebus.windows.net:9093
 * spring.cloud.azure.credential.client-id=<client-id>
 * spring.cloud.azure.credential.client-secret=<client-secret>
 * spring.cloud.azure.profile.tenant-id=<tenant-id>
 * }</pre>
 * 
 * @since 6.1.0
 */
package com.azure.spring.cloud.autoconfigure.implementation.kafka.authentication;
