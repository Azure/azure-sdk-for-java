// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.b2c;

import com.azure.spring.autoconfigure.b2c.AADB2COidcLoginConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the AADB2COidcLoginConfigurer in README.md
 */
@EnableWebSecurity
public class AADB2COidcLoginConfigSample extends WebSecurityConfigurerAdapter {

    private final AADB2COidcLoginConfigurer configurer;

    public AADB2COidcLoginConfigSample(AADB2COidcLoginConfigurer configurer) {
        this.configurer = configurer;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()
                .and()
            .apply(configurer);
        // @formatter:on
    }
}
