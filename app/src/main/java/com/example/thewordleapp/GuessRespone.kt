package com.example.thewordleapp

data class GuessResponse(
    val guess: String,
    val result: List<LetterResult>
)

