package coroutine.demo

import com.google.gson.Gson
import com.google.gson.internal.`$Gson$Types`.getRawType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.lang.reflect.Method
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.util.Date
import java.util.concurrent.Flow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


interface Callback<T : Any> {

    fun onSuccess(data: T)

    fun onFailed(throwable: Throwable)

}

interface ApiServiceV3 {

    @GET("article/list/0/json")
    fun repos(
        @Field("author") author: String
    ): KtCall<HttpResult>

    @GET("article/list/0/json")
    fun reposSync(
        @Field("author") author: String
    ): HttpResult

}

class KtCall<T : Any>(
    private val call: Call,
    private val gson: Gson,
    private val type: Type
) {

    fun call(callback: Callback<T>): Call {
        //1、使用call请求api
        //2、根据请求结果，调用callback.onSuccess()或者callback.onFailed()
        //3、返回OkHttp的Call对象
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailed(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val bodyStr = response.body?.string()
                    println("===========================================================================")
                    println("type: $type")
                    println("body: $bodyStr")
                    println("===========================================================================")
                    val data = gson.fromJson<T>(bodyStr, type)
                    callback.onSuccess(data)
                } catch (e: Exception) {
                    callback.onFailed(e)
                }
            }
        })

        return call
    }

}

/**
 * 重点学习这个函数
 */
suspend fun <T : Any> KtCall<T>.await(): T = suspendCancellableCoroutine { continuation ->
    val call = call(object : Callback<T> {
        override fun onSuccess(data: T) {
            println("response success, data: $data")
//            continuation.resumeWith(Result.success(data))
            continuation.resume(data)
        }

        override fun onFailed(throwable: Throwable) {
            println("response failed, throwable: $throwable")
//            continuation.resumeWith(Result.failure(throwable))
            continuation.resumeWithException(throwable)
        }

    })

    continuation.invokeOnCancellation {
        println("call cancel")
        call.cancel()
    }
}

/**
 * 通过给KtCall添加扩展函数，将其结果转换成一个flow
 */
fun <T : Any> KtCall<T>.asFlow() = callbackFlow<T> {
    val call = call(object : Callback<T> {

        override fun onSuccess(data: T) {
//            trySend(data)
            //trySendBlocking相比trySend，当管道容量已经满了的时候，会等待管道空闲之后再返回成功
            //这里的onSuccess和onFailure不可获取，因为我们要close，否则调用完trySendBlocking，我们的程序不会退出
            trySendBlocking(data)
                .onSuccess { close() }
                .onFailure { close(it) }
        }

        override fun onFailed(throwable: Throwable) {
            close(throwable)
        }

    })

    awaitClose {
        call.cancel()
    }
}

object KtHttpV3 {

    private var okHttpClient: OkHttpClient = OkHttpClient()
    private var gson: Gson = Gson()
    var baseUrl = "https://wanandroid.com/"

    fun <T : Any> create(service: Class<T>): T {
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf<Class<*>>(service)
        ) { proxy, method, args ->
            val annotations = method.annotations
            for (annotation in annotations) {
                if (annotation is GET) {
                    val url = baseUrl + annotation.value
                    return@newProxyInstance invoke<T>(url, method, args!!)
                }
            }
            return@newProxyInstance null

        } as T
    }

    private fun <T : Any> invoke(path: String, method: Method, args: Array<Any>): Any? {
        if (method.parameterAnnotations.size != args.size) return null

        var url = path
        val parameterAnnotations = method.parameterAnnotations
        for (i in parameterAnnotations.indices) {
            for (parameterAnnotation in parameterAnnotations[i]) {
                if (parameterAnnotation is Field) {
                    val key = parameterAnnotation.value
                    val value = args[i].toString()
                    url += if (!url.contains("?")) {
                        "?$key=$value"
                    } else {
                        "&$key=$value"
                    }

                }
            }
        }
        println("====================================")
        println("$url")

        val request = Request.Builder()
            .url(url)
            .build()


        return if (isKtCallReturn(method)) {
            val call = okHttpClient.newCall(request)
            val genericReturnType = getTypeArgument(method)
            KtCall<T>(call, gson, genericReturnType)
        } else {
            val response = okHttpClient.newCall(request).execute()

            val genericReturnType = method.genericReturnType
            val json = response.body?.string()
            println("====================================")
            println("$json")
            println("====================================")
            gson.fromJson<Any?>(json, genericReturnType)
        }
    }

    private fun getTypeArgument(method: Method) =
        (method.genericReturnType as ParameterizedType).actualTypeArguments[0]

    private fun isKtCallReturn(method: Method) =
        getRawType(method.genericReturnType) == KtCall::class.java


}

fun main() {
//    testSync()
    testAsync()
}

fun mainCoroutine() = runBlocking {
    val api: ApiServiceV3 = KtHttpV3.create(ApiServiceV3::class.java)
    val result = api.repos("郭霖").await()
}

private fun testSync() {
    val api: ApiServiceV3 = KtHttpV3.create(ApiServiceV3::class.java)
    val data: HttpResult = api.reposSync("鸿洋")
    println(data)
}

private fun testAsync() {
    KtHttpV3.create(ApiServiceV3::class.java).repos("鸿洋").call(object : Callback<HttpResult> {

        override fun onSuccess(data: HttpResult) {
            println(data)
        }

        override fun onFailed(throwable: Throwable) {
            println(throwable)
        }
    })
}
