// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.DeviceLocationVerificationContent;
import com.azure.communication.programmableconnectivity.models.DeviceLocationVerificationResult;
import com.azure.communication.programmableconnectivity.models.LocationDevice;
import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class LocationVerifyTest extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    /**
     * This test verifies that the API correctly processes the location verification request.
     */
    @Test
    public void testLocationVerify() {
        System.out.println("Starting Location Verify test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505201109";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");

        LocationDevice device = new LocationDevice().setPhoneNumber("+8000000000000");

        DeviceLocationVerificationContent content = new DeviceLocationVerificationContent(networkId,   // Network identifier
            80.0,        // Latitude
            85.0,        // Longitude
            50,          // Accuracy in meters
            device       // Device information
        );

        System.out.println("Request parameters:");
        System.out.println("- Gateway ID: " + gatewayId);
        System.out.println("- Network Type: " + networkId.getIdentifierType());
        System.out.println("- Network Value: " + networkId.getIdentifier());
        System.out.println("- Latitude: 80.0, Longitude: 85.0, Accuracy: 50 meters");
        System.out.println("- Phone Number: " + device.getPhoneNumber());

        DeviceLocationVerificationResult result = deviceLocationClient.verify(gatewayId, content);

        System.out.println("Verification result: " + result.isVerificationResult());

        Assertions.assertNotNull(result, "Verification result should not be null");
        Assertions.assertFalse(result.isVerificationResult(),
            "Expected the verification result to be false for these coordinates");

        System.out.println("Test completed successfully.");
    }
}
