package tech.jknair.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class StubFactory(
    vararg val targetClass: KClass<*>
)