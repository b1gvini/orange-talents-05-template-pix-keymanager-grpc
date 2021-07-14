package br.com.zup.b1gvini.pix.model

import br.com.zup.b1gvini.pix.model.enums.TipoChave
import br.com.zup.b1gvini.pix.model.enums.TipoConta
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
    @field:NotBlank val clientId: String,
    @field:NotNull @Enumerated(EnumType.STRING) val tipoChave: TipoChave,
    @field:NotBlank @field:Size(max=77) val chave: String,
    @field:NotNull @Enumerated(EnumType.STRING) val tipoConta: TipoConta,
    @field:Valid @Embedded val conta: ContaAssociada
) {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null

    var pixId: String = UUID.randomUUID().toString()

    var criadoEm: LocalDateTime = LocalDateTime.now()
}