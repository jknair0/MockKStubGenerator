package tech.jknair.processor

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class StubFactory(
    vararg val targetClass: KClass<*>
)