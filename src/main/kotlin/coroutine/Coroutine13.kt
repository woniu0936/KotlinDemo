package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlin.concurrent.thread


/**
 * 第十三讲，Kotlin协程示例
 */
fun main() {
//    mainNormal()
//    mainSequence()
//    mainChannel()

//    mainThread()
//    mainCoroutine()

    //通过如下两个方法的对比，启动10亿个协程可以正常运行，但是已启动10亿个线程直接会报内存不足，说明协程的确比线程【轻量】
//    runLargeCoroutines()
//    runLargeThreads()

//    runCoroutines()

//    runBlock()
    runNoBlock()
}

fun runBlock() {
    repeat(3) {
        Thread.sleep(1000L)
        println("print-1: ${Thread.currentThread().name}")
    }

    repeat(3) {
        Thread.sleep(900L)
        println("print-2: ${Thread.currentThread().name}")
    }
}

fun runNoBlock() = runBlocking {
    launch {
        repeat(3) {
            delay(1000L)
            println("print-1: ${Thread.currentThread().name}")
        }
    }

    launch {
        repeat(3) {
            delay(900L)
            println("print-2: ${Thread.currentThread().name}")
        }
    }

    delay(3000L)
}

/**
 * 通过输出的结果，可以看出协程没有个特定的线程绑定，同一个协程是会运行在不同的线程里面的
 */
fun runCoroutines() = runBlocking(Dispatchers.IO) {
    repeat(3) {
        launch {
            repeat(3) {
                println(Thread.currentThread().name)
                delay(100L)
            }
        }
    }
    Thread.sleep(5000L)
}

/**
 * 报错：
 * Exception in thread "main" java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached
at java.base/java.lang.Thread.start0(Native Method)
at java.base/java.lang.Thread.start(Thread.java:800)
at kotlin.concurrent.ThreadsKt.thread(Thread.kt:42)
at kotlin.concurrent.ThreadsKt.thread$default(Thread.kt:20)
at coroutine.Coroutine13Kt.runLargeThreads(Coroutine13.kt:29)
at coroutine.Coroutine13Kt.main(Coroutine13.kt:24)
at coroutine.Coroutine13Kt.main(Coroutine13.kt)
 */
fun runLargeThreads() {
    repeat(1000_000_000) {
        thread {
            Thread.sleep(1000000L)
        }
    }
    Thread.sleep(10000L)
}

/**
 * 能正常运行
 */
fun runLargeCoroutines() = runBlocking {
    repeat(1000_000_000) {
        launch {
            delay(1000000L)
        }
    }
    Thread.sleep(10000L)
}

/**
 * 启动两个协程
 * 【重点】协程可以理解为运行在线程当中的，更加轻量的task
 *
 * 调试协程，在IDEA中添加-Dkotlinx.coroutines.debug
 */
fun mainCoroutine() = runBlocking {
    println(Thread.currentThread().name)
    launch {
        println(Thread.currentThread().name)
        delay(100L)
    }
    Thread.sleep(1000L)
}

/**
 * 启动两个线程
 */
fun mainThread() {
    println(Thread.currentThread().name)
    thread {
        println(Thread.currentThread().name)
        Thread.sleep(100L)
    }
    Thread.sleep(1000L)
}

fun mainChannel() = runBlocking {
    println("===================================协程的函数调用(channel实现)===============================")
    val producer = getProducer(this)
    testConsumer(producer)
}

fun getProducer(scope: CoroutineScope) = scope.produce {
    println("send 1")
    //yield代表挂起、让步的意思
    send(1)
    println("send 2")
    send(2)
    println("send 3")
    send(3)
    println("send 4")
    send(4)
}

suspend fun testConsumer(channel: ReceiveChannel<Int>) {
    delay(100)
    val i = channel.receive()
    println("receive $i")
    delay(100)
    val j = channel.receive()
    println("receive $j")
    delay(100)
    val k = channel.receive()
    println("receive $k")
    delay(100)
    val l = channel.receive()
    println("receive $l")
}

fun mainSequence() = runBlocking {
    println("===================================协程的函数调用(sequence实现)===============================")
    val sequence = getSequence()
    printSequence(sequence)
    /*
    输出结果：
        add 1
        Get1
        add 2
        Get2
        add 3
        Get3
        add 4
        Get4
     */
}

fun getSequence() = sequence {
    println("add 1")
    //yield代表挂起、让步的意思
    yield(1)
    println("add 2")
    yield(2)
    println("add 3")
    yield(3)
    println("add 4")
    yield(4)
}

fun printSequence(sequence: Sequence<Int>) {
    val iterator = sequence.iterator()
    val i = iterator.next()
    println("Get$i")
    val j = iterator.next()
    println("Get$j")
    val k = iterator.next()
    println("Get$k")
    val l = iterator.next()
    println("Get$l")
}

fun mainNormal() {
    println("===================================普通的函数调用===============================")
    val list = getList()
    printList(list)
}

private fun getList(): List<Int> {
    val list = mutableListOf<Int>()
    println("add 1")
    list.add(1)
    println("add 2")
    list.add(2)
    println("add 3")
    list.add(3)
    println("add 4")
    list.add(4)
    return list
}

private fun printList(list: List<Int>) {
    val i = list[0]
    println("Get$i")
    val j = list[1]
    println("Get$j")
    val k = list[2]
    println("Get$k")
    val l = list[3]
    println("Get$l")
}

