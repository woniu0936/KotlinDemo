package coroutine

import kotlinx.coroutines.*
import java.util.*
import kotlin.system.measureTimeMillis

fun main() {
//    main1()
//    main2()
//    main3()
//    main4()
//    main5()
//    main6()
//    main7()
//    main8()
//    mainTest16()
//    mainTest16Answer()
    mainT()
}

fun mainT() = runBlocking {
    val job = launch {
        logX("First coroutine start!")
        delay(1000L)
        logX("First coroutine end!")
    }
    job.join()
    val job2 = launch(job) {
        logX("Second coroutine start!")
        delay(1000L)
        logX("Second coroutine end!")
    }
    job2.join()
    logX("Process end!")
}

/**
 * 测试题答案，思考为什么
 */
fun mainTest16Answer() = runBlocking {
    suspend fun test1(): String {
        logX("result1")
        delay(1000L)
        return "result1"
    }

    suspend fun test2(): String {
        logX("result2")
        delay(1000L)
        return "result2"
    }

    suspend fun test3(): String {
        logX("result3")
        delay(1000L)
        return "result3"
    }

    val list = mutableListOf<String>()

    val time = measureTimeMillis {
        val result1 = async { test1() }
        val result2 = async { test2() }
        val result3 = async { test3() }
        list.add(result1.await())
        list.add(result2.await())
        list.add(result3.await())
    }

    println("time: $time")
    println(list)
}

/**
 * 测试题，优化这段代码
 */
fun mainTest16() = runBlocking {
    suspend fun test1(): String {
        delay(1000L)
        return "result1"
    }

    suspend fun test2(): String {
        delay(1000L)
        return "result2"
    }

    suspend fun test3(): String {
        delay(1000L)
        return "result3"
    }

    val list = mutableListOf<String>()

    val time = measureTimeMillis {
        list.add(test1())
        list.add(test2())
        list.add(test3())
    }

    println("time: $time")
    println(list)
}

fun main8() = runBlocking {
    val parentJob: Job
    var job1: Job? = null
    var job2: Job? = null
    var job3: Job? = null

    parentJob = launch {
        job1 = launch {
            logX("job1 start")
            delay(1000L)
            logX("job1 end")
        }

        job2 = launch {
            logX("job2 start")
            delay(3000L)
            logX("job2 end")
        }

        job3 = launch {
            logX("job3 start")
            delay(5000L)
            logX("job3 end")
        }
    }

    delay(500L)

    // === 代表引用相等，即是否是同一个对象
    parentJob.children.forEachIndexed { index, job ->
        when (index) {
            0 -> {
                println("job === job1: ${job === job1}")
            }
            1 -> {
                println("job === job2: ${job === job2}")
            }
            2 -> {
                println("job === job3: ${job === job3}")
            }
        }
    }

    parentJob.cancel()
    logX("Process end!")

}

fun main7() = runBlocking {
    val parentJob: Job
    var job1: Job? = null
    var job2: Job? = null
    var job3: Job? = null

    parentJob = launch {
        job1 = launch {
            delay(1000L)
        }

        job2 = launch {
            delay(3000L)
        }

        job3 = launch {
            delay(5000L)
        }
    }

    delay(500L)

    // === 代表引用相等，即是否是同一个对象
    parentJob.children.forEachIndexed { index, job ->
        when (index) {
            0 -> {
                println("job === job1: ${job === job1}")
            }
            1 -> {
                println("job === job2: ${job === job2}")
            }
            2 -> {
                println("job === job3: ${job === job3}")
            }
        }
    }

    parentJob.join()
    logX("Process end!")

}

fun main6() = runBlocking {
    val deferred = async {
        logX("Coroutine start!")
        delay(1000L)
        logX("Coroutine end!")
        "Coroutine result!"
    }
    val result = deferred.await()
    println("result: $result")
    logX("Process end")
}

fun main5() = runBlocking {
    suspend fun download() {
        //模拟下载任务
        val time = (Random().nextDouble() * 1000).toLong()
        logX("Delay time: $time")
        delay(time)
    }

    val job = launch(start = CoroutineStart.LAZY) {
        logX("Coroutine start!")
        download()
        logX("Coroutine end!")
    }
    delay(500L)
    job.log()
    job.start()
    job.log()
    job.invokeOnCompletion {
        job.log()
    }
    job.join()
    logX("Process end!")
}

fun main4() = runBlocking {
    val job = launch(start = CoroutineStart.LAZY) {
        logX("Coroutine start")
        delay(4000L)
    }
    delay(500L)
    job.log()
    job.start()
    job.log()
    delay(1100L)
    job.log()
    delay(2000L)
    logX("process end!")
}

fun main3() = runBlocking {
    val job = launch(start = CoroutineStart.LAZY) {
        logX("Coroutine start")
        delay(1000L)
    }
    delay(500L)
    job.log()
    job.start()
    job.log()
    delay(1100L)
    job.log()
    delay(2000L)
    logX("process end!")
}

fun main2() = runBlocking {
    val job = launch(start = CoroutineStart.LAZY) {
        logX("Coroutine start")
        delay(1000L)
    }
    delay(500L)
    job.log()
    job.start()
    job.log()
    delay(500L)
    job.cancel()
    delay(500L)
    job.log()
    delay(2000L)
    logX("process end!")
}

fun main1() = runBlocking {
    val job = launch {
        delay(1000L)
    }
    job.log()
    job.cancel()
    job.log()
    delay(1500L)
}

fun Job.log() {
    logX(
        """
        isActive = $isActive
        isCancelled = $isCancelled
        isCompleted = $isCompleted
    """.trimIndent()
    )
}

fun logX(any: Any?) {
    println(
        """
==================${Date().toLocaleString()}=================
$any
Thread:${Thread.currentThread().name}
==================================""".trimIndent()
    )
}

