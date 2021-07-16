package br.com.zup.b1gvini.pix.grpc.remove

import br.com.zup.b1gvini.RemoveGrpcServiceGrpc
import br.com.zup.b1gvini.RemovePixRequest
import br.com.zup.b1gvini.RemovePixResponse
import br.com.zup.b1gvini.compartilhado.ErrorHandler.ErrorArroundHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
@ErrorArroundHandler
class RemoveChavePixEndpoint(private val service: RemoveChavePixService) : RemoveGrpcServiceGrpc.RemoveGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(RemoveChavePixEndpoint::class.java)

    override fun remove(
        request: RemovePixRequest,
        responseObserver: StreamObserver<RemovePixResponse>
    ) {

        logger.info("deletando chave pix...")
        val removePixRequest = request.toRemovePixDTO()
        service.remove(removePixRequest)

        responseObserver.onNext(RemovePixResponse.newBuilder()
            .setResposta("Excluída com sucesso")
            .build())
        responseObserver.onCompleted()

    }
}