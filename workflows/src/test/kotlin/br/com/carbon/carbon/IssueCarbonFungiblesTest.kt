package br.com.carbon.carbon

import br.com.carbon.FlowTests
import br.com.carbon.flows.carbon.CreateCarbonRegisterFlow
import br.com.carbon.flows.carbon.IssueCarbonFungiblesFlow
import br.com.carbon.models.CarbonRegisterDTO
import br.com.carbon.service.CarbonQueryService
import br.com.carbon.service.VaultCommonQueryService
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


class IssueCarbonFungiblesTest : FlowTests() {

    @Test
    fun `Token Issue - Vanilla Test`() {
        val tokenIdentifier = "CARBON"

        val aliceAccount = createAccount(
            mockNet = network,
            node = nodeA,
            name = "Alice ACC - NodeA"
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

        val resultIssue = nodeA.runFlow(issueCarbonFungibleFlow).getOrThrow()

        val nodeACommonQueryService = nodeA.services.cordaService(VaultCommonQueryService::class.java)
        val nodeACarbonQueryService = nodeA.services.cordaService(CarbonQueryService::class.java)

        val carbonFungibles =
            nodeACommonQueryService.getCustomFungiblesOfAccount(aliceAccount, tokenIdentifier)

        assert(carbonFungibles.states.isNotEmpty())

        val carbonRegister =
            nodeACarbonQueryService.getCarbonReportByBatchId(UniqueIdentifier.fromString(resultIssue.batchId))

        assert(carbonRegister.states.isNotEmpty())
        assertEquals(10.00, carbonRegister.states.single().state.data.usedTCO2e)
    }

    @Test
    fun `Token Issue - Error - Issuing more tokens than limited by TCO2e`() {
        val tokenIdentifier = "CARBON"

        val aliceAccount = createAccount(
            mockNet = network,
            node = nodeA,
            name = "Alice ACC - NodeA"
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

        val issueCarbonFungibleFlow2 = IssueCarbonFungiblesFlow(
            carbonBatchId = carbonDTO.batchId,
            holderAccountInfo = aliceAccount,
            amount = 10.00,
            tokenIdentifierString = tokenIdentifier
        )

        nodeA.runFlow(issueCarbonFungibleFlow2).getOrThrow()

        val nodeACommonQueryService = nodeA.services.cordaService(VaultCommonQueryService::class.java)

        val carbonFungibles = nodeACommonQueryService.getCustomFungiblesOfAccount(
            aliceAccount,
            tokenIdentifier
        )

        assert(carbonFungibles.states.isNotEmpty())
        assertEquals(2, carbonFungibles.states.size)

        val issueCarbonFungibleFlow3 = IssueCarbonFungiblesFlow(
            carbonBatchId = carbonDTO.batchId,
            holderAccountInfo = aliceAccount,
            amount = 100.00,
            tokenIdentifierString = tokenIdentifier
        )

        assertThrows<Exception> {
            nodeA.runFlow(issueCarbonFungibleFlow3).getOrThrow()
        }

    }

}