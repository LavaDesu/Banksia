package moe.lava.banksia.util

fun error(tag: String, throwable: Throwable) = error(tag, "", throwable)
expect fun log(tag: String, msg: String)
expect fun error(tag: String, msg: String, throwable: Throwable? = null)
