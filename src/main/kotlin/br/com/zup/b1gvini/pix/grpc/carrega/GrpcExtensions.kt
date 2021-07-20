package br.com.zup.b1gvini.pix.grpc.carrega

import br.com.zup.b1gvini.CarregaPixRequest
import br.com.zup.b1gvini.CarregaPixRequest.FiltroCase.*
import br.com.zup.b1gvini.compartilhado.exceptions.BadRequestException
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun CarregaPixRequest.toModel(validator: Validator): Filtro { // 1

    val filtro = when(filtroCase!!) { // 1
        PIXID -> pixId.let { // 1
            Filtro.PorPixId(clienteId = it.clientId, pixId = it.pixId) // 1
        }
        CHAVE -> Filtro.PorChave(chave) // 2
        FILTRO_NOT_SET -> throw BadRequestException("Informar apenas chavePix ou (clientId e pixId)")
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro
}