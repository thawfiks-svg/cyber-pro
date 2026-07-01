package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.repository.AcademyRepository

class CyberAcademyApplication : Application() {

    // Lazily initialize Database and Repository
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "cyber_academy_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val repository: AcademyRepository by lazy {
        AcademyRepository(database.academyDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Core app startup initialization if any
    }
}
