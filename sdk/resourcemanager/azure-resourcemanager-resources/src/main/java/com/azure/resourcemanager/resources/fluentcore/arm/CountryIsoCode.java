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
    /**
     * Country ISO code {@code AF}.
     */
    public static final CountryIsoCode AFGHANISTAN = fromString("AF");
    /**
     * Country ISO code {@code AL}.
     */
    public static final CountryIsoCode ALBANIA = fromString("AL");
    /**
     * Country ISO code {@code DZ}.
     */
    public static final CountryIsoCode ALGERIA = fromString("DZ");
    /**
     * Country ISO code {@code AS}.
     */
    public static final CountryIsoCode AMERICAN_SAMOA = fromString("AS");
    /**
     * Country ISO code {@code AD}.
     */
    public static final CountryIsoCode ANDORRA = fromString("AD");
    /**
     * Country ISO code {@code AO}.
     */
    public static final CountryIsoCode ANGOLA = fromString("AO");
    /**
     * Country ISO code {@code AI}.
     */
    public static final CountryIsoCode ANGUILLA = fromString("AI");
    /**
     * Country ISO code {@code AQ}.
     */
    public static final CountryIsoCode ANTARCTICA = fromString("AQ");
    /**
     * Country ISO code {@code AG}.
     */
    public static final CountryIsoCode ANTIGUA_AND_BARBUDA = fromString("AG");
    /**
     * Country ISO code {@code AR}.
     */
    public static final CountryIsoCode ARGENTINA = fromString("AR");
    /**
     * Country ISO code {@code AM}.
     */
    public static final CountryIsoCode ARMENIA = fromString("AM");
    /**
     * Country ISO code {@code AW}.
     */
    public static final CountryIsoCode ARUBA = fromString("AW");
    /**
     * Country ISO code {@code AU}.
     */
    public static final CountryIsoCode AUSTRALIA = fromString("AU");
    /**
     * Country ISO code {@code AT}.
     */
    public static final CountryIsoCode AUSTRIA = fromString("AT");
    /**
     * Country ISO code {@code AZ}.
     */
    public static final CountryIsoCode AZERBAIJAN = fromString("AZ");
    /**
     * Country ISO code {@code BS}.
     */
    public static final CountryIsoCode BAHAMAS = fromString("BS");
    /**
     * Country ISO code {@code BH}.
     */
    public static final CountryIsoCode BAHRAIN = fromString("BH");
    /**
     * Country ISO code {@code BD}.
     */
    public static final CountryIsoCode BANGLADESH = fromString("BD");
    /**
     * Country ISO code {@code BB}.
     */
    public static final CountryIsoCode BARBADOS = fromString("BB");
    /**
     * Country ISO code {@code BY}.
     */
    public static final CountryIsoCode BELARUS = fromString("BY");
    /**
     * Country ISO code {@code BE}.
     */
    public static final CountryIsoCode BELGIUM = fromString("BE");
    /**
     * Country ISO code {@code BZ}.
     */
    public static final CountryIsoCode BELIZE = fromString("BZ");
    /**
     * Country ISO code {@code BJ}.
     */
    public static final CountryIsoCode BENIN = fromString("BJ");
    /**
     * Country ISO code {@code BM}.
     */
    public static final CountryIsoCode BERMUDA = fromString("BM");
    /**
     * Country ISO code {@code BT}.
     */
    public static final CountryIsoCode BHUTAN = fromString("BT");
    /**
     * Country ISO code {@code BO}.
     */
    public static final CountryIsoCode BOLIVIA = fromString("BO");
    /**
     * Country ISO code {@code BA}.
     */
    public static final CountryIsoCode BOSNIA_AND_HERZEGOVINA = fromString("BA");
    /**
     * Country ISO code {@code BW}.
     */
    public static final CountryIsoCode BOTSWANA = fromString("BW");
    /**
     * Country ISO code {@code BV}.
     */
    public static final CountryIsoCode BOUVET_ISLAND = fromString("BV");
    /**
     * Country ISO code {@code BR}.
     */
    public static final CountryIsoCode BRAZIL = fromString("BR");
    /**
     * Country ISO code {@code IO}.
     */
    public static final CountryIsoCode BRITISH_INDIAN_OCEAN_TERRITORY = fromString("IO");
    /**
     * Country ISO code {@code BN}.
     */
    public static final CountryIsoCode BRUNEI_DARUSSALAM = fromString("BN");
    /**
     * Country ISO code {@code BG}.
     */
    public static final CountryIsoCode BULGARIA = fromString("BG");
    /**
     * Country ISO code {@code BF}.
     */
    public static final CountryIsoCode BURKINA_FASO = fromString("BF");
    /**
     * Country ISO code {@code BI}.
     */
    public static final CountryIsoCode BURUNDI = fromString("BI");
    /**
     * Country ISO code {@code KH}.
     */
    public static final CountryIsoCode CAMBODIA = fromString("KH");
    /**
     * Country ISO code {@code CM}.
     */
    public static final CountryIsoCode CAMEROON = fromString("CM");
    /**
     * Country ISO code {@code CA}.
     */
    public static final CountryIsoCode CANADA = fromString("CA");
    /**
     * Country ISO code {@code CV}.
     */
    public static final CountryIsoCode CAPE_VERDE = fromString("CV");
    /**
     * Country ISO code {@code KY}.
     */
    public static final CountryIsoCode CAYMAN_ISLANDS = fromString("KY");
    /**
     * Country ISO code {@code CF}.
     */
    public static final CountryIsoCode CENTRAL_AFRICAN_REPUBLIC = fromString("CF");
    /**
     * Country ISO code {@code TD}.
     */
    public static final CountryIsoCode CHAD = fromString("TD");
    /**
     * Country ISO code {@code CL}.
     */
    public static final CountryIsoCode CHILE = fromString("CL");
    /**
     * Country ISO code {@code CN}.
     */
    public static final CountryIsoCode CHINA = fromString("CN");
    /**
     * Country ISO code {@code CX}.
     */
    public static final CountryIsoCode CHRISTMAS_ISLAND = fromString("CX");
    /**
     * Country ISO code {@code CC}.
     */
    public static final CountryIsoCode COCOS_KEELING_ISLANDS = fromString("CC");
    /**
     * Country ISO code {@code CO}.
     */
    public static final CountryIsoCode COLOMBIA = fromString("CO");
    /**
     * Country ISO code {@code KM}.
     */
    public static final CountryIsoCode COMOROS = fromString("KM");
    /**
     * Country ISO code {@code CG}.
     */
    public static final CountryIsoCode CONGO = fromString("CG");
    /**
     * Country ISO code {@code CD}.
     */
    public static final CountryIsoCode CONGO_DEMOCRATIC_REPUBLIC = fromString("CD");
    /**
     * Country ISO code {@code CK}.
     */
    public static final CountryIsoCode COOK_ISLANDS = fromString("CK");
    /**
     * Country ISO code {@code CR}.
     */
    public static final CountryIsoCode COSTA_RICA = fromString("CR");
    /**
     * Country ISO code {@code CI}.
     */
    public static final CountryIsoCode IVORY_COAST = fromString("CI");
    /**
     * Country ISO code {@code HR}.
     */
    public static final CountryIsoCode CROATIA = fromString("HR");
    /**
     * Country ISO code {@code CY}.
     */
    public static final CountryIsoCode CYPRUS = fromString("CY");
    /**
     * Country ISO code {@code CZ}.
     */
    public static final CountryIsoCode CZECH_REPUBLIC = fromString("CZ");
    /**
     * Country ISO code {@code DK}.
     */
    public static final CountryIsoCode DENMARK = fromString("DK");
    /**
     * Country ISO code {@code DJ}.
     */
    public static final CountryIsoCode DJIBOUTI = fromString("DJ");
    /**
     * Country ISO code {@code DM}.
     */
    public static final CountryIsoCode DOMINICA = fromString("DM");
    /**
     * Country ISO code {@code DO}.
     */
    public static final CountryIsoCode DOMINICAN_REPUBLIC = fromString("DO");
    /**
     * Country ISO code {@code EC}.
     */
    public static final CountryIsoCode ECUADOR = fromString("EC");
    /**
     * Country ISO code {@code EG}.
     */
    public static final CountryIsoCode EGYPT = fromString("EG");
    /**
     * Country ISO code {@code SV}.
     */
    public static final CountryIsoCode EL_SALVADOR = fromString("SV");
    /**
     * Country ISO code {@code GQ}.
     */
    public static final CountryIsoCode EQUATORIAL_GUINEA = fromString("GQ");
    /**
     * Country ISO code {@code ER}.
     */
    public static final CountryIsoCode ERITREA = fromString("ER");
    /**
     * Country ISO code {@code EE}.
     */
    public static final CountryIsoCode ESTONIA = fromString("EE");
    /**
     * Country ISO code {@code ET}.
     */
    public static final CountryIsoCode ETHIOPIA = fromString("ET");
    /**
     * Country ISO code {@code FK}.
     */
    public static final CountryIsoCode FALKLAND_ISLANDS_MALVINAS = fromString("FK");
    /**
     * Country ISO code {@code FO}.
     */
    public static final CountryIsoCode FAROE_ISLANDS = fromString("FO");
    /**
     * Country ISO code {@code FJ}.
     */
    public static final CountryIsoCode FIJI = fromString("FJ");
    /**
     * Country ISO code {@code FI}.
     */
    public static final CountryIsoCode FINLAND = fromString("FI");
    /**
     * Country ISO code {@code FR}.
     */
    public static final CountryIsoCode FRANCE = fromString("FR");
    /**
     * Country ISO code {@code GF}.
     */
    public static final CountryIsoCode FRENCH_GUIANA = fromString("GF");
    /**
     * Country ISO code {@code PF}.
     */
    public static final CountryIsoCode FRENCH_POLYNESIA = fromString("PF");
    /**
     * Country ISO code {@code TF}.
     */
    public static final CountryIsoCode FRENCH_SOUTHERN_TERRITORIES = fromString("TF");
    /**
     * Country ISO code {@code GA}.
     */
    public static final CountryIsoCode GABON = fromString("GA");
    /**
     * Country ISO code {@code GM}.
     */
    public static final CountryIsoCode GAMBIA = fromString("GM");
    /**
     * Country ISO code {@code GE}.
     */
    public static final CountryIsoCode GEORGIA = fromString("GE");
    /**
     * Country ISO code {@code DE}.
     */
    public static final CountryIsoCode GERMANY = fromString("DE");
    /**
     * Country ISO code {@code GH}.
     */
    public static final CountryIsoCode GHANA = fromString("GH");
    /**
     * Country ISO code {@code GI}.
     */
    public static final CountryIsoCode GIBRALTAR = fromString("GI");
    /**
     * Country ISO code {@code GR}.
     */
    public static final CountryIsoCode GREECE = fromString("GR");
    /**
     * Country ISO code {@code GL}.
     */
    public static final CountryIsoCode GREENLAND = fromString("GL");
    /**
     * Country ISO code {@code GD}.
     */
    public static final CountryIsoCode GRENADA = fromString("GD");
    /**
     * Country ISO code {@code GP}.
     */
    public static final CountryIsoCode GUADELOUPE = fromString("GP");
    /**
     * Country ISO code {@code GU}.
     */
    public static final CountryIsoCode GUAM = fromString("GU");
    /**
     * Country ISO code {@code GT}.
     */
    public static final CountryIsoCode GUATEMALA = fromString("GT");
    /**
     * Country ISO code {@code GG}.
     */
    public static final CountryIsoCode GUERNSEY = fromString("GG");
    /**
     * Country ISO code {@code GN}.
     */
    public static final CountryIsoCode GUINEA = fromString("GN");
    /**
     * Country ISO code {@code GW}.
     */
    public static final CountryIsoCode GUINEA_BISSAU = fromString("GW");
    /**
     * Country ISO code {@code GY}.
     */
    public static final CountryIsoCode GUYANA = fromString("GY");
    /**
     * Country ISO code {@code HT}.
     */
    public static final CountryIsoCode HAITI = fromString("HT");
    /**
     * Country ISO code {@code HM}.
     */
    public static final CountryIsoCode HEARD_ISLAND_MCDONALD_ISLANDS = fromString("HM");
    /**
     * Country ISO code {@code VA}.
     */
    public static final CountryIsoCode HOLY_SEE_VATICAN_CITY_STATE = fromString("VA");
    /**
     * Country ISO code {@code HN}.
     */
    public static final CountryIsoCode HONDURAS = fromString("HN");
    /**
     * Country ISO code {@code HK}.
     */
    public static final CountryIsoCode HONG_KONG = fromString("HK");
    /**
     * Country ISO code {@code HU}.
     */
    public static final CountryIsoCode HUNGARY = fromString("HU");
    /**
     * Country ISO code {@code IS}.
     */
    public static final CountryIsoCode ICELAND = fromString("IS");
    /**
     * Country ISO code {@code IN}.
     */
    public static final CountryIsoCode INDIA = fromString("IN");
    /**
     * Country ISO code {@code ID}.
     */
    public static final CountryIsoCode INDONESIA = fromString("ID");
    /**
     * Country ISO code {@code IQ}.
     */
    public static final CountryIsoCode IRAQ = fromString("IQ");
    /**
     * Country ISO code {@code IE}.
     */
    public static final CountryIsoCode IRELAND = fromString("IE");
    /**
     * Country ISO code {@code IM}.
     */
    public static final CountryIsoCode ISLE_OF_MAN = fromString("IM");
    /**
     * Country ISO code {@code IL}.
     */
    public static final CountryIsoCode ISRAEL = fromString("IL");
    /**
     * Country ISO code {@code IT}.
     */
    public static final CountryIsoCode ITALY = fromString("IT");
    /**
     * Country ISO code {@code JM}.
     */
    public static final CountryIsoCode JAMAICA = fromString("JM");
    /**
     * Country ISO code {@code JP}.
     */
    public static final CountryIsoCode JAPAN = fromString("JP");
    /**
     * Country ISO code {@code JE}.
     */
    public static final CountryIsoCode JERSEY = fromString("JE");
    /**
     * Country ISO code {@code JO}.
     */
    public static final CountryIsoCode JORDAN = fromString("JO");
    /**
     * Country ISO code {@code KZ}.
     */
    public static final CountryIsoCode KAZAKHSTAN = fromString("KZ");
    /**
     * Country ISO code {@code KE}.
     */
    public static final CountryIsoCode KENYA = fromString("KE");
    /**
     * Country ISO code {@code KI}.
     */
    public static final CountryIsoCode KIRIBATI = fromString("KI");
    /**
     * Country ISO code {@code KR}.
     */
    public static final CountryIsoCode KOREA = fromString("KR");
    /**
     * Country ISO code {@code KW}.
     */
    public static final CountryIsoCode KUWAIT = fromString("KW");
    /**
     * Country ISO code {@code KG}.
     */
    public static final CountryIsoCode KYRGYZSTAN = fromString("KG");
    /**
     * Country ISO code {@code LA}.
     */
    public static final CountryIsoCode LAO_PEOPLES_DEMOCRATIC_REPUBLIC = fromString("LA");
    /**
     * Country ISO code {@code LV}.
     */
    public static final CountryIsoCode LATVIA = fromString("LV");
    /**
     * Country ISO code {@code LB}.
     */
    public static final CountryIsoCode LEBANON = fromString("LB");
    /**
     * Country ISO code {@code LS}.
     */
    public static final CountryIsoCode LESOTHO = fromString("LS");
    /**
     * Country ISO code {@code LR}.
     */
    public static final CountryIsoCode LIBERIA = fromString("LR");
    /**
     * Country ISO code {@code LY}.
     */
    public static final CountryIsoCode STATE_OF_LIBYA = fromString("LY");
    /**
     * Country ISO code {@code LI}.
     */
    public static final CountryIsoCode LIECHTENSTEIN = fromString("LI");
    /**
     * Country ISO code {@code LT}.
     */
    public static final CountryIsoCode LITHUANIA = fromString("LT");
    /**
     * Country ISO code {@code LU}.
     */
    public static final CountryIsoCode LUXEMBOURG = fromString("LU");
    /**
     * Country ISO code {@code MO}.
     */
    public static final CountryIsoCode MACAO = fromString("MO");
    /**
     * Country ISO code {@code MK}.
     */
    public static final CountryIsoCode MACEDONIA = fromString("MK");
    /**
     * Country ISO code {@code MG}.
     */
    public static final CountryIsoCode MADAGASCAR = fromString("MG");
    /**
     * Country ISO code {@code MW}.
     */
    public static final CountryIsoCode MALAWI = fromString("MW");
    /**
     * Country ISO code {@code MY}.
     */
    public static final CountryIsoCode MALAYSIA = fromString("MY");
    /**
     * Country ISO code {@code MV}.
     */
    public static final CountryIsoCode MALDIVES = fromString("MV");
    /**
     * Country ISO code {@code ML}.
     */
    public static final CountryIsoCode MALI = fromString("ML");
    /**
     * Country ISO code {@code MT}.
     */
    public static final CountryIsoCode MALTA = fromString("MT");
    /**
     * Country ISO code {@code MH}.
     */
    public static final CountryIsoCode MARSHALL_ISLANDS = fromString("MH");
    /**
     * Country ISO code {@code MQ}.
     */
    public static final CountryIsoCode MARTINIQUE = fromString("MQ");
    /**
     * Country ISO code {@code MR}.
     */
    public static final CountryIsoCode MAURITANIA = fromString("MR");
    /**
     * Country ISO code {@code MU}.
     */
    public static final CountryIsoCode MAURITIUS = fromString("MU");
    /**
     * Country ISO code {@code YT}.
     */
    public static final CountryIsoCode MAYOTTE = fromString("YT");
    /**
     * Country ISO code {@code MX}.
     */
    public static final CountryIsoCode MEXICO = fromString("MX");
    /**
     * Country ISO code {@code FM}.
     */
    public static final CountryIsoCode MICRONESIA_FEDERATED_STATES_OF = fromString("FM");
    /**
     * Country ISO code {@code MD}.
     */
    public static final CountryIsoCode MOLDOVA = fromString("MD");
    /**
     * Country ISO code {@code MC}.
     */
    public static final CountryIsoCode MONACO = fromString("MC");
    /**
     * Country ISO code {@code MN}.
     */
    public static final CountryIsoCode MONGOLIA = fromString("MN");
    /**
     * Country ISO code {@code ME}.
     */
    public static final CountryIsoCode MONTENEGRO = fromString("ME");
    /**
     * Country ISO code {@code MS}.
     */
    public static final CountryIsoCode MONTSERRAT = fromString("MS");
    /**
     * Country ISO code {@code MA}.
     */
    public static final CountryIsoCode MOROCCO = fromString("MA");
    /**
     * Country ISO code {@code MZ}.
     */
    public static final CountryIsoCode MOZAMBIQUE = fromString("MZ");
    /**
     * Country ISO code {@code MM}.
     */
    public static final CountryIsoCode MYANMAR = fromString("MM");
    /**
     * Country ISO code {@code NA}.
     */
    public static final CountryIsoCode NAMIBIA = fromString("NA");
    /**
     * Country ISO code {@code NR}.
     */
    public static final CountryIsoCode NAURU = fromString("NR");
    /**
     * Country ISO code {@code NP}.
     */
    public static final CountryIsoCode NEPAL = fromString("NP");
    /**
     * Country ISO code {@code NL}.
     */
    public static final CountryIsoCode NETHERLANDS = fromString("NL");
    /**
     * Country ISO code {@code NC}.
     */
    public static final CountryIsoCode NEW_CALEDONIA = fromString("NC");
    /**
     * Country ISO code {@code NZ}.
     */
    public static final CountryIsoCode NEW_ZEALAND = fromString("NZ");
    /**
     * Country ISO code {@code NI}.
     */
    public static final CountryIsoCode NICARAGUA = fromString("NI");
    /**
     * Country ISO code {@code NE}.
     */
    public static final CountryIsoCode NIGER = fromString("NE");
    /**
     * Country ISO code {@code NG}.
     */
    public static final CountryIsoCode NIGERIA = fromString("NG");
    /**
     * Country ISO code {@code NU}.
     */
    public static final CountryIsoCode NIUE = fromString("NU");
    /**
     * Country ISO code {@code NF}.
     */
    public static final CountryIsoCode NORFOLK_ISLAND = fromString("NF");
    /**
     * Country ISO code {@code MP}.
     */
    public static final CountryIsoCode NORTHERN_MARIANA_ISLANDS = fromString("MP");
    /**
     * Country ISO code {@code NO}.
     */
    public static final CountryIsoCode NORWAY = fromString("NO");
    /**
     * Country ISO code {@code OM}.
     */
    public static final CountryIsoCode OMAN = fromString("OM");
    /**
     * Country ISO code {@code PK}.
     */
    public static final CountryIsoCode PAKISTAN = fromString("PK");
    /**
     * Country ISO code {@code PW}.
     */
    public static final CountryIsoCode PALAU = fromString("PW");
    /**
     * Country ISO code {@code PS}.
     */
    public static final CountryIsoCode PALESTINIAN_TERRITORY_OCCUPIED = fromString("PS");
    /**
     * Country ISO code {@code PA}.
     */
    public static final CountryIsoCode PANAMA = fromString("PA");
    /**
     * Country ISO code {@code PG}.
     */
    public static final CountryIsoCode PAPUA_NEW_GUINEA = fromString("PG");
    /**
     * Country ISO code {@code PY}.
     */
    public static final CountryIsoCode PARAGUAY = fromString("PY");
    /**
     * Country ISO code {@code PE}.
     */
    public static final CountryIsoCode PERU = fromString("PE");
    /**
     * Country ISO code {@code PH}.
     */
    public static final CountryIsoCode PHILIPPINES = fromString("PH");
    /**
     * Country ISO code {@code PN}.
     */
    public static final CountryIsoCode PITCAIRN = fromString("PN");
    /**
     * Country ISO code {@code PL}.
     */
    public static final CountryIsoCode POLAND = fromString("PL");
    /**
     * Country ISO code {@code PT}.
     */
    public static final CountryIsoCode PORTUGAL = fromString("PT");
    /**
     * Country ISO code {@code PR}.
     */
    public static final CountryIsoCode PUERTO_RICO = fromString("PR");
    /**
     * Country ISO code {@code QA}.
     */
    public static final CountryIsoCode QATAR = fromString("QA");
    /**
     * Country ISO code {@code RE}.
     */
    public static final CountryIsoCode REUNION = fromString("RE");
    /**
     * Country ISO code {@code RO}.
     */
    public static final CountryIsoCode ROMANIA = fromString("RO");
    /**
     * Country ISO code {@code RU}.
     */
    public static final CountryIsoCode RUSSIAN_FEDERATION = fromString("RU");
    /**
     * Country ISO code {@code RW}.
     */
    public static final CountryIsoCode RWANDA = fromString("RW");
    /**
     * Country ISO code {@code SH}.
     */
    public static final CountryIsoCode SAINT_HELENA = fromString("SH");
    /**
     * Country ISO code {@code KN}.
     */
    public static final CountryIsoCode SAINT_KITTS_AND_NEVIS = fromString("KN");
    /**
     * Country ISO code {@code LC}.
     */
    public static final CountryIsoCode SAINT_LUCIA = fromString("LC");
    /**
     * Country ISO code {@code PM}.
     */
    public static final CountryIsoCode SAINT_PIERRE_AND_MIQUELON = fromString("PM");
    /**
     * Country ISO code {@code VC}.
     */
    public static final CountryIsoCode SAINT_VINCENT_AND_GRENADINES = fromString("VC");
    /**
     * Country ISO code {@code WS}.
     */
    public static final CountryIsoCode SAMOA = fromString("WS");
    /**
     * Country ISO code {@code SM}.
     */
    public static final CountryIsoCode SAN_MARINO = fromString("SM");
    /**
     * Country ISO code {@code ST}.
     */
    public static final CountryIsoCode SAO_TOME_AND_PRINCIPE = fromString("ST");
    /**
     * Country ISO code {@code SA}.
     */
    public static final CountryIsoCode SAUDI_ARABIA = fromString("SA");
    /**
     * Country ISO code {@code SN}.
     */
    public static final CountryIsoCode SENEGAL = fromString("SN");
    /**
     * Country ISO code {@code RS}.
     */
    public static final CountryIsoCode SERBIA = fromString("RS");
    /**
     * Country ISO code {@code SC}.
     */
    public static final CountryIsoCode SEYCHELLES = fromString("SC");
    /**
     * Country ISO code {@code SL}.
     */
    public static final CountryIsoCode SIERRA_LEONE = fromString("SL");
    /**
     * Country ISO code {@code SG}.
     */
    public static final CountryIsoCode SINGAPORE = fromString("SG");
    /**
     * Country ISO code {@code SK}.
     */
    public static final CountryIsoCode SLOVAKIA = fromString("SK");
    /**
     * Country ISO code {@code SI}.
     */
    public static final CountryIsoCode SLOVENIA = fromString("SI");
    /**
     * Country ISO code {@code SB}.
     */
    public static final CountryIsoCode SOLOMON_ISLANDS = fromString("SB");
    /**
     * Country ISO code {@code SO}.
     */
    public static final CountryIsoCode SOMALIA = fromString("SO");
    /**
     * Country ISO code {@code ZA}.
     */
    public static final CountryIsoCode SOUTH_AFRICA = fromString("ZA");
    /**
     * Country ISO code {@code GS}.
     */
    public static final CountryIsoCode SOUTH_GEORGIA_AND_SANDWICH_ISLAND = fromString("GS");
    /**
     * Country ISO code {@code ES}.
     */
    public static final CountryIsoCode SPAIN = fromString("ES");
    /**
     * Country ISO code {@code LK}.
     */
    public static final CountryIsoCode SRI_LANKA = fromString("LK");
    /**
     * Country ISO code {@code SR}.
     */
    public static final CountryIsoCode SURINAME = fromString("SR");
    /**
     * Country ISO code {@code SJ}.
     */
    public static final CountryIsoCode SVALBARD_AND_JAN_MAYEN = fromString("SJ");
    /**
     * Country ISO code {@code SZ}.
     */
    public static final CountryIsoCode SWAZILAND = fromString("SZ");
    /**
     * Country ISO code {@code SE}.
     */
    public static final CountryIsoCode SWEDEN = fromString("SE");
    /**
     * Country ISO code {@code CH}.
     */
    public static final CountryIsoCode SWITZERLAND = fromString("CH");
    /**
     * Country ISO code {@code TW}.
     */
    public static final CountryIsoCode TAIWAN = fromString("TW");
    /**
     * Country ISO code {@code TJ}.
     */
    public static final CountryIsoCode TAJIKISTAN = fromString("TJ");
    /**
     * Country ISO code {@code TZ}.
     */
    public static final CountryIsoCode TANZANIA = fromString("TZ");
    /**
     * Country ISO code {@code TH}.
     */
    public static final CountryIsoCode THAILAND = fromString("TH");
    /**
     * Country ISO code {@code TL}.
     */
    public static final CountryIsoCode TIMOR_LESTE = fromString("TL");
    /**
     * Country ISO code {@code TG}.
     */
    public static final CountryIsoCode TOGO = fromString("TG");
    /**
     * Country ISO code {@code TK}.
     */
    public static final CountryIsoCode TOKELAU = fromString("TK");
    /**
     * Country ISO code {@code TO}.
     */
    public static final CountryIsoCode TONGA = fromString("TO");
    /**
     * Country ISO code {@code TT}.
     */
    public static final CountryIsoCode TRINIDAD_AND_TOBAGO = fromString("TT");
    /**
     * Country ISO code {@code TN}.
     */
    public static final CountryIsoCode TUNISIA = fromString("TN");
    /**
     * Country ISO code {@code TR}.
     */
    public static final CountryIsoCode TURKEY = fromString("TR");
    /**
     * Country ISO code {@code TM}.
     */
    public static final CountryIsoCode TURKMENISTAN = fromString("TM");
    /**
     * Country ISO code {@code TC}.
     */
    public static final CountryIsoCode TURKS_AND_CAICOS_ISLANDS = fromString("TC");
    /**
     * Country ISO code {@code TV}.
     */
    public static final CountryIsoCode TUVALU = fromString("TV");
    /**
     * Country ISO code {@code UG}.
     */
    public static final CountryIsoCode UGANDA = fromString("UG");
    /**
     * Country ISO code {@code UA}.
     */
    public static final CountryIsoCode UKRAINE = fromString("UA");
    /**
     * Country ISO code {@code AE}.
     */
    public static final CountryIsoCode UNITED_ARAB_EMIRATES = fromString("AE");
    /**
     * Country ISO code {@code GB}.
     */
    public static final CountryIsoCode UNITED_KINGDOM = fromString("GB");
    /**
     * Country ISO code {@code US}.
     */
    public static final CountryIsoCode UNITED_STATES = fromString("US");
    /**
     * Country ISO code {@code UM}.
     */
    public static final CountryIsoCode UNITED_STATES_OUTLYING_ISLANDS = fromString("UM");
    /**
     * Country ISO code {@code UY}.
     */
    public static final CountryIsoCode URUGUAY = fromString("UY");
    /**
     * Country ISO code {@code UZ}.
     */
    public static final CountryIsoCode UZBEKISTAN = fromString("UZ");
    /**
     * Country ISO code {@code VU}.
     */
    public static final CountryIsoCode VANUATU = fromString("VU");
    /**
     * Country ISO code {@code VE}.
     */
    public static final CountryIsoCode VENEZUELA = fromString("VE");
    /**
     * Country ISO code {@code VN}.
     */
    public static final CountryIsoCode VIETNAM = fromString("VN");
    /**
     * Country ISO code {@code VG}.
     */
    public static final CountryIsoCode VIRGIN_ISLANDS_BRITISH = fromString("VG");
    /**
     * Country ISO code {@code VI}.
     */
    public static final CountryIsoCode VIRGIN_ISLANDS_US = fromString("VI");
    /**
     * Country ISO code {@code WF}.
     */
    public static final CountryIsoCode WALLIS_AND_FUTUNA = fromString("WF");
    /**
     * Country ISO code {@code EH}.
     */
    public static final CountryIsoCode WESTERN_SAHARA = fromString("EH");
    /**
     * Country ISO code {@code YE}.
     */
    public static final CountryIsoCode YEMEN = fromString("YE");
    /**
     * Country ISO code {@code ZM}.
     */
    public static final CountryIsoCode ZAMBIA = fromString("ZM");
    /**
     * Country ISO code {@code ZW}.
     */
    public static final CountryIsoCode ZIMBABWE = fromString("ZW");

    /**
     * Creates a new instance of CountryIsoCode value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public CountryIsoCode() {
    }

    /**
     * Gets known CountryIsoCode values.
     *
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
