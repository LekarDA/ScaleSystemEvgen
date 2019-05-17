package com.dmitriy.android.scalesystem

interface ReadData {
    suspend fun readScannerData(): Int?
}