// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets the PII entity category inferred by the text analytics service's PII entity recognition model.
 * The list of available categories is described at
 * See <a href="https://docs.microsoft.com/azure/cognitive-services/Text-Analytics/named-entity-types?tabs=personal">PII entity types</a>.
 */
@Immutable
public final class PiiEntityCategory extends ExpandableStringEnum<PiiEntityCategory> {
    /** Static value ABARoutingNumber for PiiEntityCategory. */
    public static final PiiEntityCategory ABA_ROUTING_NUMBER = fromString("ABARoutingNumber");

    /** Static value ARNationalIdentityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AR_NATIONAL_IDENTITY_NUMBER = fromString("ARNationalIdentityNumber");

    /** Static value AUBankAccountNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AU_BANK_ACCOUNT_NUMBER = fromString("AUBankAccountNumber");

    /** Static value AUDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AU_DRIVERS_LICENSE_NUMBER = fromString("AUDriversLicenseNumber");

    /** Static value AUMedicalAccountNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AU_MEDICAL_ACCOUNT_NUMBER = fromString("AUMedicalAccountNumber");

    /** Static value AUPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AU_PASSPORT_NUMBER = fromString("AUPassportNumber");

    /** Static value AUTaxFileNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AU_TAX_FILE_NUMBER = fromString("AUTaxFileNumber");

    /** Static value AUBusinessNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AU_BUSINESS_NUMBER = fromString("AUBusinessNumber");

    /** Static value AUCompanyNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AU_COMPANY_NUMBER = fromString("AUCompanyNumber");

    /** Static value ATIdentityCard for PiiEntityCategory. */
    public static final PiiEntityCategory AT_IDENTITY_CARD = fromString("ATIdentityCard");

    /** Static value ATTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AT_TAX_IDENTIFICATION_NUMBER = fromString("ATTaxIdentificationNumber");

    /** Static value ATValueAddedTaxNumber for PiiEntityCategory. */
    public static final PiiEntityCategory AT_VALUE_ADDED_TAX_NUMBER = fromString("ATValueAddedTaxNumber");

    /** Static value AzureDocumentDBAuthKey for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_DOCUMENT_DB_AUTH_KEY = fromString("AzureDocumentDBAuthKey");

    /** Static value AzureIAASDatabaseConnectionAndSQLString for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_IAAS_DATABASE_CONNECTION_AND_SQL_STRING =
            fromString("AzureIAASDatabaseConnectionAndSQLString");

    /** Static value AzureIoTConnectionString for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_IOT_CONNECTION_STRING = fromString("AzureIoTConnectionString");

    /** Static value AzurePublishSettingPassword for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_PUBLISH_SETTING_PASSWORD = fromString("AzurePublishSettingPassword");

    /** Static value AzureRedisCacheString for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_REDIS_CACHE_STRING = fromString("AzureRedisCacheString");

    /** Static value AzureSAS for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_SAS = fromString("AzureSAS");

    /** Static value AzureServiceBusString for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_SERVICE_BUS_STRING = fromString("AzureServiceBusString");

    /** Static value AzureStorageAccountKey for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_STORAGE_ACCOUNT_KEY = fromString("AzureStorageAccountKey");

    /** Static value AzureStorageAccountGeneric for PiiEntityCategory. */
    public static final PiiEntityCategory AZURE_STORAGE_ACCOUNT_GENERIC = fromString("AzureStorageAccountGeneric");

    /** Static value BENationalNumber for PiiEntityCategory. */
    public static final PiiEntityCategory BE_NATIONAL_NUMBER = fromString("BENationalNumber");

    /** Static value BENationalNumberV2 for PiiEntityCategory. */
    public static final PiiEntityCategory BE_NATIONAL_NUMBER_V2 = fromString("BENationalNumberV2");

    /** Static value BEValueAddedTaxNumber for PiiEntityCategory. */
    public static final PiiEntityCategory BE_VALUE_ADDED_TAX_NUMBER = fromString("BEValueAddedTaxNumber");

    /** Static value BRCPFNumber for PiiEntityCategory. */
    public static final PiiEntityCategory BR_CPF_NUMBER = fromString("BRCPFNumber");

