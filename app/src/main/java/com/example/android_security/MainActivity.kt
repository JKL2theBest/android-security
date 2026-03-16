package com.example.android_security

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- СЕТЕВОЙ СЛОЙ (Retrofit) ---
data class Post(val id: Int, val title: String, val body: String)

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}

object RetrofitClient {
    // ЭТОТ URL МЫ ПОТОМ НАЙДЕМ ЧЕРЕЗ APKLeaks!
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// --- VIEW MODEL ---
class MainViewModel : ViewModel() {
    var posts by mutableStateOf<List<Post>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun fetchPosts() {
        viewModelScope.launch {
            isLoading = true
            try {
                posts = RetrofitClient.api.getPosts().take(10) // Берем первые 10
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}

// --- MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainApp()
            }
        }
    }
}

// --- UI (COMPOSE) ---
@Composable
fun MainApp() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Профиль", "API Данные", "Обратная связь")
    val icons = listOf(Icons.Filled.Person, Icons.AutoMirrored.Filled.List, Icons.Filled.Email)
    val routes = listOf("profile", "api", "feedback")

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(routes[index]) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController, startDestination = "profile", Modifier.padding(paddingValues)) {
            composable("profile") { ProfileScreen() }
            composable("api") { ApiScreen() }
            composable("feedback") { FeedbackScreen() }
        }
    }
}

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Выполнил студент:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        // TODO: ВПИШИ СЮДА СВОЕ ФИО
        Text(text = "Суханкулиев Мухаммет", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Группа: N3346", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun ApiScreen(viewModel: MainViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.fetchPosts() }

    if (viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(viewModel.posts) { post ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = post.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = post.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackScreen() {
    var text by remember { mutableStateOf("") }
    var isSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Ваше сообщение") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { isSent = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отправить")
        }
        if (isSent) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Успешно отправлено!", color = MaterialTheme.colorScheme.primary)
        }
    }
}
