package com.example.data.rpc

/**
 * Supported Base Chain networks with their respective JSON-RPC endpoints,
 * chain IDs, and block explorers.
 */
enum class BaseNetwork(
  val displayName: String,
  val chainId: Long,
  val rpcUrl: String,
  val explorerUrl: String,
  val isTestnet: Boolean
) {
  MAINNET(
    displayName = "Base Mainnet",
    chainId = 8453L,
    rpcUrl = "https://mainnet.base.org/",
    explorerUrl = "https://basescan.org",
    isTestnet = false
  ),
  SEPOLIA(
    displayName = "Base Sepolia",
    chainId = 84532L,
    rpcUrl = "https://sepolia.base.org/",
    explorerUrl = "https://sepolia.basescan.org",
    isTestnet = true
  );

  /**
   * Returns the alternate network for quick toggling.
   */
  fun toggle(): BaseNetwork = if (this == MAINNET) SEPOLIA else MAINNET
}
