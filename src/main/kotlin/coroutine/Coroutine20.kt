package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() {
//    flow1()
//    flow2()
//    flow3()
//    flow4()
    flow5()
//    flow6()
//    flow7()
}

/**
 * launchIn的基本使用(补充flowOn的不足)，onEach代替collection
 */
fun flow7() = runBlocking {
    val scope = CoroutineScope(mySingleDispatcher)
    flow {
        logX("start")
        emit(1)
        logX("emit(1)")
        emit(2)
        logX("emit(2)")
        emit(3)
        logX("emit(3)")
    }
        .flowOn(Dispatchers.IO)
        .filter {
            logX("filter: $it")
            it > 2
        }
        .onEach {
            logX("onEach: $it")
        }
        .launchIn(scope)

}

/**
 * 协程上下文切换, flowOn()仅作用于他的上游
 */
fun flow6() = runBlocking {
    flow {
        logX("start")
        emit(1)
        logX("emit(1)")
        emit(2)
        logX("emit(2)")
        emit(3)
        logX("emit(3)")
    }
        .filter {
            logX("filter: $it")
            it > 2
        }
        .flowOn(Dispatchers.IO)
        .collect {
            logX("collect: $it")
        }
}

/**
 * catch只能catch住他上游的exception
 */
fun flow5() = runBlocking {
    flow {
        emit(1)
        emit(2)
        throw Exception("this is e error")
        emit(3)
    }
        .map { it * 2 }
        .catch {
            println("catch: $it")
        }
        .onCompletion {
            println("completion: $it")
        }
        .collect {
            println("collect: $it")
        }
}

fun flow4() = runBlocking {
    flowOf(1, 2, 3, 4, 5)
        .filter {
            println("filter: $it")
            it > 2
        }
        .map {
            println("map: $it")
            it * 2
        }
        .take(2)
        .onStart {
            println("on start")
        }
        .onCompletion {
            println("on completion")
        }
        .collect {
            println("collect: $it")
        }

}

/**
 * 对比flow2可以看出，list和flow之间是可以转换的
 */
fun flow3() = runBlocking {
    println("---------------------flow-------------------")
    flowOf(1, 2, 3, 4, 5)
        .toList()
        .filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .forEach {
            println(it)
        }

    println("---------------------list-------------------")
    listOf(1, 2, 3, 4, 5)
        .asFlow()
        .filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .collect {
            println(it)
        }
}

/**
 * 可以看到flow的api和list基本相同
 */
fun flow2() = runBlocking {
    println("---------------------flow-------------------")
    flowOf(1, 2, 3, 4, 5)
        .filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .collect {
            println(it)
        }

    println("---------------------list-------------------")
    listOf(1, 2, 3, 4, 5)
        .filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .forEach {
            println(it)
        }
}

fun flow1() = runBlocking {
    flow {
        emit(1)
        emit(2)
        emit(3)
        emit(4)
        emit(5)
    }.filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .collect {
            println(it)
        }

}