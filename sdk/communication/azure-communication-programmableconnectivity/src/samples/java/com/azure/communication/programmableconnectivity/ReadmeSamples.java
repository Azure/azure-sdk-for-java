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
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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
        
        // Example 1 (Async): Verify device location asynchronously
        DeviceLocationAsyncClient deviceLocationAsyncClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildDeviceLocationAsyncClient();
            
        // Execute the verification request asynchronously
        deviceLocationAsyncClient.verify(gatewayId, verificationContent)
            .subscribe(asyncResult -> {
                System.out.println("Async location verification result: " + asyncResult.isVerificationResult());
            }, error -> {
                System.err.println("Async location verification failed: " + error.getMessage());
            });
        
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
        
        // Example 2 (Async): Retrieve device network information asynchronously
        DeviceNetworkAsyncClient deviceNetworkAsyncClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildDeviceNetworkAsyncClient();
            
        // Retrieve network information asynchronously
        deviceNetworkAsyncClient.retrieve(gatewayId, ipNetworkId)
            .subscribe(asyncNetworkInfo -> {
                if (asyncNetworkInfo != null && asyncNetworkInfo.getNetworkCode() != null) {
                    System.out.println("Async network code: " + asyncNetworkInfo.getNetworkCode());
                }
            }, error -> {
                System.err.println("Async network retrieval failed: " + error.getMessage());
            });
        
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
        
        // Example 3 (Async): SIM Swap verification asynchronously
        SimSwapAsyncClient simSwapAsyncClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildSimSwapAsyncClient();
            
        // Execute verification request asynchronously
        simSwapAsyncClient.verify(gatewayId, verifyContent)
            .subscribe(asyncVerifyResult -> {
                System.out.println("Async SIM swap verification result: " + asyncVerifyResult.isVerificationResult());
            }, error -> {
                System.err.println("Async SIM swap verification failed: " + error.getMessage());
            });
        
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
        
        // Example 4 (Async): Retrieve SIM Swap information asynchronously
        simSwapAsyncClient.retrieve(gatewayId, retrieveContent)
            .subscribe(asyncSwapResult -> {
                // Process the SIM swap date information
                OffsetDateTime asyncSwapDate = asyncSwapResult.getDate();
                if (asyncSwapDate != null) {
                    String formattedDate = asyncSwapDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    System.out.println("Async SIM swap date: " + formattedDate);
                } else {
                    System.out.println("Async - No SIM swap date available");
                }
            }, error -> {
                System.err.println("Async SIM swap retrieval failed: " + error.getMessage());
            });
        
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
        
        // Example 5 (Async): Number verification with authentication flow asynchronously
        NumberVerificationAsyncClient numberVerificationAsyncClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildNumberVerificationAsyncClient();
            
        // Make the initial async request (in a real app, this would redirect to operator)
        Mono<Response<Void>> initialResponseMono = numberVerificationAsyncClient.verifyWithoutCodeWithResponse(
            gatewayId, BinaryData.fromObject(initialContent), requestOptions);
            
        initialResponseMono.subscribe(asyncInitialResponse -> {
            System.out.println("Async initial response status: " + asyncInitialResponse.getStatusCode());
            
            // In a real application, you would handle the redirect and auth callback flow
            // Then, once you have the APC code from the callback:
            
            // Example of the final verification step (would be in a separate method/callback in real app)
            String asyncApcCode = "apc_received_from_operator"; // This would come from your callback handler
            NumberVerificationWithCodeContent asyncFinalContent = new NumberVerificationWithCodeContent(asyncApcCode);
            
            numberVerificationAsyncClient.verifyWithCode(gatewayId, asyncFinalContent)
                .subscribe(asyncNumberResult -> {
                    System.out.println("Async number verification result: " + asyncNumberResult.isVerificationResult());
                }, error -> {
                    System.err.println("Async final verification failed: " + error.getMessage());
                });
        }, error -> {
            System.err.println("Async initial verification failed: " + error.getMessage());
        });
        
        // Example of using block() for testing or synchronous code paths (not recommended for production)
        // This is just to show how you might use async clients in a synchronous context if needed
        try {
            NumberVerificationResult blockingResult = numberVerificationAsyncClient
                .verifyWithCode(gatewayId, finalContent)
                .block(); // Blocks until the operation completes - only for demonstration
            
            System.out.println("Blocking result from async client: " + blockingResult.isVerificationResult());
        } catch (Exception e) {
            System.err.println("Error in blocking operation: " + e.getMessage());
        }
        // END: com.azure.communication.programmableconnectivity.readme
    }
    
    /**
     * Alternative example showing composable reactive operations.
     */
    public void asyncWorkflowExample() {
        // BEGIN: com.azure.communication.programmableconnectivity.asyncworkflow
        String endpoint = "https://your-resource-name.communication.azure.com";
        String gatewayId = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/your-resource-group/providers/Private.programmableconnectivity/gateways/your-gateway";
        
        // Create network identifier and content
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "YourOperatorNetwork");
        String phoneNumber = "12065550100";
        
        // Initialize the async client
        SimSwapAsyncClient simSwapAsyncClient = new ProgrammableConnectivityClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildSimSwapAsyncClient();
        
        // Example of composing multiple operations using reactive patterns
        SimSwapVerificationContent verifyContent = new SimSwapVerificationContent(networkId)
            .setPhoneNumber(phoneNumber)
            .setMaxAgeHours(120);
            
        SimSwapRetrievalContent retrieveContent = new SimSwapRetrievalContent(networkId)
            .setPhoneNumber(phoneNumber);
            
        // Compose operations - first verify SIM swap status, then if true, get the swap date
        simSwapAsyncClient.verify(gatewayId, verifyContent)
            .flatMap(verifyResult -> {
                System.out.println("SIM swap verification: " + verifyResult.isVerificationResult());
                
                if (verifyResult.isVerificationResult()) {
                    // If verification passes, retrieve the swap details
                    return simSwapAsyncClient.retrieve(gatewayId, retrieveContent);
                } else {
                    // If verification fails, don't proceed with retrieval
                    return Mono.empty();
                }
            })
            .subscribe(retrieveResult -> {
                // This is only called if verification passed and retrieval succeeded
                if (retrieveResult != null && retrieveResult.getDate() != null) {
                    String formattedDate = retrieveResult.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    System.out.println("SIM was swapped on: " + formattedDate);
                }
            }, error -> {
                System.err.println("Workflow failed: " + error.getMessage());
            });
        // END: com.azure.communication.programmableconnectivity.asyncworkflow
    }
}
