package com.test_gg_ua.testapp.account

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.*
import com.test_gg_ua.testapp.act.MainActivity
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.db_manager.DbManager
import com.test_gg_ua.testapp.db_manager.UserPerfItem

class AccountHelper(activityM: MainActivity?) {
    //Инициализируем FirebaseAuth
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var activity = activityM

    //Регистрация по Email и Password
    private fun signUp(email: String, password: String, name:String,
                       secName:String, fatherName:String, context: Context,dbManager:DbManager) {
        if (email != "" && password != "" && name != "" && secName != "" && fatherName != "") {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { result ->

                    if (result.isSuccessful) {
                        if (mAuth.currentUser != null) {
                            val user = mAuth.currentUser
                            activity?.getAdminState()
                            val userPerfItem = UserPerfItem()
                            userPerfItem.userName = name
                            userPerfItem.userSecName = secName
                            userPerfItem.userFatherName = fatherName
                            userPerfItem.userRightAnswers = "0"
                            userPerfItem.userFalseAnswers = "0"
                            userPerfItem.userEmail = user?.email!!
                            dbManager.saveUserPerf(userPerfItem, context)
                        }
                        //activity.getUserData()
                    }


                }
        } else {

            Toast.makeText(context, "Внимание, вы заполнили не все поля! Заполните все поля для регистрации!", Toast.LENGTH_LONG).show()

        }
    }
    //Вход по Email и Password
    private fun signIn(email: String, password: String) {
        if (email != "" && password != "") {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        activity?.getAdminState()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(
                            "MyLogMainActivity",
                            "signInWithEmail:failure",

                            task.exception
                        )
                        Toast.makeText(
                            activity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
        } else {
            Toast.makeText(activity, "Email или Password пустой!!", Toast.LENGTH_SHORT).show()
        }
    }
    //Диалоговое окно для входа и регистрации
    fun signUpDialog(title: Int, buttonTitle: Int, index: Int, dbManager:DbManager) {


        val dialogBuilder = AlertDialog.Builder(activity!!)
        val inflater = activity!!.layoutInflater
        val dialogView = inflater.inflate(R.layout.sign_up_layout, null)
        dialogBuilder.setView(dialogView)
        val titleTextView = dialogView.findViewById<TextView>(R.id.tvAlertTitle)
        titleTextView.setText(title)
        val signUpEmail = dialogView.findViewById<Button>(R.id.buttonSignUp)
        val edEmail = dialogView.findViewById<EditText>(R.id.edEmail)
        val edPassword = dialogView.findViewById<EditText>(R.id.edPassword)
        val edName = dialogView.findViewById<EditText>(R.id.edName)
        val edSecName = dialogView.findViewById<EditText>(R.id.edSecName)
        val edFatherName = dialogView.findViewById<EditText>(R.id.edFatherName)
        signUpEmail.setText(buttonTitle)

        if(index > 0){

            edName.visibility = View.GONE
            edSecName.visibility = View.GONE
            edFatherName.visibility = View.GONE
        }

        val dialog = dialogBuilder.create()
        signUpEmail.setOnClickListener {

            if (index == 0) {
                signUp(edEmail.text.toString(), edPassword.text.toString(),
                        edName.text.toString(), edSecName.text.toString(),
                        edFatherName.text.toString(), activity!!,dbManager)
            } else {
                signIn(edEmail.text.toString(), edPassword.text.toString())
            }
            dialog.dismiss()
        }


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
    //Выход из аккаунта
    fun signOut() {
        mAuth.signOut()
        activity?.setAdminState(false)
        activity?.setUI(null)
        //activity.getUserData()
    }

}