    /** Static value BRLegalEntityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory BR_LEGAL_ENTITY_NUMBER = fromString("BRLegalEntityNumber");

    /** Static value BRNationalIDRG for PiiEntityCategory. */
    public static final PiiEntityCategory BR_NATIONAL_ID_RG = fromString("BRNationalIDRG");

    /** Static value BGUniformCivilNumber for PiiEntityCategory. */
    public static final PiiEntityCategory BG_UNIFORM_CIVIL_NUMBER = fromString("BGUniformCivilNumber");

    /** Static value CABankAccountNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CA_BANK_ACCOUNT_NUMBER = fromString("CABankAccountNumber");

    /** Static value CADriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CA_DRIVERS_LICENSE_NUMBER = fromString("CADriversLicenseNumber");

    /** Static value CAHealthServiceNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CA_HEALTH_SERVICE_NUMBER = fromString("CAHealthServiceNumber");

    /** Static value CAPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CA_PASSPORT_NUMBER = fromString("CAPassportNumber");

    /** Static value CAPersonalHealthIdentification for PiiEntityCategory. */
    public static final PiiEntityCategory CA_PERSONAL_HEALTH_IDENTIFICATION =
        fromString("CAPersonalHealthIdentification");

    /** Static value CASocialInsuranceNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CA_SOCIAL_INSURANCE_NUMBER = fromString("CASocialInsuranceNumber");

    /** Static value CLIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CL_IDENTITY_CARD_NUMBER = fromString("CLIdentityCardNumber");

    /** Static value CNResidentIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CN_RESIDENT_IDENTITY_CARD_NUMBER = fromString("CNResidentIdentityCardNumber");

    /** Static value CreditCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CREDIT_CARD_NUMBER = fromString("CreditCardNumber");

    /** Static value HRIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory HR_IDENTITY_CARD_NUMBER = fromString("HRIdentityCardNumber");

    /** Static value HRNationalIDNumber for PiiEntityCategory. */
    public static final PiiEntityCategory HR_NATIONAL_ID_NUMBER = fromString("HRNationalIDNumber");

    /** Static value HRPersonalIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory HR_PERSONAL_IDENTIFICATION_NUMBER =
        fromString("HRPersonalIdentificationNumber");

    /** Static value HRPersonalIdentificationOIBNumberV2 for PiiEntityCategory. */
    public static final PiiEntityCategory HR_PERSONAL_IDENTIFICATION_OIB_NUMBER_V2 =
        fromString("HRPersonalIdentificationOIBNumberV2");

    /** Static value CYIdentityCard for PiiEntityCategory. */
    public static final PiiEntityCategory CY_IDENTITY_CARD = fromString("CYIdentityCard");

    /** Static value CYTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CY_TAX_IDENTIFICATION_NUMBER = fromString("CYTaxIdentificationNumber");

    /** Static value CZPersonalIdentityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CZ_PERSONAL_IDENTITY_NUMBER = fromString("CZPersonalIdentityNumber");

    /** Static value CZPersonalIdentityV2 for PiiEntityCategory. */
    public static final PiiEntityCategory CZ_PERSONAL_IDENTITY_V2 = fromString("CZPersonalIdentityV2");

    /** Static value DKPersonalIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory DK_PERSONAL_IDENTIFICATION_NUMBER =
        fromString("DKPersonalIdentificationNumber");

    /** Static value DKPersonalIdentificationV2 for PiiEntityCategory. */
    public static final PiiEntityCategory DK_PERSONAL_IDENTIFICATION_V2 = fromString("DKPersonalIdentificationV2");

    /** Static value DrugEnforcementAgencyNumber for PiiEntityCategory. */
    public static final PiiEntityCategory DRUG_ENFORCEMENT_AGENCY_NUMBER = fromString("DrugEnforcementAgencyNumber");

    /** Static value EEPersonalIdentificationCode for PiiEntityCategory. */
    public static final PiiEntityCategory EE_PERSONAL_IDENTIFICATION_CODE = fromString("EEPersonalIdentificationCode");

