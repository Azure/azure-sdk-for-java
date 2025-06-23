// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/**
 * Defines market codes for various country/language combinations.
 */
public final class MarketCode extends ExpandableStringEnum<MarketCode> {

    /**
     * Creates a new instance of MarketCode value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    private MarketCode() {
    }

    /** MarketCode 'es-AR' for Spanish - Argentina. */
    public static final MarketCode ES_AR = fromString("es-AR");

    /** MarketCode 'en-AU' for English - Australia. */
    public static final MarketCode EN_AU = fromString("en-AU");

    /** MarketCode 'de-AT' for German - Austria. */
    public static final MarketCode DE_AT = fromString("de-AT");

    /** MarketCode 'nl-BE' for Dutch - Belgium. */
    public static final MarketCode NL_BE = fromString("nl-BE");

    /** MarketCode 'fr-BE' for French - Belgium. */
    public static final MarketCode FR_BE = fromString("fr-BE");

    /** MarketCode 'pt-BR' for Portuguese - Brazil. */
    public static final MarketCode PT_BR = fromString("pt-BR");

    /** MarketCode 'en-CA' for English - Canada. */
    public static final MarketCode EN_CA = fromString("en-CA");

    /** MarketCode 'fr-CA' for French - Canada. */
    public static final MarketCode FR_CA = fromString("fr-CA");

    /** MarketCode 'es-CL' for Spanish - Chile. */
    public static final MarketCode ES_CL = fromString("es-CL");

    /** MarketCode 'da-DK' for Danish - Denmark. */
    public static final MarketCode DA_DK = fromString("da-DK");

    /** MarketCode 'fi-FI' for Finnish - Finland. */
    public static final MarketCode FI_FI = fromString("fi-FI");

    /** MarketCode 'fr-FR' for French - France. */
    public static final MarketCode FR_FR = fromString("fr-FR");

    /** MarketCode 'de-DE' for German - Germany. */
    public static final MarketCode DE_DE = fromString("de-DE");

    /** MarketCode 'zh-HK' for Traditional Chinese - Hong Kong SAR. */
    public static final MarketCode ZH_HK = fromString("zh-HK");

    /** MarketCode 'en-IN' for English - India. */
    public static final MarketCode EN_IN = fromString("en-IN");

    /** MarketCode 'en-ID' for English - Indonesia. */
    public static final MarketCode EN_ID = fromString("en-ID");

    /** MarketCode 'it-IT' for Italian - Italy. */
    public static final MarketCode IT_IT = fromString("it-IT");

    /** MarketCode 'ja-JP' for Japanese - Japan. */
    public static final MarketCode JA_JP = fromString("ja-JP");

    /** MarketCode 'ko-KR' for Korean - Korea. */
    public static final MarketCode KO_KR = fromString("ko-KR");

    /** MarketCode 'en-MY' for English - Malaysia. */
    public static final MarketCode EN_MY = fromString("en-MY");

    /** MarketCode 'es-MX' for Spanish - Mexico. */
    public static final MarketCode ES_MX = fromString("es-MX");

    /** MarketCode 'nl-NL' for Dutch - Netherlands. */
    public static final MarketCode NL_NL = fromString("nl-NL");

    /** MarketCode 'en-NZ' for English - New Zealand. */
    public static final MarketCode EN_NZ = fromString("en-NZ");

    /** MarketCode 'no-NO' for Norwegian - Norway. */
    public static final MarketCode NO_NO = fromString("no-NO");

    /** MarketCode 'zh-CN' for Chinese - People's Republic of China. */
    public static final MarketCode ZH_CN = fromString("zh-CN");

    /** MarketCode 'pl-PL' for Polish - Poland. */
    public static final MarketCode PL_PL = fromString("pl-PL");

    /** MarketCode 'en-PH' for English - Republic of the Philippines. */
    public static final MarketCode EN_PH = fromString("en-PH");

    /** MarketCode 'ru-RU' for Russian - Russia. */
    public static final MarketCode RU_RU = fromString("ru-RU");

    /** MarketCode 'en-ZA' for English - South Africa. */
    public static final MarketCode EN_ZA = fromString("en-ZA");

    /** MarketCode 'es-ES' for Spanish - Spain. */
    public static final MarketCode ES_ES = fromString("es-ES");

    /** MarketCode 'sv-SE' for Swedish - Sweden. */
    public static final MarketCode SV_SE = fromString("sv-SE");

    /** MarketCode 'fr-CH' for French - Switzerland. */
    public static final MarketCode FR_CH = fromString("fr-CH");

    /** MarketCode 'de-CH' for German - Switzerland. */
    public static final MarketCode DE_CH = fromString("de-CH");

    /** MarketCode 'zh-TW' for Traditional Chinese - Taiwan. */
    public static final MarketCode ZH_TW = fromString("zh-TW");

    /** MarketCode 'tr-TR' for Turkish - Türkiye. */
    public static final MarketCode TR_TR = fromString("tr-TR");

    /** MarketCode 'en-GB' for English - United Kingdom. */
    public static final MarketCode EN_GB = fromString("en-GB");

    /** MarketCode 'en-US' for English - United States. */
    public static final MarketCode EN_US = fromString("en-US");

    /** MarketCode 'es-US' for Spanish - United States. */
    public static final MarketCode ES_US = fromString("es-US");

    /**
     * Creates or finds a MarketCode from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding MarketCode.
     */
    @JsonCreator
    public static MarketCode fromString(String name) {
        return fromString(name, MarketCode.class);
    }

    /**
     * Gets known MarketCode values.
     *
     * @return known MarketCode values.
     */
    public static Collection<MarketCode> values() {
        return values(MarketCode.class);
    }
}
