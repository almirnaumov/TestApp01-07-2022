package com.test_gg_ua.testapp.db_manager

import java.io.Serializable

class QuestionItem : Serializable {

    var title:String = ""
    var imagePath:String = "empty"
    var question:String = ""
    var rightAnswer:String = ""
    var falseAnswer:String = ""
    var falseAnswer2:String = ""
    var falseAnswer3:String = ""
    var key:String = "empty"
    var testCat:String = ""
}