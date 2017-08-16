package com.microsoft.rest.v2;

import org.junit.Test;

import static org.junit.Assert.*;

public class SwaggerInterfaceProxyDetailsTests {
    @Test
    public void getMethodProxyDetails() {
        final SwaggerInterfaceProxyDetails interfaceDetails = new SwaggerInterfaceProxyDetails();
        final SwaggerMethodProxyDetails methodDetails = interfaceDetails.getMethodProxyDetails("MockMethodName");
        assertNotNull(methodDetails);
        final SwaggerMethodProxyDetails methodDetails2 = interfaceDetails.getMethodProxyDetails("MockMethodName");
        assertSame(methodDetails, methodDetails2);
    }
}
