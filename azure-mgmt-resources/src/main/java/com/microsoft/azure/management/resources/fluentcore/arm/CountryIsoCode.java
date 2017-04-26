/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for country codes in ISO standard.
 * E.g. the country code for United Kingdom is 'GB'.
 */
public final class CountryIsoCode {
    // CHECKSTYLE IGNORE Javadoc FOR NEXT 237 LINES
    public static final CountryIsoCode AFGHANISTAN = new CountryIsoCode("AF");
    public static final CountryIsoCode ALBANIA = new CountryIsoCode("AL");
    public static final CountryIsoCode ALGERIA = new CountryIsoCode("DZ");
    public static final CountryIsoCode AMERICAN_SAMOA = new CountryIsoCode("AS");
    public static final CountryIsoCode ANDORRA = new CountryIsoCode("AD");
    public static final CountryIsoCode ANGOLA = new CountryIsoCode("AO");
    public static final CountryIsoCode ANGUILLA = new CountryIsoCode("AI");
    public static final CountryIsoCode ANTARCTICA = new CountryIsoCode("AQ");
    public static final CountryIsoCode ANTIGUA_AND_BARBUDA = new CountryIsoCode("AG");
    public static final CountryIsoCode ARGENTINA = new CountryIsoCode("AR");
    public static final CountryIsoCode ARMENIA = new CountryIsoCode("AM");
    public static final CountryIsoCode ARUBA = new CountryIsoCode("AW");
    public static final CountryIsoCode AUSTRALIA = new CountryIsoCode("AU");
    public static final CountryIsoCode AUSTRIA = new CountryIsoCode("AT");
    public static final CountryIsoCode AZERBAIJAN = new CountryIsoCode("AZ");
    public static final CountryIsoCode BAHAMAS = new CountryIsoCode("BS");
    public static final CountryIsoCode BAHRAIN = new CountryIsoCode("BH");
    public static final CountryIsoCode BANGLADESH = new CountryIsoCode("BD");
    public static final CountryIsoCode BARBADOS = new CountryIsoCode("BB");
    public static final CountryIsoCode BELARUS = new CountryIsoCode("BY");
    public static final CountryIsoCode BELGIUM = new CountryIsoCode("BE");
    public static final CountryIsoCode BELIZE = new CountryIsoCode("BZ");
    public static final CountryIsoCode BENIN = new CountryIsoCode("BJ");
    public static final CountryIsoCode BERMUDA = new CountryIsoCode("BM");
    public static final CountryIsoCode BHUTAN = new CountryIsoCode("BT");
    public static final CountryIsoCode BOLIVIA = new CountryIsoCode("BO");
    public static final CountryIsoCode BOSNIA_AND_HERZEGOVINA = new CountryIsoCode("BA");
    public static final CountryIsoCode BOTSWANA = new CountryIsoCode("BW");
    public static final CountryIsoCode BOUVET_ISLAND = new CountryIsoCode("BV");
    public static final CountryIsoCode BRAZIL = new CountryIsoCode("BR");
    public static final CountryIsoCode BRITISH_INDIAN_OCEAN_TERRITORY = new CountryIsoCode("IO");
    public static final CountryIsoCode BRUNEI_DARUSSALAM = new CountryIsoCode("BN");
    public static final CountryIsoCode BULGARIA = new CountryIsoCode("BG");
    public static final CountryIsoCode BURKINA_FASO = new CountryIsoCode("BF");
    public static final CountryIsoCode BURUNDI = new CountryIsoCode("BI");
    public static final CountryIsoCode CAMBODIA = new CountryIsoCode("KH");
    public static final CountryIsoCode CAMEROON = new CountryIsoCode("CM");
    public static final CountryIsoCode CANADA = new CountryIsoCode("CA");
    public static final CountryIsoCode CAPE_VERDE = new CountryIsoCode("CV");
    public static final CountryIsoCode CAYMAN_ISLANDS = new CountryIsoCode("KY");
    public static final CountryIsoCode CENTRAL_AFRICAN_REPUBLIC = new CountryIsoCode("CF");
    public static final CountryIsoCode CHAD = new CountryIsoCode("TD");
    public static final CountryIsoCode CHILE = new CountryIsoCode("CL");
    public static final CountryIsoCode CHINA = new CountryIsoCode("CN");
    public static final CountryIsoCode CHRISTMAS_ISLAND = new CountryIsoCode("CX");
    public static final CountryIsoCode COCOS_KEELING_ISLANDS = new CountryIsoCode("CC");
    public static final CountryIsoCode COLOMBIA = new CountryIsoCode("CO");
    public static final CountryIsoCode COMOROS = new CountryIsoCode("KM");
    public static final CountryIsoCode CONGO = new CountryIsoCode("CG");
    public static final CountryIsoCode CONGO_DEMOCRATIC_REPUBLIC = new CountryIsoCode("CD");
    public static final CountryIsoCode COOK_ISLANDS = new CountryIsoCode("CK");
    public static final CountryIsoCode COSTA_RICA = new CountryIsoCode("CR");
    public static final CountryIsoCode IVORY_COAST = new CountryIsoCode("CI");
    public static final CountryIsoCode CROATIA = new CountryIsoCode("HR");
    public static final CountryIsoCode CYPRUS = new CountryIsoCode("CY");
    public static final CountryIsoCode CZECH_REPUBLIC = new CountryIsoCode("CZ");
    public static final CountryIsoCode DENMARK = new CountryIsoCode("DK");
    public static final CountryIsoCode DJIBOUTI = new CountryIsoCode("DJ");
    public static final CountryIsoCode DOMINICA = new CountryIsoCode("DM");
    public static final CountryIsoCode DOMINICAN_REPUBLIC = new CountryIsoCode("DO");
    public static final CountryIsoCode ECUADOR = new CountryIsoCode("EC");
    public static final CountryIsoCode EGYPT = new CountryIsoCode("EG");
    public static final CountryIsoCode EL_SALVADOR = new CountryIsoCode("SV");
    public static final CountryIsoCode EQUATORIAL_GUINEA = new CountryIsoCode("GQ");
    public static final CountryIsoCode ERITREA = new CountryIsoCode("ER");
    public static final CountryIsoCode ESTONIA = new CountryIsoCode("EE");
    public static final CountryIsoCode ETHIOPIA = new CountryIsoCode("ET");
    public static final CountryIsoCode FALKLAND_ISLANDS_MALVINAS = new CountryIsoCode("FK");
    public static final CountryIsoCode FAROE_ISLANDS = new CountryIsoCode("FO");
    public static final CountryIsoCode FIJI = new CountryIsoCode("FJ");
    public static final CountryIsoCode FINLAND = new CountryIsoCode("FI");
    public static final CountryIsoCode FRANCE = new CountryIsoCode("FR");
    public static final CountryIsoCode FRENCH_GUIANA = new CountryIsoCode("GF");
    public static final CountryIsoCode FRENCH_POLYNESIA = new CountryIsoCode("PF");
    public static final CountryIsoCode FRENCH_SOUTHERN_TERRITORIES = new CountryIsoCode("TF");
    public static final CountryIsoCode GABON = new CountryIsoCode("GA");
    public static final CountryIsoCode GAMBIA = new CountryIsoCode("GM");
    public static final CountryIsoCode GEORGIA = new CountryIsoCode("GE");
    public static final CountryIsoCode GERMANY = new CountryIsoCode("DE");
    public static final CountryIsoCode GHANA = new CountryIsoCode("GH");
    public static final CountryIsoCode GIBRALTAR = new CountryIsoCode("GI");
    public static final CountryIsoCode GREECE = new CountryIsoCode("GR");
    public static final CountryIsoCode GREENLAND = new CountryIsoCode("GL");
    public static final CountryIsoCode GRENADA = new CountryIsoCode("GD");
    public static final CountryIsoCode GUADELOUPE = new CountryIsoCode("GP");
    public static final CountryIsoCode GUAM = new CountryIsoCode("GU");
    public static final CountryIsoCode GUATEMALA = new CountryIsoCode("GT");
    public static final CountryIsoCode GUERNSEY = new CountryIsoCode("GG");
    public static final CountryIsoCode GUINEA = new CountryIsoCode("GN");
    public static final CountryIsoCode GUINEA_BISSAU = new CountryIsoCode("GW");
    public static final CountryIsoCode GUYANA = new CountryIsoCode("GY");
    public static final CountryIsoCode HAITI = new CountryIsoCode("HT");
    public static final CountryIsoCode HEARD_ISLAND_MCDONALD_ISLANDS = new CountryIsoCode("HM");
    public static final CountryIsoCode HOLY_SEE_VATICAN_CITY_STATE = new CountryIsoCode("VA");
    public static final CountryIsoCode HONDURAS = new CountryIsoCode("HN");
    public static final CountryIsoCode HONG_KONG = new CountryIsoCode("HK");
    public static final CountryIsoCode HUNGARY = new CountryIsoCode("HU");
    public static final CountryIsoCode ICELAND = new CountryIsoCode("IS");
    public static final CountryIsoCode INDIA = new CountryIsoCode("IN");
    public static final CountryIsoCode INDONESIA = new CountryIsoCode("ID");
    public static final CountryIsoCode IRAQ = new CountryIsoCode("IQ");
    public static final CountryIsoCode IRELAND = new CountryIsoCode("IE");
    public static final CountryIsoCode ISLE_OF_MAN = new CountryIsoCode("IM");
    public static final CountryIsoCode ISRAEL = new CountryIsoCode("IL");
    public static final CountryIsoCode ITALY = new CountryIsoCode("IT");
    public static final CountryIsoCode JAMAICA = new CountryIsoCode("JM");
    public static final CountryIsoCode JAPAN = new CountryIsoCode("JP");
    public static final CountryIsoCode JERSEY = new CountryIsoCode("JE");
    public static final CountryIsoCode JORDAN = new CountryIsoCode("JO");
    public static final CountryIsoCode KAZAKHSTAN = new CountryIsoCode("KZ");
    public static final CountryIsoCode KENYA = new CountryIsoCode("KE");
    public static final CountryIsoCode KIRIBATI = new CountryIsoCode("KI");
    public static final CountryIsoCode KOREA = new CountryIsoCode("KR");
    public static final CountryIsoCode KUWAIT = new CountryIsoCode("KW");
    public static final CountryIsoCode KYRGYZSTAN = new CountryIsoCode("KG");
    public static final CountryIsoCode LAO_PEOPLES_DEMOCRATIC_REPUBLIC = new CountryIsoCode("LA");
    public static final CountryIsoCode LATVIA = new CountryIsoCode("LV");
    public static final CountryIsoCode LEBANON = new CountryIsoCode("LB");
    public static final CountryIsoCode LESOTHO = new CountryIsoCode("LS");
    public static final CountryIsoCode LIBERIA = new CountryIsoCode("LR");
    public static final CountryIsoCode STATE_OF_LIBYA = new CountryIsoCode("LY");
    public static final CountryIsoCode LIECHTENSTEIN = new CountryIsoCode("LI");
    public static final CountryIsoCode LITHUANIA = new CountryIsoCode("LT");
    public static final CountryIsoCode LUXEMBOURG = new CountryIsoCode("LU");
    public static final CountryIsoCode MACAO = new CountryIsoCode("MO");
    public static final CountryIsoCode MACEDONIA = new CountryIsoCode("MK");
    public static final CountryIsoCode MADAGASCAR = new CountryIsoCode("MG");
    public static final CountryIsoCode MALAWI = new CountryIsoCode("MW");
    public static final CountryIsoCode MALAYSIA = new CountryIsoCode("MY");
    public static final CountryIsoCode MALDIVES = new CountryIsoCode("MV");
    public static final CountryIsoCode MALI = new CountryIsoCode("ML");
    public static final CountryIsoCode MALTA = new CountryIsoCode("MT");
    public static final CountryIsoCode MARSHALL_ISLANDS = new CountryIsoCode("MH");
    public static final CountryIsoCode MARTINIQUE = new CountryIsoCode("MQ");
    public static final CountryIsoCode MAURITANIA = new CountryIsoCode("MR");
    public static final CountryIsoCode MAURITIUS = new CountryIsoCode("MU");
    public static final CountryIsoCode MAYOTTE = new CountryIsoCode("YT");
    public static final CountryIsoCode MEXICO = new CountryIsoCode("MX");
    public static final CountryIsoCode MICRONESIA_FEDERATED_STATES_OF = new CountryIsoCode("FM");
    public static final CountryIsoCode MOLDOVA = new CountryIsoCode("MD");
    public static final CountryIsoCode MONACO = new CountryIsoCode("MC");
    public static final CountryIsoCode MONGOLIA = new CountryIsoCode("MN");
    public static final CountryIsoCode MONTENEGRO = new CountryIsoCode("ME");
    public static final CountryIsoCode MONTSERRAT = new CountryIsoCode("MS");
    public static final CountryIsoCode MOROCCO = new CountryIsoCode("MA");
    public static final CountryIsoCode MOZAMBIQUE = new CountryIsoCode("MZ");
    public static final CountryIsoCode MYANMAR = new CountryIsoCode("MM");
    public static final CountryIsoCode NAMIBIA = new CountryIsoCode("NA");
    public static final CountryIsoCode NAURU = new CountryIsoCode("NR");
    public static final CountryIsoCode NEPAL = new CountryIsoCode("NP");
    public static final CountryIsoCode NETHERLANDS = new CountryIsoCode("NL");
    public static final CountryIsoCode NEW_CALEDONIA = new CountryIsoCode("NC");
    public static final CountryIsoCode NEW_ZEALAND = new CountryIsoCode("NZ");
    public static final CountryIsoCode NICARAGUA = new CountryIsoCode("NI");
    public static final CountryIsoCode NIGER = new CountryIsoCode("NE");
    public static final CountryIsoCode NIGERIA = new CountryIsoCode("NG");
    public static final CountryIsoCode NIUE = new CountryIsoCode("NU");
    public static final CountryIsoCode NORFOLK_ISLAND = new CountryIsoCode("NF");
    public static final CountryIsoCode NORTHERN_MARIANA_ISLANDS = new CountryIsoCode("MP");
    public static final CountryIsoCode NORWAY = new CountryIsoCode("NO");
    public static final CountryIsoCode OMAN = new CountryIsoCode("OM");
    public static final CountryIsoCode PAKISTAN = new CountryIsoCode("PK");
    public static final CountryIsoCode PALAU = new CountryIsoCode("PW");
    public static final CountryIsoCode PALESTINIAN_TERRITORY_OCCUPIED = new CountryIsoCode("PS");
    public static final CountryIsoCode PANAMA = new CountryIsoCode("PA");
    public static final CountryIsoCode PAPUA_NEW_GUINEA = new CountryIsoCode("PG");
    public static final CountryIsoCode PARAGUAY = new CountryIsoCode("PY");
    public static final CountryIsoCode PERU = new CountryIsoCode("PE");
    public static final CountryIsoCode PHILIPPINES = new CountryIsoCode("PH");
    public static final CountryIsoCode PITCAIRN = new CountryIsoCode("PN");
    public static final CountryIsoCode POLAND = new CountryIsoCode("PL");
    public static final CountryIsoCode PORTUGAL = new CountryIsoCode("PT");
    public static final CountryIsoCode PUERTO_RICO = new CountryIsoCode("PR");
    public static final CountryIsoCode QATAR = new CountryIsoCode("QA");
    public static final CountryIsoCode REUNION = new CountryIsoCode("RE");
    public static final CountryIsoCode ROMANIA = new CountryIsoCode("RO");
    public static final CountryIsoCode RUSSIAN_FEDERATION = new CountryIsoCode("RU");
    public static final CountryIsoCode RWANDA = new CountryIsoCode("RW");
    public static final CountryIsoCode SAINT_HELENA = new CountryIsoCode("SH");
    public static final CountryIsoCode SAINT_KITTS_AND_NEVIS = new CountryIsoCode("KN");
    public static final CountryIsoCode SAINT_LUCIA = new CountryIsoCode("LC");
    public static final CountryIsoCode SAINT_PIERRE_AND_MIQUELON = new CountryIsoCode("PM");
    public static final CountryIsoCode SAINT_VINCENT_AND_GRENADINES = new CountryIsoCode("VC");
    public static final CountryIsoCode SAMOA = new CountryIsoCode("WS");
    public static final CountryIsoCode SAN_MARINO = new CountryIsoCode("SM");
    public static final CountryIsoCode SAO_TOME_AND_PRINCIPE = new CountryIsoCode("ST");
    public static final CountryIsoCode SAUDI_ARABIA = new CountryIsoCode("SA");
    public static final CountryIsoCode SENEGAL = new CountryIsoCode("SN");
    public static final CountryIsoCode SERBIA = new CountryIsoCode("RS");
    public static final CountryIsoCode SEYCHELLES = new CountryIsoCode("SC");
    public static final CountryIsoCode SIERRA_LEONE = new CountryIsoCode("SL");
    public static final CountryIsoCode SINGAPORE = new CountryIsoCode("SG");
    public static final CountryIsoCode SLOVAKIA = new CountryIsoCode("SK");
    public static final CountryIsoCode SLOVENIA = new CountryIsoCode("SI");
    public static final CountryIsoCode SOLOMON_ISLANDS = new CountryIsoCode("SB");
    public static final CountryIsoCode SOMALIA = new CountryIsoCode("SO");
    public static final CountryIsoCode SOUTH_AFRICA = new CountryIsoCode("ZA");
    public static final CountryIsoCode SOUTH_GEORGIA_AND_SANDWICH_ISLAND = new CountryIsoCode("GS");
    public static final CountryIsoCode SPAIN = new CountryIsoCode("ES");
    public static final CountryIsoCode SRI_LANKA = new CountryIsoCode("LK");
    public static final CountryIsoCode SURINAME = new CountryIsoCode("SR");
    public static final CountryIsoCode SVALBARD_AND_JAN_MAYEN = new CountryIsoCode("SJ");
    public static final CountryIsoCode SWAZILAND = new CountryIsoCode("SZ");
    public static final CountryIsoCode SWEDEN = new CountryIsoCode("SE");
    public static final CountryIsoCode SWITZERLAND = new CountryIsoCode("CH");
    public static final CountryIsoCode TAIWAN = new CountryIsoCode("TW");
    public static final CountryIsoCode TAJIKISTAN = new CountryIsoCode("TJ");
    public static final CountryIsoCode TANZANIA = new CountryIsoCode("TZ");
    public static final CountryIsoCode THAILAND = new CountryIsoCode("TH");
    public static final CountryIsoCode TIMOR_LESTE = new CountryIsoCode("TL");
    public static final CountryIsoCode TOGO = new CountryIsoCode("TG");
    public static final CountryIsoCode TOKELAU = new CountryIsoCode("TK");
    public static final CountryIsoCode TONGA = new CountryIsoCode("TO");
    public static final CountryIsoCode TRINIDAD_AND_TOBAGO = new CountryIsoCode("TT");
    public static final CountryIsoCode TUNISIA = new CountryIsoCode("TN");
    public static final CountryIsoCode TURKEY = new CountryIsoCode("TR");
    public static final CountryIsoCode TURKMENISTAN = new CountryIsoCode("TM");
    public static final CountryIsoCode TURKS_AND_CAICOS_ISLANDS = new CountryIsoCode("TC");
    public static final CountryIsoCode TUVALU = new CountryIsoCode("TV");
    public static final CountryIsoCode UGANDA = new CountryIsoCode("UG");
    public static final CountryIsoCode UKRAINE = new CountryIsoCode("UA");
    public static final CountryIsoCode UNITED_ARAB_EMIRATES = new CountryIsoCode("AE");
    public static final CountryIsoCode UNITED_KINGDOM = new CountryIsoCode("GB");
    public static final CountryIsoCode UNITED_STATES = new CountryIsoCode("US");
    public static final CountryIsoCode UNITED_STATES_OUTLYING_ISLANDS = new CountryIsoCode("UM");
    public static final CountryIsoCode URUGUAY = new CountryIsoCode("UY");
    public static final CountryIsoCode UZBEKISTAN = new CountryIsoCode("UZ");
    public static final CountryIsoCode VANUATU = new CountryIsoCode("VU");
    public static final CountryIsoCode VENEZUELA = new CountryIsoCode("VE");
    public static final CountryIsoCode VIETNAM = new CountryIsoCode("VN");
    public static final CountryIsoCode VIRGIN_ISLANDS_BRITISH = new CountryIsoCode("VG");
    public static final CountryIsoCode VIRGIN_ISLANDS_US = new CountryIsoCode("VI");
    public static final CountryIsoCode WALLIS_AND_FUTUNA = new CountryIsoCode("WF");
    public static final CountryIsoCode WESTERN_SAHARA = new CountryIsoCode("EH");
    public static final CountryIsoCode YEMEN = new CountryIsoCode("YE");
    public static final CountryIsoCode ZAMBIA = new CountryIsoCode("ZM");
    public static final CountryIsoCode ZIMBABWE = new CountryIsoCode("ZW");

    private String value;

    /**
     * Creates a custom value for CountryISOCode.
     * @param value the custom value
     */
    public CountryIsoCode(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CountryIsoCode)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        CountryIsoCode rhs = (CountryIsoCode) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
