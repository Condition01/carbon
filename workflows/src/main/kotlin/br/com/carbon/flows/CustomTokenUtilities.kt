package br.com.carbon.flows

import br.com.carbon.contracts.CarbonReportContract
import br.com.carbon.states.CustomFungibleToken
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder

fun addMoveCustomFungibleTokens(txBuilder: TransactionBuilder,
                                serviceHub: ServiceHub,
                                amount: Amount<TokenType>,
                                holderParty: AnonymousParty,
                                newHolderParty: AnonymousParty,
                                coinSelectionCriteria: QueryCriteria.VaultQueryCriteria) {
    TODO(
        "Precisa implementar uma forma de fazer seleção de tokens customizada" +
                "Precisa implementar na transação o envio do Report para o peer que esta recebendo a ação de Move"
    )
//    addMoveTokens()
}

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

fun getCustomFungible(
    issuedTokenType: Amount<IssuedTokenType>,
    owner: AbstractParty,
    reportLinearId: UniqueIdentifier
): CustomFungibleToken = CustomFungibleToken(issuedTokenType, owner, reportLinearId)