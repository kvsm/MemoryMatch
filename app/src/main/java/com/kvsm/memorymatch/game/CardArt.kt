package com.kvsm.memorymatch.game

import androidx.annotation.DrawableRes
import com.kvsm.memorymatch.R

/** Every picture that can appear on a card. Each game deals a random selection, two of each. */
enum class CardArt(@DrawableRes val drawable: Int, val label: String) {
    // Animals
    CAT(R.drawable.card_cat, "Cat"),
    DOG(R.drawable.card_dog, "Dog"),
    FROG(R.drawable.card_frog, "Frog"),
    PIG(R.drawable.card_pig, "Pig"),
    OWL(R.drawable.card_owl, "Owl"),
    PENGUIN(R.drawable.card_penguin, "Penguin"),
    LION(R.drawable.card_lion, "Lion"),
    BUNNY(R.drawable.card_bunny, "Bunny"),

    // Toys
    TEDDY(R.drawable.card_teddy, "Teddy bear"),
    DUCK(R.drawable.card_duck, "Rubber duck"),
    BALL(R.drawable.card_ball, "Beach ball"),
    ROCKET(R.drawable.card_rocket, "Rocket"),
    ROBOT(R.drawable.card_robot, "Robot"),
    KITE(R.drawable.card_kite, "Kite"),
    CAR(R.drawable.card_car, "Toy car"),
    DRUM(R.drawable.card_drum, "Drum"),
}
