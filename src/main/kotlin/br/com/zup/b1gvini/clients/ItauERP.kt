package br.com.zup.b1gvini.clients

import br.com.zup.b1gvini.clients.dtos.ContaClienteResponse
import br.com.zup.b1gvini.pix.model.enums.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.client.url}")
interface ItauERP {

    @Get("/v1/clientes/{clienteId}/contas")
    fun buscarContaCliente(@PathVariable clienteId: String, @QueryValue("tipo") tipo: TipoConta): HttpResponse<ContaClienteResponse>

}