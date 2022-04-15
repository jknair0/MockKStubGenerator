package tech.jknair.simpleksp

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

}