// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OcrSkill;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchIndexerSkill;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.WebApiSkill;

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
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();

        createOcrSkillset(searchIndexerClient);
        createCustomSkillset(searchIndexerClient);
        cleanupSkillset(searchIndexerClient);
    }

    private static void createOcrSkillset(SearchIndexerClient searchIndexerClient) {
        // Sample OCR definition
        // https://docs.microsoft.com/en-us/azure/search/cognitive-search-skill-ocr#sample-definition

        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("image")
                .setSource("/document/normalized_images/*")
        );

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry("text")
                .setTargetName("mytext"),
            new OutputFieldMappingEntry("layoutText")
                .setTargetName("myLayoutText")
        );

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*")
        );

        SearchIndexerSkillset skillset = new SearchIndexerSkillset(OCR_SKILLSET_NAME, skills)
            .setDescription("Extracts text (plain and structured) from image.");

        System.out.println(String.format("Creating OCR skillset '%s'", skillset.getName()));

        SearchIndexerSkillset createdSkillset = searchIndexerClient.createSkillset(skillset);

        System.out.println("Created OCR skillset");
        System.out.println(String.format("Name: %s", createdSkillset.getName()));
        System.out.println(String.format("ETag: %s", createdSkillset.getETag()));

        System.out.println("\n");
    }

    private static void createCustomSkillset(SearchIndexerClient searchIndexerClient) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", "foobar");

        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("text")
                .setSource("/document/mytext")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry("textItems")
                .setTargetName("myTextItems")
        );

        SearchIndexerSkill webApiSkill = new WebApiSkill(inputs, outputs, "https://example.com")
            .setHttpMethod("POST") // Supports only "POST" and "PUT" HTTP methods
            .setHttpHeaders(headers)
            .setName("webapi-skill")
            .setDescription("A WebApiSkill that can be used to call a custom web api function");

        SearchIndexerSkillset skillset = new SearchIndexerSkillset(CUSTOME_SKILLSET_NAME,
            Collections.singletonList(webApiSkill))
            .setDescription("Skillset for testing custom skillsets");

        System.out.println(String.format("Creating custom skillset '%s'", skillset.getName()));

        SearchIndexerSkillset createdSkillset = searchIndexerClient.createSkillset(skillset);

        System.out.println("Created custom skillset");
        System.out.println(String.format("Name: %s", createdSkillset.getName()));
        System.out.println(String.format("ETag: %s", createdSkillset.getETag()));
    }

    private static void cleanupSkillset(SearchIndexerClient searchIndexerClient) {
        searchIndexerClient.deleteSkillset(OCR_SKILLSET_NAME);
        searchIndexerClient.deleteSkillset(CUSTOME_SKILLSET_NAME);
    }
}
