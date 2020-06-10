package com.example.pokedexv2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pokedexv2.PokemonEntity

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun SavePokemon(pokemon: PokemonEntity)

    @Query("select * from PokemonEntity")
    fun readPokemon() : List<PokemonEntity>

    @Query("select * from PokemonEntity where name LIKE :nameStr")
    fun readPokemonFromName(nameStr : String) : List<PokemonEntity>
}