package com.example.kamera

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.BufferedOutputStream
import java.io.File
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL
import kotlin.concurrent.thread


private const val FILE_NAME ="photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File

class MainActivity : AppCompatActivity() {

    private lateinit var btnTakePicture: Button
    private lateinit var GalleryButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var UploadButton: FloatingActionButton

    private var safedBool: Boolean = false
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val apiManager = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl("http://10.0.2.2:8000")        // diese Verbindung wird gebraucht um mit dem Localhost zu kommunizieren
        .build()
        .create(ApiManager::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hier der Code für den Button der das Bild macht
        btnTakePicture = findViewById(R.id.btnTakePicture)
        btnTakePicture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider = FileProvider.getUriForFile(this, "com.example.kamera.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            startActivityForResult(takePictureIntent, REQUEST_CODE)
        }

        //Hier der Code für den Button der sich mit der GalleryOption befasst
        GalleryButton = findViewById(R.id.GalleryButton)
        GalleryButton.setOnClickListener {
            if (!safedBool) {
                var builder = AlertDialog.Builder(this)
                builder.setTitle("Soll dieses Bild gespeichert werden")
                builder.setPositiveButton("Nein") { dialog, which ->
                    Toast.makeText(
                        applicationContext,
                        "Bild wurde nicht gespeichert",
                        Toast.LENGTH_SHORT
                    ).show()
                    val viewIntent = Intent(this, SecondActivity::class.java)
                    startActivity(viewIntent)
                    finish()
                }
                builder.setNegativeButton("Ja") { dialog, which ->
                    // hier dann der Api call hin wo geuploaded wird
                }
                builder.show()
                true
            } else {
                val viewIntent = Intent(this, SecondActivity::class.java)
                startActivity(viewIntent)
                finish()
            }
        }

        // hier der Code der sich mit dem Upload button befasst
        UploadButton = findViewById(R.id.UploadButton)
        UploadButton.setOnClickListener {
            safedBool = true
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            val fileProvider = FileProvider.getUriForFile(this, "com.example.kamera.fileprovider", photoFile)

            imageView = findViewById(R.id.imageView)
            imageView.setImageBitmap(takenImage)

            val user_id = "Irgendwas"
            val file = File(photoFile.absolutePath)
            val inputStream = contentResolver.openInputStream(fileProvider)
            val outputStram = FileOutputStream(file)
            inputStream!!.copyTo(outputStram)
            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val part = MultipartBody.Part.createFormData("pic", file.name, requestBody)
            // upload des bildes
            lifecycleScope.launch {
                val response = apiManager.upload(files = part)
                Log.println(Log.DEBUG, "Mango", response)
            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}

private fun MainActivity.getPhotoFile(fileName: String): File {
    // Use "getExternalFilesDir" on Context to access package-specific directories
    val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(fileName, ".jpg", storageDirectory)
}

interface ApiManager {

    @FormUrlEncoded
    @POST("signin")
    suspend fun signin(
        @Field("name") name: String,
        @Field("password") password: String
    ): Response<SignInResponse>

    @POST("/signup")
    suspend fun signup(
        @Query("name") username: String,
        @Query("password") password: String,
    ): Response<SignUpResponse>

    @Multipart
    @POST("upload")
    suspend fun upload(
        @Part files: MultipartBody.Part,
    ): String

    @GET("gallery/{UID}")
    suspend fun getGallery(@Path("UID") UID: String): Array<Gallery>

    @GET("data/result/{Pic}")
    suspend fun getPicture(@Path("Pic") Pic: String)
}

data class SignUpResponse(
    val message: String,
    val user_id: String
)

data class SignInResponse(
    val user_id: String
)

data class FileData(
    val fileName: String,
    val BufferedReader: BufferedOutputStream,
    val ImageType: String
)

data class Gallery(
    val id: Int,
    val category: String,
    val timestamp: String,
    //val info_text: String,        wenn pascal es schafft den mir wiederzugeben
    val result_url: String
)


// zusätzliche Variable beim hochladen für speichern nur gesetzt bei speicher Knopf
// Passwort Hash256