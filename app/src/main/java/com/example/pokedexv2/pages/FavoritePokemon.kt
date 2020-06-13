package com.example.pokedexv2.pages

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.example.pokedexv2.*
import com.example.pokedexv2.database.PokemonDatabase
import kotlinx.android.synthetic.main.activity_favorite_pokemon.*
import java.io.File
import java.io.FileOutputStream


class FavoritePokemon : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_pokemon)

        val pokeUrl = intent.getStringExtra(CustomViewHolder.PokemonUrl)
        val navbarTitle = intent.getStringExtra(CustomViewHolder.PokemonName)
        supportActionBar?.title = navbarTitle
        supportPostponeEnterTransition()



        Thread {
                PokemonDatabase.getInstance(
                    applicationContext
                ).pokemonDao().readPokemonFromName(navbarTitle).forEach {
                pokemon->
                fpokeImage.setImageBitmap(
                    Utils.getImage(
                        pokemon.image
                    )
                )
                ftype.text = pokemon.type
                fstatsText.text = pokemon.stats
                fevolText.text = pokemon.evolutionchain

            }
            supportStartPostponedEnterTransition()
        }.start()

        share.setOnClickListener {
            //val builder = VmPolicy.Builder()
            //StrictMode.setVmPolicy(builder.build())
            val image = fpokeImage.drawable.toBitmap()
            val file = File(this.externalCacheDir,"image.png")
            val fout = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 100, fout)
            fout.flush()
            fout.close()
            file.setReadable(true,false)
            val text = supportActionBar?.title.toString() + "\n\n" + ftype.text + "\n" + fevolText.text + "\n\n" +  fstatsText.text
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this@FavoritePokemon,
                    BuildConfig.APPLICATION_ID + ".provider", file))
                putExtra(Intent.EXTRA_TEXT, text)
                type = "image/png"
            }
            startActivity(Intent.createChooser(sendIntent, "Share"))
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

}
