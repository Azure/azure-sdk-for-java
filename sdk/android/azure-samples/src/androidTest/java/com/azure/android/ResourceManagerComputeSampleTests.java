package com.azure.android;

import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.resourcemanager.compute.ReadmeSamplesResourceManagerCompute;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ResourceManagerComputeSampleTests {


    @Test
    public void testAuthenticate() {
        try {
            ReadmeSamplesResourceManagerCompute.authenticate();
        } catch (RuntimeException e) {
            fail();
        }
    }

}
