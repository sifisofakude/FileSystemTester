@file:Suppress("DEPRECATION")

package com.slambyte.tester.filesystem.ui.composable

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.sifisofakude.filesystem.FileOperation
import io.github.sifisofakude.filesystem.FileSystems
import io.github.sifisofakude.filesystem.android.AndroidSafFileSystem
import java.io.IOException

// --------------------------------------------------
// Picker purposes
// --------------------------------------------------

private enum class PickerPurpose {
    ROOT,
    HOME,
    SOURCE,
    DESTINATION
}

// --------------------------------------------------
// Main Screen
// --------------------------------------------------

@Composable
fun FileManagerScreen(
    selectedRoot: String?,
    message: String?,
    onAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Selected directory",
            style = MaterialTheme.typography.subtitle1
        )

        Text(
            selectedRoot ?: "No directory selected",
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        if (message != null) {
            Text(
                message,
                color = MaterialTheme.colors.error
            )
        }

        Button(onClick = { onAction("selectRoot") }) {
            Text("Select Root")
        }

        Button(
            onClick = { onAction("browse") },
            enabled = selectedRoot != null
        ) {
            Text("Browse Files")
        }

        Button(
            onClick = { onAction("createFolder") },
            enabled = selectedRoot != null
        ) {
            Text("Create Folder")
        }

        Button(
            onClick = { onAction("createFile") },
            enabled = selectedRoot != null
        ) {
            Text("Create File")
        }

        Button(onClick = { onAction("pick") }) {
            Text("Pick File")
        }

        Button(
            onClick = { onAction("delete") },
            enabled = selectedRoot != null
        ) {
            Text("Delete by Name")
        }

        Button(onClick = { onAction("copy") }) {
            Text("Copy File or Folder")
        }
    }
}

// --------------------------------------------------
// Name Input Screen
// --------------------------------------------------

@Composable
fun NameInputScreen(
    action: String,
    error: String? = null,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(action) {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            when (action) {
                "createFolder" ->
                    "Create folder in selected directory"

                "createFile" ->
                    "Create file in selected directory"

                "delete" ->
                    "Delete an item from selected directory"

                else -> action
            }
        )

        TextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text(
                    if (action == "delete") {
                        "File or folder name"
                    } else {
                        "Name"
                    }
                )
            },
            singleLine = true,
            isError = error != null
        )

        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colors.error
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Confirm")
            }

            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}

// --------------------------------------------------
// Directory Grid
// --------------------------------------------------

data class FileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridScreen(
    directory: String,
    items: List<FileEntry>,
    error: String?,
    onRefresh: () -> Unit,
    onUp: () -> Unit,
    onItemClick: (FileEntry) -> Unit,
    onDelete: (FileEntry) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Directory",
            style = MaterialTheme.typography.h6
        )

        Text(
            directory,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colors.error
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onUp) {
                Text("Up")
            }

            OutlinedButton(onClick = onRefresh) {
                Text("Refresh")
            }

            OutlinedButton(onClick = onBack) {
                Text("Home")
            }
        }

        if (items.isEmpty()) {
            Text(
                "This directory is empty or its contents could not be listed.",
                modifier = Modifier.padding(top = 12.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items.size) { index ->
                    val entry = items[index]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemClick(entry)
                            },
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (entry.isDirectory) {
                                    Icons.Default.Folder
                                } else {
                                    Icons.Default.InsertDriveFile
                                },
                                contentDescription = if (entry.isDirectory) {
                                    "Directory"
                                } else {
                                    "File"
                                }
                            )

                            Text(
                                entry.name,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            TextButton(
                                onClick = { onDelete(entry) }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// Copy Screen
// --------------------------------------------------

@Composable
fun CopyScreen(
    fromPath: String,
    toPath: String,
    error: String?,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onCopy: (String, String) -> Unit,
    onPickSource: () -> Unit,
    onPickDestination: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Copy a file or folder",
            style = MaterialTheme.typography.h6
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = fromPath,
                onValueChange = onFromChange,
                label = {
                    Text("Source path or URI")
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(onClick = onPickSource) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = "Pick source"
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = toPath,
                onValueChange = onToChange,
                label = {
                    Text("Destination path or URI")
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(onClick = onPickDestination) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = "Pick destination"
                )
            }
        }

        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colors.error
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    onCopy(fromPath.trim(), toPath.trim())
                },
                enabled = fromPath.isNotBlank() &&
                    toPath.isNotBlank()
            ) {
                Text("Copy")
            }

            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }

        Text(
            "The destination does not need to exist. Its type must " +
                "be compatible with the source. Existing items require confirmation.",
            style = MaterialTheme.typography.caption
        )
    }
}

