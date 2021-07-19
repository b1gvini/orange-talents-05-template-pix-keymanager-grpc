package br.com.zup.b1gvini.pix.grpc.registra

import br.com.zup.b1gvini.clients.BCB
import br.com.zup.b1gvini.clients.CreatePixKeyRequest
import br.com.zup.b1gvini.clients.ItauERP
import br.com.zup.b1gvini.compartilhado.exceptions.BadRequestException
import br.com.zup.b1gvini.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.b1gvini.pix.model.ChavePix
import br.com.zup.b1gvini.pix.repository.ChavePixRepository
import io.micronaut.http.HttpStatus
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
    @Inject private val itau: ItauERP,
    @Inject private val bcb: BCB) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePixRequest: NovaChavePixRequest): ChavePix {

        val possivelChavePix = repository.existsByChave(novaChavePixRequest.chave!!)

        if(possivelChavePix){
            throw ChavePixExistenteException("Chave pix ${novaChavePixRequest.chave} já existe")
        }

        logger.info("Verificando com serviço externo")
        val responseItau = itau.buscarContaCliente(novaChavePixRequest.clientId, novaChavePixRequest.tipoConta)

        logger.info("validando dados do serviço externo do Itau")
        val conta = responseItau.body()?.toModel() ?: throw BadRequestException("ClienteId '${novaChavePixRequest.clientId}' nao encontrado")

        val chavePix = novaChavePixRequest.toChavePix(conta)



        val bcbRequest = CreatePixKeyRequest.of(chavePix).also {
            logger. info("registrando chave ${chavePix.pixId} no BCB")
        }

        val bcbResponse = bcb.criarChavePix(bcbRequest)
        if(bcbResponse.status != HttpStatus.CREATED) throw BadRequestException("Erro ao registrar chave pix no banco central do Brasil")
        chavePix.chave = bcbResponse.body()!!.key

        logger.info("registrando no database")
        repository.save(chavePix)
        logger.info("chave pix salva com sucesso no dabatase")

        return chavePix
    }
}