package br.com.carbon.carbon

import br.com.carbon.FlowTests
import br.com.carbon.flows.carbon.CreateCarbonRegisterFlow
import br.com.carbon.flows.carbon.IssueCarbonFungiblesFlow
import br.com.carbon.flows.carbon.MoveCarbonFungiblesFlow
import br.com.carbon.models.CarbonRegisterDTO
import br.com.carbon.service.CarbonQueryService
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import org.junit.Test

class MoveCarbonFungiblesTest : FlowTests() {

    @Test
    fun `Token Move - Vanilla Test`() {
        val tokenIdentifier = "CARBON"

        val aliceAccount = createAccount(
            mockNet = network,
            node = nodeA,
            name = "Alice ACC - NodeA"
        )

        val bobAccount = createAccount(
            mockNet = network,
            node = nodeB,
            name = "Bob ACC - NodeB"
        )

        val carbonDTO = CarbonRegisterDTO(
            batchId = UniqueIdentifier(),
            totalTCO2e = 99.00,
            description = "Carbon Register is a good DAM thing!"
        )

        val createCarbonFlow = CreateCarbonRegisterFlow(
            carbonRegisterDTO = carbonDTO
        )

        nodeA.runFlow(createCarbonFlow).getOrThrow()

        val issueCarbonFungibleFlow = IssueCarbonFungiblesFlow(
            carbonBatchId = carbonDTO.batchId,
            holderAccountInfo = aliceAccount,
            amount = 10.00,
            tokenIdentifierString = tokenIdentifier
        )

        nodeA.runFlow(issueCarbonFungibleFlow).getOrThrow()

        val moveCarbonFungibleFlow = MoveCarbonFungiblesFlow.MoveCarbonFungiblesInitiatingFlow(
            amount = 5.00,
            tokenIdentifierString = tokenIdentifier,
            fractionDigits = 0,
            holderAccountInfo = aliceAccount,
            newHolderAccountInfo = bobAccount
        )

        nodeA.runFlow(moveCarbonFungibleFlow).getOrThrow()


        val nodeBCarbonQueryService = nodeB.services.cordaService(CarbonQueryService::class.java)

        val carbonFungiblesOfNodeB =
            nodeBCarbonQueryService.getCustomFungiblesOfAccount(
                aliceAccount,
                tokenIdentifier
            )

        assert(carbonFungiblesOfNodeB.states.isNotEmpty())
    }

}