package br.com.zup.b1gvini.pix.grpc.remove

import br.com.zup.b1gvini.RemovePixRequest

fun RemovePixRequest.toRemovePixDTO(): RemovePixDto{
    return RemovePixDto(
        pixId = this.pixId,
        clientId = this.clientId
    )

}