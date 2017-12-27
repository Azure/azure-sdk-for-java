/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for Currency.
 */
public final class Currency extends ExpandableStringEnum<Currency> {
    /** Static value USD for Currency. */
    public static final Currency USD = fromString("USD");

    /** Static value CAD for Currency. */
    public static final Currency CAD = fromString("CAD");

    /** Static value GBP for Currency. */
    public static final Currency GBP = fromString("GBP");

    /** Static value EUR for Currency. */
    public static final Currency EUR = fromString("EUR");

    /** Static value COP for Currency. */
    public static final Currency COP = fromString("COP");

    /** Static value JPY for Currency. */
    public static final Currency JPY = fromString("JPY");

    /** Static value CNY for Currency. */
    public static final Currency CNY = fromString("CNY");

    /** Static value AUD for Currency. */
    public static final Currency AUD = fromString("AUD");

    /** Static value INR for Currency. */
    public static final Currency INR = fromString("INR");

    /** Static value AED for Currency. */
    public static final Currency AED = fromString("AED");

    /** Static value AFN for Currency. */
    public static final Currency AFN = fromString("AFN");

    /** Static value ALL for Currency. */
    public static final Currency ALL = fromString("ALL");

    /** Static value AMD for Currency. */
    public static final Currency AMD = fromString("AMD");

    /** Static value ANG for Currency. */
    public static final Currency ANG = fromString("ANG");

    /** Static value AOA for Currency. */
    public static final Currency AOA = fromString("AOA");

    /** Static value ARS for Currency. */
    public static final Currency ARS = fromString("ARS");

    /** Static value AWG for Currency. */
    public static final Currency AWG = fromString("AWG");

    /** Static value AZN for Currency. */
    public static final Currency AZN = fromString("AZN");

    /** Static value BAM for Currency. */
    public static final Currency BAM = fromString("BAM");

    /** Static value BBD for Currency. */
    public static final Currency BBD = fromString("BBD");

    /** Static value BDT for Currency. */
    public static final Currency BDT = fromString("BDT");

    /** Static value BGN for Currency. */
    public static final Currency BGN = fromString("BGN");

    /** Static value BHD for Currency. */
    public static final Currency BHD = fromString("BHD");

    /** Static value BIF for Currency. */
    public static final Currency BIF = fromString("BIF");

    /** Static value BMD for Currency. */
    public static final Currency BMD = fromString("BMD");

    /** Static value BND for Currency. */
    public static final Currency BND = fromString("BND");

    /** Static value BOB for Currency. */
    public static final Currency BOB = fromString("BOB");

    /** Static value BOV for Currency. */
    public static final Currency BOV = fromString("BOV");

    /** Static value BRL for Currency. */
    public static final Currency BRL = fromString("BRL");

    /** Static value BSD for Currency. */
    public static final Currency BSD = fromString("BSD");

    /** Static value BTN for Currency. */
    public static final Currency BTN = fromString("BTN");

    /** Static value BWP for Currency. */
    public static final Currency BWP = fromString("BWP");

    /** Static value BYR for Currency. */
    public static final Currency BYR = fromString("BYR");

    /** Static value BZD for Currency. */
    public static final Currency BZD = fromString("BZD");

    /** Static value CDF for Currency. */
    public static final Currency CDF = fromString("CDF");

    /** Static value CHE for Currency. */
    public static final Currency CHE = fromString("CHE");

    /** Static value CHF for Currency. */
    public static final Currency CHF = fromString("CHF");

    /** Static value CHW for Currency. */
    public static final Currency CHW = fromString("CHW");

    /** Static value CLF for Currency. */
    public static final Currency CLF = fromString("CLF");

    /** Static value CLP for Currency. */
    public static final Currency CLP = fromString("CLP");

    /** Static value COU for Currency. */
    public static final Currency COU = fromString("COU");

    /** Static value CRC for Currency. */
    public static final Currency CRC = fromString("CRC");

    /** Static value CUC for Currency. */
    public static final Currency CUC = fromString("CUC");

    /** Static value CUP for Currency. */
    public static final Currency CUP = fromString("CUP");

    /** Static value CVE for Currency. */
    public static final Currency CVE = fromString("CVE");

    /** Static value CZK for Currency. */
    public static final Currency CZK = fromString("CZK");

    /** Static value DJF for Currency. */
    public static final Currency DJF = fromString("DJF");

    /** Static value DKK for Currency. */
    public static final Currency DKK = fromString("DKK");

    /** Static value DOP for Currency. */
    public static final Currency DOP = fromString("DOP");

    /** Static value DZD for Currency. */
    public static final Currency DZD = fromString("DZD");

    /** Static value EGP for Currency. */
    public static final Currency EGP = fromString("EGP");

    /** Static value ERN for Currency. */
    public static final Currency ERN = fromString("ERN");

    /** Static value ETB for Currency. */
    public static final Currency ETB = fromString("ETB");

    /** Static value FJD for Currency. */
    public static final Currency FJD = fromString("FJD");

