package com.test_gg_ua.testapp.act

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.test_gg_ua.testapp.OProgActivity
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.account.AccountConsts
import com.test_gg_ua.testapp.account.AccountHelper
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_content.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnAdminStateListener, OnPerfRead {

    private val accountHelper = AccountHelper(this)
    private val dbManager = DbManager(this)
    private var userPerfAccount:UserPerfItem? = null
    private var mAuth:FirebaseAuth? = null
    private var isAdminAcc = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
        setCatTextColor()
    }

    override fun onStart() {
        super.onStart()
        getAdminState()
    }

    override fun onResume() {
        drawerLayout.openDrawer(GravityCompat.START)
        if(userPerfAccount == null) getUserPerf()
        super.onResume()
    }
    //Проверка нажатия кнопок меню
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.id_teoria -> {

                if (mAuth?.uid != null) {
                    val i = Intent(this, InfoAct::class.java).apply {
                        putExtra(DbConstants.READ_ONLY_INTENT, true)
                        putExtra(DbConstants.EDIT_TYPE_INTENT, DbConstants.EDIT_TYPE_THEORY)
                    }
                    startActivity(i)
                }

            }
            R.id.id_practica -> {

                if (mAuth?.uid != null) {
                    val i = Intent(this, InfoAct::class.java).apply {
                        putExtra(DbConstants.READ_ONLY_INTENT, true)
                        putExtra(DbConstants.EDIT_TYPE_INTENT, DbConstants.EDIT_TYPE_PRACTICA)
                    }
                    startActivity(i)
                }

            }
            R.id.id_test -> {

                if (mAuth?.uid != null) {
                    //Запускаем Активити с тестами передаем состояние sAdminAcc
                    //и передаем userPerfAccount который несет в себе информацию аккаунта
                    //пользователя где будем сохранять результат теста
                    if(userPerfAccount != null){
                    val i = Intent(this, TestListAct::class.java).apply {
                        putExtra(DbConstants.USER_PERF_INTENT, userPerfAccount)
                        putExtra(DbConstants.IS_ADMIN, isAdminAcc)
                    }
                    startActivity(i)
                }
                }

            }
            R.id.id_trener ->{

                if (mAuth?.uid != null) {

                    if(userPerfAccount != null){
                        val i = Intent(this, TestListAct::class.java).apply {
                            putExtra(DbConstants.USER_PERF_INTENT, userPerfAccount)
                            putExtra(DbConstants.TEST_PRUEBA, true)
                            putExtra(DbConstants.IS_ADMIN, isAdminAcc)
                        }
                        startActivity(i)
                    }
                }


            }
            R.id.id_o_prog ->{

                val i = Intent(this, OProgActivity::class.java)
                startActivity(i)
            }

            R.id.id_tests -> {


                val i = Intent(this, TestListAct::class.java).apply {
                    //putExtra(DbConstants.USER_PERF_INTENT, userPerfAccount)
                    putExtra(DbConstants.IS_ADMIN, true)
                }

                startActivity(i)


            }
            R.id.id_users -> {
                val i = Intent(this, UsersAct::class.java)
                startActivity(i)
            }
            R.id.id_theory_adm -> {

                val i = Intent(this, InfoAct::class.java).apply {
                    putExtra(DbConstants.READ_ONLY_INTENT, false)
                    putExtra(DbConstants.EDIT_TYPE_INTENT, DbConstants.EDIT_TYPE_THEORY)
                }
                startActivity(i)

            }
            R.id.id_practica_adm -> {

                val i = Intent(this, InfoAct::class.java).apply {
                    putExtra(DbConstants.READ_ONLY_INTENT, false)
                    putExtra(DbConstants.EDIT_TYPE_INTENT, DbConstants.EDIT_TYPE_PRACTICA)
                }
                startActivity(i)

            }
            R.id.id_sign_up -> {

                accountHelper.signUpDialog(
                    R.string.sign_up, R.string.sign_up_button,
                    AccountConsts.SIGN_UP_STATE, dbManager
                )

            }
            R.id.id_sign_in -> {

                accountHelper.signUpDialog(
                    R.string.sign_in, R.string.sign_in_button,
                    AccountConsts.SIGN_IN_STATE, dbManager
                )

            }
            R.id.id_sign_out -> {

                accountHelper.signOut()

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    private fun init(){

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
                R.string.toggle_open,
                R.string.toggle_close
        )
        //Добавляем к выдвижному меню кнопку закрытия
        //и с помощью openDrawer ткрываем меню изначально
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        //Прячем панель с админом и считываем сосотяние админ/ не админ
        nav_view.menu.findItem(R.id.id_admin).isVisible = false
        dbManager.setOnUserPerfRead(this)

    }

    private fun setCatTextColor(){
        val menu = nav_view.menu

        val categoryAccountItem = menu.findItem(R.id.accountCatId)
        val categoryAdsItem = menu.findItem(R.id.mainCatId)
        val categoryAdminItem = menu.findItem(R.id.id_admin)
        val sp = SpannableString(categoryAccountItem.title)
        val sp2 = SpannableString(categoryAdsItem.title)
        val sp3 = SpannableString(categoryAdminItem.title)
        sp.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)), 0, sp.length, 0)
        sp2.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)), 0, sp2.length, 0)
        sp3.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)), 0, sp3.length, 0)
        categoryAccountItem.title = sp
        categoryAdsItem.title = sp2
        categoryAdminItem.title = sp3
    }

    override fun onAdminStateChanged(isAdmin: Boolean) {

      setAdminState(isAdmin)

    }
    //Проверка админа
    fun setAdminState(isAdmin:Boolean){

        if(isAdmin){
            toolbar?.title = resources.getString(R.string.admin)
            nav_view.menu.findItem(R.id.id_admin).isVisible = true
        } else {
            toolbar?.title = ""
            nav_view.menu.findItem(R.id.id_admin).isVisible = false
        }

    }
    //Проверка состояния админа
    fun getAdminState(){
        mAuth = FirebaseAuth.getInstance()
        if(mAuth?.currentUser == null)return
        dbManager.isAdmin(mAuth!!)
        getUserPerf()
        setUI(mAuth)
    }
    //Получаем пользователский аккаунт
    private fun getUserPerf(){
        dbManager.getUserPerf()
    }

    fun setUI(mAuth:FirebaseAuth?){

        if(mAuth == null){
            nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvEmail)
                .text = resources.getString(R.string.sign_up_please)
        } else {
            nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvEmail)
                .text = mAuth.currentUser?.email
        }

    }



    override fun onPerfRead(item: UserPerfItem) {
        userPerfAccount = item
    }


}