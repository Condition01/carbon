package br.com.carbon.flows.carbon.models

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CarbonMoveFlowResponse(
    val txId: String,
    val amount: Double,
    val tokenIdentifier: String,
    val oldHolderName: String,
    val newHolderName: String
)