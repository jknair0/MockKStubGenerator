package tech.jknair.processor.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class StubFactory(
    vararg val targetClass: KClass<*>
)