    /** Static value EUDebitCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory EU_DEBIT_CARD_NUMBER = fromString("EUDebitCardNumber");

    /** Static value EUDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory EU_DRIVERS_LICENSE_NUMBER = fromString("EUDriversLicenseNumber");

    /** Static value EUGPSCoordinates for PiiEntityCategory. */
    public static final PiiEntityCategory EU_GPS_COORDINATES = fromString("EUGPSCoordinates");

    /** Static value EUNationalIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory EU_NATIONAL_IDENTIFICATION_NUMBER =
        fromString("EUNationalIdentificationNumber");

    /** Static value EUPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory EU_PASSPORT_NUMBER = fromString("EUPassportNumber");

    /** Static value EUSocialSecurityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory EU_SOCIAL_SECURITY_NUMBER = fromString("EUSocialSecurityNumber");

    /** Static value EUTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory EU_TAX_IDENTIFICATION_NUMBER = fromString("EUTaxIdentificationNumber");

    /** Static value FIEuropeanHealthNumber for PiiEntityCategory. */
    public static final PiiEntityCategory FI_EUROPEAN_HEALTH_NUMBER = fromString("FIEuropeanHealthNumber");

    /** Static value FINationalID for PiiEntityCategory. */
    public static final PiiEntityCategory FI_NATIONAL_ID = fromString("FINationalID");

    /** Static value FINationalIDV2 for PiiEntityCategory. */
    public static final PiiEntityCategory FI_NATIONAL_ID_V2 = fromString("FINationalIDV2");

    /** Static value FIPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory FI_PASSPORT_NUMBER = fromString("FIPassportNumber");

    /** Static value FRDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory FR_DRIVERS_LICENSE_NUMBER = fromString("FRDriversLicenseNumber");

    /** Static value FRHealthInsuranceNumber for PiiEntityCategory. */
    public static final PiiEntityCategory FR_HEALTH_INSURANCE_NUMBER = fromString("FRHealthInsuranceNumber");

    /** Static value FRNationalID for PiiEntityCategory. */
    public static final PiiEntityCategory FR_NATIONAL_ID = fromString("FRNationalID");

    /** Static value FRPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory FR_PASSPORT_NUMBER = fromString("FRPassportNumber");

    /** Static value FRSocialSecurityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory FR_SOCIAL_SECURITY_NUMBER = fromString("FRSocialSecurityNumber");

    /** Static value FRTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory FR_TAX_IDENTIFICATION_NUMBER = fromString("FRTaxIdentificationNumber");

    /** Static value FRValueAddedTaxNumber for PiiEntityCategory. */
    public static final PiiEntityCategory FR_VALUE_ADDED_TAX_NUMBER = fromString("FRValueAddedTaxNumber");

    /** Static value DEDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory DE_DRIVERS_LICENSE_NUMBER = fromString("DEDriversLicenseNumber");

    /** Static value DEPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory DE_PASSPORT_NUMBER = fromString("DEPassportNumber");

    /** Static value DEIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory DE_IDENTITY_CARD_NUMBER = fromString("DEIdentityCardNumber");

    /** Static value DETaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory DE_TAX_IDENTIFICATION_NUMBER = fromString("DETaxIdentificationNumber");

    /** Static value DEValueAddedNumber for PiiEntityCategory. */
    public static final PiiEntityCategory DE_VALUE_ADDED_NUMBER = fromString("DEValueAddedNumber");

    /** Static value GRNationalIDCard for PiiEntityCategory. */
    public static final PiiEntityCategory GR_NATIONAL_ID_CARD = fromString("GRNationalIDCard");

    /** Static value GRNationalIDV2 for PiiEntityCategory. */
    public static final PiiEntityCategory GR_NATIONAL_ID_V2 = fromString("GRNationalIDV2");

    /** Static value GRTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory GR_TAX_IDENTIFICATION_NUMBER = fromString("GRTaxIdentificationNumber");

    /** Static value HKIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory HK_IDENTITY_CARD_NUMBER = fromString("HKIdentityCardNumber");

    /** Static value HUValueAddedNumber for PiiEntityCategory. */
    public static final PiiEntityCategory HU_VALUE_ADDED_NUMBER = fromString("HUValueAddedNumber");

