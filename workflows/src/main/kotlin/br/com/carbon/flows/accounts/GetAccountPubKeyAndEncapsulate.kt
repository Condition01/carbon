package br.com.carbon.flows.accounts

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount
import com.r3.corda.lib.accounts.workflows.internal.flows.createKeyForAccount
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.AnonymousParty

class GetAccountPubKeyAndEncapsulate(val accountInfo: AccountInfo): FlowLogic<AnonymousParty>() {
    @Suspendable
    override fun call(): AnonymousParty {
        return if (accountInfo.host == ourIdentity) {
            serviceHub.createKeyForAccount(accountInfo)
        } else {
            subFlow(RequestKeyForAccount(accountInfo))
        }
    }
}