package com.example.notefire_local.data.local

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*

@Entity(tableName = "notes")
data class NotesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id") val noteId: Int? = null,
    @ColumnInfo(name = "note_title") val noteTitle: String? = null,
    @ColumnInfo(name = "note_description") val noteDescription: String? = null,
    @ColumnInfo(name = "time") val date: String = System.currentTimeMillis().toString()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(noteId)
        parcel.writeString(noteTitle)
        parcel.writeString(noteDescription)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotesEntity> {
        override fun createFromParcel(parcel: Parcel): NotesEntity {
            return NotesEntity(parcel)
        }

        override fun newArray(size: Int): Array<NotesEntity?> {
            return arrayOfNulls(size)
        }
    }
}
