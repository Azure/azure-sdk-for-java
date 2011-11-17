/**
 * 
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

    //@Test
    public void roleEnvironmentIsAvailable() {
        assertThat(RoleEnvironment.isAvailable(), is(true));
    }

    //@Test
    public void roleEnvironmentSetStateSetsState() {
        Calendar exp = Calendar.getInstance(TimeZone.getTimeZone("GMT+0:00"));

        exp.add(Calendar.MINUTE, 1);

        RoleEnvironment.setStatus(RoleInstanceStatus.Ready, exp.getTime());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void horrificEnvironmentModification(String endpoint) {
        try {
            Class processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field field = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            field.setAccessible(true);
            Object obj = field.get(null);
            Map<String, String> map = (Map<String, String>) obj;
            map.put("WaRuntimeEndpoint", endpoint);
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
