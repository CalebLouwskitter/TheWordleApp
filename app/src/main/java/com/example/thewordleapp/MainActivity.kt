package com.example.thewordleapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.util.concurrent.Executors
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var etLanguage: EditText
    private lateinit var etGuess: EditText
    private lateinit var btnStartGame: Button
    private lateinit var btnCheckGuess: Button
    private lateinit var tvResult: TextView

    private val executorService = Executors.newSingleThreadExecutor()

    val gson = Gson()
    val client = OkHttpClient()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        etLanguage = findViewById(R.id.etLanguage)
        etGuess = findViewById(R.id.etGuess)
        btnStartGame = findViewById(R.id.btnStartGame)
        btnCheckGuess = findViewById(R.id.btnCheckGuess)
        tvResult = findViewById(R.id.tvResult)

        btnStartGame.setOnClickListener { startGame() }

    }



    fun startGame() {
        val url = "https://localhost:7095/"
        val request = Request.Builder()
            .url(url)
            .post("".toRequestBody())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    // Use Gson to parse the response
                    val responseBody = response.body?.string()
                    val gameStartResponse = gson.fromJson(responseBody, GameStartResponse::class.java)

                    runOnUiThread {
                        tvResult.text = gameStartResponse?.message ?: "Game started! Try guessing the word."
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error: ${response.body?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to start game", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }



    fun checkGuess(userGuess: String) {
        // Validate the user's guess before sending to API
        if (userGuess.length != 5) {
            Toast.makeText(applicationContext, "Guess must be 5 letters.", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://localhost:7095/"

        // Create request body with the user guess
        val requestBody = userGuess.toRequestBody("text/plain".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    // Parse the response using Gson
                    val responseBody = response.body?.string()
                    val guessResponse = gson.fromJson(responseBody, GuessResponse::class.java)

                    runOnUiThread {
                        // Display the result on the UI
                        updateGuessResult(guessResponse)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error: ${response.body?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to check guess", Toast.LENGTH_SHORT).show()
                }
            }
        })


    }
    private fun updateGuessResult(response: GuessResponse?) {
        if (response == null) return

        tvResult.text = "Your guess: ${response.guess}"

    }

}





