package com.example.project1
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class GameModel(
    var currentPlayer : String = ""
    var gameID : String = "-1",
    var sketch : SnapshotStateList<Line> = mutableStateListOf(),
    var word : String = "",
    var guess : String = "",
    var isCorrect : String = "",
    var gameStatus : GameStatus = GameStatus.CREATED
)

enum class GameStatus{
    CREATED,
    JOINED,
    DRAW,
    GUESS,
    FINISHED
}


