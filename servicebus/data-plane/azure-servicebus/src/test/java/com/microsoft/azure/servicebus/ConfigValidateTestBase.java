package com.microsoft.azure.servicebus;

import org.junit.Assume;
import org.junit.BeforeClass;

public class ConfigValidateTestBase {
    static final String RECORD_MODE = "RECORD";

    @BeforeClass
    public static void skipIfNotConfigured() {
        Assume.assumeTrue("The test only runs in Live mode.", ConfigValidateTestBase.getTestMode().equalsIgnoreCase(ConfigValidateTestBase.RECORD_MODE));
    }

    public static String getTestMode(){
        String testMode =  System.getenv("AZURE_TEST_MODE");
        if(testMode == null){
            testMode =  "PLAYBACK";
        }
        return testMode;
    }
}
