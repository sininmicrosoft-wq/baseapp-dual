package com.example

import com.example.data.rpc.BaseNetworkClient
import com.example.data.rpc.BaseRpcService
import com.example.data.rpc.model.BlockRpcDetails
import com.example.data.rpc.model.JsonRpcRequest
import com.example.data.rpc.model.JsonRpcResponse
import com.example.data.rpc.model.TransactionReceiptRpcDetails
import com.example.data.rpc.model.TransactionRpcDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseRpcClientTest {

  @Test
  fun `verify JsonRpcRequest serialization with Moshi`() {
    val client = BaseNetworkClient()
    val adapter = client.moshi.adapter(JsonRpcRequest::class.java)

    val request = JsonRpcRequest(
      method = "eth_blockNumber",
      params = emptyList(),
      id = 42L
    )

    val json = adapter.toJson(request)
    assertTrue(json.contains("\"method\":\"eth_blockNumber\""))
    assertTrue(json.contains("\"jsonrpc\":\"2.0\""))
    assertTrue(json.contains("\"id\":42"))
  }

  @Test
  fun `verify JsonRpcResponse deserialization with Moshi`() {
    val client = BaseNetworkClient()
    val adapter = client.moshi.adapter<JsonRpcResponse<String>>(
      com.squareup.moshi.Types.newParameterizedType(
        JsonRpcResponse::class.java,
        String::class.java
      )
    )

    val rawJson = """
      {
        "jsonrpc": "2.0",
        "id": 1,
        "result": "0x15d4872"
      }
    """.trimIndent()

    val response = adapter.fromJson(rawJson)
    assertNotNull(response)
    assertEquals("2.0", response?.jsonrpc)
    assertEquals("0x15d4872", response?.result)
    assertEquals(1L, response?.id)
  }

  @Test
  fun `verify TransactionRpcDetails deserialization`() {
    val client = BaseNetworkClient()
    val adapter = client.moshi.adapter<JsonRpcResponse<TransactionRpcDetails>>(
      com.squareup.moshi.Types.newParameterizedType(
        JsonRpcResponse::class.java,
        TransactionRpcDetails::class.java
      )
    )

    val rawJson = """
      {
        "jsonrpc": "2.0",
        "id": 1,
        "result": {
          "hash": "0x3f5c7829a4a7...",
          "from": "0x8453cE882aB0bA88126F7425875960098Fa90731",
          "to": "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913",
          "value": "0x0",
          "gas": "0x5208",
          "gasPrice": "0x3b9aca00",
          "blockNumber": "0x15d4872"
        }
      }
    """.trimIndent()

    val response = adapter.fromJson(rawJson)
    assertNotNull(response)
    val tx = response?.result
    assertNotNull(tx)
    assertEquals("0x8453cE882aB0bA88126F7425875960098Fa90731", tx?.from)
    assertEquals("0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913", tx?.to)
    assertEquals("0x15d4872", tx?.blockNumber)
  }

  @Test
  fun `verify BlockRpcDetails deserialization`() {
    val client = BaseNetworkClient()
    val adapter = client.moshi.adapter<JsonRpcResponse<BlockRpcDetails>>(
      com.squareup.moshi.Types.newParameterizedType(
        JsonRpcResponse::class.java,
        BlockRpcDetails::class.java
      )
    )

    val rawJson = """
      {
        "jsonrpc": "2.0",
        "id": 1,
        "result": {
          "number": "0x15d4872",
          "hash": "0x98127391...",
          "baseFeePerGas": "0x3b9a",
          "gasUsed": "0x12000"
        }
      }
    """.trimIndent()

    val response = adapter.fromJson(rawJson)
    assertNotNull(response)
    assertEquals("0x15d4872", response?.result?.number)
    assertEquals("0x3b9a", response?.result?.baseFeePerGas)
  }

  @Test
  fun `verify BaseRpcService method reflection on client`() {
    val client = BaseNetworkClient()
    assertNotNull(client.rpcService)
    assertNotNull(client.retrofit)
    assertNotNull(client.okHttpClient)

    // Check that BaseRpcService has expected endpoints declared
    val serviceClass = BaseRpcService::class.java
    val methodNames = serviceClass.methods.map { it.name }
    assertTrue(methodNames.contains("eth_blockNumber"))
    assertTrue(methodNames.contains("eth_gasPrice"))
    assertTrue(methodNames.contains("eth_getTransactionByHash"))
    assertTrue(methodNames.contains("getBlockHeight"))
    assertTrue(methodNames.contains("getGasPrice"))
    assertTrue(methodNames.contains("getTransactionByHash"))
    assertTrue(methodNames.contains("getTransactionReceipt"))
  }

  @Test
  fun `verify RetrofitClient configuration file initialization`() {
    val service = com.example.data.rpc.RetrofitClient.baseRpcService
    assertNotNull(service)

    val retrofit = com.example.data.rpc.RetrofitClient.retrofit
    assertNotNull(retrofit)
    assertEquals("https://mainnet.base.org/", retrofit.baseUrl().toString())

    val okHttpClient = com.example.data.rpc.RetrofitClient.okHttpClient
    assertNotNull(okHttpClient)
    assertEquals(15000L, okHttpClient.connectTimeoutMillis.toLong())
    assertTrue(okHttpClient.interceptors.isNotEmpty())

    assertEquals("https://mainnet.base.org/", com.example.data.rpc.RetrofitClient.BASE_CHAIN_RPC_URL)
  }
}
