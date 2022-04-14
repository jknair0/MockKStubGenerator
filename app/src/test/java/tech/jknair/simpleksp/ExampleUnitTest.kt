package tech.jknair.simpleksp

import org.junit.Test

import org.junit.Assert.*
import tech.jknair.stub.StubSomeRepository

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private val stubSomeRepository: StubSomeRepository = StubSomeRepository()

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}