    /** Static value FKP for Currency. */
    public static final Currency FKP = fromString("FKP");

    /** Static value GEL for Currency. */
    public static final Currency GEL = fromString("GEL");

    /** Static value GHS for Currency. */
    public static final Currency GHS = fromString("GHS");

    /** Static value GIP for Currency. */
    public static final Currency GIP = fromString("GIP");

    /** Static value GMD for Currency. */
    public static final Currency GMD = fromString("GMD");

    /** Static value GNF for Currency. */
    public static final Currency GNF = fromString("GNF");

    /** Static value GTQ for Currency. */
    public static final Currency GTQ = fromString("GTQ");

    /** Static value GYD for Currency. */
    public static final Currency GYD = fromString("GYD");

    /** Static value HKD for Currency. */
    public static final Currency HKD = fromString("HKD");

    /** Static value HNL for Currency. */
    public static final Currency HNL = fromString("HNL");

    /** Static value HRK for Currency. */
    public static final Currency HRK = fromString("HRK");

    /** Static value HTG for Currency. */
    public static final Currency HTG = fromString("HTG");

    /** Static value HUF for Currency. */
    public static final Currency HUF = fromString("HUF");

    /** Static value IDR for Currency. */
    public static final Currency IDR = fromString("IDR");

    /** Static value ILS for Currency. */
    public static final Currency ILS = fromString("ILS");

    /** Static value IQD for Currency. */
    public static final Currency IQD = fromString("IQD");

    /** Static value IRR for Currency. */
    public static final Currency IRR = fromString("IRR");

    /** Static value ISK for Currency. */
    public static final Currency ISK = fromString("ISK");

    /** Static value JMD for Currency. */
    public static final Currency JMD = fromString("JMD");

    /** Static value JOD for Currency. */
    public static final Currency JOD = fromString("JOD");

    /** Static value KES for Currency. */
    public static final Currency KES = fromString("KES");

    /** Static value KGS for Currency. */
    public static final Currency KGS = fromString("KGS");

    /** Static value KHR for Currency. */
    public static final Currency KHR = fromString("KHR");

    /** Static value KMF for Currency. */
    public static final Currency KMF = fromString("KMF");

    /** Static value KPW for Currency. */
    public static final Currency KPW = fromString("KPW");

    /** Static value KRW for Currency. */
    public static final Currency KRW = fromString("KRW");

    /** Static value KWD for Currency. */
    public static final Currency KWD = fromString("KWD");

    /** Static value KYD for Currency. */
    public static final Currency KYD = fromString("KYD");

    /** Static value KZT for Currency. */
    public static final Currency KZT = fromString("KZT");

    /** Static value LAK for Currency. */
    public static final Currency LAK = fromString("LAK");

    /** Static value LBP for Currency. */
    public static final Currency LBP = fromString("LBP");

    /** Static value LKR for Currency. */
    public static final Currency LKR = fromString("LKR");

    /** Static value LRD for Currency. */
    public static final Currency LRD = fromString("LRD");

    /** Static value LSL for Currency. */
    public static final Currency LSL = fromString("LSL");

    /** Static value LYD for Currency. */
    public static final Currency LYD = fromString("LYD");

    /** Static value MAD for Currency. */
    public static final Currency MAD = fromString("MAD");

    /** Static value MDL for Currency. */
    public static final Currency MDL = fromString("MDL");

    /** Static value MGA for Currency. */
    public static final Currency MGA = fromString("MGA");

    /** Static value MKD for Currency. */
    public static final Currency MKD = fromString("MKD");

    /** Static value MMK for Currency. */
    public static final Currency MMK = fromString("MMK");

    /** Static value MNT for Currency. */
    public static final Currency MNT = fromString("MNT");

    /** Static value MOP for Currency. */
    public static final Currency MOP = fromString("MOP");

    /** Static value MRO for Currency. */
    public static final Currency MRO = fromString("MRO");

    /** Static value MUR for Currency. */
    public static final Currency MUR = fromString("MUR");

    /** Static value MVR for Currency. */
    public static final Currency MVR = fromString("MVR");

    /** Static value MWK for Currency. */
    public static final Currency MWK = fromString("MWK");

    /** Static value MXN for Currency. */
    public static final Currency MXN = fromString("MXN");

    /** Static value MXV for Currency. */
    public static final Currency MXV = fromString("MXV");

    /** Static value MYR for Currency. */
    public static final Currency MYR = fromString("MYR");

    /** Static value MZN for Currency. */
    public static final Currency MZN = fromString("MZN");

    /** Static value NAD for Currency. */
    public static final Currency NAD = fromString("NAD");

    /** Static value NGN for Currency. */
    public static final Currency NGN = fromString("NGN");

    /** Static value NIO for Currency. */
    public static final Currency NIO = fromString("NIO");

    /** Static value NOK for Currency. */
    public static final Currency NOK = fromString("NOK");

    /** Static value NPR for Currency. */
    public static final Currency NPR = fromString("NPR");

