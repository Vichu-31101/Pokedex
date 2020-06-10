package com.example.pokedexv2

class PokemonList(var results: MutableList<Result>)

class Result(val name: String, val url: String, var sprite_url: String, var image: ByteArray? = null)

class Pokemon(val sprites: PokemonSprite, val types: List<Type>, val id: Int, val height: Int, val weight: Int, val species: Species, val stats: List<Stat>)

class Species(val url: String)

class PokemonSpecies(val evolution_chain: Url)

class Url(val url: String)

class EvolutionChain(val chain:  Chain)

class Chain(val evolves_to: List<Evolution>, val species: Result)

class Evolution(val evolves_to: List<Evolution>, val species: Result)

class PokemonSprite(val front_default: String)

class Type(val type: Prop)

class Stat(val stat: Prop, val base_stat: Int)

class Prop(val name: String, val url: String)

class Item(val sprites: ItemSprite, val effect_entries: List<Effect>, val id:Int)

class Effect(val effect: String)

class ItemSprite(val default: String)

class PokemonEncounter(val pokemon_encounters: MutableList<ePokemon>)

class ePokemon(val pokemon: Result)

class PokemonType(val pokemon: List<ePokemon>)