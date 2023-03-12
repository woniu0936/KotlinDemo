package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.sql.PreparedStatement
import kotlin.system.measureTimeMillis


/**
 * 线程和协程的并发操作
 */
fun main() {
//    multiCoroutine1()
//    multiCoroutine2()
//    multiCoroutine3()
//    multiCoroutine4()
//    multiCoroutine5()
//    multiCoroutine6()
//    multiCoroutine7()
//    multiCoroutine8()
    multiCoroutine9()
}

/**
 * 对multiCoroutine8()使用函数式编程思想重构
 * 【重点】函数式编程具有不可变性和无副作用性，所以无惧并发编程
 */
fun multiCoroutine9() = runBlocking {

    val result = (1..10).map {
        async(Dispatchers.Default) {
            var i = 0
            repeat(1000) {
                i++
            }
            return@async i
        }
    }.awaitAll()
        .sum()

    println("i = $result")

}

/**
 * 避免共享状态，每个协程都有自己的变量i，最后把所有协程的结果加起来就行了
 * 【重点】这里借鉴的是函数式编程当中的不可变性和无副作用
 */
fun multiCoroutine8() = runBlocking {
    val deferreds = mutableListOf<Deferred<Int>>()

    repeat(10) {
        val deferred = async(Dispatchers.Default) {
            var i = 0
            repeat(1000) {
                i++
            }
            return@async i
        }
        deferreds.add(deferred)
    }

    var result = 0
    deferreds.forEach {
        result += it.await()
    }

    println("i = $result")

}

sealed class Msg
object AddMsg : Msg()
class ResultMsg(
    val result: CompletableDeferred<Int>
) : Msg()

fun multiCoroutine7() = runBlocking {
    //Actor就是对channel的封装，他的多线程同步能力来自于channel
    suspend fun addActor() = actor<Msg> {
        var count = 0
        for (msg in channel) {
            when (msg) {
                is AddMsg -> {
                    count++
                }
                is ResultMsg -> {
                    msg.result.complete(count)
                }
            }

        }
    }

    val addActor = addActor()
    val jobs = mutableListOf<Job>()

    repeat(10) {
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                //这里AddMsg是一个单例，很细节
                addActor.send(AddMsg)
            }
        }
        jobs.add(job)
    }

    jobs.joinAll()

    val deferred = CompletableDeferred<Int>()
    addActor.send(ResultMsg(deferred))

    val result = deferred.await()
    println("i = $result")
}

/**
 * 利用kotlin的Mutex来解决multiCoroutine2()中的多线程同步问题
 * 【重点】Mutex相比于java的同步锁，具备挂起和恢复的能力，更适合协程
 */
fun multiCoroutine6() = runBlocking {
    val mutex = Mutex()
    var i = 0
    //变化在这里
    val jobs = mutableListOf<Job>()
    repeat(10) {
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                //变化在这里, 注意不要直接使用mutex.lock()和mutex.unlock()，否则执行的过程中报错的话就会导致mutex.unlock()没执行，程序一直卡主的问题
                mutex.withLock {
                    i++
                }
            }
        }
        jobs.add(job)
    }

    //等待协程执行完毕
    jobs.joinAll()
    println("i = $i")
}

/**
 * 利用kotlin协程来解决multiCoroutine2()中的多线程同步问题
 * 【重点】这里虽然创建了10个协程，但是是在同一个线程里面执行，所以既会并发执行，而且不会存在多线程同步的问题
 */
fun multiCoroutine5() = runBlocking {
    var i = 0
    //变化在这里
    val jobs = mutableListOf<Job>()
    repeat(10) {
        val job = launch(mySingleDispatcher) {
            repeat(1000) {
                //变化在这里
//                    编译器报错，因为synchronized是java线程的产物，所以不能解决全部协程的问题
//                    prepare()
                i++
            }
        }
        jobs.add(job)
    }

    //等待协程执行完毕
    jobs.joinAll()
    println("i = $i")
}

/**
 * 通过async是实现一个经典的单线程并发demo
 */
fun multiCoroutine4() = runBlocking {
    suspend fun getResult1(): String {
        logX("start getResult1")
        //模拟耗时操作
        delay(1000)
        logX("end getResult1")
        return "result1"
    }

    suspend fun getResult2(): String {
        logX("start getResult2")
        //模拟耗时操作
        delay(1000)
        logX("end getResult2")
        return "result2"
    }

    suspend fun getResult3(): String {
        logX("start getResult3")
        //模拟耗时操作
        delay(1000)
        logX("end getResult3")
        return "result3"
    }

    val result = mutableListOf<String>()

    val time = measureTimeMillis {
        val deferred1 = async { getResult1() }
        val deferred2 = async { getResult2() }
        val deferred3 = async { getResult3() }
        result.add(deferred1.await())
        result.add(deferred2.await())
        result.add(deferred3.await())
    }
    logX("耗时: $time")

}

/**
 * 利用java的synchronized来解决multiCoroutine2()中的多线程同步问题
 */
fun multiCoroutine3() = runBlocking {
    var i = 0
    //变化在这里
    val lock = Any()
    val jobs = mutableListOf<Job>()
    repeat(10) {
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                //变化在这里
                synchronized(lock) {
//                    编译器报错，因为synchronized是java线程的产物，所以不能解决全部协程的问题
//                    prepare()
                    i++
                }
            }
        }
        jobs.add(job)
    }

    //等待协程执行完毕
    jobs.joinAll()
    println("i = $i")
}

/**
 * 这里Default是一个线程池，所以这里创建的10个协程是在不用的线程里面工作，会有多线程同步的问题
 */
fun multiCoroutine2() = runBlocking {
    var i = 0
    val jobs = mutableListOf<Job>()
    repeat(10) {
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                i++
            }
        }
        jobs.add(job)
    }

    //等待协程执行完毕
    jobs.joinAll()
    println("i = $i")
}

/**
 * 所有的鞋操作都在一个协程，所以不存在多线程同步的问题
 */
fun multiCoroutine1() = runBlocking {
    var i = 0

    launch(Dispatchers.Default) {
        repeat(1000) {
            i++
        }
    }

    delay(1000L)
    println("i = $i")
}