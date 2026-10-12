package com.example.data.rpc

import com.example.data.rpc.model.BlockRpcDetails
import com.example.data.rpc.model.JsonRpcRequest
import com.example.data.rpc.model.TransactionReceiptRpcDetails
import com.example.data.rpc.model.TransactionRpcDetails
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.math.BigInteger
import java.util.concurrent.TimeUnit

/**
 * Network client setup configuring OkHttp, Moshi, and Retrofit
 * to communicate with Base Chain and OP Superchain JSON-RPC endpoints.
 */
class BaseNetworkClient(
  private val defaultBaseUrl: String = "https://mainnet.base.org/"
) {

  private val headerInterceptor = Interceptor { chain ->
    val original = chain.request()
    val request = original.newBuilder()
      .header("User-Agent", "BaseApp-Android/1.0")
      .header("Accept", "application/json")
      .header("Content-Type", "application/json")
      .method(original.method, original.body)
      .build()
    chain.proceed(request)
  }

  private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(headerInterceptor)
    .addInterceptor(loggingInterceptor)
    .build()

  val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(defaultBaseUrl)
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

  val rpcService: BaseRpcService = retrofit.create(BaseRpcService::class.java)

  /**
   * Fetches latest block height from the given Base Chain RPC URL via eth_blockNumber.
   */
  suspend fun fetchBlockNumber(rpcUrl: String): Result<Long> = withContext(Dispatchers.IO) {
    try {
      val response = rpcService.eth_blockNumber(url = rpcUrl)

      if (response.isSuccessful) {
        val resultHex = response.body()?.result
        if (resultHex != null && resultHex.startsWith("0x")) {
          val blockNumber = parseHexToLong(resultHex)
          Result.success(blockNumber)
        } else {
          Result.failure(Exception("Invalid result hex: $resultHex"))
        }
      } else {
        Result.failure(Exception("HTTP error ${response.code()}: ${response.message()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches current L2 gas price in Gwei from the given Base Chain RPC URL via eth_gasPrice.
   */
  suspend fun fetchGasPriceGwei(rpcUrl: String): Result<Double> = withContext(Dispatchers.IO) {
    try {
      val response = rpcService.eth_gasPrice(url = rpcUrl)

      if (response.isSuccessful) {
        val resultHex = response.body()?.result
        if (resultHex != null && resultHex.startsWith("0x")) {
          val wei = parseHexToBigInteger(resultHex)
          val gwei = wei.toDouble() / 1_000_000_000.0
          Result.success(gwei)
        } else {
          Result.failure(Exception("Invalid gas price hex: $resultHex"))
        }
      } else {
        Result.failure(Exception("HTTP error ${response.code()}: ${response.message()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches complete transaction details by hash via eth_getTransactionByHash.
   */
  suspend fun fetchTransactionByHash(rpcUrl: String, txHash: String): Result<TransactionRpcDetails> = withContext(Dispatchers.IO) {
    try {
      val response = rpcService.eth_getTransactionByHash(
        url = rpcUrl,
        request = JsonRpcRequest(method = "eth_getTransactionByHash", params = listOf(txHash))
      )

      if (response.isSuccessful) {
        val details = response.body()?.result
        if (details != null) {
          Result.success(details)
        } else {
          Result.failure(Exception("Transaction not found or pending"))
        }
      } else {
        Result.failure(Exception("HTTP error ${response.code()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches transaction receipt details by hash via getTransactionReceipt.
   */
  suspend fun fetchTransactionReceipt(rpcUrl: String, txHash: String): Result<TransactionReceiptRpcDetails> = withContext(Dispatchers.IO) {
    try {
      val response = rpcService.getTransactionReceipt(
        url = rpcUrl,
        request = JsonRpcRequest(method = "eth_getTransactionReceipt", params = listOf(txHash))
      )

      if (response.isSuccessful) {
        val receipt = response.body()?.result
        if (receipt != null) {
          Result.success(receipt)
        } else {
          Result.failure(Exception("Receipt not available yet"))
        }
      } else {
        Result.failure(Exception("HTTP error ${response.code()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches block metadata and details via getBlockByNumber.
   */
  suspend fun fetchBlockDetails(rpcUrl: String, blockTag: String = "latest"): Result<BlockRpcDetails> = withContext(Dispatchers.IO) {
    try {
      val response = rpcService.getBlockByNumber(
        url = rpcUrl,
        request = JsonRpcRequest(method = "eth_getBlockByNumber", params = listOf(blockTag, false))
      )

      if (response.isSuccessful) {
        val block = response.body()?.result
        if (block != null) {
          Result.success(block)
        } else {
          Result.failure(Exception("Block not found"))
        }
      } else {
        Result.failure(Exception("HTTP error ${response.code()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches chain ID from the given RPC URL via getChainId.
   */
  suspend fun fetchChainId(rpcUrl: String): Result<Long> = withContext(Dispatchers.IO) {
    try {
      val response = rpcService.getChainId(
        url = rpcUrl,
        request = JsonRpcRequest(method = "eth_chainId", params = emptyList())
      )

      if (response.isSuccessful) {
        val resultHex = response.body()?.result
        if (resultHex != null && resultHex.startsWith("0x")) {
          Result.success(parseHexToLong(resultHex))
        } else {
          Result.failure(Exception("Invalid chainId hex: $resultHex"))
        }
      } else {
        Result.failure(Exception("HTTP error ${response.code()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches native ETH balance for a given address on Base via getBalance.
   */
  suspend fun fetchBalance(rpcUrl: String, address: String): Result<String> = withContext(Dispatchers.IO) {
    try {
      val response = rpcService.getBalance(
        url = rpcUrl,
        request = JsonRpcRequest(method = "eth_getBalance", params = listOf(address, "latest"))
      )

      if (response.isSuccessful) {
        val hex = response.body()?.result ?: "0x0"
        val wei = parseHexToBigInteger(hex)
        val eth = wei.toDouble() / 1e18
        Result.success(String.format("%.4f ETH", eth))
      } else {
        Result.failure(Exception("HTTP error ${response.code()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches an ERC-20 token balance for a given contract and wallet address via eth_call.
   */
  suspend fun fetchErc20Balance(
    rpcUrl: String,
    contractAddress: String,
    walletAddress: String,
    decimals: Int = 18
  ): Result<Pair<String, Double>> = withContext(Dispatchers.IO) {
    try {
      val response = rpcService.getErc20Balance(
        url = rpcUrl,
        contractAddress = contractAddress,
        walletAddress = walletAddress
      )

      if (response.isSuccessful) {
        val hex = response.body()?.result ?: "0x0"
        val rawUnits = parseHexToBigInteger(hex)
        val divisor = Math.pow(10.0, decimals.toDouble())
        val numericBalance = rawUnits.toDouble() / divisor
        Result.success(Pair(hex, numericBalance))
      } else {
        Result.failure(Exception("HTTP error ${response.code()}: ${response.message()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Dispatches arbitrary JSON-RPC requests via Retrofit's raw response body and measures duration.
   */
  suspend fun executeRawRpc(
    rpcUrl: String,
    method: String,
    paramsJson: String = "[]"
  ): Triple<Boolean, String, Long> = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
      val parsedParams = parseParamsJson(paramsJson)
      val request = JsonRpcRequest(method = method, params = parsedParams)

      val response = rpcService.callRawRpc(url = rpcUrl, request = request)
      val duration = System.currentTimeMillis() - startTime
      val rawBody = response.body()?.string() ?: response.errorBody()?.string() ?: "{}"

      if (response.isSuccessful) {
        val formatted = formatJson(rawBody)
        Triple(true, formatted, duration)
      } else {
        Triple(false, "HTTP ${response.code()}: $rawBody", duration)
      }
    } catch (e: Exception) {
      val duration = System.currentTimeMillis() - startTime
      Triple(false, "Network error: ${e.localizedMessage ?: "Unknown"}", duration)
    }
  }

  private fun parseParamsJson(raw: String): List<Any> {
    return try {
      val trimmed = raw.trim()
      if (trimmed.startsWith("[")) {
        val array = JSONArray(trimmed)
        val list = mutableListOf<Any>()
        for (i in 0 until array.length()) {
          list.add(array.get(i))
        }
        list
      } else {
        emptyList()
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun parseHexToLong(hex: String): Long {
    val clean = hex.removePrefix("0x")
    return if (clean.isEmpty()) 0L else clean.toLong(16)
  }

  private fun parseHexToBigInteger(hex: String): BigInteger {
    val clean = hex.removePrefix("0x")
    return if (clean.isEmpty()) BigInteger.ZERO else BigInteger(clean, 16)
  }

  private fun formatJson(raw: String): String {
    return try {
      if (raw.trim().startsWith("{")) {
        JSONObject(raw).toString(2)
      } else if (raw.trim().startsWith("[")) {
        JSONArray(raw).toString(2)
      } else {
        raw
      }
    } catch (_: Exception) {
      raw
    }
  }
}
