// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.resource.server;

import com.azure.spring.test.aad.AADWebApiITHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.Constant.MULTI_TENANT_SCOPE_GRAPH_READ;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static org.junit.Assert.assertEquals;

public class AADWeiResourceServerIT {

    private AADWebApiITHelper aadWebApiITHelper;

    @Before
    public void init() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.activedirectory.client-id", AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.client-secret", AAD_MULTI_TENANT_CLIENT_SECRET);
        properties.put("azure.activedirectory.app-id-uri", "api://" + AAD_MULTI_TENANT_CLIENT_ID);
        aadWebApiITHelper = new AADWebApiITHelper(
            DumbApp.class,
            properties,
            AAD_MULTI_TENANT_CLIENT_ID,
            AAD_MULTI_TENANT_CLIENT_SECRET,
            Collections.singletonList(MULTI_TENANT_SCOPE_GRAPH_READ));
    }

    @Test
    public void testHasScope() {
        assertEquals(aadWebApiITHelper.httpGetStringByAccessToken("graph"), "graph");
    }

    @Test(expected = HttpClientErrorException.class)
    public void testHasNoScope() {
        aadWebApiITHelper.httpGetStringByAccessToken("notExist");
    }

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        }

        @GetMapping("graph")
        @PreAuthorize("hasAuthority('SCOPE_ResourceAccessGraph.Read')")
        public String graph() {
            return "graph";
        }

        @GetMapping("notExist")
        @PreAuthorize("hasAuthority('SCOPE_NotExist')")
        public String notExist() {
            return "notExist";
        }
    }
}
