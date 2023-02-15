 // Copyright (c) Microsoft Corporation. All rights reserved.
 // Licensed under the MIT License.

 package com.azure.verticals.agrifood.farming;

 import com.azure.core.http.HttpClient;
 import com.azure.identity.DefaultAzureCredentialBuilder;

 /**
  * Code samples for the README.md
  */
 public class ReadmeSamples {
     /**
      * Sample for creating low level client.
      */
     public void createClient() {
         // BEGIN: readme-sample-createPartiesClient
         PartiesClientBuilder builder =
            new PartiesClientBuilder()
                .host("https://bb-prod-wcus-1.farmbeats.azure.net")
                .httpClient(HttpClient.createDefault());
         PartiesClient partiesClient = builder.buildClient();
         // END: readme-sample-createPartiesClient
     }
 }
