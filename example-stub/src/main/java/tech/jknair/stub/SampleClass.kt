package tech.jknair.stub

import tech.jknair.annotations.StubFactory
import tech.jknair.simpleksp.SomeConcreteClass
import tech.jknair.simpleksp.SomeOtherRepository
import tech.jknair.simpleksp.SomeRepository

@StubFactory(
    targetClass = [
        SomeRepository::class,
        SomeOtherRepository::class,
        SomeConcreteClass::class
    ]
)
interface SampleClass