package com.test_gg_ua.testapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.act.NewQuestionAct
import com.test_gg_ua.testapp.db_manager.DbConstants
import com.test_gg_ua.testapp.db_manager.DbManager
import com.test_gg_ua.testapp.db_manager.OnItemRemoved
import com.test_gg_ua.testapp.db_manager.QuestionItem
import kotlinx.android.synthetic.main.question_item_layout.view.*

class QuestionsAdapter(dbManager:DbManager) : RecyclerView.Adapter<QuestionsAdapter.MyHolder>(), OnItemRemoved {
    private val mainList = ArrayList<QuestionItem>()
    private var dbManagerAd:DbManager = dbManager
    private lateinit var testDir: String


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.question_item_layout,parent, false)
        return MyHolder(testDir,view, dbManagerAd, this, parent.context)
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.setData(mainList[position])
    }

    class MyHolder(testDir: String,itemView: View, dbManager: DbManager, onRemovedQ: OnItemRemoved, con:Context) : RecyclerView.ViewHolder(itemView) {
        private val dbManagerVh = dbManager
        private val onRemoved = onRemovedQ
        private val context = con
        private var testDir = testDir
        fun setData(item:QuestionItem){

            itemView.tvTitle.text = item.title
            itemView.imBdelete.setOnClickListener {
                dbManagerVh.deleteQuestionItem(item, adapterPosition)
            }
            itemView.setOnClickListener {

                val intent = Intent(context, NewQuestionAct::class.java).apply {

                    putExtra(DbConstants.QUESTION_ITEM_INTENT, item)
                    putExtra(DbConstants.NEW_TEST_DIR, testDir)
                    putExtra(DbConstants.EDIT_INTENT, true)

                }
                context.startActivity(intent)


            }
        }

    }

    fun updateAdapter(titleList:List<QuestionItem>){

        mainList.clear()
        mainList.addAll(titleList)
        notifyDataSetChanged()
    }

    override fun onItemRemoved(pos: Int) {
        mainList.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, mainList.size)
    }

    fun setTestDir(testDir:String){
        this.testDir =testDir
    }
    fun setOnItemRemoved(onRemovedQ: OnItemRemoved){
        dbManagerAd.setItemRemovedListener(onRemovedQ)
    }


}