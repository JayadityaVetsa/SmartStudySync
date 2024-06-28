package com.example.timemanagement

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun PhotoPickerScreenGemini(
//    navController: NavController,
    systemInstructions: String,
    viewModel: SimpleChatViewModelGemini = viewModel(factory = SimpleChatViewModelFactoryGemini(Constants.apiKeyImage, "gemini-1.5-flash-001", systemInstructions))
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var textFieldValue by remember { mutableStateOf("") }
    var buttonClicked by remember { mutableStateOf(false) }
    var titleValue by remember { mutableStateOf("") }
    var questionCount by remember { mutableStateOf("1") }
    val response by viewModel.response.collectAsState()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Register the image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the selected image or a placeholder
        if (selectedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(model = selectedImageUri),
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No Image Selected", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ) {
            Text(text = "Select Image")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Outlined Text Field for title input
        OutlinedTextField(
            value = titleValue,
            onValueChange = { newValue -> titleValue = newValue },
            label = { Text(text = "Enter Title") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Outlined Text Field for topic input
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue -> textFieldValue = newValue },
            label = { Text(text = "Enter Topic") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Outlined Text Field for question count input
        OutlinedTextField(
            value = questionCount,
            onValueChange = { newValue -> questionCount = newValue },
            label = { Text(text = "Number of Questions") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedImageUri?.let { uri ->
                    val bitmap = uriToBitmap(context, uri)
                    bitmap?.let {
                        val prompt = "$textFieldValue, Number of Questions: $questionCount"
                        viewModel.generateContentWithImage(it, prompt)
                    }
                }
                buttonClicked = true

            },
            shape = RoundedCornerShape(8.dp),
            enabled = !buttonClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ) {
            Text(text = "Generate Content")
        }

        Spacer(modifier = Modifier.height(32.dp))

//        SelectionContainer {
//            Text(text = "Response: $response")
//        }
        if (response.isNotEmpty() && titleValue.isNotEmpty() && buttonClicked) {
            val currentUser = auth.currentUser
            currentUser?.let {
                val quizData = QuizData(response, titleValue, questionCount)
                firestore.collection("users").document(it.uid).collection("quizzes")
                    .add(quizData)
                    .addOnSuccessListener {
                        firebaseSent = true
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Error adding quiz", e)
                    }
            }
            Text(text = "Quiz Is Made")
        }
//        if(firebaseSent){
//            Button(
//                shape = RoundedCornerShape(8.dp),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 40.dp),
//                onClick = { navController.navigate(Routes.AutomaticQuizMakerPage) }
//            ) {
//                Text(text = "Go to Quiz")
//            }
//        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

class SimpleChatViewModelFactoryGemini(
    private val apiKey: String,
    private val model: String,
    private val systemInstructions: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimpleChatViewModelGemini::class.java)) {
            val generativeModel : GenerativeModel = GenerativeModel(
                modelName = model,
                apiKey = apiKey,
                systemInstruction = content { text(systemInstructions) },
            )
            return SimpleChatViewModelGemini(generativeModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SimpleChatViewModelGemini(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _response: MutableStateFlow<String> = MutableStateFlow("")
    val response: StateFlow<String> = _response

    fun generateContentWithImage(bitmap: Bitmap, prompt: String) {
        viewModelScope.launch {
            _response.value = ""
            try {
                val content = content {
                    image(bitmap)
                    text(prompt)
                }
                val modelResponse = generativeModel.generateContent(content).text ?: "No response"
                _response.value = modelResponse
            } catch (e: Exception) {
                _response.value = "Error: ${e.localizedMessage}"
            }
        }
    }
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

