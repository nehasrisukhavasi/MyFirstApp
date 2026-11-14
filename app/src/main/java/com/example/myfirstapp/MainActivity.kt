package com.example.myfirstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ToDoApp()
            }
        }
    }
}

data class Task(
    val id: Int,
    val title: String,
    val category: String,
    var completed: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoApp() {
    val tasks = remember { mutableStateListOf<Task>() }
    var screen by rememberSaveable { mutableStateOf("Home") }
    var nextId by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (screen == "Home") "My To-Do App"
                        else "Add New Task"
                    )
                },
                navigationIcon = {
                    if (screen == "Add") {
                        IconButton(onClick = { screen = "Home" }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (screen == "Home") {
                FloatingActionButton(
                    onClick = { screen = "Add" }
                ) {
                    Icon(Icons.Default.Add, "Add Task")
                }
            }
        }
    ) { padding ->
        if (screen == "Home") {
            HomeScreen(
                tasks = tasks,
                modifier = Modifier.padding(padding),
                onDelete = { tasks.remove(it) },
                onToggle = {
                    it.completed = !it.completed
                }
            )
        } else {
            AddTaskScreen(
                modifier = Modifier.padding(padding),
                onSave = { title, category ->
                    tasks.add(
                        Task(
                            id = nextId++,
                            title = title,
                            category = category
                        )
                    )
                    screen = "Home"
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    onDelete: (Task) -> Unit,
    onToggle: (Task) -> Unit
) {
    var selectedCategory by rememberSaveable {
        mutableStateOf("All")
    }

    val categories = listOf(
        "All", "Personal", "College", "Work"
    )

    val filteredTasks = if (selectedCategory == "All") {
        tasks
    } else {
        tasks.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Organize Your Day!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Total Tasks: ${tasks.size}")

        Text("Completed: ${tasks.count { it.completed }}")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Categories",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredTasks.isEmpty()) {
            Text(
                text = "No tasks yet! Tap + to add a task.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredTasks,
                    key = { it.id }
                ) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { onToggle(task) },
                        onDelete = { onDelete(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() }
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.completed) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )

                Text(
                    text = task.category,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun AddTaskScreen(
    modifier: Modifier = Modifier,
    onSave: (String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable {
        mutableStateOf("Personal")
    }
    var error by rememberSaveable { mutableStateOf(false) }

    val categories = listOf(
        "Personal", "College", "Work"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Enter Task Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                error = false
            },
            label = { Text("Task Name") },
            placeholder = { Text("Enter your task") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error) {
            Text(
                text = "Task name cannot be empty!",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Select Category",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        categories.forEach { item ->
            FilterChip(
                selected = category == item,
                onClick = { category = item },
                label = { Text(item) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (title.isBlank()) {
                    error = true
                } else {
                    onSave(title.trim(), category)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Task")
        }
    }
}