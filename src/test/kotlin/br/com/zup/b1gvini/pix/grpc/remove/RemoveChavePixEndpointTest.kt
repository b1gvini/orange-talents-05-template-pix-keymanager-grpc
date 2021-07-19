package br.com.zup.b1gvini.pix.grpc.remove

import br.com.zup.b1gvini.RemoveGrpcServiceGrpc
import br.com.zup.b1gvini.RemovePixRequest
import br.com.zup.b1gvini.clients.ItauERP
import br.com.zup.b1gvini.clients.dtos.ContaClienteItauResponse
import br.com.zup.b1gvini.pix.model.ChavePix
import br.com.zup.b1gvini.pix.model.ContaAssociada
import br.com.zup.b1gvini.pix.model.enums.TipoChave
import br.com.zup.b1gvini.pix.model.enums.TipoConta
import br.com.zup.b1gvini.pix.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    private val repository: ChavePixRepository,
    private val serviceGrpc: RemoveGrpcServiceGrpc.RemoveGrpcServiceBlockingStub
){
    @Inject
    lateinit var itauClient: ItauERP;

    @BeforeEach
    internal fun setUp(){
        repository.deleteAll()
    }

    @MockBean(ItauERP::class)
    fun itauErpMock(): ItauERP {
        return Mockito.mock(ItauERP::class.java)
    }

    val request = RemovePixRequest.newBuilder()
        .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
        .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")

    val chavePixArmando = ChavePix(
        clientId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
        tipoChave = TipoChave.EMAIL,
        chave = "armando@mail.com",
        tipoConta = TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "delcoi",
            instituicaoIspb = "60701190",
            titularNome = "Armando Del Coi",
            titularCpf = "06628726061",
            agencia = "0001",
            numeroConta = "100001"
        )
    )

    val chavePixGi = ChavePix(
        clientId = "89c1bfbe-e64e-11eb-ba80-0242ac130004",
        tipoChave = TipoChave.EMAIL,
        chave = "gi@mail.com",
        tipoConta = TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "delcoi",
            instituicaoIspb = "60701190",
            titularNome = "Gi",
            titularCpf = "86135457004",
            agencia = "0002",
            numeroConta = "200002"
        )
    )

    val itauResponse = ContaClienteItauResponse(
        tipo = "CONTA_CORRENTE",
        instituicao = ContaClienteItauResponse.InstituicaoResponse("delcoi", "60701190"),
        agencia = "00001",
        numero = "100001",
        titular = ContaClienteItauResponse.TitularResponse("Armando Del Coi", "06628726061")
    )

    @Factory
    class ClientRemoverChavePix  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemoveGrpcServiceGrpc.RemoveGrpcServiceBlockingStub? {
            return RemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @Test
    fun `deve remover chave pix com suuuucesso`(){
        //cenario
        repository.save(chavePixArmando)
        val chavePix = repository.findAll().first()
        val request = request.setPixId(chavePix.pixId)
                                .setClientId(chavePix.clientId)
                                .build()
        Mockito.`when`(itauClient.buscarContaCliente(request.clientId, chavePix.tipoConta)).thenReturn(HttpResponse.ok(itauResponse))
        //acao
        val response = serviceGrpc.remove(request)
        //validacao
        assertEquals("Excluída com sucesso", response.resposta)
        assertTrue(repository.findAll().isEmpty())
    }

    @Test
    fun `deve retornar erro se os valores da requisicao forem nulos`(){

        //cenario
        val request = request.setClientId("").setPixId("").build()

        val excecao = assertThrows<StatusRuntimeException>{
            serviceGrpc.remove(request)
        }
        // validacao
        assertEquals(Status.INVALID_ARGUMENT.code, excecao.status.code)

    }

    @Test
    fun `deve retornar erro not found quando chave pix nao encontrada`(){
        val request = request.build()

        val excecao = assertThrows<StatusRuntimeException> {
            serviceGrpc.remove(request)
        }
        //validacao
        assertEquals(Status.NOT_FOUND.code, excecao.status.code)

    }

    @Test
    fun `deve retonar erro ao tentar excluir chave pix de um outro usuario`(){
        //cenario
        repository.save(chavePixGi)
        val gi = repository.findAll().first().pixId
        val request = request.setPixId(gi).build()

        Mockito.`when`(itauClient.buscarContaCliente(request.clientId, chavePixArmando.tipoConta)).thenReturn(HttpResponse.ok(itauResponse))
        //acao
        val excecao = assertThrows<StatusRuntimeException>{
            serviceGrpc.remove(request)
        }
        //validacao
        assertEquals(Status.INTERNAL.code, excecao.status.code)
        assertEquals("Dados inconsistente.", excecao.status.description)

    }
}

