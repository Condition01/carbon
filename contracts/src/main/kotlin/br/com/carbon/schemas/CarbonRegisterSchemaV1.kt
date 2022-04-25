package br.com.carbon.schemas

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

object CarbonRegisterSchemaV1 : MappedSchema(
    schemaFamily = CarbonRegisterSchema.javaClass,
    version = 1,
    mappedTypes = listOf(
        PersistentCarbonRegister::class.java
    )
) {

    @Entity
    @CordaSerializable
    @Table(
        name = "tbl_carbon_report",
        indexes = [Index(name = "cr_batch_id_idx", columnList = "cr_batch_id")]
    )
    data class PersistentCarbonRegister(
        var linearId: String? = null,
        @Column(name = "cr_maintainer")
        var maintainer: Party? = null,
        @Column(name = "cr_batch_id")
        var batchId: String? = null,
        @Column(name = "cr_description")
        var description: String? = null,
        @Column(name = "cr_totalTCO2e")
        var totalTCO2e: Double? = null,
        @Column(name = "cr_usedTCO2e")
        var usedTCO2e: Double? = null
    ) : PersistentState(), Serializable

}