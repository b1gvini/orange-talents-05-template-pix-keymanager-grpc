package br.com.zup.b1gvini.pix.grpc.carrega

import br.com.zup.b1gvini.pix.model.ChavePix
import br.com.zup.b1gvini.pix.model.ContaAssociada
import br.com.zup.b1gvini.pix.model.enums.TipoChave
import br.com.zup.b1gvini.pix.model.enums.TipoConta
import java.time.LocalDateTime

class ChavePixInfo(
    val pixId: String? = "",
    val clienteId: String? = "",
    val tipo: TipoChave,
    val chave: String,
    val tipoDeConta: TipoConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.pixId,
                clienteId = chave.clientId,
                tipo = chave.tipoChave,
                chave = chave.chave,
                tipoDeConta = chave.tipoConta,
                conta = chave.conta,
                registradaEm = chave.criadoEm
            )
        }
    }
}