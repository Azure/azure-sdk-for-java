/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatCode;

public class AADUserGroupsPropertyValidatorTest {

    private AADAuthenticationProperties aadAuthenticationProperties;

    @Before
    public void setUp() {
        aadAuthenticationProperties = new AADAuthenticationProperties();
    }

    @Test
    public void isValidNoGroupsDefined() {
        assertThatCode(() -> aadAuthenticationProperties.validateUserGroupProperties())
                .isInstanceOf(IllegalStateException.class).hasMessage(
                "One of the User Group Properties must be populated. "
                        + "Please populate azure.activedirectory.user-group.allowed-groups");
    }

    @Test
    public void isValidDeprecatedPropertySet() {
        aadAuthenticationProperties.setActiveDirectoryGroups(Collections.singletonList("user-group"));
        assertThatCode(() -> aadAuthenticationProperties.validateUserGroupProperties()).doesNotThrowAnyException();
    }

    @Test
    public void isValidUserGroupPropertySet() {
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(Collections.singletonList("user-group"));
        assertThatCode(() -> aadAuthenticationProperties.validateUserGroupProperties()).doesNotThrowAnyException();
    }

    @Test
    public void isValidBothUserGroupPropertiesSet() {
        aadAuthenticationProperties.setActiveDirectoryGroups(Collections.singletonList("user-group"));
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(Collections.singletonList("user-group"));
        assertThatCode(() -> aadAuthenticationProperties.validateUserGroupProperties()).doesNotThrowAnyException();
    }
}
