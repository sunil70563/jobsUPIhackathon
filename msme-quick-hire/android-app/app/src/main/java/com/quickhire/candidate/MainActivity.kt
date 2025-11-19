package com.quickhire.candidate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentChange
import com.quickhire.candidate.screens.JobAlertListener
import com.quickhire.candidate.screens.VoiceOnboardingScreen
import com.quickhire.candidate.screens.HiredScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val db = FirebaseFirestore.getInstance()
                    val auth = Firebase.auth

                    var isAuthenticated by remember { mutableStateOf(false) }

                    var isHired by remember { mutableStateOf(false) }
                    var hiredRole by remember { mutableStateOf("") }
                    var employerName by remember { mutableStateOf("") }
                    var salaryOffer by remember { mutableStateOf("₹15,000/mo") }

                    // 1. ANONYMOUS AUTHENTICATION FIX (Runs once on start)
                    LaunchedEffect(Unit) {
                        auth.signInAnonymously()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    isAuthenticated = true
                                } else {
                                    Toast.makeText(context, "Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }

                    // 2. Global Listener for Urgent Jobs (Background)
                    JobAlertListener()

                    // 3. Listener for "Hired" Status
                    LaunchedEffect(Unit) {
                        db.collection("personas")
                            .whereEqualTo("status", "HIRED")
                            .addSnapshotListener { snapshots, e ->
                                if (e != null) return@addSnapshotListener

                                if (snapshots != null) {
                                    for (dc in snapshots.documentChanges) {
                                        // Only trigger if the document was MODIFIED (changed to Hired)
                                        if (dc.type == DocumentChange.Type.MODIFIED) {
                                            val data = dc.document.data
                                            hiredRole = data["role"] as? String ?: "Staff"
                                            employerName = data["hiredBy"] as? String ?: "A Shop Owner"
                                            isHired = true
                                        }
                                    }
                                }
                            }
                    }

                    // 4. Conditional Navigation
                    if (isAuthenticated) {
                        if (isHired) {
                            HiredScreen(
                                role = hiredRole,
                                employerName = employerName,
                                salary = salaryOffer,
                                onClose = { isHired = false }
                            )
                        } else {
                            VoiceOnboardingScreen()
                        }
                    } else {
                        // Show a loading screen until Auth succeeds
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Text("Connecting to server...", modifier = Modifier.padding(top = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}