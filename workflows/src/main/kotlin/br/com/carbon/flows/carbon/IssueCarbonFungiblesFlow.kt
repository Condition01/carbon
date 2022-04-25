package br.com.carbon.flows.carbon

import br.com.carbon.contracts.CarbonReportContract
import br.com.carbon.flows.accounts.GetAccountPubKeyAndEncapsulate
import br.com.carbon.flows.carbon.models.CarbonIssueFlowResponse
import br.com.carbon.service.CarbonQueryService
import br.com.carbon.states.CustomFungibleToken
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.contracts.utilities.of
import com.r3.corda.lib.tokens.workflows.flows.issue.addIssueTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
class IssueCarbonFungiblesFlow(
    private val carbonBatchId: UniqueIdentifier,
    private val holderAccountInfo: AccountInfo,
    private var observers: MutableList<Party> = mutableListOf(),
    private val amount: Double,
    private val tokenIdentifierString: String
) : FlowLogic<CarbonIssueFlowResponse>() {

    companion object {
        object INITIATING_TRANSACTION : ProgressTracker.Step("Initiating Bike Token Issue Transaction.")
        object UPDATING_ISSUER : ProgressTracker.Step("Updating Issuer Information Before Generating Tokens.")
        object GENERATING_NONFUNGIBLE_TOKEN : ProgressTracker.Step("Generating Non-Fungible Token.")
        object SIGNING_TX : ProgressTracker.Step("Signing transaction.")
        object FINALIZING_TX: ProgressTracker.Step("Finalizing transaction.")

        fun tracker() = ProgressTracker(
            INITIATING_TRANSACTION,
            UPDATING_ISSUER,
            GENERATING_NONFUNGIBLE_TOKEN,
            SIGNING_TX,
            FINALIZING_TX
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): CarbonIssueFlowResponse {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        progressTracker.currentStep = INITIATING_TRANSACTION

        val carbonQueryService = serviceHub.cordaService(CarbonQueryService::class.java)

        val vaultPage = carbonQueryService.getCarbonReportByBatchId(
            batchId = carbonBatchId
        )

        if (vaultPage.states.isEmpty())
            throw FlowException("No states with 'carbonBatchId' - $carbonBatchId found")

        val carbonRegisterStateAndRef = vaultPage.states.single()

        val carbonRegisterData = carbonRegisterStateAndRef.state.data

        require(ourIdentity in carbonRegisterData.maintainers) {
            "This flow can only be started by existing maintainers of the EvolvableTokenType."
        }

        require(carbonRegisterData.usedTCO2e + amount <= carbonRegisterData.totalTCO2e) {
            "Cannot issue more tokens than the total TCO2e"
        }

        val updatedCarbonRegister = carbonRegisterData.copy(usedTCO2e = carbonRegisterData.usedTCO2e + amount)

        val txBuilder = TransactionBuilder(notary = notary)

        addUpdateEvolvableTokenForIssue(
            txBuilder,
            carbonRegisterStateAndRef,
            updatedCarbonRegister
        )

        val tokenIdentifier = TokenType(tokenIdentifierString, carbonRegisterData.fractionDigits)

        val holderParty = subFlow(GetAccountPubKeyAndEncapsulate(holderAccountInfo))

        val issueTokenType = amount of tokenIdentifier issuedBy ourIdentity // customHeldBy(holderParty, carbonRegisterData.li)

        val customFungible = getCustomFungible(issueTokenType, holderParty, carbonRegisterData.linearId)

        val observersSession = mutableListOf<FlowSession>()

        for (observer in observers) {
            observersSession.add(initiateFlow(observer))
        }

        addIssueTokens(txBuilder, listOf(customFungible))

        progressTracker.currentStep = SIGNING_TX

        val stx = serviceHub.signInitialTransaction(txBuilder)

        subFlow(ObserverAwareFinalityFlow(signedTransaction = stx, allSessions = observersSession))

        subFlow(UpdateDistributionListFlow(stx))

        progressTracker.currentStep = FINALIZING_TX

        return CarbonIssueFlowResponse(
            txId = stx.id.toHexString(),
            carbonReportLinearId = carbonRegisterData.linearId,
            amount = amount,
            batchId = carbonBatchId.toString(),
            tokenIdentifier = tokenIdentifierString,
            holderName = holderAccountInfo.name

        )
    }

    @Suspendable
    fun addUpdateEvolvableTokenForIssue(
        transactionBuilder: TransactionBuilder,
        oldStateAndRef: StateAndRef<EvolvableTokenType>,
        newState: EvolvableTokenType
    ): TransactionBuilder {
        val oldState = oldStateAndRef.state.data
        val maintainers = (oldState.maintainers + newState.maintainers).toSet()
        val signingKeys = maintainers.map { it.owningKey }
        return transactionBuilder
            .addCommand(data = CarbonReportContract.UpdateForIssue(), keys = signingKeys)
            .addInputState(oldStateAndRef)
            .addOutputState(state = newState, contract = oldStateAndRef.state.contract)
    }

    fun getCustomFungible(issuedTokenType: Amount<IssuedTokenType>,
                          owner: AbstractParty,
                          reportLinearId: UniqueIdentifier): CustomFungibleToken
        = CustomFungibleToken(issuedTokenType, owner, reportLinearId)

}
