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
    /**
     * Country phone code {@code +355}.
     */
    public static final CountryPhoneCode ALBANIA = fromString("+355");
    /**
     * Country phone code {@code +213}.
     */
    public static final CountryPhoneCode ALGERIA = fromString("+213");
    /**
     * Country phone code {@code +376}.
     */
    public static final CountryPhoneCode ANDORRA = fromString("+376");
    /**
     * Country phone code {@code +244}.
     */
    public static final CountryPhoneCode ANGOLA = fromString("+244");
    /**
     * Country phone code {@code +672}.
     */
    public static final CountryPhoneCode ANTARCTICA = fromString("+672");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode ANTIGUA_AND_BARBUDA = fromString("+1");
    /**
     * Country phone code {@code +54}.
     */
    public static final CountryPhoneCode ARGENTINA = fromString("+54");
    /**
     * Country phone code {@code +374}.
     */
    public static final CountryPhoneCode ARMENIA = fromString("+374");
    /**
     * Country phone code {@code +297}.
     */
    public static final CountryPhoneCode ARUBA = fromString("+297");
    /**
     * Country phone code {@code +247}.
     */
    public static final CountryPhoneCode ASCENSION_ISLAND = fromString("+247");
    /**
     * Country phone code {@code +61}.
     */
    public static final CountryPhoneCode AUSTRALIA = fromString("+61");
    /**
     * Country phone code {@code +43}.
     */
    public static final CountryPhoneCode AUSTRIA = fromString("+43");
    /**
     * Country phone code {@code +994}.
     */
    public static final CountryPhoneCode AZERBAIJAN = fromString("+994");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode BAHAMAS = fromString("+1");
    /**
     * Country phone code {@code +973}.
     */
    public static final CountryPhoneCode BAHRAIN = fromString("+973");
    /**
     * Country phone code {@code +880}.
     */
    public static final CountryPhoneCode BANGLADESH = fromString("+880");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode BARBADOS = fromString("+1");
    /**
     * Country phone code {@code +375}.
     */
    public static final CountryPhoneCode BELARUS = fromString("+375");
    /**
     * Country phone code {@code +32}.
     */
    public static final CountryPhoneCode BELGIUM = fromString("+32");
    /**
     * Country phone code {@code +501}.
     */
    public static final CountryPhoneCode BELIZE = fromString("+501");
    /**
     * Country phone code {@code +229}.
     */
    public static final CountryPhoneCode BENIN = fromString("+229");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode BERMUDA = fromString("+1");
    /**
     * Country phone code {@code +975}.
     */
    public static final CountryPhoneCode BHUTAN = fromString("+975");
    /**
     * Country phone code {@code +591}.
     */
    public static final CountryPhoneCode BOLIVIA = fromString("+591");
    /**
     * Country phone code {@code +387}.
     */
    public static final CountryPhoneCode BOSNIA_AND_HERZEGOVINA = fromString("+387");
    /**
     * Country phone code {@code +267}.
     */
    public static final CountryPhoneCode BOTSWANA = fromString("+267");
    /**
     * Country phone code {@code +47}.
     */
    public static final CountryPhoneCode BOUVET_ISLAND = fromString("+47");
    /**
     * Country phone code {@code +55}.
     */
    public static final CountryPhoneCode BRAZIL = fromString("+55");
    /**
     * Country phone code {@code +44}.
     */
    public static final CountryPhoneCode BRITISH_INDIAN_OCEAN_TERRITORY = fromString("+44");
    /**
     * Country phone code {@code +673}.
     */
    public static final CountryPhoneCode BRUNEI = fromString("+673");
    /**
     * Country phone code {@code +359}.
     */
    public static final CountryPhoneCode BULGARIA = fromString("+359");
    /**
     * Country phone code {@code +226}.
     */
    public static final CountryPhoneCode BURKINA_FASO = fromString("+226");
    /**
     * Country phone code {@code +257}.
     */
    public static final CountryPhoneCode BURUNDI = fromString("+257");
    /**
     * Country phone code {@code +238}.
     */
    public static final CountryPhoneCode CABO_VERDE = fromString("+238");
    /**
     * Country phone code {@code +855}.
     */
    public static final CountryPhoneCode CAMBODIA = fromString("+855");
    /**
     * Country phone code {@code +237}.
     */
    public static final CountryPhoneCode CAMEROON = fromString("+237");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode CANADA = fromString("+1");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode CAYMAN_ISLANDS = fromString("+1");
    /**
     * Country phone code {@code +236}.
     */
    public static final CountryPhoneCode CENTRAL_AFRICAN_REPUBLIC = fromString("+236");
    /**
     * Country phone code {@code +235}.
     */
    public static final CountryPhoneCode CHAD = fromString("+235");
    /**
     * Country phone code {@code +56}.
     */
    public static final CountryPhoneCode CHILE = fromString("+56");
    /**
     * Country phone code {@code +86}.
     */
    public static final CountryPhoneCode CHINA = fromString("+86");
    /**
     * Country phone code {@code +61}.
     */
    public static final CountryPhoneCode CHRISTMAS_ISLAND = fromString("+61");
    /**
     * Country phone code {@code +61}.
     */
    public static final CountryPhoneCode COCOS_KEELING_ISLANDS = fromString("+61");
    /**
     * Country phone code {@code +57}.
     */
    public static final CountryPhoneCode COLOMBIA = fromString("+57");
    /**
     * Country phone code {@code +269}.
     */
    public static final CountryPhoneCode COMOROS = fromString("+269");
    /**
     * Country phone code {@code +242}.
     */
    public static final CountryPhoneCode CONGO = fromString("+242");
    /**
     * Country phone code {@code +243}.
     */
    public static final CountryPhoneCode CONGO_DRC = fromString("+243");
    /**
     * Country phone code {@code +682}.
     */
    public static final CountryPhoneCode COOK_ISLANDS = fromString("+682");
    /**
     * Country phone code {@code +506}.
     */
    public static final CountryPhoneCode COSTA_RICA = fromString("+506");
    /**
     * Country phone code {@code +385}.
     */
    public static final CountryPhoneCode CROATIA = fromString("+385");
    /**
     * Country phone code {@code +53}.
     */
    public static final CountryPhoneCode CUBA = fromString("+53");
    /**
     * Country phone code {@code +357}.
     */
    public static final CountryPhoneCode CYPRUS = fromString("+357");
    /**
     * Country phone code {@code +420}.
     */
    public static final CountryPhoneCode CZECH_REPUBLIC = fromString("+420");
    /**
     * Country phone code {@code +45}.
     */
    public static final CountryPhoneCode DENMARK = fromString("+45");
    /**
     * Country phone code {@code +253}.
     */
    public static final CountryPhoneCode DJIBOUTI = fromString("+253");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode DOMINICA = fromString("+1");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode DOMINICAN_REPUBLIC = fromString("+1");
    /**
     * Country phone code {@code +593}.
     */
    public static final CountryPhoneCode ECUADOR = fromString("+593");
    /**
     * Country phone code {@code +20}.
     */
    public static final CountryPhoneCode EGYPT = fromString("+20");
    /**
     * Country phone code {@code +503}.
     */
    public static final CountryPhoneCode EL_SALVADOR = fromString("+503");
    /**
     * Country phone code {@code +240}.
     */
    public static final CountryPhoneCode EQUATORIAL_GUINEA = fromString("+240");
    /**
     * Country phone code {@code +291}.
     */
    public static final CountryPhoneCode ERITREA = fromString("+291");
    /**
     * Country phone code {@code +372}.
     */
    public static final CountryPhoneCode ESTONIA = fromString("+372");
    /**
     * Country phone code {@code +251}.
     */
    public static final CountryPhoneCode ETHIOPIA = fromString("+251");
    /**
     * Country phone code {@code +500}.
     */
    public static final CountryPhoneCode FALKLAND_ISLANDS = fromString("+500");
    /**
     * Country phone code {@code +298}.
     */
    public static final CountryPhoneCode FAROE_ISLANDS = fromString("+298");
    /**
     * Country phone code {@code +679}.
     */
    public static final CountryPhoneCode FIJI_ISLANDS = fromString("+679");
    /**
     * Country phone code {@code +358}.
     */
    public static final CountryPhoneCode FINLAND = fromString("+358");
    /**
     * Country phone code {@code +33}.
     */
    public static final CountryPhoneCode FRANCE = fromString("+33");
    /**
     * Country phone code {@code +594}.
     */
    public static final CountryPhoneCode FRENCH_GUIANA = fromString("+594");
    /**
     * Country phone code {@code +689}.
     */
    public static final CountryPhoneCode FRENCH_POLYNESIA = fromString("+689");
    /**
     * Country phone code {@code +241}.
     */
    public static final CountryPhoneCode GABON = fromString("+241");
    /**
     * Country phone code {@code +220}.
     */
    public static final CountryPhoneCode GAMBIA_THE = fromString("+220");
    /**
     * Country phone code {@code +995}.
     */
    public static final CountryPhoneCode GEORGIA = fromString("+995");
    /**
     * Country phone code {@code +49}.
     */
    public static final CountryPhoneCode GERMANY = fromString("+49");
    /**
     * Country phone code {@code +233}.
     */
    public static final CountryPhoneCode GHANA = fromString("+233");
    /**
     * Country phone code {@code +350}.
     */
    public static final CountryPhoneCode GIBRALTAR = fromString("+350");
    /**
     * Country phone code {@code +30}.
     */
    public static final CountryPhoneCode GREECE = fromString("+30");
    /**
     * Country phone code {@code +299}.
     */
    public static final CountryPhoneCode GREENLAND = fromString("+299");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode GRENADA = fromString("+1");
    /**
     * Country phone code {@code +590}.
     */
    public static final CountryPhoneCode GUADELOUPE = fromString("+590");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode GUAM = fromString("+1");
    /**
     * Country phone code {@code +502}.
     */
    public static final CountryPhoneCode GUATEMALA = fromString("+502");
    /**
     * Country phone code {@code +44}.
     */
    public static final CountryPhoneCode GUERNSEY = fromString("+44");
    /**
     * Country phone code {@code +224}.
     */
    public static final CountryPhoneCode GUINEA = fromString("+224");
    /**
     * Country phone code {@code +245}.
     */
    public static final CountryPhoneCode GUINEA_BISSAU = fromString("+245");
    /**
     * Country phone code {@code +592}.
     */
    public static final CountryPhoneCode GUYANA = fromString("+592");
    /**
     * Country phone code {@code +509}.
     */
    public static final CountryPhoneCode HAITI = fromString("+509");
    /**
     * Country phone code {@code +379}.
     */
    public static final CountryPhoneCode HOLY_SEE_VATICAN_CITY = fromString("+379");
    /**
     * Country phone code {@code +504}.
     */
    public static final CountryPhoneCode HONDURAS = fromString("+504");
    /**
     * Country phone code {@code +852}.
     */
    public static final CountryPhoneCode HONG_KONG_SAR = fromString("+852");
    /**
     * Country phone code {@code +36}.
     */
    public static final CountryPhoneCode HUNGARY = fromString("+36");
    /**
     * Country phone code {@code +354}.
     */
    public static final CountryPhoneCode ICELAND = fromString("+354");
    /**
     * Country phone code {@code +91}.
     */
    public static final CountryPhoneCode INDIA = fromString("+91");
    /**
     * Country phone code {@code +62}.
     */
    public static final CountryPhoneCode INDONESIA = fromString("+62");
    /**
     * Country phone code {@code +98}.
     */
    public static final CountryPhoneCode IRAN = fromString("+98");
    /**
     * Country phone code {@code +964}.
     */
    public static final CountryPhoneCode IRAQ = fromString("+964");
    /**
     * Country phone code {@code +353}.
     */
    public static final CountryPhoneCode IRELAND = fromString("+353");
    /**
     * Country phone code {@code +44}.
     */
    public static final CountryPhoneCode ISLE_OF_MAN = fromString("+44");
    /**
     * Country phone code {@code +972}.
     */
    public static final CountryPhoneCode ISRAEL = fromString("+972");
    /**
     * Country phone code {@code +39}.
     */
    public static final CountryPhoneCode ITALY = fromString("+39");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode JAMAICA = fromString("+1");
    /**
     * Country phone code {@code +47}.
     */
    public static final CountryPhoneCode JAN_MAYEN = fromString("+47");
    /**
     * Country phone code {@code +81}.
     */
    public static final CountryPhoneCode JAPAN = fromString("+81");
    /**
     * Country phone code {@code +44}.
     */
    public static final CountryPhoneCode JERSEY = fromString("+44");
    /**
     * Country phone code {@code +962}.
     */
    public static final CountryPhoneCode JORDAN = fromString("+962");
    /**
     * Country phone code {@code +7}.
     */
    public static final CountryPhoneCode KAZAKHSTAN = fromString("+7");
    /**
     * Country phone code {@code +254}.
     */
    public static final CountryPhoneCode KENYA = fromString("+254");
    /**
     * Country phone code {@code +686}.
     */
    public static final CountryPhoneCode KIRIBATI = fromString("+686");
    /**
     * Country phone code {@code +82}.
     */
    public static final CountryPhoneCode KOREA = fromString("+82");
    /**
     * Country phone code {@code +965}.
     */
    public static final CountryPhoneCode KUWAIT = fromString("+965");
    /**
     * Country phone code {@code +996}.
     */
    public static final CountryPhoneCode KYRGYZSTAN = fromString("+996");
    /**
     * Country phone code {@code +856}.
     */
    public static final CountryPhoneCode LAOS = fromString("+856");
    /**
     * Country phone code {@code +371}.
     */
    public static final CountryPhoneCode LATVIA = fromString("+371");
    /**
     * Country phone code {@code +961}.
     */
    public static final CountryPhoneCode LEBANON = fromString("+961");
    /**
     * Country phone code {@code +266}.
     */
    public static final CountryPhoneCode LESOTHO = fromString("+266");
    /**
     * Country phone code {@code +231}.
     */
    public static final CountryPhoneCode LIBERIA = fromString("+231");
    /**
     * Country phone code {@code +218}.
     */
    public static final CountryPhoneCode LIBYA = fromString("+218");
    /**
     * Country phone code {@code +423}.
     */
    public static final CountryPhoneCode LIECHTENSTEIN = fromString("+423");
    /**
     * Country phone code {@code +370}.
     */
    public static final CountryPhoneCode LITHUANIA = fromString("+370");
    /**
     * Country phone code {@code +352}.
     */
    public static final CountryPhoneCode LUXEMBOURG = fromString("+352");
    /**
     * Country phone code {@code +853}.
     */
    public static final CountryPhoneCode MACAO_SAR = fromString("+853");
    /**
     * Country phone code {@code +389}.
     */
    public static final CountryPhoneCode MACEDONIA_FORMER_YUGOSLAV_REPUBLIC_OF = fromString("+389");
    /**
     * Country phone code {@code +261}.
     */
    public static final CountryPhoneCode MADAGASCAR = fromString("+261");
    /**
     * Country phone code {@code +265}.
     */
    public static final CountryPhoneCode MALAWI = fromString("+265");
    /**
     * Country phone code {@code +60}.
     */
    public static final CountryPhoneCode MALAYSIA = fromString("+60");
    /**
     * Country phone code {@code +960}.
     */
    public static final CountryPhoneCode MALDIVES = fromString("+960");
    /**
     * Country phone code {@code +223}.
     */
    public static final CountryPhoneCode MALI = fromString("+223");
    /**
     * Country phone code {@code +356}.
     */
    public static final CountryPhoneCode MALTA = fromString("+356");
    /**
     * Country phone code {@code +692}.
     */
    public static final CountryPhoneCode MARSHALL_ISLANDS = fromString("+692");
    /**
     * Country phone code {@code +596}.
     */
    public static final CountryPhoneCode MARTINIQUE = fromString("+596");
    /**
     * Country phone code {@code +222}.
     */
    public static final CountryPhoneCode MAURITANIA = fromString("+222");
    /**
     * Country phone code {@code +230}.
     */
    public static final CountryPhoneCode MAURITIUS = fromString("+230");
    /**
     * Country phone code {@code +262}.
     */
    public static final CountryPhoneCode MAYOTTE = fromString("+262");
    /**
     * Country phone code {@code +52}.
     */
    public static final CountryPhoneCode MEXICO = fromString("+52");
    /**
     * Country phone code {@code +691}.
     */
    public static final CountryPhoneCode MICRONESIA = fromString("+691");
    /**
     * Country phone code {@code +373}.
     */
    public static final CountryPhoneCode MOLDOVA = fromString("+373");
    /**
     * Country phone code {@code +377}.
     */
    public static final CountryPhoneCode MONACO = fromString("+377");
    /**
     * Country phone code {@code +976}.
     */
    public static final CountryPhoneCode MONGOLIA = fromString("+976");
    /**
     * Country phone code {@code +382}.
     */
    public static final CountryPhoneCode MONTENEGRO = fromString("+382");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode MONTSERRAT = fromString("+1");
    /**
     * Country phone code {@code +212}.
     */
    public static final CountryPhoneCode MOROCCO = fromString("+212");
    /**
     * Country phone code {@code +258}.
     */
    public static final CountryPhoneCode MOZAMBIQUE = fromString("+258");
    /**
     * Country phone code {@code +95}.
     */
    public static final CountryPhoneCode MYANMAR = fromString("+95");
    /**
     * Country phone code {@code +264}.
     */
    public static final CountryPhoneCode NAMIBIA = fromString("+264");
    /**
     * Country phone code {@code +674}.
     */
    public static final CountryPhoneCode NAURU = fromString("+674");
    /**
     * Country phone code {@code +977}.
     */
    public static final CountryPhoneCode NEPAL = fromString("+977");
    /**
     * Country phone code {@code +31}.
     */
    public static final CountryPhoneCode NETHERLANDS = fromString("+31");
    /**
     * Country phone code {@code +599}.
     */
    public static final CountryPhoneCode NETHERLANDS_ANTILLES_FORMER = fromString("+599");
    /**
     * Country phone code {@code +687}.
     */
    public static final CountryPhoneCode NEW_CALEDONIA = fromString("+687");
    /**
     * Country phone code {@code +64}.
     */
    public static final CountryPhoneCode NEW_ZEALAND = fromString("+64");
    /**
     * Country phone code {@code +505}.
     */
    public static final CountryPhoneCode NICARAGUA = fromString("+505");
    /**
     * Country phone code {@code +227}.
     */
    public static final CountryPhoneCode NIGER = fromString("+227");
    /**
     * Country phone code {@code +234}.
     */
    public static final CountryPhoneCode NIGERIA = fromString("+234");
    /**
     * Country phone code {@code +683}.
     */
    public static final CountryPhoneCode NIUE = fromString("+683");
    /**
     * Country phone code {@code +850}.
     */
    public static final CountryPhoneCode NORTH_KOREA = fromString("+850");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode NORTHERN_MARIANA_ISLANDS = fromString("+1");
    /**
     * Country phone code {@code +47}.
     */
    public static final CountryPhoneCode NORWAY = fromString("+47");
    /**
     * Country phone code {@code +968}.
     */
    public static final CountryPhoneCode OMAN = fromString("+968");
    /**
     * Country phone code {@code +92}.
     */
    public static final CountryPhoneCode PAKISTAN = fromString("+92");
    /**
     * Country phone code {@code +680}.
     */
    public static final CountryPhoneCode PALAU = fromString("+680");
    /**
     * Country phone code {@code +970}.
     */
    public static final CountryPhoneCode PALESTINIAN_AUTHORITY_970 = fromString("+970");
    /**
     * Country phone code {@code +972}.
     */
    public static final CountryPhoneCode PALESTINIAN_AUTHORITY_972 = fromString("+972");
    /**
     * Country phone code {@code +507}.
     */
    public static final CountryPhoneCode PANAMA = fromString("+507");
    /**
     * Country phone code {@code +675}.
     */
    public static final CountryPhoneCode PAPUA_NEW_GUINEA = fromString("+675");
    /**
     * Country phone code {@code +595}.
     */
    public static final CountryPhoneCode PARAGUAY = fromString("+595");
    /**
     * Country phone code {@code +51}.
     */
    public static final CountryPhoneCode PERU = fromString("+51");
    /**
     * Country phone code {@code +63}.
     */
    public static final CountryPhoneCode PHILIPPINES = fromString("+63");
    /**
     * Country phone code {@code +48}.
     */
    public static final CountryPhoneCode POLAND = fromString("+48");
    /**
     * Country phone code {@code +351}.
     */
    public static final CountryPhoneCode PORTUGAL = fromString("+351");
    /**
     * Country phone code {@code +974}.
     */
    public static final CountryPhoneCode QATAR = fromString("+974");
    /**
     * Country phone code {@code +225}.
     */
    public static final CountryPhoneCode IVORY_COAST = fromString("+225");
    /**
     * Country phone code {@code +262}.
     */
    public static final CountryPhoneCode REUNION = fromString("+262");
    /**
     * Country phone code {@code +40}.
     */
    public static final CountryPhoneCode ROMANIA = fromString("+40");
    /**
     * Country phone code {@code +7}.
     */
    public static final CountryPhoneCode RUSSIA = fromString("+7");
    /**
     * Country phone code {@code +250}.
     */
    public static final CountryPhoneCode RWANDA = fromString("+250");
    /**
     * Country phone code {@code +290}.
     */
    public static final CountryPhoneCode SAINT_HELENA_ASCENSION_AND_TRISTAN_DA_CUNHA = fromString("+290");
    /**
     * Country phone code {@code +685}.
     */
    public static final CountryPhoneCode SAMOA = fromString("+685");
    /**
     * Country phone code {@code +378}.
     */
    public static final CountryPhoneCode SAN_MARINO = fromString("+378");
    /**
     * Country phone code {@code +239}.
     */
    public static final CountryPhoneCode SAO_TOME_AND_PRINCIPE = fromString("+239");
    /**
     * Country phone code {@code +966}.
     */
    public static final CountryPhoneCode SAUDI_ARABIA = fromString("+966");
    /**
     * Country phone code {@code +221}.
     */
    public static final CountryPhoneCode SENEGAL = fromString("+221");
    /**
     * Country phone code {@code +381}.
     */
    public static final CountryPhoneCode SERBIA = fromString("+381");
    /**
     * Country phone code {@code +248}.
     */
    public static final CountryPhoneCode SEYCHELLES = fromString("+248");
    /**
     * Country phone code {@code +232}.
     */
    public static final CountryPhoneCode SIERRA_LEONE = fromString("+232");
    /**
     * Country phone code {@code +65}.
     */
    public static final CountryPhoneCode SINGAPORE = fromString("+65");
    /**
     * Country phone code {@code +421}.
     */
    public static final CountryPhoneCode SLOVAKIA = fromString("+421");
    /**
     * Country phone code {@code +386}.
     */
    public static final CountryPhoneCode SLOVENIA = fromString("+386");
    /**
     * Country phone code {@code +677}.
     */
    public static final CountryPhoneCode SOLOMON_ISLANDS = fromString("+677");
    /**
     * Country phone code {@code +252}.
     */
    public static final CountryPhoneCode SOMALIA = fromString("+252");
    /**
     * Country phone code {@code +27}.
     */
    public static final CountryPhoneCode SOUTH_AFRICA = fromString("+27");
    /**
     * Country phone code {@code +34}.
     */
    public static final CountryPhoneCode SPAIN = fromString("+34");
    /**
     * Country phone code {@code +94}.
     */
    public static final CountryPhoneCode SRI_LANKA = fromString("+94");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode ST_KITTS_AND_NEVIS = fromString("+1");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode ST_LUCIA = fromString("+1");
    /**
     * Country phone code {@code +508}.
     */
    public static final CountryPhoneCode ST_PIERRE_AND_MIQUELON = fromString("+508");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode ST_VINCENT_AND_THE_GRENADINES = fromString("+1");
    /**
     * Country phone code {@code +249}.
     */
    public static final CountryPhoneCode SUDAN = fromString("+249");
    /**
     * Country phone code {@code +597}.
     */
    public static final CountryPhoneCode SURINAME = fromString("+597");
    /**
     * Country phone code {@code +268}.
     */
    public static final CountryPhoneCode SWAZILAND = fromString("+268");
    /**
     * Country phone code {@code +46}.
     */
    public static final CountryPhoneCode SWEDEN = fromString("+46");
    /**
     * Country phone code {@code +41}.
     */
    public static final CountryPhoneCode SWITZERLAND = fromString("+41");
    /**
     * Country phone code {@code +963}.
     */
    public static final CountryPhoneCode SYRIA = fromString("+963");
    /**
     * Country phone code {@code +886}.
     */
    public static final CountryPhoneCode TAIWAN = fromString("+886");
    /**
     * Country phone code {@code +992}.
     */
    public static final CountryPhoneCode TAJIKISTAN = fromString("+992");
    /**
     * Country phone code {@code +255}.
     */
    public static final CountryPhoneCode TANZANIA = fromString("+255");
    /**
     * Country phone code {@code +66}.
     */
    public static final CountryPhoneCode THAILAND = fromString("+66");
    /**
     * Country phone code {@code +670}.
     */
    public static final CountryPhoneCode TIMOR_LESTE = fromString("+670");
    /**
     * Country phone code {@code +228}.
     */
    public static final CountryPhoneCode TOGO = fromString("+228");
    /**
     * Country phone code {@code +690}.
     */
    public static final CountryPhoneCode TOKELAU = fromString("+690");
    /**
     * Country phone code {@code +676}.
     */
    public static final CountryPhoneCode TONGA = fromString("+676");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode TRINIDAD_AND_TOBAGO = fromString("+1");
    /**
     * Country phone code {@code +290}.
     */
    public static final CountryPhoneCode TRISTAN_DA_CUNHA = fromString("+290");
    /**
     * Country phone code {@code +216}.
     */
    public static final CountryPhoneCode TUNISIA = fromString("+216");
    /**
     * Country phone code {@code +90}.
     */
    public static final CountryPhoneCode TURKEY = fromString("+90");
    /**
     * Country phone code {@code +993}.
     */
    public static final CountryPhoneCode TURKMENISTAN = fromString("+993");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode TURKS_AND_CAICOS_ISLANDS = fromString("+1");
    /**
     * Country phone code {@code +688}.
     */
    public static final CountryPhoneCode TUVALU = fromString("+688");
    /**
     * Country phone code {@code +256}.
     */
    public static final CountryPhoneCode UGANDA = fromString("+256");
    /**
     * Country phone code {@code +380}.
     */
    public static final CountryPhoneCode UKRAINE = fromString("+380");
    /**
     * Country phone code {@code +971}.
     */
    public static final CountryPhoneCode UNITED_ARAB_EMIRATES = fromString("+971");
    /**
     * Country phone code {@code +44}.
     */
    public static final CountryPhoneCode UNITED_KINGDOM = fromString("+44");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode UNITED_STATES = fromString("+1");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode UNITED_STATES_MINOR_OUTLYING_ISLANDS = fromString("+1");
    /**
     * Country phone code {@code +598}.
     */
    public static final CountryPhoneCode URUGUAY = fromString("+598");
    /**
     * Country phone code {@code +998}.
     */
    public static final CountryPhoneCode UZBEKISTAN = fromString("+998");
    /**
     * Country phone code {@code +678}.
     */
    public static final CountryPhoneCode VANUATU = fromString("+678");
    /**
     * Country phone code {@code +58}.
     */
    public static final CountryPhoneCode VENEZUELA = fromString("+58");
    /**
     * Country phone code {@code +84}.
     */
    public static final CountryPhoneCode VIETNAM = fromString("+84");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode VIRGIN_ISLANDS_BRITISH = fromString("+1");
    /**
     * Country phone code {@code +1}.
     */
    public static final CountryPhoneCode VIRGIN_ISLANDS_US = fromString("+1");
    /**
     * Country phone code {@code +681}.
     */
    public static final CountryPhoneCode WALLIS_AND_FUTUNA = fromString("+681");
    /**
     * Country phone code {@code +967}.
     */
    public static final CountryPhoneCode YEMEN = fromString("+967");
    /**
     * Country phone code {@code +260}.
     */
    public static final CountryPhoneCode ZAMBIA = fromString("+260");
    /**
     * Country phone code {@code +263}.
     */
    public static final CountryPhoneCode ZIMBABWE = fromString("+263");

    /**
     * Creates a new instance of CountryPhoneCode value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public CountryPhoneCode() {
    }

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
     * Gets known CountryPhoneCode values.
     *
     * @return known country phone codes
     */
    public static Collection<CountryPhoneCode> values() {
        return values(CountryPhoneCode.class);
    }
}
