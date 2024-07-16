package com.example.timemanagement

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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
import java.io.File

@Composable
fun PhotoPickerScreenGeminiHomework(
    systemInstructions: String,
    viewModel: SimpleChatViewModelGemini = viewModel(factory = SimpleChatViewModelFactoryGemini(Constants.apiKeyImage, "gemini-1.5-flash-001", systemInstructions))
) {
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var textFieldValue by rememberSaveable { mutableStateOf("") }
    var buttonClicked by remember { mutableStateOf(false) }
    val response by viewModel.response.collectAsState()
    val context = LocalContext.current

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


        // Outlined Text Field for prompt input
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue -> textFieldValue = newValue },
            label = { Text(text = "Enter Prompt") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedImageUri?.let { uri ->
                    val bitmap = uriToBitmap(context, uri)
                    bitmap?.let {
                        val prompt = textFieldValue
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
        if(buttonClicked && response.isNotEmpty()){
            ResponseSection(response)
        }else if(buttonClicked){
            ResponseSection("Loading...")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ResponseSection(response: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "Response:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = response,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PhotoPickerScreenGeminiQuiz(
    systemInstructions: String,
    viewModel: SimpleChatViewModelGemini = viewModel(factory = SimpleChatViewModelFactoryGemini(Constants.apiKeyImage, "gemini-1.5-flash-001", systemInstructions))
) {
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var textFieldValue by rememberSaveable { mutableStateOf("") }
    var buttonClicked by remember { mutableStateOf(false) }
    var titleValue by rememberSaveable { mutableStateOf("") }
    var questionCount by rememberSaveable { mutableStateOf("1") }
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
                val quizData = QuizData("", response, titleValue, questionCount)
                val docRef = firestore.collection("users").document(it.uid).collection("quizzes").document()
                quizData.documentId = docRef.id
                docRef.set(quizData)
                    .addOnSuccessListener {
                        firebaseSent = true
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Error adding quiz", e)
                    }
            }
            Text(text = "Quiz Is Made")
        }

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


@Composable
fun RequestCameraPermission(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            onPermissionResult(isGranted)
        }
    )

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted
                onPermissionResult(true)
            }
            else -> {
                // Request permission
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}