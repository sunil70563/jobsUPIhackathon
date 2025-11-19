package com.quickhire.candidate.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun JobAlertListener() {
    val context = LocalContext.current
    val db = Firebase.firestore

    // 1. Permission Launcher (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                println("Notification Permission Granted")
            }
        }
    )

    // 2. Request Permission on Launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Create Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "urgent_jobs",
                "Urgent Job Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for high-priority jobs nearby"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 3. Listen for Firestore Updates
        db.collection("broadcasts")
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        // Trigger ONLY for newly added broadcasts
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val job = dc.document.data
                            val role = job["role"] as? String ?: "Worker"
                            val wage = job["wage"] as? String ?: "Standard"

                            // Show Notification
                            showNotification(context, role, wage)
                        }
                    }
                }
            }
    }
}

fun showNotification(context: Context, role: String, wage: String) {
    // Double-check permission before showing
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    val notificationManager = NotificationManagerCompat.from(context)

    val builder = NotificationCompat.Builder(context, "urgent_jobs")
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("🚨 URGENT JOB NEARBY!")
        .setContentText("Wanted: $role. Pay: $wage/hr. Tap to Apply!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound + Vibrate
        .setAutoCancel(true)

    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
}