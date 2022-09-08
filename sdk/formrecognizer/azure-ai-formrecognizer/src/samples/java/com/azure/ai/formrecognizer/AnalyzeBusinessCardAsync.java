// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous sample for analyzing business card information from a document given through a file.
 */
public class AnalyzeBusinessCardAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisAsyncClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        File businessCard = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
            + "sample-forms/businessCards/businessCard.jpg");
        byte[] fileContent = Files.readAllBytes(businessCard.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        PollerFlux<OperationResult, AnalyzeResult> analyzeBusinessCardPoller =
            client.beginAnalyzeDocument("prebuilt-businessCard",
                BinaryData.fromStream(targetStream),
                new AnalyzeDocumentOptions().setPages(Arrays.asList("1")).setLocale("en-US"));

        Mono<AnalyzeResult> businessCardPageResultsMono
            = analyzeBusinessCardPoller
            .last()
            .flatMap(pollResponse -> {
                if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(pollResponse.getStatus())) {
                    System.out.println("Polling completed successfully");
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        businessCardPageResultsMono.subscribe(businessCardPageResults -> {
            for (int i = 0; i < businessCardPageResults.getDocuments().size(); i++) {
                System.out.printf("--------Analyzing business card %d%n--------", i);
                AnalyzedDocument analyzedBusinessCard = businessCardPageResults.getDocuments().get(i);

                Map<String, DocumentField> businessCardFields = analyzedBusinessCard.getFields();
                DocumentField contactNamesDocumentField = businessCardFields.get("ContactNames");
                if (contactNamesDocumentField != null) {
                    if (DocumentFieldType.LIST == contactNamesDocumentField.getType()) {
                        List<DocumentField> contactNamesList = contactNamesDocumentField.getValueAsList();
                        contactNamesList.stream()
                            .filter(contactName -> DocumentFieldType.MAP == contactName.getType())
                            .map(contactName -> {
                                System.out.printf("Contact name: %s%n", contactName.getContent());
                                return contactName.getValueAsMap();
                            })
                            .forEach(contactNamesMap -> contactNamesMap.forEach((key, contactName) -> {
                                if ("FirstName".equals(key)) {
                                    if (DocumentFieldType.STRING == contactName.getType()) {
                                        String firstName = contactName.getValueAsString();
                                        System.out.printf("\tFirst Name: %s, confidence: %.2f%n",
                                            firstName, contactName.getConfidence());
                                    }
                                }
                                if ("LastName".equals(key)) {
                                    if (DocumentFieldType.STRING == contactName.getType()) {
                                        String lastName = contactName.getValueAsString();
                                        System.out.printf("\tLast Name: %s, confidence: %.2f%n",
                                            lastName, contactName.getConfidence());
                                    }
                                }
                            }));
                    }
                }

                DocumentField jobTitles = businessCardFields.get("JobTitles");
                if (jobTitles != null) {
                    if (DocumentFieldType.LIST == jobTitles.getType()) {
                        List<DocumentField> jobTitlesItems = jobTitles.getValueAsList();
                        jobTitlesItems.forEach(jobTitlesItem -> {
                            if (DocumentFieldType.STRING == jobTitlesItem.getType()) {
                                String jobTitle = jobTitlesItem.getValueAsString();
                                System.out.printf("Job Title: %s, confidence: %.2f%n",
                                    jobTitle, jobTitlesItem.getConfidence());
                            }
                        });
                    }
                }

                DocumentField departments = businessCardFields.get("Departments");
                if (departments != null) {
                    if (DocumentFieldType.LIST == departments.getType()) {
                        List<DocumentField> departmentsItems = departments.getValueAsList();
                        departmentsItems.forEach(departmentsItem -> {
                            if (DocumentFieldType.STRING == departmentsItem.getType()) {
                                String department = departmentsItem.getValueAsString();
                                System.out.printf("Department: %s, confidence: %.2f%n",
                                    department, departmentsItem.getConfidence());
                            }
                        });
                    }
                }

                DocumentField emails = businessCardFields.get("Emails");
                if (emails != null) {
                    if (DocumentFieldType.LIST == emails.getType()) {
                        List<DocumentField> emailsItems = emails.getValueAsList();
                        emailsItems.forEach(emailsItem -> {
                            if (DocumentFieldType.STRING == emailsItem.getType()) {
                                String email = emailsItem.getValueAsString();
                                System.out.printf("Email: %s, confidence: %.2f%n", email, emailsItem.getConfidence());
                            }
                        });
                    }
                }

                DocumentField websites = businessCardFields.get("Websites");
                if (websites != null) {
                    if (DocumentFieldType.LIST == websites.getType()) {
                        List<DocumentField> websitesItems = websites.getValueAsList();
                        websitesItems.forEach(websitesItem -> {
                            if (DocumentFieldType.STRING == websitesItem.getType()) {
                                String website = websitesItem.getValueAsString();
                                System.out.printf("Web site: %s, confidence: %.2f%n",
                                    website, websitesItem.getConfidence());
                            }
                        });
                    }
                }

                DocumentField mobilePhones = businessCardFields.get("MobilePhones");
                if (mobilePhones != null) {
                    if (DocumentFieldType.LIST == mobilePhones.getType()) {
                        List<DocumentField> mobilePhonesItems = mobilePhones.getValueAsList();
                        mobilePhonesItems.forEach(mobilePhonesItem -> {
                            if (DocumentFieldType.PHONE_NUMBER == mobilePhonesItem.getType()) {
                                String mobilePhoneNumber = mobilePhonesItem.getValueAsPhoneNumber();
                                System.out.printf("Mobile phone number: %s, confidence: %.2f%n",
                                    mobilePhoneNumber, mobilePhonesItem.getConfidence());
                            }
                        });
                    }
                }

                DocumentField otherPhones = businessCardFields.get("OtherPhones");
                if (otherPhones != null) {
                    if (DocumentFieldType.LIST == otherPhones.getType()) {
                        List<DocumentField> otherPhonesItems = otherPhones.getValueAsList();
                        otherPhonesItems.forEach(otherPhonesItem -> {
                            if (DocumentFieldType.PHONE_NUMBER == otherPhonesItem.getType()) {
                                String otherPhoneNumber = otherPhonesItem.getValueAsPhoneNumber();
                                System.out.printf("Other phone number: %s, confidence: %.2f%n",
                                    otherPhoneNumber, otherPhonesItem.getConfidence());
                            }
                        });
                    }
                }

                DocumentField faxes = businessCardFields.get("Faxes");
                if (faxes != null) {
                    if (DocumentFieldType.LIST == faxes.getType()) {
                        List<DocumentField> faxesItems = faxes.getValueAsList();
                        faxesItems.forEach(faxesItem -> {
                            if (DocumentFieldType.PHONE_NUMBER == faxesItem.getType()) {
                                String faxPhoneNumber = faxesItem.getValueAsPhoneNumber();
                                System.out.printf("Fax phone number: %s, confidence: %.2f%n",
                                    faxPhoneNumber, faxesItem.getConfidence());
                            }
                        });
                    }
                }

                DocumentField addresses = businessCardFields.get("Addresses");
                if (addresses != null) {
                    if (DocumentFieldType.LIST == addresses.getType()) {
                        List<DocumentField> addressesItems = addresses.getValueAsList();
                        addressesItems.forEach(addressesItem -> {
                            if (DocumentFieldType.STRING == addressesItem.getType()) {
                                String address = addressesItem.getValueAsString();
                                System.out
                                    .printf("Address: %s, confidence: %.2f%n", address, addressesItem.getConfidence());
                            }
                        });
                    }
                }

                DocumentField companyName = businessCardFields.get("CompanyNames");
                if (companyName != null) {
                    if (DocumentFieldType.LIST == companyName.getType()) {
                        List<DocumentField> companyNameItems = companyName.getValueAsList();
                        companyNameItems.forEach(companyNameItem -> {
                            if (DocumentFieldType.STRING == companyNameItem.getType()) {
                                String companyNameValue = companyNameItem.getValueAsString();
                                System.out.printf("Company name: %s, confidence: %.2f%n", companyNameValue,
                                    companyNameItem.getConfidence());
                            }
                        });
                    }
                }
            }
        });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
