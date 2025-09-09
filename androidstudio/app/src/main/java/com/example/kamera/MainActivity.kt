package com.example.kamera

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.File
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.Multipart
import retrofit2.http.Part
import java.io.InputStream
import java.net.URL
import kotlin.concurrent.thread


private const val FILE_NAME ="photo.jpg"
private const val BASEURL = "http://10.0.2.2:8000"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File
private var category: String = ""
private var pictureTaken: Boolean = false

class MainActivity : AppCompatActivity() {

    private lateinit var btnTakePicture: Button
    private lateinit var GalleryButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var UploadButton: FloatingActionButton

    private var safedBool: Boolean = false
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val apiManager = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASEURL)        // diese Verbindung wird gebraucht um mit dem Localhost zu kommunizieren
        .build()
        .create(ApiManager::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pictureTaken = false

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
                if (category != "") {
                    category = ""
                    var builder = AlertDialog.Builder(this)
                    builder.setTitle("Should this image be uploaded")
                    builder.setPositiveButton("No") { dialog, which ->
                        Toast.makeText(
                            applicationContext,
                            "Image wasn't safed",
                            Toast.LENGTH_SHORT
                        ).show()
                        val viewIntent = Intent(this, SecondActivity::class.java)
                        startActivity(viewIntent)
                        finish()
                    }

                    builder.setNegativeButton("Yes") { dialog, which ->
                        val user_id = UIDDAta().getUID()
                        val requestBody =
                            RequestBody.create("image/*".toMediaTypeOrNull(), photoFile)
                        val part =
                            MultipartBody.Part.createFormData("file", photoFile.name, requestBody)
                        // upload des bildes
                        lifecycleScope.launch {
                            apiManager.upload(user_id = user_id, safe = true, file = part)
                        }.invokeOnCompletion {
                            val viewIntent = Intent(this, SecondActivity::class.java)
                            startActivity(viewIntent)
                            finish()
                        }

                    }
                    builder.show()
                    true
                } else {
                    val viewIntent = Intent(this, SecondActivity::class.java)
                    startActivity(viewIntent)
                    finish()
                }
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
            if (category != "") {
                category = ""
                val user_id = UIDDAta().getUID()
                val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), photoFile)
                val part = MultipartBody.Part.createFormData("file", photoFile.name, requestBody)
                // upload des bildes
                lifecycleScope.launch {
                    val response = apiManager.upload(user_id = user_id, safe = true, file = part)
                }
            } else if (!pictureTaken){
                Toast.makeText(
                    applicationContext, "No image taken", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    applicationContext, "Image was already uploaded", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            pictureTaken = true
            //val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            //val fileProvider = FileProvider.getUriForFile(this, "com.example.kamera.fileprovider", photoFile)

            //imageView = findViewById(R.id.imageView)
            //imageView.setImageBitmap(takenImage)
            //thread {
                //    val inputStream = URL("${BASEURL}/${result_url}").openStream()
            //        setBitmap(inputStream)
            //}.join()

            val user_id = UIDDAta().getUID()
            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), photoFile)
            val part = MultipartBody.Part.createFormData("file", photoFile.name, requestBody)
            // upload des bildes
            lifecycleScope.launch {
                val response = apiManager.upload(user_id = user_id, safe = false,file = part)
                category = response.category
                textView = findViewById(R.id.textView)
                textView.setText(response.category)
                thread {
                    val inputStream = URL("${BASEURL}/${response.result_url}").openStream()
                    setBitmap(inputStream)
                }.join()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
    public fun setBitmap(newBitmap: InputStream) {
        val bitmap = BitmapFactory.decodeStream(newBitmap)
        this.imageView.setImageBitmap(bitmap)
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

    @FormUrlEncoded
    @POST("/signup")
    suspend fun signup(
        @Field("name") username: String,
        @Field("password") password: String,
    ): Response<SignUpResponse>

    @Multipart
    @POST("upload")
    suspend fun upload(
        @Part("user_id") user_id: String?,
        @Part("safe") safe: Boolean,
        @Part file: MultipartBody.Part,
    ): UploadResponse

    @GET("gallery/{UID}")
    suspend fun getGallery(@Path("UID") UID: String?): Array<Gallery>

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

data class UploadResponse(
    val id: String,
    val category: String,
    val timestamp:String,
    val result_url: String
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