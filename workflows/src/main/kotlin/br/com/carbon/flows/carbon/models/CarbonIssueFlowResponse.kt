package br.com.carbon.flows.carbon.models

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class CarbonIssueFlowResponse(
    txId: String,
    batchId: String,
    carbonReportLinearId: UniqueIdentifier,
    val amount: Double,
    val tokenIdentifier: String,
    val holderName: String
): BaseCarbonFlowResponse(
    txId = txId,
    batchId = batchId,
    carbonReportLinearId = carbonReportLinearId
)