// ContactDatabase.java
package com.example.contactapp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService; // Import
import java.util.concurrent.Executors;    // Import

@Database(entities = {Contact.class}, version = 2)
public abstract class ContactDatabase extends RoomDatabase {

    private static ContactDatabase instance;

    public abstract ContactDao contactDao();

    // Define an ExecutorService for background database operations
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static synchronized ContactDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ContactDatabase.class, "contact_database")
                    // .allowMainThreadQueries() // REMOVE this for production
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}