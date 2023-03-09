// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.clinicalmatching;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.polling.PollerFlux;
import com.azure.health.insights.clinicalmatching.models.TrialMatcherData;
import com.azure.health.insights.clinicalmatching.models.GeographicLocation;
import com.azure.health.insights.clinicalmatching.models.ClinicalTrials;
import com.azure.health.insights.clinicalmatching.models.ClinicalTrialSource;
import com.azure.health.insights.clinicalmatching.models.ClinicalTrialRegistryFilter;
import com.azure.health.insights.clinicalmatching.models.PatientInfoSex;
import com.azure.health.insights.clinicalmatching.models.ClinicalCodedElement;
import com.azure.health.insights.clinicalmatching.models.PatientInfo;
import com.azure.health.insights.clinicalmatching.models.PatientRecord;
import com.azure.health.insights.clinicalmatching.models.TrialMatcherModelConfiguration;
import com.azure.health.insights.clinicalmatching.models.TrialMatcherResults;
import com.azure.health.insights.clinicalmatching.models.TrialMatcherResult;
import com.azure.health.insights.clinicalmatching.models.ClinicalTrialStudyType;
import com.azure.health.insights.clinicalmatching.models.ClinicalTrialPhase;

/*
 *  This example demonstrates Finding potential eligible trials for a patient,
 *  based on patient’s structured medical information.
 *
 *  It looks for structured clinical trials that were taken from ClinicalTrials.gov
 *  Trial Matcher model matches a single patient to a set of relevant clinical trials,
 *  that this patient appears to be qualified for. This use case will demonstrate:
 *  a. How to use the trial matcher when patient clinical health information is provided to the
 *  Trial Matcher in a key-value structure with coded elements.
 *  b. How to use the clinical trial configuration to narrow down the trial condition,
 *  recruitment status, location and other criteria that the service users may choose to prioritize.
 */
public class SampleMatchTrialAsync {
    public static void runSample(String endpoint, String apiKey) throws InterruptedException, IOException {

        ClinicalMatchingAsyncClient asyncClient = new ClinicalMatchingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(AzureHealthInsightsServiceVersion.getLatest())
            .httpClient(HttpClient.createDefault(new HttpClientOptions()))
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        // construct Patient
        PatientRecord patient1 = new PatientRecord("patient_1");
        PatientInfo patientInfo = new PatientInfo();
        patientInfo.setBirthDate(LocalDate.parse("1965-12-26"));
        patientInfo.setSex(PatientInfoSex.MALE);
        final List<ClinicalCodedElement> clinicalInfo = new ArrayList<ClinicalCodedElement>();
        patientInfo.setClinicalInfo(clinicalInfo);
        patient1.setInfo(patientInfo);

        final String system = "http://www.nlm.nih.gov/research/umls";
        // Attach clinical info to the patient
        clinicalInfo.add(createClinicalCodedElement(system, "C0006826", "Malignant Neoplasms", "true"));
        clinicalInfo.add(createClinicalCodedElement(system, "C1522449", "Therapeutic radiology procedure", "true"));
        clinicalInfo.add(createClinicalCodedElement(system, "C1512162", "Eastern Cooperative Oncology Group", "1"));
        clinicalInfo.add(createClinicalCodedElement(system, "C0019693", "HIV Infections", "false"));
        clinicalInfo.add(createClinicalCodedElement(system, "C1300072", "Tumor stage", "2"));

        // Create registry filter
        ClinicalTrialRegistryFilter registryFilters = new ClinicalTrialRegistryFilter();
        // Limit the trial to a specific patient condition ("Non-small cell lung cancer")
        registryFilters.setConditions(Arrays.asList("Non-small cell lung cancer"));
        // Limit the clinical trial to a certain phase, phase 1
        registryFilters.setPhases(Arrays.asList(ClinicalTrialPhase.PHASE1));
        // Specify the clinical trial registry source as ClinicalTrials.Gov
        registryFilters.setSources(Arrays.asList(ClinicalTrialSource.CLINICALTRIALS_GOV));
        // Limit the clinical trial to a certain location, in this case California, USA
        registryFilters.setFacilityLocations(Arrays.asList(new GeographicLocation("United States")));
        // Limit the trial to a specific study type, interventional
        registryFilters.setStudyTypes(Arrays.asList(ClinicalTrialStudyType.INTERVENTIONAL));

        // Construct ClinicalTrial instance and attach the registry filter to it.
        ClinicalTrials clinicalTrials = new ClinicalTrials();
        clinicalTrials.setRegistryFilters(Arrays.asList(registryFilters));

        // Create TrialMatcherData
        TrialMatcherModelConfiguration configuration = new TrialMatcherModelConfiguration(clinicalTrials);
        TrialMatcherData trialMatcherData = new TrialMatcherData(Arrays.asList(patient1));
        trialMatcherData.setConfiguration(configuration);

        PollerFlux<TrialMatcherResult, TrialMatcherResult> asyncPoller = asyncClient.beginMatchTrials(trialMatcherData);

        TrialMatcherResult response = asyncPoller.blockLast().getValue();
        printResults(response);
    }

    private static void printResults(TrialMatcherResult tmRespone) {
        TrialMatcherResults tmResults = tmRespone.getResults();

        tmResults.getPatients().forEach(patientResult -> {
            System.out.println("Inferences of Patient " + patientResult.getId());
            patientResult.getInferences().forEach(inference -> {
                System.out.println("Trial Id " + inference.getId());
                System.out.println("Type: " + inference.getType() + " Value: " + inference.getValue());
                System.out.println("Description " + inference.getDescription());
            });
        });
    }

    private static ClinicalCodedElement createClinicalCodedElement(String system, String code, String name, String value) {
        ClinicalCodedElement element = new ClinicalCodedElement(system, code);
        element.setName(name);
        element.setValue(value);
        return element;
    }
}
