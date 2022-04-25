package br.com.carbon.service

import br.com.carbon.schemas.CarbonRegisterSchemaV1
import br.com.carbon.states.CarbonRegister
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder

@CordaService
class CarbonQueryService(service: AppServiceHub) : VaultCommonQueryService(service = service) {
    fun getCarbonReportByBatchId(batchId: UniqueIdentifier): Vault.Page<CarbonRegister> {
        val serialNumberCriteria = QueryCriteria.VaultCustomQueryCriteria(builder {
            CarbonRegisterSchemaV1.PersistentCarbonRegister::batchId.equal(batchId.toString())
        })
        val criteria= QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED).and(serialNumberCriteria)

        return this.getStatesWithCriteria(criteria)
    }
}