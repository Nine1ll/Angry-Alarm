package kr.nine1ll.newtext.textdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TextTable::class], version = 1, exportSchema = false)
abstract class TextDaoDatabase: RoomDatabase(){
    abstract fun textDAO(): TextDAO

    companion object{
        private var instance: TextDaoDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): TextDaoDatabase?{
            if (instance==null){
                synchronized(TextDaoDatabase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TextDaoDatabase::class.java,
                        "text-database"
                    ).build()
                }
            }
            return instance
        }
    }
}