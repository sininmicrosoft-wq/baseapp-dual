package com.example.ui.viewmodel

import com.example.data.rpc.BaseNetwork
import com.example.data.rpc.RetrofitClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChainStateViewModelTest {

  @Test
  fun `verify retrofit client initialization`() {
    assertNotNull(RetrofitClient.retrofit)
    assertNotNull(RetrofitClient.okHttpClient)
    assertNotNull(RetrofitClient.baseRpcService)
    assertEquals("https://mainnet.base.org/", RetrofitClient.BASE_CHAIN_RPC_URL)
    assertEquals("https://sepolia.base.org/", RetrofitClient.BASE_SEPOLIA_RPC_URL)
  }

  @Test
  fun `verify initial network state is Base Mainnet`() {
    val viewModel = ChainStateViewModel()
    try {
      assertEquals(BaseNetwork.MAINNET, viewModel.currentNetwork.value)
      assertEquals("https://mainnet.base.org/", viewModel.currentRpcUrl.value)
      assertEquals(10_000L, viewModel.pollingIntervalMs.value)
      assertTrue(viewModel.isPollingActive.value)
    } finally {
      viewModel.stopPeriodicPolling()
    }
  }

  @Test
  fun `verify network toggle between Base Mainnet and Base Sepolia`() {
    val viewModel = ChainStateViewModel()
    try {
      assertEquals(BaseNetwork.MAINNET, viewModel.currentNetwork.value)

      // Toggle to Sepolia
      viewModel.toggleNetwork()
      assertEquals(BaseNetwork.SEPOLIA, viewModel.currentNetwork.value)
      assertEquals(84532L, viewModel.currentNetwork.value.chainId)
      assertEquals("https://sepolia.base.org/", viewModel.currentRpcUrl.value)

      // Toggle back to Mainnet
      viewModel.toggleNetwork()
      assertEquals(BaseNetwork.MAINNET, viewModel.currentNetwork.value)
      assertEquals(8453L, viewModel.currentNetwork.value.chainId)
      assertEquals("https://mainnet.base.org/", viewModel.currentRpcUrl.value)
    } finally {
      viewModel.stopPeriodicPolling()
    }
  }

  @Test
  fun `verify gas limit fee estimation calculation`() {
    val viewModel = ChainStateViewModel()
    try {
      // Default is 21,000 gas limit
      assertEquals("21000", viewModel.gasLimitInput.value)
      val defaultEthCost = viewModel.estimatedCostEth.value
      assertNotNull(defaultEthCost)
      assertTrue(defaultEthCost!! > 0.0)

      // Set custom gas limit for DEX swap (150,000)
      viewModel.setGasLimit("150000")
      assertEquals("150000", viewModel.gasLimitInput.value)
      val swapEthCost = viewModel.estimatedCostEth.value
      assertNotNull(swapEthCost)
      assertTrue(swapEthCost!! > defaultEthCost)

      // Set preset gas limit
      viewModel.setGasLimitPreset(65_000L)
      assertEquals("65000", viewModel.gasLimitInput.value)
      val usdcTransferCost = viewModel.estimatedCostEth.value
      assertNotNull(usdcTransferCost)
      assertTrue(usdcTransferCost!! > defaultEthCost)
      assertTrue(usdcTransferCost < swapEthCost)
    } finally {
      viewModel.stopPeriodicPolling()
    }
  }

  @Test
  fun `verify periodic polling controls`() {
    val viewModel = ChainStateViewModel()
    try {
      assertTrue(viewModel.isPollingActive.value)

      // Stop polling
      viewModel.stopPeriodicPolling()
      assertFalse(viewModel.isPollingActive.value)

      // Start polling with custom interval
      viewModel.startPeriodicPolling(5_000L)
      assertTrue(viewModel.isPollingActive.value)
      assertEquals(5_000L, viewModel.pollingIntervalMs.value)
    } finally {
      viewModel.stopPeriodicPolling()
    }
  }

  @Test
  fun `verify gas price and block trend history seeded`() {
    val viewModel = ChainStateViewModel()
    try {
      val gasHistory = viewModel.gasPriceHistory.value
      val blockHistory = viewModel.blockHistory.value

      assertTrue("Gas price history should not be empty", gasHistory.isNotEmpty())
      assertTrue("Block history should not be empty", blockHistory.isNotEmpty())
      assertEquals(gasHistory.size, blockHistory.size)
    } finally {
      viewModel.stopPeriodicPolling()
    }
  }
}
