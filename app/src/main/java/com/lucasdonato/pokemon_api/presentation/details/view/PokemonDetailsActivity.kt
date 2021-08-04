package com.lucasdonato.pokemon_api.presentation.details.view

import Abilities
import Types
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.*
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lucasdonato.pokemon_api.R
import com.lucasdonato.pokemon_api.data.model.Pokemon
import com.lucasdonato.pokemon_api.data.model.Results
import com.lucasdonato.pokemon_api.mechanism.EXTRA_ID
import com.lucasdonato.pokemon_api.mechanism.extensions.convertValue
import com.lucasdonato.pokemon_api.mechanism.extensions.toast
import com.lucasdonato.pokemon_api.mechanism.livedata.Status
import com.lucasdonato.pokemon_api.presentation.details.adapter.AbilitiesRecyclerAdapter
import com.lucasdonato.pokemon_api.presentation.details.adapter.TypeRecyclerAdapter
import com.lucasdonato.pokemon_api.presentation.details.presenter.DetailsPresenter
import com.lucasdonato.pokemon_api.presentation.home.adapter.PokemonRecyclerAdapter
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.include_card_image_description.*
import kotlinx.android.synthetic.main.include_description.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class PokemonDetailsActivity : AppCompatActivity() {

    companion object {
        fun getStartIntent(context: Context, pokemon: Results? = null): Intent =
            Intent(context, PokemonDetailsActivity::class.java).apply {
                putExtra(EXTRA_ID, pokemon)
            }
    }

    private val abilitiesList: AbilitiesRecyclerAdapter by lazy { AbilitiesRecyclerAdapter() }
    private val typeList: TypeRecyclerAdapter by lazy { TypeRecyclerAdapter() }
    private val presenter: DetailsPresenter by inject { parametersOf(this) }
    private lateinit var pokemonData: Results

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        clickListeners()
        pokemonData = intent?.getSerializableExtra(EXTRA_ID) as Results
        receiveData()
    }

    private fun clickListeners() {
        back.setOnClickListener { finish() }
    }

    private fun receiveData() {
        pokemonData.also {
            it.number?.let { number -> presenter.getPokemonDetails(number) }
            it.imageUrl?.let { image -> presenter.getImageInGlide(image, this, image_pokemon) }
        }
        setupObserver()
    }

    private fun setupObserver() {
        presenter.getListLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> loader.visibility = VISIBLE
                Status.SUCCESS -> setupView(it.data)
                Status.ERROR -> showErrorToast()
                else -> showErrorToast()
            }
        })
        presenter.image.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> image_progress.visibility = VISIBLE
                Status.ERROR -> errorImage()
                Status.SUCCESS -> image_progress.visibility = GONE
                else -> errorImage()
            }
        })
    }

    private fun setupView(pokemon: Pokemon?) {
        loader.visibility = GONE
        pokemon?.let {
            name.text = it.name?.capitalize()
            height.text = getString(R.string.pokemon_height, convertValue(it.height))
            weight.text = getString(R.string.pokemon_weight, convertValue(it.weight))
            it.types?.let { types -> type(types) }
            it.abilities?.let { abilities -> abilities(abilities) }
        }
    }

    private fun errorImage() {
        image_progress.visibility = GONE
        image_pokemon.setImageResource(R.drawable.pikachu_surprised)
    }

    private fun type(types: List<Types>) {
        typeList.data = types.toMutableList()
        type_recycler.apply {
            adapter = typeList
            isFocusable = false
        }
    }

    private fun abilities(abilities: List<Abilities>) {
        abilitiesList.data = abilities.toMutableList()
        abilities_recycler.apply {
            adapter = abilitiesList
            isFocusable = false
        }
    }

    private fun showErrorToast() {
        loader.visibility = GONE
        toast(getString(R.string.error_generic))
    }

}