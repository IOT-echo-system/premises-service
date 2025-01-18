package com.robotutor.premisesService

import com.robotutor.iot.utils.gateway.views.PremisesRole
import com.robotutor.iot.utils.gateway.views.UserWithRole
import com.robotutor.iot.utils.models.PremisesData
import com.robotutor.loggingstarter.Logger
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.Context

class Test {
    private val logger = Logger(this::class.java)

    @Test
    fun test() {
        process {
            Mono.deferContextual { ctx ->
                val data = ctx.getOrEmpty<PremisesData>(PremisesData::class.java)
                Mono.just(it)
                    .logOnSuccess(
                        "----1------$it-----inside test fn--${
                            if (data.isPresent) data.get().toString() else "No data"
                        }-----"
                    )
            }
        }
            .logOnSuccess("2. success")
            .subscribe()
    }
}

fun process(p: (it: String) -> Mono<String>): Flux<String> {
    return Flux.fromIterable(listOf(1, 2, 3))
        .flatMap { item ->


            Mono.just("Processed item $item")
                .flatMap { p(it) }
                .contextWrite { ctx -> writeContext(item, ctx) }
        }
}

fun writeContext(item: Int?, ctx: Context): Context {
    val premisesData = PremisesData(
        "premisesId$item",
        "name$item",
        UserWithRole("userId$item", PremisesRole.OWNER)
    )
    return ctx.put(PremisesData::class.java, premisesData)
}

fun <T> Mono<T>.logOnSuccess(name: String): Mono<T> {
    return doOnEach { signal ->
        if (signal.isOnNext) {
            val premises = signal.contextView.get(PremisesData::class.java)
            println("-------$name--$premises----")
        }
    }
}

fun <T> Flux<T>.logOnSuccess(name: String): Flux<T> {
    return doOnEach { signal ->
        if (signal.isOnComplete) {
            val premises = signal.contextView.getOrEmpty<PremisesData>(PremisesData::class.java)
            if (premises.isPresent) {
                println("---flux----$name--${premises.get()}----")
            } else {
                println("---flux----$name--missing premises----")
            }
        }
    }
}
