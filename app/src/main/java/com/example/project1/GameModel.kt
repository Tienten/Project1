package com.example.project1

data class GameModel(
    var currentPlayer : String = "", //draw or guess
    var gameID : String = "-1",
    var sketch: List<LineDTO> = listOf(),

//    var sketch : SnapshotStateList<Line> = mutableStateListOf(),
//    var sketch: List<Line> = listOf(),
    var word : String = "",
    var guess : String = "",
    var isCorrect : String = "",
    var gameStatus : GameStatus = GameStatus.CREATED,
    var sketchStatus : SketchStatus = SketchStatus.NONE,
    var guessStatus : GuessStatus = GuessStatus.NONE

)

enum class GameStatus{
    CREATED,
    JOINING,
    JOINED,
    DRAW,
    GUESS,
    FINISHED
}

enum class SketchStatus{
    NONE,
    DONE
}

enum class GuessStatus{
    NONE,
    DONE
}



