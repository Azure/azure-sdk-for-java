Feature: SqlServer should work

Background: 
    Given I create a "SqlManagementClient" with name "management"

Scenario: Create SQL Server
    Given I create a "Microsoft.WindowsAzure.Management.Sql.Models.ServerCreateParameters" with name "parameters"
    And set "parameters.Location" with value "West US" of type "System.String"
    And set "parameters.AdministratorPassword" with value "TestPassword12!" of type "System.String"
    And set "parameters.AdministratorUserName" with value "testadministrator" of type "System.String"
    And I invoke "management.ServersOperations.Create" with parameter "parameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "201"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"

Scenario: List SQL Server
    When I invoke "management.ServersOperations.List" I get the result into "operationResponse" 
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And set "element1" with value from list "operationResponse.Servers" where "Location" of type "System.String" equals "West US"

Scenario: Create SQL Database
    Given I invoke "management.ServersOperations.List" I get the result into "operationResponse" 
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And set "element1" with value from list "operationResponse.Servers" where "Location" of type "System.String" equals "West US"
    And set "serverName" with value from path "element1.Name" of type "System.String"
    When I create a "Microsoft.WindowsAzure.Management.Sql.Models.DatabaseCreateParameters" with name "parameters"
    And set "parameters.MaximumDatabaseSizeInGB" with value "1" of type "System.Int32"
    And set "parameters.Name" with value "expecteddatabasename" of type "System.String"
    And set "parameters.CollationName" with value "SQL_Latin1_General_CP1_CI_AS" of type "System.String"
    And set "parameters.Edition" with value "Web" of type "System.String"
    And I invoke "management.DatabasesOperations.Create" with parameters "serverName" and "parameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "201"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    
Scenario: Delete SQL Server
    Given I invoke "management.ServersOperations.List" I get the result into "operationResponse" 
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And set "element1" with value from list "operationResponse.Servers" where "Location" of type "System.String" equals "West US"
    And set "serverName" with value from path "element1.Name" of type "System.String"
    And I create a "System.String" with name "param1"
    And set "param1" with value "expecteddatabasename" of type "System.String"
    When I invoke "management.DatabasesOperations.Delete" with parameters "serverName" and "param1" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    