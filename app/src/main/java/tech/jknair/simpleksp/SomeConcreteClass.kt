package tech.jknair.simpleksp

class SomeConcreteClass {

    init {
        println("this is just an init block that does nothing but helps with the test")
    }

    fun sum(a: Int, b: Int): Int {
        return a + b
    }

    fun sum10(a: Int, b: Int = 10): Int {
        return a + b
    }

}