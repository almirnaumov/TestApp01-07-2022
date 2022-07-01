package com.test_gg_ua.testapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.act.*
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.question_item_layout.view.*

class TestsAdapter (dbManager: DbManager, isReadOnlyStateA: Boolean,
                    editType:String, userPerfAcc:UserPerfItem?,
                    isTestPrueba:Boolean) : RecyclerView.Adapter<TestsAdapter.MyHolder>(),
    OnItemRemoved {
    private val mainList = ArrayList<TestItem>()
    private var dbManagerAd: DbManager = dbManager
    private var isReadOnlyState = isReadOnlyStateA
    private var isTestPrueba = isTestPrueba
    private var editType = editType
    private var userPerfItem = userPerfAcc



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.question_item_layout,parent, false)
        return MyHolder(view,  parent.context, dbManagerAd, this, isReadOnlyState, editType, userPerfItem, isTestPrueba)
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.setData(mainList[position])
    }

    class MyHolder(
            itemView: View, con: Context, dbManager: DbManager,
            onItemRemoved: OnItemRemoved,
            isReadOnlyStateVh: Boolean, editType: String, userPerfItem: UserPerfItem?, isTestPrueba: Boolean
    ) : RecyclerView.ViewHolder(itemView) {
        private val dbManagerVh = dbManager
        private val onRemoved = onItemRemoved
        private val context = con
        private val isReadOnlyState = isReadOnlyStateVh
        private val isTestPrueba = isTestPrueba
        private val editType = editType
        private val userPerfItem = userPerfItem
        fun setData(item: TestItem){

            val textName = item.testName
            itemView.tvTitle.text = textName
            if(isReadOnlyState)itemView.imBdelete.visibility = View.GONE
            itemView.imBdelete.setOnClickListener {
                dbManagerVh.deleteTestItem(item, adapterPosition, onRemoved)
            }

            itemView.setOnClickListener {
                var intent: Intent? = if(!isReadOnlyState){
                    Intent(context, AdminQuestionListAct::class.java).apply {
                        putExtra(DbConstants.TEST_ITEM_INTENT, item)
                        putExtra(DbConstants.EDIT_INTENT, true)
                        putExtra(DbConstants.NEW_TEST_DIR, item)
                    }
                } else {
                    Intent(context, TestAct::class.java).apply {
                        putExtra(DbConstants.TEST_ITEM_INTENT, item)
                        putExtra(DbConstants.USER_PERF_INTENT, userPerfItem)
                        putExtra(DbConstants.TEST_PRUEBA, isTestPrueba)
                    }
                }

                context.startActivity(intent)


            }
        }

    }

    fun updateAdapter(titleList:List<TestItem>){

        mainList.clear()
        mainList.addAll(titleList)
        notifyDataSetChanged()

    }

    override fun onItemRemoved(pos: Int) {
        mainList.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, mainList.size)
    }



}