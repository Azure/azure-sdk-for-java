Feature: ServerFarm should work

Background: 
    Given I create a "WebSiteManagementClient" with name "management"

Scenario: Create Server Farm
    Given I create a "Microsoft.WindowsAzure.Management.WebSites.Models.ServerFarmCreateParameters" with name "parameters"
    And set "parameters.NumberOfWorkers" with value "1" of type "System.Int32"
    And set "parameters.WorkerSize" with value "Small" of type "Microsoft.WindowsAzure.Management.WebSites.Models.ServerFarmWorkerSize"
    And I create a "System.String" with name "param1"
    And set "param1" with value "eastuswebspace" of type "System.String"
    And I invoke "management.ServerFarmsOperations.Create" with parameters "param1" and "parameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"

Scenario: List Server Farm
    When I invoke "management.ServerFarmsOperations.List" with parameter value "eastuswebspace" of type "System.String" I get the result into "operationResponse" 
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"

Scenario: Delete Server Farm
    When I invoke "management.ServerFarmsOperations.Delete" with parameter value "eastuswebspace" of type "System.String" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
