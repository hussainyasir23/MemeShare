package com.example.memeshare

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    var currentImageUrl: String? = null
    var memeImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadMeme()

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    private fun loadMeme(){
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE

        val url = "https://meme-api.herokuapp.com/gimme"

        // Request a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    currentImageUrl = response.getString("url")
                    memeImage = response.getString("title")
                    Glide.with(this).load(currentImageUrl).listener(object : RequestListener<Drawable> {

                        override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                        ): Boolean {
                            findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                        ): Boolean {
                            findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                            return false
                        }
                    }).into(findViewById<ImageView>(R.id.memeImageView))
                },
                {
                    Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show()
                })

        // Add the request to the RequestQueue.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    fun shareMeme(view: View) {

        val bitmap: Bitmap = getBitmapFromView(findViewById<ImageView>(R.id.memeImageView))
        try {
            val file = File(this.externalCacheDir, memeImage+".png")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            file.setReadable(true, false)
            val intent = Intent(Intent.ACTION_SEND).setType("image/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_TEXT, "Hey, Checkout this cool meme")
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            startActivity(Intent.createChooser(intent, "Share this meme using..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun nextMeme(view: View) {
        loadMeme()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }
}