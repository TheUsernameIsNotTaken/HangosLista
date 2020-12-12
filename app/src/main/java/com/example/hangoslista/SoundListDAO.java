package com.example.hangoslista;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SoundListDAO {
    @Insert
    void insertItem(ListItem item);

    @Query("SELECT * FROM SoundList")
    LiveData<List<ListItem>> getAllItems();

    @Query("SELECT * FROM SoundList WHERE user LIKE :userName ")
    LiveData<List<ListItem>> getUserItems(String userName);

    @Query("SELECT * FROM SoundList WHERE user LIKE :userName ORDER BY itemName ASC, itemPrice ASC")
    LiveData<List<ListItem>> getUserItemsByName(String userName);

    @Query("SELECT * FROM SoundList WHERE user LIKE :userName ORDER BY itemPrice ASC, itemName ASC  ")
    LiveData<List<ListItem>> getUserItemsByPrice(String userName);

    @Query("DELETE FROM SoundList")
    void clearDB();

    @Query("DELETE FROM SoundList WHERE user LIKE :userName")
    void clearUserDB(String userName);

    @Delete
    public void deleteFromList(ListItem item);
}
