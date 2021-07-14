package br.com.zup.b1gvini.pix.grpc

import br.com.zup.b1gvini.clients.ItauERP
import br.com.zup.b1gvini.compartilhado.exceptions.BadRequestException
import br.com.zup.b1gvini.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.b1gvini.pix.model.ChavePix
import br.com.zup.b1gvini.pix.repository.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class NovaChavePixService(
    @Inject private val repository: ChavePixRepository,
    @Inject private val itau: ItauERP) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePixRequest: NovaChavePixRequest): ChavePix {

        val possivelChavePix = repository.existsByChave(novaChavePixRequest.chave!!)

        if(possivelChavePix){
            throw ChavePixExistenteException("Chave pix ${novaChavePixRequest.chave} já existe")
        }

        logger.info("Verificando com serviço externo")
        val responseItau = itau.buscarContaCliente(novaChavePixRequest.clientId, novaChavePixRequest.tipoConta)

        logger.info("validando dados do serviço externo")
        val conta = responseItau.body()?.toModel() ?: throw BadRequestException("ClienteId '${novaChavePixRequest.clientId}' nao encontrado")

        val chavePix = novaChavePixRequest.toChavePix(conta)
        repository.save(chavePix)
        logger.info("chave pix salva com sucesso")
        return chavePix
    }
}