    /** Static value HUPersonalIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory HU_PERSONAL_IDENTIFICATION_NUMBER = fromString("HUPersonalIdentificationNumber");

    /** Static value HUTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory HU_TAX_IDENTIFICATION_NUMBER = fromString("HUTaxIdentificationNumber");

    /** Static value INPermanentAccount for PiiEntityCategory. */
    public static final PiiEntityCategory IN_PERMANENT_ACCOUNT = fromString("INPermanentAccount");

    /** Static value INUniqueIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory IN_UNIQUE_IDENTIFICATION_NUMBER = fromString("INUniqueIdentificationNumber");

    /** Static value IDIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory ID_IDENTITY_CARD_NUMBER = fromString("IDIdentityCardNumber");

    /** Static value InternationalBankingAccountNumber for PiiEntityCategory. */
    public static final PiiEntityCategory INTERNATIONAL_BANKING_ACCOUNT_NUMBER =
            fromString("InternationalBankingAccountNumber");

    /** Static value IEPersonalPublicServiceNumber for PiiEntityCategory. */
    public static final PiiEntityCategory IE_PERSONAL_PUBLIC_SERVICE_NUMBER =
        fromString("IEPersonalPublicServiceNumber");

    /** Static value IEPersonalPublicServiceNumberV2 for PiiEntityCategory. */
    public static final PiiEntityCategory IE_PERSONAL_PUBLIC_SERVICE_NUMBER_V2 =
        fromString("IEPersonalPublicServiceNumberV2");

    /** Static value ILBankAccountNumber for PiiEntityCategory. */
    public static final PiiEntityCategory IL_BANK_ACCOUNT_NUMBER = fromString("ILBankAccountNumber");

    /** Static value ILNationalID for PiiEntityCategory. */
    public static final PiiEntityCategory IL_NATIONAL_ID = fromString("ILNationalID");

    /** Static value ITDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory IT_DRIVERS_LICENSE_NUMBER = fromString("ITDriversLicenseNumber");

    /** Static value ITFiscalCode for PiiEntityCategory. */
    public static final PiiEntityCategory IT_FISCAL_CODE = fromString("ITFiscalCode");

    /** Static value ITValueAddedTaxNumber for PiiEntityCategory. */
    public static final PiiEntityCategory IT_VALUE_ADDED_TAX_NUMBER = fromString("ITValueAddedTaxNumber");

    /** Static value JPBankAccountNumber for PiiEntityCategory. */
    public static final PiiEntityCategory JP_BANK_ACCOUNT_NUMBER = fromString("JPBankAccountNumber");

    /** Static value JPDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory JP_DRIVERS_LICENSE_NUMBER = fromString("JPDriversLicenseNumber");

    /** Static value JPPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory JP_PASSPORT_NUMBER = fromString("JPPassportNumber");

    /** Static value JPResidentRegistrationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory JP_RESIDENT_REGISTRATION_NUMBER = fromString("JPResidentRegistrationNumber");

    /** Static value JPSocialInsuranceNumber for PiiEntityCategory. */
    public static final PiiEntityCategory JP_SOCIAL_INSURANCE_NUMBER = fromString("JPSocialInsuranceNumber");

    /** Static value JPMyNumberCorporate for PiiEntityCategory. */
    public static final PiiEntityCategory JP_MY_NUMBER_CORPORATE = fromString("JPMyNumberCorporate");

    /** Static value JPMyNumberPersonal for PiiEntityCategory. */
    public static final PiiEntityCategory JP_MY_NUMBER_PERSONAL = fromString("JPMyNumberPersonal");

    /** Static value JPResidenceCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory JP_RESIDENCE_CARD_NUMBER = fromString("JPResidenceCardNumber");

    /** Static value LVPersonalCode for PiiEntityCategory. */
    public static final PiiEntityCategory LV_PERSONAL_CODE = fromString("LVPersonalCode");

    /** Static value LTPersonalCode for PiiEntityCategory. */
    public static final PiiEntityCategory LT_PERSONAL_CODE = fromString("LTPersonalCode");

