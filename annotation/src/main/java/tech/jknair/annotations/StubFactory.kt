package tech.jknair.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class StubFactory(
    val targetClass: Array<KClass<*>>
)