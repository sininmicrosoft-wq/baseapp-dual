package com.example.data.rpc

import com.example.data.rpc.model.BlockRpcDetails
import com.example.data.rpc.model.JsonRpcRequest
import com.example.data.rpc.model.JsonRpcResponse
import com.example.data.rpc.model.TransactionReceiptRpcDetails
import com.example.data.rpc.model.TransactionRpcDetails
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit service interface defining JSON-RPC methods for Base Chain endpoints,
 * specifically providing eth_blockNumber, eth_gasPrice, and eth_getTransactionByHash.
 */
interface BaseRpcService {

  // ========================================================
  // 1. eth_blockNumber (Current Block Height)
  // ========================================================

  /**
   * Invokes eth_blockNumber on the default Base Chain endpoint.
   */
  @POST("/")
  suspend fun eth_blockNumber(
    @Body request: JsonRpcRequest = JsonRpcRequest(method = "eth_blockNumber")
  ): Response<JsonRpcResponse<String>>

  /**
   * Invokes eth_blockNumber on a specific dynamic Base Chain / OP Stack RPC URL.
   */
  @POST
  suspend fun eth_blockNumber(
    @Url url: String,
    @Body request: JsonRpcRequest = JsonRpcRequest(method = "eth_blockNumber")
  ): Response<JsonRpcResponse<String>>

  /**
   * Alias: getBlockHeight with dynamic RPC URL.
   */
  @POST
  suspend fun getBlockHeight(
    @Url url: String,
    @Body request: JsonRpcRequest = JsonRpcRequest(method = "eth_blockNumber")
  ): Response<JsonRpcResponse<String>>

  // ========================================================
  // 2. eth_gasPrice (Current L2 Gas Price)
  // ========================================================

  /**
   * Invokes eth_gasPrice on the default Base Chain endpoint.
   */
  @POST("/")
  suspend fun eth_gasPrice(
    @Body request: JsonRpcRequest = JsonRpcRequest(method = "eth_gasPrice")
  ): Response<JsonRpcResponse<String>>

  /**
   * Invokes eth_gasPrice on a specific dynamic Base Chain / OP Stack RPC URL.
   */
  @POST
  suspend fun eth_gasPrice(
    @Url url: String,
    @Body request: JsonRpcRequest = JsonRpcRequest(method = "eth_gasPrice")
  ): Response<JsonRpcResponse<String>>

  /**
   * Alias: getGasPrice with dynamic RPC URL.
   */
  @POST
  suspend fun getGasPrice(
    @Url url: String,
    @Body request: JsonRpcRequest = JsonRpcRequest(method = "eth_gasPrice")
  ): Response<JsonRpcResponse<String>>

  // ========================================================
  // 3. eth_getTransactionByHash (Transaction Details)
  // ========================================================

  /**
   * Invokes eth_getTransactionByHash on the default Base Chain endpoint.
   */
  @POST("/")
  suspend fun eth_getTransactionByHash(
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<TransactionRpcDetails>>

  /**
   * Invokes eth_getTransactionByHash on a specific dynamic Base Chain RPC URL.
   */
  @POST
  suspend fun eth_getTransactionByHash(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<TransactionRpcDetails>>

  /**
   * Alias: getTransactionByHash with dynamic RPC URL.
   */
  @POST
  suspend fun getTransactionByHash(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<TransactionRpcDetails>>

  // ========================================================
  // 4. ADDITIONAL BASE CHAIN RPC ENDPOINTS
  // ========================================================

  /**
   * Invokes eth_getTransactionReceipt to inspect transaction status and gas used.
   */
  @POST
  suspend fun getTransactionReceipt(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<TransactionReceiptRpcDetails>>

  /**
   * Invokes eth_getBlockByNumber to fetch block metadata and transactions.
   */
  @POST
  suspend fun getBlockByNumber(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<BlockRpcDetails>>

  /**
   * Invokes eth_getBalance to query native ETH balance.
   */
  @POST
  suspend fun getBalance(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Invokes eth_chainId to query chain ID.
   */
  @POST
  suspend fun getChainId(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Generic JSON-RPC dispatch with raw ResponseBody for terminal inspection.
   */
  @POST
  suspend fun callRawRpc(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<ResponseBody>

  /**
   * Generic JSON-RPC dispatch with parsed JsonRpcResponse<Any>.
   */
  @POST
  suspend fun callGenericRpc(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<Any>>
}
