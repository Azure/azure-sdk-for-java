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
import com.azure.search.documents.indexes.models.WebApiHttpHeaders;
import com.azure.search.documents.indexes.models.WebApiSkill;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkill;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillChunkingProperties;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillChunkingUnit;
import com.azure.search.documents.indexes.models.AIServicesAccountKey;
import com.azure.search.documents.models.ContentUnderstandingSkillChunkingMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CreateSkillsetExample {

    /**
     * From the Azure portal, get your Azure AI Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");
    private static final String OCR_SKILLSET_NAME = "ocr-skillset";
    private static final String CUSTOM_SKILLSET_NAME = "custom-skillset";

    public static void main(String[] args) {
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();

        if (args.length > 0 && "content-understanding".equals(args[0])) {
            SearchIndexerSkillset skillset = new SearchIndexerSkillset("content-understanding-sample",
                contentUnderstandingSkill(System.getenv("SEARCH_CONTENT_MODEL_NAME"),
                    System.getenv("SEARCH_CONTENT_MODEL_DEPLOYMENT"), ContentUnderstandingSkillChunkingMethod.SEMANTIC))
                .setCognitiveServicesAccount(new AIServicesAccountKey(System.getenv("SEARCH_AI_SERVICES_API_KEY"),
                    System.getenv("SEARCH_AI_SERVICES_ENDPOINT")));
            searchIndexerClient.createSkillset(skillset);
            try {
                System.out.println(searchIndexerClient.getSkillset(skillset.getName()).getSkills());
            } finally {
                searchIndexerClient.deleteSkillset(skillset.getName());
            }
            return;
        }

        createOcrSkillset(searchIndexerClient);
        createCustomSkillset(searchIndexerClient);
        cleanupSkillset(searchIndexerClient);
    }

    /**
     * Builds a content-understanding skill for file data, using token-based semantic chunks or fixed-size characters.
     * Run the sample with {@code content-understanding} and set SEARCH_CONTENT_MODEL_NAME,
     * SEARCH_CONTENT_MODEL_DEPLOYMENT, SEARCH_AI_SERVICES_ENDPOINT, and SEARCH_AI_SERVICES_API_KEY in addition to the Search endpoint and key.
     * An indexer using this skill must expose /document/file_data (allowSkillsetToReadFileData).
     *
     * @param modelName The supported model name.
     * @param deployment The provisioned model deployment.
     * @param method The chunking method.
     * @return The configured content-understanding skill.
     */
    public static ContentUnderstandingSkill contentUnderstandingSkill(String modelName, String deployment,
        ContentUnderstandingSkillChunkingMethod method) {
        boolean semantic = ContentUnderstandingSkillChunkingMethod.SEMANTIC.equals(method);
        return new ContentUnderstandingSkill(
            Collections.singletonList(new InputFieldMappingEntry("file_data").setSource("/document/file_data")),
            Collections.singletonList(new OutputFieldMappingEntry("text_sections").setTargetName("sections")))
            .setModelName(modelName)
            .setModelDeployment(deployment)
            .setChunkingProperties(new ContentUnderstandingSkillChunkingProperties().setMethod(method)
                .setUnit(semantic ? ContentUnderstandingSkillChunkingUnit.TOKENS
                    : ContentUnderstandingSkillChunkingUnit.CHARACTERS)
                .setMaximumLength(semantic ? 500 : 2000));
    }

    private static void createOcrSkillset(SearchIndexerClient searchIndexerClient) {
        // Sample OCR definition
        // https://docs.microsoft.com/azure/search/cognitive-search-skill-ocr#sample-definition

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

        System.out.printf("Creating OCR skillset '%s'%n", skillset.getName());

        SearchIndexerSkillset createdSkillset = searchIndexerClient.createSkillset(skillset);

        System.out.println("Created OCR skillset");
        System.out.printf("Name: %s%n", createdSkillset.getName());
        System.out.printf("ETag: %s%n%n", createdSkillset.getETag());
    }

    private static void createCustomSkillset(SearchIndexerClient searchIndexerClient) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", "foobar");

        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("text").setSource("/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry("textItems").setTargetName("myTextItems"));

        SearchIndexerSkill webApiSkill = new WebApiSkill(inputs, outputs, "https://example.com")
            .setHttpMethod("POST") // Supports only "POST" and "PUT" HTTP methods
            .setHttpHeaders(new WebApiHttpHeaders().setAdditionalProperties(headers))
            .setName("webapi-skill")
            .setDescription("A WebApiSkill that can be used to call a custom web api function");

        SearchIndexerSkillset skillset = new SearchIndexerSkillset(CUSTOM_SKILLSET_NAME, webApiSkill)
            .setDescription("Skillset for testing custom skillsets");

        System.out.printf("Creating custom skillset '%s'%n", skillset.getName());

        SearchIndexerSkillset createdSkillset = searchIndexerClient.createSkillset(skillset);

        System.out.println("Created custom skillset");
        System.out.printf("Name: %s%n", createdSkillset.getName());
        System.out.printf("ETag: %s%n", createdSkillset.getETag());
    }

    private static void cleanupSkillset(SearchIndexerClient searchIndexerClient) {
        searchIndexerClient.deleteSkillset(OCR_SKILLSET_NAME);
        searchIndexerClient.deleteSkillset(CUSTOM_SKILLSET_NAME);
    }
}
