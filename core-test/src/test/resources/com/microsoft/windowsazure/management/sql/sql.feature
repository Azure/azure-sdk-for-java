Feature: SqlServer should work

Background: 
    Given I create a "SqlManagementClient" with name "management"

Scenario: Create SQL Server
    Given I create a "Microsoft.WindowsAzure.Management.Sql.Models.ServerCreateParameters" with name "parameters"
    And set "parameters.Location" with value "West US" of type "System.String"
    And set "parameters.AdministratorPassword" with value "FooBar12!" of type "System.String"
    And set "parameters.AdministratorUserName" with value "andrerod" of type "System.String"
    And I invoke "management.ServersOperations.Create" with parameter "parameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "201"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"

Scenario: List SQL Server
    When I invoke "management.ServersOperations.List" I get the result into "operationResponse" 
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null" 