package com.example.data.rpc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BaseRpcClient {
  private val client = OkHttpClient.Builder()
    .connectTimeout(6, TimeUnit.SECONDS)
    .readTimeout(6, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun callRawRpc(
    rpcUrl: String,
    method: String,
    paramsJson: String = "[]"
  ): Triple<Boolean, String, Long> = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
      val parsedParams = if (paramsJson.trim().startsWith("[")) {
        JSONArray(paramsJson)
      } else {
        JSONArray()
      }

      val requestPayload = JSONObject().apply {
        put("jsonrpc", "2.0")
        put("method", method)
        put("params", parsedParams)
        put("id", 1)
      }.toString()

      val body = requestPayload.toRequestBody(jsonMediaType)
      val request = Request.Builder()
        .url(rpcUrl)
        .post(body)
        .header("User-Agent", "BaseApp-Android/1.0")
        .build()

      val response = client.newCall(request).execute()
      val duration = System.currentTimeMillis() - startTime
      val responseText = response.body?.string() ?: "{}"

      if (response.isSuccessful) {
        val pretty = formatJson(responseText)
        Triple(true, pretty, duration)
      } else {
        Triple(false, "HTTP Error: ${response.code}\n$responseText", duration)
      }
    } catch (e: Exception) {
      val duration = System.currentTimeMillis() - startTime
      Triple(false, "Connection error: ${e.localizedMessage ?: "Unknown"}", duration)
    }
  }

  suspend fun getBlockNumber(rpcUrl: String): Long? = withContext(Dispatchers.IO) {
    try {
      val payload = """{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}"""
      val request = Request.Builder()
        .url(rpcUrl)
        .post(payload.toRequestBody(jsonMediaType))
        .build()

      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val json = JSONObject(response.body?.string() ?: "{}")
        val hex = json.optString("result", "")
        if (hex.startsWith("0x")) {
          return@withContext hex.substring(2).toLong(16)
        }
      }
    } catch (_: Exception) {}
    null
  }

  suspend fun getGasPriceGwei(rpcUrl: String): Double? = withContext(Dispatchers.IO) {
    try {
      val payload = """{"jsonrpc":"2.0","method":"eth_gasPrice","params":[],"id":1}"""
      val request = Request.Builder()
        .url(rpcUrl)
        .post(payload.toRequestBody(jsonMediaType))
        .build()

      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val json = JSONObject(response.body?.string() ?: "{}")
        val hex = json.optString("result", "")
        if (hex.startsWith("0x")) {
          val wei = hex.substring(2).toBigInteger(16)
          return@withContext wei.toDouble() / 1_000_000_000.0
        }
      }
    } catch (_: Exception) {}
    null
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
