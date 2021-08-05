// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.filter.group.count;

import com.azure.spring.autoconfigure.aad.AADAuthenticationFilter;
import com.azure.spring.autoconfigure.aad.UserPrincipal;
import com.azure.spring.test.aad.AADWebApiITHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.azure.spring.test.Constant.MULTI_TENANT_SCOPE_GRAPH_READ;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADGroupsCountIT {

    private AADWebApiITHelper aadWebApiITHelper;

    @BeforeAll
    public void beforeAll() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.activedirectory.tenant-id", AAD_TENANT_ID_1);
        properties.put("azure.activedirectory.client-id", AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.client-secret", AAD_MULTI_TENANT_CLIENT_SECRET);
        properties.put("azure.activedirectory.user-group.allowed-groups", "group1,group2");
        aadWebApiITHelper = new AADWebApiITHelper(
            DumbApp.class,
            properties,
            AAD_MULTI_TENANT_CLIENT_ID,
            AAD_MULTI_TENANT_CLIENT_SECRET,
            Collections.singletonList(MULTI_TENANT_SCOPE_GRAPH_READ));
    }

    @Test
    public void testGroupsCount() {
        assertEquals("111", aadWebApiITHelper.httpGetStringByAccessToken("api/groupsCount"));
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        @Autowired
        private AADAuthenticationFilter aadAuthenticationFilter;

        @Override
        protected void configure(HttpSecurity http) {
            http.addFilterBefore(aadAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        @GetMapping(value = "api/groupsCount")
        public ResponseEntity<String> groupsCount(PreAuthenticatedAuthenticationToken authToken) {
            String groupsCount = Optional.of(authToken)
                                         .map(PreAuthenticatedAuthenticationToken::getPrincipal)
                                         .map(p -> (UserPrincipal) p)
                                         .map(UserPrincipal::getGroups)
                                         .map(Set::size)
                                         .map(String::valueOf)
                                         .orElse("");
            return new ResponseEntity<>(groupsCount, HttpStatus.OK);
        }
    }

}
