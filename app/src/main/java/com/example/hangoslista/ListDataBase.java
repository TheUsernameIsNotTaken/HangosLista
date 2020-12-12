package com.example.hangoslista;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ListItem.class}, version = 2, exportSchema = false)
public abstract class ListDataBase extends RoomDatabase {
    public abstract SoundListDAO soundListDAO();
}
