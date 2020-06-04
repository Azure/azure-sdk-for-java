// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Specifies some text and analysis components used to break that text into
 * tokens.
 */
@Fluent
public final class AnalyzeTextOptions {
    /*
     * The text to break into tokens.
     */
    @JsonProperty(value = "text", required = true)
    private String text;

    /*
     * The name of the analyzer to use to break the given text. If this
     * parameter is not specified, you must specify a tokenizer instead. The
     * tokenizer and analyzer parameters are mutually exclusive. Possible
     * values include: 'ArMicrosoft', 'ArLucene', 'HyLucene', 'BnMicrosoft',
     * 'EuLucene', 'BgMicrosoft', 'BgLucene', 'CaMicrosoft', 'CaLucene',
     * 'ZhHansMicrosoft', 'ZhHansLucene', 'ZhHantMicrosoft', 'ZhHantLucene',
     * 'HrMicrosoft', 'CsMicrosoft', 'CsLucene', 'DaMicrosoft', 'DaLucene',
     * 'NlMicrosoft', 'NlLucene', 'EnMicrosoft', 'EnLucene', 'EtMicrosoft',
     * 'FiMicrosoft', 'FiLucene', 'FrMicrosoft', 'FrLucene', 'GlLucene',
     * 'DeMicrosoft', 'DeLucene', 'ElMicrosoft', 'ElLucene', 'GuMicrosoft',
     * 'HeMicrosoft', 'HiMicrosoft', 'HiLucene', 'HuMicrosoft', 'HuLucene',
     * 'IsMicrosoft', 'IdMicrosoft', 'IdLucene', 'GaLucene', 'ItMicrosoft',
     * 'ItLucene', 'JaMicrosoft', 'JaLucene', 'KnMicrosoft', 'KoMicrosoft',
     * 'KoLucene', 'LvMicrosoft', 'LvLucene', 'LtMicrosoft', 'MlMicrosoft',
     * 'MsMicrosoft', 'MrMicrosoft', 'NbMicrosoft', 'NoLucene', 'FaLucene',
     * 'PlMicrosoft', 'PlLucene', 'PtBrMicrosoft', 'PtBrLucene',
     * 'PtPtMicrosoft', 'PtPtLucene', 'PaMicrosoft', 'RoMicrosoft', 'RoLucene',
     * 'RuMicrosoft', 'RuLucene', 'SrCyrillicMicrosoft', 'SrLatinMicrosoft',
     * 'SkMicrosoft', 'SlMicrosoft', 'EsMicrosoft', 'EsLucene', 'SvMicrosoft',
     * 'SvLucene', 'TaMicrosoft', 'TeMicrosoft', 'ThMicrosoft', 'ThLucene',
     * 'TrMicrosoft', 'TrLucene', 'UkMicrosoft', 'UrMicrosoft', 'ViMicrosoft',
     * 'StandardLucene', 'StandardAsciiFoldingLucene', 'Keyword', 'Pattern',
     * 'Simple', 'Stop', 'Whitespace'
     */
    @JsonProperty(value = "analyzer")
    private LexicalAnalyzerName analyzer;

    /*
     * The name of the tokenizer to use to break the given text. If this
     * parameter is not specified, you must specify an analyzer instead. The
     * tokenizer and analyzer parameters are mutually exclusive. Possible
     * values include: 'Classic', 'EdgeNGram', 'Keyword', 'Letter',
     * 'Lowercase', 'MicrosoftLanguageTokenizer',
     * 'MicrosoftLanguageStemmingTokenizer', 'NGram', 'PathHierarchy',
     * 'Pattern', 'Standard', 'UaxUrlEmail', 'Whitespace'
     */
    @JsonProperty(value = "tokenizer")
    private LexicalTokenizerName tokenizer;

    /*
     * An optional list of token filters to use when breaking the given text.
     * This parameter can only be set when using the tokenizer parameter.
     */
    @JsonProperty(value = "tokenFilters")
    private List<TokenFilterName> tokenFilters;

    /*
     * An optional list of character filters to use when breaking the given
     * text. This parameter can only be set when using the tokenizer parameter.
     */
    @JsonProperty(value = "charFilters")
    private List<CharFilterName> charFilters;

