package br.com.zup.b1gvini.pix.model

import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable
class ContaAssociada(
    @field:NotBlank
    val instituicaoNome: String,

    @field:NotBlank
    val instituicaoIspb: String,

    @field:NotBlank
    val titularNome: String,

    @field:NotBlank
    @field:Size(max = 11)
    val titularCpf: String,

    @field:NotBlank
    @field:Size(max = 4)
    val agencia: String,

    @field:NotBlank
    @field:Size(max = 6)
    val numeroConta: String



) {
    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}
