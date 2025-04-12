package moe.lava.banksia

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform