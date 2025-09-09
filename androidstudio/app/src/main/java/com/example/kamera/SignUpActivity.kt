package com.example.kamera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kamera.databinding.ActivitySignUpBinding
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private const val BASEURL = "http://10.0.2.2:8000"

class SignUpActivity : AppCompatActivity() {
    private lateinit var response : Response<SignUpResponse>
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val apiManager = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .baseUrl(BASEURL)
            .build()
            .create(ApiManager::class.java)

        binding.loginRedirectText.setOnClickListener {
            val viewIntent = Intent(this, SignInActivity::class.java)
            startActivity(viewIntent)
            finish()
        }

        binding.signupButton.setOnClickListener {
            val user = binding.signupUser.text.toString()
            val pass = binding.signupPassword.text.toString()
            if (user.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch {
                    response = apiManager.signup(username = user, password = pass)
                }.invokeOnCompletion{
                    if (response.code() != 200) {
                        Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.println(Log.DEBUG, "Mango", "${response.body()?.user_id}")
                        UIDDAta().setUID(response.body()?.user_id)
                        val viewIntent = Intent(this, MainActivity::class.java)
                        startActivity(viewIntent)
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "User or Password is missing", Toast.LENGTH_SHORT).show()
            }
        }
    }
}