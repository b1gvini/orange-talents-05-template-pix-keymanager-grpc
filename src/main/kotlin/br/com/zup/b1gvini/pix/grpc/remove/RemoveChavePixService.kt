package br.com.zup.b1gvini.pix.grpc.remove

import br.com.zup.b1gvini.clients.ItauERP
import br.com.zup.b1gvini.compartilhado.exceptions.BadRequestException
import br.com.zup.b1gvini.compartilhado.exceptions.NotFoundException
import br.com.zup.b1gvini.pix.model.ChavePix
import br.com.zup.b1gvini.pix.repository.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoveChavePixService(
    @Inject private val repository: ChavePixRepository,
    @Inject private val itauERP: ItauERP
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun remove(@Valid removePixDto: RemovePixDto){

        logger.info("buscando chave no bd...")
        val possivelChavePix = repository.findByPixId(removePixDto.pixId)
        if (possivelChavePix.isEmpty) throw NotFoundException("Chave nao encontrada")
        val chavePix = possivelChavePix.get()

        logger.info("verificando existencia do cliente no itau...")
        val responseItau = itauERP.buscarContaCliente(removePixDto.clientId,chavePix.tipoConta)
        val conta = responseItau.body()?.toModel() ?: throw BadRequestException("ClienteId '${removePixDto.clientId}' nao encontrado")

        val cpfConta = conta.titularCpf
        val cpfDonoPix = chavePix.conta.titularCpf

        logger.info("verificando se o cliente é o possuidor da chave...")
        if (cpfConta != cpfDonoPix) throw BadRequestException("Dados inconsistente.")
        repository.deleteById(chavePix.id!!)
    }
}