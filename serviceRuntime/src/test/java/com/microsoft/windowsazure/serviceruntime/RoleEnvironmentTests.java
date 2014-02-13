/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;

// import org.junit.Test;

/**
 *
 */
public class RoleEnvironmentTests {
    @Before
    public void setupTests() {
        horrificEnvironmentModification("\\\\.\\pipe\\578cb0d1-a330-4019-b634-755aa3d1e9d2");
    }

    // @Test
    public void roleEnvironmentIsAvailable() {
        assertThat(RoleEnvironment.isAvailable(), is(true));
    }

    // @Test
    public void roleEnvironmentSetStateSetsState() {
        Calendar exp = Calendar.getInstance(TimeZone.getTimeZone("GMT+0:00"));

        exp.add(Calendar.MINUTE, 1);

        RoleEnvironment.setStatus(RoleInstanceStatus.Ready, exp.getTime());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void horrificEnvironmentModification(String endpoint) {
        try {
            Class processEnvironmentClass = Class
                    .forName("java.lang.ProcessEnvironment");
            Field field = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            field.setAccessible(true);
            Object obj = field.get(null);
            Map<String, String> map = (Map<String, String>) obj;
            map.put("WaRuntimeEndpoint", endpoint);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
