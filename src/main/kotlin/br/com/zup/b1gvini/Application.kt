package br.com.zup.b1gvini

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.b1gvini")
		.start()
}

