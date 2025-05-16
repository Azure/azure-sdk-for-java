package com.azure.communication.programmableconnectivity.generated;

import com.azure.communication.programmableconnectivity.ProgrammableConnectivityClientBuilder;
import com.azure.communication.programmableconnectivity.SimSwapClient;
import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationContent;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationResult;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.AzurePowerShellCredentialBuilder;

public class SimSwapVerifyDebug {
    public static void main(String[] args) {
        System.out.println("Starting SimSwap verification test");
        System.out.println("Using endpoint: " + Configuration.getGlobalConfiguration().get("ENDPOINT"));
        
        try {
            // Build client with detailed logging
            SimSwapClient simSwapClient = new ProgrammableConnectivityClientBuilder()
                .credential(new AzurePowerShellCredentialBuilder().build())
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildSimSwapClient();

            // Gateway ID
            String gatewayId = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505081537";
            System.out.println("Using gateway ID: " + gatewayId);
            
            // Create verification content
            SimSwapVerificationContent content = new SimSwapVerificationContent(
                new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso"))
                .setPhoneNumber("10000100")
                .setMaxAgeHours(941);
                
            System.out.println("Sending SimSwap verification request...");
            
            // Make the API call
            SimSwapVerificationResult response = simSwapClient.verify(gatewayId, content);
            
            // Process result
            System.out.println("SimSwap verification successful!");
            System.out.println("Verification result: " + response.isVerificationResult());
            
        } catch (Exception e) {
            System.err.println("Error during SimSwap verification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
