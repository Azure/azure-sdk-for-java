Feature: Locations should work 

Background: 
    Given I create a "ManagementClient" with name "management" 
    
Scenario: List Locations 
    When I invoke "management.LocationsOperations.List" I get the result into "operationResponse" 
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null" 
    And set "element1" with value from list "operationResponse.Locations" where "Name" of type "System.String" equals "Southeast Asia" 
    And property with type "System.String" and path "element1.Name" should equal "Southeast Asia" 
    And set "element2" with value from list "operationResponse.Locations" where "Name" of type "System.String" equals "East Asia" 
    And property with type "System.String" and path "element2.Name" should equal "East Asia"