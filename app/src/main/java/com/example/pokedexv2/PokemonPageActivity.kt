package com.example.pokedexv2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.MotionEventCompat
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.pokemon_page_activity.*
import okhttp3.*
import java.io.IOException
import kotlin.properties.Delegates


class PokemonPageActivity: AppCompatActivity(){

    lateinit var load : LoadingAnimation
    lateinit var spurl: String
    lateinit var types: MutableList<String>
    lateinit var stats: String
    lateinit var pokeUrl: String
    var id: Int = 0
    var height: Int = 0
    var weight: Int = 0

    var save = false


    var option by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pokemon_page_activity)
        //setContentView(R.layout.loading)
        supportPostponeEnterTransition()


        pokeUrl = intent.getStringExtra(CustomViewHolder.PokemonUrl)
        val navbarTitle = intent.getStringExtra(CustomViewHolder.PokemonName)
        option = intent.getIntExtra(CustomViewHolder.option,0)
        if(option == 1)
        {
            load =
                LoadingAnimation(
                    this,
                    "loadingnews.json"
                )
            load.playAnimation(true)
        }

        supportActionBar?.title = navbarTitle
        fetchPokemon(pokeUrl)


    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val action: Int = MotionEventCompat.getActionMasked(event)
        val DEBUG_TAG = "bruhh"
        return when (action) {
            MotionEvent.ACTION_UP -> {
                Log.d(DEBUG_TAG, "Action was UP")
                save = true
                fetchPokemon(pokeUrl)
                true
            }
            else -> super.onTouchEvent(event)
        }
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

                val pokemon = gson.fromJson(body, Pokemon::class.java)
                spurl = pokemon.sprites.front_default
                id = pokemon.id
                weight = pokemon.weight
                height = pokemon.height
                types = mutableListOf("")
                types.clear()
                for(i in pokemon.types){
                    types.add(i.type.name)
                }
                stats = ""
                for(i in pokemon.stats){
                    stats += i.stat.name + " : " +i.base_stat + "\n"
                }


                fetchPokemonSpecies(pokemon.species.url)


            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }

        })

    }

    fun fetchPokemonSpecies(pokemonUrl: String) {
    println("Connecting")

    val url = pokemonUrl

    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object: Callback {
        override fun onResponse(call: Call, response: Response) {
            val body =  response.body?.string()
            println(body)

            val gson = GsonBuilder().create()

            val species = gson.fromJson(body, PokemonSpecies::class.java)

            fetchPokemonEvolution(species.evolution_chain.url){
                evolution ->
                if(save){
                    var pokemon = PokemonEntity()
                    pokemon.name = supportActionBar?.title as String
                    pokemon.id = id
                    pokemon.height = height
                    pokemon.weight = weight
                    for(i in types){
                        pokemon.type += i + "\n"
                    }
                    pokemon.evolutionchain = evolution.chain.species.name
                    if(!evolution.chain.evolves_to[0].species.name.isNullOrEmpty()){
                        pokemon.evolutionchain += "->" + evolution.chain.evolves_to[0].species.name
                        if(!evolution.chain.evolves_to[0].evolves_to[0].species.name.isNullOrEmpty()){
                            pokemon.evolutionchain += "->" + evolution.chain.evolves_to[0].evolves_to[0].species.name
                        }
                    }

                    pokemon.stats = stats
                    pokemon.image = Utils.getBytes(pokeImage.drawable.toBitmap())
                    PokemonDatabase.getInstance(this@PokemonPageActivity).pokemonDao().SavePokemon(pokemon)
                    runOnUiThread {
                        Toast.makeText(applicationContext,"Saved", Toast.LENGTH_SHORT).show()
                    }

                    save = false
                }
                else{
                    runOnUiThread {
                        intent = Intent(applicationContext, PokemonPageActivity::class.java)
                        intent.putExtra(CustomViewHolder.option, 1)

                        if(option == 1){
                            load.stopAnimation(R.layout.pokemon_page_activity)
                        }

                        Picasso.with(applicationContext).load(spurl).into(pokeImage)

                        type.text = types[0]
                        type.setOnClickListener {
                            val text = type.text
                            val url = "https://pokeapi.co/api/v2/type/$text"
                            Log.d("hello",url)
                            intent = Intent(applicationContext, TypePageActivity::class.java)
                            intent.putExtra(CustomViewHolder.PokemonName, text)
                            intent.putExtra(CustomViewHolder.PokemonUrl, url)
                            startActivity(intent)
                        }
                        if(types.size > 1)
                        {
                            type2.text = types[1]
                            type2.setOnClickListener {
                                val text = type2.text
                                val url = "https://pokeapi.co/api/v2/type/$text"
                                Log.d("hello",url)
                                intent = Intent(applicationContext, TypePageActivity::class.java)
                                intent.putExtra(CustomViewHolder.PokemonName, text)
                                intent.putExtra(CustomViewHolder.PokemonUrl, url)
                                startActivity(intent)
                            }
                        }
                        else{
                            type2.visibility = View.GONE
                        }
                        statsText.text = stats
                        if(evolution.chain.evolves_to.isNotEmpty()){

                            evo1.text = evolution.chain.species.name
                            evo3.text = evolution.chain.evolves_to[0].species.name
                            if(evolution.chain.evolves_to[0].evolves_to.isNotEmpty()){
                                evo2.text = evolution.chain.evolves_to[0].species.name
                                evo3.text = evolution.chain.evolves_to[0].evolves_to[0].species.name
                                evo2.setOnClickListener {
                                    intent.putExtra(CustomViewHolder.PokemonName, evolution.chain.evolves_to[0].species.name)
                                    val url = evolution.chain.evolves_to[0].species.url.replace("-species","")
                                    intent.putExtra(CustomViewHolder.PokemonUrl, url)
                                    finish()
                                    startActivity(intent)
                                }
                                evo3.setOnClickListener {
                                    intent.putExtra(CustomViewHolder.PokemonName, evolution.chain.evolves_to[0].evolves_to[0].species.name)
                                    val url = evolution.chain.evolves_to[0].evolves_to[0].species.url.replace("-species","")
                                    intent.putExtra(CustomViewHolder.PokemonUrl, url)
                                    finish()
                                    startActivity(intent)
                                }
                            }
                            else{
                                evo2.visibility = View.GONE
                                evo3.setOnClickListener {
                                    intent.putExtra(CustomViewHolder.PokemonName, evolution.chain.evolves_to[0].species.name)
                                    val url = evolution.chain.evolves_to[0].species.url.replace("-species","")
                                    intent.putExtra(CustomViewHolder.PokemonUrl, url)
                                    finish()
                                    startActivity(intent)
                                }
                            }
                            evo1.setOnClickListener {
                                intent.putExtra(CustomViewHolder.PokemonName,  evolution.chain.species.name)
                                val url = evolution.chain.species.url.replace("-species","")
                                intent.putExtra(CustomViewHolder.PokemonUrl, url)
                                finish()
                                startActivity(intent)
                            }
                        }
                        else{
                            evo1.visibility = View.GONE
                            evo2.visibility = View.GONE
                            evo3.visibility = View.GONE
                        }
                        supportStartPostponedEnterTransition()
                    }
                }


            }

        }

        override fun onFailure(call: Call, e: IOException) {
            println("Failed")
        }

    })

    }
    fun fetchPokemonEvolution(pokemonUrl: String, resultHandler: (EvolutionChain) -> Unit) {
        println("Connecting")

        val url = pokemonUrl

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body =  response.body?.string()
                println(body)

                val gson = GsonBuilder().create()

                resultHandler(gson.fromJson(body, EvolutionChain::class.java))

            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }

        })

    }
}