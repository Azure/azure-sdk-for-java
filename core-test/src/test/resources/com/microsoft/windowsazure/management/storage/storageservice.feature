Feature: Managing Storage accounts

Background: 
    Given I create a "StorageManagementClient" with name "management"

Scenario: Create a storage account
    Given I create a "Microsoft.WindowsAzure.Management.Storage.Models.StorageAccountCreateParameters" with name "parameters"
    And set "parameters.ServiceName" with value "mystaccount" of type "System.String"
    And set "parameters.Location" with value "West US" of type "System.String"
    And set "parameters.Description" with value "Hi there" of type "System.String"
    And set "parameters.Label" with value "Great St account" of type "System.String"
    When I invoke "management.StorageAccountsOperations.Create" with parameter "parameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "201"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"

Scenario: List storage accounts
    When I invoke "management.StorageAccountsOperations.List" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And set "element" with value from list "operationResponse.StorageAccounts" where "ServiceName" of type "System.String" equals "mystaccount"
    And property with type "System.String" and path "element.Name" should equal "mystaccount"

Scenario: Delete storage account
    When I invoke "management.StorageAccountsOperations.Delete" with parameter value "mystaccount" of type "System.String" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"