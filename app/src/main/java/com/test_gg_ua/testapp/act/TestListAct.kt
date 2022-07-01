package com.test_gg_ua.testapp.act

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.adapter.TestsAdapter
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.activity_admin_test.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class TestListAct : AppCompatActivity(), OnTestsReceived, OnItemSaved{
    val dbManager:DbManager = DbManager(null)
    private lateinit var adapter: TestsAdapter
    private var isAdminAcc = false
    private var isTestPrueba = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_test)
        getIntents()
        init()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_element_menu, menu)
        menu?.findItem(R.id.id_new)?.isVisible = isAdminAcc
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.id_new){
            createNewTestDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        dbManager.readDataFromTestNode()
        super.onResume()
    }
    private fun init(){
        dbManager.setOnTestReceivedListener(this)
        dbManager.setOnItemSaved(this)
        rcView.layoutManager = LinearLayoutManager(this)
        rcView.adapter = adapter


    }

    private fun getIntents(){
        isAdminAcc = intent.getBooleanExtra(DbConstants.IS_ADMIN, false)
        isTestPrueba = intent.getBooleanExtra(DbConstants.TEST_PRUEBA, false)
        val i= intent.getSerializableExtra(DbConstants.USER_PERF_INTENT)
        adapter = if(i is UserPerfItem){
            val userPerfAccount = intent.getSerializableExtra(DbConstants.USER_PERF_INTENT) as UserPerfItem
            TestsAdapter(dbManager,!isAdminAcc, "null", userPerfAccount, isTestPrueba)
        } else {
            TestsAdapter(dbManager,!isAdminAcc, "null", null, false)
        }

    }


    private fun createNewTestDialog(){
        val builder = AlertDialog.Builder(this)
        val view = this.layoutInflater.inflate(R.layout.new_test_dialog,null, false)
        builder.setView(view)
        val edName = view.findViewById<EditText>(R.id.edTestName)
        val dialog = builder.create()
        val bOk = view.findViewById<Button>(R.id.bOk)
        val bBack = view.findViewById<Button>(R.id.bBack)
        bOk.setOnClickListener{
            dbManager.createNewTestNode(edName.text.toString())
            dialog.dismiss()
        }
        bBack.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onTestReceived(tests: List<TestItem>) {
        adapter.updateAdapter(tests)
    }

    override fun onSaveItem() {
        dbManager.readDataFromTestNode()
    }
}