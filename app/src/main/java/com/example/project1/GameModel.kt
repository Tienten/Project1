package com.example.project1

data class GameModel(
    var currentPlayer : String = "", //draw or guess
    var gameID : String = "-1",
    var sketch: List<LineDTO> = listOf(),
    var word : String = "",
    var guess : String = "",
    var isCorrect : String = "",
    var gameStatus : GameStatus = GameStatus.CREATED,
    var sketchStatus : SketchStatus = SketchStatus.NONE,
    var guessStatus : GuessStatus = GuessStatus.NONE

)

enum class GameStatus{
    CREATED,    // Player 1 created a game session
    JOINING,    // Assigned gameID and a word to guess
    JOINED,     // Player 2 joined
    DRAW,       // Player 1 is sketching
    GUESS,      // Player 2 is guessing
    FINISHED    // Game session finishes
}

enum class SketchStatus{
    NONE,
    DONE
}

enum class GuessStatus{
    NONE,
    DONE
}



