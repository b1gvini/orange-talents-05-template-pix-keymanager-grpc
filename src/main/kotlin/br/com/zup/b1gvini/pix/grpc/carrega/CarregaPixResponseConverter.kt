package br.com.zup.b1gvini.pix.grpc.carrega

import br.com.zup.b1gvini.CarregaPixResponse
import br.com.zup.b1gvini.TipoChave
import br.com.zup.b1gvini.TipoConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaPixResponseConverter {
    fun convert(chaveInfo: ChavePixInfo): CarregaPixResponse {
        return CarregaPixResponse.newBuilder()
            .setClientId(chaveInfo.clienteId ?: "") // Protobuf usa "" como default value para String
            .setPixId(chaveInfo.pixId ?: "") // Protobuf usa "" como default value para String
            .setChave(CarregaPixResponse.ChavePix
                .newBuilder()
                .setTipo(TipoChave.valueOf(chaveInfo.tipo.name))
                .setChave(chaveInfo.chave)
                .setConta(CarregaPixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(TipoConta.valueOf(chaveInfo.tipoDeConta.name))
                    .setInstituicao(chaveInfo.conta.instituicaoNome)
                    .setTitularNome(chaveInfo.conta.titularNome)
                    .setTitularCpf(chaveInfo.conta.titularCpf)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroConta(chaveInfo.conta.numeroConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }
}