package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import java.util.concurrent.Executors

fun main() {
//    mainChannel1()
//    mainChannel2()
//    mainChannel3()
//    mainChannel4()
//    mainChannel5()
//    mainChannel6()
//    mainChannel7()
//    mainChannel8()
//    mainChannel9()
//    mainChannel10()
    mainChannel11()
}

fun mainChannel1() = runBlocking {
    //创建管道
    val channel = Channel<Int>()

    launch {
        (1..3).forEach {
            channel.send(it)
            logX("send int: $it")
        }
        //如果不主动close，则主线程不会退出，程序还会处于运行状态，程序结束的标志：Process finished with exit code 0
        channel.close()
    }

    launch {
        for (i in channel) {
            logX("Receive: $i")
        }
    }

    logX("end")
}

fun mainChannel2() = runBlocking {
    //创建管道
    val channel = Channel<Int>(capacity = UNLIMITED)

    launch {
        (1..3).forEach {
            channel.send(it)
            logX("send int: $it")
        }
        //如果不主动close，则主线程不会退出，程序还会处于运行状态，程序结束的标志：Process finished with exit code 0
        channel.close()
    }

    launch {
        for (i in channel) {
            logX("Receive: $i")
        }
    }

    logX("end")
}

fun mainChannel3() = runBlocking {
    //创建管道
    val channel = Channel<Int>(capacity = CONFLATED)

    launch {
        (1..3).forEach {
            channel.send(it)
            logX("send int: $it")
        }
        //如果不主动close，则主线程不会退出，程序还会处于运行状态，程序结束的标志：Process finished with exit code 0
        channel.close()
    }

    launch {
        for (i in channel) {
            logX("Receive: $i")
        }
    }

    logX("end")
}

fun mainChannel4() = runBlocking {
    //创建管道
    val channel = Channel<Int>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    launch {
        (1..3).forEach {
            channel.send(it)
            logX("send int: $it")
        }
        //如果不主动close，则主线程不会退出，程序还会处于运行状态，程序结束的标志：Process finished with exit code 0
        channel.close()
    }

    launch {
        for (i in channel) {
            logX("Receive: $i")
        }
    }

    logX("end")
}

fun mainChannel5() = runBlocking {
    //创建管道
    val channel = Channel<Int>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    launch {
        (1..3).forEach {
            channel.send(it)
            logX("send int: $it")
        }
        //如果不主动close，则主线程不会退出，程序还会处于运行状态，程序结束的标志：Process finished with exit code 0
        channel.close()
    }

    launch {
        for (i in channel) {
            logX("Receive: $i")
        }
    }

    logX("end")
}

fun mainChannel6() = runBlocking {
    //创建管道
    val channel = Channel<Int>(
        capacity = 3,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    launch {
        (1..3).forEach {
            channel.send(it)
            logX("send int: $it")
        }
        //会被丢弃
        channel.send(4)
        logX("send int: 4")
        //会被丢弃
        channel.send(5)
        logX("send int: 5")
        //如果不主动close，则主线程不会退出，程序还会处于运行状态，程序结束的标志：Process finished with exit code 0
        channel.close()
    }

    launch {
        for (i in channel) {
            logX("Receive: $i")
        }
    }

    logX("end")
}

fun mainChannel7() = runBlocking {
    //创建无限容量管道
    val channel = Channel<Int>(
        capacity = UNLIMITED
    ) {
        //onUndeliveredElement用来接收channel发送出去但没有被接收的数据
        println("onUndeliveredElement = $it")
    }

    //等价于如下写法
//    val channel = Channel<Int>(capacity = UNLIMITED, onUndeliveredElement = {println("onUndeliveredElement = $it")})

    (1..3).forEach {
        channel.send(it)
    }

    //取出一个剩下两个
    val receive = channel.receive()
    println("channel 中取出了一个： $receive")

    //取消当前channel
    channel.cancel()

    logX("end")
}

fun mainChannel8() = runBlocking {
    //channel创建方式发生改变，这样的话，就不用手动去close channel了，防止忘记close导致程序一直处于运行状态
    val channel: ReceiveChannel<Int> = produce {
        (1..3).forEach {
            send(it)
            logX("send $it")
        }
    }

    launch {
        //接收数据
        for (i in channel) {
            logX("receive: $i")
        }
    }

    logX("end!")
}

fun mainChannel9() = runBlocking {
    //channel创建方式发生改变，这样的话，就不用手动去close channel了，防止忘记close导致程序一直处于运行状态
    val channel: ReceiveChannel<Int> = produce {
        (1..3).forEach {
            send(it)
            logX("send $it")
        }
    }

    //调用4次接收，看运行结果可以验证通过produce创建的channel会自动close
    channel.receive()
    channel.receive()
    channel.receive()
    channel.receive()       // kotlinx.coroutines.channels.ClosedReceiveChannelException: Channel was closed

    logX("end!")
}

/**
 * kotlinx.coroutines.channels.ClosedReceiveChannelException: Channel was closed
 * 当channel指定了capacity之后，使用channel.isClosedForReceive是不可靠的
 */
fun mainChannel10() = runBlocking {
    //channel创建方式发生改变，这样的话，就不用手动去close channel了，防止忘记close导致程序一直处于运行状态
    val channel: ReceiveChannel<Int> = produce(capacity = 3) {
        (1..300).forEach {
            send(it)
            logX("send $it")
        }
    }

    while (!channel.isClosedForReceive) {
        val i = channel.receive()
        logX("receive: $i")
    }

    logX("end!")
}

fun mainChannel11() = runBlocking {
    //channel创建方式发生改变，这样的话，就不用手动去close channel了，防止忘记close导致程序一直处于运行状态
    val channel: ReceiveChannel<Int> = produce(capacity = 3) {
        (1..300).forEach {
            send(it)
            logX("send $it")
        }
    }

    //注意尽量不要使用channel.receive(),使用如下方法更可靠
    channel.consumeEach {
        logX("receive: $it")
    }

    logX("end!")
}