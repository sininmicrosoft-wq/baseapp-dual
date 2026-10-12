package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
  @PrimaryKey val id: String,
  val amount: Double,
  val currency: String,
  val recipient: String,
  val memo: String,
  val status: String, // PENDING, AUTHORIZED, CAPTURED, REFUNDED
  val txHash: String?,
  val timestamp: Long,
  val feeUsd: Double
)

@Entity(tableName = "b20_assets")
data class B20AssetEntity(
  @PrimaryKey val symbol: String,
  val name: String,
  val assetClass: String,
  val totalSupply: Double,
  val unitValuationUsd: Double,
  val annualYieldPercent: Double,
  val isFrozen: Boolean,
  val isWhitelistedOnly: Boolean,
  val holdersCount: Int,
  val lastDistributionUsd: Double,
  val createdAt: Long
)

@Entity(tableName = "tracked_wallets")
data class TrackedWalletEntity(
  @PrimaryKey val address: String,
  val label: String,
  val networkId: String,
  val addedAt: Long
)
