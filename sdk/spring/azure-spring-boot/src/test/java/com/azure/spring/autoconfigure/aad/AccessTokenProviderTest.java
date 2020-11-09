// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;


@RunWith(MockitoJUnitRunner.class)
public class AccessTokenProviderTest {

    @Autowired
    AccessTokenProvider accessTokenProvider;

    @Autowired
    AADAuthenticationProperties aadAuthenticationProperties;

    Set<String> set = new HashSet<>();

    String accessToken;

    @Before
    public void setup() {
        accessTokenProvider = new AccessTokenProvider();
    }

    @Test
    public void test() throws Throwable {

        set.add("api://b91ad630-2d3d-454a-86a7-7ac57299940e/access_as_user");

        accessToken = "enter accessToken string from api a";

        String str = accessTokenProvider.acquireTokenByOboflow(set, accessToken);

        System.out.println(str);
    }

}
