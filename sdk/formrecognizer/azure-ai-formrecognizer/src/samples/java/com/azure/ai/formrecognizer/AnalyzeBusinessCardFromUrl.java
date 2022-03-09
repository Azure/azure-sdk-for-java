// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;
import java.util.Map;

/**
 * Sample for analyzing business card information from a URL.
 */
public class AnalyzeBusinessCardFromUrl {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String businessCardUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
                + "/azure-ai-formrecognizer/src/samples/resources/sample-forms/businessCards/businessCard.jpg";

        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeBusinessCardPoller =
            client.beginAnalyzeDocumentFromUrl("prebuilt-businessCard", businessCardUrl);

        AnalyzeResult businessCardPageResults = analyzeBusinessCardPoller.getFinalResult();

        for (int i = 0; i < businessCardPageResults.getDocuments().size(); i++) {
            System.out.printf("--------Analyzing business card %d%n--------", i);
            AnalyzedDocument analyzedBusinessCard = businessCardPageResults.getDocuments().get(i);
            Map<String, DocumentField> businessCardFields = analyzedBusinessCard.getFields();
            DocumentField contactNamesDocumentField = businessCardFields.get("ContactNames");
            if (contactNamesDocumentField != null) {
                if (DocumentFieldType.LIST == contactNamesDocumentField.getType()) {
                    List<DocumentField> contactNamesList = contactNamesDocumentField.getValueList();
                    contactNamesList.stream()
                        .filter(contactName -> DocumentFieldType.MAP == contactName.getType())
                        .map(contactName -> {
                            System.out.printf("Contact name: %s%n", contactName.getContent());
                            return contactName.getValueMap();
                        })
                        .forEach(contactNamesMap -> contactNamesMap.forEach((key, contactName) -> {
                            if ("FirstName".equals(key)) {
                                if (DocumentFieldType.STRING == contactName.getType()) {
                                    String firstName = contactName.getValueString();
                                    System.out.printf("\tFirst Name: %s, confidence: %.2f%n",
                                        firstName, contactName.getConfidence());
                                }
                            }
                            if ("LastName".equals(key)) {
                                if (DocumentFieldType.STRING == contactName.getType()) {
                                    String lastName = contactName.getValueString();
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
                    List<DocumentField> jobTitlesItems = jobTitles.getValueList();
                    jobTitlesItems.forEach(jobTitlesItem -> {
                        if (DocumentFieldType.STRING == jobTitlesItem.getType()) {
                            String jobTitle = jobTitlesItem.getValueString();
                            System.out.printf("Job Title: %s, confidence: %.2f%n",
                                jobTitle, jobTitlesItem.getConfidence());
                        }
                    });
                }
            }

            DocumentField departments = businessCardFields.get("Departments");
            if (departments != null) {
                if (DocumentFieldType.LIST == departments.getType()) {
                    List<DocumentField> departmentsItems = departments.getValueList();
                    departmentsItems.forEach(departmentsItem -> {
                        if (DocumentFieldType.STRING == departmentsItem.getType()) {
                            String department = departmentsItem.getValueString();
                            System.out.printf("Department: %s, confidence: %.2f%n",
                                department, departmentsItem.getConfidence());
                        }
                    });
                }
            }

            DocumentField emails = businessCardFields.get("Emails");
            if (emails != null) {
                if (DocumentFieldType.LIST == emails.getType()) {
                    List<DocumentField> emailsItems = emails.getValueList();
                    emailsItems.forEach(emailsItem -> {
                        if (DocumentFieldType.STRING == emailsItem.getType()) {
                            String email = emailsItem.getValueString();
                            System.out.printf("Email: %s, confidence: %.2f%n", email, emailsItem.getConfidence());
                        }
                    });
                }
            }

            DocumentField websites = businessCardFields.get("Websites");
            if (websites != null) {
                if (DocumentFieldType.LIST == websites.getType()) {
                    List<DocumentField> websitesItems = websites.getValueList();
                    websitesItems.forEach(websitesItem -> {
                        if (DocumentFieldType.STRING == websitesItem.getType()) {
                            String website = websitesItem.getValueString();
                            System.out.printf("Web site: %s, confidence: %.2f%n",
                                website, websitesItem.getConfidence());
                        }
                    });
                }
            }

            DocumentField mobilePhones = businessCardFields.get("MobilePhones");
            if (mobilePhones != null) {
                if (DocumentFieldType.LIST == mobilePhones.getType()) {
                    List<DocumentField> mobilePhonesItems = mobilePhones.getValueList();
                    mobilePhonesItems.forEach(mobilePhonesItem -> {
                        if (DocumentFieldType.PHONE_NUMBER == mobilePhonesItem.getType()) {
                            String mobilePhoneNumber = mobilePhonesItem.getValuePhoneNumber();
                            System.out.printf("Mobile phone number: %s, confidence: %.2f%n",
                                mobilePhoneNumber, mobilePhonesItem.getConfidence());
                        }
                    });
                }
            }

            DocumentField otherPhones = businessCardFields.get("OtherPhones");
            if (otherPhones != null) {
                if (DocumentFieldType.LIST == otherPhones.getType()) {
                    List<DocumentField> otherPhonesItems = otherPhones.getValueList();
                    otherPhonesItems.forEach(otherPhonesItem -> {
                        if (DocumentFieldType.PHONE_NUMBER == otherPhonesItem.getType()) {
                            String otherPhoneNumber = otherPhonesItem.getValuePhoneNumber();
                            System.out.printf("Other phone number: %s, confidence: %.2f%n",
                                otherPhoneNumber, otherPhonesItem.getConfidence());
                        }
                    });
                }
            }

            DocumentField faxes = businessCardFields.get("Faxes");
            if (faxes != null) {
                if (DocumentFieldType.LIST == faxes.getType()) {
                    List<DocumentField> faxesItems = faxes.getValueList();
                    faxesItems.forEach(faxesItem -> {
                        if (DocumentFieldType.PHONE_NUMBER == faxesItem.getType()) {
                            String faxPhoneNumber = faxesItem.getValuePhoneNumber();
                            System.out.printf("Fax phone number: %s, confidence: %.2f%n",
                                faxPhoneNumber, faxesItem.getConfidence());
                        }
                    });
                }
            }

            DocumentField addresses = businessCardFields.get("Addresses");
            if (addresses != null) {
                if (DocumentFieldType.LIST == addresses.getType()) {
                    List<DocumentField> addressesItems = addresses.getValueList();
                    addressesItems.forEach(addressesItem -> {
                        if (DocumentFieldType.STRING == addressesItem.getType()) {
                            String address = addressesItem.getValueString();
                            System.out
                                .printf("Address: %s, confidence: %.2f%n", address, addressesItem.getConfidence());
                        }
                    });
                }
            }

            DocumentField companyName = businessCardFields.get("CompanyNames");
            if (companyName != null) {
                if (DocumentFieldType.LIST == companyName.getType()) {
                    List<DocumentField> companyNameItems = companyName.getValueList();
                    companyNameItems.forEach(companyNameItem -> {
                        if (DocumentFieldType.STRING == companyNameItem.getType()) {
                            String companyNameValue = companyNameItem.getValueString();
                            System.out.printf("Company name: %s, confidence: %.2f%n", companyNameValue,
                                companyNameItem.getConfidence());
                        }
                    });
                }
            }
        }
    }
}
