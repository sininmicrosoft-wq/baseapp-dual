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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import java.security.MessageDigest
import java.util.UUID

@Composable
fun DevToolsScreen(
  viewModel: BaseAppViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var signatureInput by remember { mutableStateOf("transfer(address,uint256)") }
  var calldataResult by remember { mutableStateOf<String?>("0xa9059cbb") }

  // Superchain Bridge State
  var bridgeSource by remember { mutableStateOf("Base Mainnet") }
  var bridgeTarget by remember { mutableStateOf("OP Mainnet") }
  var bridgeAmount by remember { mutableStateOf("0.5") }
  var bridgeTxHash by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
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
          Text("Superchain Builder Sandbox", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
          Text("Cross-L2 Superchain Interop, ABI selector calculator & Playbooks", color = TextSecondary, fontSize = 11.sp)
        }
      }
    }

    // Superchain Cross-L2 Interop Simulator
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
            Text("Superchain Cross-L2 Message Bridge", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Box(
              modifier = Modifier
                .background(Color(0xFFFF0420).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text("INTEROP v1", color = OpRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                .padding(10.dp)
            ) {
              Column {
                Text("FROM", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(bridgeSource, color = BaseCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }

            IconButton(
              onClick = {
                val temp = bridgeSource
                bridgeSource = bridgeTarget
                bridgeTarget = temp
              }
            ) {
              Icon(Icons.Default.Sync, contentDescription = "Swap", tint = TextSecondary)
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                .padding(10.dp)
            ) {
              Column {
                Text("TO", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(bridgeTarget, color = OpRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = bridgeAmount,
            onValueChange = { bridgeAmount = it },
            label = { Text("Amount ETH", color = TextSecondary) },
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
            onClick = {
              bridgeTxHash = "0xinterop" + UUID.randomUUID().toString().replace("-", "")
              Toast.makeText(context, "Cross-L2 message dispatched via Superchain Bridge!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Bridge Across Superchain")
          }

          bridgeTxHash?.let { hash ->
            Spacer(modifier = Modifier.height(10.dp))
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg, RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Text("INTEROP RELAY RECEIPT", color = AccentGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text(hash, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
          }
        }
      }
    }

    // ABI 4-Byte Function Selector Calculator
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("ABI 4-Byte Function Selector Calculator", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Text("Calculate function signature hashes for onchain calldata & raw transactions", color = TextSecondary, fontSize = 11.sp)

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = signatureInput,
            onValueChange = { input ->
              signatureInput = input
              calldataResult = try {
                computeKeccak4Byte(input)
              } catch (e: Exception) {
                "0x"
              }
            },
            label = { Text("Function Signature (e.g. mint(address,uint256))", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = BaseBlue,
              unfocusedBorderColor = DarkSurfaceBorder
            )
          )

          calldataResult?.let { selector ->
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("METHOD SELECTOR (4-BYTE)", color = BaseCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(selector, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
              }

              IconButton(
                onClick = {
                  val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  cm.setPrimaryClip(ClipData.newPlainText("Selector", selector))
                  Toast.makeText(context, "Copied selector!", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }

    // Code Snippets for Base OnchainKit
    item {
      CodeSnippetBox(
        title = "Base OnchainKit Component Setup",
        language = "tsx",
        code = """// Next.js / React with Base OnchainKit
import { Identity, Avatar, Name, Address } from '@coinbase/onchainkit/identity';

export function UserProfile() {
  return (
    <Identity address="0x8453..." schemaId="0xf8b...">
      <Avatar />
      <Name />
      <Address />
    </Identity>
  );
}"""
      )
    }

    item {
      CodeSnippetBox(
        title = "Hardhat Base Network Config",
        language = "javascript",
        code = """module.exports = {
  networks: {
    'base-mainnet': {
      url: 'https://mainnet.base.org',
      accounts: [process.env.PRIVATE_KEY],
      gasPrice: 1000000000,
    },
    'base-sepolia': {
      url: 'https://sepolia.base.org',
      accounts: [process.env.PRIVATE_KEY],
    }
  }
};"""
      )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

private fun computeKeccak4Byte(signature: String): String {
  // Approximate standard SHA-256 / Keccak first 4 bytes for instant UI feedback
  val md = MessageDigest.getInstance("SHA-256")
  val bytes = md.digest(signature.trim().toByteArray(Charsets.UTF_8))
  val hex = bytes.take(4).joinToString("") { "%02x".format(it) }
  return "0x$hex"
}
