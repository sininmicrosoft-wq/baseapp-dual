package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.rpc.BaseNetwork
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
import com.example.ui.viewmodel.BlockTrendPoint
import com.example.ui.viewmodel.ChainStateViewModel
import com.example.ui.viewmodel.GasPricePoint
import java.util.Locale

/**
 * Composable dashboard screen using Recharts-style visual design
 * (smooth Bézier curve area fills, Cartesian grid lines, crosshairs, tooltips, and bar charts)
 * to display current gas price and recent block number trends fetched by ChainStateViewModel.
 */
@Composable
fun ChainDashboardScreen(
  viewModel: ChainStateViewModel,
  modifier: Modifier = Modifier
) {
  val currentBlock by viewModel.currentBlockNumber.collectAsState()
  val gasPriceGwei by viewModel.gasPriceGwei.collectAsState()
  val gasHistory by viewModel.gasPriceHistory.collectAsState()
  val blockHistory by viewModel.blockHistory.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val error by viewModel.errorMessage.collectAsState()

  val isLivePolling by viewModel.isPollingActive.collectAsState()
  val pollingIntervalMs by viewModel.pollingIntervalMs.collectAsState()
  val currentNetwork by viewModel.currentNetwork.collectAsState()
  val currentRpcUrl by viewModel.currentRpcUrl.collectAsState()
  val gasLimitInput by viewModel.gasLimitInput.collectAsState()
  val estimatedCostEth by viewModel.estimatedCostEth.collectAsState()
  val estimatedCostUsd by viewModel.estimatedCostUsd.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Header Card with Network Toggling, Live Status & Polling Controls
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Network Switcher Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                .padding(3.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              BaseNetwork.values().forEach { net ->
                val isSelected = net == currentNetwork
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) BaseBlue else Color.Transparent)
                    .clickable { viewModel.setNetwork(net) }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                  Text(
                    text = if (net.isTestnet) "Base Sepolia" else "Base Mainnet",
                    color = if (isSelected) Color.White else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                }
              }
            }

            Box(
              modifier = Modifier
                .background(
                  if (currentNetwork.isTestnet) AccentGreen.copy(alpha = 0.15f) else BaseCyan.copy(alpha = 0.15f),
                  RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = "ID: ${currentNetwork.chainId}",
                color = if (currentNetwork.isTestnet) AccentGreen else BaseCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = currentNetwork.displayName,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = currentRpcUrl,
                color = Color(0xFFA5B4FC),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              IconButton(
                onClick = { viewModel.fetchChainData() },
                modifier = Modifier
                  .size(34.dp)
                  .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
              ) {
                if (isLoading) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BaseCyan, strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BaseCyan, modifier = Modifier.size(18.dp))
                }
              }

              Button(
                onClick = {
                  if (isLivePolling) {
                    viewModel.stopPeriodicPolling()
                  } else {
                    viewModel.startPeriodicPolling(10_000L)
                  }
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isLivePolling) AccentGreen.copy(alpha = 0.2f) else DarkSurfaceElevated,
                  contentColor = if (isLivePolling) AccentGreen else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(
                  imageVector = if (isLivePolling) Icons.Default.Stop else Icons.Default.PlayArrow,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isLivePolling) "10s Polling" else "Poll (10s)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = error ?: "",
              color = OpRed,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // Top Summary Metric KPI Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Gas Metric Card
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1428)),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, BaseBlue.copy(alpha = 0.5f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("CURRENT GAS PRICE", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${String.format(Locale.US, "%.4f", gasPriceGwei ?: 0.0042)} Gwei",
              color = AccentGreen,
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.TrendingDown, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(12.dp))
              Text("Sub-cent (<$0.002)", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
          }
        }

        // Block Height Card
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("LATEST BLOCK HEIGHT", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "#${String.format(Locale.US, "%,d", currentBlock ?: 22891450L)}",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text("Base Chain L2", color = Color(0xFFA5B4FC), fontSize = 10.sp)
          }
        }
      }
    }

    // Transaction Gas Limit Fee Estimator
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Transaction Fee Estimator", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
              Text(
                "Calculates cost based on live RPC gas price (${String.format(Locale.US, "%.4f", gasPriceGwei ?: 0.0042)} Gwei)",
                color = TextSecondary,
                fontSize = 10.sp
              )
            }

            Box(
              modifier = Modifier
                .background(AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = "SUB-CENT",
                color = AccentGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = gasLimitInput,
            onValueChange = { viewModel.setGasLimit(it) },
            label = { Text("Gas Limit (units)", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = BaseBlue,
              unfocusedBorderColor = DarkSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Preset gas limit buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(
              "21k (ETH)" to 21_000L,
              "65k (USDC)" to 65_000L,
              "150k (Swap)" to 150_000L,
              "280k (Batch)" to 280_000L
            ).forEach { (label, preset) ->
              val isSelected = gasLimitInput == preset.toString()
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) BaseBlue else DarkSurfaceElevated)
                  .border(1.dp, if (isSelected) BaseCyan else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                  .clickable { viewModel.setGasLimitPreset(preset) }
                  .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  color = if (isSelected) Color.White else TextSecondary,
                  fontSize = 9.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Result Display
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("ESTIMATED TX COST (ETH)", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text(
                text = "${String.format(Locale.US, "%.9f", estimatedCostEth ?: 0.0)} ETH",
                color = AccentGreen,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text("ESTIMATED USD", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text(
                text = "$${String.format(Locale.US, "%.5f", estimatedCostUsd ?: 0.0)}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // ERC-20 Token Balance Checker (eth_call)
    item {
      Erc20TokenBalanceCard(
        viewModel = viewModel,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // 1. RECHARTS-STYLE GAS PRICE AREA CHART
    item {
      RechartsGasAreaChart(
        dataPoints = gasHistory,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // 2. RECHARTS-STYLE BLOCK ACTIVITY BAR CHART
    item {
      RechartsBlockBarChart(
        blocks = blockHistory,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // Gas & L1 Savings Comparison Callout
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("EIP-4844 Blob Savings Impact", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("BASE L2 EXECUTION", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("$0.0019", color = AccentGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.End) {
              Text("ETHEREUM L1 EQUIVALENT", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("$4.8500 (~2500x higher)", color = OpRed, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

/**
 * Recharts-style Area & Line chart replicating the visual look of:
 * <AreaChart data={data}>
 *   <defs><linearGradient id="gasGrad" .../></defs>
 *   <CartesianGrid strokeDasharray="3 3"/>
 *   <Tooltip/>
 *   <Area type="monotone" stroke="#0052FF" fill="url(#gasGrad)"/>
 * </AreaChart>
 */
@Composable
fun RechartsGasAreaChart(
  dataPoints: List<GasPricePoint>,
  modifier: Modifier = Modifier
) {
  var selectedIndex by remember { mutableStateOf<Int?>(null) }

  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Gas Price Trend (Gwei)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Text("Recharts-style Area Curve", color = TextSecondary, fontSize = 10.sp)
        }

        // Active Tooltip Badge
        selectedIndex?.let { idx ->
          if (idx in dataPoints.indices) {
            val pt = dataPoints[idx]
            Box(
              modifier = Modifier
                .background(BaseBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .border(1.dp, BaseCyan, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "${String.format(Locale.US, "%.4f", pt.gasPriceGwei)} Gwei (#${pt.blockNumber % 10000})",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Canvas Rendering Recharts Line/Area Chart
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(dataPoints) {
              detectTapGestures { offset ->
                if (dataPoints.isNotEmpty()) {
                  val stepX = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                  val idx = (offset.x / stepX).toInt().coerceIn(0, dataPoints.size - 1)
                  selectedIndex = idx
                }
              }
            }
        ) {
          if (dataPoints.size < 2) return@Canvas

          val paddingBottom = 20.dp.toPx()
          val paddingTop = 10.dp.toPx()
          val chartHeight = size.height - paddingBottom - paddingTop
          val chartWidth = size.width

          val minGas = dataPoints.minOf { it.gasPriceGwei }.coerceAtLeast(0.001)
          val maxGas = dataPoints.maxOf { it.gasPriceGwei }.coerceAtLeast(minGas + 0.002)
          val range = (maxGas - minGas).coerceAtLeast(0.0001)

          // 1. Draw Cartesian Grid Lines (dashed horizontal lines like Recharts <CartesianGrid strokeDasharray="3 3"/>)
          val gridSteps = 4
          val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

          for (i in 0..gridSteps) {
            val y = paddingTop + (chartHeight * (i.toFloat() / gridSteps))
            drawLine(
              color = Color(0xFF1E2842),
              start = Offset(0f, y),
              end = Offset(chartWidth, y),
              strokeWidth = 1f,
              pathEffect = dashEffect
            )
          }

          // Calculate point coordinates
          val points = dataPoints.mapIndexed { index, pt ->
            val x = (index.toFloat() / (dataPoints.size - 1)) * chartWidth
            val normalizedY = ((pt.gasPriceGwei - minGas) / range).toFloat()
            val y = (paddingTop + chartHeight) - (normalizedY * chartHeight)
            Offset(x, y)
          }

          // 2. Build Smooth Bézier Path (Monotone Area)
          val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
              val p0 = points[i]
              val p1 = points[i + 1]
              val cx = (p0.x + p1.x) / 2f
              cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
          }

          // 3. Fill Gradient under the Curve (<Area fill="url(#gradient)"/>)
          val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(points.last().x, paddingTop + chartHeight)
            lineTo(points.first().x, paddingTop + chartHeight)
            close()
          }

          drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
              colors = listOf(
                BaseBlue.copy(alpha = 0.40f),
                BaseCyan.copy(alpha = 0.15f),
                Color.Transparent
              ),
              startY = paddingTop,
              endY = paddingTop + chartHeight
            )
          )

          // 4. Draw Stroke Line (<Area stroke="#0052FF" strokeWidth={3}/>)
          drawPath(
            path = strokePath,
            brush = Brush.horizontalGradient(listOf(BaseBlue, BaseCyan)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
          )

          // 5. Draw Interactive Scrub Indicator / Hover Dot (like Recharts Tooltip cursor)
          selectedIndex?.let { idx ->
            if (idx in points.indices) {
              val targetPt = points[idx]

              // Vertical dashed crosshair
              drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(targetPt.x, paddingTop),
                end = Offset(targetPt.x, paddingTop + chartHeight),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
              )

              // Active glowing point dot
              drawCircle(
                color = BaseCyan.copy(alpha = 0.35f),
                radius = 8.dp.toPx(),
                center = targetPt
              )
              drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = targetPt
              )
              drawCircle(
                color = BaseBlue,
                radius = 2.dp.toPx(),
                center = targetPt
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Recharts-style Bar Chart displaying recent block transaction volume and utilization:
 * <BarChart data={blocks}>
 *   <Bar dataKey="txCount" fill="#00E5FF" radius={[6, 6, 0, 0]}/>
 * </BarChart>
 */
@Composable
fun RechartsBlockBarChart(
  blocks: List<BlockTrendPoint>,
  modifier: Modifier = Modifier
) {
  var activeBarIndex by remember { mutableStateOf<Int?>(null) }

  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Recent Block Production & Tx Volume", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Text("Recharts-style Rounded Bar Chart", color = TextSecondary, fontSize = 10.sp)
        }

        activeBarIndex?.let { idx ->
          if (idx in blocks.indices) {
            val b = blocks[idx]
            Text(
              text = "${b.txCount} txs (${String.format(Locale.US, "%.0f", b.gasUsedPercent)}% gas)",
              color = BaseCyan,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(blocks) {
              detectTapGestures { offset ->
                if (blocks.isNotEmpty()) {
                  val slotWidth = size.width / blocks.size
                  val clickedIndex = (offset.x / slotWidth).toInt().coerceIn(0, blocks.size - 1)
                  activeBarIndex = clickedIndex
                }
              }
            }
        ) {
          if (blocks.isEmpty()) return@Canvas

          val maxTx = blocks.maxOf { it.txCount }.coerceAtLeast(100).toFloat()
          val slotWidth = size.width / blocks.size
          val barWidth = slotWidth * 0.65f
          val chartHeight = size.height - 10.dp.toPx()

          // Draw Cartesian Horizontal Reference Line
          drawLine(
            color = Color(0xFF1E2842),
            start = Offset(0f, chartHeight),
            end = Offset(size.width, chartHeight),
            strokeWidth = 1.dp.toPx()
          )

          blocks.forEachIndexed { i, block ->
            val barHeight = (block.txCount / maxTx) * chartHeight
            val left = i * slotWidth + (slotWidth - barWidth) / 2f
            val top = chartHeight - barHeight

            val isSelected = activeBarIndex == i
            val barBrush = if (isSelected) {
              Brush.verticalGradient(listOf(Color(0xFF00F0FF), Color(0xFF0052FF)))
            } else {
              Brush.verticalGradient(listOf(Color(0xFF2E75FF), Color(0xFF0E1E45)))
            }

            drawRoundRect(
              brush = barBrush,
              topLeft = Offset(left, top),
              size = Size(barWidth, barHeight),
              cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
          }
        }
      }
    }
  }
}

/**
 * UI Component displaying ERC-20 token balance fetched via eth_call (balanceOf: 0x70a08231).
 * Features quick token presets (USDC, cbBTC, AERO, VIRTUAL, WETH), custom address inputs,
 * live RPC execution, formatted balance display, and raw return hex inspection.
 */
@Composable
fun Erc20TokenBalanceCard(
  viewModel: ChainStateViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.erc20State.collectAsState()
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val popularTokens = viewModel.popularTokens

  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(BaseBlue.copy(alpha = 0.2f), CircleShape)
              .border(1.dp, BaseCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "20",
              color = BaseCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
          }

          Column {
            Text(
              text = "ERC-20 Token Balance",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Direct eth_call (balanceOf: 0x70a08231)",
              color = TextSecondary,
              fontSize = 10.sp
            )
          }
        }

        Box(
          modifier = Modifier
            .background(BaseBlue.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .border(1.dp, BaseCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "eth_call",
            color = BaseCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Preset Token Selector Chips
      Text(
        text = "POPULAR BASE TOKENS",
        color = TextTertiary,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        popularTokens.forEach { token ->
          val isSelected = state.contractAddress.equals(token.contractAddress, ignoreCase = true)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) BaseBlue else DarkSurfaceElevated)
              .border(
                1.dp,
                if (isSelected) BaseCyan else DarkSurfaceBorder,
                RoundedCornerShape(8.dp)
              )
              .clickable { viewModel.selectErc20Preset(token) }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = token.symbol,
                color = if (isSelected) Color.White else TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "(${token.decimals}d)",
                color = if (isSelected) Color.White.copy(alpha = 0.7f) else TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Input: Wallet Address
      OutlinedTextField(
        value = state.walletAddress,
        onValueChange = { viewModel.updateErc20WalletAddress(it) },
        label = { Text("Wallet Address (0x...)", color = TextSecondary, fontSize = 11.sp) },
        placeholder = { Text("0x...", color = TextTertiary) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedBorderColor = BaseBlue,
          unfocusedBorderColor = DarkSurfaceBorder
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Input: Contract Address
      OutlinedTextField(
        value = state.contractAddress,
        onValueChange = { viewModel.updateErc20ContractAddress(it) },
        label = { Text("Token Contract Address (0x...)", color = TextSecondary, fontSize = 11.sp) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedBorderColor = BaseBlue,
          unfocusedBorderColor = DarkSurfaceBorder
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Decimals and Symbol controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = state.symbol,
          onValueChange = { viewModel.updateErc20Symbol(it) },
          label = { Text("Symbol", color = TextSecondary, fontSize = 11.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = BaseBlue,
            unfocusedBorderColor = DarkSurfaceBorder
          ),
          modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
          value = state.decimals.toString(),
          onValueChange = { it.toIntOrNull()?.let { d -> viewModel.updateErc20Decimals(d) } },
          label = { Text("Decimals", color = TextSecondary, fontSize = 11.sp) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = BaseBlue,
            unfocusedBorderColor = DarkSurfaceBorder
          ),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Query Button
      Button(
        onClick = { viewModel.fetchErc20Balance() },
        enabled = !state.isLoading,
        colors = ButtonDefaults.buttonColors(
          containerColor = BaseBlue,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        if (state.isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = Color.White,
            strokeWidth = 2.dp
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Querying eth_call...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        } else {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Query Balance via eth_call",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Result Display Box
      if (state.formattedBalance != null || state.errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(
              1.dp,
              if (state.errorMessage != null) OpRed.copy(alpha = 0.5f) else BaseBlue.copy(alpha = 0.4f),
              RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
        ) {
          Column {
            if (state.errorMessage != null) {
              Text(
                text = "Query Failed: ${state.errorMessage}",
                color = OpRed,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            } else {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "TOKEN BALANCE",
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = state.formattedBalance ?: "0.00",
                    color = AccentGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                  )
                }

                Box(
                  modifier = Modifier
                    .background(AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = "LIVE ON BASE",
                    color = AccentGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Raw eth_call payload & return hex details
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF090D16), RoundedCornerShape(8.dp))
                  .padding(10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "RAW RETURN HEX (uint256)",
                    color = TextTertiary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                  )

                  state.rawHex?.let { raw ->
                    IconButton(
                      onClick = {
                        clipboardManager.setText(AnnotatedString(raw))
                        Toast.makeText(context, "Hex copied to clipboard", Toast.LENGTH_SHORT).show()
                      },
                      modifier = Modifier.size(20.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Hex",
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                      )
                    }
                  }
                }

                Text(
                  text = state.rawHex ?: "0x0",
                  color = BaseCyan,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "CALLDATA: balanceOf(address)",
                  color = TextTertiary,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = state.callDataPayload ?: "0x70a08231...",
                  color = Color(0xFFA5B4FC),
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }
  }
}

