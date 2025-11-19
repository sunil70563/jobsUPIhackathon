package com.quickhire.candidate.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun VideoRecorderScreen(
    onVideoUploaded: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var recording by remember { mutableStateOf<Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    val videoCapture = remember {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .build()
        VideoCapture.withOutput(recorder)
    }

    // Permission Request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.CAMERA] == false) {
                Toast.makeText(context, "Camera permission required!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))
    }

    // Upload Handler (Picks video from gallery - still requires upload permissions)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadVideoToFirebase(context, it, onVideoUploaded) { loading -> isUploading = loading }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            videoCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Close Button
        Button(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
        ) {
            Text("X Close")
        }

        // 3. Recording Controls
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.White)
                Text("Uploading Video...", color = Color.White, modifier = Modifier.padding(top=8.dp))
            } else {
                // Record Button
                Button(
                    onClick = {
                        if (!isRecording) {
                            // START RECORDING
                            val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())

                            val contentValues = android.content.ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/QuickHire")
                            }

                            val mediaStoreOutputOptions = MediaStoreOutputOptions
                                .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                                .setContentValues(contentValues)
                                .build()

                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                val newRecording = videoCapture.output
                                    .prepareRecording(context, mediaStoreOutputOptions)
                                    .withAudioEnabled()
                                    .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                        if (recordEvent is VideoRecordEvent.Finalize) {
                                            if (!recordEvent.hasError()) {
                                                val uri = recordEvent.outputResults.outputUri
                                                uploadVideoToFirebase(context, uri, onVideoUploaded) { loading -> isUploading = loading }
                                            } else {
                                                Toast.makeText(context, "Error: ${recordEvent.error}", Toast.LENGTH_SHORT).show()
                                                isRecording = false
                                            }
                                        }
                                    }
                                recording = newRecording
                                isRecording = true
                            }
                        } else {
                            // STOP RECORDING
                            recording?.stop()
                            recording = null
                            isRecording = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else Color.White),
                    modifier = Modifier.size(80.dp).border(4.dp, Color.White, CircleShape),
                    shape = CircleShape
                ) {
                    if (isRecording) {
                        Box(modifier = Modifier.size(24.dp).background(Color.White, CircleShape))
                    } else {
                        Icon(Icons.Rounded.Videocam, "Record", tint = Color.Red, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Upload Button
                OutlinedButton(
                    onClick = { videoPickerLauncher.launch("video/*") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White)),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Or Upload Existing Video")
                }
            }
        }
    }
}

fun uploadVideoToFirebase(
    context: Context,
    fileUri: Uri,
    onSuccess: (String) -> Unit,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    val storageRef = FirebaseStorage.getInstance().reference
    val videoRef = storageRef.child("videos/${UUID.randomUUID()}.mp4")

    videoRef.putFile(fileUri)
        .addOnSuccessListener {
            videoRef.downloadUrl.addOnSuccessListener { uri ->
                setLoading(false)
                Toast.makeText(context, "Upload Complete! Saved URL.", Toast.LENGTH_LONG).show()
                onSuccess(uri.toString())
            }
        }
        .addOnFailureListener { e ->
            setLoading(false)
            Toast.makeText(context, "Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
}