package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [InvoiceEntity::class, B20AssetEntity::class, TrackedWalletEntity::class],
  version = 1,
  exportSchema = false
)
abstract class BaseAppDatabase : RoomDatabase() {
  abstract fun invoiceDao(): InvoiceDao
  abstract fun b20AssetDao(): B20AssetDao
  abstract fun trackedWalletDao(): TrackedWalletDao

  companion object {
    @Volatile
    private var INSTANCE: BaseAppDatabase? = null

    fun getDatabase(context: Context): BaseAppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BaseAppDatabase::class.java,
          "base_app_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
