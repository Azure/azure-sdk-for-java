@description('Ledger Name')
@minLength(3)
@maxLength(24)
param ledgerName string = 'tssdkci-${uniqueString(resourceGroup().id)}'
// resourceGroup().name is too long
// uniqueString is 13 characters long
// https://docs.microsoft.com/en-us/azure/azure-resource-manager/bicep/bicep-functions-string#uniquestring
// Prepend a string to easy identification + ledger name must start with a letter.

@description('Oid of the user')
param principalId string = 'ec667af1-0642-45f0-be8a-b76758a35dde'

@description('Location for all resources.')
param location string = 'EastUS'
// Explicitly set a region due to regional restrictions e.g. ACL is not currently available in westus

param tenantId string
param testApplicationId string

var ledgerUri = 'https://${ledgerName}.confidential-ledger.azure.com'

resource baseName_resource 'Microsoft.ConfidentialLedger/ledgers@2020-12-01-preview' = {
  name: ledgerName
  location: location
  properties: {
    ledgerType: 'Public'
    aadBasedSecurityPrincipals: [
      {
        principalId: principalId
        tenantId: tenantId
        ledgerRoleName: 'Administrator'
      }
      {
        principalId: testApplicationId
        ledgerRoleName: 'Administrator'
      }
    ]
  }
}

output LEDGER_NAME string = ledgerName
output LEDGERURI string = ledgerUri
output IDENTITY_SERVICE_URL string = 'https://identity.confidential-ledger.core.azure.com'
