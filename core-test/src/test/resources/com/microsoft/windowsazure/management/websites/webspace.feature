Feature: WebSpace should work

Background: 
    Given I create a "WebSiteManagementClient" with name "management"

Scenario: List webspaces
    When I invoke "management.WebSpacesOperations.List" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And set "element1" with value from list "operationResponse.WebSpaces" where "GeoRegion" of type "System.String" equals "East US"
    And property with type "System.String" and path "element1.GeoRegion" should equal "East US"
    And property with type "System.String" and path "element1.Name" should equal "destanko-EastUSwebspace"
