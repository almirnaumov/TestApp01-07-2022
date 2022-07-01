package com.test_gg_ua.testapp.act

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.db_manager.DbConstants
import com.test_gg_ua.testapp.db_manager.InfoItem
import kotlinx.android.synthetic.main.activity_read_info.*
import kotlinx.android.synthetic.main.activity_read_info.pdfView
import java.io.File

class ReadInfoAct : AppCompatActivity() {
    lateinit var tempTheoryFile:File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_info)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
    }
    private fun init(){
        val item = intent.getSerializableExtra(DbConstants.TEST_ITEM_INTENT) as InfoItem
        tvTitleReadInfo.text = item.titleInfo
        pbBarRead.visibility = View.VISIBLE
        showPdf(item.infoPdfPath)

    }
    private fun showPdf(pdfPath: String){
        PRDownloader.initialize(applicationContext)
        downloadPdfFromInternet(pdfPath, "pdfTheoryTempFile")

    }

    private fun downloadPdfFromInternet(url: String, fileName: String) {

        PRDownloader.download(url,applicationContext.cacheDir.path, fileName).build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    tempTheoryFile = File(applicationContext.cacheDir.path,fileName)
                    // progressBar.visibility = View.GONE
                    pbBarRead.visibility = View.GONE
                    showPdfFromFile(tempTheoryFile)
                    //Log.d("MyLog","Path temp : $downloadedFile")
                }

                override fun onError(error: com.downloader.Error?) {
                    Toast.makeText(
                        applicationContext,
                        "Error in downloading file : $error",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    pbBarRead.visibility = View.GONE
                }
            })
    }

    private fun showPdfFromFile(file: File) {
        pdfView.fromFile(file)
            .password(null)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onPageError { page, _ ->
                Toast.makeText(
                    applicationContext,
                    "Error at page: $page", Toast.LENGTH_LONG
                ).show()
            }
            .load()
    }
}