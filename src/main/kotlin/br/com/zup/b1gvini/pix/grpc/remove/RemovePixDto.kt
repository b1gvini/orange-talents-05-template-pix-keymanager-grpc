package br.com.zup.b1gvini.pix.grpc.remove

import br.com.zup.b1gvini.pix.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemovePixDto(
    @field:ValidUUID @field: NotBlank val pixId: String,
    @field:ValidUUID @field: NotBlank val clientId: String
) {

}