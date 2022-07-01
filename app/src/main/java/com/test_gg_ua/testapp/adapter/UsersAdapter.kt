package com.test_gg_ua.testapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.act.NewQuestionAct
import com.test_gg_ua.testapp.act.ResultActivity
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.question_item_layout.view.*

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.MyHolder>() {
    private val mainList = ArrayList<UserPerfItem>()
    //private var dbManagerAd:DbManager = dbManager


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.question_item_layout,parent, false)
        return MyHolder(view,  parent.context)
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.setData(mainList[position])
    }

    class MyHolder(itemView: View, con:Context) : RecyclerView.ViewHolder(itemView) {
        //private val dbManagerVh = dbManager
        //private val onRemoved = onRemovedQ
        private val context = con
        fun setData(item:UserPerfItem){

            val textName = "${item.userSecName} ${item.userName} ${item.userFatherName}"
            itemView.tvTitle.text = textName
            itemView.imBdelete.visibility = View.GONE
            /*itemView.imBdelete.setOnClickListener {
                dbManagerVh.deleteItem(item)
                onRemoved.onQuestionRemoved(adapterPosition)

            }*/

            itemView.setOnClickListener {

                val intent = Intent(context, ResultActivity::class.java).apply {
                    putExtra(DbConstants.USER_PERF_INTENT, item)
                }

                context.startActivity(intent)


            }
        }

    }

    fun updateAdapter(titleList:List<UserPerfItem>){

        mainList.clear()
        mainList.addAll(titleList)
        notifyDataSetChanged()

    }

    /*override fun onQuestionRemoved(pos: Int) {
        mainList.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, mainList.size)
    }*/


}