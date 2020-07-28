// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for country codes for phone number prefixes.
 * E.g. the country code for +1 123 456 7890 is '+1'.
 */
public final class CountryPhoneCode extends ExpandableStringEnum<CountryPhoneCode> {
    // CHECKSTYLE IGNORE Javadoc FOR NEXT 236 LINES
    public static final CountryPhoneCode ALBANIA = fromString("+355");
    public static final CountryPhoneCode ALGERIA = fromString("+213");
    public static final CountryPhoneCode ANDORRA = fromString("+376");
    public static final CountryPhoneCode ANGOLA = fromString("+244");
    public static final CountryPhoneCode ANTARCTICA = fromString("+672");
    public static final CountryPhoneCode ANTIGUA_AND_BARBUDA = fromString("+1");
    public static final CountryPhoneCode ARGENTINA = fromString("+54");
    public static final CountryPhoneCode ARMENIA = fromString("+374");
    public static final CountryPhoneCode ARUBA = fromString("+297");
    public static final CountryPhoneCode ASCENSION_ISLAND = fromString("+247");
    public static final CountryPhoneCode AUSTRALIA = fromString("+61");
    public static final CountryPhoneCode AUSTRIA = fromString("+43");
    public static final CountryPhoneCode AZERBAIJAN = fromString("+994");
    public static final CountryPhoneCode BAHAMAS = fromString("+1");
    public static final CountryPhoneCode BAHRAIN = fromString("+973");
    public static final CountryPhoneCode BANGLADESH = fromString("+880");
    public static final CountryPhoneCode BARBADOS = fromString("+1");
    public static final CountryPhoneCode BELARUS = fromString("+375");
    public static final CountryPhoneCode BELGIUM = fromString("+32");
    public static final CountryPhoneCode BELIZE = fromString("+501");
    public static final CountryPhoneCode BENIN = fromString("+229");
    public static final CountryPhoneCode BERMUDA = fromString("+1");
    public static final CountryPhoneCode BHUTAN = fromString("+975");
    public static final CountryPhoneCode BOLIVIA = fromString("+591");
    public static final CountryPhoneCode BOSNIA_AND_HERZEGOVINA = fromString("+387");
    public static final CountryPhoneCode BOTSWANA = fromString("+267");
    public static final CountryPhoneCode BOUVET_ISLAND = fromString("+47");
    public static final CountryPhoneCode BRAZIL = fromString("+55");
    public static final CountryPhoneCode BRITISH_INDIAN_OCEAN_TERRITORY = fromString("+44");
    public static final CountryPhoneCode BRUNEI = fromString("+673");
    public static final CountryPhoneCode BULGARIA = fromString("+359");
    public static final CountryPhoneCode BURKINA_FASO = fromString("+226");
    public static final CountryPhoneCode BURUNDI = fromString("+257");
    public static final CountryPhoneCode CABO_VERDE = fromString("+238");
    public static final CountryPhoneCode CAMBODIA = fromString("+855");
    public static final CountryPhoneCode CAMEROON = fromString("+237");
    public static final CountryPhoneCode CANADA = fromString("+1");
    public static final CountryPhoneCode CAYMAN_ISLANDS = fromString("+1");
    public static final CountryPhoneCode CENTRAL_AFRICAN_REPUBLIC = fromString("+236");
    public static final CountryPhoneCode CHAD = fromString("+235");
    public static final CountryPhoneCode CHILE = fromString("+56");
    public static final CountryPhoneCode CHINA = fromString("+86");
    public static final CountryPhoneCode CHRISTMAS_ISLAND = fromString("+61");
    public static final CountryPhoneCode COCOS_KEELING_ISLANDS = fromString("+61");
    public static final CountryPhoneCode COLOMBIA = fromString("+57");
    public static final CountryPhoneCode COMOROS = fromString("+269");
    public static final CountryPhoneCode CONGO = fromString("+242");
    public static final CountryPhoneCode CONGO_DRC = fromString("+243");
    public static final CountryPhoneCode COOK_ISLANDS = fromString("+682");
    public static final CountryPhoneCode COSTA_RICA = fromString("+506");
    public static final CountryPhoneCode CROATIA = fromString("+385");
    public static final CountryPhoneCode CUBA = fromString("+53");
    public static final CountryPhoneCode CYPRUS = fromString("+357");
    public static final CountryPhoneCode CZECH_REPUBLIC = fromString("+420");
    public static final CountryPhoneCode DENMARK = fromString("+45");
    public static final CountryPhoneCode DJIBOUTI = fromString("+253");
    public static final CountryPhoneCode DOMINICA = fromString("+1");
    public static final CountryPhoneCode DOMINICAN_REPUBLIC = fromString("+1");
    public static final CountryPhoneCode ECUADOR = fromString("+593");
    public static final CountryPhoneCode EGYPT = fromString("+20");
    public static final CountryPhoneCode EL_SALVADOR = fromString("+503");
    public static final CountryPhoneCode EQUATORIAL_GUINEA = fromString("+240");
    public static final CountryPhoneCode ERITREA = fromString("+291");
    public static final CountryPhoneCode ESTONIA = fromString("+372");
    public static final CountryPhoneCode ETHIOPIA = fromString("+251");
    public static final CountryPhoneCode FALKLAND_ISLANDS = fromString("+500");
    public static final CountryPhoneCode FAROE_ISLANDS = fromString("+298");
    public static final CountryPhoneCode FIJI_ISLANDS = fromString("+679");
    public static final CountryPhoneCode FINLAND = fromString("+358");
    public static final CountryPhoneCode FRANCE = fromString("+33");
    public static final CountryPhoneCode FRENCH_GUIANA = fromString("+594");
    public static final CountryPhoneCode FRENCH_POLYNESIA = fromString("+689");
    public static final CountryPhoneCode GABON = fromString("+241");
    public static final CountryPhoneCode GAMBIA_THE = fromString("+220");
    public static final CountryPhoneCode GEORGIA = fromString("+995");
    public static final CountryPhoneCode GERMANY = fromString("+49");
    public static final CountryPhoneCode GHANA = fromString("+233");
    public static final CountryPhoneCode GIBRALTAR = fromString("+350");
    public static final CountryPhoneCode GREECE = fromString("+30");
    public static final CountryPhoneCode GREENLAND = fromString("+299");
    public static final CountryPhoneCode GRENADA = fromString("+1");
    public static final CountryPhoneCode GUADELOUPE = fromString("+590");
    public static final CountryPhoneCode GUAM = fromString("+1");
    public static final CountryPhoneCode GUATEMALA = fromString("+502");
    public static final CountryPhoneCode GUERNSEY = fromString("+44");
    public static final CountryPhoneCode GUINEA = fromString("+224");
    public static final CountryPhoneCode GUINEA_BISSAU = fromString("+245");
    public static final CountryPhoneCode GUYANA = fromString("+592");
    public static final CountryPhoneCode HAITI = fromString("+509");
    public static final CountryPhoneCode HOLY_SEE_VATICAN_CITY = fromString("+379");
    public static final CountryPhoneCode HONDURAS = fromString("+504");
    public static final CountryPhoneCode HONG_KONG_SAR = fromString("+852");
    public static final CountryPhoneCode HUNGARY = fromString("+36");
    public static final CountryPhoneCode ICELAND = fromString("+354");
    public static final CountryPhoneCode INDIA = fromString("+91");
    public static final CountryPhoneCode INDONESIA = fromString("+62");
    public static final CountryPhoneCode IRAN = fromString("+98");
    public static final CountryPhoneCode IRAQ = fromString("+964");
    public static final CountryPhoneCode IRELAND = fromString("+353");
    public static final CountryPhoneCode ISLE_OF_MAN = fromString("+44");
    public static final CountryPhoneCode ISRAEL = fromString("+972");
    public static final CountryPhoneCode ITALY = fromString("+39");
    public static final CountryPhoneCode JAMAICA = fromString("+1");
    public static final CountryPhoneCode JAN_MAYEN = fromString("+47");
    public static final CountryPhoneCode JAPAN = fromString("+81");
    public static final CountryPhoneCode JERSEY = fromString("+44");
    public static final CountryPhoneCode JORDAN = fromString("+962");
    public static final CountryPhoneCode KAZAKHSTAN = fromString("+7");
    public static final CountryPhoneCode KENYA = fromString("+254");
    public static final CountryPhoneCode KIRIBATI = fromString("+686");
    public static final CountryPhoneCode KOREA = fromString("+82");
    public static final CountryPhoneCode KUWAIT = fromString("+965");
    public static final CountryPhoneCode KYRGYZSTAN = fromString("+996");
    public static final CountryPhoneCode LAOS = fromString("+856");
    public static final CountryPhoneCode LATVIA = fromString("+371");
    public static final CountryPhoneCode LEBANON = fromString("+961");
    public static final CountryPhoneCode LESOTHO = fromString("+266");
    public static final CountryPhoneCode LIBERIA = fromString("+231");
    public static final CountryPhoneCode LIBYA = fromString("+218");
    public static final CountryPhoneCode LIECHTENSTEIN = fromString("+423");
    public static final CountryPhoneCode LITHUANIA = fromString("+370");
    public static final CountryPhoneCode LUXEMBOURG = fromString("+352");
    public static final CountryPhoneCode MACAO_SAR = fromString("+853");
    public static final CountryPhoneCode MACEDONIA_FORMER_YUGOSLAV_REPUBLIC_OF = fromString("+389");
    public static final CountryPhoneCode MADAGASCAR = fromString("+261");
    public static final CountryPhoneCode MALAWI = fromString("+265");
    public static final CountryPhoneCode MALAYSIA = fromString("+60");
    public static final CountryPhoneCode MALDIVES = fromString("+960");
    public static final CountryPhoneCode MALI = fromString("+223");
    public static final CountryPhoneCode MALTA = fromString("+356");
    public static final CountryPhoneCode MARSHALL_ISLANDS = fromString("+692");
    public static final CountryPhoneCode MARTINIQUE = fromString("+596");
    public static final CountryPhoneCode MAURITANIA = fromString("+222");
    public static final CountryPhoneCode MAURITIUS = fromString("+230");
    public static final CountryPhoneCode MAYOTTE = fromString("+262");
    public static final CountryPhoneCode MEXICO = fromString("+52");
    public static final CountryPhoneCode MICRONESIA = fromString("+691");
    public static final CountryPhoneCode MOLDOVA = fromString("+373");
    public static final CountryPhoneCode MONACO = fromString("+377");
    public static final CountryPhoneCode MONGOLIA = fromString("+976");
    public static final CountryPhoneCode MONTENEGRO = fromString("+382");
    public static final CountryPhoneCode MONTSERRAT = fromString("+1");
    public static final CountryPhoneCode MOROCCO = fromString("+212");
    public static final CountryPhoneCode MOZAMBIQUE = fromString("+258");
    public static final CountryPhoneCode MYANMAR = fromString("+95");
    public static final CountryPhoneCode NAMIBIA = fromString("+264");
    public static final CountryPhoneCode NAURU = fromString("+674");
    public static final CountryPhoneCode NEPAL = fromString("+977");
    public static final CountryPhoneCode NETHERLANDS = fromString("+31");
    public static final CountryPhoneCode NETHERLANDS_ANTILLES_FORMER = fromString("+599");
    public static final CountryPhoneCode NEW_CALEDONIA = fromString("+687");
    public static final CountryPhoneCode NEW_ZEALAND = fromString("+64");
    public static final CountryPhoneCode NICARAGUA = fromString("+505");
    public static final CountryPhoneCode NIGER = fromString("+227");
    public static final CountryPhoneCode NIGERIA = fromString("+234");
    public static final CountryPhoneCode NIUE = fromString("+683");
    public static final CountryPhoneCode NORTH_KOREA = fromString("+850");
    public static final CountryPhoneCode NORTHERN_MARIANA_ISLANDS = fromString("+1");
    public static final CountryPhoneCode NORWAY = fromString("+47");
    public static final CountryPhoneCode OMAN = fromString("+968");
    public static final CountryPhoneCode PAKISTAN = fromString("+92");
    public static final CountryPhoneCode PALAU = fromString("+680");
    public static final CountryPhoneCode PALESTINIAN_AUTHORITY_970 = fromString("+970");
    public static final CountryPhoneCode PALESTINIAN_AUTHORITY_972 = fromString("+972");
    public static final CountryPhoneCode PANAMA = fromString("+507");
    public static final CountryPhoneCode PAPUA_NEW_GUINEA = fromString("+675");
    public static final CountryPhoneCode PARAGUAY = fromString("+595");
    public static final CountryPhoneCode PERU = fromString("+51");
    public static final CountryPhoneCode PHILIPPINES = fromString("+63");
    public static final CountryPhoneCode POLAND = fromString("+48");
    public static final CountryPhoneCode PORTUGAL = fromString("+351");
    public static final CountryPhoneCode QATAR = fromString("+974");
    public static final CountryPhoneCode IVORY_COAST = fromString("+225");
    public static final CountryPhoneCode REUNION = fromString("+262");
    public static final CountryPhoneCode ROMANIA = fromString("+40");
    public static final CountryPhoneCode RUSSIA = fromString("+7");
    public static final CountryPhoneCode RWANDA = fromString("+250");
    public static final CountryPhoneCode SAINT_HELENA_ASCENSION_AND_TRISTAN_DA_CUNHA = fromString("+290");
    public static final CountryPhoneCode SAMOA = fromString("+685");
    public static final CountryPhoneCode SAN_MARINO = fromString("+378");
    public static final CountryPhoneCode SAO_TOME_AND_PRINCIPE = fromString("+239");
    public static final CountryPhoneCode SAUDI_ARABIA = fromString("+966");
    public static final CountryPhoneCode SENEGAL = fromString("+221");
    public static final CountryPhoneCode SERBIA = fromString("+381");
    public static final CountryPhoneCode SEYCHELLES = fromString("+248");
    public static final CountryPhoneCode SIERRA_LEONE = fromString("+232");
    public static final CountryPhoneCode SINGAPORE = fromString("+65");
    public static final CountryPhoneCode SLOVAKIA = fromString("+421");
    public static final CountryPhoneCode SLOVENIA = fromString("+386");
    public static final CountryPhoneCode SOLOMON_ISLANDS = fromString("+677");
    public static final CountryPhoneCode SOMALIA = fromString("+252");
    public static final CountryPhoneCode SOUTH_AFRICA = fromString("+27");
    public static final CountryPhoneCode SPAIN = fromString("+34");
    public static final CountryPhoneCode SRI_LANKA = fromString("+94");
    public static final CountryPhoneCode ST_KITTS_AND_NEVIS = fromString("+1");
    public static final CountryPhoneCode ST_LUCIA = fromString("+1");
    public static final CountryPhoneCode ST_PIERRE_AND_MIQUELON = fromString("+508");
    public static final CountryPhoneCode ST_VINCENT_AND_THE_GRENADINES = fromString("+1");
    public static final CountryPhoneCode SUDAN = fromString("+249");
    public static final CountryPhoneCode SURINAME = fromString("+597");
    public static final CountryPhoneCode SWAZILAND = fromString("+268");
    public static final CountryPhoneCode SWEDEN = fromString("+46");
    public static final CountryPhoneCode SWITZERLAND = fromString("+41");
    public static final CountryPhoneCode SYRIA = fromString("+963");
    public static final CountryPhoneCode TAIWAN = fromString("+886");
    public static final CountryPhoneCode TAJIKISTAN = fromString("+992");
    public static final CountryPhoneCode TANZANIA = fromString("+255");
    public static final CountryPhoneCode THAILAND = fromString("+66");
    public static final CountryPhoneCode TIMOR_LESTE = fromString("+670");
    public static final CountryPhoneCode TOGO = fromString("+228");
    public static final CountryPhoneCode TOKELAU = fromString("+690");
    public static final CountryPhoneCode TONGA = fromString("+676");
    public static final CountryPhoneCode TRINIDAD_AND_TOBAGO = fromString("+1");
    public static final CountryPhoneCode TRISTAN_DA_CUNHA = fromString("+290");
    public static final CountryPhoneCode TUNISIA = fromString("+216");
    public static final CountryPhoneCode TURKEY = fromString("+90");
    public static final CountryPhoneCode TURKMENISTAN = fromString("+993");
    public static final CountryPhoneCode TURKS_AND_CAICOS_ISLANDS = fromString("+1");
    public static final CountryPhoneCode TUVALU = fromString("+688");
    public static final CountryPhoneCode UGANDA = fromString("+256");
    public static final CountryPhoneCode UKRAINE = fromString("+380");
    public static final CountryPhoneCode UNITED_ARAB_EMIRATES = fromString("+971");
    public static final CountryPhoneCode UNITED_KINGDOM = fromString("+44");
    public static final CountryPhoneCode UNITED_STATES = fromString("+1");
    public static final CountryPhoneCode UNITED_STATES_MINOR_OUTLYING_ISLANDS = fromString("+1");
    public static final CountryPhoneCode URUGUAY = fromString("+598");
    public static final CountryPhoneCode UZBEKISTAN = fromString("+998");
    public static final CountryPhoneCode VANUATU = fromString("+678");
    public static final CountryPhoneCode VENEZUELA = fromString("+58");
    public static final CountryPhoneCode VIETNAM = fromString("+84");
    public static final CountryPhoneCode VIRGIN_ISLANDS_BRITISH = fromString("+1");
    public static final CountryPhoneCode VIRGIN_ISLANDS_US = fromString("+1");
    public static final CountryPhoneCode WALLIS_AND_FUTUNA = fromString("+681");
    public static final CountryPhoneCode YEMEN = fromString("+967");
    public static final CountryPhoneCode ZAMBIA = fromString("+260");
    public static final CountryPhoneCode ZIMBABWE = fromString("+263");

    /**
     * Creates or finds a country phone code from its string representation.
     *
     * @param code a country phone code
     * @return the corresponding CountryPhoneCode
     */
    public static CountryPhoneCode fromString(String code) {
        return fromString(code, CountryPhoneCode.class);
    }

    /**
     * @return known country phone codes
     */
    public static Collection<CountryPhoneCode> values() {
        return values(CountryPhoneCode.class);
    }
}
