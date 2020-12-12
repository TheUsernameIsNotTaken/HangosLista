package com.example.hangoslista;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Comparator;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(tableName = "SoundList")
public @Data @NoArgsConstructor
@ToString
class ListItem {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int itemId;

    private String itemName;

    private int itemPrice;

    private String user; //added in version 2
}