// --------------------------------------------------
// Pick Option Dialog
// --------------------------------------------------

@Composable
fun PickOptionDialog(
    onDismiss: () -> Unit,
    onPickFile: () -> Unit,
    onPickFolder: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select what to pick",
                    style = MaterialTheme.typography.h6
                )

                Button(onClick = onPickFile) {
                    Text("Pick File")
                }

                Button(onClick = onPickFolder) {
                    Text("Pick Folder")
                }

                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    }
}

// --------------------------------------------------
// Overwrite Confirmation
// --------------------------------------------------

@Composable
fun OverwriteConfirmScreen(
    itemName: String,
    isFolder: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Overwrite existing item?")
        },
        text = {
            Text(
                "A ${if (isFolder) "folder" else "file"} " +
                    "named \"$itemName\" already exists. Replace it?"
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Overwrite")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

// --------------------------------------------------
// Main Application
// --------------------------------------------------

@Composable
fun FileManagerApp(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    val fileSystem = remember {
        FileSystems.current as AndroidSafFileSystem
    }

    var selectedRoot by remember {
        mutableStateOf<String?>(null)
    }

    var currentDirectory by remember {
        mutableStateOf<String?>(null)
    }

    val directoryStack = remember {
        mutableStateListOf<String>()
    }

    var fromPath by remember {
        mutableStateOf("")
    }

    var toPath by remember {
        mutableStateOf("")
    }

    var inputError by remember {
        mutableStateOf<String?>(null)
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    var copyError by remember {
        mutableStateOf<String?>(null)
    }

    var gridError by remember {
        mutableStateOf<String?>(null)
    }

    var gridItems by remember {
        mutableStateOf<List<FileEntry>>(emptyList())
    }

    var showPickDialog by remember {
        mutableStateOf(false)
    }

    var showOverwrite by remember {
        mutableStateOf(false)
    }

    var pickerPurpose by remember {
        mutableStateOf(PickerPurpose.ROOT)
    }

    var pendingOverwrite by remember {
        mutableStateOf<Pair<String, String>?>(null)
    }

    var pendingOverwriteIsFolder by remember {
        mutableStateOf(false)
    }

    // --------------------------------------------------
    // URI Permissions
    // --------------------------------------------------

    fun rememberUriPermission(uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // The provider may grant only temporary access.
        } catch (_: IllegalArgumentException) {
            // The URI may not support persistable permissions.
        }
    }

    // --------------------------------------------------
    // Refresh Directory
    // --------------------------------------------------

    fun refreshGrid(path: String) {
        try {
            val directory = FileOperation(path)

            if (!directory.exists() || !directory.isDirectory()) {
                throw IOException(
                    "Not a directory or access denied: $path"
                )
            }

            gridItems = directory.listFiles()
                .map { source ->
                    FileEntry(
                        name = source.name,
                        path = source.path,
                        isDirectory = source.isDirectory()
                    )
                }
                .sortedWith(
                    compareBy<FileEntry>(
                        { !it.isDirectory },
                        { it.name.lowercase() }
                    )
                )

            gridError = null
        } catch (e: Exception) {
            gridItems = emptyList()
            gridError = e.message
                ?: "Unable to list this directory."
        }
    }

    // --------------------------------------------------
    // Select Root
    // --------------------------------------------------

    fun selectRoot(uri: Uri) {
        fileSystem.changeSelectedDirectory(uri)

        val path = uri.toString()

        selectedRoot = path
        currentDirectory = path

        directoryStack.clear()
        directoryStack.add(path)

        refreshGrid(path)

        message = "Root directory selected."
    }

    // --------------------------------------------------
    // Folder Picker
    // --------------------------------------------------

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            rememberUriPermission(uri)

            when (pickerPurpose) {
                PickerPurpose.ROOT -> {
                    selectRoot(uri)
                }

                PickerPurpose.HOME -> {
                    fromPath = uri.toString()
                    message = "Folder selected."
                }

                PickerPurpose.SOURCE -> {
                    fromPath = uri.toString()
                }

                PickerPurpose.DESTINATION -> {
                    toPath = uri.toString()
                }
            }
        }
    }

    // --------------------------------------------------
    // File Picker
    // --------------------------------------------------

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            rememberUriPermission(uri)

            when (pickerPurpose) {
                PickerPurpose.SOURCE -> {
                    fromPath = uri.toString()
                }

                PickerPurpose.HOME -> {
                    fromPath = uri.toString()
                    message = "File selected."
                }

                PickerPurpose.DESTINATION -> {
                    toPath = uri.toString()
                }

                PickerPurpose.ROOT -> {
                    fromPath = uri.toString()
                }
            }
        }
    }

    // --------------------------------------------------
    // Launch Pickers
    // --------------------------------------------------

    fun launchFolderPicker(purpose: PickerPurpose) {
        pickerPurpose = purpose
        folderLauncher.launch(null)
    }

    fun launchFilePicker(purpose: PickerPurpose) {
        pickerPurpose = purpose
        fileLauncher.launch(arrayOf("*/*"))
    }

    // --------------------------------------------------
    // Navigation
    // --------------------------------------------------

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // --------------------------------------------------
        // Main Screen
        // --------------------------------------------------

        composable("main") {
            FileManagerScreen(
                selectedRoot = selectedRoot,
                message = message
            ) { action ->
                message = null
                inputError = null

                when (action) {
                    "selectRoot" -> {
                        launchFolderPicker(PickerPurpose.ROOT)
                    }

                    "browse" -> {
                        val directory =
                            currentDirectory ?: selectedRoot

                        if (directory != null) {
                            refreshGrid(directory)
                            navController.navigate("grid")
                        }
                    }

                    "createFolder",
                    "createFile",
                    "delete" -> {
                        navController.navigate("nameInput/$action")
                    }

                    "pick" -> {
                        pickerPurpose = PickerPurpose.HOME
                        showPickDialog = true
                    }

                    "copy" -> {
                        copyError = null
                        navController.navigate("copy")
                    }
                }
            }

            if (showPickDialog) {
                PickOptionDialog(
                    onDismiss = {
                        showPickDialog = false
                    },
                    onPickFile = {
                        showPickDialog = false
                        launchFilePicker(PickerPurpose.HOME)
                    },
                    onPickFolder = {
                        showPickDialog = false
                        launchFolderPicker(PickerPurpose.HOME)
                    }
                )
            }
        }

        // --------------------------------------------------
        // Create / Delete by Name
        // --------------------------------------------------

        composable("nameInput/{action}") { backStackEntry ->
            val action = backStackEntry.arguments
                ?.getString("action") ?: ""

            NameInputScreen(
                action = action,
                error = inputError,
                onConfirm = { rawName ->
                    try {
                        val parent = currentDirectory
                            ?: selectedRoot
                            ?: throw IOException("No root selected.")

                        val name = rawName.trim()

                        require(name.isNotBlank()) {
                            "Name cannot be empty."
                        }

                        require(
                            '/' !in name &&
                                '\\' !in name &&
                                name != "." &&
                                name != ".."
                        ) {
                            "Enter a single file or folder name."
                        }

                        val targetPath = fileSystem.combinePath(
                            parent,
                            name
                        )

                        val target = FileOperation(targetPath)

                        when (action) {
                            "createFolder" -> {
                                if (target.exists()) {
                                    throw IOException(
                                        "An item named '$name' already exists."
                                    )
                                }

                                if (!target.mkdirs()) {
                                    throw IOException(
                                        "Could not create folder: $name"
                                    )
                                }

                                message = "Folder created: $name"
                            }

                            "createFile" -> {
                                if (target.exists()) {
                                    throw IOException(
                                        "An item named '$name' already exists."
                                    )
                                }

                                if (!target.createNewFile()) {
                                    throw IOException(
                                        "Could not create file: $name"
                                    )
                                }

                                message = "File created: $name"
                            }

                            "delete" -> {
                                if (!target.exists()) {
                                    throw IOException(
                                        "Item not found: $name"
                                    )
                                }

                                if (!target.delete()) {
                                    throw IOException(
                                        "Could not delete: $name"
                                    )
                                }

                                message = "Deleted: $name"
                            }

                            else -> {
                                throw IOException(
                                    "Unknown action: $action"
                                )
                            }
                        }

                        refreshGrid(parent)
                        navController.popBackStack()
                    } catch (e: Exception) {
                        inputError = e.message
                            ?: "The operation failed."
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // --------------------------------------------------
        // Browse Directory
        // --------------------------------------------------

        composable("grid") {
            val directory = currentDirectory ?: selectedRoot

            if (directory == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                GridScreen(
                    directory = directory,
                    items = gridItems,
                    error = gridError,
                    onRefresh = {
                        refreshGrid(directory)
                    },
                    onUp = {
                        if (directoryStack.size > 1) {
                            directoryStack.removeAt(
                                directoryStack.lastIndex
                            )

                            val parent = directoryStack.last()

                            currentDirectory = parent
                            refreshGrid(parent)
                        }
                    },
                    onItemClick = { entry ->
                        if (entry.isDirectory) {
                            directoryStack.add(entry.path)
                            currentDirectory = entry.path
                            refreshGrid(entry.path)
                        } else {
                            message = "Selected file: ${entry.name}"
                            fromPath = entry.path
                        }
                    },
                    onDelete = { entry ->
                        try {
                            val operation = FileOperation(entry.path)

                            if (!operation.delete()) {
                                throw IOException(
                                    "Could not delete ${entry.name}"
                                )
                            }

                            refreshGrid(directory)
                        } catch (e: Exception) {
                            gridError = e.message
                                ?: "Delete failed."
                        }
                    },
                    onBack = {
                        navController.popBackStack("main", false)
                    }
                )
            }
        }

        // --------------------------------------------------
        // Copy File or Directory
        // --------------------------------------------------

        composable("copy") {
            CopyScreen(
                fromPath = fromPath,
                toPath = toPath,
                error = copyError,
                onFromChange = {
                    fromPath = it
                },
                onToChange = {
                    toPath = it
                },
                onPickSource = {
                    pickerPurpose = PickerPurpose.SOURCE
                    showPickDialog = true
                },
                onPickDestination = {
                    launchFolderPicker(PickerPurpose.DESTINATION)
                },
                onCancel = {
                    navController.popBackStack()
                },
                onCopy = { sourcePath, destinationPath ->
                    try {
                        val source = FileOperation(sourcePath)

                        // The source must exist, but it can be
                        // either a file or a directory.
                        if (!source.exists()) {
                            throw IOException(
                                "Source does not exist."
                            )
                        }

                        val destination =
                            FileOperation(destinationPath)

                        if (destination.exists()) {
                            // An existing destination must have
                            // the same type as the source.
                            if (
                                source.isDirectory() &&
                                destination.isFile()
                            ) {
                                throw IOException(
                                    "Source and destination types do not match."
                                )
                            }

                            // The destination exists, so ask
                            // before overwriting it.
                            pendingOverwrite =
                                sourcePath to destinationPath

                            pendingOverwriteIsFolder =
                                source.isDirectory()

                            showOverwrite = true
                        } else {
                            // Pass the exact destination path.
                            // It does not need to exist.
                            // Let the filesystem resolve it.
                            val result = source.copy(destinationPath)

                            if (result == null) {
                                throw IOException("Copy failed.")
                            }

                            copyError = null
                            message = "Copy completed."
                            navController.popBackStack()
                        }
                    } catch (e: Exception) {
                        copyError = e.message ?: "Copy failed."
                    }
                }
            )

            // --------------------------------------------------
            // Source Picker Dialog
            // --------------------------------------------------

            if (showPickDialog) {
                PickOptionDialog(
                    onDismiss = {
                        showPickDialog = false
                    },
                    onPickFile = {
                        showPickDialog = false
                        launchFilePicker(PickerPurpose.SOURCE)
                    },
                    onPickFolder = {
                        showPickDialog = false
                        launchFolderPicker(PickerPurpose.SOURCE)
                    }
                )
            }

            // --------------------------------------------------
            // Overwrite Confirmation
            // --------------------------------------------------

            if (showOverwrite) {
                val pending = pendingOverwrite

                OverwriteConfirmScreen(
                    itemName = pending?.second
                        ?.substringAfterLast('/')
                        ?: "item",
                    isFolder = pendingOverwriteIsFolder,
                    onConfirm = {
                        try {
                            val pair = pending
                                ?: throw IOException(
                                    "No pending copy operation."
                                )

                            val source = FileOperation(pair.first)
                            val destination = FileOperation(pair.second)

                            if (!source.exists()) {
                                throw IOException(
                                    "Source does not exist."
                                )
                            }

                            // Recheck the destination in case
                            // it changed while the dialog was open.
                            if (destination.exists()) {
                                if (
                                    source.isDirectory() &&
                                    destination.isFile()
                                ) {
                                    throw IOException(
                                        "Source and destination types do not match."
                                    )
                                }
                            }

                            // Overwrite the exact destination.
                            // Do not manually delete it first.
                            val result = source.copy(
                                destination,
                                true
                            )

                            if (result == null) {
                                throw IOException("Overwrite failed.")
                            }

                            copyError = null
                            message = "Copy completed."
                            showOverwrite = false
                            pendingOverwrite = null

                            navController.popBackStack()
                        } catch (e: Exception) {
                            copyError = e.message
                                ?: "Overwrite failed."

                            showOverwrite = false
                            pendingOverwrite = null
                        }
                    },
                    onCancel = {
                        showOverwrite = false
                        pendingOverwrite = null
                    }
                )
            }
        }
    }
}
