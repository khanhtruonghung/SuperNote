package com.truongkhanh.supernote.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "Todo_Tag_List",
    primaryKeys = ["Todo_ID", "Tag_Type_ID"],
    indices = [Index("Tag_Type_ID")],
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Todo::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["Todo_ID"]
        ),
        ForeignKey(
            entity = TagType::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["Tag_Type_ID"]
        )
    )
)
data class TodoTagList(
    @ColumnInfo(name = "Todo_ID")
    var todoID: Int,
    @ColumnInfo(name = "Tag_Type_ID")
    var tagTypeID: Int
)