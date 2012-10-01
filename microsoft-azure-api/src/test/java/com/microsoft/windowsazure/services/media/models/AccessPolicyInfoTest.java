/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

public class AccessPolicyInfoTest {

    @Test
    public void getSetId() {
        AccessPolicyInfo policy = new AccessPolicyInfo();
        String expected = "expectedId";

        String actual = policy.setId(expected).getId();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSetCreated() {
        AccessPolicyInfo policy = new AccessPolicyInfo();
        Date expected = new Date();

        Date actual = policy.setCreated(expected).getCreated();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSetLastModified() {
        AccessPolicyInfo policy = new AccessPolicyInfo();
        Date expected = new Date();

        Date actual = policy.setLastModified(expected).getLastModified();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSetName() {
        AccessPolicyInfo policy = new AccessPolicyInfo();
        String expected = "policy name goes here";

        String actual = policy.setName(expected).getName();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSetDurationInMinutes() {
        AccessPolicyInfo policy = new AccessPolicyInfo();
        double expected = 60; // arbitrary value

        double actual = policy.setDurationInMinutes(expected).getDurationInMinutes();

        Assert.assertEquals(expected, actual, 0.0);
    }

    @Test
    public void getSetPermissions() {
        AccessPolicyInfo policy = new AccessPolicyInfo();
        EnumSet<AccessPolicyPermission> expected = EnumSet
                .of(AccessPolicyPermission.LIST, AccessPolicyPermission.WRITE);

        EnumSet<AccessPolicyPermission> actual = policy.setPermissions(expected).getPermissions();

        Assert.assertEquals(expected, actual);
    }
}
