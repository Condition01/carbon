package br.com.carbon.contracts

import br.com.carbon.states.CarbonRegister
import br.com.carbon.states.CustomFungibleToken
import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import com.r3.corda.lib.tokens.contracts.commands.EvolvableTokenTypeCommand
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.utilities.singleInput
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction

class CarbonReportContract: EvolvableTokenContract(), Contract {
    companion object {
        const val ID = "br.com.carbon.contracts.CarbonReportContract"
    }

    override fun additionalCreateChecks(tx: LedgerTransaction) {
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<EvolvableTokenTypeCommand>()

        if(command.value is UpdateForIssue) {
            verifyUpdateForIssue(tx)
        } else {
            super.verify(tx)
        }
    }

    private fun verifyUpdateForIssue(tx: LedgerTransaction) {
        // Check commands.
        val command = tx.commands.requireSingleCommand<UpdateForIssue>()

        // Check inputs.
        require(tx.inputs.size == 1) { "Update evolvable token transactions must contain exactly one input." }
        val input = tx.singleInput<EvolvableTokenType>()

        // Check participants.
        require(input.participants.containsAll(input.maintainers)) {
            "All evolvable token maintainers must also be participants."
        }

        var output: EvolvableTokenType? = null
        val otherOutputs = mutableListOf<CustomFungibleToken>()

        for(outputState in tx.outputStates) {
            if (outputState is EvolvableTokenType && output == null) {
                output = outputState
            } else if(outputState is CustomFungibleToken) {
                otherOutputs.add(outputState)
            } else {
                throw  IllegalArgumentException("Output must be 'EvolvableTokenType' or 'CustomFungibleToken'")
            }
        }

        require(output != null) { "The output can't be null" }
        require(otherOutputs.size > 0) { "You should have at least ONE fungible token in this transaction" }

        // Check participants.
        require(output!!.participants.containsAll(output.maintainers)) {
            "All evolvable token maintainers must also be participants."
        }

        // Normalise participants and maintainers for ease of reference.
        val maintainers = (input.maintainers + output.maintainers).toSet()
        val maintainerKeys = maintainers.map(AbstractParty::owningKey).toSet()

        // Check signatures.
        require(command.signers.toSet() == maintainerKeys) {
            "Only evolvable token maintainers (from inputs and outputs) may sign the update evolvable token transaction."
        }

        // Verify linear IDs does not change.
        require(input.linearId == output.linearId) {
            "The Linear ID of the evolvable token cannot change during an update."
        }

        require(output is CarbonRegister) {
            "The output Evolvable should be 'CarbonRegister' type"
        }

        val parsedOutput = output as CarbonRegister

        require(parsedOutput.usedTCO2e <= parsedOutput.totalTCO2e) {
            "Contract - Cannot issue more tokens than the total TCO2e"
        }

    }

    class UpdateForIssue : EvolvableTokenTypeCommand, TypeOnlyCommandData()
}