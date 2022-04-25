package br.com.carbon.states

import br.com.carbon.contracts.CarbonReportContract
import br.com.carbon.models.CarbonRegisterDTO
import br.com.carbon.schemas.CarbonRegisterSchemaV1
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

@BelongsToContract(CarbonReportContract::class)
data class CarbonRegister(
    val maintainer: Party,
    val batchId: UniqueIdentifier,
    val description: String,
    val totalTCO2e: Double,
    val usedTCO2e: Double,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : EvolvableTokenType(), QueryableState {
    companion object {
        fun dtoToState(carbonRegisterDTO: CarbonRegisterDTO, maintainer: Party): CarbonRegister {
            return CarbonRegister(
                batchId = carbonRegisterDTO.batchId,
                description = carbonRegisterDTO.description,
                totalTCO2e = carbonRegisterDTO.totalTCO2e,
                usedTCO2e = 0.00,
                maintainer = maintainer
            )
        }
    }

    override val maintainers: List<Party>
        get() = listOf(maintainer)
    override val fractionDigits: Int
        get() = 0

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is CarbonRegisterSchemaV1 -> {
                CarbonRegisterSchemaV1.PersistentCarbonRegister(
                    linearId = this.linearId.toString(),
                    description = this.description,
                    totalTCO2e = this.totalTCO2e,
                    usedTCO2e = this.usedTCO2e,
                    batchId = this.batchId.toString(),
                    maintainer = this.maintainer
                )
            }
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(CarbonRegisterSchemaV1)
    }
}