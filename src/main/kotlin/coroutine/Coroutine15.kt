package coroutine

fun main() {
//    getUserInfo()
}

suspend fun m1() {
    val user = getUserInfo()
}

suspend fun getUserInfo(): String {
    return "hello"
}