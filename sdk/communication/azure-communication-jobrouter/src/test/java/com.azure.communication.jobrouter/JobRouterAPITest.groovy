package com.azure.communication.jobrouter


import com.azure.communication.jobrouter.models.DistributionPolicy
import com.azure.communication.jobrouter.models.RoundRobinMode

class JobRouterAPITest extends APISpec {

    def "Create distribution policy"() {
        setup:
        var distributionPolicy = new DistributionPolicy()
        RoundRobinMode roundRobinMode = new RoundRobinMode()
        roundRobinMode.setMinConcurrentOffers(1)
        roundRobinMode.setMaxConcurrentOffers(10)
        distributionPolicy.setMode(roundRobinMode)
        distributionPolicy.setName("Test_Policy")
        distributionPolicy.setOfferTtlSeconds(10)

        when:
        var id = "Contoso_Jobs_Distribution_policy"
        var response = jrc.upsertDistributionPolicy(id, distributionPolicy);

        then:
        response.statusCode == 200
    }
}
