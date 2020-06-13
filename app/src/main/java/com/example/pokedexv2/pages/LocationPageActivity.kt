package com.example.pokedexv2.pages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokedexv2.*
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_location_page.*
import okhttp3.*
import java.io.IOException

class LocationPageActivity : AppCompatActivity() {
    var spUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_page)
        val pokeUrl = intent.getStringExtra(CustomViewHolder.PokemonUrl)
        val navbarTitle = intent.getStringExtra(CustomViewHolder.PokemonName)
        supportActionBar?.title = navbarTitle
        fetchPokemons(pokeUrl)

        PokemonLocList.layoutManager = LinearLayoutManager(this)

    }

    fun fetchPokemons(pokemonUrl: String) {
        println("Connecting")

        val url = pokemonUrl

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body =  response.body?.string()


                val gson = GsonBuilder().create()

                val pokemons = gson.fromJson(body, PokemonEncounter::class.java)
                val pokemonsList =
                    PokemonList(
                        mutableListOf(
                            Result(
                                "name",
                                "url",
                                "spurl"
                            )
                        )
                    )
                pokemonsList.results.clear()
                Log.d("noi", pokemons.pokemon_encounters[0].pokemon.name)

                for(i in pokemons.pokemon_encounters){
                    fetchPokemonImg(i.pokemon.url){
                            SpriteUrl ->
                        spUrl = SpriteUrl.sprites.front_default
                        pokemonsList.results.add(
                            Result(
                                i.pokemon.name,
                                i.pokemon.url,
                                spUrl
                            )
                        )
                        runOnUiThread{
                            //Log.d("noi", pokemonsList.results[0].sprite_url.isNullOrEmpty().toString())
                            PokemonLocList.adapter =
                                MainAdapter(
                                    pokemonsList,
                                    this@LocationPageActivity,
                                    1
                                )

                        }
                    }


                }



            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }

        })

    }

    fun fetchPokemonImg(pokemonUrl: String, resultHandler: (Pokemon) -> Unit) {
        println("Connecting")

        val url = pokemonUrl

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body =  response.body?.string()
                println(body)

                val gson = GsonBuilder().create()

                resultHandler(gson.fromJson(body, Pokemon::class.java))

            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }

        })

    }
}
