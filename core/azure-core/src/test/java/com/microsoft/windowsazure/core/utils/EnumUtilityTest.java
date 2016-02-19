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

package com.microsoft.windowsazure.core.utils;

import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EnumUtilityTest {
    private enum Animal {
        Cat,
        Dog
    }

    @Test
    public void EnumUtilityShouldParseCaseInsensitively()
            throws Exception {
        String cat = "cat";
        String dog = "dOG";
        Assert.assertEquals(Animal.Cat, EnumUtility.fromString(Animal.class, cat));
        Assert.assertEquals(Animal.Dog, EnumUtility.fromString(Animal.class, dog));
    }

    @Test
    public void EnumUtilityShouldThrowOnBadString()
            throws Exception {
        String cat = "cag";
        try {
            EnumUtility.fromString(Animal.class, cat);
            fail();
        } catch (IllegalArgumentException ex) { }
    }

    @Test
    public void EnumUtilityShouldParseInterger() throws Exception {
        String cat = "0";
        String dog = "1";
        Assert.assertEquals(Animal.Cat, EnumUtility.fromString(Animal.class, cat));
        Assert.assertEquals(Animal.Dog, EnumUtility.fromString(Animal.class, dog));
    }
}
