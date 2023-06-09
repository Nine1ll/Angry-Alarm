package com.example.angry_alarm

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class AlarmDatabase {
    object MyDBContract {
        object MyEntry : BaseColumns {
            const val TABLE_NAME = "ALARM"
            const val alarm_id = "alarm_id"
            const val title = "title"
            const val hour = "hour"
            const val minute = "minute"
            const val alarm_days = "alarm_days"
            const val repeat_count = "repeat_count"
            const val repeat_interval = "repeat_interval"
            const val isVibrator = "isVibrator"
            const val isSwitchOn = "isSwitchOn"
        }
    }

    class MyDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${MyDBContract.MyEntry.TABLE_NAME} (" +
                    "${MyDBContract.MyEntry.alarm_id} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${MyDBContract.MyEntry.title} TEXT," +
                    "${MyDBContract.MyEntry.hour} INTEGER," +
                    "${MyDBContract.MyEntry.minute} INTEGER," +
                    "${MyDBContract.MyEntry.alarm_days} TEXT," +
                    "${MyDBContract.MyEntry.repeat_count} INTEGER," +
                    "${MyDBContract.MyEntry.repeat_interval} INTEGER," +
                    "${MyDBContract.MyEntry.isVibrator} INTEGER DEFAULT 0," +
                    "${MyDBContract.MyEntry.isSwitchOn} INTEGER DEFAULT 0)"
        val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${MyDBContract.MyEntry.TABLE_NAME}"

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

        companion object {
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "myDBfile.db"
        }

        fun selectAll(): MutableList<MyElement> {
            val readList = mutableListOf<MyElement>()
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM " + MyDBContract.MyEntry.TABLE_NAME + ";", null)
            Log.d(
                "TAG",
                "Select All Query: " + "SELECT * FROM " + MyDBContract.MyEntry.TABLE_NAME + ";"
            )
            Log.d("TAG", cursor.toString())
            with(cursor) {
                while (moveToNext()) {
                    val alarmId = getInt(getColumnIndexOrThrow(MyDBContract.MyEntry.alarm_id))
                    val title = getString(getColumnIndexOrThrow(MyDBContract.MyEntry.title))
                    val hour = getInt(getColumnIndexOrThrow(MyDBContract.MyEntry.hour))
                    val minute = getInt(getColumnIndexOrThrow(MyDBContract.MyEntry.minute))
                    val alarmDays = getString(getColumnIndexOrThrow(MyDBContract.MyEntry.alarm_days))
                    val repeatCount = getInt(getColumnIndexOrThrow(MyDBContract.MyEntry.repeat_count))
                    val repeatInterval = getLong(getColumnIndexOrThrow(MyDBContract.MyEntry.repeat_interval))
                    val isVibrator = getInt(getColumnIndexOrThrow(MyDBContract.MyEntry.isVibrator)) == 1
                    val isSwitchOn = getInt(getColumnIndexOrThrow(MyDBContract.MyEntry.isSwitchOn)) == 1

                    readList.add(
                        MyElement(
                            alarmId,
                            title,
                            hour,
                            minute,
                            alarmDays,
                            repeatCount,
                            repeatInterval,
                            isVibrator,
                            isSwitchOn
                        )
                    )
                }
                cursor.close()
            }

            return readList
        }

        fun delEntry(alarmId:Int):Int{
            val db = writableDatabase

            val myentry = AlarmDatabase.MyDBContract.MyEntry
            val where = "${myentry.alarm_id} = ?"
            val whereArgs = arrayOf(alarmId.toString())
            return db.delete( myentry.TABLE_NAME,where,whereArgs);
        }
    }
}
