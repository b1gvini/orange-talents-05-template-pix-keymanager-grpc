package br.com.zup.b1gvini.pix.grpc.registra

import br.com.zup.b1gvini.RegistraGrpcServiceGrpc
import br.com.zup.b1gvini.RegistraPixRequest
import br.com.zup.b1gvini.TipoChave
import br.com.zup.b1gvini.TipoConta
import br.com.zup.b1gvini.clients.ItauERP
import br.com.zup.b1gvini.clients.dtos.ContaClienteItauResponse
import br.com.zup.b1gvini.pix.model.ChavePix
import br.com.zup.b1gvini.pix.model.ContaAssociada
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
internal class NovaChavePixEndpointTest(
    private val repository: ChavePixRepository,
    private val serviceGrpc: RegistraGrpcServiceGrpc.RegistraGrpcServiceBlockingStub
){

    @Inject
    lateinit var itauClient: ItauERP;

    @MockBean(ItauERP::class)
    fun itauClientMock(): ItauERP{
        return Mockito.mock(ItauERP::class.java)
    }

    val requestChavePix = RegistraPixRequest.newBuilder()
        .setClientId("5260263c-a3c1-4727-ae32-3bdb2538841b")
        .setTipoChave(TipoChave.EMAIL)
        .setValorChave("armando@mail.com")
        .setTipoConta(TipoConta.CONTA_CORRENTE)

    val chavePix = ChavePix(
        clientId = "5260263c-a3c1-4727-ae32-3bdb2538841b",
        tipoChave = br.com.zup.b1gvini.pix.model.enums.TipoChave.EMAIL,
        chave = "armando@mail.com",
        tipoConta = br.com.zup.b1gvini.pix.model.enums.TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicaoNome = "Delcoi",
            instituicaoIspb = "60701190",
            titularNome = "Armando",
            titularCpf = "01002003045",
            agencia = "0007",
            numeroConta = "100010"
        )
    )

    val itauResponse = ContaClienteItauResponse(
        tipo = "CONTA_CORRENTE",
        instituicao = ContaClienteItauResponse
            .InstituicaoResponse("Delcoi","60701190"),
        agencia = "0007",
        numero = "100010",
        titular = ContaClienteItauResponse
            .TitularResponse("Armando","01002003045")
    )

    @BeforeEach
    internal fun setUp(){
        repository.deleteAll()
    }

    @Test
    fun `Deve cadastrar chave pix com sucesso`(){

        // cenario
        val request = requestChavePix.build()
        Mockito.`when`(itauClient.buscarContaCliente(request.clientId, br.com.zup.b1gvini.pix.model.enums.TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(itauResponse))
        // acao
        val response = serviceGrpc.registra(request)
        // validacao
        val chavePix = repository.findAll().first()
        with(response) {
            assertEquals(chavePix.pixId, pixId)
        }
    }

    @Test
    fun `Deve cadastrar chave pix aleatoria com sucesso`(){

        // cenario
        val request = requestChavePix.setTipoChave(TipoChave.ALEATORIA)
            .setValorChave("")
            .build()
        Mockito.`when`(itauClient.buscarContaCliente(request.clientId, br.com.zup.b1gvini.pix.model.enums.TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(itauResponse))
        // acao
        val response = serviceGrpc.registra(request)
        // validacao
        val chavePix = repository.findAll().first()
        with(response) {
            assertNotNull(chavePix.chave)
        }
    }

    @Test
    fun `nao deve registrar chave pix que ja existe`() {
        //cenario
        repository.save(chavePix)
        val request = requestChavePix.build()
        Mockito.`when`(itauClient.buscarContaCliente(request.clientId, br.com.zup.b1gvini.pix.model.enums.TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.ok(itauResponse))

        //acao
        val excecao = assertThrows<StatusRuntimeException>{
            serviceGrpc.registra(request)

        }

        //validacao
        with(excecao){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave pix armando@mail.com já existe", status.description)
        }

    }

    @Test
    fun `Deve retornar erro quando o tipo da chave nao eh valida`(){

        //cenarioo
        val request = requestChavePix.setTipoChave(TipoChave.CHAVE_DESCONHECIDA).build()

        //acao
        val excecao = assertThrows<StatusRuntimeException>{
            serviceGrpc.registra(request)
        }
        // validacao
        with(excecao){
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("Ops, um erro inesperado ocorreu",status.description)
        }

    }

    @Test
    fun `Deve retornar erro quando o tipo da conta nao eh valida`(){

        //cenarioo
        val request = requestChavePix.setTipoConta(TipoConta.CONTA_DESCONHECIDA).build()

        //acao
        val excecao = assertThrows<StatusRuntimeException>{
            serviceGrpc.registra(request)
        }
        // validacao
        with(excecao){
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("Ops, um erro inesperado ocorreu",status.description)
        }

    }

    @Test
    fun `nao deve registrar chave pix quando cliente invalido no itau`() {
        // cenário
        val request = requestChavePix.setClientId("471d4c54-e582-11eb-ba80-0242ac130004").build()
        Mockito.`when`(itauClient.buscarContaCliente(request.clientId, br.com.zup.b1gvini.pix.model.enums.TipoConta.CONTA_CORRENTE))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            serviceGrpc.registra(request)
        }

        // validação
        with(thrown) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals("ClienteId '471d4c54-e582-11eb-ba80-0242ac130004' nao encontrado", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`() {
        // ação
        val request = requestChavePix.setClientId("").build()
        val excecao = assertThrows<StatusRuntimeException> {
            serviceGrpc.registra(request)
        }

        // validação
        with(excecao) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChavePixRequest.clientId: não deve estar em branco", status.description)
        }
    }

    @Factory
    class ClientNovaChave  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegistraGrpcServiceGrpc.RegistraGrpcServiceBlockingStub? {
            return RegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }


}