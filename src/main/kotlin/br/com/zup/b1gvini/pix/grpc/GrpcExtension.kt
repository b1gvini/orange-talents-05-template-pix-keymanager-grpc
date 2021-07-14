package br.com.zup.b1gvini.pix.grpc

import br.com.zup.b1gvini.RegistraPixRequest
import br.com.zup.b1gvini.pix.model.enums.TipoChave
import br.com.zup.b1gvini.pix.model.enums.TipoConta

fun RegistraPixRequest.toNovaChavePixRequest(): NovaChavePixRequest {
    val novaChavePixRequest =  NovaChavePixRequest(
        clientId = this.clientId,
        tipoChave = TipoChave.valueOf(this.tipoChave.name),
        chave = this.valorChave,
        tipoConta = TipoConta.valueOf(this.tipoConta.name)
    )

    return novaChavePixRequest
}