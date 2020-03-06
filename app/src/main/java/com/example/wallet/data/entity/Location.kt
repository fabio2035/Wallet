package com.example.wallet.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "location")
public data class DLocation(
    @PrimaryKey(autoGenerate = true)
    var id:Long,
    var date: String,
    var lat: String,
    var lng: String,
    var status: String

){
    constructor(): this(0, "","","","")
}