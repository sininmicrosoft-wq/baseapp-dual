package com.example.data.model

data class NetworkChain(
  val id: String,
  val name: String,
  val chainId: Long,
  val rpcUrl: String,
  val explorerUrl: String,
  val isTestnet: Boolean = false,
  val isOpStack: Boolean = true,
  val currencySymbol: String = "ETH"
) {
  companion object {
    val BaseMainnet = NetworkChain(
      id = "base_mainnet",
      name = "Base Mainnet",
      chainId = 8453,
      rpcUrl = "https://mainnet.base.org",
      explorerUrl = "https://basescan.org",
      isTestnet = false,
      isOpStack = true
    )

    val BaseSepolia = NetworkChain(
      id = "base_sepolia",
      name = "Base Sepolia",
      chainId = 84532,
      rpcUrl = "https://sepolia.base.org",
      explorerUrl = "https://sepolia.basescan.org",
      isTestnet = true,
      isOpStack = true
    )

    val OpMainnet = NetworkChain(
      id = "op_mainnet",
      name = "OP Mainnet",
      chainId = 10,
      rpcUrl = "https://mainnet.optimism.io",
      explorerUrl = "https://optimistic.etherscan.io",
      isTestnet = false,
      isOpStack = true
    )

    val OpSepolia = NetworkChain(
      id = "op_sepolia",
      name = "OP Sepolia",
      chainId = 11155420,
      rpcUrl = "https://sepolia.optimism.io",
      explorerUrl = "https://sepolia-optimism.etherscan.io",
      isTestnet = true,
      isOpStack = true
    )

    val Vibenet = NetworkChain(
      id = "vibenet",
      name = "Vibenet Sandbox",
      chainId = 845399,
      rpcUrl = "https://vibenet-rpc.base.org",
      explorerUrl = "https://vibenet.base.org",
      isTestnet = true,
      isOpStack = true
    )

    val allChains = listOf(BaseMainnet, BaseSepolia, OpMainnet, OpSepolia, Vibenet)
  }
}

data class TokenAsset(
  val symbol: String,
  val name: String,
  val contractAddress: String,
  val decimals: Int = 18,
  val priceUsd: Double,
  val balance: Double = 0.0,
  val change24h: Double = 0.0
)

enum class SolutionCategory(val title: String, val subtitle: String) {
  DEFI("Integrate DeFi", "Trading, direct lending, collateral borrowing & earn vaults"),
  RWA_B20("Tokenize Assets", "B20 Asset issuance, holder whitelist & distributions"),
  STABLECOINS("Issue Stablecoins", "Fiat-backed token with minting, compliance & reserves"),
  PAYMENTS("Accept Payments", "Full payment lifecycle: Request, Authorize, Capture, Refund"),
  PRIVATE_TX("Private Transactions", "Confidential settlement onchain using Base Ledgers"),
  VIBENET("Test on Vibenet", "Disposable test network, local faucet & rapid prototyping")
}

data class B20Asset(
  val id: String,
  val name: String,
  val symbol: String,
  val assetClass: String, // "US Treasury Bills", "Commercial Real Estate", "Private Credit", "Commodities"
  val totalSupply: Double,
  val unitValuationUsd: Double,
  val annualYieldPercent: Double,
  val isFrozen: Boolean = false,
  val isWhitelistedOnly: Boolean = true,
  val holdersCount: Int = 124,
  val lastDistributionUsd: Double = 5200.0,
  val issuanceTimestamp: Long = System.currentTimeMillis()
)

data class StablecoinConfig(
  val symbol: String,
  val name: String,
  val pegCurrency: String = "USD",
  val circulatingSupply: Double,
  val reserveBalanceUsd: Double,
  val isPaused: Boolean = false,
  val blacklistedAddressesCount: Int = 3,
  val multiSigRequired: String = "3 of 5 Guardians"
)

data class PaymentInvoice(
  val id: String,
  val amount: Double,
  val currency: String, // "USDC" or "ETH"
  val recipientAddress: String,
  val memo: String,
  val status: PaymentStatus,
  val txHash: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val feeUsd: Double = 0.002
)

enum class PaymentStatus {
  PENDING,
  AUTHORIZED,
  CAPTURED,
  REFUNDED
}

data class FlashblockInfo(
  val blockNumber: Long,
  val subIndex: Int,
  val timestampMs: Long,
  val txCount: Int,
  val latencyMs: Long,
  val l1DataFeeGwei: Double,
  val l2ExecutionFeeGwei: Double
)

data class RpcCallRecord(
  val method: String,
  val params: String,
  val response: String,
  val durationMs: Long,
  val isSuccess: Boolean
)
