package com.test_gg_ua.testapp.db_manager

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.test_gg_ua.testapp.utils.DocumentUtils
import java.io.ByteArrayOutputStream


class DbManager(onAdminStateListenerM: OnAdminStateListener?) {

    //Управление базой данных
    private val fb = FirebaseDatabase.getInstance()
    private val onAdminStateListener = onAdminStateListenerM
    private val db = FirebaseDatabase.getInstance()
    private val dbStorage = FirebaseStorage.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private var qListener:OnQuestionReceivedListener? = null
    private var onItemDeletedM: OnItemDeleted? = null
    private var onItemSaved: OnItemSaved? = null
    private var onPerfRead: OnPerfRead? = null
    private var onUsersReceived: OnUsersReceived? = null
    lateinit var onTestReceived: OnTestsReceived
    lateinit var onItemRemoved: OnItemRemoved

    //Проверка админа
    fun isAdmin(mAuth: FirebaseAuth) {
       // Log.d("MyLog","Admin key 0 ")
        val dbAdminRef = fb.getReference(mAuth.currentUser!!.uid)
        dbAdminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                onAdminStateListener?.onAdminStateChanged(false)
            }

            override fun onDataChange(dataSnap: DataSnapshot) {

                val adminKey = dataSnap.value.toString()
                var isAdmin = false
                if (adminKey == "admin") {
                    isAdmin = true
                }
                onAdminStateListener?.onAdminStateChanged(isAdmin)

            }

        })

    }
    //Получаем вопросы из базы данных
    fun getQuestions(path:String){

        val dbRef = fb.getReference(path)
        val qList = ArrayList<QuestionItem>()
        if(mAuth.uid == null) return
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Log.d("MyLog","Data size : " + snapshot.childrenCount)
                if (snapshot.childrenCount > 0) {

                   qList.clear()
                   for (ds: DataSnapshot in snapshot.children) {

                       val checkPoint: String = ds.children.iterator().next().key!!
                       if(checkPoint != "testDir"){

                           for (ds2: DataSnapshot in ds.children) {

                               val item: QuestionItem = ds2.child("pregunta").getValue(QuestionItem::class.java)!!
                               qList.add(item)
                           }

                       }

                   }
                }
                qListener?.onQuestionReceived(qList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
    //Записываем текстувую часть вопросов
    private fun addTextQuestionToDb(testDir: String, questionItem: QuestionItem, isEditMode: Boolean){

        if(mAuth.uid == null)return
        val dRef = db.getReference(testDir)
        var keyD = ""
        if(!isEditMode){
            keyD = dRef.push().key.toString()
            questionItem.key = keyD
        } else {
            keyD = questionItem.key
        }


        if(mAuth.uid == null) return
        //Log.d("MyLog","Test dir 2 $testDir")
        dRef.child(mAuth.uid!!).child(keyD).child("pregunta").setValue(questionItem)
                .addOnCompleteListener { task->

                    if(task.isSuccessful){

                        onItemSaved?.onSaveItem()

                    }

                }

    }
    //Добавляем вопрос (Картинка и текст)
    fun addQuestionToDb(testDir:String,
        questionItem: QuestionItem,
        bitMap: Bitmap?,
        isImageSelected: Int,
        isEditMode: Boolean
    ){

        if(isImageSelected == EditImageConst.IMAGE_NOT_SELECTED || isImageSelected == EditImageConst.IMAGE_FROM_FB){

            addTextQuestionToDb(testDir, questionItem, isEditMode)

        } else if(isImageSelected == EditImageConst.IMAGE_TO_DELETE_FB) {

            deleteQuestionImage(testDir, questionItem, isEditMode)

        } else {


            val stRefChild:StorageReference?
            val outStream = ByteArrayOutputStream()
            bitMap?.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
            val byteArray = outStream.toByteArray()

            stRefChild = if(!isEditMode || questionItem.imagePath == "empty"){

                val stRef = dbStorage.getReference(DbConstants.TEST_IM_DIR)
                stRef.child("${System.currentTimeMillis()}_image")

            } else {

                dbStorage.getReferenceFromUrl(questionItem.imagePath)

            }
            val uploadTask = stRefChild.putBytes(byteArray)
            uploadTask.continueWithTask{ stRefChild.downloadUrl }
                    .addOnCompleteListener{ task ->

                        if(task.isSuccessful){
                            questionItem.imagePath = task.result.toString()
                            addTextQuestionToDb(testDir,questionItem, isEditMode)
                        }

                    }
        }

    }
    //Удаляем картинку вопроса
    private fun deleteQuestionImage(testDir: String,item: QuestionItem, isEditMode: Boolean){

        val dStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(item.imagePath)
        dStorageRef.delete().addOnCompleteListener { task->

            if(task.isSuccessful){

                item.imagePath = "empty"
                addTextQuestionToDb(testDir, item, isEditMode)

            }

        }
    }
    //Удаление картинку вопроса
    fun deleteQuestionItem(item: QuestionItem, pos:Int) {

        if (item.imagePath == "empty") {

            deleteQuestionFromDb(item, pos)

        } else {

            val dStorageRef = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(item.imagePath)
            dStorageRef.delete().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    deleteQuestionFromDb(item, pos)
                }
            }

        }

    }

    private fun deleteQuestionFromDb(item: QuestionItem, pos:Int){
        val dRef = FirebaseDatabase.getInstance().getReference(item.testCat)
        dRef.child(mAuth.uid.toString()).child(item.key).removeValue().addOnCompleteListener { task->

            if(task.isSuccessful)onItemRemoved.onItemRemoved(pos)

        }
    }

    fun setOnQReceivedListener(listener: OnQuestionReceivedListener){
        qListener = listener
    }
    //Получаем данные пользователя
    fun getUsers(){

        val dbRef = fb.getReference(DbConstants.USER_PATH)
        val qList = ArrayList<UserPerfItem>()
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MyLog", "Data size : " + snapshot.childrenCount)
                if (snapshot.childrenCount > 0) {
                    // val snapData:DataSnapshot = snapshot.children.iterator().next()

                    qList.clear()
                    for (ds: DataSnapshot in snapshot.children) {

                        val item: UserPerfItem = ds.getValue(UserPerfItem::class.java)!!

                        qList.add(item)

                    }
                }
                onUsersReceived?.onUsersReceived(qList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    fun getUserPerf(){
        val dbRef = fb.getReference(DbConstants.USER_PATH).child(mAuth.uid.toString())
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.childrenCount <= 0) return
                onPerfRead?.onPerfRead(snapshot.getValue(UserPerfItem::class.java)!!)

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    //Сохраняем данные пользователя
    fun saveUserPerf(userItem: UserPerfItem, context: Context){

        if(mAuth.uid == null)return
        val dRef = db.getReference(DbConstants.USER_PATH)

        dRef.child(mAuth.uid!!).setValue(userItem)
                .addOnCompleteListener { task->

                    if(task.isSuccessful){

                        Toast.makeText(
                            context,
                            "Данные пользователя успешно сохранены",
                            Toast.LENGTH_LONG
                        ).show()

                    }

                }

    }

    fun setOnUserPerfRead(onPerfRead: OnPerfRead){

        this.onPerfRead = onPerfRead

    }

    fun setOnUsersReceiverListener(onUsersReceiver: OnUsersReceived){

        this.onUsersReceived = onUsersReceiver

    }

    //Info
    private fun saveTextInfo(path:String,item: InfoItem, isInfoEditMode: Boolean){
        if(mAuth.uid == null)return
        val dRef = db.getReference(path)
        val keyD:String
        if(!isInfoEditMode){
            keyD = dRef.push().key.toString()
            item.key = keyD
        } else {
            keyD = item.key
        }

        dRef.child(mAuth.uid!!).child(keyD).setValue(item)
            .addOnCompleteListener { task->

                if(task.isSuccessful){

                    onItemSaved?.onSaveItem()

                }

            }
    }

    fun saveInfoToDb(path: String, item: InfoItem, isInfoEditMode: Boolean, mContext: Activity, newFilePath:String){


        if(newFilePath.isNotEmpty()){
        lateinit var saveRef : StorageReference
        val fileToLoad = Uri.fromFile(DocumentUtils.getFile(mContext, newFilePath.toUri()))
        saveRef = if(isInfoEditMode){
            Log.d("MyLog","Path : ${item.infoPdfPath}")
            dbStorage.getReferenceFromUrl(item.infoPdfPath)
        } else {
            val stRef = dbStorage.getReference(path)
            stRef.child("${System.currentTimeMillis()}_pdf")
        }
        val uploadTask = saveRef.putFile(fileToLoad)
        uploadTask.continueWithTask{ saveRef.downloadUrl }
            .addOnCompleteListener{ task ->

                if(task.isSuccessful){
                    Log.d("MyLog", "Uri " + task.result.toString())
                    item.infoPdfPath = task.result.toString()
                    saveTextInfo(path,item, isInfoEditMode)

                }

            }
        } else {
          //  Log.d("MyLog", "Uri ")
            saveTextInfo(path,item, isInfoEditMode)
        }
    }

    fun deleteInfoItem(item: InfoItem, pos: Int, onRemoved: OnItemRemoved) {
        if (item.infoPdfPath.isNotEmpty()) {
            val dStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(item.infoPdfPath)
            dStorageRef.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val dRef = FirebaseDatabase.getInstance().getReference(DbConstants.THEORY_DIR)
                    dRef.child(mAuth.uid.toString()).child(item.key).removeValue()
                        .addOnCompleteListener { task2 ->

                            if (task2.isSuccessful) onRemoved.onItemRemoved(pos)

                        }
                }
            }

        }

    }

    fun getInfoItemsFromDb(onItemsReceived: OnInfoItemsListener, path:String){
        val dbRef = fb.getReference(path)
        val iList = ArrayList<InfoItem>()
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Log.d("MyLog","Data size : " + snapshot.childrenCount)
                if (snapshot.childrenCount > 0) {
                    //val snapData:DataSnapshot = snapshot.children.iterator().next()
                    iList.clear()
                    for (ds: DataSnapshot in snapshot.children)
                        for (ds: DataSnapshot in ds.children) {

                            val item: InfoItem = ds.getValue(InfoItem::class.java)!!

                            iList.add(item)

                        }
                }
                onItemsReceived.onInfoItemListener(iList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    //New Test Methods
    fun createNewTestNode(testName:String){
        val dRef = db.getReference(DbConstants.TEST_PATH)
        val testItem = TestItem()
        testItem.testDir = DbConstants.TEST_PATH + "/$testName"
        testItem.testName = testName
        if(testName.isNotEmpty())dRef.child(testName).child(testName).setValue(testItem)
            .addOnCompleteListener {task->
                if(task.isSuccessful){
                    onItemSaved?.onSaveItem()
                }

        }
    }

    fun readDataFromTestNode(){
        val dRef = db.getReference(DbConstants.TEST_PATH)
        val testsList = ArrayList<TestItem>()
        dRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                testsList.clear()
                for(ds:DataSnapshot in snapshot.children){
                    for(ds2:DataSnapshot in ds.children){
                        val testItem = ds2.getValue(TestItem::class.java)!!
                    if(testItem.testDir.isNotEmpty())testsList.add(testItem)
                    }
                }
                testsList.reverse()
                onTestReceived.onTestReceived(testsList)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    fun deleteTestItem(item:TestItem, pos:Int, onRemoved: OnItemRemoved){
        val dRef = FirebaseDatabase.getInstance().getReference(DbConstants.TEST_PATH)
        dRef.child(item.testName).removeValue().addOnCompleteListener {task->
            if(task.isSuccessful){
                onRemoved.onItemRemoved(pos)
            }
        }
    }

    //Stters & Getters
    fun setOnItemDeleted(onItemDeleted: OnItemDeleted){

        onItemDeletedM = onItemDeleted

    }

    fun setOnItemSaved(onItemSavedT: OnItemSaved){

        onItemSaved = onItemSavedT

    }

    fun setOnTestReceivedListener(onTestsReceived: OnTestsReceived){
        this.onTestReceived = onTestsReceived
    }

    fun setItemRemovedListener(onItemRemoved: OnItemRemoved){
        this.onItemRemoved = onItemRemoved
    }




}