package com.test_gg_ua.testapp.act

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.activity_test.*
import java.util.*
import kotlin.collections.ArrayList

class TestAct : AppCompatActivity(), OnQuestionReceivedListener {
    //Активити где проводим тест опрос
    private val dbManager = DbManager(null)
    private var myQuestionsList = ArrayList<QuestionItem>()
    private var answerCounter = 0
    private var trueAnswerCounter = 0
    private var falseAnswerCounter = 0
    private var testDone = false
    private var userPerfAccount: UserPerfItem? = null
    lateinit var testItem: TestItem
    lateinit var timer:CountDownTimer
    private var isTimerFinished = true
    private var isTestPrueba = true
    private val timeToAnswer = 60000L
    private var answerByUser = ""
    private var questionsLengh = 0
    private var answered = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
        getMyIntents()
        readTestFromDb()
    }

    override fun onDestroy() {
        if(!isTimerFinished)timer.cancel()
        super.onDestroy()
    }

    private fun init(){
        dbManager.setOnQReceivedListener(this)
    }

    private fun readTestFromDb(){
        dbManager.getQuestions(testItem.testDir)
    }
    //Получение вопросов из базы данных
    override fun onQuestionReceived(qList: List<QuestionItem>) {
        myQuestionsList.clear()
        myQuestionsList.addAll(qList)
        if(qList.isNotEmpty()){
            testContentLayout.visibility = View.VISIBLE
            questionsLengh = qList.size
            nextQuestion()
        } else {
            testContentLayout.visibility = View.GONE
            noQuestionsText.visibility = View.VISIBLE
        }
    }
    //Ответ
    fun onClickAnswer(view: View){

        if(testDone)return
        when(view.id){

            R.id.tvAnswer->{
                if(answered)return
                answerByUser = (view as TextView).text.toString()
                checkAnswer()
                if(!isTestPrueba)nextQuestion()
            }
            R.id.tvAnswer2->{
                if(answered)return
                answerByUser = (view as TextView).text.toString()
                checkAnswer()
                if(!isTestPrueba)nextQuestion()
            }
            R.id.tvAnswer3->{
                if(answered)return
                answerByUser = (view as TextView).text.toString()
                checkAnswer()
                if(!isTestPrueba)nextQuestion()
            }
            R.id.tvAnswer4->{
                if(answered)return
                answerByUser = (view as TextView).text.toString()
                checkAnswer()
                if(!isTestPrueba)nextQuestion()
            }
        }
    }
    //Следущий вопрос функция
    private fun nextQuestion(){

         if(testDone)return

         setActionBarTitle()
         val listAnswers = ArrayList<String>()
         listAnswers.add(myQuestionsList[answerCounter].rightAnswer)
         listAnswers.add(myQuestionsList[answerCounter].falseAnswer)
         listAnswers.add(myQuestionsList[answerCounter].falseAnswer2)
         listAnswers.add(myQuestionsList[answerCounter].falseAnswer3)

         val listShuffled = listAnswers.shuffled()

         tvMainQuestion.text = myQuestionsList[answerCounter].question
         tvAnswer.text = listShuffled[0]
         tvAnswer2.text = listShuffled[1]
         tvAnswer3.text = listShuffled[2]
         tvAnswer4.text = listShuffled[3]

        if(myQuestionsList[answerCounter].imagePath != "empty") {
            if(!imMainTest.isVisible)imMainTest.visibility = View.VISIBLE
            Picasso.get().load(myQuestionsList[answerCounter].imagePath).into(imMainTest)
        } else {
            imMainTest.setImageResource(R.drawable.ic_def_image)
            imMainTest.visibility = View.GONE
        }
        if(!isTestPrueba)startTimer()



    }
    //Проверка ответа
    private fun checkAnswer(){

      if(answerByUser == myQuestionsList[answerCounter].rightAnswer){
          trueAnswerCounter++
          if(isTestPrueba){
          btNextQuestion.visibility = View.VISIBLE
          tvShowAnswer.text = "Верно"
          tvShowAnswer.setTextColor(Color.GREEN)
          }
      } else {
          falseAnswerCounter++
          if(isTestPrueba){
          btGoToTheory.visibility = View.VISIBLE
          tvShowAnswer.text = "Неверно"
          tvShowAnswer.setTextColor(Color.RED)
          }
      }
        answerCounter++
        //Log.d("MyLog","Answer count $answerCounter")
        if(answerCounter == myQuestionsList.size){
            if(!isTestPrueba){
            testDone()
            } else {
                btNextQuestion.visibility = View.GONE
                btFinTest.visibility = View.VISIBLE
            }
        }
        if(isTestPrueba)answered = true
        answerByUser = ""
    }
    //Текст для верхней панели ToolBar
    private fun setActionBarTitle(){
        supportActionBar?.title = resources.getString(R.string.hint_question) + " ${answerCounter + 1} из ${myQuestionsList.size}"
    }

    private fun getMyIntents(){

        userPerfAccount = intent.getSerializableExtra(DbConstants.USER_PERF_INTENT) as UserPerfItem
        testItem = intent.getSerializableExtra(DbConstants.TEST_ITEM_INTENT) as TestItem
        isTestPrueba = intent.getBooleanExtra(DbConstants.TEST_PRUEBA, false)
        Log.d("MyLog", "TestItem: ${testItem.testName}")

    }
    //Тест закончен
    private fun testDone(){
        if(!isTimerFinished)timer.cancel()
        testDone = true
        userPerfAccount?.userRightAnswers = trueAnswerCounter.toString()
        userPerfAccount?.userFalseAnswers = falseAnswerCounter.toString() + "_" + questionsLengh + "_" +testItem.testName
        if(!isTestPrueba)dbManager.saveUserPerf(userPerfAccount!!, this)
        val i = Intent(this, ResultActivity::class.java).apply {
            putExtra(DbConstants.USER_PERF_INTENT, userPerfAccount)
            putExtra(DbConstants.QUESTIONS_LENGH, questionsLengh)
        }

        startActivity(i)
        finish()

    }
    //Запуск таймера
    private fun startTimer(){
        if(!isTimerFinished) timer.cancel()

       timer = object : CountDownTimer(timeToAnswer,1000){
            override fun onTick(timeInM: Long) {
                val titleT = resources.getString(R.string.hint_question) + " ${answerCounter + 1} из ${myQuestionsList.size}"
                supportActionBar?.title = titleT + " -/- Время: ${timeInM/1000}"
            }

            override fun onFinish() {
                isTimerFinished = true
                checkAnswer()
                nextQuestion()
            }

        }.start()
        isTimerFinished = false
    }
    //Следущий вопрос
    fun onClickNextQuestion(view:View){
        btNextQuestion.visibility = View.GONE
        answered = false
        nextQuestion()
    }
    //Идти к теории
    fun onClickGoToTheory(view:View){
        btGoToTheory.visibility = View.GONE
        answered = false
        val i = Intent(this, InfoAct::class.java).apply {
            putExtra(DbConstants.READ_ONLY_INTENT, true)
            putExtra(DbConstants.EDIT_TYPE_INTENT, DbConstants.EDIT_TYPE_THEORY)
        }
        startActivity(i)
        finish()
    }
    //Завершить тест
    fun onClickFinTest(view:View){
        testDone()
    }
}