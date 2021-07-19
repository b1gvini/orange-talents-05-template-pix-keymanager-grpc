package br.com.zup.b1gvini.clients.dtos

import br.com.zup.b1gvini.pix.model.ContaAssociada

data class ContaClienteItauResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    data class TitularResponse(val nome: String, val cpf: String)
    data class InstituicaoResponse(val nome: String, val ispb: String)

    fun toModel(): ContaAssociada {
        return ContaAssociada(
            instituicaoNome = this.instituicao.nome,
            instituicaoIspb = this.instituicao.ispb,
            titularNome = this.titular.nome,
            titularCpf = this.titular.cpf,
            agencia = this.agencia,
            numeroConta = this.numero
        )
    }
}