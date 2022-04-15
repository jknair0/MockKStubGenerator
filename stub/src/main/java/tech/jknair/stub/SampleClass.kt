package tech.jknair.stub

import tech.jknair.processor.annotations.StubFactory
import tech.jknair.simpleksp.SomeConcreteClass
import tech.jknair.simpleksp.SomeOtherRepository
import tech.jknair.simpleksp.SomeRepository

@StubFactory(
    SomeRepository::class,
    SomeOtherRepository::class,
    SomeConcreteClass::class
)
interface SampleClass