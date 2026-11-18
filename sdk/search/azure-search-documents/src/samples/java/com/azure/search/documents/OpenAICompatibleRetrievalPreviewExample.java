// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseOpenAICompatibleModel;
import com.azure.search.documents.indexes.models.KnowledgeBaseOpenAICompatibleParameters;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.KnowledgeSourceSynchronizationStatus;
import com.azure.search.documents.indexes.models.SearchIndexFieldReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAgenticReasoningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;
import com.azure.search.documents.knowledgebases.models.SearchIndexKnowledgeSourceParams;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Creates a hotel index, knowledge source, and knowledge base, then retrieves evidence through an
 * OpenAI-compatible provider or Azure API Management gateway using the November 2026 preview.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code AZURE_SEARCH_COMPATIBLE_CHAT_URI}, and
 * {@code AZURE_SEARCH_COMPATIBLE_CHAT_MODEL}. The URI is the complete HTTPS POST URL, including its path and query;
 * it is not a resource root. Set exactly one of {@code AZURE_SEARCH_COMPATIBLE_CHAT_HEADERS_JSON}
 * (a nonempty JSON string-to-string header map, for example an APIM subscription header) or
 * {@code AZURE_SEARCH_COMPATIBLE_CHAT_MI_AUDIENCE} (the third-party Entra application's bare GUID).
 * In the latter case, grant the Search service's managed identity access to that application.</p>
 *
 * <p>The gateway must select the actual deployed model: Search does not send a model property to the compatible
 * provider. For GPT-6, specify a real supported profile such as {@code gpt-6-sol}, not {@code gpt-6}, and a full
 * {@code /responses} URI, including any required API-version query. The provider must implement stateless Responses
 * tool continuation. A successful Search retrieval does not by itself prove the provider's wire exchange;
 * confirm that exchange in appropriately redacted gateway traces.</p>
 *
 * <p>Requires compatible-provider preview availability, semantic ranking, and a free index slot. No Web knowledge
 * sources are used. Only resources successfully created by this run are deleted, in reverse order.
 * Provider secrets, URIs, and response content are not printed.</p>
 */
public final class OpenAICompatibleRetrievalPreviewExample {
    private OpenAICompatibleRetrievalPreviewExample() {
    }

    /**
     * Runs the compatible-provider retrieval scenario.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        KnowledgeBaseOpenAICompatibleParameters provider = providerFromEnvironment();
        run(PreviewSampleSupport.indexClient(), PreviewSampleSupport::retrievalClient, provider);
        System.out.println("Retrieved seeded hotel evidence through the configured compatible provider.");
        if (provider.getModelName().toString().startsWith("gpt-6")) {
            System.out.println("GPT-6 profile selected. Confirm stateless Responses continuation in gateway traces.");
        }
    }

    static KnowledgeBaseOpenAICompatibleParameters providerFromEnvironment() {
        return provider(PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_CHAT_URI"),
            PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_CHAT_MODEL"),
            System.getenv("AZURE_SEARCH_COMPATIBLE_CHAT_HEADERS_JSON"),
            System.getenv("AZURE_SEARCH_COMPATIBLE_CHAT_MI_AUDIENCE"));
    }

    static KnowledgeBaseOpenAICompatibleParameters provider(String uri, String model, String headers, String audience) {
        String fullUri = fullUri(uri);
        if (model == null || model.trim().isEmpty() || !model.equals(model.trim()) || "gpt-6".equals(model)) {
            throw new IllegalArgumentException("Specify the actual supported model profile, not a generic gpt-6 name.");
        }
        if (model.startsWith("gpt-6") && !URI.create(fullUri).getPath().endsWith("/responses")) {
            throw new IllegalArgumentException("The GPT-6 sample requires a full HTTPS /responses POST URI.");
        }
        CompatibleAuth auth = CompatibleAuth.parse(headers, audience);
        return new KnowledgeBaseOpenAICompatibleParameters(fullUri, AzureOpenAIModelName.fromString(model))
            .setHeaders(auth.headers)
            .setManagedIdentityAudience(auth.audience);
    }

    static void run(SearchIndexClient indexes, Function<String, KnowledgeBaseRetrievalClient> retrievalFactory,
        KnowledgeBaseOpenAICompatibleParameters provider) {
        String indexName = PreviewSampleSupport.name("compatible-hotels");
        String sourceName = indexName + "-source";
        String baseName = indexName + "-kb";
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createIndex(PreviewSampleSupport.textIndex(indexName));
            PreviewSampleSupport.upload(indexes.getSearchClient(indexName), PreviewSampleSupport.hotelDocuments());
            resources.createKnowledgeSource(new SearchIndexKnowledgeSource(sourceName,
                new SearchIndexKnowledgeSourceParameters(indexName).setSemanticConfigurationName("default")
                    .setSourceDataFields(new SearchIndexFieldReference("id"), new SearchIndexFieldReference("title"),
                        new SearchIndexFieldReference("content"), new SearchIndexFieldReference("country"))));
            waitForSource(indexes, sourceName);
            resources.createKnowledgeBase(knowledgeBase(baseName, sourceName, provider));
            KnowledgeBaseRetrievalResult result = retrievalFactory.apply(baseName).retrieve(request(sourceName));
            verifyResult(result, sourceName, new HashSet<>(Arrays.asList("hotel-ca-1", "hotel-ca-2", "hotel-us-1")),
                false);
        }
    }

    static KnowledgeBase knowledgeBase(String name, String sourceName,
        KnowledgeBaseOpenAICompatibleParameters provider) {
        return new KnowledgeBase(name, new KnowledgeSourceReference(sourceName))
            .setModels(new KnowledgeBaseOpenAICompatibleModel(provider))
            .setRetrievalReasoningEffort(new KnowledgeRetrievalLowReasoningEffort())
            .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA);
    }

    static KnowledgeBaseRetrievalOptions request(String sourceName) {
        return new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent(
                "Find the hotel guest benefits, including internet, breakfast, and parking."))
            .setIncludeActivity(true)
            .setKnowledgeSourceParams(Collections.singletonList(new SearchIndexKnowledgeSourceParams(sourceName)
                .setIncludeReferences(true).setIncludeReferenceSourceData(true).setFailOnError(true)));
    }

    static void waitForSource(SearchIndexClient indexes, String sourceName) {
        for (int attempt = 0; attempt < 30; attempt++) {
            KnowledgeSourceStatus status = indexes.getKnowledgeSourceStatus(sourceName);
            if (KnowledgeSourceSynchronizationStatus.ACTIVE.equals(status.getSynchronizationStatus())) {
                return;
            }
            if (!KnowledgeSourceSynchronizationStatus.CREATING.equals(status.getSynchronizationStatus())) {
                throw new IllegalStateException("The compatible knowledge source did not become active.");
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for the compatible knowledge source.", ex);
            }
        }
        throw new IllegalStateException("The compatible knowledge source did not become active within 60 seconds.");
    }

    static void verifyResult(KnowledgeBaseRetrievalResult result, String sourceName, Set<String> allowedKeys,
        boolean requireReasoningUsage) {
        if (result.getResponse() == null || result.getResponse().stream()
            .filter(message -> message.getContent() != null)
            .flatMap(message -> message.getContent().stream())
            .filter(KnowledgeBaseMessageTextContent.class::isInstance)
            .map(KnowledgeBaseMessageTextContent.class::cast)
            .noneMatch(content -> content.getText() != null && !content.getText().trim().isEmpty())) {
            throw new IllegalStateException("Retrieval returned no extractive response content.");
        }
        List<KnowledgeBaseActivityRecord> activity = result.getActivity();
        if (activity == null || activity.stream().anyMatch(record -> record.getError() != null)) {
            throw new IllegalStateException("Retrieval must return activity without errors.");
        }
        Set<Integer> sourceActivityIds = new HashSet<>();
        boolean reasoningUsage = false;
        for (KnowledgeBaseActivityRecord record : activity) {
            if (record instanceof KnowledgeBaseSearchIndexActivityRecord) {
                if (!sourceName.equals(((KnowledgeBaseSearchIndexActivityRecord) record).getKnowledgeSourceName())) {
                    throw new IllegalStateException("Retrieval searched an unexpected knowledge source.");
                }
                sourceActivityIds.add(record.getId());
            }
            if (record instanceof KnowledgeBaseAgenticReasoningActivityRecord) {
                Integer tokens = ((KnowledgeBaseAgenticReasoningActivityRecord) record).getReasoningTokens();
                if (tokens != null && tokens < 0) {
                    throw new IllegalStateException("Retrieval reported invalid reasoning usage.");
                }
                reasoningUsage |= tokens != null;
            }
        }
        if (sourceActivityIds.isEmpty() || result.getReferences() == null || result.getReferences().isEmpty()) {
            throw new IllegalStateException("Retrieval must return search activity and document references.");
        }
        for (KnowledgeBaseReference reference : result.getReferences()) {
            if (!(reference instanceof KnowledgeBaseSearchIndexReference)
                || !allowedKeys.contains(((KnowledgeBaseSearchIndexReference) reference).getDocKey())
                || !sourceActivityIds.contains(reference.getActivitySource())
                || reference.getSourceData() == null
                || !(reference.getSourceData().get("content") instanceof String)
                || ((String) reference.getSourceData().get("content")).trim().isEmpty()) {
                throw new IllegalStateException("Retrieval references did not contain the expected scoped evidence.");
            }
        }
        if (requireReasoningUsage && !reasoningUsage) {
            throw new IllegalStateException("The GPT-6 retrieval did not report agentic reasoning token usage.");
        }
    }

    static String fullUri(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Configure a valid full HTTPS provider POST URI.");
        }
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawFragment() != null
                || uri.getRawPath() == null || uri.getRawPath().length() < 2) {
                throw new IllegalArgumentException(
                    "Configure the full HTTPS provider POST URI without credentials or a fragment.");
            }
            return value;
        } catch (IllegalArgumentException ex) {
            // Do not include an exception cause: URI errors can contain secret query parameters.
            throw new IllegalArgumentException("Configure a valid full HTTPS provider POST URI.");
        }
    }

    static final class CompatibleAuth {
        final Map<String, String> headers;
        final String audience;

        private CompatibleAuth(Map<String, String> headers, String audience) {
            this.headers = headers;
            this.audience = audience;
        }

        static CompatibleAuth parse(String headersJson, String audience) {
            if ((headersJson == null) == (audience == null)) {
                throw new IllegalArgumentException("Set exactly one provider HEADERS_JSON or MI_AUDIENCE.");
            }
            if (audience != null) {
                if (!audience.matches("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")) {
                    throw new IllegalArgumentException("MI_AUDIENCE must be the third-party application's bare GUID.");
                }
                return new CompatibleAuth(null, audience);
            }
            Map<String, String> headers = new LinkedHashMap<>();
            Set<String> names = new HashSet<>();
            try (JsonReader reader = JsonProviders.createReader(headersJson)) {
                if (reader.nextToken() != JsonToken.START_OBJECT) {
                    throw new IllegalArgumentException();
                }
                Map<String, Object> values = reader.readMap(JsonReader::readUntyped);
                if (values == null || values.isEmpty()) {
                    throw new IllegalArgumentException();
                }
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    String name = entry.getKey();
                    String lowerName = name.toLowerCase(Locale.ROOT);
                    if (!name.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+") || !names.add(lowerName)
                        || Arrays.asList("host", "content-length", "content-type", "connection", "transfer-encoding",
                            "te", "trailer", "upgrade", "proxy-authorization", "proxy-authenticate").contains(lowerName)
                        || !(entry.getValue() instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    String value = (String) entry.getValue();
                    if (value.trim().isEmpty()
                        || value.chars().anyMatch(character -> character < 32 || character == 127)) {
                        throw new IllegalArgumentException();
                    }
                    headers.put(name, value);
                }
                if (reader.nextToken() != JsonToken.END_DOCUMENT) {
                    throw new IllegalArgumentException();
                }
            } catch (IOException | RuntimeException ex) {
                // Never echo malformed JSON or attach a parser exception containing header secrets.
                throw new IllegalArgumentException("HEADERS_JSON must be a nonempty object of safe string headers.");
            }
            return new CompatibleAuth(Collections.unmodifiableMap(headers), null);
        }
    }
}
