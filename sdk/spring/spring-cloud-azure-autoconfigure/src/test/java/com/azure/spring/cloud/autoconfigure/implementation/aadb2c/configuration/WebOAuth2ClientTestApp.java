// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration;

import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SuppressWarnings({"removal", "deprecation"})
@EnableWebSecurity
@Import(org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class)
public class WebOAuth2ClientTestApp {

}
