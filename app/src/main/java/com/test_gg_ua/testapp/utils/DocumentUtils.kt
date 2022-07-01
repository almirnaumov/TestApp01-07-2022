package com.test_gg_ua.testapp.utils

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.android.synthetic.main.activity_edit_info.*
import java.io.File
import java.io.FileOutputStream

object DocumentUtils {
    fun getFile(mContext: Activity?, documentUri: Uri): File {
        val inputStream = mContext?.contentResolver?.openInputStream(documentUri)
        var file =  File("")
        inputStream.use { input ->
            file =
                File(mContext?.cacheDir, getDisplayName(documentUri, mContext?.applicationContext!!))
            FileOutputStream(file).use { output ->
                val buffer =
                    ByteArray(100 * 1024) // or other buffer size
                var read: Int = -1
                while (input?.read(buffer).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }
        return file
    }

    private fun getDisplayName(uri:Uri, context: Context) : String{
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        val nameIndex: Int = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)!!
        cursor.moveToFirst()
        val name = cursor.getString(nameIndex)
        cursor.close()
        return name

    }
}