package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.utils.PropertyHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertyHelperTest {
    @Test
    void testGetSDKVersionNumber() {
        String sdkVersionNumber = PropertyHelper.getSdkVersionNumber();
        String regex = "[0-9].[0-9].[0-9].*";
        assertTrue(sdkVersionNumber.matches(regex));
    }

    @Test
    void testGetQualifiedSDKVersionString() {
        String sdkVersionString = PropertyHelper.getQualifiedSdkVersionString();
        String regex = "java[0-9].?[0-9]:ot.*:ext.*";
        assertTrue(sdkVersionString.matches(regex));
    }
}
