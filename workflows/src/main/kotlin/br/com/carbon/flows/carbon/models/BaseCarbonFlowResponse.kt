package br.com.carbon.flows.carbon.models

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
open class BaseCarbonFlowResponse(
    open val txId: String,
    open val batchId: String,
    open val carbonReportLinearId: UniqueIdentifier
)