    /** Static value LUNationalIdentificationNumberNatural for PiiEntityCategory. */
    public static final PiiEntityCategory LU_NATIONAL_IDENTIFICATION_NUMBER_NATURAL =
            fromString("LUNationalIdentificationNumberNatural");

    /** Static value LUNationalIdentificationNumberNonNatural for PiiEntityCategory. */
    public static final PiiEntityCategory LU_NATIONAL_IDENTIFICATION_NUMBER_NON_NATURAL =
            fromString("LUNationalIdentificationNumberNonNatural");

    /** Static value MYIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory MY_IDENTITY_CARD_NUMBER = fromString("MYIdentityCardNumber");

    /** Static value MTIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory MT_IDENTITY_CARD_NUMBER = fromString("MTIdentityCardNumber");

    /** Static value MTTaxIDNumber for PiiEntityCategory. */
    public static final PiiEntityCategory MT_TAX_ID_NUMBER = fromString("MTTaxIDNumber");

    /** Static value NLCitizensServiceNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NL_CITIZENS_SERVICE_NUMBER = fromString("NLCitizensServiceNumber");

    /** Static value NLCitizensServiceNumberV2 for PiiEntityCategory. */
    public static final PiiEntityCategory NL_CITIZENS_SERVICE_NUMBER_V2 = fromString("NLCitizensServiceNumberV2");

    /** Static value NLTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NL_TAX_IDENTIFICATION_NUMBER = fromString("NLTaxIdentificationNumber");

    /** Static value NLValueAddedTaxNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NL_VALUE_ADDED_TAX_NUMBER = fromString("NLValueAddedTaxNumber");

    /** Static value NZBankAccountNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NZ_BANK_ACCOUNT_NUMBER = fromString("NZBankAccountNumber");

    /** Static value NZDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NZ_DRIVERS_LICENSE_NUMBER = fromString("NZDriversLicenseNumber");

    /** Static value NZInlandRevenueNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NZ_INLAND_REVENUE_NUMBER = fromString("NZInlandRevenueNumber");

    /** Static value NZMinistryOfHealthNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NZ_MINISTRY_OF_HEALTH_NUMBER = fromString("NZMinistryOfHealthNumber");

    /** Static value NZSocialWelfareNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NZ_SOCIAL_WELFARE_NUMBER = fromString("NZSocialWelfareNumber");

    /** Static value NOIdentityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory NO_IDENTITY_NUMBER = fromString("NOIdentityNumber");

    /** Static value PHUnifiedMultiPurposeIDNumber for PiiEntityCategory. */
    public static final PiiEntityCategory PH_UNIFIED_MULTI_PURPOSE_ID_NUMBER =
        fromString("PHUnifiedMultiPurposeIDNumber");

    /** Static value PLIdentityCard for PiiEntityCategory. */
    public static final PiiEntityCategory PL_IDENTITY_CARD = fromString("PLIdentityCard");

    /** Static value PLNationalID for PiiEntityCategory. */
    public static final PiiEntityCategory PL_NATIONAL_ID = fromString("PLNationalID");

    /** Static value PLNationalIDV2 for PiiEntityCategory. */
    public static final PiiEntityCategory PL_NATIONAL_ID_V2 = fromString("PLNationalIDV2");

    /** Static value PLPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory PL_PASSPORT_NUMBER = fromString("PLPassportNumber");

    /** Static value PLTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory PL_TAX_IDENTIFICATION_NUMBER = fromString("PLTaxIdentificationNumber");

    /** Static value PLREGONNumber for PiiEntityCategory. */
    public static final PiiEntityCategory PL_REGON_NUMBER = fromString("PLREGONNumber");

    /** Static value PTCitizenCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory PT_CITIZEN_CARD_NUMBER = fromString("PTCitizenCardNumber");

    /** Static value PTCitizenCardNumberV2 for PiiEntityCategory. */
    public static final PiiEntityCategory PT_CITIZEN_CARD_NUMBER_V2 = fromString("PTCitizenCardNumberV2");

    /** Static value PTTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory PT_TAX_IDENTIFICATION_NUMBER = fromString("PTTaxIdentificationNumber");

    /** Static value ROPersonalNumericalCode for PiiEntityCategory. */
    public static final PiiEntityCategory RO_PERSONAL_NUMERICAL_CODE = fromString("ROPersonalNumericalCode");

