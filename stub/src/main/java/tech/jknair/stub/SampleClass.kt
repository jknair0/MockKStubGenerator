package tech.jknair.stub

import tech.jknair.processor.StubFactory
import tech.jknair.simpleksp.SomeOtherRepository
import tech.jknair.simpleksp.SomeRepository

@StubFactory(
    SomeRepository::class,
    SomeOtherRepository::class
)
interface SampleClass {

}