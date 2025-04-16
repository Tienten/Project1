// GameViewModel.kt
package com.example.project1

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class GameViewModel : ViewModel() {
    private val _gameModel = MutableLiveData(
        GameModel(
            sketch = mutableStateListOf()
        )
    )
    val gameModel: LiveData<GameModel> = _gameModel

    val gameStatusText: LiveData<String>
        get() = MutableLiveData(_gameModel.value?.let { getStatusText(it) })

    fun updateGameModel(update: GameModel.() -> Unit) {
        val current = _gameModel.value ?: GameModel()
        current.update()
        _gameModel.postValue(current)

        if(current.gameID!="-1") {
            Firebase.firestore.collection("games")
                .document(current.gameID)
                .set(current)
        }

    }

    private fun getStatusText(gameModel: GameModel): String {
        return when (gameModel.gameStatus) {
            GameStatus.CREATED -> "Game ID: ${gameModel.gameID}"
            GameStatus.JOINED -> "Click on start game"
            GameStatus.DRAW -> "Your friend is sketching..."
            GameStatus.GUESS -> "Your friend is taking a guess..."
            GameStatus.FINISHED -> {
                if (gameModel.isCorrect.isNotEmpty()) "${gameModel.isCorrect} Won" else "DRAW"
            }
        }
    }
}
