package com.leeweeder.timetable.ui.subjects

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.timetable.R
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.subject.Subject
import com.leeweeder.timetable.data.source.subject.SubjectWithDetails
import com.leeweeder.timetable.ui.components.IconButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubjectsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHomeScreenForSubjectEdit: (Int) -> Unit,
    viewModel: SubjectsScreenViewModel = koinViewModel()
) {
    val uiState by viewModel.subjectsWithDetails.collectAsStateWithLifecycle()

    SubjectsScreen(
        onNavigateBack = onNavigateBack,
        onEvent = viewModel::onEvent,
        uiState = uiState,
        onEditClick = onNavigateToHomeScreenForSubjectEdit
    )
}

/* TODO: Implement (if effective, try researching) the following:
*   1. Group or filter (choose the appropriate term) the subjects by timetable
*   2. Sort by dateAdded, subject code, subject name, and session count
*
* */

@Composable
private fun SubjectsScreen(
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onEvent: (SubjectsScreenEvent) -> Unit,
    uiState: SubjectsScreenUiState
) {
    Scaffold(
        topBar = {
            SubjectsScreenTopAppBar(onNavigateBack)
        }
    ) {
        when (uiState) {
            is SubjectsScreenUiState.Error -> {
                // TODO: Implement proper error showing here
            }

            SubjectsScreenUiState.Loading -> {
                LoadingScreen()
            }

            is SubjectsScreenUiState.Success -> {
                SubjectsList(
                    paddingValues = it,
                    onEditClick = onEditClick,
                    onDeleteClick = { subject, sessions ->
                        onEvent(SubjectsScreenEvent.DeleteSubject(subject = subject, sessions = sessions))
                    },
                    subjectsWithDetails = uiState.subjectsWithDetails
                )
            }
        }

    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectsScreenTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(title = {
        Text("Subjects")
    }, navigationIcon = {
        IconButton(
            R.drawable.arrow_back_24px,
            contentDescription = "Go back to Home screen",
            onClick = onNavigateBack
        )
    })
}

@Composable
private fun SubjectsList(
    paddingValues: PaddingValues,
    subjectsWithDetails: List<SubjectWithDetails>,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Subject, List<Session>) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(paddingValues)) {
        subjects(subjectsWithDetails, onEditClick = onEditClick, onDeleteClick = onDeleteClick)
    }
}

private fun LazyListScope.subjects(
    subjectsWithDetails: List<SubjectWithDetails>,
    onEditClick: (subjectId: Int) -> Unit,
    onDeleteClick: (Subject, List<Session>) -> Unit
) {
    items(subjectsWithDetails) { subjectWithDetails ->

        val subject = subjectWithDetails.subject

        SubjectItem(subjectWithDetails, onEditClick = {
            onEditClick(subject.id)
        }, onDeleteClick = {
            onDeleteClick(subject, subjectWithDetails.sessions)
        })
    }
}

@Composable
private fun SubjectItem(
    subjectWithDetails: SubjectWithDetails, onEditClick: () -> Unit, onDeleteClick: () -> Unit
) {
    ListItem(headlineContent = {
        Text(subjectWithDetails.subject.description)
    }, overlineContent = {
        Text(subjectWithDetails.subject.code)
    }, supportingContent = {
        Text(subjectWithDetails.instructor?.name ?: "No instructor")
    }, trailingContent = {
        TrailingContent(
            subjectWithDetails.sessions.count(),
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    })
}

@Composable
private fun TrailingContent(sessionCount: Int, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
    ) {
        Text("$sessionCount session${if (sessionCount == 1) "" else "s"}")
        MoreMenuBox(onEditClick = onEditClick, onDeleteClick = onDeleteClick)
    }
}

@Composable
private fun MoreMenuBox(onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Box {
        var expanded by remember { mutableStateOf(false) }

        IconButton(
            R.drawable.more_vert_24px, contentDescription = "More options"
        ) {
            expanded = true
        }

        MoreMenu(
            expanded = expanded, onDismissRequest = {
                expanded = false
            }, onEditClick = onEditClick, onDeleteClick = onDeleteClick
        )
    }
}

@Composable
private fun MoreMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        MoreDropdownItem("Edit", R.drawable.edit_24px, onEditClick)
        MoreDropdownItem("Delete", R.drawable.delete_24px, onDeleteClick)
    }
}

@Composable
private fun MoreDropdownItem(text: String, @DrawableRes iconId: Int, onClick: () -> Unit) {
    DropdownMenuItem(text = {
        Text(text)
    }, onClick = onClick, leadingIcon = {
        Icon(painter = painterResource(iconId), contentDescription = null)
    })
}