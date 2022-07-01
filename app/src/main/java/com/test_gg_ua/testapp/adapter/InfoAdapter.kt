package com.test_gg_ua.testapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test_gg_ua.testapp.R
import com.test_gg_ua.testapp.act.EditInfoAct
import com.test_gg_ua.testapp.act.ReadInfoAct
import com.test_gg_ua.testapp.db_manager.*
import kotlinx.android.synthetic.main.question_item_layout.view.*

class InfoAdapter(dbManager: DbManager, isReadOnlyStateA: Boolean, editType:String) : RecyclerView.Adapter<InfoAdapter.MyHolder>(), OnItemRemoved {
    private val mainList = ArrayList<InfoItem>()
    private var dbManagerAd:DbManager = dbManager
    private var isReadOnlyState = isReadOnlyStateA
    private var editType = editType



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.question_item_layout,parent, false)
        return MyHolder(view,  parent.context, dbManagerAd, this, isReadOnlyState, editType)
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.setData(mainList[position])
    }

    class MyHolder(itemView: View, con:Context, dbManager: DbManager,
                   onItemRemoved: OnItemRemoved,
                   isReadOnlyStateVh: Boolean, editType:String) : RecyclerView.ViewHolder(itemView) {
        private val dbManagerVh = dbManager
        private val onRemoved = onItemRemoved
        private val context = con
        private val isReadOnlyState = isReadOnlyStateVh
        private val editType = editType
        fun setData(item:InfoItem){

            val textName = item.titleInfo
            itemView.tvTitle.text = textName
            if(isReadOnlyState)itemView.imBdelete.visibility = View.GONE
            itemView.imBdelete.setOnClickListener {
                dbManagerVh.deleteInfoItem(item, adapterPosition, onRemoved)
            }

            itemView.setOnClickListener {
                var intent:Intent? = if(!isReadOnlyState){
                    Intent(context, EditInfoAct::class.java)
                } else {
                    Intent(context, ReadInfoAct::class.java)
                }

                intent?.apply {

                    putExtra(DbConstants.TEST_ITEM_INTENT, item)
                    putExtra(DbConstants.EDIT_INTENT, true)
                    putExtra(DbConstants.EDIT_TYPE_INTENT, editType)
                }

                context.startActivity(intent)


            }
        }

    }

    fun updateAdapter(titleList:List<InfoItem>){

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