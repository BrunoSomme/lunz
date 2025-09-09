package com.example.kamera

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.InputStream
import java.net.URL
import kotlin.collections.mutableListOf
import kotlin.concurrent.thread

private const val BASEURL = "http://10.0.2.2:8000"
class SecondActivity : AppCompatActivity() {
    private lateinit var BackButton: FloatingActionButton
    private val pictureItems = mutableListOf<ListData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val apiManager = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASEURL)        // diese Verbindung wird gebraucht um mit dem Localhost zu kommunizieren
            .build()
            .create(ApiManager::class.java)

        lifecycleScope.launch {
            val user_id = UIDDAta().getUID()
            val pictures = apiManager.getGallery(user_id)
            thread {
                for (i in pictures) {
                    val inputStream = URL("${BASEURL}/${i.result_url}").openStream()
                    setBitmap(inputStream)
                }

            }.join()
        }.invokeOnCompletion{
            val myListView = findViewById<ListView>(R.id.listview)
            myListView.adapter = ListAdapter(this, pictureItems)
        }

        BackButton = findViewById(R.id.BackButton)
        BackButton.setOnClickListener {
            val viewIntent = Intent(this, MainActivity::class.java)
            startActivity(viewIntent)
            finish()
        }
    }
    public fun setBitmap(newBitmap: InputStream) {
        val bitmap = BitmapFactory.decodeStream(newBitmap)
        this.pictureItems.add(ListData(bitmap))
    }
}