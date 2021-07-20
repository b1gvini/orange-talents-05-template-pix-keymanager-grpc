package br.com.zup.b1gvini.pix.grpc.carrega

import br.com.zup.b1gvini.*
import br.com.zup.b1gvini.clients.*
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
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class CarregaChaveEndpointTest(
    private val repository: ChavePixRepository,
    private val serviceGrpc: CarregaGrpcServiceGrpc.CarregaGrpcServiceBlockingStub
){
    @Inject
    lateinit var bcb: BCB

    @MockBean(BCB::class)
    fun bcbMock(): BCB {
        return Mockito.mock(BCB::class.java)
    }

    @Factory
    class ClientCarregaChavePix  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarregaGrpcServiceGrpc.CarregaGrpcServiceBlockingStub? {
            return CarregaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

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

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.EMAIL,
            key = "user.from.another.bank@santander.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "01002003045",
            branch = "0007",
            accountNumber = "100010",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }

    @Test
    fun `deve carregar chave por pixId e clienteId`() {
        // cenário
        repository.save(chavePix)
        val chaveExistente = repository.findByChave("armando@mail.com").get()

        // ação
        val response = serviceGrpc.carrega(CarregaPixRequest.newBuilder()
            .setPixId(
                CarregaPixRequest.FiltroPorPixId.newBuilder()
                .setPixId(chaveExistente.pixId)
                .setClientId(chaveExistente.clientId)
                .build()
            ).build())

        // validação
        with(response) {
            assertEquals(chaveExistente.pixId, this.pixId)
            assertEquals(chaveExistente.clientId, this.clientId)
            assertEquals(chaveExistente.tipoChave.name, this.chave.tipo.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `deve carregar chave por chave`(){
        //cenario
        repository.save(chavePix)
        val chaveExistente = repository.findByChave("armando@mail.com").get()
        //acao
        val response = serviceGrpc.carrega(CarregaPixRequest.newBuilder()
            .setChave("armando@mail.com")
            .build())
        with(response){
            assertEquals(chaveExistente.tipoChave.name, this.chave.tipo.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `deve retornar erro quando for passado apenas a chave e a chave for vazia`(){

        //acao
        val response = assertThrows<StatusRuntimeException>{
            serviceGrpc.carrega(CarregaPixRequest.newBuilder()
                .setChave("")
                .build())
        }
        //acao
        with(response){
            assertEquals("chave: não deve estar em branco",status.description)
        }

    }

    @Test
    fun `deve retornar erro quando for passado pixId e clientId e eles forem vazios`(){

        //acao
        val response = assertThrows<StatusRuntimeException>{
            serviceGrpc.carrega(CarregaPixRequest.newBuilder().setPixId(
                CarregaPixRequest.FiltroPorPixId.newBuilder()
                    .setClientId("")
                    .setPixId("")
                    .build())
                .build()
            )
        }
        //acao
        with(response){
            assertEquals(Status.INVALID_ARGUMENT.code,status.code)
        }

    }

    @Test
    fun `deve retornar erro quando for passado pixId e clientId e o pixId nao for informado`(){

        //acao
        val response = assertThrows<StatusRuntimeException>{
            serviceGrpc.carrega(CarregaPixRequest.newBuilder().setPixId(
                CarregaPixRequest.FiltroPorPixId.newBuilder()
                    .setClientId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .setPixId("")
                    .build())
                .build()
            )
        }
        //acao
        with(response){
            assertEquals(Status.INVALID_ARGUMENT.code,status.code)
        }

    }

    @Test
    fun `deve retornar erro quando for passado pixId e clientId e o clientId nao for informado`(){

        //acao
        val response = assertThrows<StatusRuntimeException>{
            serviceGrpc.carrega(CarregaPixRequest.newBuilder().setPixId(
                CarregaPixRequest.FiltroPorPixId.newBuilder()
                    .setClientId("")
                    .setPixId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .build())
                .build()
            )
        }
        //acao
        with(response){
            assertEquals(Status.INVALID_ARGUMENT.code,status.code)
        }

    }

    @Test
    fun `deve retornar um erro caso nao tenha dados de entrada`() {
        // cenario
        val request = CarregaPixRequest.newBuilder().build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.carrega(request)
        }
        // validacao
        assertEquals(Status.INTERNAL.code, response.status.code)
        assertEquals("Informar apenas chavePix ou (clientId e pixId)", response.status.description)
    }

    @Test
    fun `deve retornar NotFound caso nao consiga achar chave no BCB`() {
        // cenario
        val request = CarregaPixRequest.newBuilder()
            .setChave("armando@mail.com").build()
        Mockito.`when`(bcb.buscarChavePix(chavePix.chave))
            .thenReturn(HttpResponse.notFound())
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.carrega(request)
        }
        // validacao
        with(response){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }

    }

    @Test
    fun `deve retornar NotFound caso nao consiga achar clientId, pixId local`() {
        // cenario
        val request = CarregaPixRequest.newBuilder()
            .setPixId(
                CarregaPixRequest.FiltroPorPixId.newBuilder()
                    .setClientId(UUID.randomUUID().toString())
                    .setPixId(UUID.randomUUID().toString())
                    .build())
            .build()
        // acao
        val response = assertThrows<StatusRuntimeException> {
            serviceGrpc.carrega(request)
        }
        // validacao
        with(response){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }

    }
}