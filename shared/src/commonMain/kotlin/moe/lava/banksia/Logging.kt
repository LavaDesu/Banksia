package moe.lava.banksia

expect fun log(tag: String, msg: String)
fun error(tag: String, throwable: Throwable) = error(tag, "", throwable)
expect fun error(tag: String, msg: String, throwable: Throwable? = null)
