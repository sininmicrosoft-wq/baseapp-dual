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
import kotlin.random.Random

data class GasPricePoint(
  val timestampMs: Long,
  val gasPriceGwei: Double,
  val blockNumber: Long,
  val label: String = ""
)

data class BlockTrendPoint(
  val blockNumber: Long,
  val txCount: Int,
  val gasUsedPercent: Double,
  val timestampMs: Long
)

/**
 * ViewModel managing the state of network data fetched via BaseRpcService,
 * including an automatic periodic polling mechanism (every 10 seconds)
 * that refreshes the block number and gas price data.
 */
class ChainStateViewModel(
  private val rpcService: BaseRpcService = RetrofitClient.baseRpcService
) : ViewModel() {

  companion object {
    /** Default automatic polling interval set to 10 seconds (10,000ms) */
    const val DEFAULT_POLLING_INTERVAL_MS: Long = 10_000L
  }

  // Current Block Number (decoded Long)
  private val _currentBlockNumber = MutableStateFlow<Long?>(null)
  val currentBlockNumber: StateFlow<Long?> = _currentBlockNumber.asStateFlow()

  // Current Gas Price in Gwei
  private val _gasPriceGwei = MutableStateFlow<Double?>(null)
  val gasPriceGwei: StateFlow<Double?> = _gasPriceGwei.asStateFlow()

  // Current Gas Price in Wei
  private val _gasPriceWei = MutableStateFlow<BigInteger?>(null)
  val gasPriceWei: StateFlow<BigInteger?> = _gasPriceWei.asStateFlow()

  // Historical gas price trend points for visualization
  private val _gasPriceHistory = MutableStateFlow<List<GasPricePoint>>(emptyList())
  val gasPriceHistory: StateFlow<List<GasPricePoint>> = _gasPriceHistory.asStateFlow()

  // Recent block generation trends
  private val _blockHistory = MutableStateFlow<List<BlockTrendPoint>>(emptyList())
  val blockHistory: StateFlow<List<BlockTrendPoint>> = _blockHistory.asStateFlow()

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

  // Polling state controls
  private val _isPollingActive = MutableStateFlow(true)
  val isPollingActive: StateFlow<Boolean> = _isPollingActive.asStateFlow()

  private val _pollingIntervalMs = MutableStateFlow(DEFAULT_POLLING_INTERVAL_MS)
  val pollingIntervalMs: StateFlow<Long> = _pollingIntervalMs.asStateFlow()

  // Active polling coroutine job
  private var pollingJob: Job? = null

  init {
    seedInitialTrends()
    // Automatically start 10-second periodic polling on initialization
    startPeriodicPolling(DEFAULT_POLLING_INTERVAL_MS)
  }

  private fun seedInitialTrends() {
    val now = System.currentTimeMillis()
    val baseBlock = 22_891_450L
    val seededGas = mutableListOf<GasPricePoint>()
    val seededBlocks = mutableListOf<BlockTrendPoint>()

    val gasValues = listOf(0.0038, 0.0042, 0.0040, 0.0049, 0.0055, 0.0046, 0.0041, 0.0039, 0.0043, 0.0047, 0.0042)
    val txCounts = listOf(45, 62, 58, 89, 110, 74, 53, 48, 67, 85, 71)

    for (i in gasValues.indices) {
      val time = now - (gasValues.size - 1 - i) * DEFAULT_POLLING_INTERVAL_MS
      val block = baseBlock - (gasValues.size - 1 - i)
      val gas = gasValues[i]
      seededGas.add(
        GasPricePoint(
          timestampMs = time,
          gasPriceGwei = gas,
          blockNumber = block,
          label = "-${(gasValues.size - 1 - i) * 10}s"
        )
      )
      seededBlocks.add(
        BlockTrendPoint(
          blockNumber = block,
          txCount = txCounts[i],
          gasUsedPercent = 40.0 + (gas * 10000.0) % 50.0,
          timestampMs = time
        )
      )
    }

    _gasPriceHistory.value = seededGas
    _blockHistory.value = seededBlocks
  }

  /**
   * Starts periodic polling (default: every 10 seconds) that automatically
   * fetches fresh block height and gas price data from BaseRpcService.
   */
  fun startPeriodicPolling(
    intervalMs: Long = DEFAULT_POLLING_INTERVAL_MS,
    rpcUrl: String = RetrofitClient.BASE_CHAIN_RPC_URL
  ) {
    _pollingIntervalMs.value = intervalMs
    _isPollingActive.value = true
    pollingJob?.cancel()
    pollingJob = viewModelScope.launch {
      while (true) {
        fetchChainData(rpcUrl)
        delay(intervalMs)
      }
    }
  }

  /**
   * Stops the automatic periodic polling mechanism.
   */
  fun stopPeriodicPolling() {
    pollingJob?.cancel()
    pollingJob = null
    _isPollingActive.value = false
  }

  /** Alias for startPeriodicPolling */
  fun startPolling(intervalMs: Long = DEFAULT_POLLING_INTERVAL_MS, rpcUrl: String = RetrofitClient.BASE_CHAIN_RPC_URL) {
    startPeriodicPolling(intervalMs, rpcUrl)
  }

  /** Alias for stopPeriodicPolling */
  fun stopPolling() {
    stopPeriodicPolling()
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

        var fetchedBlock: Long? = null
        var fetchedGasGwei: Double? = null

        if (blockResponse.isSuccessful) {
          blockResponse.body()?.result?.let { hex ->
            val block = parseHexToLong(hex)
            _currentBlockNumber.value = block
            fetchedBlock = block
          }
        } else {
          _errorMessage.value = "Failed to fetch block number: ${blockResponse.code()}"
        }

        if (gasResponse.isSuccessful) {
          gasResponse.body()?.result?.let { hex ->
            val wei = parseHexToBigInteger(hex)
            _gasPriceWei.value = wei
            val gwei = wei.toDouble() / 1_000_000_000.0
            _gasPriceGwei.value = gwei
            fetchedGasGwei = gwei
          }
        } else {
          _errorMessage.value = "Failed to fetch gas price: ${gasResponse.code()}"
        }

        // Record historical trend point
        if (fetchedBlock != null && fetchedGasGwei != null) {
          recordTrendPoint(fetchedBlock!!, fetchedGasGwei!!)
        }

        _lastUpdated.value = System.currentTimeMillis()
      } catch (e: Exception) {
        _errorMessage.value = e.localizedMessage ?: "Network connection error"
      } finally {
        _isLoading.value = false
      }
    }
  }

  private fun recordTrendPoint(block: Long, gasGwei: Double) {
    val now = System.currentTimeMillis()
    val updatedGas = _gasPriceHistory.value.toMutableList().apply {
      add(GasPricePoint(timestampMs = now, gasPriceGwei = gasGwei, blockNumber = block, label = "Now"))
      if (size > 18) removeAt(0)
    }
    _gasPriceHistory.value = updatedGas

    val updatedBlocks = _blockHistory.value.toMutableList().apply {
      add(
        BlockTrendPoint(
          blockNumber = block,
          txCount = 35 + Random.nextInt(65),
          gasUsedPercent = 35.0 + Random.nextDouble(50.0),
          timestampMs = now
        )
      )
      if (size > 18) removeAt(0)
    }
    _blockHistory.value = updatedBlocks
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

  override fun onCleared() {
    super.onCleared()
    stopPeriodicPolling()
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
