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
public final class CountryISOCode {
    // CHECKSTYLE IGNORE Javadoc FOR NEXT 237 LINES
    public static final CountryISOCode AFGHANISTAN = new CountryISOCode("AF");
    public static final CountryISOCode ALBANIA = new CountryISOCode("AL");
    public static final CountryISOCode ALGERIA = new CountryISOCode("DZ");
    public static final CountryISOCode AMERICAN_SAMOA = new CountryISOCode("AS");
    public static final CountryISOCode ANDORRA = new CountryISOCode("AD");
    public static final CountryISOCode ANGOLA = new CountryISOCode("AO");
    public static final CountryISOCode ANGUILLA = new CountryISOCode("AI");
    public static final CountryISOCode ANTARCTICA = new CountryISOCode("AQ");
    public static final CountryISOCode ANTIGUA_AND_BARBUDA = new CountryISOCode("AG");
    public static final CountryISOCode ARGENTINA = new CountryISOCode("AR");
    public static final CountryISOCode ARMENIA = new CountryISOCode("AM");
    public static final CountryISOCode ARUBA = new CountryISOCode("AW");
    public static final CountryISOCode AUSTRALIA = new CountryISOCode("AU");
    public static final CountryISOCode AUSTRIA = new CountryISOCode("AT");
    public static final CountryISOCode AZERBAIJAN = new CountryISOCode("AZ");
    public static final CountryISOCode BAHAMAS = new CountryISOCode("BS");
    public static final CountryISOCode BAHRAIN = new CountryISOCode("BH");
    public static final CountryISOCode BANGLADESH = new CountryISOCode("BD");
    public static final CountryISOCode BARBADOS = new CountryISOCode("BB");
    public static final CountryISOCode BELARUS = new CountryISOCode("BY");
    public static final CountryISOCode BELGIUM = new CountryISOCode("BE");
    public static final CountryISOCode BELIZE = new CountryISOCode("BZ");
    public static final CountryISOCode BENIN = new CountryISOCode("BJ");
    public static final CountryISOCode BERMUDA = new CountryISOCode("BM");
    public static final CountryISOCode BHUTAN = new CountryISOCode("BT");
    public static final CountryISOCode BOLIVIA = new CountryISOCode("BO");
    public static final CountryISOCode BOSNIA_AND_HERZEGOVINA = new CountryISOCode("BA");
    public static final CountryISOCode BOTSWANA = new CountryISOCode("BW");
    public static final CountryISOCode BOUVET_ISLAND = new CountryISOCode("BV");
    public static final CountryISOCode BRAZIL = new CountryISOCode("BR");
    public static final CountryISOCode BRITISH_INDIAN_OCEAN_TERRITORY = new CountryISOCode("IO");
    public static final CountryISOCode BRUNEI_DARUSSALAM = new CountryISOCode("BN");
    public static final CountryISOCode BULGARIA = new CountryISOCode("BG");
    public static final CountryISOCode BURKINA_FASO = new CountryISOCode("BF");
    public static final CountryISOCode BURUNDI = new CountryISOCode("BI");
    public static final CountryISOCode CAMBODIA = new CountryISOCode("KH");
    public static final CountryISOCode CAMEROON = new CountryISOCode("CM");
    public static final CountryISOCode CANADA = new CountryISOCode("CA");
    public static final CountryISOCode CAPE_VERDE = new CountryISOCode("CV");
    public static final CountryISOCode CAYMAN_ISLANDS = new CountryISOCode("KY");
    public static final CountryISOCode CENTRAL_AFRICAN_REPUBLIC = new CountryISOCode("CF");
    public static final CountryISOCode CHAD = new CountryISOCode("TD");
    public static final CountryISOCode CHILE = new CountryISOCode("CL");
    public static final CountryISOCode CHINA = new CountryISOCode("CN");
    public static final CountryISOCode CHRISTMAS_ISLAND = new CountryISOCode("CX");
    public static final CountryISOCode COCOS_KEELING_ISLANDS = new CountryISOCode("CC");
    public static final CountryISOCode COLOMBIA = new CountryISOCode("CO");
    public static final CountryISOCode COMOROS = new CountryISOCode("KM");
    public static final CountryISOCode CONGO = new CountryISOCode("CG");
    public static final CountryISOCode CONGO_DEMOCRATIC_REPUBLIC = new CountryISOCode("CD");
    public static final CountryISOCode COOK_ISLANDS = new CountryISOCode("CK");
    public static final CountryISOCode COSTA_RICA = new CountryISOCode("CR");
    public static final CountryISOCode IVORY_COAST = new CountryISOCode("CI");
    public static final CountryISOCode CROATIA = new CountryISOCode("HR");
    public static final CountryISOCode CYPRUS = new CountryISOCode("CY");
    public static final CountryISOCode CZECH_REPUBLIC = new CountryISOCode("CZ");
    public static final CountryISOCode DENMARK = new CountryISOCode("DK");
    public static final CountryISOCode DJIBOUTI = new CountryISOCode("DJ");
    public static final CountryISOCode DOMINICA = new CountryISOCode("DM");
    public static final CountryISOCode DOMINICAN_REPUBLIC = new CountryISOCode("DO");
    public static final CountryISOCode ECUADOR = new CountryISOCode("EC");
    public static final CountryISOCode EGYPT = new CountryISOCode("EG");
    public static final CountryISOCode EL_SALVADOR = new CountryISOCode("SV");
    public static final CountryISOCode EQUATORIAL_GUINEA = new CountryISOCode("GQ");
    public static final CountryISOCode ERITREA = new CountryISOCode("ER");
    public static final CountryISOCode ESTONIA = new CountryISOCode("EE");
    public static final CountryISOCode ETHIOPIA = new CountryISOCode("ET");
    public static final CountryISOCode FALKLAND_ISLANDS_MALVINAS = new CountryISOCode("FK");
    public static final CountryISOCode FAROE_ISLANDS = new CountryISOCode("FO");
    public static final CountryISOCode FIJI = new CountryISOCode("FJ");
    public static final CountryISOCode FINLAND = new CountryISOCode("FI");
    public static final CountryISOCode FRANCE = new CountryISOCode("FR");
    public static final CountryISOCode FRENCH_GUIANA = new CountryISOCode("GF");
    public static final CountryISOCode FRENCH_POLYNESIA = new CountryISOCode("PF");
    public static final CountryISOCode FRENCH_SOUTHERN_TERRITORIES = new CountryISOCode("TF");
    public static final CountryISOCode GABON = new CountryISOCode("GA");
    public static final CountryISOCode GAMBIA = new CountryISOCode("GM");
    public static final CountryISOCode GEORGIA = new CountryISOCode("GE");
    public static final CountryISOCode GERMANY = new CountryISOCode("DE");
    public static final CountryISOCode GHANA = new CountryISOCode("GH");
    public static final CountryISOCode GIBRALTAR = new CountryISOCode("GI");
    public static final CountryISOCode GREECE = new CountryISOCode("GR");
    public static final CountryISOCode GREENLAND = new CountryISOCode("GL");
    public static final CountryISOCode GRENADA = new CountryISOCode("GD");
    public static final CountryISOCode GUADELOUPE = new CountryISOCode("GP");
    public static final CountryISOCode GUAM = new CountryISOCode("GU");
    public static final CountryISOCode GUATEMALA = new CountryISOCode("GT");
    public static final CountryISOCode GUERNSEY = new CountryISOCode("GG");
    public static final CountryISOCode GUINEA = new CountryISOCode("GN");
    public static final CountryISOCode GUINEA_BISSAU = new CountryISOCode("GW");
    public static final CountryISOCode GUYANA = new CountryISOCode("GY");
    public static final CountryISOCode HAITI = new CountryISOCode("HT");
    public static final CountryISOCode HEARD_ISLAND_MCDONALD_ISLANDS = new CountryISOCode("HM");
    public static final CountryISOCode HOLY_SEE_VATICAN_CITY_STATE = new CountryISOCode("VA");
    public static final CountryISOCode HONDURAS = new CountryISOCode("HN");
    public static final CountryISOCode HONG_KONG = new CountryISOCode("HK");
    public static final CountryISOCode HUNGARY = new CountryISOCode("HU");
    public static final CountryISOCode ICELAND = new CountryISOCode("IS");
    public static final CountryISOCode INDIA = new CountryISOCode("IN");
    public static final CountryISOCode INDONESIA = new CountryISOCode("ID");
    public static final CountryISOCode IRAQ = new CountryISOCode("IQ");
    public static final CountryISOCode IRELAND = new CountryISOCode("IE");
    public static final CountryISOCode ISLE_OF_MAN = new CountryISOCode("IM");
    public static final CountryISOCode ISRAEL = new CountryISOCode("IL");
    public static final CountryISOCode ITALY = new CountryISOCode("IT");
    public static final CountryISOCode JAMAICA = new CountryISOCode("JM");
    public static final CountryISOCode JAPAN = new CountryISOCode("JP");
    public static final CountryISOCode JERSEY = new CountryISOCode("JE");
    public static final CountryISOCode JORDAN = new CountryISOCode("JO");
    public static final CountryISOCode KAZAKHSTAN = new CountryISOCode("KZ");
    public static final CountryISOCode KENYA = new CountryISOCode("KE");
    public static final CountryISOCode KIRIBATI = new CountryISOCode("KI");
    public static final CountryISOCode KOREA = new CountryISOCode("KR");
    public static final CountryISOCode KUWAIT = new CountryISOCode("KW");
    public static final CountryISOCode KYRGYZSTAN = new CountryISOCode("KG");
    public static final CountryISOCode LAO_PEOPLES_DEMOCRATIC_REPUBLIC = new CountryISOCode("LA");
    public static final CountryISOCode LATVIA = new CountryISOCode("LV");
    public static final CountryISOCode LEBANON = new CountryISOCode("LB");
    public static final CountryISOCode LESOTHO = new CountryISOCode("LS");
    public static final CountryISOCode LIBERIA = new CountryISOCode("LR");
    public static final CountryISOCode STATE_OF_LIBYA = new CountryISOCode("LY");
    public static final CountryISOCode LIECHTENSTEIN = new CountryISOCode("LI");
    public static final CountryISOCode LITHUANIA = new CountryISOCode("LT");
    public static final CountryISOCode LUXEMBOURG = new CountryISOCode("LU");
    public static final CountryISOCode MACAO = new CountryISOCode("MO");
    public static final CountryISOCode MACEDONIA = new CountryISOCode("MK");
    public static final CountryISOCode MADAGASCAR = new CountryISOCode("MG");
    public static final CountryISOCode MALAWI = new CountryISOCode("MW");
    public static final CountryISOCode MALAYSIA = new CountryISOCode("MY");
    public static final CountryISOCode MALDIVES = new CountryISOCode("MV");
    public static final CountryISOCode MALI = new CountryISOCode("ML");
    public static final CountryISOCode MALTA = new CountryISOCode("MT");
    public static final CountryISOCode MARSHALL_ISLANDS = new CountryISOCode("MH");
    public static final CountryISOCode MARTINIQUE = new CountryISOCode("MQ");
    public static final CountryISOCode MAURITANIA = new CountryISOCode("MR");
    public static final CountryISOCode MAURITIUS = new CountryISOCode("MU");
    public static final CountryISOCode MAYOTTE = new CountryISOCode("YT");
    public static final CountryISOCode MEXICO = new CountryISOCode("MX");
    public static final CountryISOCode MICRONESIA_FEDERATED_STATES_OF = new CountryISOCode("FM");
    public static final CountryISOCode MOLDOVA = new CountryISOCode("MD");
    public static final CountryISOCode MONACO = new CountryISOCode("MC");
    public static final CountryISOCode MONGOLIA = new CountryISOCode("MN");
    public static final CountryISOCode MONTENEGRO = new CountryISOCode("ME");
    public static final CountryISOCode MONTSERRAT = new CountryISOCode("MS");
    public static final CountryISOCode MOROCCO = new CountryISOCode("MA");
    public static final CountryISOCode MOZAMBIQUE = new CountryISOCode("MZ");
    public static final CountryISOCode MYANMAR = new CountryISOCode("MM");
    public static final CountryISOCode NAMIBIA = new CountryISOCode("NA");
    public static final CountryISOCode NAURU = new CountryISOCode("NR");
    public static final CountryISOCode NEPAL = new CountryISOCode("NP");
    public static final CountryISOCode NETHERLANDS = new CountryISOCode("NL");
    public static final CountryISOCode NEW_CALEDONIA = new CountryISOCode("NC");
    public static final CountryISOCode NEW_ZEALAND = new CountryISOCode("NZ");
    public static final CountryISOCode NICARAGUA = new CountryISOCode("NI");
    public static final CountryISOCode NIGER = new CountryISOCode("NE");
    public static final CountryISOCode NIGERIA = new CountryISOCode("NG");
    public static final CountryISOCode NIUE = new CountryISOCode("NU");
    public static final CountryISOCode NORFOLK_ISLAND = new CountryISOCode("NF");
    public static final CountryISOCode NORTHERN_MARIANA_ISLANDS = new CountryISOCode("MP");
    public static final CountryISOCode NORWAY = new CountryISOCode("NO");
    public static final CountryISOCode OMAN = new CountryISOCode("OM");
    public static final CountryISOCode PAKISTAN = new CountryISOCode("PK");
    public static final CountryISOCode PALAU = new CountryISOCode("PW");
    public static final CountryISOCode PALESTINIAN_TERRITORY_OCCUPIED = new CountryISOCode("PS");
    public static final CountryISOCode PANAMA = new CountryISOCode("PA");
    public static final CountryISOCode PAPUA_NEW_GUINEA = new CountryISOCode("PG");
    public static final CountryISOCode PARAGUAY = new CountryISOCode("PY");
    public static final CountryISOCode PERU = new CountryISOCode("PE");
    public static final CountryISOCode PHILIPPINES = new CountryISOCode("PH");
    public static final CountryISOCode PITCAIRN = new CountryISOCode("PN");
    public static final CountryISOCode POLAND = new CountryISOCode("PL");
    public static final CountryISOCode PORTUGAL = new CountryISOCode("PT");
    public static final CountryISOCode PUERTO_RICO = new CountryISOCode("PR");
    public static final CountryISOCode QATAR = new CountryISOCode("QA");
    public static final CountryISOCode REUNION = new CountryISOCode("RE");
    public static final CountryISOCode ROMANIA = new CountryISOCode("RO");
    public static final CountryISOCode RUSSIAN_FEDERATION = new CountryISOCode("RU");
    public static final CountryISOCode RWANDA = new CountryISOCode("RW");
    public static final CountryISOCode SAINT_HELENA = new CountryISOCode("SH");
    public static final CountryISOCode SAINT_KITTS_AND_NEVIS = new CountryISOCode("KN");
    public static final CountryISOCode SAINT_LUCIA = new CountryISOCode("LC");
    public static final CountryISOCode SAINT_PIERRE_AND_MIQUELON = new CountryISOCode("PM");
    public static final CountryISOCode SAINT_VINCENT_AND_GRENADINES = new CountryISOCode("VC");
    public static final CountryISOCode SAMOA = new CountryISOCode("WS");
    public static final CountryISOCode SAN_MARINO = new CountryISOCode("SM");
    public static final CountryISOCode SAO_TOME_AND_PRINCIPE = new CountryISOCode("ST");
    public static final CountryISOCode SAUDI_ARABIA = new CountryISOCode("SA");
    public static final CountryISOCode SENEGAL = new CountryISOCode("SN");
    public static final CountryISOCode SERBIA = new CountryISOCode("RS");
    public static final CountryISOCode SEYCHELLES = new CountryISOCode("SC");
    public static final CountryISOCode SIERRA_LEONE = new CountryISOCode("SL");
    public static final CountryISOCode SINGAPORE = new CountryISOCode("SG");
    public static final CountryISOCode SLOVAKIA = new CountryISOCode("SK");
    public static final CountryISOCode SLOVENIA = new CountryISOCode("SI");
    public static final CountryISOCode SOLOMON_ISLANDS = new CountryISOCode("SB");
    public static final CountryISOCode SOMALIA = new CountryISOCode("SO");
    public static final CountryISOCode SOUTH_AFRICA = new CountryISOCode("ZA");
    public static final CountryISOCode SOUTH_GEORGIA_AND_SANDWICH_ISLAND = new CountryISOCode("GS");
    public static final CountryISOCode SPAIN = new CountryISOCode("ES");
    public static final CountryISOCode SRI_LANKA = new CountryISOCode("LK");
    public static final CountryISOCode SURINAME = new CountryISOCode("SR");
    public static final CountryISOCode SVALBARD_AND_JAN_MAYEN = new CountryISOCode("SJ");
    public static final CountryISOCode SWAZILAND = new CountryISOCode("SZ");
    public static final CountryISOCode SWEDEN = new CountryISOCode("SE");
    public static final CountryISOCode SWITZERLAND = new CountryISOCode("CH");
    public static final CountryISOCode TAIWAN = new CountryISOCode("TW");
    public static final CountryISOCode TAJIKISTAN = new CountryISOCode("TJ");
    public static final CountryISOCode TANZANIA = new CountryISOCode("TZ");
    public static final CountryISOCode THAILAND = new CountryISOCode("TH");
    public static final CountryISOCode TIMOR_LESTE = new CountryISOCode("TL");
    public static final CountryISOCode TOGO = new CountryISOCode("TG");
    public static final CountryISOCode TOKELAU = new CountryISOCode("TK");
    public static final CountryISOCode TONGA = new CountryISOCode("TO");
    public static final CountryISOCode TRINIDAD_AND_TOBAGO = new CountryISOCode("TT");
    public static final CountryISOCode TUNISIA = new CountryISOCode("TN");
    public static final CountryISOCode TURKEY = new CountryISOCode("TR");
    public static final CountryISOCode TURKMENISTAN = new CountryISOCode("TM");
    public static final CountryISOCode TURKS_AND_CAICOS_ISLANDS = new CountryISOCode("TC");
    public static final CountryISOCode TUVALU = new CountryISOCode("TV");
    public static final CountryISOCode UGANDA = new CountryISOCode("UG");
    public static final CountryISOCode UKRAINE = new CountryISOCode("UA");
    public static final CountryISOCode UNITED_ARAB_EMIRATES = new CountryISOCode("AE");
    public static final CountryISOCode UNITED_KINGDOM = new CountryISOCode("GB");
    public static final CountryISOCode UNITED_STATES = new CountryISOCode("US");
    public static final CountryISOCode UNITED_STATES_OUTLYING_ISLANDS = new CountryISOCode("UM");
    public static final CountryISOCode URUGUAY = new CountryISOCode("UY");
    public static final CountryISOCode UZBEKISTAN = new CountryISOCode("UZ");
    public static final CountryISOCode VANUATU = new CountryISOCode("VU");
    public static final CountryISOCode VENEZUELA = new CountryISOCode("VE");
    public static final CountryISOCode VIETNAM = new CountryISOCode("VN");
    public static final CountryISOCode VIRGIN_ISLANDS_BRITISH = new CountryISOCode("VG");
    public static final CountryISOCode VIRGIN_ISLANDS_US = new CountryISOCode("VI");
    public static final CountryISOCode WALLIS_AND_FUTUNA = new CountryISOCode("WF");
    public static final CountryISOCode WESTERN_SAHARA = new CountryISOCode("EH");
    public static final CountryISOCode YEMEN = new CountryISOCode("YE");
    public static final CountryISOCode ZAMBIA = new CountryISOCode("ZM");
    public static final CountryISOCode ZIMBABWE = new CountryISOCode("ZW");

    private String value;

    /**
     * Creates a custom value for CountryISOCode.
     * @param value the custom value
     */
    public CountryISOCode(String value) {
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
        if (!(obj instanceof CountryISOCode)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        CountryISOCode rhs = (CountryISOCode) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
