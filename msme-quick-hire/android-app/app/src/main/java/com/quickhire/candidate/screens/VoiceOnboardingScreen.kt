package com.quickhire.candidate.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickhire.candidate.network.GeminiClient
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore

// Global variable to store the ID of the created profile
var currentPersonaId: String? = null

@Composable
fun VoiceOnboardingScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    // State variables
    var spokenText by remember { mutableStateOf("Tap the Mic and speak...") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var parsedRole by remember { mutableStateOf<String?>(null) }
    var parsedSkills by remember { mutableStateOf<List<String>>(emptyList()) }

    // Video State and Navigation
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    // If Camera is active, show it fullscreen instead of the Voice UI
    if (showCamera) {
        VideoRecorderScreen(
            onVideoUploaded = { url ->
                videoUrl = url
                showCamera = false // Close camera after upload
                Toast.makeText(context, "Video Attached!", Toast.LENGTH_SHORT).show()
            },
            onClose = {
                showCamera = false // Allows user to exit camera view
            }
        )
        return
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = results?.get(0)

        if (text != null) {
            spokenText = text
            isAnalyzing = true
            Toast.makeText(context, "Processing...", Toast.LENGTH_SHORT).show()

            scope.launch {
                val resultString = GeminiClient.parseVoiceInput(text)

                if (resultString.trim().startsWith("{")) {
                    try {
                        val json = JSONObject(resultString)
                        parsedRole = json.optString("role", "Unknown")

                        val skillsArray = json.optJSONArray("skills")
                        val skillsList = mutableListOf<String>()
                        if (skillsArray != null) {
                            for (i in 0 until skillsArray.length()) {
                                skillsList.add(skillsArray.getString(i))
                            }
                        }
                        parsedSkills = skillsList
                        Toast.makeText(context, "Profile Generated!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Parse Error", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "AI Error: $resultString", Toast.LENGTH_LONG).show()
                }
                isAnalyzing = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Voice Setup", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tell us what work you do.", color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        // Mic Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(if (isAnalyzing) Color.Gray else Color(0xFF1E88E5))
                .clickable(enabled = !isAnalyzing && !isSaving) {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now (e.g., 'I am a driver')")
                    }
                    try {
                        speechLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Mic Error", Toast.LENGTH_SHORT).show()
                    }
                }
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Mic",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = spokenText, style = MaterialTheme.typography.bodyLarge)

        // Result Card
        if (parsedRole != null) {
            Spacer(modifier = Modifier.height(32.dp))

            // Video Vibe Check Button
            OutlinedButton(
                onClick = { showCamera = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (videoUrl != null) Color(0xFF2E7D32) else Color(0xFFD81B60)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (videoUrl != null) Color(0xFF2E7D32) else Color(0xFFD81B60))
                )
            ) {
                Text(if (videoUrl != null) "✅ Video Recorded" else "📹 Record or Upload Video Intro")
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AI Detected Profile:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Role: $parsedRole", fontSize = 18.sp, color = Color(0xFF1565C0))
                    Text("Skills: ${parsedSkills.joinToString(", ")}")

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (videoUrl == null) {
                                Toast.makeText(context, "Please record a video intro first!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSaving = true
                            val userId = "user_${UUID.randomUUID().toString().take(5)}"

                            val personaMap = hashMapOf(
                                "personaId" to "p_${System.currentTimeMillis()}",
                                "userId" to userId,
                                "role" to parsedRole,
                                "skills" to parsedSkills,
                                "isActive" to true,
                                "status" to "AVAILABLE",
                                "videoIntro" to videoUrl, // Save the Video URL
                                "location" to hashMapOf(
                                    "lat" to 12.9716 + (Math.random() * 0.01),
                                    "lng" to 77.5946 + (Math.random() * 0.01),
                                    "address" to "Bangalore"
                                )
                            )

                            db.collection("personas")
                                .add(personaMap)
                                .addOnSuccessListener { docRef ->
                                    isSaving = false
                                    currentPersonaId = docRef.id
                                    Toast.makeText(context, "Saved to Live Map! 🌍", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        },
                        enabled = !isSaving && videoUrl != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaving) Color.Gray else Color(0xFF1E88E5)
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...")
                        } else {
                            Text("Confirm & Go Live")
                        }
                    }
                }
            }
        }
    }
}