package com.example.pokedexv2.pages

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.pokedexv2.*
import com.example.pokedexv2.database.PokemonDatabase
import com.example.pokedexv2.database.PokemonEntity
import com.example.pokedexv2.loading.LoadingAnimation
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.pokemon_page_activity.*
import okhttp3.*
import java.io.IOException
import kotlin.math.abs
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

    open class OnSwipeTouchListener : View.OnTouchListener {

        private val gestureDetector = GestureDetector(GestureListener())

        fun onTouch(event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onTouch(e)
                return true
            }


            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val result = false
                try {
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    if (abs(diffX) > abs(diffY)) {
                        if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        }
                    } else {
                        // onTouch(e);
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

                return result
            }
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        open fun onSwipeRight() {}

        open fun onSwipeLeft() {}

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
                    if(evolution.chain.evolves_to.isNotEmpty()){
                        if(!evolution.chain.evolves_to[0].species.name.isNullOrEmpty()){
                            pokemon.evolutionchain += "->" + evolution.chain.evolves_to[0].species.name
                            if(!evolution.chain.evolves_to[0].evolves_to[0].species.name.isNullOrEmpty()){
                                pokemon.evolutionchain += "->" + evolution.chain.evolves_to[0].evolves_to[0].species.name
                            }
                        }
                    }

                    pokemon.stats = stats
                    pokemon.image =
                        Utils.getBytes(pokeImage.drawable.toBitmap())
                    PokemonDatabase.getInstance(
                        this@PokemonPageActivity
                    ).pokemonDao().SavePokemon(pokemon)
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
                        saveAnim.visibility = View.GONE
                        pageLayout.setOnTouchListener(object : OnSwipeTouchListener(){
                            override fun onSwipeLeft() {
                                Log.e("ViewSwipe", "Left")
                                save = true
                                fetchPokemon(pokeUrl)

                                saveAnim.visibility = View.VISIBLE
                                saveAnim.playAnimation()


                            }

                            override fun onSwipeRight() {
                                Log.e("ViewSwipe", "Right")
                                save = true
                                fetchPokemon(pokeUrl)
                                saveAnim.visibility = View.VISIBLE
                                saveAnim.playAnimation()
                            }
                        })

                        saveAnim.addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                Log.e("Animation:", "start")
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                                TODO("Not yet implemented")
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                saveAnim.visibility = View.GONE
                                Log.e("Animation:", "end")
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                                TODO("Not yet implemented")
                            }
                        })

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
                            evo3.visibility = View.GONE
                            evo2.text = evolution.chain.species.name
                            evo2.isClickable = false
                            evo2.setOnClickListener {
                                intent.putExtra(CustomViewHolder.PokemonName,  evolution.chain.species.name)
                                val url = evolution.chain.species.url.replace("-species","")
                                intent.putExtra(CustomViewHolder.PokemonUrl, url)
                                finish()
                                startActivity(intent)
                            }
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