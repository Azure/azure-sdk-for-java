// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a search index definition, which describes the fields and search
 * behavior of an index.
 */
@Fluent
public final class SearchIndex {
    /*
     * The name of the index.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /*
     * The fields of the index.
     */
    @JsonProperty(value = "fields", required = true)
    private List<SearchField> fields;

    /*
     * The scoring profiles for the index.
     */
    @JsonProperty(value = "scoringProfiles")
    private List<ScoringProfile> scoringProfiles;

    /*
     * The name of the scoring profile to use if none is specified in the
     * query. If this property is not set and no scoring profile is specified
     * in the query, then default scoring (tf-idf) will be used.
     */
    @JsonProperty(value = "defaultScoringProfile")
    private String defaultScoringProfile;

    /*
     * Options to control Cross-Origin Resource Sharing (CORS) for the index.
     */
    @JsonProperty(value = "corsOptions")
    private CorsOptions corsOptions;

    /*
     * The suggesters for the index.
     */
    @JsonProperty(value = "suggesters")
    private List<SearchSuggester> suggesters;

    /*
     * The analyzers for the index.
     */
    @JsonProperty(value = "analyzers")
    private List<LexicalAnalyzer> analyzers;

    /*
     * The tokenizers for the index.
     */
    @JsonProperty(value = "tokenizers")
    private List<LexicalTokenizer> tokenizers;

    /*
     * The token filters for the index.
     */
    @JsonProperty(value = "tokenFilters")
    private List<TokenFilter> tokenFilters;

    /*
     * The character filters for the index.
     */
    @JsonProperty(value = "charFilters")
    private List<CharFilter> charFilters;

    /*
     * A description of an encryption key that you create in Azure Key Vault.
     * This key is used to provide an additional level of encryption-at-rest
     * for your data when you want full assurance that no one, not even
     * Microsoft, can decrypt your data in Azure Cognitive Search. Once you
     * have encrypted your data, it will always remain encrypted. Azure
     * Cognitive Search will ignore attempts to set this property to null. You
     * can change this property as needed if you want to rotate your encryption
     * key; Your data will be unaffected. Encryption with customer-managed keys
     * is not available for free search services, and is only available for
     * paid services created on or after January 1, 2019.
     */
    @JsonProperty(value = "encryptionKey")
    private SearchResourceEncryptionKey encryptionKey;

    /*
     * The type of similarity algorithm to be used when scoring and ranking the
     * documents matching a search query. The similarity algorithm can only be
     * defined at index creation time and cannot be modified on existing
     * indexes. If null, the ClassicSimilarity algorithm is used.
     */
    @JsonProperty(value = "similarity")
    private SimilarityAlgorithm similarity;

    /*
     * The ETag of the index.
     */
    @JsonProperty(value = "@odata.etag")
    private String eTag;


    /**
     * Constructor of {@link SearchIndex}.
     * @param name The name of the index.
     */
    public SearchIndex(String name) {
        this.name = name;
    }

    /**
     * Constructor of {@link SearchIndex}.
     * @param name The name of the index.
     * @param fields The fields of the index.
     */
    @JsonCreator
    public SearchIndex(
        @JsonProperty(value = "name") String name,
        @JsonProperty(value = "fields") List<SearchField> fields) {
        this.name = name;
        this.fields = fields;
    }

    /**
     * Get the name property: The name of the index.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the fields property: The fields of the index.
     *
     * @return the fields value.
     */
    public List<SearchField> getFields() {
        return this.fields;
    }