    /**
     * Get the text property: The text to break into tokens.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Set the text property: The text to break into tokens.
     *
     * @param text the text value to set.
     * @return the AnalyzeRequest object itself.
     */
    public AnalyzeTextOptions setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the analyzer property: The name of the analyzer to use to break the
     * given text. If this parameter is not specified, you must specify a
     * tokenizer instead. The tokenizer and analyzer parameters are mutually
     * exclusive. Possible values include: 'ArMicrosoft', 'ArLucene',
     * 'HyLucene', 'BnMicrosoft', 'EuLucene', 'BgMicrosoft', 'BgLucene',
     * 'CaMicrosoft', 'CaLucene', 'ZhHansMicrosoft', 'ZhHansLucene',
     * 'ZhHantMicrosoft', 'ZhHantLucene', 'HrMicrosoft', 'CsMicrosoft',
     * 'CsLucene', 'DaMicrosoft', 'DaLucene', 'NlMicrosoft', 'NlLucene',
     * 'EnMicrosoft', 'EnLucene', 'EtMicrosoft', 'FiMicrosoft', 'FiLucene',
     * 'FrMicrosoft', 'FrLucene', 'GlLucene', 'DeMicrosoft', 'DeLucene',
     * 'ElMicrosoft', 'ElLucene', 'GuMicrosoft', 'HeMicrosoft', 'HiMicrosoft',
     * 'HiLucene', 'HuMicrosoft', 'HuLucene', 'IsMicrosoft', 'IdMicrosoft',
     * 'IdLucene', 'GaLucene', 'ItMicrosoft', 'ItLucene', 'JaMicrosoft',
     * 'JaLucene', 'KnMicrosoft', 'KoMicrosoft', 'KoLucene', 'LvMicrosoft',
     * 'LvLucene', 'LtMicrosoft', 'MlMicrosoft', 'MsMicrosoft', 'MrMicrosoft',
     * 'NbMicrosoft', 'NoLucene', 'FaLucene', 'PlMicrosoft', 'PlLucene',
     * 'PtBrMicrosoft', 'PtBrLucene', 'PtPtMicrosoft', 'PtPtLucene',
     * 'PaMicrosoft', 'RoMicrosoft', 'RoLucene', 'RuMicrosoft', 'RuLucene',
     * 'SrCyrillicMicrosoft', 'SrLatinMicrosoft', 'SkMicrosoft', 'SlMicrosoft',
     * 'EsMicrosoft', 'EsLucene', 'SvMicrosoft', 'SvLucene', 'TaMicrosoft',
     * 'TeMicrosoft', 'ThMicrosoft', 'ThLucene', 'TrMicrosoft', 'TrLucene',
     * 'UkMicrosoft', 'UrMicrosoft', 'ViMicrosoft', 'StandardLucene',
     * 'StandardAsciiFoldingLucene', 'Keyword', 'Pattern', 'Simple', 'Stop',
     * 'Whitespace'.
     *
     * @return the analyzer value.
     */
    public LexicalAnalyzerName getAnalyzer() {
        return this.analyzer;
    }

