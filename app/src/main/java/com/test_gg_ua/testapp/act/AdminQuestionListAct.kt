package com.test_gg_ua.testapp.act

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.adapter.QuestionsAdapter
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.activity_admin_question.*

class AdminQuestionListAct : AppCompatActivity(), OnQuestionReceivedListener {
    //Активити редактирования вопросов для теста
    private val dbManager = DbManager(null)
    private val adapter = QuestionsAdapter(dbManager)
    private lateinit var testPath:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_question)
        init()
        getIntents()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        dbManager.getQuestions(testPath)
    }

    override fun onQuestionReceived(qList: List<QuestionItem>) {
        adapter.updateAdapter(qList)
    }

    private fun init(){
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rcView.layoutManager = LinearLayoutManager(this)
        adapter.setOnItemRemoved(adapter)
        rcView.adapter = adapter
        dbManager.setOnQReceivedListener(this)


    }
    //Получаем Intent с MainActivity
    private fun getIntents(){
        val testItem = intent.getSerializableExtra(DbConstants.NEW_TEST_DIR) as TestItem
        supportActionBar?.title = testItem.testName
        testPath = testItem.testDir
        adapter.setTestDir(testItem.testDir)
    }
    //Создаем новый вопрос
    fun onClickNewQ(view: View){

        val i = Intent(this, NewQuestionAct::class.java).apply {
            putExtra(DbConstants.EDIT_INTENT, false)
            putExtra(DbConstants.NEW_TEST_DIR, testPath)
        }
        startActivity(i)

    }


}