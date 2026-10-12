package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.rpc.BaseRpcService
import com.example.data.rpc.RetrofitClient
import com.example.data.rpc.model.JsonRpcRequest
import com.example.data.rpc.model.TransactionRpcDetails
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger

/**
 * ViewModel managing the state of network data fetched via BaseRpcService,
 * including current block number, gas price, transaction details, loading, and error states.
 */
class ChainStateViewModel(
  private val rpcService: BaseRpcService = RetrofitClient.baseRpcService
) : ViewModel() {

  // Current Block Number (decoded Long)
  private val _currentBlockNumber = MutableStateFlow<Long?>(null)
  val currentBlockNumber: StateFlow<Long?> = _currentBlockNumber.asStateFlow()

  // Current Gas Price in Gwei
  private val _gasPriceGwei = MutableStateFlow<Double?>(null)
  val gasPriceGwei: StateFlow<Double?> = _gasPriceGwei.asStateFlow()

  // Current Gas Price in Wei
  private val _gasPriceWei = MutableStateFlow<BigInteger?>(null)
  val gasPriceWei: StateFlow<BigInteger?> = _gasPriceWei.asStateFlow()

  // Last fetched transaction details
  private val _transactionDetails = MutableStateFlow<TransactionRpcDetails?>(null)
  val transactionDetails: StateFlow<TransactionRpcDetails?> = _transactionDetails.asStateFlow()

  // Network loading indicator
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Network error message
  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Timestamp of last successful network fetch
  private val _lastUpdated = MutableStateFlow<Long>(0L)
  val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()

  // Active polling coroutine job
  private var pollingJob: Job? = null

  init {
    // Initial fetch on creation
    fetchChainData()
  }

  /**
   * Fetches both block number and gas price concurrently using BaseRpcService.
   */
  fun fetchChainData(rpcUrl: String = RetrofitClient.BASE_CHAIN_RPC_URL) {
    viewModelScope.launch {
      _isLoading.value = true
      _errorMessage.value = null

      try {
        val blockDeferred = async {
          rpcService.eth_blockNumber(
            url = rpcUrl,
            request = JsonRpcRequest(method = "eth_blockNumber")
          )
        }

        val gasDeferred = async {
          rpcService.eth_gasPrice(
            url = rpcUrl,
            request = JsonRpcRequest(method = "eth_gasPrice")
          )
        }

        val blockResponse = blockDeferred.await()
        val gasResponse = gasDeferred.await()

        if (blockResponse.isSuccessful) {
          blockResponse.body()?.result?.let { hex ->
            _currentBlockNumber.value = parseHexToLong(hex)
          }
        } else {
          _errorMessage.value = "Failed to fetch block number: ${blockResponse.code()}"
        }

        if (gasResponse.isSuccessful) {
          gasResponse.body()?.result?.let { hex ->
            val wei = parseHexToBigInteger(hex)
            _gasPriceWei.value = wei
            _gasPriceGwei.value = wei.toDouble() / 1_000_000_000.0
          }
        } else {
          _errorMessage.value = "Failed to fetch gas price: ${gasResponse.code()}"
        }

        _lastUpdated.value = System.currentTimeMillis()
      } catch (e: Exception) {
        _errorMessage.value = e.localizedMessage ?: "Network connection error"
      } finally {
        _isLoading.value = false
      }
    }
  }

  /**
   * Fetches only the current block number via eth_blockNumber.
   */
  fun fetchBlockNumber(rpcUrl: String = RetrofitClient.BASE_CHAIN_RPC_URL) {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val response = rpcService.eth_blockNumber(
          url = rpcUrl,
          request = JsonRpcRequest(method = "eth_blockNumber")
        )
        if (response.isSuccessful) {
          response.body()?.result?.let { hex ->
            _currentBlockNumber.value = parseHexToLong(hex)
            _lastUpdated.value = System.currentTimeMillis()
          }
        } else {
          _errorMessage.value = "HTTP Error ${response.code()}"
        }
      } catch (e: Exception) {
        _errorMessage.value = e.localizedMessage
      } finally {
        _isLoading.value = false
      }
    }
  }

  /**
   * Fetches only current gas price via eth_gasPrice.
   */
  fun fetchGasPrice(rpcUrl: String = RetrofitClient.BASE_CHAIN_RPC_URL) {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val response = rpcService.eth_gasPrice(
          url = rpcUrl,
          request = JsonRpcRequest(method = "eth_gasPrice")
        )
        if (response.isSuccessful) {
          response.body()?.result?.let { hex ->
            val wei = parseHexToBigInteger(hex)
            _gasPriceWei.value = wei
            _gasPriceGwei.value = wei.toDouble() / 1_000_000_000.0
            _lastUpdated.value = System.currentTimeMillis()
          }
        } else {
          _errorMessage.value = "HTTP Error ${response.code()}"
        }
      } catch (e: Exception) {
        _errorMessage.value = e.localizedMessage
      } finally {
        _isLoading.value = false
      }
    }
  }

  /**
   * Fetches transaction details by transaction hash via eth_getTransactionByHash.
   */
  fun fetchTransactionDetails(txHash: String, rpcUrl: String = RetrofitClient.BASE_CHAIN_RPC_URL) {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val response = rpcService.eth_getTransactionByHash(
          url = rpcUrl,
          request = JsonRpcRequest(method = "eth_getTransactionByHash", params = listOf(txHash))
        )
        if (response.isSuccessful) {
          _transactionDetails.value = response.body()?.result
        } else {
          _errorMessage.value = "Transaction not found: ${response.code()}"
        }
      } catch (e: Exception) {
        _errorMessage.value = e.localizedMessage
      } finally {
        _isLoading.value = false
      }
    }
  }

  /**
   * Starts periodic polling for live block number and gas price.
   */
  fun startPolling(intervalMs: Long = 3000L, rpcUrl: String = RetrofitClient.BASE_CHAIN_RPC_URL) {
    pollingJob?.cancel()
    pollingJob = viewModelScope.launch {
      while (true) {
        fetchChainData(rpcUrl)
        delay(intervalMs)
      }
    }
  }

  /**
   * Stops periodic polling.
   */
  fun stopPolling() {
    pollingJob?.cancel()
    pollingJob = null
  }

  override fun onCleared() {
    super.onCleared()
    stopPolling()
  }

  private fun parseHexToLong(hex: String): Long {
    val clean = hex.removePrefix("0x")
    return if (clean.isEmpty()) 0L else clean.toLong(16)
  }

  private fun parseHexToBigInteger(hex: String): BigInteger {
    val clean = hex.removePrefix("0x")
    return if (clean.isEmpty()) BigInteger.ZERO else BigInteger(clean, 16)
  }
}