    /**
     * Set the fields property: The fields of the index.
     *
     * @param fields the fields value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setFields(SearchField... fields) {
        this.fields = (fields == null) ? null : Arrays.asList(fields);
        return this;
    }

    /**
     * Set the fields property: The fields of the index.
     *
     * @param fields the fields value to set.
     * @return the SearchIndex object itself.
     */
    @JsonSetter
    public SearchIndex setFields(List<SearchField> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * Get the scoringProfiles property: The scoring profiles for the index.
     *
     * @return the scoringProfiles value.
     */
    public List<ScoringProfile> getScoringProfiles() {
        return this.scoringProfiles;
    }

    /**
     * Set the scoringProfiles property: The scoring profiles for the index.
     *
     * @param scoringProfiles the scoringProfiles value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setScoringProfiles(ScoringProfile... scoringProfiles) {
        this.scoringProfiles = (scoringProfiles == null) ? null : Arrays.asList(scoringProfiles);
        return this;
    }

    /**
     * Set the scoringProfiles property: The scoring profiles for the index.
     *
     * @param scoringProfiles the scoringProfiles value to set.
     * @return the SearchIndex object itself.
     */
    @JsonSetter
    public SearchIndex setScoringProfiles(List<ScoringProfile> scoringProfiles) {
        this.scoringProfiles = scoringProfiles;
        return this;
    }

    /**
     * Get the defaultScoringProfile property: The name of the scoring profile
     * to use if none is specified in the query. If this property is not set
     * and no scoring profile is specified in the query, then default scoring
     * (tf-idf) will be used.
     *
     * @return the defaultScoringProfile value.
     */
    public String getDefaultScoringProfile() {
        return this.defaultScoringProfile;
    }

    /**
     * Set the defaultScoringProfile property: The name of the scoring profile
     * to use if none is specified in the query. If this property is not set
     * and no scoring profile is specified in the query, then default scoring
     * (tf-idf) will be used.
     *
     * @param defaultScoringProfile the defaultScoringProfile value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setDefaultScoringProfile(String defaultScoringProfile) {
        this.defaultScoringProfile = defaultScoringProfile;
        return this;
    }

    /**
     * Get the corsOptions property: Options to control Cross-Origin Resource
     * Sharing (CORS) for the index.
     *
     * @return the corsOptions value.
     */
    public CorsOptions getCorsOptions() {
        return this.corsOptions;
    }

    /**
     * Set the corsOptions property: Options to control Cross-Origin Resource
     * Sharing (CORS) for the index.
     *
     * @param corsOptions the corsOptions value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setCorsOptions(CorsOptions corsOptions) {
        this.corsOptions = corsOptions;
        return this;
    }

    /**
     * Get the suggesters property: The suggesters for the index.
     *
     * @return the suggesters value.
     */
    public List<SearchSuggester> getSuggesters() {
        return this.suggesters;
    }

    /**
     * Set the suggesters property: The suggesters for the index.
     *
     * @param suggesters the suggesters value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setSuggesters(SearchSuggester... suggesters) {
        this.suggesters = (suggesters == null) ? null : Arrays.asList(suggesters);
        return this;
    }

    /**
     * Set the suggesters property: The suggesters for the index.
     *
     * @param suggesters the suggesters value to set.
     * @return the SearchIndex object itself.
     */
    @JsonSetter
    public SearchIndex setSuggesters(List<SearchSuggester> suggesters) {
        this.suggesters = suggesters;
        return this;
    }

    /**
     * Get the analyzers property: The analyzers for the index.
     *
     * @return the analyzers value.
     */
    public List<LexicalAnalyzer> getAnalyzers() {
        return this.analyzers;
    }

    /**
     * Set the analyzers property: The analyzers for the index.
     *
     * @param analyzers the analyzers value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setAnalyzers(LexicalAnalyzer... analyzers) {
        this.analyzers = (analyzers == null) ? null : Arrays.asList(analyzers);
        return this;
    }

    /**
     * Set the analyzers property: The analyzers for the index.
     *
     * @param analyzers the analyzers value to set.
     * @return the SearchIndex object itself.
     */
    @JsonSetter
    public SearchIndex setAnalyzers(List<LexicalAnalyzer> analyzers) {
        this.analyzers = analyzers;
        return this;
    }

    /**
     * Get the tokenizers property: The tokenizers for the index.
     *
     * @return the tokenizers value.
     */
    public List<LexicalTokenizer> getTokenizers() {
        return this.tokenizers;
    }

    /**
     * Set the tokenizers property: The tokenizers for the index.
     *
     * @param tokenizers the tokenizers value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setTokenizers(LexicalTokenizer... tokenizers) {
        this.tokenizers = (tokenizers == null) ? null : Arrays.asList(tokenizers);
        return this;
    }

    /**
     * Set the tokenizers property: The tokenizers for the index.
     *
     * @param tokenizers the tokenizers value to set.
     * @return the SearchIndex object itself.
     */
    @JsonSetter
    public SearchIndex setTokenizers(List<LexicalTokenizer> tokenizers) {
        this.tokenizers = tokenizers;
        return this;
    }

    /**
     * Get the tokenFilters property: The token filters for the index.
     *
     * @return the tokenFilters value.
     */
    public List<TokenFilter> getTokenFilters() {
        return this.tokenFilters;
    }

    /**
     * Set the tokenFilters property: The token filters for the index.
     *
     * @param tokenFilters the tokenFilters value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setTokenFilters(TokenFilter... tokenFilters) {
        this.tokenFilters = (tokenFilters == null) ? null : Arrays.asList(tokenFilters);
        return this;
    }

    /**
     * Set the tokenFilters property: The token filters for the index.
     *
     * @param tokenFilters the tokenFilters value to set.
     * @return the SearchIndex object itself.
     */
    @JsonSetter
    public SearchIndex setTokenFilters(List<TokenFilter> tokenFilters) {
        this.tokenFilters = tokenFilters;
        return this;
    }

    /**
     * Get the charFilters property: The character filters for the index.
     *
     * @return the charFilters value.
     */
    public List<CharFilter> getCharFilters() {
        return this.charFilters;
    }

    /**
     * Set the charFilters property: The character filters for the index.
     *
     * @param charFilters the charFilters value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setCharFilters(CharFilter... charFilters) {
        this.charFilters = (charFilters == null) ? null : Arrays.asList(charFilters);
        return this;
    }

    /**
     * Set the charFilters property: The character filters for the index.
     *
     * @param charFilters the charFilters value to set.
     * @return the SearchIndex object itself.
     */
    @JsonSetter
    public SearchIndex setCharFilters(List<CharFilter> charFilters) {
        this.charFilters = charFilters;
        return this;
    }

    /**
     * Get the encryptionKey property: A description of an encryption key that
     * you create in Azure Key Vault. This key is used to provide an additional
     * level of encryption-at-rest for your data when you want full assurance
     * that no one, not even Microsoft, can decrypt your data in Azure
     * Cognitive Search. Once you have encrypted your data, it will always
     * remain encrypted. Azure Cognitive Search will ignore attempts to set
     * this property to null. You can change this property as needed if you
     * want to rotate your encryption key; Your data will be unaffected.
     * Encryption with customer-managed keys is not available for free search
     * services, and is only available for paid services created on or after
     * January 1, 2019.
     *
     * @return the encryptionKey value.
     */
    public SearchResourceEncryptionKey getEncryptionKey() {
        return this.encryptionKey;
    }

    /**
     * Set the encryptionKey property: A description of an encryption key that
     * you create in Azure Key Vault. This key is used to provide an additional
     * level of encryption-at-rest for your data when you want full assurance
     * that no one, not even Microsoft, can decrypt your data in Azure
     * Cognitive Search. Once you have encrypted your data, it will always
     * remain encrypted. Azure Cognitive Search will ignore attempts to set
     * this property to null. You can change this property as needed if you
     * want to rotate your encryption key; Your data will be unaffected.
     * Encryption with customer-managed keys is not available for free search
     * services, and is only available for paid services created on or after
     * January 1, 2019.
     *
     * @param encryptionKey the encryptionKey value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setEncryptionKey(SearchResourceEncryptionKey encryptionKey) {
        this.encryptionKey = encryptionKey;
        return this;
    }

    /**
     * Get the similarity property: The type of similarity algorithm to be used
     * when scoring and ranking the documents matching a search query. The
     * similarity algorithm can only be defined at index creation time and
     * cannot be modified on existing indexes. If null, the ClassicSimilarity
     * algorithm is used.
     *
     * @return the similarity value.
     */
    public SimilarityAlgorithm getSimilarity() {
        return this.similarity;
    }

    /**
     * Set the similarity property: The type of similarity algorithm to be used
     * when scoring and ranking the documents matching a search query. The
     * similarity algorithm can only be defined at index creation time and
     * cannot be modified on existing indexes. If null, the ClassicSimilarity
     * algorithm is used.
     *
     * @param similarity the similarity value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setSimilarity(SimilarityAlgorithm similarity) {
        this.similarity = similarity;
        return this;
    }

    /**
     * Get the eTag property: The ETag of the index.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag property: The ETag of the index.
     *
     * @param eTag the eTag value to set.
     * @return the SearchIndex object itself.
     */
    public SearchIndex setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