    /** Static value RUPassportNumberDomestic for PiiEntityCategory. */
    public static final PiiEntityCategory RU_PASSPORT_NUMBER_DOMESTIC = fromString("RUPassportNumberDomestic");

    /** Static value RUPassportNumberInternational for PiiEntityCategory. */
    public static final PiiEntityCategory RU_PASSPORT_NUMBER_INTERNATIONAL =
        fromString("RUPassportNumberInternational");

    /** Static value SANationalID for PiiEntityCategory. */
    public static final PiiEntityCategory SA_NATIONAL_ID = fromString("SANationalID");

    /** Static value SGNationalRegistrationIdentityCardNumber for PiiEntityCategory. */
    public static final PiiEntityCategory SG_NATIONAL_REGISTRATION_IDENTITY_CARD_NUMBER =
            fromString("SGNationalRegistrationIdentityCardNumber");

    /** Static value SKPersonalNumber for PiiEntityCategory. */
    public static final PiiEntityCategory SK_PERSONAL_NUMBER = fromString("SKPersonalNumber");

    /** Static value SITaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory SI_TAX_IDENTIFICATION_NUMBER = fromString("SITaxIdentificationNumber");

    /** Static value SIUniqueMasterCitizenNumber for PiiEntityCategory. */
    public static final PiiEntityCategory SI_UNIQUE_MASTER_CITIZEN_NUMBER = fromString("SIUniqueMasterCitizenNumber");

    /** Static value ZAIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory ZA_IDENTIFICATION_NUMBER = fromString("ZAIdentificationNumber");

    /** Static value KRResidentRegistrationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory KR_RESIDENT_REGISTRATION_NUMBER = fromString("KRResidentRegistrationNumber");

    /** Static value ESDNI for PiiEntityCategory. */
    public static final PiiEntityCategory ES_DNI = fromString("ESDNI");

    /** Static value ESSocialSecurityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory ES_SOCIAL_SECURITY_NUMBER = fromString("ESSocialSecurityNumber");

    /** Static value ESTaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory ES_TAX_IDENTIFICATION_NUMBER = fromString("ESTaxIdentificationNumber");

    /** Static value SQLServerConnectionString for PiiEntityCategory. */
    public static final PiiEntityCategory SQL_SERVER_CONNECTION_STRING = fromString("SQLServerConnectionString");

    /** Static value SENationalID for PiiEntityCategory. */
    public static final PiiEntityCategory SE_NATIONAL_ID = fromString("SENationalID");

    /** Static value SENationalIDV2 for PiiEntityCategory. */
    public static final PiiEntityCategory SE_NATIONAL_ID_V2 = fromString("SENationalIDV2");

    /** Static value SEPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory SE_PASSPORT_NUMBER = fromString("SEPassportNumber");

    /** Static value SETaxIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory SE_TAX_IDENTIFICATION_NUMBER = fromString("SETaxIdentificationNumber");

    /** Static value SWIFTCode for PiiEntityCategory. */
    public static final PiiEntityCategory SWIFT_CODE = fromString("SWIFTCode");

    /** Static value CHSocialSecurityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory CH_SOCIAL_SECURITY_NUMBER = fromString("CHSocialSecurityNumber");

    /** Static value TWNationalID for PiiEntityCategory. */
    public static final PiiEntityCategory TW_NATIONAL_ID = fromString("TWNationalID");

    /** Static value TWPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory TW_PASSPORT_NUMBER = fromString("TWPassportNumber");

    /** Static value TWResidentCertificate for PiiEntityCategory. */
    public static final PiiEntityCategory TW_RESIDENT_CERTIFICATE = fromString("TWResidentCertificate");

    /** Static value THPopulationIdentificationCode for PiiEntityCategory. */
    public static final PiiEntityCategory TH_POPULATION_IDENTIFICATION_CODE =
        fromString("THPopulationIdentificationCode");

    /** Static value TRNationalIdentificationNumber for PiiEntityCategory. */
    public static final PiiEntityCategory TR_NATIONAL_IDENTIFICATION_NUMBER =
        fromString("TRNationalIdentificationNumber");

