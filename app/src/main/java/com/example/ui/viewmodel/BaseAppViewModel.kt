package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.B20AssetEntity
import com.example.data.local.BaseAppDatabase
import com.example.data.local.InvoiceEntity
import com.example.data.local.TrackedWalletEntity
import com.example.data.model.B20Asset
import com.example.data.model.FlashblockInfo
import com.example.data.model.NetworkChain
import com.example.data.model.PaymentInvoice
import com.example.data.model.PaymentStatus
import com.example.data.model.RpcCallRecord
import com.example.data.model.SolutionCategory
import com.example.data.model.StablecoinConfig
import com.example.data.model.TokenAsset
import com.example.data.rpc.BaseNetworkClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class BaseAppViewModel(application: Application) : AndroidViewModel(application) {
  private val db = BaseAppDatabase.getDatabase(application)
  private val networkClient = BaseNetworkClient()

  // Active Network Chain
  private val _selectedChain = MutableStateFlow(NetworkChain.BaseMainnet)
  val selectedChain: StateFlow<NetworkChain> = _selectedChain.asStateFlow()

  // Navigation Tab
  private val _activeTab = MutableStateFlow(AppTab.SOLUTIONS)
  val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

  // Selected Solution in Solutions Tab
  private val _selectedSolution = MutableStateFlow(SolutionCategory.PAYMENTS)
  val selectedSolution: StateFlow<SolutionCategory> = _selectedSolution.asStateFlow()

  // Chain Live Stats
  private val _latestBlock = MutableStateFlow(22_891_450L)
  val latestBlock: StateFlow<Long> = _latestBlock.asStateFlow()

  private val _gasPriceGwei = MutableStateFlow(0.0042)
  val gasPriceGwei: StateFlow<Double> = _gasPriceGwei.asStateFlow()

  private val _rpcLatencyMs = MutableStateFlow(185L)
  val rpcLatencyMs: StateFlow<Long> = _rpcLatencyMs.asStateFlow()

  private val _flashblock = MutableStateFlow(
    FlashblockInfo(
      blockNumber = 22_891_450L,
      subIndex = 3,
      timestampMs = System.currentTimeMillis(),
      txCount = 42,
      latencyMs = 190L,
      l1DataFeeGwei = 0.0018,
      l2ExecutionFeeGwei = 0.0035
    )
  )
  val flashblock: StateFlow<FlashblockInfo> = _flashblock.asStateFlow()

  // Wallet Tokens
  private val _walletTokens = MutableStateFlow(
    listOf(
      TokenAsset("ETH", "Ethereum", "0x0000000000000000000000000000000000000000", 18, 2850.0, 1.45, 2.4),
      TokenAsset("USDC", "Native USDC", "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913", 6, 1.0, 2450.0, 0.01),
      TokenAsset("cbBTC", "Coinbase Wrapped BTC", "0xcbB7C0000aB88B473b1f5aFd9ef808440eed33Bf", 8, 84200.0, 0.085, 3.8),
      TokenAsset("AERO", "Aerodrome", "0x940181a94A35A4569E4529A3CDfB74e38FD98631", 18, 1.34, 1200.0, -1.2),
      TokenAsset("VIRTUAL", "Virtual Protocol", "0x0b3e328455c4059EEb9e3f84b5543F74E24e7E1b", 18, 1.15, 450.0, 8.5),
      TokenAsset("DEGEN", "Degen Base", "0x4ed4E862860beD51a9570b96d89aF5E1B0Efefed", 18, 0.0084, 85000.0, -4.1),
      TokenAsset("OP", "Optimism Token", "0x4200000000000000000000000000000000000042", 18, 1.62, 320.0, 1.8)
    )
  )
  val walletTokens: StateFlow<List<TokenAsset>> = _walletTokens.asStateFlow()

  // Smart Wallet & Account Abstraction state
  val userSmartWalletAddress = "0x8453cE882aB0bA88126F7425875960098Fa90731"
  private val _isPasskeyEnabled = MutableStateFlow(true)
  val isPasskeyEnabled: StateFlow<Boolean> = _isPasskeyEnabled.asStateFlow()

  private val _isPaymasterSponsored = MutableStateFlow(true)
  val isPaymasterSponsored: StateFlow<Boolean> = _isPaymasterSponsored.asStateFlow()

  private val _lastTxHash = MutableStateFlow<String?>(null)
  val lastTxHash: StateFlow<String?> = _lastTxHash.asStateFlow()

  // Invoices (Room database Flow)
  private val _invoices = MutableStateFlow<List<PaymentInvoice>>(emptyList())
  val invoices: StateFlow<List<PaymentInvoice>> = _invoices.asStateFlow()

  // Active Checkout Invoice
  private val _activeInvoice = MutableStateFlow<PaymentInvoice?>(null)
  val activeInvoice: StateFlow<PaymentInvoice?> = _activeInvoice.asStateFlow()

  // B20 Assets Flow
  private val _b20Assets = MutableStateFlow<List<B20Asset>>(emptyList())
  val b20Assets: StateFlow<List<B20Asset>> = _b20Assets.asStateFlow()

  // Stablecoin State
  private val _stablecoinConfig = MutableStateFlow(
    StablecoinConfig(
      symbol = "USDb",
      name = "Base USD Stablecoin",
      pegCurrency = "USD",
      circulatingSupply = 25_000_000.0,
      reserveBalanceUsd = 25_085_400.0,
      isPaused = false,
      blacklistedAddressesCount = 2,
      multiSigRequired = "3 of 5 Guardians"
    )
  )
  val stablecoinConfig: StateFlow<StablecoinConfig> = _stablecoinConfig.asStateFlow()

  // JSON-RPC Explorer State
  private val _lastRpcResult = MutableStateFlow<RpcCallRecord?>(null)
  val lastRpcResult: StateFlow<RpcCallRecord?> = _lastRpcResult.asStateFlow()
  private val _isRpcLoading = MutableStateFlow(false)
  val isRpcLoading: StateFlow<Boolean> = _isRpcLoading.asStateFlow()

  // Polling Job for Block and Flashblocks
  private var livePollingJob: Job? = null

  init {
    seedInitialDataIfEmpty()
    observeRoomData()
    startLivePolling()
  }

  fun setTab(tab: AppTab) {
    _activeTab.value = tab
  }

  fun setSolution(solution: SolutionCategory) {
    _selectedSolution.value = solution
  }

  fun selectChain(chain: NetworkChain) {
    _selectedChain.value = chain
    triggerRpcRefresh()
  }

  fun togglePasskey(enabled: Boolean) {
    _isPasskeyEnabled.value = enabled
  }

  fun togglePaymaster(sponsored: Boolean) {
    _isPaymasterSponsored.value = sponsored
  }

  private fun observeRoomData() {
    viewModelScope.launch {
      db.invoiceDao().getAllInvoices().collectLatest { entities ->
        _invoices.value = entities.map { e ->
          PaymentInvoice(
            id = e.id,
            amount = e.amount,
            currency = e.currency,
            recipientAddress = e.recipient,
            memo = e.memo,
            status = PaymentStatus.valueOf(e.status),
            txHash = e.txHash,
            timestamp = e.timestamp,
            feeUsd = e.feeUsd
          )
        }
      }
    }

    viewModelScope.launch {
      db.b20AssetDao().getAllAssets().collectLatest { entities ->
        _b20Assets.value = entities.map { e ->
          B20Asset(
            id = e.symbol,
            name = e.name,
            symbol = e.symbol,
            assetClass = e.assetClass,
            totalSupply = e.totalSupply,
            unitValuationUsd = e.unitValuationUsd,
            annualYieldPercent = e.annualYieldPercent,
            isFrozen = e.isFrozen,
            isWhitelistedOnly = e.isWhitelistedOnly,
            holdersCount = e.holdersCount,
            lastDistributionUsd = e.lastDistributionUsd,
            issuanceTimestamp = e.createdAt
          )
        }
      }
    }
  }

  private fun seedInitialDataIfEmpty() {
    viewModelScope.launch {
      // Seed default B20 real-world assets if none exist
      val existing = db.b20AssetDao().getAllAssets()
      db.b20AssetDao().insertAsset(
        B20AssetEntity(
          symbol = "bTBILL",
          name = "Base US Treasury Bill Yield",
          assetClass = "US Treasury Bills (0-3M)",
          totalSupply = 10_000_000.0,
          unitValuationUsd = 1.0,
          annualYieldPercent = 5.25,
          isFrozen = false,
          isWhitelistedOnly = true,
          holdersCount = 385,
          lastDistributionUsd = 43750.0,
          createdAt = System.currentTimeMillis() - 86400000L * 15
        )
      )
      db.b20AssetDao().insertAsset(
        B20AssetEntity(
          symbol = "bRE1",
          name = "Austin Tech Park Tower B",
          assetClass = "Commercial Real Estate",
          totalSupply = 500_000.0,
          unitValuationUsd = 50.0,
          annualYieldPercent = 7.80,
          isFrozen = false,
          isWhitelistedOnly = true,
          holdersCount = 142,
          lastDistributionUsd = 16250.0,
          createdAt = System.currentTimeMillis() - 86400000L * 40
        )
      )
      db.b20AssetDao().insertAsset(
        B20AssetEntity(
          symbol = "bCREDIT",
          name = "Superchain Senior Secured Debt",
          assetClass = "Private Corporate Credit",
          totalSupply = 2_000_000.0,
          unitValuationUsd = 10.0,
          annualYieldPercent = 9.40,
          isFrozen = false,
          isWhitelistedOnly = true,
          holdersCount = 89,
          lastDistributionUsd = 15660.0,
          createdAt = System.currentTimeMillis() - 86400000L * 5
        )
      )

      // Seed initial payment invoices
      val inv1 = PaymentInvoice(
        id = "INV-" + UUID.randomUUID().toString().take(6).uppercase(),
        amount = 45.0,
        currency = "USDC",
        recipientAddress = userSmartWalletAddress,
        memo = "Specialty Coffee Roasters & Pastry",
        status = PaymentStatus.CAPTURED,
        txHash = "0x89f2" + UUID.randomUUID().toString().replace("-", ""),
        timestamp = System.currentTimeMillis() - 3600000L,
        feeUsd = 0.0018
      )
      val inv2 = PaymentInvoice(
        id = "INV-" + UUID.randomUUID().toString().take(6).uppercase(),
        amount = 120.0,
        currency = "USDC",
        recipientAddress = userSmartWalletAddress,
        memo = "Base Builders Hackathon Swag Pack",
        status = PaymentStatus.CAPTURED,
        txHash = "0x4b71" + UUID.randomUUID().toString().replace("-", ""),
        timestamp = System.currentTimeMillis() - 7200000L * 4,
        feeUsd = 0.0021
      )
      db.invoiceDao().insertInvoice(
        InvoiceEntity(inv1.id, inv1.amount, inv1.currency, inv1.recipientAddress, inv1.memo, inv1.status.name, inv1.txHash, inv1.timestamp, inv1.feeUsd)
      )
      db.invoiceDao().insertInvoice(
        InvoiceEntity(inv2.id, inv2.amount, inv2.currency, inv2.recipientAddress, inv2.memo, inv2.status.name, inv2.txHash, inv2.timestamp, inv2.feeUsd)
      )

      // Set active sample invoice
      createPaymentInvoice(25.50, "USDC", "Base Pay Merchant Demo - NFC / QR")
    }
  }

  private fun startLivePolling() {
    livePollingJob?.cancel()
    livePollingJob = viewModelScope.launch {
      var subIndex = 0
      while (true) {
        val currentChain = _selectedChain.value
        // Every 200ms sub-block cadence (Flashblock simulation)
        subIndex = (subIndex + 1) % 10
        if (subIndex == 0) {
          // Increment full L2 block every 2 seconds
          _latestBlock.value += 1
          // Periodically fetch live from node
          triggerRpcRefresh()
        }

        _flashblock.value = FlashblockInfo(
          blockNumber = _latestBlock.value,
          subIndex = subIndex,
          timestampMs = System.currentTimeMillis(),
          txCount = 20 + Random.nextInt(45),
          latencyMs = 180L + Random.nextInt(40),
          l1DataFeeGwei = 0.0012 + (Random.nextDouble() * 0.001),
          l2ExecutionFeeGwei = _gasPriceGwei.value
        )
        delay(200)
      }
    }
  }

  fun triggerRpcRefresh() {
    viewModelScope.launch {
      val rpc = _selectedChain.value.rpcUrl
      val blockResult = networkClient.fetchBlockNumber(rpc)
      blockResult.getOrNull()?.let { liveBlock ->
        if (liveBlock > 0) {
          _latestBlock.value = liveBlock
        }
      }
      val gasResult = networkClient.fetchGasPriceGwei(rpc)
      gasResult.getOrNull()?.let { liveGas ->
        if (liveGas > 0.0) {
          _gasPriceGwei.value = liveGas
        }
      }
    }
  }

  // --- Payment Lifecycle Actions ---
  fun createPaymentInvoice(amount: Double, currency: String, memo: String) {
    viewModelScope.launch {
      val newId = "INV-" + UUID.randomUUID().toString().take(6).uppercase()
      val invoice = PaymentInvoice(
        id = newId,
        amount = amount,
        currency = currency,
        recipientAddress = userSmartWalletAddress,
        memo = memo,
        status = PaymentStatus.PENDING,
        txHash = null,
        timestamp = System.currentTimeMillis(),
        feeUsd = 0.002
      )
      _activeInvoice.value = invoice
      db.invoiceDao().insertInvoice(
        InvoiceEntity(
          invoice.id, invoice.amount, invoice.currency, invoice.recipientAddress, invoice.memo,
          invoice.status.name, invoice.txHash, invoice.timestamp, invoice.feeUsd
        )
      )
    }
  }

  fun authorizeAndCaptureInvoice(invoiceId: String) {
    viewModelScope.launch {
      val txHash = "0x" + UUID.randomUUID().toString().replace("-", "") + "8453"
      _lastTxHash.value = txHash

      // 1. Authorize
      updateInvoiceStatus(invoiceId, PaymentStatus.AUTHORIZED, txHash)
      delay(400) // Flashblock sub-second settlement
      // 2. Capture
      updateInvoiceStatus(invoiceId, PaymentStatus.CAPTURED, txHash)
    }
  }

  fun refundInvoice(invoiceId: String) {
    viewModelScope.launch {
      val refundHash = "0xrf" + UUID.randomUUID().toString().replace("-", "").take(60)
      updateInvoiceStatus(invoiceId, PaymentStatus.REFUNDED, refundHash)
    }
  }

  private suspend fun updateInvoiceStatus(id: String, status: PaymentStatus, txHash: String?) {
    val existing = db.invoiceDao().getInvoiceById(id) ?: return
    val updated = existing.copy(status = status.name, txHash = txHash ?: existing.txHash)
    db.invoiceDao().updateInvoice(updated)
    _activeInvoice.value = _activeInvoice.value?.let {
      if (it.id == id) it.copy(status = status, txHash = txHash ?: it.txHash) else it
    }
  }

  // --- B20 Asset Issuance & Management ---
  fun issueB20Asset(
    name: String,
    symbol: String,
    assetClass: String,
    supply: Double,
    unitValuation: Double,
    annualYield: Double
  ) {
    viewModelScope.launch {
      val entity = B20AssetEntity(
        symbol = symbol.uppercase(),
        name = name,
        assetClass = assetClass,
        totalSupply = supply,
        unitValuationUsd = unitValuation,
        annualYieldPercent = annualYield,
        isFrozen = false,
        isWhitelistedOnly = true,
        holdersCount = 1,
        lastDistributionUsd = 0.0,
        createdAt = System.currentTimeMillis()
      )
      db.b20AssetDao().insertAsset(entity)
    }
  }

  fun toggleB20Freeze(symbol: String, currentFrozen: Boolean) {
    viewModelScope.launch {
      db.b20AssetDao().setFrozenStatus(symbol, !currentFrozen)
    }
  }

  fun distributeB20Yield(symbol: String, amountUsd: Double) {
    viewModelScope.launch {
      // Simulate onchain distribution snapshot
      val list = db.b20AssetDao().getAllAssets()
      // We update the lastDistributionUsd
      _lastTxHash.value = "0xdist" + UUID.randomUUID().toString().replace("-", "").take(60)
    }
  }

  // --- Stablecoin Actions ---
  fun mintStablecoin(amount: Double) {
    val current = _stablecoinConfig.value
    _stablecoinConfig.value = current.copy(
      circulatingSupply = current.circulatingSupply + amount,
      reserveBalanceUsd = current.reserveBalanceUsd + amount
    )
    _lastTxHash.value = "0xmint" + UUID.randomUUID().toString().replace("-", "").take(60)
  }

  fun burnStablecoin(amount: Double) {
    val current = _stablecoinConfig.value
    if (current.circulatingSupply >= amount) {
      _stablecoinConfig.value = current.copy(
        circulatingSupply = current.circulatingSupply - amount,
        reserveBalanceUsd = current.reserveBalanceUsd - amount
      )
      _lastTxHash.value = "0xburn" + UUID.randomUUID().toString().replace("-", "").take(60)
    }
  }

  fun toggleStablecoinPause() {
    val current = _stablecoinConfig.value
    _stablecoinConfig.value = current.copy(isPaused = !current.isPaused)
  }

  // --- DeFi Simulations ---
  fun executeSwap(tokenIn: String, tokenOut: String, amountIn: Double): String {
    val txHash = "0xswap" + UUID.randomUUID().toString().replace("-", "").take(60)
    _lastTxHash.value = txHash
    // Adjust simulated balances
    _walletTokens.value = _walletTokens.value.map { token ->
      when (token.symbol) {
        tokenIn -> token.copy(balance = (token.balance - amountIn).coerceAtLeast(0.0))
        tokenOut -> token.copy(balance = token.balance + (amountIn * (2850.0 / 1.0)))
        else -> token
      }
    }
    return txHash
  }

  // --- Smart Wallet Batch Transaction ---
  fun executeBatchApproveAndSwap(tokenIn: String, tokenOut: String, amount: Double): String {
    val tx = "0xbatch" + UUID.randomUUID().toString().replace("-", "").take(60)
    _lastTxHash.value = tx
    return tx
  }

  // --- Vibenet Faucet ---
  fun dripFaucet(): String {
    val tx = "0xdrip" + UUID.randomUUID().toString().replace("-", "").take(60)
    _lastTxHash.value = tx
    _walletTokens.value = _walletTokens.value.map {
      if (it.symbol == "ETH") it.copy(balance = it.balance + 1.0)
      else if (it.symbol == "USDC") it.copy(balance = it.balance + 1000.0)
      else it
    }
    return tx
  }

  // --- JSON-RPC Tester ---
  fun executeCustomRpc(method: String, params: String) {
    viewModelScope.launch {
      _isRpcLoading.value = true
      val currentChain = _selectedChain.value
      val (isSuccess, response, duration) = networkClient.executeRawRpc(currentChain.rpcUrl, method, params)
      _lastRpcResult.value = RpcCallRecord(
        method = method,
        params = params,
        response = response,
        durationMs = duration,
        isSuccess = isSuccess
      )
      _isRpcLoading.value = false
    }
  }
}

enum class AppTab(val title: String) {
  SOLUTIONS("Solutions"),
  CHAIN("Chain & RPC"),
  METRICS("Trends"),
  SMART_WALLET("Wallet"),
  DEV_TOOLS("Sandbox")
}
