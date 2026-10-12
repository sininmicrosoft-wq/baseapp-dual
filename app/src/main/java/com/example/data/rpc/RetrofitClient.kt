package com.example.data.rpc

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client configuration file that initializes BaseRpcService using
 * the Base Chain RPC base URL ("https://mainnet.base.org/") and provides an
 * OkHttpClient with full HTTP logging for debugging network calls.
 */
object RetrofitClient {

  /** Base Chain Mainnet JSON-RPC base URL */
  const val BASE_CHAIN_RPC_URL: String = "https://mainnet.base.org/"

  /** Base Chain Sepolia Testnet JSON-RPC base URL */
  const val BASE_SEPOLIA_RPC_URL: String = "https://sepolia.base.org/"

  /** OP Mainnet JSON-RPC base URL */
  const val OP_MAINNET_RPC_URL: String = "https://mainnet.optimism.io/"

  /**
   * Logging interceptor configured to log full HTTP request and response
   * headers and body for debugging JSON-RPC network calls.
   */
  private val loggingInterceptor: HttpLoggingInterceptor by lazy {
    HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }
  }

  /**
   * Header interceptor attaching standard JSON-RPC 2.0 content types and user agent.
   */
  private val headerInterceptor: Interceptor by lazy {
    Interceptor { chain ->
      val request = chain.request().newBuilder()
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .header("User-Agent", "BaseApp-Android/1.0 (OP-Stack)")
        .build()
      chain.proceed(request)
    }
  }

  /**
   * OkHttpClient instance configured with logging, custom headers, and network timeouts.
   */
  val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .addInterceptor(headerInterceptor)
      .addInterceptor(loggingInterceptor)
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .retryOnConnectionFailure(true)
      .build()
  }

  /**
   * Moshi instance configured with Kotlin reflection adapter.
   */
  val moshi: Moshi by lazy {
    Moshi.Builder()
      .addLast(KotlinJsonAdapterFactory())
      .build()
  }

  /**
   * Retrofit instance configured with Base Chain RPC base URL, OkHttpClient, and Moshi converter.
   */
  val retrofit: Retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_CHAIN_RPC_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
  }

  /**
   * Initialized BaseRpcService instance ready for JSON-RPC calls.
   */
  val baseRpcService: BaseRpcService by lazy {
    retrofit.create(BaseRpcService::class.java)
  }

  /**
   * Creates a BaseRpcService instance targeting a custom RPC URL (e.g. Base Sepolia or Vibenet).
   */
  fun createCustomService(rpcBaseUrl: String): BaseRpcService {
    val cleanUrl = if (rpcBaseUrl.endsWith("/")) rpcBaseUrl else "$rpcBaseUrl/"
    return Retrofit.Builder()
      .baseUrl(cleanUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(BaseRpcService::class.java)
  }
}
