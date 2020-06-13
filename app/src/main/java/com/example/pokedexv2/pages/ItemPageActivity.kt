package com.example.pokedexv2.pages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.pokedexv2.CustomViewHolder
import com.example.pokedexv2.Item
import com.example.pokedexv2.R
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_page_activity.*
import okhttp3.*
import java.io.IOException

class ItemPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_page_activity)
        supportPostponeEnterTransition()

        val pokeUrl = intent.getStringExtra(CustomViewHolder.PokemonUrl)
        val navbarTitle = intent.getStringExtra(CustomViewHolder.PokemonName)
        supportActionBar?.title = navbarTitle
        fetchPokemon(pokeUrl)


    }

    override fun onOptionsItemSelected(item:android.view.MenuItem):Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onClickHome()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun onClickHome() {
        super.onBackPressed()
    }


    fun fetchPokemon(pokemonUrl: String) {
        println("Connecting")

        val url = pokemonUrl

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body =  response.body?.string()
                println(body)

                val gson = GsonBuilder().create()

                val item = gson.fromJson(body, Item::class.java)
                Log.d("boi",body)

                runOnUiThread{
                    Picasso.with(applicationContext).load(item.sprites.default).into(itemImage)
                    var effText = ""
                    for(i in item.effect_entries){
                        effText += i.effect
                    }
                    val new = effText.replace(":","\n")
                    Log.d("jiii",new)
                    effect.text = new
                    supportStartPostponedEnterTransition()
                }


            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }

        })

    }
}