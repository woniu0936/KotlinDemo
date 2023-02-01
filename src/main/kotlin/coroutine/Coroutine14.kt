package coroutine

import kotlinx.coroutines.*

fun main() {
//    mainLaunch()
//    mainRunBlock()
//    mainRunBlock2()
//    mainRunBlock3()
//    mainAsync()
    mainTest()
}

/**
 * 课后测试题，分析这个方法的执行结果
 *
 * async函数就像launch函数会异步执行，所以会有输出结果，
 * 只是async函数有个对应的await函数可以接收返回值，这个接收过程是阻塞式的
 */
fun mainTest() = runBlocking {
    val deferred: Deferred<String> = async {
        println("in async: ${Thread.currentThread().name} ")
        //模拟耗时操作
        delay(1000L)
        println("in async after delay")
        return@async "Task Complete!"
    }
    //这里没有调用deferred.await()方法，但是async里面的代码还是执行了，说明async里面代码的执行不依赖是否需要获取返回值
//    deferred.await()
    delay(2000L)
}

/**
 * async是有返回值且不阻塞的协程，是runBlock很好的替代
 */
fun mainAsync() = runBlocking {
    println("in runBlocking: ${Thread.currentThread().name} ")
    val deferred: Deferred<String> = async {
        println("in async: ${Thread.currentThread().name} ")
        //模拟耗时操作
        delay(1000L)
        return@async "Task Complete!"
    }
    println("after async: ${Thread.currentThread().name} ")
    val result = deferred.await()
    println("Result: $result")
}

/**
 * runBlock有返回值
 */
fun mainRunBlock3() {
    val result = runBlocking {
        delay(1000L)
        return@runBlocking "Coroutine done"
    }

    println("Result: $result")
}

/**
 * 【注意】请不要再生产环境中使用runBlocking，runBlocking一般用来连接线程和编写demo
 */
fun mainRunBlock2() {
    runBlocking {
        println("First: ${Thread.currentThread().name}")
        delay(1000L)
        println("hello First")
    }

    runBlocking {
        println("Second: ${Thread.currentThread().name}")
        delay(1000L)
        println("hello Second")
    }

    runBlocking {
        println("Third: ${Thread.currentThread().name}")
        delay(1000L)
        println("hello Third")
    }

    //注意这里没有调用Thread.sleep()方法，但是runBlock还是会执行完，说明runBLock阻塞了线程
    println("Process End")

}

/**
 * runBlocking 阻塞式
 */
fun mainRunBlock() {
    runBlocking {
        println("Coroutines start")
        delay(1000L)
        println("hello world")
    }

    println("Coroutines after")
    Thread.sleep(2000L)
    println("process end")

}

/**
 * launch 非阻塞式
 */
fun mainLaunch() {
    GlobalScope.launch(Dispatchers.IO) {
        println("Coroutine start: ${Thread.currentThread().name}")
        delay(1000L)
        println("Hello World")
    }
    println("Coroutine after: ${Thread.currentThread().name}")
    Thread.sleep(2000L)

}