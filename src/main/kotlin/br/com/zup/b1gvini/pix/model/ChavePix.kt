package br.com.zup.b1gvini.pix.model

import br.com.zup.b1gvini.pix.ValidPixKey
import br.com.zup.b1gvini.pix.model.enums.TipoChave
import br.com.zup.b1gvini.pix.model.enums.TipoConta
import br.com.zup.b1gvini.pix.validations.ValidUUID
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.persistence.GenerationType.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotBlank @field:ValidUUID val clientId: String,
    @field:NotNull @Enumerated(EnumType.STRING) val tipoChave: TipoChave,
    @field:NotBlank @field:Size(max=77) var chave: String,
    @field:NotNull @Enumerated(EnumType.STRING) val tipoConta: TipoConta,
    @field:Valid @Embedded val conta: ContaAssociada
) {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null

    var pixId: String = UUID.randomUUID().toString()

    var criadoEm: LocalDateTime = LocalDateTime.now()

    //EQUALS AND HASHCODE
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChavePix

        if (id != other.id) return false
        if (pixId != other.pixId) return false
        if (criadoEm != other.criadoEm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + pixId.hashCode()
        result = 31 * result + criadoEm.hashCode()
        return result
    }

    fun pertenceAo(clienteId: String) : Boolean{
        return this.clientId == clientId
    }


}