package com.test_gg_ua.testapp.act

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.activity_new_question.*


class NewQuestionAct : AppCompatActivity(), OnItemRemoved, OnItemSaved {
    private val requestImage = 10
    private var isImageSelected = 0
    private val dbManager = DbManager(null)
    private var itemQuestion:QuestionItem? = null
    private var isEditMode = false
    private var testDir:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_question)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
        getMyIntents()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        imMain.setImageURI(data?.data)
        isImageSelected = EditImageConst.IMAGE_SELECTED

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.id_delete_question -> {

                if(isEditMode){

                    deleteItem(itemQuestion!!)

                } else {

                    edTitleQuestion.setText("")
                    edQuestion.setText("")
                    edRightAnswer.setText("")
                    edFalseAnswer.setText("")
                    edFalseAnswer2.setText("")
                    edFalseAnswer3.setText("")
                    imMain.setImageURI(null)
                    isImageSelected = EditImageConst.IMAGE_NOT_SELECTED


                }

            }
            R.id.id_save_question -> {

                saveQuestion()

            }
            android.R.id.home -> {

                finish()

            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun init() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title =  resources.getString(R.string.new_question)
        dbManager.setOnItemSaved(this)
    }

    private fun getMyIntents(){

        isEditMode = intent.getBooleanExtra(DbConstants.EDIT_INTENT, false)
        testDir = intent.getStringExtra(DbConstants.NEW_TEST_DIR)!!

        if(isEditMode){

            dbManager.setItemRemovedListener(this)
            supportActionBar?.title =  resources.getString(R.string.edit)
            itemQuestion = intent.getSerializableExtra(DbConstants.QUESTION_ITEM_INTENT) as QuestionItem
            edTitleQuestion.setText(itemQuestion?.title)
            edQuestion.setText(itemQuestion?.question)
            edRightAnswer.setText(itemQuestion?.rightAnswer)
            edFalseAnswer.setText(itemQuestion?.falseAnswer)
            edFalseAnswer2.setText(itemQuestion?.falseAnswer2)
            edFalseAnswer3.setText(itemQuestion?.falseAnswer3)

            if(itemQuestion?.imagePath != "empty"){
                Picasso.get().load(itemQuestion?.imagePath).into(imMain)
                isImageSelected = EditImageConst.IMAGE_FROM_FB
            }
        }

    }

    fun onClickEditImage(view: View) {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestImage)

    }

    fun onClickDeleteImage(view: View) {

        imMain.setImageResource(R.drawable.ic_def_image)
        isImageSelected = if(isEditMode && isImageSelected == EditImageConst.IMAGE_FROM_FB){
            EditImageConst.IMAGE_TO_DELETE_FB
        } else {
            EditImageConst.IMAGE_NOT_SELECTED
        }

    }

    private fun saveQuestion() {


        progressBar.visibility = View.VISIBLE
        var bitMap: Bitmap? = null
        if (isImageSelected == EditImageConst.IMAGE_SELECTED) {

            bitMap = (imMain.drawable as BitmapDrawable).bitmap

        }
        val item = QuestionItem()
        item.title = edTitleQuestion.text.toString()
        item.imagePath = "empty"
        item.question = edQuestion.text.toString()
        item.rightAnswer = edRightAnswer.text.toString()
        item.falseAnswer = edFalseAnswer.text.toString()
        item.falseAnswer2 = edFalseAnswer2.text.toString()
        item.falseAnswer3 = edFalseAnswer3.text.toString()
        item.testCat = testDir

        if(!isEditMode){
            item.key = "empty"
        } else {
            item.key = itemQuestion?.key!!
            item.imagePath = itemQuestion!!.imagePath
            if(isImageSelected == EditImageConst.IMAGE_NOT_SELECTED && itemQuestion!!.imagePath != "empty")
                isImageSelected = EditImageConst.IMAGE_TO_DELETE_FB
        }
        //Log.d("MyLog", "Image state : $isImageSelected")

        dbManager.addQuestionToDb(testDir,item, bitMap, isImageSelected, isEditMode)
    }

    private fun deleteItem(item:QuestionItem){

        progressBar.visibility = View.VISIBLE
        dbManager.deleteQuestionItem(item, 0)
    }

    override fun onItemRemoved(pos:Int) {
        Toast.makeText(this, "Удалено", Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        finish()
    }

    override fun onSaveItem() {

        Toast.makeText(this,"Сохранено!",Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        finish()

    }
}