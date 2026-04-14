package tech.kaustubhdeshpande.sos.screens

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactPickerScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("sos_prefs", MODE_PRIVATE)

    // Coroutine scope for showing snackbars from callbacks
    val scope = rememberCoroutineScope()

    // Load saved contacts into a SnapshotStateList for UI
    val initialContacts = prefs.getStringSet("contacts", emptySet())?.toList() ?: emptyList()
    val contacts = remember {
        mutableStateListOf<String>().apply { addAll(initialContacts) }
    }

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher for system phone number picker (uses native Contacts app)
    val pickPhoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@rememberLauncherForActivityResult
            // Query the selected phone record for NUMBER column
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (numberIndex >= 0) {
                        val raw = c.getString(numberIndex) ?: return@use
                        val number = raw.replace("\\s".toRegex(), "") // normalize spaces

                        if (!contacts.contains(number)) {
                            contacts.add(number)
                            prefs.edit().putStringSet("contacts", contacts.toSet()).apply()
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Contact already added.")
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Launch native phone picker
                    val intent = Intent(
                        Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                    )
                    pickPhoneLauncher.launch(intent)
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                text = "Selected contacts",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (contacts.isEmpty()) {
                Text(
                    text = "Tap + to pick contacts from your phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(contacts, key = { it }) { contact ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .combinedClickable(
                                    onClick = { /* optional: show details */ },
                                    onLongClick = {
                                        // Remove on long press
                                        val removed = contacts.remove(contact)
                                        if (removed) {
                                            prefs.edit().putStringSet("contacts", contacts.toSet())
                                                .apply()
                                            // Feedback
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Removed $contact")
                                            }
                                        }
                                    }
                                ),
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = contact,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ContactPickerPreview() {
    ContactPickerScreen()
}