package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.NetworkChain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BaseApp", appName)
  }

  @Test
  fun `verify network chains configuration`() {
    val baseMainnet = NetworkChain.BaseMainnet
    assertEquals(8453L, baseMainnet.chainId)
    assertEquals("https://mainnet.base.org", baseMainnet.rpcUrl)
    assertNotNull(baseMainnet.explorerUrl)
  }
}
