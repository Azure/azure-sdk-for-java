Feature: WebSite should work

Background: 
    Given I create a "WebSiteManagementClient" with name "management"

Scenario: Create WebSite
    Given I create a "Microsoft.WindowsAzure.Management.WebSites.Models.WebSiteCreateParameters" with name "parameters"
    And set "webspace" with value "eastuswebspace" of type "System.String"
    And I create a "10" character random String with name "testWebSiteName1" and prefix "azurejavatest"
    And set "parameters.Name" with "testWebSiteName1" of type "System.String"
    And set "parameters.WebSpaceName" with value "eastuswebspace" of type "System.String"
    And I create a "Microsoft.WindowsAzure.Management.WebSites.Models.WebSiteCreateParameters.WebSpaceDetails" with name "webSpaceObj"
    And set "webSpaceObj.Name" with value "eastuswebspace" of type "System.String"
    And set "webSpaceObj.GeoRegion" with value "East US" of type "System.String"
    And set "webSpaceObj.Plan" with value "VirtualDedicatedPlan" of type "System.String"
    And set "parameters.WebSpace" with "webSpaceObj" of type "Microsoft.WindowsAzure.Management.WebSites.Models.WebSiteCreateParameters.WebSpaceDetails"
    And I create a "System.String" with name "param1"
    And set "param1" with value "eastuswebspace" of type "System.String"
    And I invoke "management.WebSitesOperations.Create" with parameters "param1" and "parameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And I create a "Microsoft.WindowsAzure.Management.WebSites.Models.WebSiteDeleteParameters" with name "deleteParameters" 
    When I invoke "management.WebSitesOperations.Delete" with parameters "webspace" and "testWebSiteName1" and "deleteParameters"

Scenario: List websites
    When I invoke "management.WebSpacesOperations.ListWebSites" with parameter values "eastuswebspace" of type "System.String" and "null" of type "Microsoft.WindowsAzure.Management.WebSites.Models.WebSiteListParameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And set "element1" with value from list "operationResponse.WebSites" where "Name" of type "System.String" equals "newtstsite"
    And property with type "System.String" and path "element1.Name" should equal "newtstsite"

Scenario: Create repository
    When I invoke "management.WebSitesOperations.CreateRepository" with parameter values "eastuswebspace" of type "System.String" and "newtstsite" of type "System.String" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"

Scenario: Generate password
    When I invoke "management.WebSitesOperations.GeneratePassword" with parameter values "eastuswebspace" of type "System.String" and "newtstsite" of type "System.String" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
