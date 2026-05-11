package com.nursena.fenlab_android.core.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class PdfOpenActivity : Activity() {

    private var renderer: PdfRenderer? = null
    private var pfd: ParcelFileDescriptor? = null
    private var pageIndex = 0
    private var currentBitmap: Bitmap? = null

    private lateinit var imageView: ImageView
    private lateinit var pageText: TextView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        val uriString = intent.getStringExtra("pdf_uri")
        if (uriString.isNullOrBlank()) {
            finish()
            return
        }

        imageView = ImageView(this).apply {
            setBackgroundColor(Color.WHITE)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = false
        }

        pageText = TextView(this).apply {
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setBackgroundColor(Color.WHITE)
            setPadding(0, 8, 0, 8)
        }

        prevButton = Button(this).apply {
            text = "Önceki"
            setOnClickListener {
                if (pageIndex > 0) {
                    pageIndex--
                    renderPage()
                }
            }
        }

        nextButton = Button(this).apply {
            text = "Sonraki"
            setOnClickListener {
                val count = renderer?.pageCount ?: 0
                if (pageIndex < count - 1) {
                    pageIndex++
                    renderPage()
                }
            }
        }

        val closeButton = Button(this).apply {
            text = "Kapat"
            setOnClickListener { finish() }
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(8, 8, 8, 16)
            addView(prevButton)
            addView(nextButton)
            addView(closeButton)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            addView(
                imageView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
            addView(
                pageText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                buttonRow,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.WHITE)
            addView(
                contentLayout,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        setContentView(root)

        try {
            val uri = Uri.parse(uriString)
            pfd = contentResolver.openFileDescriptor(uri, "r")
                ?: throw IllegalStateException("PDF dosyası açılamadı")

            renderer = PdfRenderer(pfd!!)
            renderPage()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun renderPage() {
        val pdfRenderer = renderer ?: return
        if (pdfRenderer.pageCount == 0) return

        val page = pdfRenderer.openPage(pageIndex)

        currentBitmap?.recycle()

        val screenWidth = resources.displayMetrics.widthPixels
        val scale = screenWidth.toFloat() / page.width.toFloat()
        val bitmapWidth = screenWidth
        val bitmapHeight = (page.height * scale).toInt()

        val bitmap = Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight,
            Bitmap.Config.ARGB_8888
        )

        bitmap.eraseColor(Color.WHITE)

        page.render(
            bitmap,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )

        currentBitmap = bitmap
        imageView.setImageBitmap(bitmap)

        pageText.text = "${pageIndex + 1} / ${pdfRenderer.pageCount}"

        prevButton.isEnabled = pageIndex > 0
        nextButton.isEnabled = pageIndex < pdfRenderer.pageCount - 1

        page.close()
    }

    override fun onDestroy() {
        currentBitmap?.recycle()
        renderer?.close()
        pfd?.close()
        super.onDestroy()
    }
}