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
 * supporting dynamic URLs and network-aware calls for Base Mainnet and Base Sepolia.
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

  // ========================================================
  // 5. eth_call (Smart Contract Read & ERC-20 Balances)
  // ========================================================

  /**
   * Invokes eth_call on the default Base Chain endpoint.
   */
  @POST("/")
  suspend fun eth_call(
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Invokes eth_call on a dynamic Base Chain / OP Stack RPC URL.
   */
  @POST
  suspend fun eth_call(
    @Url url: String,
    @Body request: JsonRpcRequest
  ): Response<JsonRpcResponse<String>>

  /**
   * Fetches an ERC-20 token balance for a given wallet address and contract address using eth_call.
   * Encodes standard ERC-20 balanceOf(address) selector: 0x70a08231 + 32-byte padded address.
   */
  suspend fun getErc20Balance(
    url: String,
    contractAddress: String,
    walletAddress: String
  ): Response<JsonRpcResponse<String>> {
    val cleanAddress = walletAddress.trim().removePrefix("0x").padStart(64, '0')
    val callData = "0x70a08231$cleanAddress"
    val callPayload = mapOf(
      "to" to contractAddress.trim(),
      "data" to callData
    )
    val request = JsonRpcRequest(
      method = "eth_call",
      params = listOf(callPayload, "latest")
    )
    return eth_call(url = url, request = request)
  }

  // ========================================================
  // 6. NETWORK-AWARE HELPERS FOR BASE MAINNET & SEPOLIA
  // ========================================================

  /**
   * Queries eth_blockNumber dynamically targeting the specified BaseNetwork.
   */
  suspend fun getBlockNumber(network: BaseNetwork): Response<JsonRpcResponse<String>> =
    eth_blockNumber(url = network.rpcUrl)

  /**
   * Queries eth_gasPrice dynamically targeting the specified BaseNetwork.
   */
  suspend fun getGasPrice(network: BaseNetwork): Response<JsonRpcResponse<String>> =
    eth_gasPrice(url = network.rpcUrl)

  /**
   * Queries eth_getTransactionByHash dynamically targeting the specified BaseNetwork.
   */
  suspend fun getTransactionByHash(
    network: BaseNetwork,
    txHash: String
  ): Response<JsonRpcResponse<TransactionRpcDetails>> =
    eth_getTransactionByHash(
      url = network.rpcUrl,
      request = JsonRpcRequest(method = "eth_getTransactionByHash", params = listOf(txHash))
    )

  /**
   * Queries ERC-20 token balance dynamically targeting the specified BaseNetwork.
   */
  suspend fun getErc20Balance(
    network: BaseNetwork,
    contractAddress: String,
    walletAddress: String
  ): Response<JsonRpcResponse<String>> =
    getErc20Balance(
      url = network.rpcUrl,
      contractAddress = contractAddress,
      walletAddress = walletAddress
    )
}
