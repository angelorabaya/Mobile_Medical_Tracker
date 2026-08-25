package com.example.medtrack.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.medtrack.util.ImageUtils
import java.io.File

@Composable
fun ImageAttachmentPicker(
    imageUri: String?,
    onImageSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Attachment / Photo"
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var showFullImageDialog by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null) {
            val savedPath = ImageUtils.saveImageToInternalStorage(context, Uri.fromFile(tempCameraFile))
            if (savedPath != null) {
                onImageSelected(savedPath)
            } else {
                onImageSelected(tempCameraFile?.absolutePath)
            }
        }
    }

    val launchCamera = {
        val (file, uri) = ImageUtils.createImageFileUri(context)
        tempCameraFile = file
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        }
    }

    val onTakePhotoClick = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageUtils.saveImageToInternalStorage(context, uri)
            onImageSelected(savedPath ?: uri.toString())
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (imageUri != null && imageUri.isNotBlank()) {
            // Preview card with delete & view options
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .clickable { showFullImageDialog = true }
            ) {
                AsyncImage(
                    model = File(imageUri),
                    contentDescription = "Attached photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay gradient bar for actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onImageSelected(null) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Tap to expand indicator
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Tap to enlarge",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // Action buttons to pick or take image
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onTakePhotoClick() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Take Photo")
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("From File")
                }
            }
        }
    }

    // Full screen zoomable view dialog
    if (showFullImageDialog && imageUri != null) {
        ZoomableImageDialog(
            imagePath = imageUri,
            title = "Document Attachment",
            onDismiss = { showFullImageDialog = false }
        )
    }
}
