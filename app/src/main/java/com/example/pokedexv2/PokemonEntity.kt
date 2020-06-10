package com.example.pokedexv2

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PokemonEntity {
    @PrimaryKey
    var id: Int = 0
    var name: String = ""
    var height: Int = 0
    var weight: Int = 0
    var type: String = ""
    var stats: String = ""
    var evolutionchain: String = ""
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray? = null
}