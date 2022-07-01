package com.test_gg_ua.testapp.act

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.db_manager.DbConstants
import com.test_gg_ua.testapp.db_manager.UserPerfItem
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    //Показываем результат теста
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        getMyIntents()
    }

    private fun getMyIntents(){

        val item = intent.getSerializableExtra(DbConstants.USER_PERF_INTENT) as UserPerfItem
        val falseAnswerArray = getAnswerCountAndFalseAnswer(item.userFalseAnswers)
        val testName = falseAnswerArray[2]
        tvTestName.text = testName
        val porCienD = (calculatePercentage(item.userRightAnswers.toDouble(), falseAnswerArray[1].toDouble()))
        val resultInPorCien = "$porCienD%"
        setNota(porCienD)
        tvPorCien.text = resultInPorCien
        tvUserName.text = item.userName
        tvUserSecName.text = item.userSecName
        tvUserFatherName.text = item.userFatherName
        tvTrueAnswers.text = item.userRightAnswers
        tvFalseAnswers.text = falseAnswerArray[1]
    }

    private fun getAnswerCountAndFalseAnswer(text: String): List<String>{
        return text.split("_")
    }

    private fun calculatePercentage(obtained: Double, total: Double): Double {
        return obtained * 100 / total
    }
    //Оценка теста
    private fun setNota(porCien : Double){
       if(porCien >= 0 && porCien < 49){
           tvNota.text = "Неудовлетворительно"
       } else if(porCien > 49 && porCien < 74){
           tvNota.text = "Удовлетворительно"
       } else if(porCien > 74 && porCien < 89){
           tvNota.text = "Хорошо"
       } else if(porCien > 89) {
           tvNota.text = "Отлично"
       }

    }
}