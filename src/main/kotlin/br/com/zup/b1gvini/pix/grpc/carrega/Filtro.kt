package br.com.zup.b1gvini.pix.grpc.carrega

import br.com.zup.b1gvini.clients.BCB
import br.com.zup.b1gvini.compartilhado.exceptions.BadRequestException
import br.com.zup.b1gvini.compartilhado.exceptions.NotFoundException
import br.com.zup.b1gvini.pix.repository.ChavePixRepository
import br.com.zup.b1gvini.pix.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BCB): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String,
    ) : Filtro() {

        override fun filtra(repository: ChavePixRepository, bcbClient: BCB): ChavePixInfo {
            return repository.findByPixId(pixId)
                .filter { it.pertenceAo(clienteId) }
                .map(ChavePixInfo::of)
                .orElseThrow { NotFoundException("Chave Pix não encontrada") }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String) : Filtro() {

        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: BCB): ChavePixInfo {
            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    LOGGER.info("Consultando chave Pix '$chave' no Banco Central do Brasil (BCB)")

                    val response = bcbClient.buscarChavePix(chave)
                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw NotFoundException("Chave Pix não encontrada")
                    }
                }
        }
    }
}

