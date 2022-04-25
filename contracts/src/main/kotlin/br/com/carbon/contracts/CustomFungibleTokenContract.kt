package br.com.carbon.contracts

import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class CustomFungibleTokenContract : Contract {
    companion object {
        const val ID = "br.com.carbon.contracts.CustomFungibleTokenContract"
    }

    override fun verify(tx: LedgerTransaction) {
        //TODO - Implementar lógica de contrato
    }
}