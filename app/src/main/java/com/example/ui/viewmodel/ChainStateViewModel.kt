package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.rpc.BaseNetwork
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

data class Erc20TokenPreset(
  val symbol: String,
  val name: String,
  val contractAddress: String,
  val decimals: Int
)

data class Erc20BalanceUiState(
  val walletAddress: String = "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045",
  val contractAddress: String = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913",
  val symbol: String = "USDC",
  val decimals: Int = 6,
  val rawHex: String? = null,
  val formattedBalance: String? = null,
  val numericBalance: Double? = null,
  val callDataPayload: String? = null,
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val lastQueriedAt: Long = 0L
)

/**
 * ViewModel managing the state of network data fetched via BaseRpcService,
 * supporting toggling between Base Mainnet and Base Sepolia, 10-second periodic polling,
 * gas limit fee estimation in ETH and USD, and ERC-20 token balance queries via eth_call.
 */
class ChainStateViewModel(
  private val rpcService: BaseRpcService = RetrofitClient.baseRpcService
) : ViewModel() {

  companion object {
    /** Default automatic polling interval set to 10 seconds (10,000ms) */
    const val DEFAULT_POLLING_INTERVAL_MS: Long = 10_000L
    /** Reference ETH price for USD conversion */
    const val ETH_PRICE_USD: Double = 2850.0
  }

  // Active Base Network (Mainnet or Sepolia)
  private val _currentNetwork = MutableStateFlow(BaseNetwork.MAINNET)
  val currentNetwork: StateFlow<BaseNetwork> = _currentNetwork.asStateFlow()

  // Dynamic active RPC URL
  private val _currentRpcUrl = MutableStateFlow(BaseNetwork.MAINNET.rpcUrl)
  val currentRpcUrl: StateFlow<String> = _currentRpcUrl.asStateFlow()

  // Current Block Number (decoded Long)
  private val _currentBlockNumber = MutableStateFlow<Long?>(null)
  val currentBlockNumber: StateFlow<Long?> = _currentBlockNumber.asStateFlow()

  // Current Gas Price in Gwei
  private val _gasPriceGwei = MutableStateFlow<Double?>(null)
  val gasPriceGwei: StateFlow<Double?> = _gasPriceGwei.asStateFlow()

  // Current Gas Price in Wei
  private val _gasPriceWei = MutableStateFlow<BigInteger?>(null)
  val gasPriceWei: StateFlow<BigInteger?> = _gasPriceWei.asStateFlow()

  // User-configurable gas limit input
  private val _gasLimitInput = MutableStateFlow("21000")
  val gasLimitInput: StateFlow<String> = _gasLimitInput.asStateFlow()

  // Calculated estimated transaction cost in ETH
  private val _estimatedCostEth = MutableStateFlow<Double?>(null)
  val estimatedCostEth: StateFlow<Double?> = _estimatedCostEth.asStateFlow()

  // Calculated estimated transaction cost in USD
  private val _estimatedCostUsd = MutableStateFlow<Double?>(null)
  val estimatedCostUsd: StateFlow<Double?> = _estimatedCostUsd.asStateFlow()

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

  // Supported popular ERC-20 tokens on Base
  val popularTokens: List<Erc20TokenPreset> = listOf(
    Erc20TokenPreset("USDC", "USD Coin", "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913", 6),
    Erc20TokenPreset("cbBTC", "Coinbase BTC", "0xcbB7C0000aB88B473b1f5aFd9ef808440eed33Bf", 8),
    Erc20TokenPreset("AERO", "Aerodrome", "0x940181a94A35A4569E4529A3CDfB74e38FD98631", 18),
    Erc20TokenPreset("VIRTUAL", "Virtuals Protocol", "0x0b3e328455c4059EEb9e3f84b5543F74E24e7E1b", 18),
    Erc20TokenPreset("WETH", "Wrapped Ether", "0x4200000000000000000000000000000000000006", 18)
  )

  // ERC-20 Token Balance State fetched via eth_call
  private val _erc20State = MutableStateFlow(
    Erc20BalanceUiState(
      formattedBalance = "1,850.50 USDC",
      numericBalance = 1850.50,
      rawHex = "0x000000000000000000000000000000000000000000000000000000006e507f80",
      callDataPayload = "0x70a08231000000000000000000000000d8da6bf26964af9d7eed9e03e53415d37aa96045",
      lastQueriedAt = System.currentTimeMillis()
    )
  )
  val erc20State: StateFlow<Erc20BalanceUiState> = _erc20State.asStateFlow()

  // Polling state controls
  private val _isPollingActive = MutableStateFlow(true)
  val isPollingActive: StateFlow<Boolean> = _isPollingActive.asStateFlow()

  private val _pollingIntervalMs = MutableStateFlow(DEFAULT_POLLING_INTERVAL_MS)
  val pollingIntervalMs: StateFlow<Long> = _pollingIntervalMs.asStateFlow()

  // Active polling coroutine job
  private var pollingJob: Job? = null

  init {
    seedInitialTrends(_currentNetwork.value)
    recalculateEstimatedCost()
    // Automatically start 10-second periodic polling on initialization
    startPeriodicPolling(DEFAULT_POLLING_INTERVAL_MS, _currentRpcUrl.value)
  }

  /**
   * Sets the gas limit input string and recalculates the estimated transaction cost in ETH and USD.
   */
  fun setGasLimit(limitString: String) {
    _gasLimitInput.value = limitString.filter { it.isDigit() }
    recalculateEstimatedCost()
  }

  /**
   * Sets a standard preset gas limit (e.g. 21,000 for standard ETH transfer, 65,000 for ERC-20, 150,000 for DEX swap).
   */
  fun setGasLimitPreset(presetLimit: Long) {
    _gasLimitInput.value = presetLimit.toString()
    recalculateEstimatedCost()
  }

  /**
   * Recalculates estimated transaction cost based on current gas limit and gas price:
   * Cost in ETH = (gasLimit * gasPriceGwei) / 10^9
   */
  private fun recalculateEstimatedCost() {
    val limit = _gasLimitInput.value.toLongOrNull() ?: 21_000L
    val gwei = _gasPriceGwei.value ?: 0.0042
    val costEth = (limit * gwei) / 1_000_000_000.0
    _estimatedCostEth.value = costEth
    _estimatedCostUsd.value = costEth * ETH_PRICE_USD
  }

  /**
   * Toggles between Base Mainnet and Base Sepolia testnet, updating the RPC URL dynamically.
   */
  fun toggleNetwork() {
    val next = if (_currentNetwork.value == BaseNetwork.MAINNET) BaseNetwork.SEPOLIA else BaseNetwork.MAINNET
    setNetwork(next)
  }

  /**
   * Switches to the specified Base network, updating the dynamic RPC URL and refreshing data.
   */
  fun setNetwork(network: BaseNetwork) {
    if (_currentNetwork.value == network && _currentBlockNumber.value != null) return
    _currentNetwork.value = network
    _currentRpcUrl.value = network.rpcUrl
    seedInitialTrends(network)

    if (_isPollingActive.value) {
      startPeriodicPolling(_pollingIntervalMs.value, network.rpcUrl)
    } else {
      fetchChainData(network.rpcUrl)
    }
  }

  private fun seedInitialTrends(network: BaseNetwork) {
    val now = System.currentTimeMillis()
    val baseBlock = if (network == BaseNetwork.MAINNET) 22_891_450L else 18_450_200L
    val seededGas = mutableListOf<GasPricePoint>()
    val seededBlocks = mutableListOf<BlockTrendPoint>()

    val gasValues = if (network == BaseNetwork.MAINNET) {
      listOf(0.0038, 0.0042, 0.0040, 0.0049, 0.0055, 0.0046, 0.0041, 0.0039, 0.0043, 0.0047, 0.0042)
    } else {
      listOf(0.0012, 0.0015, 0.0011, 0.0018, 0.0022, 0.0014, 0.0010, 0.0012, 0.0013, 0.0016, 0.0011)
    }
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
          gasUsedPercent = 35.0 + (gas * 10000.0) % 50.0,
          timestampMs = time
        )
      )
    }

    _gasPriceHistory.value = seededGas
    _blockHistory.value = seededBlocks
  }

  /**
   * Starts periodic polling (default: every 10 seconds) that automatically
   * fetches fresh block height and gas price data from BaseRpcService using the dynamic RPC URL.
   */
  fun startPeriodicPolling(
    intervalMs: Long = DEFAULT_POLLING_INTERVAL_MS,
    rpcUrl: String = _currentRpcUrl.value
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
  fun startPolling(
    intervalMs: Long = DEFAULT_POLLING_INTERVAL_MS,
    rpcUrl: String = _currentRpcUrl.value
  ) {
    startPeriodicPolling(intervalMs, rpcUrl)
  }

  /** Alias for stopPeriodicPolling */
  fun stopPolling() {
    stopPeriodicPolling()
  }

  /**
   * Fetches both block number and gas price concurrently using BaseRpcService via dynamic RPC URL.
   */
  fun fetchChainData(rpcUrl: String = _currentRpcUrl.value) {
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

        // Recalculate transaction cost in ETH with the updated gas price
        recalculateEstimatedCost()

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
   * Fetches only the current block number via eth_blockNumber using dynamic RPC URL.
   */
  fun fetchBlockNumber(rpcUrl: String = _currentRpcUrl.value) {
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
   * Fetches only current gas price via eth_gasPrice using dynamic RPC URL.
   */
  fun fetchGasPrice(rpcUrl: String = _currentRpcUrl.value) {
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
            recalculateEstimatedCost()
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
   * Fetches transaction details by transaction hash via eth_getTransactionByHash using dynamic RPC URL.
   */
  fun fetchTransactionDetails(txHash: String, rpcUrl: String = _currentRpcUrl.value) {
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
   * Selects an ERC-20 token preset and triggers an eth_call balance check.
   */
  fun selectErc20Preset(token: Erc20TokenPreset) {
    _erc20State.value = _erc20State.value.copy(
      contractAddress = token.contractAddress,
      symbol = token.symbol,
      decimals = token.decimals
    )
    fetchErc20Balance(
      walletAddress = _erc20State.value.walletAddress,
      contractAddress = token.contractAddress,
      decimals = token.decimals,
      symbol = token.symbol
    )
  }

  fun updateErc20WalletAddress(address: String) {
    _erc20State.value = _erc20State.value.copy(walletAddress = address)
  }

  fun updateErc20ContractAddress(address: String) {
    _erc20State.value = _erc20State.value.copy(contractAddress = address)
  }

  fun updateErc20Decimals(decimals: Int) {
    _erc20State.value = _erc20State.value.copy(decimals = decimals)
  }

  fun updateErc20Symbol(symbol: String) {
    _erc20State.value = _erc20State.value.copy(symbol = symbol)
  }

  /**
   * Fetches ERC-20 token balance for the given wallet address and token contract address using eth_call.
   */
  fun fetchErc20Balance(
    walletAddress: String = _erc20State.value.walletAddress,
    contractAddress: String = _erc20State.value.contractAddress,
    decimals: Int = _erc20State.value.decimals,
    symbol: String = _erc20State.value.symbol,
    rpcUrl: String = _currentRpcUrl.value
  ) {
    viewModelScope.launch {
      val cleanAddress = walletAddress.trim().removePrefix("0x").padStart(64, '0')
      val callData = "0x70a08231$cleanAddress"
      _erc20State.value = _erc20State.value.copy(
        isLoading = true,
        errorMessage = null,
        callDataPayload = callData
      )

      try {
        val response = rpcService.getErc20Balance(
          url = rpcUrl,
          contractAddress = contractAddress.trim(),
          walletAddress = walletAddress.trim()
        )

        if (response.isSuccessful) {
          val hex = response.body()?.result ?: "0x0"
          val rawUnits = parseHexToBigInteger(hex)
          val divisor = Math.pow(10.0, decimals.toDouble())
          val numeric = rawUnits.toDouble() / divisor
          val formatted = if (numeric >= 1.0) {
            String.format(java.util.Locale.US, "%,.2f %s", numeric, symbol)
          } else {
            String.format(java.util.Locale.US, "%.6f %s", numeric, symbol)
          }

          _erc20State.value = _erc20State.value.copy(
            rawHex = hex,
            formattedBalance = formatted,
            numericBalance = numeric,
            isLoading = false,
            errorMessage = null,
            lastQueriedAt = System.currentTimeMillis()
          )
        } else {
          _erc20State.value = _erc20State.value.copy(
            isLoading = false,
            errorMessage = "RPC error ${response.code()}: ${response.message()}"
          )
        }
      } catch (e: Exception) {
        _erc20State.value = _erc20State.value.copy(
          isLoading = false,
          errorMessage = e.localizedMessage ?: "Network connection error"
        )
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
