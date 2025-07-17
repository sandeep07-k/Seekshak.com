package com.example.seekshakcom.utils

data class LocationData(
    val states: List<State>
)

data class State(
    val name: String,
    val cities: List<City>
)

data class City(
    val name: String,
    val localities: List<String>
)
