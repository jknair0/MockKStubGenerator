# MockKStubGenerator

generates the reusable mockk wrapped stubs needed when testing


## Possible Improvements

- Can use kotlin-poet instead of boilerplate.


### Usage

declaring all the classes whose stubs to be generated
check (example-stub/src/main/java/tech/jknair/stub/SampleClass.kt)
```kotlin
@StubFactory(
    targetClass = [
        SomeRepository::class,
        SomeOtherRepository::class,
        SomeConcreteClass::class
    ]
)
interface SampleClass
```

for input file `SomeOtherRepository.kt` with content
```kotlin
interface SomeOtherRepository {

    fun funWithNonNulls(code: Int, message: String)

    fun funWithNullables(code: Int?, message: String?)

    fun funWithDefaultParams(code: Int = -1, message: String = "")

    fun funWithHalfDefaultParams(code: Int = -1, message: String)

    fun funWithGenericDefaultParams(
        someRandomPair: Pair<Map<out CharSequence, SomeRepository>, HashSet<in SomeOtherRepository?>>? = Pair(
            mapOf(),
            hashSetOf()
        )
    )

    fun funWithGenericDefaultParamsWithStar(
        pair: Pair<*, HashMap<String, *>>
    )

    suspend fun suspendFunction()

    fun funTheReturns(code: Int, message: String): HashMap<Pair<String, *>, String?>

    suspend fun suspendFunTheReturns(code: Int, message: String): HashMap<Pair<String, *>, String?>

    fun getConfig(callback: (value: Int) -> Unit)

}
```

it would generate 

```kotlin
package tech.jknair.simpleksp

open class StubSomeOtherRepository(private val mockedSomeOtherRepository: tech.jknair.simpleksp.SomeOtherRepository = io.mockk.mockk()) {

	fun funTheReturns(
		code: kotlin.Int,
		message: kotlin.String,
		stub_returnValue: kotlin.collections.HashMap<kotlin.Pair<kotlin.String, *>, kotlin.String?>
	) {
		io.mockk.every { mockedSomeOtherRepository.funTheReturns(code, message) } returns stub_returnValue
	}

	fun funWithDefaultParams(
		code: kotlin.Int,
		message: kotlin.String,
	) {
		io.mockk.every { mockedSomeOtherRepository.funWithDefaultParams(code, message) } returns kotlin.Unit
	}

	fun funWithGenericDefaultParams(
		someRandomPair: kotlin.Pair<kotlin.collections.Map<out kotlin.CharSequence, tech.jknair.simpleksp.SomeRepository>, kotlin.collections.HashSet<in tech.jknair.simpleksp.SomeOtherRepository?>>?,
	) {
		io.mockk.every { mockedSomeOtherRepository.funWithGenericDefaultParams(someRandomPair) } returns kotlin.Unit
	}

	fun funWithGenericDefaultParamsWithStar(
		pair: kotlin.Pair<*, kotlin.collections.HashMap<kotlin.String, *>>,
	) {
		io.mockk.every { mockedSomeOtherRepository.funWithGenericDefaultParamsWithStar(pair) } returns kotlin.Unit
	}

	fun funWithHalfDefaultParams(
		code: kotlin.Int,
		message: kotlin.String,
	) {
		io.mockk.every { mockedSomeOtherRepository.funWithHalfDefaultParams(code, message) } returns kotlin.Unit
	}

	fun funWithNonNulls(
		code: kotlin.Int,
		message: kotlin.String,
	) {
		io.mockk.every { mockedSomeOtherRepository.funWithNonNulls(code, message) } returns kotlin.Unit
	}

	fun funWithNullables(
		code: kotlin.Int?,
		message: kotlin.String?,
	) {
		io.mockk.every { mockedSomeOtherRepository.funWithNullables(code, message) } returns kotlin.Unit
	}

	fun suspendFunTheReturns(
		code: kotlin.Int,
		message: kotlin.String,
		stub_returnValue: kotlin.collections.HashMap<kotlin.Pair<kotlin.String, *>, kotlin.String?>
	) {
		io.mockk.coEvery { mockedSomeOtherRepository.suspendFunTheReturns(code, message) } returns stub_returnValue
	}

	fun suspendFunction(
	) {
		io.mockk.coEvery { mockedSomeOtherRepository.suspendFunction() } returns kotlin.Unit
	}

}
```