package com.test_gg_ua.testapp.act

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.adapter.InfoAdapter
import com.test_gg_ua.testapp.db_manager.DbConstants
import com.test_gg_ua.testapp.db_manager.DbManager
import com.test_gg_ua.testapp.db_manager.InfoItem
import com.test_gg_ua.testapp.db_manager.OnInfoItemsListener
import kotlinx.android.synthetic.main.activity_info.*

class InfoAct : AppCompatActivity(), OnInfoItemsListener {
    //Показываем Теорию или практику на этом активити
    val dbManager =  DbManager(null)
    private var editType : String? = null
    private var infoAdapter:InfoAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
    }

    override fun onResume() {
        super.onResume()
        if(editType == DbConstants.EDIT_TYPE_THEORY){
            dbManager.getInfoItemsFromDb(this,DbConstants.THEORY_DIR)
        } else {
            dbManager.getInfoItemsFromDb(this,DbConstants.PRACTIC_DIR)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)finish()
        return super.onOptionsItemSelected(item)
    }


    private fun init() {

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rcViewInfo.layoutManager = LinearLayoutManager(this)

        val isReadOnlyState = intent.getBooleanExtra(DbConstants.READ_ONLY_INTENT, false)
        editType = intent.getStringExtra(DbConstants.EDIT_TYPE_INTENT)

        if (isReadOnlyState) {
            fbNewInfo.visibility = View.GONE
        }
        //Создаем адаптер для показа списка с теорией или практикой зависит от isReadOnlyState
        if (editType != null) infoAdapter = InfoAdapter(dbManager, isReadOnlyState, editType!!)
        rcViewInfo.adapter = infoAdapter
        if (editType == DbConstants.EDIT_TYPE_THEORY) {
            supportActionBar?.title = resources.getString(R.string.teoria)
        } else {
            supportActionBar?.title = resources.getString(R.string.practica)
        }

    }

    fun onClickNewInfo(view: View){

        val intent = Intent(this, EditInfoAct::class.java).apply {
            putExtra(DbConstants.EDIT_INTENT, false)
            if(editType != null)putExtra(DbConstants.EDIT_TYPE_INTENT, editType)
        }
        startActivity(intent)


    }

    override fun onInfoItemListener(itemsInfoList: List<InfoItem>) {
        infoAdapter?.updateAdapter(itemsInfoList)
    }

}