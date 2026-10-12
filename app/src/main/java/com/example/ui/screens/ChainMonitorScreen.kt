package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChainSelectorBar
import com.example.ui.components.FlashblockGauge
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.OpRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.BaseAppViewModel
import java.util.Locale

@Composable
fun ChainMonitorScreen(
  viewModel: BaseAppViewModel,
  modifier: Modifier = Modifier
) {
  val selectedChain by viewModel.selectedChain.collectAsState()
  val latestBlock by viewModel.latestBlock.collectAsState()
  val gasPriceGwei by viewModel.gasPriceGwei.collectAsState()
  val rpcLatencyMs by viewModel.rpcLatencyMs.collectAsState()
  val flashblock by viewModel.flashblock.collectAsState()
  val rpcResult by viewModel.lastRpcResult.collectAsState()
  val isRpcLoading by viewModel.isRpcLoading.collectAsState()

  var rpcMethod by remember { mutableStateOf("eth_blockNumber") }
  var rpcParams by remember { mutableStateOf("[]") }
  val context = LocalContext.current

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Chain Selector
      ChainSelectorBar(
        selectedChain = selectedChain,
        onSelectChain = { viewModel.selectChain(it) }
      )
    }

    // Live Telemetry Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Latest Block Card
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("LATEST BLOCK", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              IconButton(
                onClick = { viewModel.triggerRpcRefresh() },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BaseCyan, modifier = Modifier.size(14.dp))
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "#${String.format(Locale.US, "%,d", latestBlock)}",
              color = Color.White,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Chain ID: ${selectedChain.chainId}",
              color = Color(0xFFA5B4FC),
              fontSize = 10.sp
            )
          }
        }

        // Gas Price Card
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("L2 GAS PRICE", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${String.format(Locale.US, "%.4f", gasPriceGwei)} Gwei",
              color = AccentGreen,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Avg tx: <$0.002",
              color = TextSecondary,
              fontSize = 10.sp
            )
          }
        }
      }
    }

    // Flashblocks Streaming Visualizer
    item {
      FlashblockGauge(
        subBlockIndex = flashblock.subIndex,
        latencyMs = flashblock.latencyMs,
        l1DataFeeGwei = flashblock.l1DataFeeGwei,
        l2ExecFeeGwei = flashblock.l2ExecutionFeeGwei
      )
    }

    // Interactive JSON-RPC Playground
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Interactive JSON-RPC Terminal", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          Text("Execute raw JSON-RPC directly against ${selectedChain.name}", color = TextSecondary, fontSize = 11.sp)

          Spacer(modifier = Modifier.height(10.dp))

          // Quick Method Templates
          val presets = listOf(
            "eth_blockNumber" to "[]",
            "eth_chainId" to "[]",
            "eth_gasPrice" to "[]",
            "net_version" to "[]",
            "web3_clientVersion" to "[]",
            "eth_getBalance" to "[\"${viewModel.userSmartWalletAddress}\", \"latest\"]"
          )

          val horizScroll = rememberScrollState()
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(horizScroll),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            presets.forEach { (m, p) ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (rpcMethod == m) BaseBlue.copy(alpha = 0.3f) else DarkSurfaceElevated)
                  .border(1.dp, if (rpcMethod == m) BaseBlue else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                  .clickable {
                    rpcMethod = m
                    rpcParams = p
                  }
                  .padding(horizontal = 8.dp, vertical = 5.dp)
              ) {
                Text(
                  text = m,
                  color = if (rpcMethod == m) BaseCyan else TextSecondary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = rpcMethod,
            onValueChange = { rpcMethod = it },
            label = { Text("Method", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = BaseBlue,
              unfocusedBorderColor = DarkSurfaceBorder
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = rpcParams,
            onValueChange = { rpcParams = it },
            label = { Text("Params JSON Array", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = BaseBlue,
              unfocusedBorderColor = DarkSurfaceBorder
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { viewModel.executeCustomRpc(rpcMethod, rpcParams) },
            enabled = !isRpcLoading,
            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            if (isRpcLoading) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Send RPC Request")
            }
          }

          rpcResult?.let { res ->
            Spacer(modifier = Modifier.height(12.dp))
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg, RoundedCornerShape(10.dp))
                .border(1.dp, if (res.isSuccess) AccentGreen.copy(alpha = 0.4f) else OpRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (res.isSuccess) "HTTP 200 OK" else "ERROR",
                  color = if (res.isSuccess) AccentGreen else OpRed,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${res.durationMs}ms latency",
                  color = TextTertiary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = res.response,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp
              )
            }
          }
        }
      }
    }

    // Chain Architecture Details
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("OP Stack Superchain Specifications", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))

          val specs = listOf(
            "Rollup Type" to "Optimistic Rollup with Fault Proof System",
            "Data Availability" to "Ethereum L1 Blobs (EIP-4844 / 4096 bytes per blob)",
            "Pre-confirmation" to "Flashblocks streaming (<200ms cadence)",
            "EVM Target" to "Cancun / Prague EVM compatible",
            "Superchain Bridge" to "L1StandardBridge + Cross-L2 Message Passing"
          )

          specs.forEach { (label, value) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(label, color = TextSecondary, fontSize = 11.sp)
              Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
