package br.com.carbon.carbon

import br.com.carbon.FlowTests
import br.com.carbon.flows.carbon.CreateCarbonRegisterFlow
import br.com.carbon.models.CarbonRegisterDTO
import br.com.carbon.states.CarbonRegister
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import org.junit.Test
import kotlin.test.assertNotNull

class CreationCarbonRegisterTest : FlowTests() {

    @Test
    fun `Carbon Register Creation - Vanilla Test`() {
        val carbonDTO = CarbonRegisterDTO(
            batchId = UniqueIdentifier(),
            totalTCO2e = 99.00,
            description = "Carbon Register is a good DAM thing!"
        )

        val createCarbonFlow = CreateCarbonRegisterFlow(
            carbonRegisterDTO = carbonDTO
        )

        val resultOfIssuing = nodeA.runFlow(createCarbonFlow).getOrThrow()

        val bikeStateAndRef = nodeA.services.vaultService.queryBy<CarbonRegister>().states
            .filter { it.state.data.linearId == resultOfIssuing.carbonReportLinearId }[0]

        assertNotNull(bikeStateAndRef)
        assertNotNull(bikeStateAndRef.state.data)
    }

}