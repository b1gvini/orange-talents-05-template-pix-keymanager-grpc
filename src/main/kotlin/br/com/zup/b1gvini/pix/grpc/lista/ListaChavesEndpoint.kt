package br.com.zup.b1gvini.pix.grpc.lista

import br.com.zup.b1gvini.*
import br.com.zup.b1gvini.compartilhado.ErrorHandler.ErrorArroundHandler
import br.com.zup.b1gvini.compartilhado.exceptions.BadRequestException
import br.com.zup.b1gvini.pix.repository.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorArroundHandler
class ListaChavesEndpoint(@Inject private val repository: ChavePixRepository) : ListaGrpcServiceGrpc.ListaGrpcServiceImplBase() {
    override fun lista(
        request: ListaPixRequest,
        responseObserver: StreamObserver<ListaPixResponse>
    ) {

        if(request.clientId.isNullOrBlank()){
            throw BadRequestException("ClientId não pode ser nulo ou vazio")
        }

        val clientId = request.clientId
        val chaves = repository.findAllByClientId(clientId).map{
            ListaPixResponse.ChavePix.newBuilder()
                .setPixId(it.pixId)
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                .setChave(it.chave)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setCriadaEm(it.criadoEm.let {
                    val criadaEm = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(criadaEm.epochSecond)
                        .setNanos(criadaEm.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaPixResponse.newBuilder()
            .setClientId(clientId)
            .addAllChaves(chaves)
            .build())
        responseObserver.onCompleted()

    }
}