Feature: Management Certificates should work

Background: 
    Given I create a "ManagementClient" with name "management"

Scenario: Get a Certificate
    When I invoke "management.ManagementCertificatesOperations.Get" with parameter value "34738E2952C1ED45867CEAFFC38E0501F8681C38" of type "System.String" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"