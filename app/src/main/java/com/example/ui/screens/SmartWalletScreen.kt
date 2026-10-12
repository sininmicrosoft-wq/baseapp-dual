package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseQrCodeView
import com.example.ui.components.CodeSnippetBox
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
fun SmartWalletScreen(
  viewModel: BaseAppViewModel,
  modifier: Modifier = Modifier
) {
  val tokens by viewModel.walletTokens.collectAsState()
  val isPasskey by viewModel.isPasskeyEnabled.collectAsState()
  val isPaymaster by viewModel.isPaymasterSponsored.collectAsState()
  val address = viewModel.userSmartWalletAddress
  val context = LocalContext.current

  var showReceiveDialog by remember { mutableStateOf(false) }
  var showSendDialog by remember { mutableStateOf(false) }
  var showBatchDialog by remember { mutableStateOf(false) }

  val totalUsd = tokens.sumOf { it.balance * it.priceUsd }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Smart Wallet Banner & Balance Card
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1326)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, BaseBlue),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                  .size(28.dp)
                  .background(BaseBlue, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              }
              Text(
                text = "Base Smart Wallet",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Box(
              modifier = Modifier
                .background(AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "ERC-4337",
                color = AccentGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text("TOTAL ONCHAIN PORTFOLIO", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Text(
            text = "$${String.format(Locale.US, "%,.2f", totalUsd)}",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Smart Wallet Address with Quick Copy
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(DarkSurfaceElevated)
              .clickable {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Base Address", address))
                Toast.makeText(context, "Copied Smart Wallet address!", Toast.LENGTH_SHORT).show()
              }
              .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "${address.take(10)}...${address.takeLast(8)}",
              color = Color(0xFFA5B4FC),
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(12.dp))
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Action Buttons: Send, Receive, Batch Tx
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { showSendDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Send", fontSize = 12.sp)
            }

            Button(
              onClick = { showReceiveDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Receive", fontSize = 12.sp)
            }

            Button(
              onClick = { showBatchDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Batch", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Account Abstraction Capabilities Toggles
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Account Abstraction Capabilities", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(10.dp))

          // Passkey Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Passkey Signer (WebAuthn)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Text("Secure hardware biometric signing without seed phrases", color = TextSecondary, fontSize = 11.sp)
            }
            Switch(
              checked = isPasskey,
              onCheckedChange = { viewModel.togglePasskey(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BaseBlue)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Paymaster Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Paymaster Gas Sponsorship", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Text("Developers sponsor 100% of user gas on Base ($0 gas fee)", color = TextSecondary, fontSize = 11.sp)
            }
            Switch(
              checked = isPaymaster,
              onCheckedChange = { viewModel.togglePaymaster(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentGreen)
            )
          }
        }
      }
    }

    // Token Portfolio List
    item {
      Text(
        text = "Base Assets & Tokens",
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
      )
    }

    items(tokens) { token ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(DarkSurface)
          .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(BaseBlue.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = token.symbol.take(2),
              color = BaseCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Column {
            Text(text = token.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "${token.symbol} · $${String.format(Locale.US, "%,.2f", token.priceUsd)}", color = TextSecondary, fontSize = 11.sp)
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${String.format(Locale.US, "%.3f", token.balance)} ${token.symbol}",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "$${String.format(Locale.US, "%,.2f", token.balance * token.priceUsd)}",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

    item {
      CodeSnippetBox(
        title = "Base Smart Wallet & Paymaster Config",
        language = "typescript",
        code = """// OnchainKit + Base Smart Wallet
import { base } from 'viem/chains';
import { createConfig } from 'wagmi';
import { coinbaseWallet } from 'wagmi/connectors';

export const config = createConfig({
  chains: [base],
  connectors: [
    coinbaseWallet({
      appName: 'BaseApp',
      preference: 'smartWalletOnly', // Passkey biometric
    })
  ]
});"""
      )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }

  // Receive Modal
  if (showReceiveDialog) {
    AlertDialog(
      onDismissRequest = { showReceiveDialog = false },
      containerColor = DarkSurfaceElevated,
      title = { Text("Receive Funds on Base", color = TextPrimary, fontSize = 16.sp) },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          BaseQrCodeView(payload = "ethereum:$address", sizeDp = 180.dp)
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = address,
            color = Color(0xFFA5B4FC),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Address", address))
            Toast.makeText(context, "Copied address!", Toast.LENGTH_SHORT).show()
            showReceiveDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
        ) {
          Text("Copy Address")
        }
      },
      dismissButton = {
        TextButton(onClick = { showReceiveDialog = false }) {
          Text("Close", color = TextSecondary)
        }
      }
    )
  }

  // Send Modal
  if (showSendDialog) {
    var toAddress by remember { mutableStateOf("0x89205A3A3b2A69De6Dbf7f01ED13B2108B2c43e7") }
    var sendAmount by remember { mutableStateOf("0.05") }

    AlertDialog(
      onDismissRequest = { showSendDialog = false },
      containerColor = DarkSurfaceElevated,
      title = { Text("Send Native ETH on Base", color = TextPrimary, fontSize = 16.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = toAddress,
            onValueChange = { toAddress = it },
            label = { Text("To Address", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = sendAmount,
            onValueChange = { sendAmount = it },
            label = { Text("Amount ETH", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Network Gas Fee:", color = TextSecondary, fontSize = 11.sp)
            Text(if (isPaymaster) "SPONSORED ($0.00)" else "$0.0018", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            Toast.makeText(context, "Transaction sent via Flashblock stream!", Toast.LENGTH_SHORT).show()
            showSendDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
        ) {
          Text("Confirm Transfer")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSendDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Batch Transaction Modal
  if (showBatchDialog) {
    AlertDialog(
      onDismissRequest = { showBatchDialog = false },
      containerColor = DarkSurfaceElevated,
      title = { Text("Batch Transaction (executeBatch)", color = TextPrimary, fontSize = 16.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "ERC-4337 Smart Wallets bundle multiple operations into one atomic transaction without needing multiple confirmations or signatures.",
            color = TextSecondary,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurface, RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Text("Step 1: Approve 500 USDC to Aerodrome Router", color = Color(0xFFA5B4FC), fontSize = 11.sp)
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurface, RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Text("Step 2: Swap 500 USDC -> cbBTC atomically", color = Color(0xFFA5B4FC), fontSize = 11.sp)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val hash = viewModel.executeBatchApproveAndSwap("USDC", "cbBTC", 500.0)
            Toast.makeText(context, "Atomic batch executed! Tx: ${hash.take(12)}...", Toast.LENGTH_SHORT).show()
            showBatchDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
        ) {
          Text("Execute Batch")
        }
      },
      dismissButton = {
        TextButton(onClick = { showBatchDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}
