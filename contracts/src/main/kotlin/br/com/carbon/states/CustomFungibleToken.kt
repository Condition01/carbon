package br.com.carbon.states

import br.com.carbon.contracts.CustomFungibleTokenContract
import br.com.carbon.schemas.CustomFungibleTokenSchemaV1
import br.com.carbon.schemas.CustomPersistentFungibleToken
import com.r3.corda.lib.tokens.contracts.FungibleTokenContract
import com.r3.corda.lib.tokens.contracts.states.AbstractToken
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.contracts.utilities.holderString
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.FungibleState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.crypto.toStringShort
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

//@BelongsToContract(FungibleTokenContract::class)
//open class CustomFungibleToken @JvmOverloads constructor(
//    override val amount: Amount<IssuedTokenType>,
//    override val holder: AbstractParty,
//    val reportLinearId: UniqueIdentifier,
//    override val tokenTypeJarHash: SecureHash? = amount.token.tokenType.getAttachmentIdForGenericParam()
//) : FungibleToken(amount = amount, holder = holder, tokenTypeJarHash = tokenTypeJarHash)

@BelongsToContract(CustomFungibleTokenContract::class)
open class CustomFungibleToken @JvmOverloads constructor(
    override val amount: Amount<IssuedTokenType>,
    override val holder: AbstractParty,
    val reportLinearId: UniqueIdentifier,
    override val tokenTypeJarHash: SecureHash? = amount.token.tokenType.getAttachmentIdForGenericParam()
) : FungibleState<IssuedTokenType>, AbstractToken, QueryableState {

    override val tokenType: TokenType get() = amount.token.tokenType

    override val issuedTokenType: IssuedTokenType get() = amount.token

    override val issuer: Party get() = amount.token.issuer

    override fun toString(): String = "$amount held by $holderString"

    override fun withNewHolder(newHolder: AbstractParty): FungibleToken {
        return FungibleToken(amount = amount, holder = newHolder, tokenTypeJarHash = tokenTypeJarHash)
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is CustomFungibleTokenSchemaV1 -> CustomPersistentFungibleToken(
            issuer = amount.token.issuer,
            holder = holder,
            reportLinearId = this.reportLinearId.toString(),
            amount = amount.quantity,
            tokenClass = amount.token.tokenType.tokenClass,
            tokenIdentifier = amount.token.tokenType.tokenIdentifier,
            owningKeyHash = holder.owningKey.toStringShort()
        )
        else -> throw IllegalArgumentException("Unrecognised schema $schema")
    }

    override fun supportedSchemas(): List<CustomFungibleTokenSchemaV1> = listOf(CustomFungibleTokenSchemaV1)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FungibleToken
        if (amount != other.amount) return false
        if (holder != other.holder) return false
        if (tokenTypeJarHash != other.tokenTypeJarHash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + holder.hashCode()
        result = 31 * result + (tokenTypeJarHash?.hashCode() ?: 0)
        return result
    }

}