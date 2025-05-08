package com.example.project1

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.applyCanvas
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.project1.ui.theme.Project1Theme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.io.OutputStream
import kotlin.random.Random
import kotlin.random.nextInt




class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project1Theme {
                AppNavigator(viewModel = viewModel)

            }
        }
    }
}


@Composable
fun AppNavigator(viewModel: GameViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        NavHost(navController, startDestination = "paint app", modifier = Modifier.padding(padding)) {
            composable("paint app") { PaintApp() }
            composable("main") { MainMenu(viewModel) }
            composable("game") { Game(navController) }
            composable("DualDeviceGame") { DualDeviceGame(navController, viewModel) }
            composable("JoinGame") { JoinGame(navController, viewModel) }
            composable("WaitingScreen1?word={word}") { WaitingScreen1(navController, viewModel) }
            composable("WaitingScreen2?word={word}") { backStackEntry ->
                val word = backStackEntry.arguments?.getString("word")
                WaitingScreen2(navController, word, viewModel) }
            composable("paint?word={word}") { backStackEntry ->
                val word = backStackEntry.arguments?.getString("word")
                PaintScreen(navController, word, viewModel)
            }
            composable("word") { WordScreen(navController, viewModel) }
            composable("answer?word={word}") { backStackEntry ->
                val word = backStackEntry.arguments?.getString("word")
                AnswerScreen(navController, word, viewModel)
            }
            composable("result?isCorrect={isCorrect}&correctAnswer={correctAnswer}") { backStackEntry ->
                val isCorrect = backStackEntry.arguments?.getString("isCorrect")
                val correctAnswer = backStackEntry.arguments?.getString("correctAnswer")
                ResultScreen(navController, isCorrect, correctAnswer, viewModel)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Triple("Home", "main", R.drawable.baseline_cottage_24),
        Triple("Paint", "paint app", R.drawable.baseline_color_lens_24),
        Triple("Game", "game", R.drawable.baseline_emoji_events_24)
    )

    androidx.compose.material3.NavigationBar (containerColor = Color(0xFF526049)){
        items.forEach { (label, route, iconRes) ->
            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xffffe6bd))
                    )
                },
                label = { Text(
                    text = label,
                    color = Color(0xFFffe6bd),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold)},
                selected = false,
                onClick = { navController.navigate(route) }
            )

        }
    }
}

//data class BottomNavItem(val label: String, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(viewModel: GameViewModel) {
    LaunchedEffect(Unit) {
        viewModel.updateGameModel {
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
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("DING", color = Color(0xFF526049), fontSize = 30.sp, fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome!", color = Color(0xffa54d43), fontSize = 40.sp)
        }
    }
}

val buttonModifier = Modifier
    .size(250.dp, 100.dp)
    .padding(vertical = 12.dp)

