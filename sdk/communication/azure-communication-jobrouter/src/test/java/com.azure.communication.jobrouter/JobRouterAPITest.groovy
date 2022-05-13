package com.azure.communication.jobrouter

import com.azure.communication.jobrouter.models.DistributionMode
import com.azure.communication.jobrouter.models.DistributionPolicy

class JobRouterAPITest extends APISpec {

    def "Create distribution policy"() {
        setup:
        var distributionPolicy = new DistributionPolicy()
        var distributionMode = new DistributionMode()
        distributionMode.setMinConcurrentOffers(1)
        distributionMode.setMaxConcurrentOffers(10)
        distributionPolicy.setMode(distributionMode)
        distributionPolicy.setName("Test_Policy")
        distributionPolicy.setOfferTtlSeconds(10)

        when:
        var id = "Contoso_Jobs_Distribution_policy"
        jrc.upsertDistributionPolicyWithResponse(id, distributionPolicy);

        then:
        println "OK"
    }
}
