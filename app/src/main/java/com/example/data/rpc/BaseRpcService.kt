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
 * Retrofit service interface defining endpoints for querying Base Chain and OP Superchain
 * JSON-RPC nodes for block height, gas prices, transaction details, and account data.
 */
interface BaseRpcService {

  // ==========================================
  // 1. BLOCK HEIGHT & BLOCK DETAILS
  // ==========================================

  /**
   * Fetches latest block height (eth_blockNumber) returning hex string (e.g. "0x15d4872").
   */
  @POST
  suspend fun getBlockHeight(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Default path variant for getBlockHeight.
   */
  @POST("/")
  suspend fun getBlockHeightDefault(
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Fetches block data by block number or tag (eth_getBlockByNumber).
   */
  @POST
  suspend fun getBlockByNumber(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<BlockRpcDetails>>

  /**
   * Fetches block data by block hash (eth_getBlockByHash).
   */
  @POST
  suspend fun getBlockByHash(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<BlockRpcDetails>>

  // ==========================================
  // 2. GAS PRICES & FEE ESTIMATION
  // ==========================================

  /**
   * Fetches current gas price in Wei (eth_gasPrice) returning hex string.
   */
  @POST
  suspend fun getGasPrice(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Default path variant for getGasPrice.
   */
  @POST("/")
  suspend fun getGasPriceDefault(
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Fetches current max priority fee per gas (eth_maxPriorityFeePerGas) for EIP-1559 on Base.
   */
  @POST
  suspend fun getMaxPriorityFeePerGas(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Estimates gas units required to execute a transaction (eth_estimateGas).
   */
  @POST
  suspend fun estimateGas(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  // ==========================================
  // 3. TRANSACTION DETAILS & RECEIPTS
  // ==========================================

  /**
   * Fetches complete transaction object by 32-byte tx hash (eth_getTransactionByHash).
   */
  @POST
  suspend fun getTransactionByHash(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<TransactionRpcDetails>>

  /**
   * Fetches transaction receipt status and execution gas (eth_getTransactionReceipt).
   */
  @POST
  suspend fun getTransactionReceipt(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<TransactionReceiptRpcDetails>>

  /**
   * Fetches account nonce / tx count (eth_getTransactionCount).
   */
  @POST
  suspend fun getTransactionCount(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  // ==========================================
  // 4. ACCOUNT, CHAIN & PROTOCOL STATE
  // ==========================================

  /**
   * Fetches native balance in Wei (eth_getBalance) for an account address on Base.
   */
  @POST
  suspend fun getBalance(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Fetches EIP-155 chain ID (eth_chainId, e.g. 8453 for Base Mainnet, 84532 for Base Sepolia).
   */
  @POST
  suspend fun getChainId(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  // ==========================================
  // 5. GENERIC & RAW DISPATCH
  // ==========================================

  /**
   * Dispatches a JSON-RPC request expecting a String result.
   */
  @POST
  suspend fun callStringRpc(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Dispatches a JSON-RPC request expecting a generic/structured result.
   */
  @POST
  suspend fun callGenericRpc(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<Any>>

  /**
   * Dispatches a JSON-RPC request and returns raw ResponseBody for terminal inspection.
   */
  @POST
  suspend fun callRawRpc(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<ResponseBody>
}
