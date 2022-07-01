package com.test_gg_ua.testapp.act

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.activity_edit_info.*
import java.io.File


class EditInfoAct : AppCompatActivity(), OnItemRemoved, OnItemSaved {
    //Редактирование информативного контента
    private var newFilePath:String = ""
    val pdfRequest = 100
    val dbManager = DbManager(null)
    var isEditInfoMode = false
    var itemInfo: InfoItem? = null
    lateinit var tempTheoryFile : File
    private var editType:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_info)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
        getMyIntents()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == pdfRequest && resultCode == RESULT_OK){
            data?.data?.also { documentUri ->
                contentResolver?.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                //val file = DocumentUtils.getFile(this, documentUri)//use pdf as file
                //btGetPdf.text = documentUri.toString()
                newFilePath = documentUri.toString()
            }


        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.id_delete_question -> {

                if (isEditInfoMode) {

                    deleteItem(itemInfo!!)

                } else {

                    edTitleInfo.setText("")
                    btGetPdf.setText("")
                }

            }
            R.id.id_save_question -> {

                saveItem()

            }
            android.R.id.home -> {

                finish()

            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onItemRemoved(pos: Int) {
        Toast.makeText(this, "Удалено!", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onSaveItem() {
        pbBar.visibility = View.GONE
        Toast.makeText(this, "Сохранено!", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun init() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title =  resources.getString(R.string.new_info)
        dbManager.setOnItemSaved(this)
    }

    private fun getMyIntents(){

        isEditInfoMode = intent.getBooleanExtra(DbConstants.EDIT_INTENT, false)
        editType = intent.getStringExtra(DbConstants.EDIT_TYPE_INTENT)
        Log.d("MyLog","EditType EditAct Result : $editType ")
        if(isEditInfoMode){

            itemInfo = intent.getSerializableExtra(DbConstants.TEST_ITEM_INTENT) as InfoItem
            edTitleInfo.setText(itemInfo?.titleInfo)
            pbBar.visibility = View.VISIBLE
            showPdf(itemInfo?.infoPdfPath!!)

        } else {
            pbBar.visibility = View.GONE
        }

    }

    fun saveItem(){

        if(newFilePath.isEmpty() && itemInfo?.infoPdfPath?.isEmpty()!!){
            Toast.makeText(applicationContext,"Файл не выбран!!", Toast.LENGTH_LONG).show()
            return
        }
        pbBar.visibility = View.VISIBLE
        val infoItem = InfoItem()
        infoItem.titleInfo = edTitleInfo.text.toString()
        if(itemInfo != null)infoItem.infoPdfPath = itemInfo?.infoPdfPath!!
        if(isEditInfoMode)infoItem.key = itemInfo?.key!!
        if(editType == DbConstants.EDIT_TYPE_THEORY){
            dbManager.saveInfoToDb(DbConstants.THEORY_DIR, infoItem, isEditInfoMode, this, newFilePath)
        } else {
            dbManager.saveInfoToDb(DbConstants.PRACTIC_DIR, infoItem, isEditInfoMode, this, newFilePath)
        }

    }

    fun getPdfFile(view: View){
        val pdfChooser = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pdfChooser.type = "application/pdf"
        pdfChooser.addCategory(Intent.CATEGORY_OPENABLE)
        pdfChooser.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(pdfChooser, pdfRequest)
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
                    pbBar.visibility = View.GONE
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
                    pbBar.visibility = View.GONE
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

    private fun deleteItem(itemInfo: InfoItem) {

        dbManager.deleteInfoItem(itemInfo, 0, this)

    }

    override fun onBackPressed() {
        tempTheoryFile.delete()
        super.onBackPressed()
    }




}