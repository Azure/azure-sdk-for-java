// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

/**
 * Sample to demonstrate listing of root causes for an incident.
 */
public class ListIncidentRootCausesSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        // List root causes for an incident under a detection configuration Id.
        //   - The id of the DetectionConfiguration resource.
        final String detectionConfigurationId = "tyu3dvhkl-bbbb-41ec-a637-78877b813gt";
        //   - The id of the AnomalyIncident resource.
        final String incidentId = "yufrjo3bs-jjjj-41ec-kldn-opn67d2bs";

        advisorClient.listIncidentRootCauses(detectionConfigurationId, incidentId)
            .forEach(incidentRootCause -> {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.printf("Series Key: %s%n", incidentRootCause.getSeriesKey());
                System.out.println(incidentRootCause.getSeriesKey().asMap());
                System.out.printf("Confidence for the detected incident root cause : %.2f%n",
                    incidentRootCause.getContributionScore());
                System.out.println("Trace the path for the incident root cause:");
                incidentRootCause.getPaths().forEach(System.out::println);
            });
    }
}
