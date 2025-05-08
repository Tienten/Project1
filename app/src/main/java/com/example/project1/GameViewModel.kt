package com.example.project1

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class GameViewModel : ViewModel() {
    private val _gameModel = MutableLiveData(
        GameModel()
    )
    val gameModel: LiveData<GameModel> = _gameModel

    private val _gameStatusText = MutableLiveData<String>()
    val gameStatusText: LiveData<String> = _gameStatusText
//    val gameStatusText: LiveData<String>
//        get() = MutableLiveData(_gameModel.value?.let { getStatusText(it) })
    private val _sketchStatusText = MutableLiveData<String>()
    val sketchStatusText: LiveData<String> = _sketchStatusText
    private val _guessStatusText = MutableLiveData<String>()
    val guessStatusText: LiveData<String> = _guessStatusText


    fun updateGameModel(update: GameModel.() -> Unit) {
        val current = _gameModel.value ?: GameModel()
        current.update()
        _gameModel.postValue(current)
        _gameStatusText.postValue(getStatusText(current))
        _sketchStatusText.postValue(getsketchStatusText(current))
        _guessStatusText.postValue(getguessStatusText(current))


        if(current.gameID!="-1") {
            Firebase.firestore.collection("games")
                .document(current.gameID)
                .set(current)
        }

    }

    fun createGameModel(update: GameModel.() -> Unit) {
        val current = _gameModel.value ?: GameModel()
        current.update()
        _gameModel.postValue(current)

        if(current.gameID!="-1") {
            Firebase.firestore.collection("games")
                .document(current.gameID)
                .set(current)
        }

    }

    fun getGameID(): String {
        return _gameModel.value?.gameID ?: ""
    }

//    fun getGuess(): String {
//        return _gameModel.value?.guess ?: ""
//    }

    fun getIsCorrect(): String {
        return _gameModel.value?.isCorrect ?: ""
    }


    fun getWord(): String {
        return _gameModel.value?.word ?: ""
    }
//    fun getCurrentPlayer(): String {
//        return _gameModel.value?.currentPlayer ?: ""
//    }

    fun getSketch(): List<Line> {
        return _gameModel.value?.sketch?.map { it.toLine() } ?: emptyList()
    }

    fun getStatusText(gameModel: GameModel): String {
        return when (gameModel.gameStatus) {
            GameStatus.CREATED -> "Game ID: ${gameModel.gameID}"
            GameStatus.JOINING -> " Game ID: ${gameModel.gameID}. Waiting for your friend to join..."
            GameStatus.JOINED -> "Your friend has joined. Click on start drawing"
            GameStatus.DRAW -> "Your friend is sketching..."
            GameStatus.GUESS -> "Your friend is taking a guess..."
            GameStatus.FINISHED -> {
                if (gameModel.isCorrect.isNotEmpty()) "${gameModel.isCorrect} Won" else "DRAW"
            }
        }
    }
    fun getsketchStatusText(gameModel: GameModel): String {
        return when (gameModel.sketchStatus) {
            SketchStatus.NONE -> "None"
            SketchStatus.DONE -> "Done"
        }
    }
    fun getguessStatusText(gameModel: GameModel): String {
        return when (gameModel.guessStatus) {
            GuessStatus.NONE -> "None"
            GuessStatus.DONE -> "Done"
        }
    }

    fun reloadGameData() {
        val id = _gameModel.value?.gameID ?: return
        Firebase.firestore.collection("games")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                val updated = doc.toObject(GameModel::class.java)
                if (updated != null) {
                    _gameModel.value = updated
                    _gameStatusText.value = getStatusText(updated)
                    _sketchStatusText.value = getsketchStatusText(updated)
                    _guessStatusText.value = getguessStatusText(updated)
                } else {
                    Log.e("Reload", "Reload failed: Missing fields in updated game data")
                }
            }
            .addOnFailureListener {
                Log.e("Reload", "Error fetching game: ${it.message}")
            }

    }
    fun resetGameModel() {
        updateGameModel {
            currentPlayer = ""
            gameID = "-1"
            sketch = listOf()
            word = ""
            guess = ""
            isCorrect = ""
            gameStatus = GameStatus.CREATED
            sketchStatus = SketchStatus.NONE
            guessStatus  = GuessStatus.NONE
        }

    }


}
