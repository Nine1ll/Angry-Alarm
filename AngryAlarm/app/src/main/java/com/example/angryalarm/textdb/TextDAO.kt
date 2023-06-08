package kr.nine1ll.newtext.textdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TextDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(textTable: TextTable)

    @Update
    fun update(textTable: TextTable)

    @Delete
    fun delete(textTable: TextTable)

    @Query("SELECT * FROM TEXTTABLE")
    fun selectAll(): MutableList<TextTable>

    @Query("DELETE FROM TEXTTABLE WHERE message=:message")
    fun deleteByMessage(message: String?)

    @Query("SELECT * FROM TEXTTABLE WHERE message=:message")
    fun selectByMessage(message: String?): TextTable

    @Query("SELECT * FROM TEXTTABLE WHERE alarmId=:alarmId")
    fun selectByAlarmId(alarmId: Int?): MutableList<TextTable>
}