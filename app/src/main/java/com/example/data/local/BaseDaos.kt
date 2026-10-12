package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
  @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
  fun getAllInvoices(): Flow<List<InvoiceEntity>>

  @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
  suspend fun getInvoiceById(id: String): InvoiceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInvoice(invoice: InvoiceEntity)

  @Update
  suspend fun updateInvoice(invoice: InvoiceEntity)

  @Query("DELETE FROM invoices WHERE id = :id")
  suspend fun deleteInvoice(id: String)
}

@Dao
interface B20AssetDao {
  @Query("SELECT * FROM b20_assets ORDER BY createdAt DESC")
  fun getAllAssets(): Flow<List<B20AssetEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAsset(asset: B20AssetEntity)

  @Update
  suspend fun updateAsset(asset: B20AssetEntity)

  @Query("UPDATE b20_assets SET isFrozen = :frozen WHERE symbol = :symbol")
  suspend fun setFrozenStatus(symbol: String, frozen: Boolean)
}

@Dao
interface TrackedWalletDao {
  @Query("SELECT * FROM tracked_wallets ORDER BY addedAt DESC")
  fun getAllWallets(): Flow<List<TrackedWalletEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWallet(wallet: TrackedWalletEntity)

  @Query("DELETE FROM tracked_wallets WHERE address = :address")
  suspend fun deleteWallet(address: String)
}