    /** Static value UKDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory UK_DRIVERS_LICENSE_NUMBER = fromString("UKDriversLicenseNumber");

    /** Static value UKElectoralRollNumber for PiiEntityCategory. */
    public static final PiiEntityCategory UK_ELECTORAL_ROLL_NUMBER = fromString("UKElectoralRollNumber");

    /** Static value UKNationalHealthNumber for PiiEntityCategory. */
    public static final PiiEntityCategory UK_NATIONAL_HEALTH_NUMBER = fromString("UKNationalHealthNumber");

    /** Static value UKNationalInsuranceNumber for PiiEntityCategory. */
    public static final PiiEntityCategory UK_NATIONAL_INSURANCE_NUMBER = fromString("UKNationalInsuranceNumber");

    /** Static value UKUniqueTaxpayerNumber for PiiEntityCategory. */
    public static final PiiEntityCategory UK_UNIQUE_TAXPAYER_NUMBER = fromString("UKUniqueTaxpayerNumber");

    /** Static value USUKPassportNumber for PiiEntityCategory. */
    public static final PiiEntityCategory US_UK_PASSPORT_NUMBER = fromString("USUKPassportNumber");

    /** Static value USBankAccountNumber for PiiEntityCategory. */
    public static final PiiEntityCategory US_BANK_ACCOUNT_NUMBER = fromString("USBankAccountNumber");

    /** Static value USDriversLicenseNumber for PiiEntityCategory. */
    public static final PiiEntityCategory US_DRIVERS_LICENSE_NUMBER = fromString("USDriversLicenseNumber");

    /** Static value USIndividualTaxpayerIdentification for PiiEntityCategory. */
    public static final PiiEntityCategory US_INDIVIDUAL_TAXPAYER_IDENTIFICATION =
            fromString("USIndividualTaxpayerIdentification");

    /** Static value USSocialSecurityNumber for PiiEntityCategory. */
    public static final PiiEntityCategory US_SOCIAL_SECURITY_NUMBER = fromString("USSocialSecurityNumber");

    /** Static value UAPassportNumberDomestic for PiiEntityCategory. */
    public static final PiiEntityCategory UA_PASSPORT_NUMBER_DOMESTIC = fromString("UAPassportNumberDomestic");

    /** Static value UAPassportNumberInternational for PiiEntityCategory. */
    public static final PiiEntityCategory UA_PASSPORT_NUMBER_INTERNATIONAL = fromString("UAPassportNumberInternational");

    /** Static value Organization for PiiEntityCategory. */
    public static final PiiEntityCategory ORGANIZATION = fromString("Organization");

    /** Static value Email for PiiEntityCategory. */
    public static final PiiEntityCategory EMAIL = fromString("Email");

    /** Static value URL for PiiEntityCategory. */
    public static final PiiEntityCategory URL = fromString("URL");

    /** Static value Age for PiiEntityCategory. */
    public static final PiiEntityCategory AGE = fromString("Age");

    /** Static value PhoneNumber for PiiEntityCategory. */
    public static final PiiEntityCategory PHONE_NUMBER = fromString("PhoneNumber");

    /** Static value IPAddress for PiiEntityCategory. */
    public static final PiiEntityCategory IP_ADDRESS = fromString("IPAddress");

    /** Static value Date for PiiEntityCategory. */
    public static final PiiEntityCategory DATE = fromString("Date");

    /** Static value Person for PiiEntityCategory. */
    public static final PiiEntityCategory PERSON = fromString("Person");

    /** Static value Address for PiiEntityCategory. */
    public static final PiiEntityCategory ADDRESS = fromString("Address");

    /** Static value All for PiiEntityCategory. */
    public static final PiiEntityCategory ALL = fromString("All");

    /** Static value Default for PiiEntityCategory. */
    public static final PiiEntityCategory DEFAULT = fromString("Default");

    /**
     * Creates or finds a PiiEntityCategory from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding PiiEntityCategory.
     */
    @JsonCreator
    public static PiiEntityCategory fromString(String name) {
        return fromString(name, PiiEntityCategory.class);
    }
}
