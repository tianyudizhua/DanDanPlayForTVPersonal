package com.seiko.torrent.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.seiko.torrent.model.TorrentEntity

@Database(entities = [
    TorrentEntity::class
], version = 1)
@TypeConverters(PriorityListConverter::class)
abstract class TorrentDatabase : RoomDatabase() {


    abstract fun torrentDao(): TorrentDao
}