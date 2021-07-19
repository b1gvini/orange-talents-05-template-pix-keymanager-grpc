package br.com.zup.b1gvini.pix.grpc.registra


import br.com.zup.b1gvini.pix.validations.ValidUUID
import br.com.zup.b1gvini.pix.ValidPixKey

import br.com.zup.b1gvini.pix.model.ChavePix
import br.com.zup.b1gvini.pix.model.ContaAssociada
import br.com.zup.b1gvini.pix.model.enums.TipoChave
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidPixKey
data class NovaChavePixRequest(
    @ValidUUID
    @field:NotBlank
    val clientId: String,
    @field:NotNull
    val tipoChave: br.com.zup.b1gvini.pix.model.enums.TipoChave,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoConta: br.com.zup.b1gvini.pix.model.enums.TipoConta
    ){

    fun toChavePix(conta: ContaAssociada): ChavePix{
        return ChavePix(
            clientId = this.clientId,
            tipoChave = this.tipoChave,
            chave = chave!!,
            tipoConta = this.tipoConta,
            conta = conta
        )
    }

}
