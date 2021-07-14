package br.com.zup.b1gvini.pix.grpc

import br.com.zup.b1gvini.RegistraGrpcServiceGrpc
import br.com.zup.b1gvini.RegistraPixRequest
import br.com.zup.b1gvini.RegistraPixResponse
import br.com.zup.b1gvini.compartilhado.ErrorHandler.ErrorArroundHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
@ErrorArroundHandler
class NovaChavePixEndpoint(private val service: NovaChavePixService) : RegistraGrpcServiceGrpc.RegistraGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(NovaChavePixEndpoint::class.java)

    override fun registra(
        request: RegistraPixRequest,
        responseObserver: StreamObserver<RegistraPixResponse>
    ) {
        val novaChavePixRequest = request.toNovaChavePixRequest()

        logger.info("tentando salvar registro")
        val chavePix = service.registra(novaChavePixRequest)

        responseObserver.onNext(RegistraPixResponse.newBuilder()
            .setClientId(chavePix.clientId)
            .setPixId(chavePix.pixId)
            .build())

        responseObserver.onCompleted()
    }
}