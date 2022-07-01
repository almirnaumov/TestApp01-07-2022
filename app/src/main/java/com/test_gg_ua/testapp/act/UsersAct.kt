package com.test_gg_ua.testapp.act

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.adapter.UsersAdapter
import com.test_gg_ua.testapp.db_manager.DbManager
import com.test_gg_ua.testapp.db_manager.OnUsersReceived
import com.test_gg_ua.testapp.db_manager.UserPerfItem
import kotlinx.android.synthetic.main.activity_edit_info.*
import kotlinx.android.synthetic.main.activity_users.*

class UsersAct : AppCompatActivity(), OnUsersReceived {
    private val dbManager = DbManager(null)
    private val userAdapter = UsersAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }



    private fun init(){

        supportActionBar?.title = resources.getString(R.string.user_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rcViewUsers.layoutManager = LinearLayoutManager(this)
        rcViewUsers.adapter = userAdapter
        dbManager.setOnUsersReceiverListener(this)
        dbManager.getUsers()
    }

    override fun onUsersReceived(itemList: List<UserPerfItem>) {

        userAdapter.updateAdapter(itemList)
        Log.d("MyLog","Data size : ")

    }
}