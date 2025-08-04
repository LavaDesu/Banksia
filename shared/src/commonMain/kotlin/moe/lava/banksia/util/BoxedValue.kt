package moe.lava.banksia.util

class BoxedValue<T>(val value: T) {
    operator fun component1() = value

    companion object {
        fun <T> T.box() = BoxedValue(this)
    }
}
