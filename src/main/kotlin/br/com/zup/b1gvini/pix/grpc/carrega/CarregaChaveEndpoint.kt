package br.com.zup.b1gvini.pix.grpc.carrega

import br.com.zup.b1gvini.CarregaGrpcServiceGrpc
import br.com.zup.b1gvini.CarregaPixRequest
import br.com.zup.b1gvini.CarregaPixResponse
import br.com.zup.b1gvini.clients.BCB
import br.com.zup.b1gvini.compartilhado.ErrorHandler.ErrorArroundHandler
import br.com.zup.b1gvini.pix.repository.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorArroundHandler
@Singleton
class CarregaChaveEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BCB,
    @Inject private val validator: Validator,
) : CarregaGrpcServiceGrpc.CarregaGrpcServiceImplBase() {

    override fun carrega(
        request: CarregaPixRequest,
        responseObserver: StreamObserver<CarregaPixResponse>,
    ) {

        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaPixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}