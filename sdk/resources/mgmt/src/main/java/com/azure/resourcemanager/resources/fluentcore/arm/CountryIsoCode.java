// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for country codes in ISO standard.
 * E.g. the country code for United Kingdom is 'GB'.
 */
public final class CountryIsoCode extends ExpandableStringEnum<CountryIsoCode> {
    // CHECKSTYLE IGNORE Javadoc FOR NEXT 237 LINES
    public static final CountryIsoCode AFGHANISTAN = fromString("AF");
    public static final CountryIsoCode ALBANIA = fromString("AL");
    public static final CountryIsoCode ALGERIA = fromString("DZ");
    public static final CountryIsoCode AMERICAN_SAMOA = fromString("AS");
    public static final CountryIsoCode ANDORRA = fromString("AD");
    public static final CountryIsoCode ANGOLA = fromString("AO");
    public static final CountryIsoCode ANGUILLA = fromString("AI");
    public static final CountryIsoCode ANTARCTICA = fromString("AQ");
    public static final CountryIsoCode ANTIGUA_AND_BARBUDA = fromString("AG");
    public static final CountryIsoCode ARGENTINA = fromString("AR");
    public static final CountryIsoCode ARMENIA = fromString("AM");
    public static final CountryIsoCode ARUBA = fromString("AW");
    public static final CountryIsoCode AUSTRALIA = fromString("AU");
    public static final CountryIsoCode AUSTRIA = fromString("AT");
    public static final CountryIsoCode AZERBAIJAN = fromString("AZ");
    public static final CountryIsoCode BAHAMAS = fromString("BS");
    public static final CountryIsoCode BAHRAIN = fromString("BH");
    public static final CountryIsoCode BANGLADESH = fromString("BD");
    public static final CountryIsoCode BARBADOS = fromString("BB");
    public static final CountryIsoCode BELARUS = fromString("BY");
    public static final CountryIsoCode BELGIUM = fromString("BE");
    public static final CountryIsoCode BELIZE = fromString("BZ");
    public static final CountryIsoCode BENIN = fromString("BJ");
    public static final CountryIsoCode BERMUDA = fromString("BM");
    public static final CountryIsoCode BHUTAN = fromString("BT");
    public static final CountryIsoCode BOLIVIA = fromString("BO");
    public static final CountryIsoCode BOSNIA_AND_HERZEGOVINA = fromString("BA");
    public static final CountryIsoCode BOTSWANA = fromString("BW");
    public static final CountryIsoCode BOUVET_ISLAND = fromString("BV");
    public static final CountryIsoCode BRAZIL = fromString("BR");
    public static final CountryIsoCode BRITISH_INDIAN_OCEAN_TERRITORY = fromString("IO");
    public static final CountryIsoCode BRUNEI_DARUSSALAM = fromString("BN");
    public static final CountryIsoCode BULGARIA = fromString("BG");
    public static final CountryIsoCode BURKINA_FASO = fromString("BF");
    public static final CountryIsoCode BURUNDI = fromString("BI");
    public static final CountryIsoCode CAMBODIA = fromString("KH");
    public static final CountryIsoCode CAMEROON = fromString("CM");
    public static final CountryIsoCode CANADA = fromString("CA");
    public static final CountryIsoCode CAPE_VERDE = fromString("CV");
    public static final CountryIsoCode CAYMAN_ISLANDS = fromString("KY");
    public static final CountryIsoCode CENTRAL_AFRICAN_REPUBLIC = fromString("CF");
    public static final CountryIsoCode CHAD = fromString("TD");
    public static final CountryIsoCode CHILE = fromString("CL");
    public static final CountryIsoCode CHINA = fromString("CN");
    public static final CountryIsoCode CHRISTMAS_ISLAND = fromString("CX");
    public static final CountryIsoCode COCOS_KEELING_ISLANDS = fromString("CC");
    public static final CountryIsoCode COLOMBIA = fromString("CO");
    public static final CountryIsoCode COMOROS = fromString("KM");
    public static final CountryIsoCode CONGO = fromString("CG");
    public static final CountryIsoCode CONGO_DEMOCRATIC_REPUBLIC = fromString("CD");
    public static final CountryIsoCode COOK_ISLANDS = fromString("CK");
    public static final CountryIsoCode COSTA_RICA = fromString("CR");
    public static final CountryIsoCode IVORY_COAST = fromString("CI");
    public static final CountryIsoCode CROATIA = fromString("HR");
    public static final CountryIsoCode CYPRUS = fromString("CY");
    public static final CountryIsoCode CZECH_REPUBLIC = fromString("CZ");
    public static final CountryIsoCode DENMARK = fromString("DK");
    public static final CountryIsoCode DJIBOUTI = fromString("DJ");
    public static final CountryIsoCode DOMINICA = fromString("DM");
    public static final CountryIsoCode DOMINICAN_REPUBLIC = fromString("DO");
    public static final CountryIsoCode ECUADOR = fromString("EC");
    public static final CountryIsoCode EGYPT = fromString("EG");
    public static final CountryIsoCode EL_SALVADOR = fromString("SV");
    public static final CountryIsoCode EQUATORIAL_GUINEA = fromString("GQ");
    public static final CountryIsoCode ERITREA = fromString("ER");
    public static final CountryIsoCode ESTONIA = fromString("EE");
    public static final CountryIsoCode ETHIOPIA = fromString("ET");
    public static final CountryIsoCode FALKLAND_ISLANDS_MALVINAS = fromString("FK");
    public static final CountryIsoCode FAROE_ISLANDS = fromString("FO");
    public static final CountryIsoCode FIJI = fromString("FJ");
    public static final CountryIsoCode FINLAND = fromString("FI");
    public static final CountryIsoCode FRANCE = fromString("FR");
    public static final CountryIsoCode FRENCH_GUIANA = fromString("GF");
    public static final CountryIsoCode FRENCH_POLYNESIA = fromString("PF");
    public static final CountryIsoCode FRENCH_SOUTHERN_TERRITORIES = fromString("TF");
    public static final CountryIsoCode GABON = fromString("GA");
    public static final CountryIsoCode GAMBIA = fromString("GM");
    public static final CountryIsoCode GEORGIA = fromString("GE");
    public static final CountryIsoCode GERMANY = fromString("DE");
    public static final CountryIsoCode GHANA = fromString("GH");
    public static final CountryIsoCode GIBRALTAR = fromString("GI");
    public static final CountryIsoCode GREECE = fromString("GR");
    public static final CountryIsoCode GREENLAND = fromString("GL");
    public static final CountryIsoCode GRENADA = fromString("GD");
    public static final CountryIsoCode GUADELOUPE = fromString("GP");
    public static final CountryIsoCode GUAM = fromString("GU");
    public static final CountryIsoCode GUATEMALA = fromString("GT");
    public static final CountryIsoCode GUERNSEY = fromString("GG");
    public static final CountryIsoCode GUINEA = fromString("GN");
    public static final CountryIsoCode GUINEA_BISSAU = fromString("GW");
    public static final CountryIsoCode GUYANA = fromString("GY");
    public static final CountryIsoCode HAITI = fromString("HT");
    public static final CountryIsoCode HEARD_ISLAND_MCDONALD_ISLANDS = fromString("HM");
    public static final CountryIsoCode HOLY_SEE_VATICAN_CITY_STATE = fromString("VA");
    public static final CountryIsoCode HONDURAS = fromString("HN");
    public static final CountryIsoCode HONG_KONG = fromString("HK");
    public static final CountryIsoCode HUNGARY = fromString("HU");
    public static final CountryIsoCode ICELAND = fromString("IS");
    public static final CountryIsoCode INDIA = fromString("IN");
    public static final CountryIsoCode INDONESIA = fromString("ID");
    public static final CountryIsoCode IRAQ = fromString("IQ");
    public static final CountryIsoCode IRELAND = fromString("IE");
    public static final CountryIsoCode ISLE_OF_MAN = fromString("IM");
    public static final CountryIsoCode ISRAEL = fromString("IL");
    public static final CountryIsoCode ITALY = fromString("IT");
    public static final CountryIsoCode JAMAICA = fromString("JM");
    public static final CountryIsoCode JAPAN = fromString("JP");
    public static final CountryIsoCode JERSEY = fromString("JE");
    public static final CountryIsoCode JORDAN = fromString("JO");
    public static final CountryIsoCode KAZAKHSTAN = fromString("KZ");
    public static final CountryIsoCode KENYA = fromString("KE");
    public static final CountryIsoCode KIRIBATI = fromString("KI");
    public static final CountryIsoCode KOREA = fromString("KR");
    public static final CountryIsoCode KUWAIT = fromString("KW");
    public static final CountryIsoCode KYRGYZSTAN = fromString("KG");
    public static final CountryIsoCode LAO_PEOPLES_DEMOCRATIC_REPUBLIC = fromString("LA");
    public static final CountryIsoCode LATVIA = fromString("LV");
    public static final CountryIsoCode LEBANON = fromString("LB");
    public static final CountryIsoCode LESOTHO = fromString("LS");
    public static final CountryIsoCode LIBERIA = fromString("LR");
    public static final CountryIsoCode STATE_OF_LIBYA = fromString("LY");
    public static final CountryIsoCode LIECHTENSTEIN = fromString("LI");
    public static final CountryIsoCode LITHUANIA = fromString("LT");
    public static final CountryIsoCode LUXEMBOURG = fromString("LU");
    public static final CountryIsoCode MACAO = fromString("MO");
    public static final CountryIsoCode MACEDONIA = fromString("MK");
    public static final CountryIsoCode MADAGASCAR = fromString("MG");
    public static final CountryIsoCode MALAWI = fromString("MW");
    public static final CountryIsoCode MALAYSIA = fromString("MY");
    public static final CountryIsoCode MALDIVES = fromString("MV");
    public static final CountryIsoCode MALI = fromString("ML");
    public static final CountryIsoCode MALTA = fromString("MT");
    public static final CountryIsoCode MARSHALL_ISLANDS = fromString("MH");
    public static final CountryIsoCode MARTINIQUE = fromString("MQ");
    public static final CountryIsoCode MAURITANIA = fromString("MR");
    public static final CountryIsoCode MAURITIUS = fromString("MU");
    public static final CountryIsoCode MAYOTTE = fromString("YT");
    public static final CountryIsoCode MEXICO = fromString("MX");
    public static final CountryIsoCode MICRONESIA_FEDERATED_STATES_OF = fromString("FM");
    public static final CountryIsoCode MOLDOVA = fromString("MD");
    public static final CountryIsoCode MONACO = fromString("MC");
    public static final CountryIsoCode MONGOLIA = fromString("MN");
    public static final CountryIsoCode MONTENEGRO = fromString("ME");
    public static final CountryIsoCode MONTSERRAT = fromString("MS");
    public static final CountryIsoCode MOROCCO = fromString("MA");
    public static final CountryIsoCode MOZAMBIQUE = fromString("MZ");
    public static final CountryIsoCode MYANMAR = fromString("MM");
    public static final CountryIsoCode NAMIBIA = fromString("NA");
    public static final CountryIsoCode NAURU = fromString("NR");
    public static final CountryIsoCode NEPAL = fromString("NP");
    public static final CountryIsoCode NETHERLANDS = fromString("NL");
    public static final CountryIsoCode NEW_CALEDONIA = fromString("NC");
    public static final CountryIsoCode NEW_ZEALAND = fromString("NZ");
    public static final CountryIsoCode NICARAGUA = fromString("NI");
    public static final CountryIsoCode NIGER = fromString("NE");
    public static final CountryIsoCode NIGERIA = fromString("NG");
    public static final CountryIsoCode NIUE = fromString("NU");
    public static final CountryIsoCode NORFOLK_ISLAND = fromString("NF");
    public static final CountryIsoCode NORTHERN_MARIANA_ISLANDS = fromString("MP");
    public static final CountryIsoCode NORWAY = fromString("NO");
    public static final CountryIsoCode OMAN = fromString("OM");
    public static final CountryIsoCode PAKISTAN = fromString("PK");
    public static final CountryIsoCode PALAU = fromString("PW");
    public static final CountryIsoCode PALESTINIAN_TERRITORY_OCCUPIED = fromString("PS");
    public static final CountryIsoCode PANAMA = fromString("PA");
    public static final CountryIsoCode PAPUA_NEW_GUINEA = fromString("PG");
    public static final CountryIsoCode PARAGUAY = fromString("PY");
    public static final CountryIsoCode PERU = fromString("PE");
    public static final CountryIsoCode PHILIPPINES = fromString("PH");
    public static final CountryIsoCode PITCAIRN = fromString("PN");
    public static final CountryIsoCode POLAND = fromString("PL");
    public static final CountryIsoCode PORTUGAL = fromString("PT");
    public static final CountryIsoCode PUERTO_RICO = fromString("PR");
    public static final CountryIsoCode QATAR = fromString("QA");
    public static final CountryIsoCode REUNION = fromString("RE");
    public static final CountryIsoCode ROMANIA = fromString("RO");
    public static final CountryIsoCode RUSSIAN_FEDERATION = fromString("RU");
    public static final CountryIsoCode RWANDA = fromString("RW");
    public static final CountryIsoCode SAINT_HELENA = fromString("SH");
    public static final CountryIsoCode SAINT_KITTS_AND_NEVIS = fromString("KN");
    public static final CountryIsoCode SAINT_LUCIA = fromString("LC");
    public static final CountryIsoCode SAINT_PIERRE_AND_MIQUELON = fromString("PM");
    public static final CountryIsoCode SAINT_VINCENT_AND_GRENADINES = fromString("VC");
    public static final CountryIsoCode SAMOA = fromString("WS");
    public static final CountryIsoCode SAN_MARINO = fromString("SM");
    public static final CountryIsoCode SAO_TOME_AND_PRINCIPE = fromString("ST");
    public static final CountryIsoCode SAUDI_ARABIA = fromString("SA");
    public static final CountryIsoCode SENEGAL = fromString("SN");
    public static final CountryIsoCode SERBIA = fromString("RS");
    public static final CountryIsoCode SEYCHELLES = fromString("SC");
    public static final CountryIsoCode SIERRA_LEONE = fromString("SL");
    public static final CountryIsoCode SINGAPORE = fromString("SG");
    public static final CountryIsoCode SLOVAKIA = fromString("SK");
    public static final CountryIsoCode SLOVENIA = fromString("SI");
    public static final CountryIsoCode SOLOMON_ISLANDS = fromString("SB");
    public static final CountryIsoCode SOMALIA = fromString("SO");
    public static final CountryIsoCode SOUTH_AFRICA = fromString("ZA");
    public static final CountryIsoCode SOUTH_GEORGIA_AND_SANDWICH_ISLAND = fromString("GS");
    public static final CountryIsoCode SPAIN = fromString("ES");
    public static final CountryIsoCode SRI_LANKA = fromString("LK");
    public static final CountryIsoCode SURINAME = fromString("SR");
    public static final CountryIsoCode SVALBARD_AND_JAN_MAYEN = fromString("SJ");
    public static final CountryIsoCode SWAZILAND = fromString("SZ");
    public static final CountryIsoCode SWEDEN = fromString("SE");
    public static final CountryIsoCode SWITZERLAND = fromString("CH");
    public static final CountryIsoCode TAIWAN = fromString("TW");
    public static final CountryIsoCode TAJIKISTAN = fromString("TJ");
    public static final CountryIsoCode TANZANIA = fromString("TZ");
    public static final CountryIsoCode THAILAND = fromString("TH");
    public static final CountryIsoCode TIMOR_LESTE = fromString("TL");
    public static final CountryIsoCode TOGO = fromString("TG");
    public static final CountryIsoCode TOKELAU = fromString("TK");
    public static final CountryIsoCode TONGA = fromString("TO");
    public static final CountryIsoCode TRINIDAD_AND_TOBAGO = fromString("TT");
    public static final CountryIsoCode TUNISIA = fromString("TN");
    public static final CountryIsoCode TURKEY = fromString("TR");
    public static final CountryIsoCode TURKMENISTAN = fromString("TM");
    public static final CountryIsoCode TURKS_AND_CAICOS_ISLANDS = fromString("TC");
    public static final CountryIsoCode TUVALU = fromString("TV");
    public static final CountryIsoCode UGANDA = fromString("UG");
    public static final CountryIsoCode UKRAINE = fromString("UA");
    public static final CountryIsoCode UNITED_ARAB_EMIRATES = fromString("AE");
    public static final CountryIsoCode UNITED_KINGDOM = fromString("GB");
    public static final CountryIsoCode UNITED_STATES = fromString("US");
    public static final CountryIsoCode UNITED_STATES_OUTLYING_ISLANDS = fromString("UM");
    public static final CountryIsoCode URUGUAY = fromString("UY");
    public static final CountryIsoCode UZBEKISTAN = fromString("UZ");
    public static final CountryIsoCode VANUATU = fromString("VU");
    public static final CountryIsoCode VENEZUELA = fromString("VE");
    public static final CountryIsoCode VIETNAM = fromString("VN");
    public static final CountryIsoCode VIRGIN_ISLANDS_BRITISH = fromString("VG");
    public static final CountryIsoCode VIRGIN_ISLANDS_US = fromString("VI");
    public static final CountryIsoCode WALLIS_AND_FUTUNA = fromString("WF");
    public static final CountryIsoCode WESTERN_SAHARA = fromString("EH");
    public static final CountryIsoCode YEMEN = fromString("YE");
    public static final CountryIsoCode ZAMBIA = fromString("ZM");
    public static final CountryIsoCode ZIMBABWE = fromString("ZW");

    /**
     * @return known country ISO codes
     */
    public static Collection<CountryIsoCode> values() {
        return values(CountryIsoCode.class);
    }

    /**
     * Creates or finds a CountryIsoCode based on the specified code.
     *
     * @param code a country ISO code
     * @return a CountryIsoCode
     */
    public static CountryIsoCode fromString(String code) {
        return fromString(code, CountryIsoCode.class);
    }
}
