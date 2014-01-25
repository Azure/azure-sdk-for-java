Feature: Management Certificates should work

Background: 
    Given I create a "ManagementClient" with name "management"

Scenario: Get a Certificate
    When I invoke "management.ManagementCertificatesOperations.Get" with parameter value "00EC75B90F4F59F172C3C321C150FE7E79FC29AB" of type "System.String" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"