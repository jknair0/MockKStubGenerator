package tech.jknair.stub.otherpackage

import tech.jknair.processor.annotations.StubFactory
import tech.jknair.simpleksp.SomeOtherRepository
import tech.jknair.simpleksp.SomeRepository

@StubFactory(
    SomeRepository::class,
    SomeOtherRepository::class
)
interface OtherSampleClass {

}