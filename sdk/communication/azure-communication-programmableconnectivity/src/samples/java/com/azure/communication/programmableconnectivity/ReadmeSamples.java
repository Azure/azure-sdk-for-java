// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.DeviceLocationVerificationContent;
import com.azure.communication.programmableconnectivity.models.DeviceLocationVerificationResult;
import com.azure.communication.programmableconnectivity.models.LocationDevice;
import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.NetworkRetrievalResult;
import com.azure.communication.programmableconnectivity.models.NumberVerificationResult;
import com.azure.communication.programmableconnectivity.models.NumberVerificationWithCodeContent;
import com.azure.communication.programmableconnectivity.models.NumberVerificationWithoutCodeContent;
import com.azure.communication.programmableconnectivity.models.SimSwapRetrievalContent;
import com.azure.communication.programmableconnectivity.models.SimSwapRetrievalResult;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationContent;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationResult;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReadmeSamples {
    public void readmeSamples() {
        // BEGIN: com.azure.communication.programmableconnectivity.readme
        // Initialize the Programmable Connectivity client
        String endpoint = "https://your-resource-name.communication.azure.com";
        String gatewayId = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/your-resource-group/providers/Private.programmableconnectivity/gateways/your-gateway";
        
        // Example 1: Verify device location
        DeviceLocationClient deviceLocationClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildDeviceLocationClient();
        
        // Prepare the device information
        LocationDevice device = new LocationDevice().setPhoneNumber("+12065550100");
        
        // Create network identifier (usually NetworkCode, IPv4, or IPv6)
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "YourOperatorNetwork");
        
        // Create location verification content with coordinates
        DeviceLocationVerificationContent verificationContent = new DeviceLocationVerificationContent(
            networkId,    // Network identifier
            47.6062,      // Latitude
            -122.3321,    // Longitude
            50,           // Accuracy in meters
            device        // Device information
        );
        
        // Execute the verification request
        DeviceLocationVerificationResult locationResult = deviceLocationClient.verify(gatewayId, verificationContent);
        System.out.println("Location verification result: " + locationResult.isVerificationResult());
        
        // Example 2: Retrieve device network information
        DeviceNetworkClient deviceNetworkClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildDeviceNetworkClient();
        
        // Create a network identifier (IPv4, IPv6, or NetworkCode)
        NetworkIdentifier ipNetworkId = new NetworkIdentifier("IPv4", "203.0.113.45");
        
        // Retrieve network information
        NetworkRetrievalResult networkInfo = deviceNetworkClient.retrieve(gatewayId, ipNetworkId);
        
        // Process the network information
        if (networkInfo != null && networkInfo.getNetworkCode() != null) {
            System.out.println("Network code: " + networkInfo.getNetworkCode());
        }
        
        // Example 3: SIM Swap verification
        SimSwapClient simSwapClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildSimSwapClient();
            
        // Set up verification with phone number and max age (hours)
        SimSwapVerificationContent verifyContent = new SimSwapVerificationContent(networkId)
            .setPhoneNumber("12065550100")
            .setMaxAgeHours(120);
            
        // Execute verification request
        SimSwapVerificationResult verifyResult = simSwapClient.verify(gatewayId, verifyContent);
        System.out.println("SIM swap verification result: " + verifyResult.isVerificationResult());
        
        // Example 4: Retrieve SIM Swap information
        SimSwapRetrievalContent retrieveContent = new SimSwapRetrievalContent(networkId)
            .setPhoneNumber("12065550100");
            
        // Execute retrieval request
        SimSwapRetrievalResult swapResult = simSwapClient.retrieve(gatewayId, retrieveContent);
        
        // Process the SIM swap date information
        OffsetDateTime swapDate = swapResult.getDate();
        if (swapDate != null) {
            String formattedDate = swapDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            System.out.println("SIM swap date: " + formattedDate);
        } else {
            System.out.println("No SIM swap date available");
        }
        
        // Example 5: Number verification with authentication flow
        NumberVerificationClient numberVerificationClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildNumberVerificationClient();
            
        // Start the verification flow (this initiates a redirect to operator authentication)
        String redirectUri = "https://your-app.example.com/callback";
        NumberVerificationWithoutCodeContent initialContent = new NumberVerificationWithoutCodeContent(
            networkId, redirectUri).setPhoneNumber("12065550100");
            
        // Create options to prevent auto-following redirects
        RequestOptions requestOptions = new RequestOptions();
        
        // Make the initial request (in a real app, this would redirect to operator)
        Response<Void> initialResponse = numberVerificationClient.verifyWithoutCodeWithResponse(
            gatewayId, BinaryData.fromObject(initialContent), requestOptions);
            
        // After user authenticates with operator, your callback endpoint receives a code
        // You then complete verification using that code:
        String apcCode = "apc_received_from_operator"; // This would come from your callback handler
        
        // Final verification step with the code
        NumberVerificationWithCodeContent finalContent = new NumberVerificationWithCodeContent(apcCode);
        NumberVerificationResult numberResult = numberVerificationClient.verifyWithCode(gatewayId, finalContent);
        
        System.out.println("Number verification result: " + numberResult.isVerificationResult());
        // END: com.azure.communication.programmableconnectivity.readme
    }
    
}