@Composable
fun Game(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("word") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
            Text("Single-Device Mode", color = Color(0xffffe6bd), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Button(onClick = { navController.navigate("DualDeviceGame") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
            Text("Dual-Device Mode", color = Color(0xffffe6bd), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Button(onClick = { navController.navigate("JoinGame") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
            Text("Join Game", color = Color(0xffffe6bd), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DualDeviceGame(navController: NavController, viewModel: GameViewModel) {
    LaunchedEffect(Unit) {
        viewModel.updateGameModel {
            currentPlayer = "draw"
            gameID = Random.nextInt(1000..9999).toString()
            sketch = listOf()
            word = ""
            guess = ""
            isCorrect = ""
            gameStatus = GameStatus.CREATED
            sketchStatus = SketchStatus.NONE
            guessStatus  = GuessStatus.NONE
        }
    }
    val statusText by viewModel.gameStatusText.observeAsState("")
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("word") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
            Text("Start Game", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }

        Text(statusText, fontSize = 25.sp)

    }
}


@Composable
fun JoinGame(navController: NavController, viewModel: GameViewModel) {
    var gameID by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter your game ID to join:")
        TextField(
            value = gameID,
            onValueChange = {
                gameID = it
                errorText = null
            },
            label = { Text("Game ID") },
            isError = errorText != null,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.8f)
        )
        if (errorText != null) {
            Text(
                text = errorText ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                if (gameID.isEmpty()) {
                    errorText = "Please enter a game ID"
                    return@Button
                }

                Firebase.firestore.collection("games")
                    .document(gameID)
                    .get()
                    .addOnSuccessListener { document ->
                        val model = document?.toObject(GameModel::class.java)
                        if (model == null) {
                            errorText = "Invalid game ID"
                        } else {
                            model.gameStatus = GameStatus.JOINED
                            model.currentPlayer ="guess"
                            viewModel.updateGameModel {
                                this.currentPlayer = model.currentPlayer
                                this.gameID = model.gameID
                                this.sketch = model.sketch
                                this.word = model.word
                                this.guess = model.guess
                                this.isCorrect = model.isCorrect
                                this.gameStatus = model.gameStatus
                            }
                            val word = model.word
                            navController.navigate("WaitingScreen2?word=$word")
                        }
                    }
                    .addOnFailureListener {
                        errorText = "Error: ${it.message}"
                    }
            },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))
        ) {
            Text("Join Game", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WaitingScreen1(navController: NavController, viewModel: GameViewModel) {
    val guessStatus by viewModel.guessStatusText.observeAsState("")


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (guessStatus == "Done"){
            val result = viewModel.getIsCorrect()
            if (result == "true"){
                Text("Your friend guessed right!!!", fontSize = 18.sp)
            } else {
                Text("Uh oh, your friend guessed wrong... Try next time!", fontSize = 18.sp)
            }
            Button(
                onClick = {navController.navigate("main")},
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))
            ) {
                Text("End Game", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }
        else {
            Text("Please wait for your friend. They are guessing...", fontSize = 18.sp)
            Button(onClick = { viewModel.reloadGameData() },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Reload", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }


    }
}

@Composable
fun WaitingScreen2(navController: NavController, word: String?, viewModel: GameViewModel) {
    val statusText by viewModel.gameStatusText.observeAsState("")
    val sketchStatus by viewModel.sketchStatusText.observeAsState("")
    val correct = viewModel.getWord()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (statusText == "Your friend is taking a guess..." && sketchStatus == "Done") {
            Text("Your friend has done sketching. You can take a guess!", fontSize = 15.sp)
            Button(
                onClick = { navController.navigate("answer?word=$correct") },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))
            ) {
                Text("Guess now!", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }

        }
        else {
            Text("Please wait for your friend. They are sketching...", fontSize = 18.sp)
            Button(onClick = { viewModel.reloadGameData() },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Reload", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }


    }
}


@Composable
fun WordScreen(navController: NavController, viewModel: GameViewModel) {
    val words = listOf(
        "Cat", "Dog", "House", "Car", "Tree", "Table", "Slide", "Trampoline", "Park", "Microwave",
        "Trash Can", "Banana", "Orange", "Sweater", "Heels", "Shoes", "Laptop", "Window", "Bottle",
        "Chair", "Lamp", "Pillow", "Blanket", "Backpack", "Phone", "Television", "Mirror", "Keyboard",
        "Mouse", "Monitor", "Door", "Fridge", "Oven", "Spoon", "Fork", "Plate", "Bicycle", "Bus",
        "Train", "Boat", "Airplane", "Clock", "Watch", "Headphones", "Book", "Notebook", "Pen", "Marker",
        "Couch", "Bed", "Curtains", "Fan", "Radio", "Camera", "Speaker", "Cup", "Mug", "Desk", "Rug",
        "Shoelaces", "Gloves", "Scarf", "Hat", "Socks", "Jacket", "Jeans", "Dress", "Suitcase", "Wallet",
        "Purse", "Earrings", "Bracelet", "Necklace", "Glasses", "Toothbrush", "Toothpaste", "Soap",
        "Shampoo", "Conditioner", "Towel", "Comb", "Brush", "Backyard", "Garage", "Sidewalk", "Street",
        "Highway", "Bridge", "Mountain", "Beach", "River", "Lake", "Ocean", "Forest", "Desert", "Island",
        "School", "Library", "Supermarket", "Mall", "Hospital", "Restaurant", "Cafe", "Bakery", "Zoo",
        "Museum", "Stadium", "Theater", "Hotel", "Airport", "Station", "Playground", "Gym", "Office",
        "Factory", "Farm", "Barn", "Market", "Pharmacy", "Bank", "Post Office", "Church", "Temple",
        "Mosque", "Castle", "Skyscraper"
    )
    val randomWord by remember { mutableStateOf(words.random()) } // Keep word consistent
    val statusText by viewModel.gameStatusText.observeAsState("")
    val gameID = viewModel.getGameID()

    viewModel.updateGameModel {
        gameStatus = GameStatus.JOINING
        word = randomWord
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (gameID == "-1") {
            Text("Draw this word: $randomWord", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("paint?word=$randomWord") },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Start Drawing", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        } else {

            Text(statusText, fontSize = 18.sp)
            Text("Draw this word: $randomWord", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("paint?word=$randomWord") },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Start Drawing", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }

    }
}


@Composable
fun PaintScreen(navController: NavController, word: String?, viewModel: GameViewModel) {
    val gameID = viewModel.getGameID()
    val correct = viewModel.getWord()

    var currentColor by remember { mutableStateOf(Color.Black) }
    val lines = remember { mutableStateListOf<Line>() }
    var brushSize by remember { mutableFloatStateOf(10f)}
    var isEraser by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
    }
    LaunchedEffect(Unit) {
//        if (gameID != "-1") {
//            Firebase.firestore.collection("games")
//                .document(gameID)
//                .update(
//                    mapOf(
//                        "currentPlayer" to "draw",
//                        "gameStatus" to GameStatus.DRAW
//                    )
//                )
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            ColorPickerBox(currentColor) { selectedColor ->
                currentColor = selectedColor
                isEraser = false
            }

            BrushSizeSelector(brushSize, onSizeSelected = {selectedSize -> brushSize = selectedSize},
                isEraser = isEraser, keepMode = {keepEraserMode -> isEraser = keepEraserMode})
            Button(onClick = {isEraser = true},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Eraser", color = Color(0xffffe6bd), fontSize = 18.sp)
            }
            Button(onClick = {lines.clear()},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Reset", color = Color(0xffffe6bd), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (gameID == "-1") {
                        navController.navigate("answer?word=$word")
                    } else {

                        Firebase.firestore.collection("games")
                            .document(gameID)
                            .get()
                            .addOnSuccessListener { document ->
                                val model = document?.toObject(GameModel::class.java)
                                if (model == null) {
                                    throw IllegalArgumentException("Model for this game ID is not found!")
                                } else {
                                    model.gameStatus = GameStatus.GUESS
                                    model.sketch = lines.map { it.toDTO() }
                                    model.sketchStatus = SketchStatus.DONE
                                    model.currentPlayer ="guess"
                                    Firebase.firestore.collection("games")
                                        .document(gameID)
                                        .set(model)
                                        .addOnSuccessListener {
                                            viewModel.updateGameModel {
                                                this.currentPlayer = model.currentPlayer
                                                this.gameID = model.gameID
                                                this.sketch = model.sketch
                                                this.word = model.word
                                                this.guess = model.guess
                                                this.isCorrect = model.isCorrect
                                                this.gameStatus = model.gameStatus
                                                this.sketchStatus = model.sketchStatus
                                                this.guessStatus = model.guessStatus
                                            }
                                            navController.navigate("WaitingScreen1?word=$correct")
                                        }
                                }
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))
            ) {
                Text("Done!", color = Color(0xffffe6bd), fontSize = 18.sp)
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()
            .background(Color.White)
            .pointerInput(true) {
                detectDragGestures{ change, dragAmount ->
                    change.consume()

                    val line = Line(
                        start = change.position - dragAmount,
                        end = change.position,
                        color = if (isEraser) Color.White else currentColor,
                        strokeWidth = brushSize
                    )
                    lines.add(line)
                }
            }) {
            lines.forEach{ line -> drawLine(
                color = line.color,
                start = line.start,
                end = line.end,
                strokeWidth = line.strokeWidth,
                cap = StrokeCap.Round
            )
            }
        }
    }


}


@Composable
fun AnswerScreen(navController: NavController, word: String?, viewModel: GameViewModel) {
    var guess by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    val correct = viewModel.getWord()

    val correctAnswer = word ?: "Unknown"
    val gameID = viewModel.getGameID()
    val sketch = viewModel.getSketch()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (gameID == "-1") {
            Text("Enter your guess:", fontSize = 18.sp)
            TextField(
                value = guess,
                onValueChange = { guess = it },
                label = { Text("Your Guess") },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(0.8f)
            )
            Button(onClick = {
                isCorrect = guess.trim().equals(correctAnswer, ignoreCase = true)
                navController.navigate("result?isCorrect=${if (isCorrect == true) "true" else "false"}&correctAnswer=$correctAnswer")
            },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Submit", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(
                Modifier.fillMaxWidth()
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enter your guess:", fontSize = 18.sp)
                TextField(
                    value = guess,
                    onValueChange = { guess = it },
                    label = { Text("Your Guess") },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    isCorrect = guess.trim().equals(correctAnswer, ignoreCase = true)
                    Firebase.firestore.collection("games")
                        .document(gameID)
                        .get()
                        .addOnSuccessListener { document ->
                            val model = document?.toObject(GameModel::class.java)
                            if (model == null) {
                                throw IllegalArgumentException("Model for this game ID is not found!")
                            } else {
                                model.gameStatus = GameStatus.FINISHED
                                model.guess = guess
                                model.guessStatus = GuessStatus.DONE
                                model.isCorrect = isCorrect.toString()
                                model.currentPlayer ="draw"
                                Firebase.firestore.collection("games")
                                    .document(gameID)
                                    .set(model)
                                    .addOnSuccessListener {
                                        viewModel.updateGameModel {
                                            this.currentPlayer = model.currentPlayer
                                            this.gameID = model.gameID
                                            this.sketch = model.sketch
                                            this.word = model.word
                                            this.guess = model.guess
                                            this.isCorrect = model.isCorrect
                                            this.gameStatus = model.gameStatus
                                            this.sketchStatus = model.sketchStatus
                                            this.guessStatus = model.guessStatus
                                        }

                                    }
                                navController.navigate("result?isCorrect=${if (isCorrect == true) "true" else "false"}&correctAnswer=$correct")
                            }
                        }
                },
                    modifier = buttonModifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                    Text("Submit", color = Color(0xffffe6bd))
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                sketch.forEach { line ->
                    drawLine(
                        color = line.color,
                        start = line.start,
                        end = line.end,
                        strokeWidth = line.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

    }
}

@Composable
fun ResultScreen(navController: NavController, isCorrect: String?, correctAnswer: String?, viewModel: GameViewModel) {
    val isCorrectBoolean = isCorrect == "true"
    val correct = viewModel.getWord()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isCorrectBoolean) "Correct!" else "Incorrect! The correct answer was $correct.",
            color = if (isCorrectBoolean) Color.Green else Color.Red,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("main") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
            Text("End Game", color = Color(0xffffe6bd), fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun PaintApp(){
    val context = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()

    var currentColor by remember { mutableStateOf(Color.Black) }
    val lines = remember { mutableStateListOf<Line>() }
    var brushSize by remember { mutableFloatStateOf(10f)}
    var isEraser by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            ColorPickerBox(currentColor) { selectedColor ->
                currentColor = selectedColor
                isEraser = false
            }

            BrushSizeSelector(brushSize, onSizeSelected = {selectedSize -> brushSize = selectedSize},
                isEraser = isEraser, keepMode = {keepEraserMode -> isEraser = keepEraserMode})
            Button(onClick = {isEraser = true},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Eraser", color = Color(0xffffe6bd), fontSize = 18.sp)
            }
            Button(onClick = {lines.clear()},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Reset", color = Color(0xffffe6bd), fontSize = 18.sp)
            }
            Button(onClick = {
                coroutineScope.launch{
                    saveDrawingToGallery(context, lines)
                }
            },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffa54d43))) {
                Text("Save", color = Color(0xffffe6bd), fontSize = 18.sp)
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()
            .background(Color.White)
            .pointerInput(true) {
                detectDragGestures{ change, dragAmount ->
                    change.consume()

                    val line = Line(
                        start = change.position - dragAmount,
                        end = change.position,
                        color = if (isEraser) Color.White else currentColor,
                        strokeWidth = brushSize
                    )
                    lines.add(line)
                }
            }) {
            lines.forEach{ line -> drawLine(
                color = line.color,
                start = line.start,
                end = line.end,
                strokeWidth = line.strokeWidth,
                cap = StrokeCap.Round
            )
            }
        }
    }
}

@Composable
fun ColorPickerBox(currentColor: Color, onColorSelected: (Color) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(currentColor, CircleShape)
            .clickable { showDialog = true }
    )

    if (showDialog) {
        ColorWheelDialog(
            onDismiss = { showDialog = false },
            onColorSelected = { selectedColor ->
                onColorSelected(selectedColor)
                showDialog = false
            }
        )
    }
}



@Composable
fun ColorWheelDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var brightness by remember { mutableFloatStateOf(1f) }

    Dialog(onDismissRequest = { }) {
        Column(
            modifier = Modifier
                .background(Color.White, shape = CircleShape)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Text(
                    "✖",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(8.dp)
                )
            }

            // Live Color Preview
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(selectedColor, CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
            )

            // Color Wheel
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ColorWheelPicker(
                    onColorSelected = { baseColor ->
                        selectedColor = baseColor.copy(alpha = brightness)
                    }
                )
            }

            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text("Brightness")
                Slider(
                    value = brightness,
                    onValueChange = { value ->
                        brightness = value
                        selectedColor = selectedColor.copy(alpha = brightness)
                    },
                    valueRange = 0f..1f
                )
            }

            Button(
                onClick = {
                    onColorSelected(selectedColor)
                    onDismiss()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Confirm")
            }
        }
    }
}




@Composable
fun ColorWheelPicker(onColorSelected: (Color) -> Unit) {
    val wheelSize = 200.dp

    Box(
        modifier = Modifier
            .size(wheelSize)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val radius = wheelSize.toPx() / 2
                    val center = Offset(radius, radius)

                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                    val hue = if (distance <= radius) {
                        val angle = kotlin.math.atan2(dy, dx) * (180 / Math.PI)
                        ((angle + 360) % 360).toFloat()
                    } else {
                        0f // Default to black outside the wheel
                    }

                    val saturation = if (distance <= radius) distance / radius else 0f
                    val selectedColor = Color.hsv(hue, saturation, 1f)

                    onColorSelected(selectedColor)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val shader = android.graphics.SweepGradient(
                size.width / 2, size.height / 2,
                intArrayOf(
                    android.graphics.Color.RED,
                    android.graphics.Color.YELLOW,
                    android.graphics.Color.GREEN,
                    android.graphics.Color.CYAN,
                    android.graphics.Color.BLUE,
                    android.graphics.Color.MAGENTA,
                    android.graphics.Color.RED
                ), null
            )

            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                this.shader = shader
                style = android.graphics.Paint.Style.FILL
            }

            drawIntoCanvas {
                it.nativeCanvas.drawCircle(size.width / 2, size.height / 2, size.width / 2, paint)
            }
        }
    }
}





@Composable
fun BrushSizeSelector(currentSize: Float, onSizeSelected: (Float) -> Unit,
                      isEraser: Boolean, keepMode: (Boolean) -> Unit) {
    var sizeText by remember { mutableStateOf(currentSize.toString()) }

    Row {
        BasicTextField(
            value = sizeText,
            onValueChange = {
                sizeText = it
                val newSize = it.toFloatOrNull() ?: currentSize
                onSizeSelected(newSize)
                keepMode(isEraser)
            },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.width(50.dp)
                .background(Color.LightGray, CircleShape)
                .padding(3.dp)
        )
        Text("px", Modifier.align(Alignment.CenterVertically))
    }
}

fun saveDrawingToGallery(context: Context, lines: List<Line>){
    val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
    bitmap.applyCanvas {
        drawColor(android.graphics.Color.WHITE)
        lines.forEach { line ->
            val paint = android.graphics.Paint().apply {
                color = line.color.toArgb()
                strokeWidth = line.strokeWidth
                style = android.graphics.Paint.Style.STROKE
                strokeCap = android.graphics.Paint.Cap.ROUND
                strokeJoin = android.graphics.Paint.Join.ROUND
            }
            drawLine(line.start.x, line.start.y, line.end.x, line.end.y, paint)
        }
    }
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "drawing_${System.currentTimeMillis()}.png")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PaintApp")
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    if (uri != null) {
        val outputStream: OutputStream? = resolver.openOutputStream(uri)
        outputStream.use {
            if (it != null){
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        Toast.makeText(context, "Save To Gallery", Toast.LENGTH_SHORT).show()
    }else{
        Toast.makeText(context, "Fail To Save", Toast.LENGTH_SHORT).show()
    }
}