    /**
     * Set the analyzer property: The name of the analyzer to use to break the
     * given text. If this parameter is not specified, you must specify a
     * tokenizer instead. The tokenizer and analyzer parameters are mutually
     * exclusive. Possible values include: 'ArMicrosoft', 'ArLucene',
     * 'HyLucene', 'BnMicrosoft', 'EuLucene', 'BgMicrosoft', 'BgLucene',
     * 'CaMicrosoft', 'CaLucene', 'ZhHansMicrosoft', 'ZhHansLucene',
     * 'ZhHantMicrosoft', 'ZhHantLucene', 'HrMicrosoft', 'CsMicrosoft',
     * 'CsLucene', 'DaMicrosoft', 'DaLucene', 'NlMicrosoft', 'NlLucene',
     * 'EnMicrosoft', 'EnLucene', 'EtMicrosoft', 'FiMicrosoft', 'FiLucene',
     * 'FrMicrosoft', 'FrLucene', 'GlLucene', 'DeMicrosoft', 'DeLucene',
     * 'ElMicrosoft', 'ElLucene', 'GuMicrosoft', 'HeMicrosoft', 'HiMicrosoft',
     * 'HiLucene', 'HuMicrosoft', 'HuLucene', 'IsMicrosoft', 'IdMicrosoft',
     * 'IdLucene', 'GaLucene', 'ItMicrosoft', 'ItLucene', 'JaMicrosoft',
     * 'JaLucene', 'KnMicrosoft', 'KoMicrosoft', 'KoLucene', 'LvMicrosoft',
     * 'LvLucene', 'LtMicrosoft', 'MlMicrosoft', 'MsMicrosoft', 'MrMicrosoft',
     * 'NbMicrosoft', 'NoLucene', 'FaLucene', 'PlMicrosoft', 'PlLucene',
     * 'PtBrMicrosoft', 'PtBrLucene', 'PtPtMicrosoft', 'PtPtLucene',
     * 'PaMicrosoft', 'RoMicrosoft', 'RoLucene', 'RuMicrosoft', 'RuLucene',
     * 'SrCyrillicMicrosoft', 'SrLatinMicrosoft', 'SkMicrosoft', 'SlMicrosoft',
     * 'EsMicrosoft', 'EsLucene', 'SvMicrosoft', 'SvLucene', 'TaMicrosoft',
     * 'TeMicrosoft', 'ThMicrosoft', 'ThLucene', 'TrMicrosoft', 'TrLucene',
     * 'UkMicrosoft', 'UrMicrosoft', 'ViMicrosoft', 'StandardLucene',
     * 'StandardAsciiFoldingLucene', 'Keyword', 'Pattern', 'Simple', 'Stop',
     * 'Whitespace'.
     *
     * @param analyzer the analyzer value to set.
     * @return the AnalyzeRequest object itself.
     */
    public AnalyzeTextOptions setAnalyzer(LexicalAnalyzerName analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    /**
     * Get the tokenizer property: The name of the tokenizer to use to break
     * the given text. If this parameter is not specified, you must specify an
     * analyzer instead. The tokenizer and analyzer parameters are mutually
     * exclusive. Possible values include: 'Classic', 'EdgeNGram', 'Keyword',
     * 'Letter', 'Lowercase', 'MicrosoftLanguageTokenizer',
     * 'MicrosoftLanguageStemmingTokenizer', 'NGram', 'PathHierarchy',
     * 'Pattern', 'Standard', 'UaxUrlEmail', 'Whitespace'.
     *
     * @return the tokenizer value.
     */
    public LexicalTokenizerName getTokenizer() {
        return this.tokenizer;
    }

    /**
     * Set the tokenizer property: The name of the tokenizer to use to break
     * the given text. If this parameter is not specified, you must specify an
     * analyzer instead. The tokenizer and analyzer parameters are mutually
     * exclusive. Possible values include: 'Classic', 'EdgeNGram', 'Keyword',
     * 'Letter', 'Lowercase', 'MicrosoftLanguageTokenizer',
     * 'MicrosoftLanguageStemmingTokenizer', 'NGram', 'PathHierarchy',
     * 'Pattern', 'Standard', 'UaxUrlEmail', 'Whitespace'.
     *
     * @param tokenizer the tokenizer value to set.
     * @return the AnalyzeRequest object itself.
     */
    public AnalyzeTextOptions setTokenizer(LexicalTokenizerName tokenizer) {
        this.tokenizer = tokenizer;
        return this;
    }

    /**
     * Get the tokenFilters property: An optional list of token filters to use
     * when breaking the given text. This parameter can only be set when using
     * the tokenizer parameter.
     *
     * @return the tokenFilters value.
     */
    public List<TokenFilterName> getTokenFilters() {
        return this.tokenFilters;
    }

    /**
     * Set the tokenFilters property: An optional list of token filters to use
     * when breaking the given text. This parameter can only be set when using
     * the tokenizer parameter.
     *
     * @param tokenFilters the tokenFilters value to set.
     * @return the AnalyzeRequest object itself.
     */
    public AnalyzeTextOptions setTokenFilters(List<TokenFilterName> tokenFilters) {
        this.tokenFilters = tokenFilters;
        return this;
    }

    /**
     * Get the charFilters property: An optional list of character filters to
     * use when breaking the given text. This parameter can only be set when
     * using the tokenizer parameter.
     *
     * @return the charFilters value.
     */
    public List<CharFilterName> getCharFilters() {
        return this.charFilters;
    }

    /**
     * Set the charFilters property: An optional list of character filters to
     * use when breaking the given text. This parameter can only be set when
     * using the tokenizer parameter.
     *
     * @param charFilters the charFilters value to set.
     * @return the AnalyzeRequest object itself.
     */
    public AnalyzeTextOptions setCharFilters(List<CharFilterName> charFilters) {
        this.charFilters = charFilters;
        return this;
    }
}
