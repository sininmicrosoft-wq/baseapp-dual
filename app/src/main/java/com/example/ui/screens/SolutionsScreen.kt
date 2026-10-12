package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.B20Asset
import com.example.data.model.PaymentInvoice
import com.example.data.model.PaymentStatus
import com.example.data.model.SolutionCategory
import com.example.ui.components.BaseQrCodeView
import com.example.ui.components.CodeSnippetBox
import com.example.ui.components.FlashblockGauge
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseBlueLight
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun SolutionsScreen(
  viewModel: BaseAppViewModel,
  modifier: Modifier = Modifier
) {
  val selectedSolution by viewModel.selectedSolution.collectAsState()
  val flashblock by viewModel.flashblock.collectAsState()
  val context = LocalContext.current

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    // Top Solutions Chip Scroll
    val scrollState = rememberScrollState()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SolutionCategory.values().forEach { category ->
        val isSelected = category == selectedSolution
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BaseBlue else DarkSurface)
            .border(1.dp, if (isSelected) BaseCyan else DarkSurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { viewModel.setSolution(category) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Text(
            text = category.title,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }

    // Main Content for Selected Solution
    Box(modifier = Modifier.fillMaxSize()) {
      when (selectedSolution) {
        SolutionCategory.PAYMENTS -> AcceptPaymentsSolution(viewModel)
        SolutionCategory.RWA_B20 -> TokenizeAssetsSolution(viewModel)
        SolutionCategory.STABLECOINS -> IssueStablecoinsSolution(viewModel)
        SolutionCategory.DEFI -> IntegrateDefiSolution(viewModel)
        SolutionCategory.PRIVATE_TX -> PrivateTransactionsSolution(viewModel)
        SolutionCategory.VIBENET -> TestOnVibenetSolution(viewModel)
      }
    }
  }
}

// -------------------------------------------------------------
// 1. ACCEPT PAYMENTS (BASE PAY)
// -------------------------------------------------------------
@Composable
private fun AcceptPaymentsSolution(viewModel: BaseAppViewModel) {
  val activeInvoice by viewModel.activeInvoice.collectAsState()
  val invoices by viewModel.invoices.collectAsState()
  var showNewInvoiceDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
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
              Text(
                text = "Base Pay & Commerce",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Full payment lifecycle: Request, Authorize, Capture, Refund",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }

            Button(
              onClick = { showNewInvoiceDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("create_invoice_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("New Invoice", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Active Invoice Checkout Card
    item {
      activeInvoice?.let { inv ->
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1528)),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, BaseBlue),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = inv.id,
                color = Color(0xFFA5B4FC),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )

              StatusChip(status = inv.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "$${String.format(Locale.US, "%.2f", inv.amount)} ${inv.currency}",
              color = Color.White,
              fontSize = 28.sp,
              fontWeight = FontWeight.ExtraBold
            )
            Text(
              text = inv.memo,
              color = TextSecondary,
              fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Procedural Base QR Code
            val qrUri = "ethereum:base:${inv.recipientAddress}?value=${inv.amount}&token=${inv.currency}&id=${inv.id}"
            BaseQrCodeView(
              payload = qrUri,
              sizeDp = 160.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Recipient: ${inv.recipientAddress.take(8)}...${inv.recipientAddress.takeLast(6)}",
              color = TextTertiary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Lifecycle Controls
            when (inv.status) {
              PaymentStatus.PENDING -> {
                Button(
                  onClick = {
                    viewModel.authorizeAndCaptureInvoice(inv.id)
                    Toast.makeText(context, "Payment Authorized & Captured on Base!", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pay_invoice_button")
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Simulate Customer Smart Wallet Pay (Flashblock <200ms)", fontSize = 12.sp)
                }
              }
              PaymentStatus.AUTHORIZED, PaymentStatus.CAPTURED -> {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                    Text(
                      text = "Settled on Base (Fee: $${String.format(Locale.US, "%.4f", inv.feeUsd)})",
                      color = AccentGreen,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  if (inv.txHash != null) {
                    Text(
                      text = "Tx: ${inv.txHash.take(12)}...${inv.txHash.takeLast(8)}",
                      color = TextTertiary,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 10.sp,
                      modifier = Modifier.clickable {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Tx", inv.txHash))
                        Toast.makeText(context, "Copied Tx Hash!", Toast.LENGTH_SHORT).show()
                      }
                    )
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  OutlinedButton(
                    onClick = {
                      viewModel.refundInvoice(inv.id)
                      Toast.makeText(context, "Refund processed onchain!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OpRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text("Refund Customer", fontSize = 11.sp)
                  }
                }
              }
              PaymentStatus.REFUNDED -> {
                Text(
                  text = "Invoice Refunded Successfully",
                  color = OpRed,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }

    // Code Snippet for Commerce Integration
    item {
      CodeSnippetBox(
        title = "Base Pay with Wagmi / Viem",
        language = "typescript",
        code = """// Request & Verify Payment with Base Pay
import { parseUnits } from 'viem';
import { useSendTransaction } from 'wagmi';

const hash = await sendTransaction({
  to: '${activeInvoice?.recipientAddress ?: "0x8453..."}',
  value: parseUnits('0.005', 18),
  data: '0x' // Instant Flashblock receipt
});"""
      )
    }

    // Historical Settlement Ledger
    item {
      Text(
        text = "Settlement Ledger (Room Database)",
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
      )
    }

    items(invoices) { inv ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(DarkSurface)
          .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = inv.memo, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Text(
            text = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(inv.timestamp)) + " · ${inv.id}",
            color = TextTertiary,
            fontSize = 10.sp
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "$${String.format(Locale.US, "%.2f", inv.amount)} ${inv.currency}",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          StatusChip(status = inv.status)
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }

  // Create Invoice Dialog
  if (showNewInvoiceDialog) {
    var amountText by remember { mutableStateOf("15.00") }
    var memoText by remember { mutableStateOf("Client Consulting Invoice") }
    var selectedCurrency by remember { mutableStateOf("USDC") }

    AlertDialog(
      onDismissRequest = { showNewInvoiceDialog = false },
      containerColor = DarkSurfaceElevated,
      title = { Text("Generate Base Pay Invoice", color = TextPrimary, fontSize = 16.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = BaseBlue,
              unfocusedBorderColor = DarkSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = memoText,
            onValueChange = { memoText = it },
            label = { Text("Memo / Description", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = BaseBlue,
              unfocusedBorderColor = DarkSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("USDC", "ETH").forEach { c ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (selectedCurrency == c) BaseBlue else DarkSurface)
                  .clickable { selectedCurrency = c }
                  .padding(horizontal = 14.dp, vertical = 6.dp)
              ) {
                Text(c, color = Color.White, fontSize = 12.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = amountText.toDoubleOrNull() ?: 10.0
            viewModel.createPaymentInvoice(amt, selectedCurrency, memoText)
            showNewInvoiceDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewInvoiceDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun StatusChip(status: PaymentStatus) {
  val (color, text) = when (status) {
    PaymentStatus.PENDING -> Pair(AccentYellow, "PENDING")
    PaymentStatus.AUTHORIZED -> Pair(BaseCyan, "AUTHORIZED")
    PaymentStatus.CAPTURED -> Pair(AccentGreen, "SETTLED")
    PaymentStatus.REFUNDED -> Pair(OpRed, "REFUNDED")
  }

  Box(
    modifier = Modifier
      .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(
      text = text,
      color = color,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}

// -------------------------------------------------------------
// 2. TOKENIZE ASSETS (B20 STANDARD & RWA)
// -------------------------------------------------------------
@Composable
private fun TokenizeAssetsSolution(viewModel: BaseAppViewModel) {
  val b20Assets by viewModel.b20Assets.collectAsState()
  var showIssuanceDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
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
              Text(
                text = "B20 Asset Standard (RWA)",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Issuance, whitelist compliance, yield & distributions",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }

            Button(
              onClick = { showIssuanceDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Issue B20", fontSize = 12.sp)
            }
          }
        }
      }
    }

    items(b20Assets) { asset ->
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (asset.isFrozen) OpRed.copy(alpha = 0.5f) else DarkSurfaceBorder
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(BaseBlue.copy(alpha = 0.2f), CircleShape)
                  .border(1.dp, BaseCyan.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = asset.symbol.take(2),
                  color = BaseCyan,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Column {
                Text(text = asset.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "${asset.symbol} · ${asset.assetClass}", color = TextSecondary, fontSize = 11.sp)
              }
            }

            // Yield Pill
            Box(
              modifier = Modifier
                .background(AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "+${String.format(Locale.US, "%.2f", asset.annualYieldPercent)}% APY",
                color = AccentGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Financial Specs Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("SUPPLY", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("${String.format(Locale.US, "%,.0f", asset.totalSupply)} tokens", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Column {
              Text("NAV / UNIT", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("$${String.format(Locale.US, "%.2f", asset.unitValuationUsd)}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Column {
              Text("HOLDERS", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("${asset.holdersCount} KYC'd", color = Color(0xFFA5B4FC), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Compliance & Governance Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (asset.isFrozen) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = null,
                tint = if (asset.isFrozen) OpRed else TextSecondary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (asset.isFrozen) "Transfers Frozen" else "Transfers Active",
                color = if (asset.isFrozen) OpRed else TextSecondary,
                fontSize = 11.sp
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = {
                  viewModel.toggleB20Freeze(asset.symbol, asset.isFrozen)
                  val state = if (asset.isFrozen) "Unfrozen" else "Frozen"
                  Toast.makeText(context, "${asset.symbol} $state", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
              ) {
                Text(if (asset.isFrozen) "Unfreeze" else "Emergency Freeze", fontSize = 10.sp)
              }

              Button(
                onClick = {
                  viewModel.distributeB20Yield(asset.symbol, 5000.0)
                  Toast.makeText(context, "USDC yield distributed to ${asset.holdersCount} holders!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Distribute Yield", fontSize = 10.sp)
              }
            }
          }
        }
      }
    }

    item {
      CodeSnippetBox(
        title = "B20 Asset Standard Solidity Interface",
        language = "solidity",
        code = """// SPDX-License-Identifier: MIT
interface IB20Asset {
    function assetValuationUsd() external view returns (uint256);
    function isWhitelisted(address holder) external view returns (bool);
    function freezeAsset(bool isFrozen) external;
    function distributeYield(address token, uint256 amount) external;
}"""
      )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }

  if (showIssuanceDialog) {
    var name by remember { mutableStateOf("New York Prime Debt") }
    var symbol by remember { mutableStateOf("bNYPD") }
    var assetClass by remember { mutableStateOf("Private Real Estate Credit") }
    var supply by remember { mutableStateOf("1000000") }
    var nav by remember { mutableStateOf("10.0") }
    var yieldRate by remember { mutableStateOf("8.5") }

    AlertDialog(
      onDismissRequest = { showIssuanceDialog = false },
      containerColor = DarkSurfaceElevated,
      title = { Text("Issue New B20 Asset", color = TextPrimary, fontSize = 16.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Asset Name", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth()
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = symbol,
              onValueChange = { symbol = it },
              label = { Text("Symbol", color = TextSecondary) },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = yieldRate,
              onValueChange = { yieldRate = it },
              label = { Text("Yield %", color = TextSecondary) },
              modifier = Modifier.weight(1f)
            )
          }
          OutlinedTextField(
            value = assetClass,
            onValueChange = { assetClass = it },
            label = { Text("Asset Class", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth()
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = supply,
              onValueChange = { supply = it },
              label = { Text("Supply", color = TextSecondary) },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = nav,
              onValueChange = { nav = it },
              label = { Text("NAV ($)", color = TextSecondary) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.issueB20Asset(
              name = name,
              symbol = symbol,
              assetClass = assetClass,
              supply = supply.toDoubleOrNull() ?: 100000.0,
              unitValuation = nav.toDoubleOrNull() ?: 1.0,
              annualYield = yieldRate.toDoubleOrNull() ?: 5.0
            )
            showIssuanceDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
        ) {
          Text("Issue Onchain")
        }
      },
      dismissButton = {
        TextButton(onClick = { showIssuanceDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

// -------------------------------------------------------------
// 3. ISSUE STABLECOINS
// -------------------------------------------------------------
@Composable
private fun IssueStablecoinsSolution(viewModel: BaseAppViewModel) {
  val config by viewModel.stablecoinConfig.collectAsState()
  var mintAmountText by remember { mutableStateOf("500000") }
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Fiat-Backed Stablecoin Engine",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Minting, Proof-of-Reserves attestation, compliance freeze & reconciliation",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

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
              Text(text = "${config.name} (${config.symbol})", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
              Text(text = "Peg: 1 ${config.symbol} = 1.00 ${config.pegCurrency}", color = BaseCyan, fontSize = 12.sp)
            }

            Box(
              modifier = Modifier
                .background(if (config.isPaused) OpRed.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = if (config.isPaused) "PAUSED" else "ACTIVE",
                color = if (config.isPaused) OpRed else AccentGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Reserve Backing Health
          val ratio = (config.reserveBalanceUsd / config.circulatingSupply) * 100.0
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("CIRCULATING", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("$${String.format(Locale.US, "%,.0f", config.circulatingSupply)}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Column {
              Text("OFFCHAIN RESERVES", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("$${String.format(Locale.US, "%,.0f", config.reserveBalanceUsd)}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Column {
              Text("COLLATERAL RATIO", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("${String.format(Locale.US, "%.1f", ratio)}%", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Mint & Burn Inputs
          Text("Treasury Mint / Burn Operator", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = mintAmountText,
              onValueChange = { mintAmountText = it },
              modifier = Modifier.weight(1f),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = BaseBlue,
                unfocusedBorderColor = DarkSurfaceBorder
              )
            )

            Button(
              onClick = {
                val amt = mintAmountText.toDoubleOrNull() ?: 100000.0
                viewModel.mintStablecoin(amt)
                Toast.makeText(context, "Minted $amt ${config.symbol} upon reserve attestation!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Mint")
            }

            Button(
              onClick = {
                val amt = mintAmountText.toDoubleOrNull() ?: 100000.0
                viewModel.burnStablecoin(amt)
                Toast.makeText(context, "Burned $amt ${config.symbol} for fiat redemption!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = OpRed),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Burn")
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Compliance Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Multi-Sig: ${config.multiSigRequired}",
              color = TextSecondary,
              fontSize = 11.sp
            )

            OutlinedButton(
              onClick = { viewModel.toggleStablecoinPause() },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(if (config.isPaused) "Resume Contract" else "Emergency Pause", fontSize = 11.sp)
            }
          }
        }
      }
    }

    item {
      CodeSnippetBox(
        title = "Base Stablecoin Compliance & Mint Hook",
        language = "solidity",
        code = """// Base Fiat Stablecoin Pattern
contract BaseStablecoin is ERC20, AccessControl {
    bytes32 public constant MINTER_ROLE = keccak256("MINTER_ROLE");
    bytes32 public constant COMPLIANCE_ROLE = keccak256("COMPLIANCE_ROLE");

    function mintWithAttestation(address to, uint256 amount, bytes calldata proof) external onlyRole(MINTER_ROLE) {
        require(verifyProofOfReserves(amount, proof), "Reserves insufficient");
        _mint(to, amount);
    }
}"""
      )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

// -------------------------------------------------------------
// 4. INTEGRATE DEFI
// -------------------------------------------------------------
@Composable
private fun IntegrateDefiSolution(viewModel: BaseAppViewModel) {
  var swapAmountIn by remember { mutableStateOf("0.25") }
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Integrate DeFi on Base",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "DEX Swaps, Direct Lending, Collateral Borrowing & Yield Vaults",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

    // DEX Swap Card
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Instant Swap Router", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(10.dp))

          // From Token
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("YOU PAY", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              OutlinedTextField(
                value = swapAmountIn,
                onValueChange = { swapAmountIn = it },
                modifier = Modifier.width(140.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                )
              )
            }

            Box(
              modifier = Modifier
                .background(BaseBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text("ETH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(20.dp))
          }

          // To Token
          val estOut = ((swapAmountIn.toDoubleOrNull() ?: 0.25) * 2850.0)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("YOU RECEIVE (EST.)", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text(
                text = "${String.format(Locale.US, "%.2f", estOut)} USDC",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Box(
              modifier = Modifier
                .background(Color(0xFF2775CA).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text("USDC", color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Route & Fee Breakdown
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Route: Aerodrome SlipStream (Base)", color = TextSecondary, fontSize = 11.sp)
            Text("Network Fee: <$0.002", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              val hash = viewModel.executeSwap("ETH", "USDC", swapAmountIn.toDoubleOrNull() ?: 0.25)
              Toast.makeText(context, "Swap executed on Base! Tx: ${hash.take(10)}...", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Swap on Base")
          }
        }
      }
    }

    // Direct Lending & Borrowing Card
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Collateralized Borrowing (Moonwell / Aave on Base)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("SUPPLIED AS COLLATERAL", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("1.50 cbBTC ($126,300)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("BORROWED DEBT", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("50,000 USDC", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Health Factor Meter
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("HEALTH FACTOR", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("2.52 (VERY SAFE)", color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Box(
              modifier = Modifier
                .background(AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("Max LTV: 75%", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    item {
      CodeSnippetBox(
        title = "Uniswap v3 Swap Router on Base",
        language = "typescript",
        code = """// Exact Input Swap on Base
const params = {
  tokenIn: '0x4200000000000000000000000000000000000006', // WETH
  tokenOut: '0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913', // USDC
  fee: 500, // 0.05% tier
  recipient: myAddress,
  amountIn: parseEther('0.1'),
  amountOutMinimum: 0n,
  sqrtPriceLimitX96: 0n
};
await routerContract.exactInputSingle(params);"""
      )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

// -------------------------------------------------------------
// 5. PRIVATE TRANSACTIONS (BASE LEDGERS)
// -------------------------------------------------------------
@Composable
private fun PrivateTransactionsSolution(viewModel: BaseAppViewModel) {
  var recipientAddress by remember { mutableStateOf("0x3f9A...8453") }
  var stealthAddressResult by remember { mutableStateOf<String?>(null) }
  var commitmentHash by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Private Transactions & Base Ledgers",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Confidential settlement, ERC-5564 stealth addresses & ZK commitments",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Stealth Address Generator (ERC-5564)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Text(
            text = "Generate a one-time unlinkable receiving address for a recipient without exposing their public identity.",
            color = TextSecondary,
            fontSize = 11.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = recipientAddress,
            onValueChange = { recipientAddress = it },
            label = { Text("Recipient Meta-Address / ENS", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = BaseBlue,
              unfocusedBorderColor = DarkSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              val randomSuffix = UUID.randomUUID().toString().replace("-", "").take(32)
              stealthAddressResult = "0x" + randomSuffix.take(40)
              commitmentHash = "0xzk" + UUID.randomUUID().toString().replace("-", "")
              Toast.makeText(context, "Stealth address & ZK commitment generated!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Derive Confidential Stealth Address")
          }

          stealthAddressResult?.let { addr ->
            Spacer(modifier = Modifier.height(14.dp))
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Text("ONE-TIME STEALTH RECEIVING ADDRESS", color = BaseCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text(
                text = addr,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.height(6.dp))
              Text("ZK COMMITMENT HASH", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text(
                text = commitmentHash ?: "",
                color = Color(0xFFA5B4FC),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    item {
      CodeSnippetBox(
        title = "Base Ledger Confidential Transfer",
        language = "typescript",
        code = """// Private Settlement on Base Ledgers
import { generateStealthAddress, createNullifier } from '@base/ledgers';

const { stealthAddress, ephemeralPublicKey } = generateStealthAddress({
  recipientMetaAddress: 'st:eth:0x...'
});

// Transfer funds to unlinkable address with ZK proof
await ledgerContract.privateTransfer({
  to: stealthAddress,
  ephemeralKey: ephemeralPublicKey,
  nullifier: createNullifier(secret)
});"""
      )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

// -------------------------------------------------------------
// 6. TEST ON VIBENET (SANDBOX)
// -------------------------------------------------------------
@Composable
private fun TestOnVibenetSolution(viewModel: BaseAppViewModel) {
  val context = LocalContext.current
  var isDeploying by remember { mutableStateOf(false) }
  var deployedContractAddress by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Test on Vibenet Sandbox",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Disposable, instant-mining, zero-cost development sandbox for Base",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

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
              Text("Vibenet Node Status", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
              Text("Chain ID: 845399 · Instant Block Time (0s)", color = TextSecondary, fontSize = 11.sp)
            }

            Box(
              modifier = Modifier
                .background(AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text("READY", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Faucet Action
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                viewModel.dripFaucet()
                Toast.makeText(context, "Dripped 1.0 testnet ETH & 1,000 USDC!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Drip Faucet (+1 ETH)")
            }

            Button(
              onClick = {
                isDeploying = true
                val newAddr = "0x" + UUID.randomUUID().toString().replace("-", "").take(40)
                deployedContractAddress = newAddr
                isDeploying = false
                Toast.makeText(context, "Contract deployed to Vibenet: $newAddr", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Deploy Sample Contract")
            }
          }

          deployedContractAddress?.let { addr ->
            Spacer(modifier = Modifier.height(12.dp))
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                .padding(10.dp)
            ) {
              Text("DEPLOYED CONTRACT ADDRESS", color = AccentGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text(addr, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
          }
        }
      }
    }

    item {
      CodeSnippetBox(
        title = "Foundry Deploy Script for Vibenet",
        language = "bash",
        code = """# Run locally on Vibenet
forge create src/MyBaseApp.sol:MyBaseApp \
  --rpc-url https://vibenet-rpc.base.org \
  --private-key 0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80"""
      )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
