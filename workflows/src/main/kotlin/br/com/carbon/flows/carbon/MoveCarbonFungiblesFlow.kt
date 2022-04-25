package br.com.carbon.flows.carbon

import br.com.carbon.flows.accounts.GetAccountPubKeyAndEncapsulate
import br.com.carbon.flows.addMoveCustomFungibleTokens
import br.com.carbon.flows.carbon.models.CarbonMoveFlowResponse
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.internal.flows.createKeyForAccount
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.of
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveFungibleTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlowHandler
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder

object MoveCarbonFungiblesFlow {

    @StartableByRPC
    @InitiatingFlow
    class MoveCarbonFungiblesInitiatingFlow(
        val amount: Double,
        val tokenIdentifierString: String,
        val fractionDigits: Int,
        val holderAccountInfo: AccountInfo,
        val newHolderAccountInfo: AccountInfo
    ) : FlowLogic<CarbonMoveFlowResponse>() {

        @Suspendable
        override fun call(): CarbonMoveFlowResponse {
            requireThat {"Actual holder account doesn't exist" using (holderAccountInfo.host == ourIdentity) }

            val notary = serviceHub.networkMapCache.notaryIdentities[0]

            val holderParty = serviceHub.createKeyForAccount(holderAccountInfo)

            val newHolderParty = subFlow(GetAccountPubKeyAndEncapsulate(newHolderAccountInfo))

            val coinSelectionCriteria = QueryCriteria.VaultQueryCriteria(
                status = Vault.StateStatus.UNCONSUMED,
                externalIds = listOf(holderAccountInfo.identifier.id)
            )

            val session = initiateFlow(newHolderAccountInfo.host)

            val txBuilder = TransactionBuilder(notary = notary)

            val tokenIdentifier = TokenType(tokenIdentifierString, fractionDigits)

            val amountOfBikeCoins = amount of tokenIdentifier

            /*** Não funciona com a customização do Fungible
            addMoveFungibleTokens(txBuilder, serviceHub, amountOfBikeCoins, newHolderParty, holderParty, coinSelectionCriteria)
             */

            addMoveCustomFungibleTokens(txBuilder, serviceHub, amountOfBikeCoins, newHolderParty, holderParty, coinSelectionCriteria)


            val signers = txBuilder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub) + ourIdentity.owningKey

            val stx = serviceHub.signInitialTransaction(txBuilder, signers)

            subFlow(ObserverAwareFinalityFlow(stx, listOf(session)))

            subFlow(UpdateDistributionListFlow(stx))

            return CarbonMoveFlowResponse(
                txId = stx.id.toHexString(),
                amount = amount,
                tokenIdentifier = tokenIdentifierString,
                oldHolderName = ourIdentity.name.organisation,
                newHolderName = newHolderAccountInfo.name
            )
        }

    }

    @InitiatedBy(MoveCarbonFungiblesInitiatingFlow::class)
    class MoveCarbonFungiblesResponderFlow(private val counterPartySession: FlowSession): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            subFlow(ObserverAwareFinalityFlowHandler(counterPartySession))
        }

    }

}