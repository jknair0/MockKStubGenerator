package tech.jknair.simpleksp

interface SomeOtherRepository {

    fun funWithNonNulls(code: Int, message: String)

    fun funWithNullables(code: Int?, message: String?)

    fun funWithDefaultParams(code: Int = -1, message: String = "")

    fun funWithHalfDefaultParams(code: Int = -1, message: String)

}