package coroutine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

fun main() {
//    select1()
//    select2()
//    select3()
//    select4()
//    select5()
//    select6()
    select7()
}

/**
 * 获取最快的结果之后，其他的结果就不用执行了，这样节省资源
 */
fun select7() = runBlocking {
    suspend fun <T> fastest(vararg deferreds: Deferred<T>): T = select<T> {
        fun cancelAll() = deferreds.forEach {
            it.cancel()
        }
        for (deferred in deferreds) {
            deferred.onAwait {
                cancelAll()
                it
            }
        }
    }

    val deferred1 = async {
        delay(100L)
        println("deferred1")
        "deferred1"
    }
    val deferred2 = async {
        delay(10L)
        println("deferred2")
        "deferred2"
    }
    val deferred3 = async {
        delay(120L)
        println("deferred3")
        "deferred3"
    }
    val deferred4 = async {
        delay(130L)
        println("deferred4")
        "deferred4"
    }
    val deferred5 = async {
        delay(150L)
        println("deferred5")
        "deferred5"
    }
    val deferred6 = async {
        delay(200L)
        println("deferred6")
        "deferred6"
    }

    val result = fastest(deferred1, deferred2, deferred3, deferred4, deferred5, deferred6)
    println("fastest result: $result")

}

fun select6() = runBlocking {
    val startTime = System.currentTimeMillis()
    val channel1 = produce {
        send("1")
        delay(200L)
        send("2")
        delay(200L)
//        send("3")
        delay(150L)//这句话不能少，不然c打印不出来
    }

    val channel2 = produce {
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
        delay(150L)
    }

    suspend fun selectChannel(channel1: ReceiveChannel<String>, channel2: ReceiveChannel<String>): String =
        select<String> {
            channel1.onReceiveCatching {
                it.getOrNull() ?: "channel1 is closed"
            }

            channel2.onReceiveCatching {
                it.getOrNull() ?: "channel2 is closed"
            }
        }

    repeat(6) {
        val result = selectChannel(channel1, channel2)
        println("channel, receive: $result")
    }

    //不取消的话，程序就不会退出
    channel1.cancel()
    channel2.cancel()

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}

fun select5() = runBlocking {
    val startTime = System.currentTimeMillis()
    val channel1 = produce {
        send("1")
        delay(200L)
        send("2")
        delay(200L)
        send("3")
        delay(150L)
    }

    val channel2 = produce {
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
        delay(150L)
    }

    suspend fun selectChannel(channel1: ReceiveChannel<String>, channel2: ReceiveChannel<String>): String =
        select<String> {
            channel1.onReceive {
                it.also {
                    println("channel1, receive: $it")
                }
            }

            channel2.onReceive {
                it.also {
                    println("channel2, receive: $it")
                }
            }
        }

    repeat(6) {
        selectChannel(channel1, channel2)
    }

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}

fun select4() = runBlocking {
    val startTime = System.currentTimeMillis()
    val channel1 = produce {
        send(1)
        delay(200L)
        send(2)
        delay(200L)
        send(3)
        delay(150L)
    }

    val channel2 = produce {
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
        delay(150L)
    }

    channel1.consumeEach {
        println("channel1, receive: $it")
    }

    channel2.consumeEach {
        println("channel2, receive: $it")
    }

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}

fun select3() = runBlocking {
    val startTime = System.currentTimeMillis()
    val produceId = "xxxxxId"

    val cacheDeferred = async {
        getCacheInfo(produceId)
    }

    val cacheDeferred2 = async {
        getCacheInfo2(produceId)
    }

    val networkDeferred = async {
        getNetworkInfo(produceId)
    }

    val product = select<Product?> {
        cacheDeferred.onAwait {
            it
        }

        cacheDeferred2.onAwait {
            it
        }

        networkDeferred.onAwait {
            it
        }
    }

    product?.let {
        updateUI(it)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }

    product?.let {
        if (it.isCache) {
            val result = networkDeferred.await() ?: return@runBlocking
            updateUI(result)
            println("Time cost: ${System.currentTimeMillis() - startTime}")
        }
    }

}

fun select2() = runBlocking {
    val startTime = System.currentTimeMillis()
    val produceId = "xxxxxId"

    val cacheDeferred = async {
        getCacheInfo(produceId)
    }

    val networkDeferred = async {
        getNetworkInfo(produceId)
    }

    val product = select<Product?> {
        cacheDeferred.onAwait {
            it
        }

        networkDeferred.onAwait {
            it
        }
    }

    product?.let {
        updateUI(it)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }

    product?.let {
        if (it.isCache) {
            val result = networkDeferred.await() ?: return@runBlocking
            updateUI(result)
            println("Time cost: ${System.currentTimeMillis() - startTime}")
        }
    }

}

fun select1() = runBlocking {
    val startTime = System.currentTimeMillis()
    val produceId = "xxxxxId"
    val product = select<Product?> {
        async {
            getCacheInfo(produceId)
        }.onAwait {
            it
        }

        async {
            getNetworkInfo(produceId)
        }.onAwait {
            it
        }
    }

    product?.let {
        updateUI(it)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }

}

fun updateUI(product: Product) {
    println("$product")
}

suspend fun getCacheInfo(produceId: String): Product? {
    delay(100L)
    return Product(produceId, 9.99, true)
}

suspend fun getCacheInfo2(produceId: String): Product? {
    delay(200L)
    return Product(produceId, 3.33, true)
}

suspend fun getNetworkInfo(produceId: String): Product? {
    delay(2000L)
    return Product(produceId, 1.11, false)
}

data class Product(
    val productId: String,
    val price: Double,
    //标记是否是缓存
    val isCache: Boolean
)