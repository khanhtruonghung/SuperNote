package com.truongkhanh.supernote.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Draft_Todo")
data class DraftTodo(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "Title")
    var title: String?,
    @ColumnInfo(name = "Description")
    var description: String?,
    @ColumnInfo(name = "Check_List")
    var checkList: String?,
    @ColumnInfo(name = "Priority")
    var priority: Int?,
    @ColumnInfo(name = "Start_Date")
    var startDate: Long?,
    @ColumnInfo(name = "End_Date")
    var endDate: Long?,
    @ColumnInfo(name = "Check_Done_Date")
    var checkDoneDate: Long?
)
