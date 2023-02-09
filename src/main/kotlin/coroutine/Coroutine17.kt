package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import java.util.concurrent.Executors

fun main() {
//    mainContext1()
//    mainContext2()
//    mainContext3()
//    mainContext4()
//    mainContext5()
//    mainContext6()
//    mainContext7()
    mainContextTest()
}

suspend fun testCoroutineContext() = coroutineContext

fun mainContextTest() = runBlocking {
    println(testCoroutineContext())

}

fun mainContext7() = runBlocking {
    val handler = CoroutineExceptionHandler { _, throwable ->
        println("catch exception: $throwable")
    }
    val scope = CoroutineScope(Job() + mySingleDispatcher)
    val job = scope.launch(handler) {
        val s: String? = null
        s!!.length
    }
    job.join()
}

@OptIn(ExperimentalStdlibApi::class)
fun mainContext6() = runBlocking {
    //注意这里

    val scope = CoroutineScope(Job() + mySingleDispatcher)

    scope.launch(CoroutineName("myTestCoroutineName")) {
        //注意这里coroutineContext[CoroutineDispatcher]相当于CoroutineContext.get(CoroutineDispatcher),类比map的使用
        logX(coroutineContext[CoroutineDispatcher] == mySingleDispatcher)
        delay(1000L)
        logX("First end!")      //不会执行
    }
    delay(500L)
    scope.cancel()
    delay(1000L)
}

fun mainContext5() = runBlocking {
    val scope = CoroutineScope(Job())

    scope.launch {
        logX("First coroutine start!")
        delay(1000L)
        logX("First coroutine end!")
    }

    scope.launch {
        logX("Second coroutine start!")
        delay(1000L)
        logX("Second coroutine end!")
    }

    scope.launch {
        logX("Third coroutine start!")
        delay(1000L)
        logX("Third coroutine end!")
    }

    delay(500L)
    scope.cancel()
    delay(1000L)
}

val mySingleDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

fun mainContext4() = runBlocking(mySingleDispatcher) {
    val userInfo = getUserInfoContext()
    logX(userInfo)
}

fun mainContext3() = runBlocking(Dispatchers.Default) {
    val userInfo = getUserInfoContext()
    logX(userInfo)
}

fun mainContext2() = runBlocking(Dispatchers.IO) {
    val userInfo = getUserInfoContext()
    logX(userInfo)
}

fun mainContext1() = runBlocking {
    val userInfo = getUserInfoContext()
    logX(userInfo)
}

suspend fun getUserInfoContext(): String {
    logX("Before IO Context")
    withContext(Dispatchers.IO) {
        logX("In IO Context")
        delay(1000L)
    }
    logX("After IO Context")
    return "Boy Coder"
}
