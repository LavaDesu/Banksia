package moe.lava.banksia

import android.util.Log

actual fun log(tag: String, msg: String) {
    Log.i(tag, msg)
}
