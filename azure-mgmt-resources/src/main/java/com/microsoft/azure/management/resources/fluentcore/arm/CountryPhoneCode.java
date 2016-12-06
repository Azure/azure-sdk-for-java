/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for country codes for phone number prefixes.
 * E.g. the country code for +1 123 456 7890 is '+1'.
 */
public final class CountryPhoneCode {
    // CHECKSTYLE IGNORE Javadoc FOR NEXT 236 LINES
    public static final CountryPhoneCode ALBANIA = new CountryPhoneCode("+355");
    public static final CountryPhoneCode ALGERIA = new CountryPhoneCode("+213");
    public static final CountryPhoneCode ANDORRA = new CountryPhoneCode("+376");
    public static final CountryPhoneCode ANGOLA = new CountryPhoneCode("+244");
    public static final CountryPhoneCode ANTARCTICA = new CountryPhoneCode("+672");
    public static final CountryPhoneCode ANTIGUA_AND_BARBUDA = new CountryPhoneCode("+1");
    public static final CountryPhoneCode ARGENTINA = new CountryPhoneCode("+54");
    public static final CountryPhoneCode ARMENIA = new CountryPhoneCode("+374");
    public static final CountryPhoneCode ARUBA = new CountryPhoneCode("+297");
    public static final CountryPhoneCode ASCENSION_ISLAND = new CountryPhoneCode("+247");
    public static final CountryPhoneCode AUSTRALIA = new CountryPhoneCode("+61");
    public static final CountryPhoneCode AUSTRIA = new CountryPhoneCode("+43");
    public static final CountryPhoneCode AZERBAIJAN = new CountryPhoneCode("+994");
    public static final CountryPhoneCode BAHAMAS = new CountryPhoneCode("+1");
    public static final CountryPhoneCode BAHRAIN = new CountryPhoneCode("+973");
    public static final CountryPhoneCode BANGLADESH = new CountryPhoneCode("+880");
    public static final CountryPhoneCode BARBADOS = new CountryPhoneCode("+1");
    public static final CountryPhoneCode BELARUS = new CountryPhoneCode("+375");
    public static final CountryPhoneCode BELGIUM = new CountryPhoneCode("+32");
    public static final CountryPhoneCode BELIZE = new CountryPhoneCode("+501");
    public static final CountryPhoneCode BENIN = new CountryPhoneCode("+229");
    public static final CountryPhoneCode BERMUDA = new CountryPhoneCode("+1");
    public static final CountryPhoneCode BHUTAN = new CountryPhoneCode("+975");
    public static final CountryPhoneCode BOLIVIA = new CountryPhoneCode("+591");
    public static final CountryPhoneCode BOSNIA_AND_HERZEGOVINA = new CountryPhoneCode("+387");
    public static final CountryPhoneCode BOTSWANA = new CountryPhoneCode("+267");
    public static final CountryPhoneCode BOUVET_ISLAND = new CountryPhoneCode("+47");
    public static final CountryPhoneCode BRAZIL = new CountryPhoneCode("+55");
    public static final CountryPhoneCode BRITISH_INDIAN_OCEAN_TERRITORY = new CountryPhoneCode("+44");
    public static final CountryPhoneCode BRUNEI = new CountryPhoneCode("+673");
    public static final CountryPhoneCode BULGARIA = new CountryPhoneCode("+359");
    public static final CountryPhoneCode BURKINA_FASO = new CountryPhoneCode("+226");
    public static final CountryPhoneCode BURUNDI = new CountryPhoneCode("+257");
    public static final CountryPhoneCode CABO_VERDE = new CountryPhoneCode("+238");
    public static final CountryPhoneCode CAMBODIA = new CountryPhoneCode("+855");
    public static final CountryPhoneCode CAMEROON = new CountryPhoneCode("+237");
    public static final CountryPhoneCode CANADA = new CountryPhoneCode("+1");
    public static final CountryPhoneCode CAYMAN_ISLANDS = new CountryPhoneCode("+1");
    public static final CountryPhoneCode CENTRAL_AFRICAN_REPUBLIC = new CountryPhoneCode("+236");
    public static final CountryPhoneCode CHAD = new CountryPhoneCode("+235");
    public static final CountryPhoneCode CHILE = new CountryPhoneCode("+56");
    public static final CountryPhoneCode CHINA = new CountryPhoneCode("+86");
    public static final CountryPhoneCode CHRISTMAS_ISLAND = new CountryPhoneCode("+61");
    public static final CountryPhoneCode COCOS_KEELING_ISLANDS = new CountryPhoneCode("+61");
    public static final CountryPhoneCode COLOMBIA = new CountryPhoneCode("+57");
    public static final CountryPhoneCode COMOROS = new CountryPhoneCode("+269");
    public static final CountryPhoneCode CONGO = new CountryPhoneCode("+242");
    public static final CountryPhoneCode CONGO_DRC = new CountryPhoneCode("+243");
    public static final CountryPhoneCode COOK_ISLANDS = new CountryPhoneCode("+682");
    public static final CountryPhoneCode COSTA_RICA = new CountryPhoneCode("+506");
    public static final CountryPhoneCode CROATIA = new CountryPhoneCode("+385");
    public static final CountryPhoneCode CUBA = new CountryPhoneCode("+53");
    public static final CountryPhoneCode CYPRUS = new CountryPhoneCode("+357");
    public static final CountryPhoneCode CZECH_REPUBLIC = new CountryPhoneCode("+420");
    public static final CountryPhoneCode DENMARK = new CountryPhoneCode("+45");
    public static final CountryPhoneCode DJIBOUTI = new CountryPhoneCode("+253");
    public static final CountryPhoneCode DOMINICA = new CountryPhoneCode("+1");
    public static final CountryPhoneCode DOMINICAN_REPUBLIC = new CountryPhoneCode("+1");
    public static final CountryPhoneCode ECUADOR = new CountryPhoneCode("+593");
    public static final CountryPhoneCode EGYPT = new CountryPhoneCode("+20");
    public static final CountryPhoneCode EL_SALVADOR = new CountryPhoneCode("+503");
    public static final CountryPhoneCode EQUATORIAL_GUINEA = new CountryPhoneCode("+240");
    public static final CountryPhoneCode ERITREA = new CountryPhoneCode("+291");
    public static final CountryPhoneCode ESTONIA = new CountryPhoneCode("+372");
    public static final CountryPhoneCode ETHIOPIA = new CountryPhoneCode("+251");
    public static final CountryPhoneCode FALKLAND_ISLANDS = new CountryPhoneCode("+500");
    public static final CountryPhoneCode FAROE_ISLANDS = new CountryPhoneCode("+298");
    public static final CountryPhoneCode FIJI_ISLANDS = new CountryPhoneCode("+679");
    public static final CountryPhoneCode FINLAND = new CountryPhoneCode("+358");
    public static final CountryPhoneCode FRANCE = new CountryPhoneCode("+33");
    public static final CountryPhoneCode FRENCH_GUIANA = new CountryPhoneCode("+594");
    public static final CountryPhoneCode FRENCH_POLYNESIA = new CountryPhoneCode("+689");
    public static final CountryPhoneCode GABON = new CountryPhoneCode("+241");
    public static final CountryPhoneCode GAMBIA_THE = new CountryPhoneCode("+220");
    public static final CountryPhoneCode GEORGIA = new CountryPhoneCode("+995");
    public static final CountryPhoneCode GERMANY = new CountryPhoneCode("+49");
    public static final CountryPhoneCode GHANA = new CountryPhoneCode("+233");
    public static final CountryPhoneCode GIBRALTAR = new CountryPhoneCode("+350");
    public static final CountryPhoneCode GREECE = new CountryPhoneCode("+30");
    public static final CountryPhoneCode GREENLAND = new CountryPhoneCode("+299");
    public static final CountryPhoneCode GRENADA = new CountryPhoneCode("+1");
    public static final CountryPhoneCode GUADELOUPE = new CountryPhoneCode("+590");
    public static final CountryPhoneCode GUAM = new CountryPhoneCode("+1");
    public static final CountryPhoneCode GUATEMALA = new CountryPhoneCode("+502");
    public static final CountryPhoneCode GUERNSEY = new CountryPhoneCode("+44");
    public static final CountryPhoneCode GUINEA = new CountryPhoneCode("+224");
    public static final CountryPhoneCode GUINEA_BISSAU = new CountryPhoneCode("+245");
    public static final CountryPhoneCode GUYANA = new CountryPhoneCode("+592");
    public static final CountryPhoneCode HAITI = new CountryPhoneCode("+509");
    public static final CountryPhoneCode HOLY_SEE_VATICAN_CITY = new CountryPhoneCode("+379");
    public static final CountryPhoneCode HONDURAS = new CountryPhoneCode("+504");
    public static final CountryPhoneCode HONG_KONG_SAR = new CountryPhoneCode("+852");
    public static final CountryPhoneCode HUNGARY = new CountryPhoneCode("+36");
    public static final CountryPhoneCode ICELAND = new CountryPhoneCode("+354");
    public static final CountryPhoneCode INDIA = new CountryPhoneCode("+91");
    public static final CountryPhoneCode INDONESIA = new CountryPhoneCode("+62");
    public static final CountryPhoneCode IRAN = new CountryPhoneCode("+98");
    public static final CountryPhoneCode IRAQ = new CountryPhoneCode("+964");
    public static final CountryPhoneCode IRELAND = new CountryPhoneCode("+353");
    public static final CountryPhoneCode ISLE_OF_MAN = new CountryPhoneCode("+44");
    public static final CountryPhoneCode ISRAEL = new CountryPhoneCode("+972");
    public static final CountryPhoneCode ITALY = new CountryPhoneCode("+39");
    public static final CountryPhoneCode JAMAICA = new CountryPhoneCode("+1");
    public static final CountryPhoneCode JAN_MAYEN = new CountryPhoneCode("+47");
    public static final CountryPhoneCode JAPAN = new CountryPhoneCode("+81");
    public static final CountryPhoneCode JERSEY = new CountryPhoneCode("+44");
    public static final CountryPhoneCode JORDAN = new CountryPhoneCode("+962");
    public static final CountryPhoneCode KAZAKHSTAN = new CountryPhoneCode("+7");
    public static final CountryPhoneCode KENYA = new CountryPhoneCode("+254");
    public static final CountryPhoneCode KIRIBATI = new CountryPhoneCode("+686");
    public static final CountryPhoneCode KOREA = new CountryPhoneCode("+82");
    public static final CountryPhoneCode KUWAIT = new CountryPhoneCode("+965");
    public static final CountryPhoneCode KYRGYZSTAN = new CountryPhoneCode("+996");
    public static final CountryPhoneCode LAOS = new CountryPhoneCode("+856");
    public static final CountryPhoneCode LATVIA = new CountryPhoneCode("+371");
    public static final CountryPhoneCode LEBANON = new CountryPhoneCode("+961");
    public static final CountryPhoneCode LESOTHO = new CountryPhoneCode("+266");
    public static final CountryPhoneCode LIBERIA = new CountryPhoneCode("+231");
    public static final CountryPhoneCode LIBYA = new CountryPhoneCode("+218");
    public static final CountryPhoneCode LIECHTENSTEIN = new CountryPhoneCode("+423");
    public static final CountryPhoneCode LITHUANIA = new CountryPhoneCode("+370");
    public static final CountryPhoneCode LUXEMBOURG = new CountryPhoneCode("+352");
    public static final CountryPhoneCode MACAO_SAR = new CountryPhoneCode("+853");
    public static final CountryPhoneCode MACEDONIA_FORMER_YUGOSLAV_REPUBLIC_OF = new CountryPhoneCode("+389");
    public static final CountryPhoneCode MADAGASCAR = new CountryPhoneCode("+261");
    public static final CountryPhoneCode MALAWI = new CountryPhoneCode("+265");
    public static final CountryPhoneCode MALAYSIA = new CountryPhoneCode("+60");
    public static final CountryPhoneCode MALDIVES = new CountryPhoneCode("+960");
    public static final CountryPhoneCode MALI = new CountryPhoneCode("+223");
    public static final CountryPhoneCode MALTA = new CountryPhoneCode("+356");
    public static final CountryPhoneCode MARSHALL_ISLANDS = new CountryPhoneCode("+692");
    public static final CountryPhoneCode MARTINIQUE = new CountryPhoneCode("+596");
    public static final CountryPhoneCode MAURITANIA = new CountryPhoneCode("+222");
    public static final CountryPhoneCode MAURITIUS = new CountryPhoneCode("+230");
    public static final CountryPhoneCode MAYOTTE = new CountryPhoneCode("+262");
    public static final CountryPhoneCode MEXICO = new CountryPhoneCode("+52");
    public static final CountryPhoneCode MICRONESIA = new CountryPhoneCode("+691");
    public static final CountryPhoneCode MOLDOVA = new CountryPhoneCode("+373");
    public static final CountryPhoneCode MONACO = new CountryPhoneCode("+377");
    public static final CountryPhoneCode MONGOLIA = new CountryPhoneCode("+976");
    public static final CountryPhoneCode MONTENEGRO = new CountryPhoneCode("+382");
    public static final CountryPhoneCode MONTSERRAT = new CountryPhoneCode("+1");
    public static final CountryPhoneCode MOROCCO = new CountryPhoneCode("+212");
    public static final CountryPhoneCode MOZAMBIQUE = new CountryPhoneCode("+258");
    public static final CountryPhoneCode MYANMAR = new CountryPhoneCode("+95");
    public static final CountryPhoneCode NAMIBIA = new CountryPhoneCode("+264");
    public static final CountryPhoneCode NAURU = new CountryPhoneCode("+674");
    public static final CountryPhoneCode NEPAL = new CountryPhoneCode("+977");
    public static final CountryPhoneCode NETHERLANDS = new CountryPhoneCode("+31");
    public static final CountryPhoneCode NETHERLANDS_ANTILLES_FORMER = new CountryPhoneCode("+599");
    public static final CountryPhoneCode NEW_CALEDONIA = new CountryPhoneCode("+687");
    public static final CountryPhoneCode NEW_ZEALAND = new CountryPhoneCode("+64");
    public static final CountryPhoneCode NICARAGUA = new CountryPhoneCode("+505");
    public static final CountryPhoneCode NIGER = new CountryPhoneCode("+227");
    public static final CountryPhoneCode NIGERIA = new CountryPhoneCode("+234");
    public static final CountryPhoneCode NIUE = new CountryPhoneCode("+683");
    public static final CountryPhoneCode NORTH_KOREA = new CountryPhoneCode("+850");
    public static final CountryPhoneCode NORTHERN_MARIANA_ISLANDS = new CountryPhoneCode("+1");
    public static final CountryPhoneCode NORWAY = new CountryPhoneCode("+47");
    public static final CountryPhoneCode OMAN = new CountryPhoneCode("+968");
    public static final CountryPhoneCode PAKISTAN = new CountryPhoneCode("+92");
    public static final CountryPhoneCode PALAU = new CountryPhoneCode("+680");
    public static final CountryPhoneCode PALESTINIAN_AUTHORITY_970 = new CountryPhoneCode("+970");
    public static final CountryPhoneCode PALESTINIAN_AUTHORITY_972 = new CountryPhoneCode("+972");
    public static final CountryPhoneCode PANAMA = new CountryPhoneCode("+507");
    public static final CountryPhoneCode PAPUA_NEW_GUINEA = new CountryPhoneCode("+675");
    public static final CountryPhoneCode PARAGUAY = new CountryPhoneCode("+595");
    public static final CountryPhoneCode PERU = new CountryPhoneCode("+51");
    public static final CountryPhoneCode PHILIPPINES = new CountryPhoneCode("+63");
    public static final CountryPhoneCode POLAND = new CountryPhoneCode("+48");
    public static final CountryPhoneCode PORTUGAL = new CountryPhoneCode("+351");
    public static final CountryPhoneCode QATAR = new CountryPhoneCode("+974");
    public static final CountryPhoneCode IVORY_COAST = new CountryPhoneCode("+225");
    public static final CountryPhoneCode REUNION = new CountryPhoneCode("+262");
    public static final CountryPhoneCode ROMANIA = new CountryPhoneCode("+40");
    public static final CountryPhoneCode RUSSIA = new CountryPhoneCode("+7");
    public static final CountryPhoneCode RWANDA = new CountryPhoneCode("+250");
    public static final CountryPhoneCode SAINT_HELENA_ASCENSION_AND_TRISTAN_DA_CUNHA = new CountryPhoneCode("+290");
    public static final CountryPhoneCode SAMOA = new CountryPhoneCode("+685");
    public static final CountryPhoneCode SAN_MARINO = new CountryPhoneCode("+378");
    public static final CountryPhoneCode SAO_TOME_AND_PRINCIPE = new CountryPhoneCode("+239");
    public static final CountryPhoneCode SAUDI_ARABIA = new CountryPhoneCode("+966");
    public static final CountryPhoneCode SENEGAL = new CountryPhoneCode("+221");
    public static final CountryPhoneCode SERBIA = new CountryPhoneCode("+381");
    public static final CountryPhoneCode SEYCHELLES = new CountryPhoneCode("+248");
    public static final CountryPhoneCode SIERRA_LEONE = new CountryPhoneCode("+232");
    public static final CountryPhoneCode SINGAPORE = new CountryPhoneCode("+65");
    public static final CountryPhoneCode SLOVAKIA = new CountryPhoneCode("+421");
    public static final CountryPhoneCode SLOVENIA = new CountryPhoneCode("+386");
    public static final CountryPhoneCode SOLOMON_ISLANDS = new CountryPhoneCode("+677");
    public static final CountryPhoneCode SOMALIA = new CountryPhoneCode("+252");
    public static final CountryPhoneCode SOUTH_AFRICA = new CountryPhoneCode("+27");
    public static final CountryPhoneCode SPAIN = new CountryPhoneCode("+34");
    public static final CountryPhoneCode SRI_LANKA = new CountryPhoneCode("+94");
    public static final CountryPhoneCode ST_KITTS_AND_NEVIS = new CountryPhoneCode("+1");
    public static final CountryPhoneCode ST_LUCIA = new CountryPhoneCode("+1");
    public static final CountryPhoneCode ST_PIERRE_AND_MIQUELON = new CountryPhoneCode("+508");
    public static final CountryPhoneCode ST_VINCENT_AND_THE_GRENADINES = new CountryPhoneCode("+1");
    public static final CountryPhoneCode SUDAN = new CountryPhoneCode("+249");
    public static final CountryPhoneCode SURINAME = new CountryPhoneCode("+597");
    public static final CountryPhoneCode SWAZILAND = new CountryPhoneCode("+268");
    public static final CountryPhoneCode SWEDEN = new CountryPhoneCode("+46");
    public static final CountryPhoneCode SWITZERLAND = new CountryPhoneCode("+41");
    public static final CountryPhoneCode SYRIA = new CountryPhoneCode("+963");
    public static final CountryPhoneCode TAIWAN = new CountryPhoneCode("+886");
    public static final CountryPhoneCode TAJIKISTAN = new CountryPhoneCode("+992");
    public static final CountryPhoneCode TANZANIA = new CountryPhoneCode("+255");
    public static final CountryPhoneCode THAILAND = new CountryPhoneCode("+66");
    public static final CountryPhoneCode TIMOR_LESTE = new CountryPhoneCode("+670");
    public static final CountryPhoneCode TOGO = new CountryPhoneCode("+228");
    public static final CountryPhoneCode TOKELAU = new CountryPhoneCode("+690");
    public static final CountryPhoneCode TONGA = new CountryPhoneCode("+676");
    public static final CountryPhoneCode TRINIDAD_AND_TOBAGO = new CountryPhoneCode("+1");
    public static final CountryPhoneCode TRISTAN_DA_CUNHA = new CountryPhoneCode("+290");
    public static final CountryPhoneCode TUNISIA = new CountryPhoneCode("+216");
    public static final CountryPhoneCode TURKEY = new CountryPhoneCode("+90");
    public static final CountryPhoneCode TURKMENISTAN = new CountryPhoneCode("+993");
    public static final CountryPhoneCode TURKS_AND_CAICOS_ISLANDS = new CountryPhoneCode("+1");
    public static final CountryPhoneCode TUVALU = new CountryPhoneCode("+688");
    public static final CountryPhoneCode UGANDA = new CountryPhoneCode("+256");
    public static final CountryPhoneCode UKRAINE = new CountryPhoneCode("+380");
    public static final CountryPhoneCode UNITED_ARAB_EMIRATES = new CountryPhoneCode("+971");
    public static final CountryPhoneCode UNITED_KINGDOM = new CountryPhoneCode("+44");
    public static final CountryPhoneCode UNITED_STATES = new CountryPhoneCode("+1");
    public static final CountryPhoneCode UNITED_STATES_MINOR_OUTLYING_ISLANDS = new CountryPhoneCode("+1");
    public static final CountryPhoneCode URUGUAY = new CountryPhoneCode("+598");
    public static final CountryPhoneCode UZBEKISTAN = new CountryPhoneCode("+998");
    public static final CountryPhoneCode VANUATU = new CountryPhoneCode("+678");
    public static final CountryPhoneCode VENEZUELA = new CountryPhoneCode("+58");
    public static final CountryPhoneCode VIETNAM = new CountryPhoneCode("+84");
    public static final CountryPhoneCode VIRGIN_ISLANDS_BRITISH = new CountryPhoneCode("+1");
    public static final CountryPhoneCode VIRGIN_ISLANDS_US = new CountryPhoneCode("+1");
    public static final CountryPhoneCode WALLIS_AND_FUTUNA = new CountryPhoneCode("+681");
    public static final CountryPhoneCode YEMEN = new CountryPhoneCode("+967");
    public static final CountryPhoneCode ZAMBIA = new CountryPhoneCode("+260");
    public static final CountryPhoneCode ZIMBABWE = new CountryPhoneCode("+263");

    private String value;

    /**
     * Creates a custom value for CountryPhoneCode.
     * @param value the custom value
     */
    public CountryPhoneCode(String value) {
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
        if (!(obj instanceof CountryPhoneCode)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        CountryPhoneCode rhs = (CountryPhoneCode) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
