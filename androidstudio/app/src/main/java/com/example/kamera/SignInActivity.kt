package com.example.kamera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kamera.databinding.ActivitySignInBinding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//private const val BASEURL = "http://192.168.178.49:8000"
private const val BASEURL = "http://10.70.119.5:8000"
class SignInActivity : AppCompatActivity() {
    private lateinit var response : Response<SignInResponse>
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val apiManager = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .baseUrl(BASEURL)
            .build()
            .create(ApiManager::class.java)

        binding.signinButton.setOnClickListener {
            val user = binding.signinUser.text.toString()
            val pass = binding.signinPassword.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch {
                    val hashedPass = pass.toSHA256()
                    response = apiManager.signin(name = user, password = hashedPass)
                }.invokeOnCompletion{
                    if (response.code() != 200) {
                        Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
                    } else {
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