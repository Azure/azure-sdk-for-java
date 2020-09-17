// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;
import java.util.Map;
/**
 * Sample for recognizing business card information from an URL.
 */
public class RecognizeBusinessCardFromUrl {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
                                          .credential(new AzureKeyCredential("{key}"))
                                          .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                                          .buildClient();

        String businessCardUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer"
            + "/azure-ai-formrecognizer/src/samples/java/sample-forms/businessCard/businessCard.jpg";

        SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> analyzeBusinessCardPoller =
            client.beginRecognizeBusinessCardFromUrl(businessCardUrl);

        List<RecognizedForm> businessCardPageResults = analyzeBusinessCardPoller.getFinalResult();

        for (int i = 0; i < businessCardPageResults.size(); i++) {
            RecognizedForm recognizedForm = businessCardPageResults.get(i);
            Map<String, FormField> recognizedFields = recognizedForm.getFields();
            System.out.printf("----------- Recognized business card info for page %d -----------%n", i);
            FormField contactNames = recognizedFields.get("ContactNames");
            if (contactNames != null) {
                if (FieldValueType.LIST == contactNames.getValue().getValueType()) {
                    List<FormField> businessCardItems = contactNames.getValue().asList();
                    businessCardItems.stream()
                        .filter(businessCardItem -> FieldValueType.MAP == businessCardItem.getValue().getValueType())
                        .map(formField -> formField.getValue().asMap())
                        .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                            if ("FirstName".equals(key)) {
                                if (FieldValueType.STRING == formField.getValue().getValueType()) {
                                    String firstName = formField.getValue().asString();
                                    System.out.printf("First Name: %s, confidence: %.2f%n",
                                        firstName, contactNames.getConfidence());
                                }
                            }
                            if ("LastName".equals(key)) {
                                if (FieldValueType.STRING == formField.getValue().getValueType()) {
                                    String lastName = formField.getValue().asString();
                                    System.out.printf("Last Name: %s, confidence: %.2f%n",
                                        lastName, contactNames.getConfidence());
                                }
                            }
                        }));
                }
            }

            FormField jobTitles = recognizedFields.get("JobTitles");
            if (jobTitles != null) {
                if (FieldValueType.LIST == jobTitles.getValue().getValueType()) {
                    List<FormField> jobTitlesItems = jobTitles.getValue().asList();
                    jobTitlesItems.stream().forEach(formField -> {
                        if (FieldValueType.STRING == formField.getValue().getValueType()) {
                            String jobTitle = formField.getValue().asString();
                            System.out.printf("Job Title: %s, confidence: %.2f%n",
                                jobTitle, jobTitles.getConfidence());
                        }
                    });
                }
            }

            FormField departments = recognizedFields.get("Departments");
            if (departments != null) {
                if (FieldValueType.LIST == departments.getValue().getValueType()) {
                    List<FormField> departmentsItems = departments.getValue().asList();
                    departmentsItems.stream().forEach(formField -> {
                        if (FieldValueType.STRING == formField.getValue().getValueType()) {
                            String department = formField.getValue().asString();
                            System.out.printf("Department: %s, confidence: %.2f%n",
                                department, departments.getConfidence());
                        }
                    });
                }
            }

            FormField emails = recognizedFields.get("Emails");
            if (emails != null) {
                if (FieldValueType.LIST == emails.getValue().getValueType()) {
                    List<FormField> emailsItems = emails.getValue().asList();
                    emailsItems.stream().forEach(formField -> {
                        if (FieldValueType.STRING == formField.getValue().getValueType()) {
                            String email = formField.getValue().asString();
                            System.out.printf("Email: %s, confidence: %.2f%n", email, emails.getConfidence());
                        }
                    });
                }
            }

            FormField websites = recognizedFields.get("Websites");
            if (websites != null) {
                if (FieldValueType.LIST == websites.getValue().getValueType()) {
                    List<FormField> websitesItems = websites.getValue().asList();
                    websitesItems.stream().forEach(formField -> {
                        if (FieldValueType.STRING == formField.getValue().getValueType()) {
                            String website = formField.getValue().asString();
                            System.out.printf("Web site: %s, confidence: %.2f%n",
                                website, websites.getConfidence());
                        }
                    });
                }
            }

            FormField mobilePhones = recognizedFields.get("MobilePhones");
            if (mobilePhones != null) {
                if (FieldValueType.LIST == mobilePhones.getValue().getValueType()) {
                    List<FormField> mobilePhonesItems = mobilePhones.getValue().asList();
                    mobilePhonesItems.stream().forEach(formField -> {
                        if (FieldValueType.PHONE_NUMBER == formField.getValue().getValueType()) {
                            String mobilePhoneNumber = formField.getValue().asPhoneNumber();
                            System.out.printf("Mobile phone number: %s, confidence: %.2f%n",
                                mobilePhoneNumber, mobilePhones.getConfidence());
                        }
                    });
                }
            }

            FormField otherPhones = recognizedFields.get("OtherPhones");
            if (otherPhones != null) {
                if (FieldValueType.LIST == otherPhones.getValue().getValueType()) {
                    List<FormField> mobilePhonesItems = otherPhones.getValue().asList();
                    mobilePhonesItems.stream().forEach(formField -> {
                        if (FieldValueType.PHONE_NUMBER == formField.getValue().getValueType()) {
                            String otherPhoneNumber = formField.getValue().asPhoneNumber();
                            System.out.printf("Other phone number: %s, confidence: %.2f%n",
                                otherPhoneNumber, otherPhones.getConfidence());
                        }
                    });
                }
            }

            FormField faxes = recognizedFields.get("Faxes");
            if (faxes != null) {
                if (FieldValueType.LIST == faxes.getValue().getValueType()) {
                    List<FormField> faxesItems = faxes.getValue().asList();
                    faxesItems.stream().forEach(formField -> {
                        if (FieldValueType.PHONE_NUMBER == formField.getValue().getValueType()) {
                            String faxPhoneNumber = formField.getValue().asPhoneNumber();
                            System.out.printf("Fax phone number: %s, confidence: %.2f%n",
                                faxPhoneNumber, faxes.getConfidence());
                        }
                    });
                }
            }

            FormField addresses = recognizedFields.get("Addresses");
            if (addresses != null) {
                if (FieldValueType.LIST == addresses.getValue().getValueType()) {
                    List<FormField> addressesItems = addresses.getValue().asList();
                    addressesItems.stream().forEach(formField -> {
                        if (FieldValueType.STRING == formField.getValue().getValueType()) {
                            String address = formField.getValue().asString();
                            System.out.printf("Address: %s, confidence: %.2f%n", address, addresses.getConfidence());
                        }
                    });
                }
            }

            FormField companyName = recognizedFields.get("CompanyNames");
            if (companyName != null) {
                if (FieldValueType.LIST == companyName.getValue().getValueType()) {
                    List<FormField> companyNameItems = companyName.getValue().asList();
                    companyNameItems.stream().forEach(formField -> {
                        if (FieldValueType.STRING == formField.getValue().getValueType()) {
                            String companyNameValue = formField.getValue().asString();
                            System.out.printf("Company name: %s, confidence: %.2f%n", companyNameValue, companyName.getConfidence());
                        }
                    });
                }
            }
        }
    }
}
