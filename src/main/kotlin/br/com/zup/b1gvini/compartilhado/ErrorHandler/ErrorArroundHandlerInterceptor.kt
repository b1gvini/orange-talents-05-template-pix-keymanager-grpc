package br.com.zup.b1gvini.compartilhado

import br.com.zup.b1gvini.compartilhado.ErrorHandler.ErrorArroundHandler
import br.com.zup.b1gvini.compartilhado.exceptions.BadRequestException
import br.com.zup.b1gvini.compartilhado.exceptions.ChavePixExistenteException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorArroundHandler::class)
class ErrorAroundHandlerInterceptor : MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            return context.proceed()
        } catch (ex: Exception) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            val status = when (ex) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription(ex.message)

                is ChavePixExistenteException -> Status.ALREADY_EXISTS
                    .withCause(ex)
                    .withDescription(ex.message)

                is BadRequestException -> Status.INTERNAL
                    .withCause(ex)
                    .withDescription(ex.message)

                else -> Status.UNKNOWN
                    .withCause(ex)
                    .withDescription("Ops, um erro inesperado ocorreu")
            }

            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}