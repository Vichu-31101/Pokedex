package com.example.pokedexv2


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.pokedexv2.database.PokemonDatabase
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity()  {

    var page = 0
    var isInit = false
    val limit = 10
    var option = 1
    var searching = false


    lateinit var pokemonList: PokemonList
    lateinit var adapter: MainAdapter
    lateinit var layoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fetchPokemonList(option)

       // SearchFilter.visibility = View.GONE


        layoutManager = LinearLayoutManager(this)

        PokemonListView.layoutManager = layoutManager
        PokemonListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val pastVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                val total = PokemonListView.adapter?.itemCount
                    if ((visibleItemCount + pastVisibleItem) >= total!! && !searching) {
                        page++
                        fetchPokemonList(option)
                    }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        val toggle = ActionBarDrawerToggle(this, Drawer, R.string.open, R.string.close)
        Drawer.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        NavView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.pokemons -> {
                    option=1
                    page = 0
                    isInit = false
                    Drawer.closeDrawers()
                    it.isChecked = false
                    fetchPokemonList(option)

                    true
                }
                R.id.items -> {
                    option=2
                    page = 0
                    isInit = false
                    Drawer.closeDrawers()
                    it.isChecked = false
                    fetchPokemonList(option)
                    true
                }
                R.id.locations -> {
                    option=3
                    page = 0
                    isInit = false
                    Drawer.closeDrawers()
                    it.isChecked = false
                    fetchPokemonList(option)
                    true
                }
                R.id.favorites -> {
                    option=4
                    page = 0
                    isInit = false
                    Drawer.closeDrawers()
                    it.isChecked = false
                    fetchPokemonList(option)
                    true
                }

                else -> false
            }

        }

        SearchFilter.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                progressBar.visibility = View.GONE

                adapter.getFilter().filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(!searching){
                    Log.d("SearchView:", "onClose")
                    searching=true
                    fetchPokemonList(option)

                }
                if(newText.isNullOrEmpty()){
                    Log.d("SearchView:", "onClose")
                    fetchPokemonList(option)
                }

                return false
            }

        })
        SearchFilter.setOnCloseListener {
            Log.d("SearchView:", "onClose")
            searching = false
            page = 0
            fetchPokemonList(option)
            isInit = false
            false
        }


    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return  when(item.itemId){
            android.R.id.home -> {
                SearchFilter.setQuery("",true)
                SearchFilter.isIconified = true

                Drawer.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    fun fetchPokemonList(option: Int){
        println("Connecting")
        progressBar.visibility = View.VISIBLE
        val start = ((page) * limit) + 1
        val end = (page + 1) * limit
        var bUrl = String()
        if(option == 1){
             bUrl = "https://pokeapi.co/api/v2/pokemon?"
        }
        else if(option == 2){
            bUrl = "https://pokeapi.co/api/v2/item?"
        }
        else if(option == 3){
            bUrl = "https://pokeapi.co/api/v2/location-area?"
        }
        else if(option == 4){
            bUrl = "https://pokeapi.co/api/v2/pokemon?"
        }

        var offStr = "offset=0"
        var limStr = "&limit="

        //offStr += start.toString()
        if(!searching){
            limStr += end.toString()
        }
        else{
            limStr += "1000"
        }


        val url = bUrl + offStr + limStr



        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }

            override fun onResponse(call: Call, response: Response) {
                val body =  response.body?.string()
                println(body)

                val gson = GsonBuilder().create()



                if(option==1){
                    pokemonList = gson.fromJson(body, PokemonList::class.java)
                    var bUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"
                    var eUrl = ".png"

                    for(i in (0 until (pokemonList.results.size)))
                    {
                        var mUrl = pokemonList.results[i].url.filter { it.isDigit() }.substring(1)
                        var spUrl = bUrl+mUrl+eUrl
                        pokemonList.results[i].sprite_url = spUrl


                    }
                }
                else if(option == 2){
                    pokemonList = gson.fromJson(body, PokemonList::class.java)
                    var bUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/"
                    var eUrl = ".png"
                    for(i in (0 until pokemonList.results.size)){
                        var mUrl = pokemonList.results[i].name
                        var spUrl = bUrl+mUrl+eUrl
                        pokemonList.results[i].sprite_url = spUrl
                    }

                }
                else if(option == 3){
                    pokemonList = gson.fromJson(body, PokemonList::class.java)
                }
                else if(option == 4){
                    pokemonList = gson.fromJson(body, PokemonList::class.java)
                    pokemonList.results.clear()
                    PokemonDatabase.getInstance(this@MainActivity).pokemonDao().readPokemon().forEach {
                            pokemon ->
                        pokemonList.results.add(Result(pokemon.name,pokemon.stats,pokemon.evolutionchain,pokemon.image))

                    }
                }

                runOnUiThread {

                        if(!isInit || page==0){
                            adapter = pokemonList?.let { MainAdapter(it,this@MainActivity,option) }!!
                            PokemonListView.adapter = adapter
                            isInit = true
                        }
                        else{
                            if (pokemonList != null) {
                                adapter.addData(pokemonList)
                            }
                            adapter.notifyDataSetChanged()
                        }
                        progressBar.visibility = View.GONE


                    }
                }



        })
    }


}


