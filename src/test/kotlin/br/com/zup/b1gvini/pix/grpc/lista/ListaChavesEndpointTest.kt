package br.com.zup.b1gvini.pix.grpc.lista

import br.com.zup.b1gvini.ListaGrpcServiceGrpc
import br.com.zup.b1gvini.ListaPixRequest
import br.com.zup.b1gvini.pix.model.ChavePix
import br.com.zup.b1gvini.pix.model.ContaAssociada
import br.com.zup.b1gvini.pix.model.enums.TipoChave
import br.com.zup.b1gvini.pix.model.enums.TipoConta
import br.com.zup.b1gvini.pix.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.AbstractBlockingStub
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.After
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest(private val repository: ChavePixRepository, private val serviceGrpc: ListaGrpcServiceGrpc.ListaGrpcServiceBlockingStub ){

    val chavePix = ChavePix(
        clientId = "5260263c-a3c1-4727-ae32-3bdb2538841b",
        tipoChave = TipoChave.EMAIL,
        chave = "armando@mail.com",
        tipoConta = TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "Delcoi",
            instituicaoIspb = "60701190",
            titularNome = "Armando",
            titularCpf = "01002003045",
            agencia = "0007",
            numeroConta = "100010"
        )
    )

    val chavePix2 = ChavePix(
        clientId = "5260263c-a3c1-4727-ae32-3bdb2538841b",
        tipoChave = TipoChave.EMAIL,
        chave = "armando2@mail.com",
        tipoConta = TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "Delcoi",
            instituicaoIspb = "60701190",
            titularNome = "Armando",
            titularCpf = "01002003045",
            agencia = "0007",
            numeroConta = "100010"
        )
    )

    @BeforeEach
    internal fun setUp(){
        repository.save(chavePix2)
        repository.save(chavePix)
    }

    @AfterEach
    internal fun cleanUp(){
        repository.deleteAll()
    }

    @Factory
    class clientListaChavePix{
        @Bean
        fun blockstub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListaGrpcServiceGrpc.ListaGrpcServiceBlockingStub{
            return ListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @Test
    fun `Deve listar todas as chaves pix do usuario quando clientId eh valido`(){
        // cenairo
        val request = ListaPixRequest.newBuilder().setClientId(chavePix.clientId).build()
        //acao
        val response = serviceGrpc.lista(request)
        //validacao
        with(response){
            assertEquals(2, response.chavesCount)
        }
    }

    @Test
    fun `Deve retorar lista vazia quando usuario eh valido mas nao tem chavepix registrada`(){
        //cenario
        repository.deleteAll()
        val request = ListaPixRequest.newBuilder().setClientId(chavePix.clientId).build()
        // acao
        val res = serviceGrpc.lista(request)
        //validacao
        with(res){
            assertEquals(0, res.chavesCount)
        }
    }

    @Test
    fun `deve retornar um erro de clienteId não informado`() {
        // cenario
        val request = ListaPixRequest.newBuilder().build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.lista(request)
        }
        // validacao
        with(response) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals("ClientId não pode ser nulo ou vazio", status.description)
        }
    }
}