Feature: Managing Storage accounts 

Background: 
	Given I create a "StorageManagementClient" with name "management" 
	
Scenario: Create, List and Delete a storage account 
	Given I create a "Microsoft.WindowsAzure.Management.Storage.Models.StorageAccountCreateParameters" with name "parameters"
	And I create a "10" character random String with name "testStorageAccountName1" and prefix "azurejavatest"
	And set "parameters.Name" with "testStorageAccountName1" of type "System.String" 
	And set "parameters.Location" with value "West US" of type "System.String" 
	And set "parameters.Description" with value "Hi there" of type "System.String" 
	And set "parameters.Label" with value "Great St account" of type "System.String" 
	When I invoke "management.StorageAccountsOperations.Create" with parameter "parameters" I get the result into "operationResponse" 
	Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"

Scenario: List a storage account
	Given I create a "Microsoft.WindowsAzure.Management.Storage.Models.StorageAccountCreateParameters" with name "parameters"
	And I create a "10" character random String with name "testStorageAccountName2" and prefix "azurejavatest"
	And set "parameters.Name" with "testStorageAccountName2" of type "System.String" 
	And set "parameters.Location" with value "West US" of type "System.String" 
	And set "parameters.Description" with value "Hi there" of type "System.String" 
	And set "parameters.Label" with value "Great St account" of type "System.String" 
	When I invoke "management.StorageAccountsOperations.Create" with parameter "parameters" I get the result into "operationResponse" 
	When I invoke "management.StorageAccountsOperations.List" I get the result into "operationResponse" 
	Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
	And property with type "System.String" and path "operationResponse.RequestId" should not equal "null" 
	And set "element" with value from list "operationResponse.StorageAccounts" where "Name" of type "System.String" equals parameter "testStorageAccountName2" 
	And property with type "System.String" and path "element.Name" should equal parameter "testStorageAccountName2" 
	
Scenario: Delete storage account 
	Given I create a "Microsoft.WindowsAzure.Management.Storage.Models.StorageAccountCreateParameters" with name "parameters"
	And I create a "10" character random String with name "testStorageAccountName3" and prefix "azurejavatest"
	And set "parameters.Name" with "testStorageAccountName3" of type "System.String" 
	And set "parameters.Location" with value "West US" of type "System.String" 
	And set "parameters.Description" with value "Hi there" of type "System.String" 
	And set "parameters.Label" with value "Great St account" of type "System.String" 
	When I invoke "management.StorageAccountsOperations.Create" with parameter "parameters" I get the result into "operationResponse" 
	When I invoke "management.StorageAccountsOperations.Delete" with parameter "testStorageAccountName3" I get the result into "operationResponse" 
	Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
	And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"