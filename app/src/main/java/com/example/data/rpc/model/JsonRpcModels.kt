package com.example.data.rpc.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonRpcRequest(
  @Json(name = "jsonrpc") val jsonrpc: String = "2.0",
  @Json(name = "method") val method: String,
  @Json(name = "params") val params: List<Any> = emptyList(),
  @Json(name = "id") val id: Long = 1L
)

@JsonClass(generateAdapter = true)
data class JsonRpcError(
  @Json(name = "code") val code: Int,
  @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class JsonRpcResponse<T>(
  @Json(name = "jsonrpc") val jsonrpc: String = "2.0",
  @Json(name = "id") val id: Long = 1L,
  @Json(name = "result") val result: T? = null,
  @Json(name = "error") val error: JsonRpcError? = null
)

/**
 * Detailed representation of an onchain transaction on Base Chain.
 */
@JsonClass(generateAdapter = true)
data class TransactionRpcDetails(
  @Json(name = "hash") val hash: String? = null,
  @Json(name = "blockHash") val blockHash: String? = null,
  @Json(name = "blockNumber") val blockNumber: String? = null,
  @Json(name = "from") val from: String? = null,
  @Json(name = "to") val to: String? = null,
  @Json(name = "value") val value: String? = null,
  @Json(name = "gas") val gas: String? = null,
  @Json(name = "gasPrice") val gasPrice: String? = null,
  @Json(name = "maxFeePerGas") val maxFeePerGas: String? = null,
  @Json(name = "maxPriorityFeePerGas") val maxPriorityFeePerGas: String? = null,
  @Json(name = "input") val input: String? = null,
  @Json(name = "nonce") val nonce: String? = null,
  @Json(name = "transactionIndex") val transactionIndex: String? = null
)

/**
 * Detailed representation of a transaction receipt on Base Chain.
 */
@JsonClass(generateAdapter = true)
data class TransactionReceiptRpcDetails(
  @Json(name = "transactionHash") val transactionHash: String? = null,
  @Json(name = "blockHash") val blockHash: String? = null,
  @Json(name = "blockNumber") val blockNumber: String? = null,
  @Json(name = "from") val from: String? = null,
  @Json(name = "to") val to: String? = null,
  @Json(name = "status") val status: String? = null, // "0x1" for success, "0x0" for failure
  @Json(name = "gasUsed") val gasUsed: String? = null,
  @Json(name = "cumulativeGasUsed") val cumulativeGasUsed: String? = null,
  @Json(name = "effectiveGasPrice") val effectiveGasPrice: String? = null,
  @Json(name = "contractAddress") val contractAddress: String? = null
)

/**
 * Detailed representation of a block on Base Chain.
 */
@JsonClass(generateAdapter = true)
data class BlockRpcDetails(
  @Json(name = "number") val number: String? = null,
  @Json(name = "hash") val hash: String? = null,
  @Json(name = "parentHash") val parentHash: String? = null,
  @Json(name = "timestamp") val timestamp: String? = null,
  @Json(name = "gasLimit") val gasLimit: String? = null,
  @Json(name = "gasUsed") val gasUsed: String? = null,
  @Json(name = "baseFeePerGas") val baseFeePerGas: String? = null
)
