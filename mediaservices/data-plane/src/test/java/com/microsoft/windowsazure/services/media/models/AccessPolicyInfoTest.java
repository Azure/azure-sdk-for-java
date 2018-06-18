/**
 * Copyright Microsoft Corporation
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

import com.microsoft.windowsazure.services.media.implementation.content.AccessPolicyType;

public class AccessPolicyInfoTest {

    @Test
    public void getSetId() {
        String expected = "expectedId";

        AccessPolicyInfo policy = new AccessPolicyInfo(null,
                new AccessPolicyType().setId(expected));

        String actual = policy.getId();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSetCreated() {
        Date expected = new Date();
        AccessPolicyInfo policy = new AccessPolicyInfo(null,
                new AccessPolicyType().setCreated(expected));

        Date actual = policy.getCreated();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSetLastModified() {
        Date expected = new Date();
        AccessPolicyInfo policy = new AccessPolicyInfo(null,
                new AccessPolicyType().setLastModified(expected));

        Date actual = policy.getLastModified();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSetName() {
        String expected = "policy name goes here";
        AccessPolicyInfo policy = new AccessPolicyInfo(null,
                new AccessPolicyType().setName(expected));

        String actual = policy.getName();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSetDurationInMinutes() {
        double expected = 60; // arbitrary value
        AccessPolicyInfo policy = new AccessPolicyInfo(null,
                new AccessPolicyType().setDurationInMinutes(expected));

        double actual = policy.getDurationInMinutes();

        Assert.assertEquals(expected, actual, 0.0);
    }

    @Test
    public void getSetPermissions() {
        EnumSet<AccessPolicyPermission> expected = EnumSet.of(
                AccessPolicyPermission.LIST, AccessPolicyPermission.WRITE);
        AccessPolicyInfo policy = new AccessPolicyInfo(null,
                new AccessPolicyType().setPermissions(AccessPolicyPermission
                        .bitsFromPermissions(expected)));

        EnumSet<AccessPolicyPermission> actual = policy.getPermissions();

        Assert.assertEquals(expected, actual);
    }
}
