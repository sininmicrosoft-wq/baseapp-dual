package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChainMonitorScreen
import com.example.ui.screens.DevToolsScreen
import com.example.ui.screens.SmartWalletScreen
import com.example.ui.screens.SolutionsScreen
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OpRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.BaseAppViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        val viewModel: BaseAppViewModel = viewModel()
        BaseAppRoot(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun BaseAppRoot(viewModel: BaseAppViewModel) {
  val activeTab by viewModel.activeTab.collectAsState()
  val selectedChain by viewModel.selectedChain.collectAsState()
  val latestBlock by viewModel.latestBlock.collectAsState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = DarkBg,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkSurface)
          .statusBarsPadding()
          .border(
            width = 1.dp,
            color = DarkSurfaceBorder,
            shape = RoundedCornerShape(0.dp)
          )
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Brand Emblem + Title
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(BaseBlue, CircleShape)
                .border(1.5.dp, BaseCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .background(Color.White, CircleShape)
              )
            }

            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "BaseApp",
                  color = TextPrimary,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 0.5.sp
                )
                Box(
                  modifier = Modifier
                    .background(OpRed.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "OP STACK",
                    color = OpRed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Text(
                text = "Financial Outcomes & Superchain Portal",
                color = TextSecondary,
                fontSize = 10.sp
              )
            }
          }

          // Live Block Stream Indicator
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(DarkSurfaceElevated)
              .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
              .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .background(AccentGreen, CircleShape)
            )
            Text(
              text = "#${latestBlock % 100000}",
              color = Color.White,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .border(1.dp, DarkSurfaceBorder),
        containerColor = DarkSurface,
        tonalElevation = 8.dp
      ) {
        NavigationBarItem(
          selected = activeTab == AppTab.SOLUTIONS,
          onClick = { viewModel.setTab(AppTab.SOLUTIONS) },
          icon = {
            Icon(
              imageVector = if (activeTab == AppTab.SOLUTIONS) Icons.Filled.Layers else Icons.Outlined.Layers,
              contentDescription = "Solutions"
            )
          },
          label = { Text("Solutions", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = BaseCyan,
            indicatorColor = BaseBlue,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_solutions")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.CHAIN,
          onClick = { viewModel.setTab(AppTab.CHAIN) },
          icon = {
            Icon(
              imageVector = if (activeTab == AppTab.CHAIN) Icons.Filled.Hub else Icons.Outlined.Hub,
              contentDescription = "Chain & RPC"
            )
          },
          label = { Text("Chain & RPC", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = BaseCyan,
            indicatorColor = BaseBlue,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_chain")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.SMART_WALLET,
          onClick = { viewModel.setTab(AppTab.SMART_WALLET) },
          icon = {
            Icon(
              imageVector = if (activeTab == AppTab.SMART_WALLET) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
              contentDescription = "Smart Wallet"
            )
          },
          label = { Text("Wallet", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = BaseCyan,
            indicatorColor = BaseBlue,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_wallet")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.DEV_TOOLS,
          onClick = { viewModel.setTab(AppTab.DEV_TOOLS) },
          icon = {
            Icon(
              imageVector = if (activeTab == AppTab.DEV_TOOLS) Icons.Filled.Code else Icons.Outlined.Code,
              contentDescription = "Dev Sandbox"
            )
          },
          label = { Text("Sandbox", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = BaseCyan,
            indicatorColor = BaseBlue,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_sandbox")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AnimatedContent(
        targetState = activeTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "TabContent"
      ) { tab ->
        when (tab) {
          AppTab.SOLUTIONS -> SolutionsScreen(viewModel = viewModel)
          AppTab.CHAIN -> ChainMonitorScreen(viewModel = viewModel)
          AppTab.SMART_WALLET -> SmartWalletScreen(viewModel = viewModel)
          AppTab.DEV_TOOLS -> DevToolsScreen(viewModel = viewModel)
        }
      }
    }
  }
}
