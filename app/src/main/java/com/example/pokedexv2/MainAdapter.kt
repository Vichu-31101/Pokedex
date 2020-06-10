package com.example.pokedexv2

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_layout.view.*
import java.util.*


class MainAdapter(var pokemonList: PokemonList, val activity: Activity, val option: Int): RecyclerView.Adapter<CustomViewHolder>() {

    override fun getItemCount(): Int {

        return pokemonList.results.size


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.row_layout, parent, false)
        return CustomViewHolder(cellForRow,activity,option)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val pokemon = pokemonList.results[position]
        holder.view.textView.text = pokemon.name

        if(option != 3 && option != 4){
            val pokemonPic = holder.view.imageView
            Picasso.with(holder.view.context).load(pokemonList.results[position].sprite_url).into(pokemonPic)
        }
        else if(option == 4){
            val pokemonPic = holder.view.imageView
            pokemonPic.setImageBitmap(Utils.getImage(pokemon.image))
        }
        else{
            holder.view.imageView.visibility = View.GONE
            val constraintSet = ConstraintSet()
            constraintSet.clone(holder.view.rowlayout)
            constraintSet.setHorizontalBias(holder.view.textView.id, 0.5F)
            constraintSet.applyTo(holder.view.rowlayout)
        }


        //ViewCompat.setTransitionName(holder.view.imageView, pokemon.name)
        holder.pokemon = pokemon
    }

    fun addData(newPokemonList: PokemonList){
        pokemonList = newPokemonList
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                   pokemonList=  pokemonList
                } else {
                    pokemonList.results = pokemonList.results.filter {
                        it.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)) || ((pokemonList.results.indexOf(it)+1).toString() == charSearch.toLowerCase(Locale.ROOT) && charSearch.toLowerCase(Locale.ROOT).length == (pokemonList.results.indexOf(it)+1).toString().length)
                    } as MutableList<Result>
                }
                val filterResults = FilterResults()
                filterResults.values = pokemonList

                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                pokemonList = results?.values as PokemonList
                for(i in pokemonList.results){
                    Log.d("hey", i.name)
                }
                notifyDataSetChanged()
            }

        }
    }




}

class CustomViewHolder(val view: View,var activity: Activity, val option: Int, var pokemon: Result? = null): RecyclerView.ViewHolder(view){

    companion object{
        val PokemonName = "Pokemon"
        val PokemonUrl = "Url"
        val option = "option"
    }
    init {

        view.setOnClickListener {
            var intent = Intent()
            if(option==1){
                intent = Intent(view.context, PokemonPageActivity::class.java)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, view.imageView, ViewCompat.getTransitionName(view.imageView)!!
                )
                intent.putExtra(PokemonName, pokemon?.name)
                intent.putExtra(PokemonUrl, pokemon?.url)
                view.context.startActivity(intent, options.toBundle())

            }
            else if(option==2){
                intent = Intent(view.context, ItemPageActivity::class.java)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, view.imageView, ViewCompat.getTransitionName(view.imageView)!!
                )
                intent.putExtra(PokemonName, pokemon?.name)
                intent.putExtra(PokemonUrl, pokemon?.url)
                view.context.startActivity(intent, options.toBundle())
            }
            else if(option==3){
                intent = Intent(view.context, LocationPageActivity::class.java)
                intent.putExtra(PokemonName, pokemon?.name)
                intent.putExtra(PokemonUrl, pokemon?.url)

                view.context.startActivity(intent)

            }
            else if(option==4){
                intent = Intent(view.context, FavoritePokemon::class.java)
                intent.putExtra(PokemonName, pokemon?.name)
                intent.putExtra(PokemonUrl, pokemon?.url)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, view.imageView, ViewCompat.getTransitionName(view.imageView)!!
                )
                view.context.startActivity(intent, options.toBundle())
            }

        }
    }

}

