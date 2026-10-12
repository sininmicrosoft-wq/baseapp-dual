package com.example

import com.example.data.rpc.BaseRpcService
import com.example.data.rpc.model.BlockRpcDetails
import com.example.data.rpc.model.JsonRpcRequest
import com.example.data.rpc.model.JsonRpcResponse
import com.example.data.rpc.model.TransactionReceiptRpcDetails
import com.example.data.rpc.model.TransactionRpcDetails
import com.example.ui.viewmodel.ChainStateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ChainStateViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // Fake BaseRpcService for testing state emissions
  private class FakeBaseRpcService : BaseRpcService {
    override suspend fun eth_blockNumber(request: JsonRpcRequest): Response<JsonRpcResponse<String>> {
      return Response.success(JsonRpcResponse(result = "0x15d4872"))
    }

    override suspend fun eth_blockNumber(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<String>> {
      return Response.success(JsonRpcResponse(result = "0x15d4872"))
    }

    override suspend fun getBlockHeight(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<String>> {
      return Response.success(JsonRpcResponse(result = "0x15d4872"))
    }

    override suspend fun eth_gasPrice(request: JsonRpcRequest): Response<JsonRpcResponse<String>> {
      // 0x3b9aca00 = 1,000,000,000 Wei = 1.0 Gwei
      return Response.success(JsonRpcResponse(result = "0x3b9aca00"))
    }

    override suspend fun eth_gasPrice(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<String>> {
      return Response.success(JsonRpcResponse(result = "0x3b9aca00"))
    }

    override suspend fun getGasPrice(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<String>> {
      return Response.success(JsonRpcResponse(result = "0x3b9aca00"))
    }

    override suspend fun eth_getTransactionByHash(request: JsonRpcRequest): Response<JsonRpcResponse<TransactionRpcDetails>> {
      return Response.success(JsonRpcResponse(result = TransactionRpcDetails(hash = "0xabc", blockNumber = "0x15d4872")))
    }

    override suspend fun eth_getTransactionByHash(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<TransactionRpcDetails>> {
      return Response.success(JsonRpcResponse(result = TransactionRpcDetails(hash = "0xabc", blockNumber = "0x15d4872")))
    }

    override suspend fun getTransactionByHash(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<TransactionRpcDetails>> {
      return Response.success(JsonRpcResponse(result = TransactionRpcDetails(hash = "0xabc", blockNumber = "0x15d4872")))
    }

    override suspend fun getTransactionReceipt(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<TransactionReceiptRpcDetails>> =
      Response.success(JsonRpcResponse())

    override suspend fun getBlockByNumber(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<BlockRpcDetails>> =
      Response.success(JsonRpcResponse())

    override suspend fun getBalance(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<String>> =
      Response.success(JsonRpcResponse())

    override suspend fun getChainId(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<String>> =
      Response.success(JsonRpcResponse(result = "0x2105"))

    override suspend fun callRawRpc(url: String, request: JsonRpcRequest): Response<ResponseBody> =
      throw UnsupportedOperationException()

    override suspend fun callGenericRpc(url: String, request: JsonRpcRequest): Response<JsonRpcResponse<Any>> =
      throw UnsupportedOperationException()
  }

  @Test
  fun `verify ChainStateViewModel updates block number and gas price correctly`() = runTest {
    val fakeService = FakeBaseRpcService()
    val viewModel = ChainStateViewModel(rpcService = fakeService)

    // Trigger initial fetch and advance coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // 0x15d4872 = 22890610
    assertEquals(22890610L, viewModel.currentBlockNumber.value)

    // 0x3b9aca00 = 1,000,000,000 Wei = 1.0 Gwei
    assertEquals(1.0, viewModel.gasPriceGwei.value ?: 0.0, 0.0001)

    // Verify loading is false after completion
    assertEquals(false, viewModel.isLoading.value)
  }

  @Test
  fun `verify transaction fetch updates transactionDetails`() = runTest {
    val fakeService = FakeBaseRpcService()
    val viewModel = ChainStateViewModel(rpcService = fakeService)

    viewModel.fetchTransactionDetails("0xabc")
    testDispatcher.scheduler.advanceUntilIdle()

    val details = viewModel.transactionDetails.value
    assertNotNull(details)
    assertEquals("0xabc", details?.hash)
    assertEquals("0x15d4872", details?.blockNumber)
  }
}
