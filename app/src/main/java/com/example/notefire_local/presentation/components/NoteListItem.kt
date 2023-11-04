package com.example.notefire_local.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notefire_local.R
import com.example.notefire_local.common.NavParam
import com.example.notefire_local.common.convertDate
import com.example.notefire_local.common.navigateTo
import com.example.notefire_local.data.local.NotesEntity
import com.example.notefire_local.presentation.NoteViewModel
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListItem(
    navController: NavController,
    noteData: NotesEntity,
    noteViewModel: NoteViewModel = hiltViewModel()
) {
    var longClick by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    Card(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)
        .combinedClickable(
            onClick = {
                navigateTo(navController, "edit_note", NavParam("noteData", noteData))
                focus.clearFocus()
            },
            onLongClick = {
                longClick = !longClick
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(150.dp)
                .padding(5.dp)
        ) {

            Text(
                text = convertDate(noteData.date, "dd MMMM  HH:mm"),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = noteData.noteTitle.toString(),
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontSize = 18.sp
            )
            Text(
                text = noteData.noteDescription.toString(),
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray
            )

        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (longClick) {
                Icon(
                    modifier = Modifier.clickable {
                        noteViewModel.deleteNote(noteData.noteId!!)
                    },
                    imageVector = Icons.Default.Delete,
                    contentDescription = "",
                    tint = colorResource(
                        id = R.color.blue,
                    )
                )
            }
        }
    }


}

