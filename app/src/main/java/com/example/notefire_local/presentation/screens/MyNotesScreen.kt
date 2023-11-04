package com.example.notefire_local.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notefire_local.R
import com.example.notefire_local.presentation.NoteViewModel
import com.example.notefire_local.presentation.components.MySearchBar
import com.example.notefire_local.presentation.components.NoteListItem

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavController,
    noteViewModel: NoteViewModel = hiltViewModel()
) {
    val noteData = noteViewModel.noteData.observeAsState(listOf())
    val sortedNoteData = noteData.value.sortedByDescending { it.noteId }

    val focus = LocalFocusManager.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Notes", color = Color.White) },
                backgroundColor = colorResource(id = R.color.blue),
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colorResource(id = R.color.gray))
                    .padding(5.dp)
            ) {


                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MySearchBar(
                        modifier = Modifier.padding(5.dp),
                        hint = "Search...",
                        onSearch = {
                            noteViewModel.searchNote(it)
                        }
                    )
                    LazyVerticalGrid(
                        modifier = Modifier.weight(1f),
                        columns = GridCells.Fixed(2),
                        content = {
                            items(sortedNoteData) { note ->
                                NoteListItem(
                                    noteData = note, navController = navController
                                )
                            }
                        })
                }


                FloatingActionButton(
                    onClick = {
                        navController.navigate("add_note")
                        focus.clearFocus()
                    },
                    modifier = Modifier
                        .align(alignment = Alignment.BottomEnd)
                        .padding(20.dp)
                        .size(70.dp), backgroundColor = colorResource(id = R.color.blue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        })
}
