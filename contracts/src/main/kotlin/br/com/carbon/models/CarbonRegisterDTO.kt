package br.com.carbon.models

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CarbonRegisterDTO (
    val batchId: UniqueIdentifier,
    val totalTCO2e: Double,
    val description: String
)