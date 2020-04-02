// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OcrSkill;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.Skill;
import com.azure.search.documents.models.Skillset;
import com.azure.search.documents.models.WebApiSkill;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CreateSkillsetExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");
    private static final String OCR_SKILLSET_NAME = "ocr-skillset";
    private static final String CUSTOME_SKILLSET_NAME = "custom-skillset";

    public static void main(String[] args) {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();

        createOcrSkillset(searchServiceClient);
        createCustomSkillset(searchServiceClient);
        cleanupSkillset(searchServiceClient);
    }

    private static void createOcrSkillset(SearchServiceClient searchServiceClient) {
        // Sample OCR definition
        // https://docs.microsoft.com/en-us/azure/search/cognitive-search-skill-ocr#sample-definition

        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("image")
                .setSource("/document/normalized_images/*")
        );

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext"),
            new OutputFieldMappingEntry()
                .setName("layoutText")
                .setTargetName("myLayoutText")
        );

        List<Skill> skills = Collections.singletonList(
            new OcrSkill()
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*")
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        Skillset skillset = new Skillset()
            .setName(OCR_SKILLSET_NAME)
            .setDescription("Extracts text (plain and structured) from image.")
            .setSkills(skills);

        System.out.println(String.format("Creating OCR skillset '%s'", skillset.getName()));

        Skillset createdSkillset = searchServiceClient.createSkillset(skillset);

        System.out.println("Created OCR skillset");
        System.out.println(String.format("Name: %s", createdSkillset.getName()));
        System.out.println(String.format("ETag: %s", createdSkillset.getETag()));

        System.out.println("\n");
    }

    private static void createCustomSkillset(SearchServiceClient searchServiceClient) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", "foobar");

        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("textItems")
                .setTargetName("myTextItems")
        );

        Skill webApiSkill = new WebApiSkill()
            .setUri("https://example.com")
            .setHttpMethod("POST") // Supports only "POST" and "PUT" HTTP methods
            .setHttpHeaders(headers)
            .setInputs(inputs)
            .setOutputs(outputs)
            .setName("webapi-skill")
            .setDescription("A WebApiSkill that can be used to call a custom web api function");

        Skillset skillset = new Skillset()
            .setName(CUSTOME_SKILLSET_NAME)
            .setDescription("Skillset for testing custom skillsets")
            .setSkills(Collections.singletonList(webApiSkill));

        System.out.println(String.format("Creating custom skillset '%s'", skillset.getName()));

        Skillset createdSkillset = searchServiceClient.createSkillset(skillset);

        System.out.println("Created custom skillset");
        System.out.println(String.format("Name: %s", createdSkillset.getName()));
        System.out.println(String.format("ETag: %s", createdSkillset.getETag()));
    }

    private static void cleanupSkillset(SearchServiceClient searchServiceClient) {
        searchServiceClient.deleteSkillset(OCR_SKILLSET_NAME);
        searchServiceClient.deleteSkillset(CUSTOME_SKILLSET_NAME);
    }
}
