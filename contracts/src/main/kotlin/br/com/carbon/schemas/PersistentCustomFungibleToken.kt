package br.com.carbon.schemas

import com.r3.corda.lib.tokens.contracts.internal.schemas.TokenClassConverter
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.*

object CustomFungibleTokenSchema

object CustomFungibleTokenSchemaV1 : MappedSchema(
    schemaFamily = CustomFungibleTokenSchema.javaClass,
    version = 1,
    mappedTypes = listOf(CustomPersistentFungibleToken::class.java)
)

@Entity
@Table(name = "fungible_token", indexes = [
    Index(name = "amount_idx", columnList = "amount"),
    Index(name = "held_token_amount_idx", columnList = "token_class, token_identifier"),
    Index(name = "holding_key_idx", columnList = "holding_key")
])
class CustomPersistentFungibleToken(
    @Column(name = "issuer", nullable = false)
    var issuer: Party? = null,

    @Column(name = "holder")
    var holder: AbstractParty? = null,

    @Column(name = "amount", nullable = false)
    var amount: Long? = null,

    // The fully qualified class name of the class which implements the token tokenType.
    // This is either a fixed token or a evolvable token.
    @Column(name = "token_class", nullable = false)
    @Convert(converter = TokenClassConverter::class)
    var tokenClass: Class<*>? = null,

    @Column(name = "report_linear_id", nullable = false)
    var reportLinearId: String? = null,

    // This can either be a symbol or a linearID depending on whether the token is evolvable or fixed.
    // Not all tokens will have identifiers if there is only one instance for a token class, for example.
    // It is expected that the combination of token_class and token_symbol will be enough to identity a unique
    // token.
    @Column(name = "token_identifier", nullable = true)
    var tokenIdentifier: String? = null,

    @Column(name = "holding_key", nullable = true)
    val owningKeyHash: String? = null
) : PersistentState()