    /** Static value NZD for Currency. */
    public static final Currency NZD = fromString("NZD");

    /** Static value OMR for Currency. */
    public static final Currency OMR = fromString("OMR");

    /** Static value PAB for Currency. */
    public static final Currency PAB = fromString("PAB");

    /** Static value PEN for Currency. */
    public static final Currency PEN = fromString("PEN");

    /** Static value PGK for Currency. */
    public static final Currency PGK = fromString("PGK");

    /** Static value PHP for Currency. */
    public static final Currency PHP = fromString("PHP");

    /** Static value PKR for Currency. */
    public static final Currency PKR = fromString("PKR");

    /** Static value PLN for Currency. */
    public static final Currency PLN = fromString("PLN");

    /** Static value PYG for Currency. */
    public static final Currency PYG = fromString("PYG");

    /** Static value QAR for Currency. */
    public static final Currency QAR = fromString("QAR");

    /** Static value RON for Currency. */
    public static final Currency RON = fromString("RON");

    /** Static value RSD for Currency. */
    public static final Currency RSD = fromString("RSD");

    /** Static value RUB for Currency. */
    public static final Currency RUB = fromString("RUB");

    /** Static value RWF for Currency. */
    public static final Currency RWF = fromString("RWF");

    /** Static value SAR for Currency. */
    public static final Currency SAR = fromString("SAR");

    /** Static value SBD for Currency. */
    public static final Currency SBD = fromString("SBD");

    /** Static value SCR for Currency. */
    public static final Currency SCR = fromString("SCR");

    /** Static value SDG for Currency. */
    public static final Currency SDG = fromString("SDG");

    /** Static value SEK for Currency. */
    public static final Currency SEK = fromString("SEK");

    /** Static value SGD for Currency. */
    public static final Currency SGD = fromString("SGD");

    /** Static value SHP for Currency. */
    public static final Currency SHP = fromString("SHP");

    /** Static value SLL for Currency. */
    public static final Currency SLL = fromString("SLL");

    /** Static value SOS for Currency. */
    public static final Currency SOS = fromString("SOS");

    /** Static value SRD for Currency. */
    public static final Currency SRD = fromString("SRD");

    /** Static value SSP for Currency. */
    public static final Currency SSP = fromString("SSP");

    /** Static value STD for Currency. */
    public static final Currency STD = fromString("STD");

    /** Static value SYP for Currency. */
    public static final Currency SYP = fromString("SYP");

    /** Static value SZL for Currency. */
    public static final Currency SZL = fromString("SZL");

    /** Static value THB for Currency. */
    public static final Currency THB = fromString("THB");

    /** Static value TJS for Currency. */
    public static final Currency TJS = fromString("TJS");

    /** Static value TMT for Currency. */
    public static final Currency TMT = fromString("TMT");

    /** Static value TND for Currency. */
    public static final Currency TND = fromString("TND");

    /** Static value TOP for Currency. */
    public static final Currency TOP = fromString("TOP");

    /** Static value TRY for Currency. */
    public static final Currency TRY = fromString("TRY");

    /** Static value TTD for Currency. */
    public static final Currency TTD = fromString("TTD");

    /** Static value TWD for Currency. */
    public static final Currency TWD = fromString("TWD");

    /** Static value TZS for Currency. */
    public static final Currency TZS = fromString("TZS");

    /** Static value UAH for Currency. */
    public static final Currency UAH = fromString("UAH");

    /** Static value UGX for Currency. */
    public static final Currency UGX = fromString("UGX");

    /** Static value UYU for Currency. */
    public static final Currency UYU = fromString("UYU");

    /** Static value UZS for Currency. */
    public static final Currency UZS = fromString("UZS");

    /** Static value VEF for Currency. */
    public static final Currency VEF = fromString("VEF");

    /** Static value VND for Currency. */
    public static final Currency VND = fromString("VND");

    /** Static value VUV for Currency. */
    public static final Currency VUV = fromString("VUV");

    /** Static value WST for Currency. */
    public static final Currency WST = fromString("WST");

    /** Static value XAF for Currency. */
    public static final Currency XAF = fromString("XAF");

    /** Static value XCD for Currency. */
    public static final Currency XCD = fromString("XCD");

    /** Static value XOF for Currency. */
    public static final Currency XOF = fromString("XOF");

    /** Static value XPF for Currency. */
    public static final Currency XPF = fromString("XPF");

    /** Static value YER for Currency. */
    public static final Currency YER = fromString("YER");

    /** Static value ZAR for Currency. */
    public static final Currency ZAR = fromString("ZAR");

    /** Static value ZMW for Currency. */
    public static final Currency ZMW = fromString("ZMW");

    /**
     * Creates or finds a Currency from its string representation.
     * @param name a name to look for
     * @return the corresponding Currency
     */
    @JsonCreator
    public static Currency fromString(String name) {
        return fromString(name, Currency.class);
    }

    /**
     * @return known Currency values
     */
    public static Collection<Currency> values() {
        return values(Currency.class);
    }
}
