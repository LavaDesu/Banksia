package moe.lava.banksia

actual fun log(tag: String, msg: String) {
    println("[$tag] $msg")
}