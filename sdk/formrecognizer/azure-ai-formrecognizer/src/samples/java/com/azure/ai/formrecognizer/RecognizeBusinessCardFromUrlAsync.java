// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous sample for recognizing business card information from a URL.
 */
public class RecognizeBusinessCardFromUrlAsync {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String businessCardUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
                + "/azure-ai-formrecognizer/src/samples/resources/sample-forms/businessCards/businessCard.jpg";

        PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> recognizeBusinessCardPoller
            = client.beginRecognizeBusinessCardsFromUrl(businessCardUrl);

        Mono<List<RecognizedForm>> businessCardPageResultsMono
            = recognizeBusinessCardPoller
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
            for (int i = 0; i < businessCardPageResults.size(); i++) {
                RecognizedForm recognizedForm = businessCardPageResults.get(i);
                Map<String, FormField> recognizedFields = recognizedForm.getFields();
                System.out.printf("----------- Recognized business card info for page %d -----------%n", i);
                FormField contactNamesFormField = recognizedFields.get("ContactNames");
                if (contactNamesFormField != null) {
                    if (FieldValueType.LIST == contactNamesFormField.getValue().getValueType()) {
                        List<FormField> contactNamesValueFormFields = contactNamesFormField.getValue().asList();
                        contactNamesValueFormFields.stream()
                            .filter(contactNamesValueFormField -> FieldValueType.MAP
                                == contactNamesValueFormField.getValue().getValueType())
                            .map(contactNamesValueFormField -> {
                                System.out
                                    .printf("Contact name: %s%n", contactNamesValueFormField.getValueData().getText());
                                return contactNamesValueFormField.getValue().asMap();
                            })
                            .forEach(contactNamesFormFieldMap -> contactNamesFormFieldMap
                                .forEach((label, contactNameFormField) -> {
                                    if ("FirstName".equals(label)) {
                                        if (FieldValueType.STRING == contactNameFormField.getValue().getValueType()) {
                                            String firstName = contactNameFormField.getValue().asString();
                                            System.out.printf("\tFirst Name: %s, confidence: %.2f%n",
                                                firstName, contactNameFormField.getConfidence());
                                        }
                                    }
                                    if ("LastName".equals(label)) {
                                        if (FieldValueType.STRING == contactNameFormField.getValue().getValueType()) {
                                            String lastName = contactNameFormField.getValue().asString();
                                            System.out.printf("\tLast Name: %s, confidence: %.2f%n",
                                                lastName, contactNameFormField.getConfidence());
                                        }
                                    }
                                }));
                    }
                }

                FormField jobTitles = recognizedFields.get("JobTitles");
                if (jobTitles != null) {
                    if (FieldValueType.LIST == jobTitles.getValue().getValueType()) {
                        List<FormField> jobTitlesItems = jobTitles.getValue().asList();
                        jobTitlesItems.forEach(jobTitlesItem -> {
                            if (FieldValueType.STRING == jobTitlesItem.getValue().getValueType()) {
                                String jobTitle = jobTitlesItem.getValue().asString();
                                System.out.printf("Job Title: %s, confidence: %.2f%n",
                                    jobTitle, jobTitlesItem.getConfidence());
                            }
                        });
                    }
                }

                FormField departments = recognizedFields.get("Departments");
                if (departments != null) {
                    if (FieldValueType.LIST == departments.getValue().getValueType()) {
                        List<FormField> departmentsItems = departments.getValue().asList();
                        departmentsItems.forEach(departmentsItem -> {
                            if (FieldValueType.STRING == departmentsItem.getValue().getValueType()) {
                                String department = departmentsItem.getValue().asString();
                                System.out.printf("Department: %s, confidence: %.2f%n",
                                    department, departmentsItem.getConfidence());
                            }
                        });
                    }
                }

                FormField emails = recognizedFields.get("Emails");
                if (emails != null) {
                    if (FieldValueType.LIST == emails.getValue().getValueType()) {
                        List<FormField> emailsItems = emails.getValue().asList();
                        emailsItems.forEach(emailsItem -> {
                            if (FieldValueType.STRING == emailsItem.getValue().getValueType()) {
                                String email = emailsItem.getValue().asString();
                                System.out.printf("Email: %s, confidence: %.2f%n", email, emailsItem.getConfidence());
                            }
                        });
                    }
                }

                FormField websites = recognizedFields.get("Websites");
                if (websites != null) {
                    if (FieldValueType.LIST == websites.getValue().getValueType()) {
                        List<FormField> websitesItems = websites.getValue().asList();
                        websitesItems.forEach(websitesItem -> {
                            if (FieldValueType.STRING == websitesItem.getValue().getValueType()) {
                                String website = websitesItem.getValue().asString();
                                System.out.printf("Web site: %s, confidence: %.2f%n",
                                    website, websitesItem.getConfidence());
                            }
                        });
                    }
                }

                FormField mobilePhones = recognizedFields.get("MobilePhones");
                if (mobilePhones != null) {
                    if (FieldValueType.LIST == mobilePhones.getValue().getValueType()) {
                        List<FormField> mobilePhonesItems = mobilePhones.getValue().asList();
                        mobilePhonesItems.forEach(mobilePhonesItem -> {
                            if (FieldValueType.PHONE_NUMBER == mobilePhonesItem.getValue().getValueType()) {
                                String mobilePhoneNumber = mobilePhonesItem.getValue().asPhoneNumber();
                                System.out.printf("Mobile phone number: %s, confidence: %.2f%n",
                                    mobilePhoneNumber, mobilePhonesItem.getConfidence());
                            }
                        });
                    }
                }

                FormField otherPhones = recognizedFields.get("OtherPhones");
                if (otherPhones != null) {
                    if (FieldValueType.LIST == otherPhones.getValue().getValueType()) {
                        List<FormField> otherPhonesItems = otherPhones.getValue().asList();
                        otherPhonesItems.forEach(otherPhonesItem -> {
                            if (FieldValueType.PHONE_NUMBER == otherPhonesItem.getValue().getValueType()) {
                                String otherPhoneNumber = otherPhonesItem.getValue().asPhoneNumber();
                                System.out.printf("Other phone number: %s, confidence: %.2f%n",
                                    otherPhoneNumber, otherPhonesItem.getConfidence());
                            }
                        });
                    }
                }

                FormField faxes = recognizedFields.get("Faxes");
                if (faxes != null) {
                    if (FieldValueType.LIST == faxes.getValue().getValueType()) {
                        List<FormField> faxesItems = faxes.getValue().asList();
                        faxesItems.forEach(faxesItem -> {
                            if (FieldValueType.PHONE_NUMBER == faxesItem.getValue().getValueType()) {
                                String faxPhoneNumber = faxesItem.getValue().asPhoneNumber();
                                System.out.printf("Fax phone number: %s, confidence: %.2f%n",
                                    faxPhoneNumber, faxesItem.getConfidence());
                            }
                        });
                    }
                }

                FormField addresses = recognizedFields.get("Addresses");
                if (addresses != null) {
                    if (FieldValueType.LIST == addresses.getValue().getValueType()) {
                        List<FormField> addressesItems = addresses.getValue().asList();
                        addressesItems.forEach(addressesItem -> {
                            if (FieldValueType.STRING == addressesItem.getValue().getValueType()) {
                                String address = addressesItem.getValue().asString();
                                System.out
                                    .printf("Address: %s, confidence: %.2f%n", address, addressesItem.getConfidence());
                            }
                        });
                    }
                }

                FormField companyName = recognizedFields.get("CompanyNames");
                if (companyName != null) {
                    if (FieldValueType.LIST == companyName.getValue().getValueType()) {
                        List<FormField> companyNameItems = companyName.getValue().asList();
                        companyNameItems.forEach(companyNameItem -> {
                            if (FieldValueType.STRING == companyNameItem.getValue().getValueType()) {
                                String companyNameValue = companyNameItem.getValue().asString();
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
