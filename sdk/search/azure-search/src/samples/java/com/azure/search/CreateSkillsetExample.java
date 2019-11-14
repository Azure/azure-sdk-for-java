// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.Configuration;
import com.azure.search.models.InputFieldMappingEntry;
import com.azure.search.models.OcrSkill;
import com.azure.search.models.OutputFieldMappingEntry;
import com.azure.search.models.Skill;
import com.azure.search.models.Skillset;
import com.azure.search.models.WebApiSkill;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CreateSkillsetExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ADMIN_KEY");

    public static void main(String[] args) {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(ADMIN_KEY))
            .buildClient();

        createOcrSkillset(searchServiceClient);
        createCustomSkillset(searchServiceClient);
    }

    private static void createOcrSkillset(SearchServiceClient searchServiceClient) {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );

        List<Skill> skills = Collections.singletonList(
            new OcrSkill()
                .setShouldDetectOrientation(true)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext("/document")
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        Skillset skillset = new Skillset()
            .setName("ocr-skillset")
            .setDescription("Skillset for testing default configuration")
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
            .setDescription("A WebApi skill that can be used as a custom skillset");

        Skillset skillset = new Skillset()
            .setName("custom-skillset")
            .setDescription("Skillset for testing custom skillsets")
            .setSkills(Collections.singletonList(webApiSkill));

        System.out.println(String.format("Creating custom skillset '%s'", skillset.getName()));

        Skillset createdSkillset = searchServiceClient.createSkillset(skillset);

        System.out.println("Created custom skillset");
        System.out.println(String.format("Name: %s", createdSkillset.getName()));
        System.out.println(String.format("ETag: %s", createdSkillset.getETag()));
    }
}