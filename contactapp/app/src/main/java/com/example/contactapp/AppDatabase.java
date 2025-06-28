package com.example.contactapp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Contact.class}, version = 2) // Increment version
public abstract class AppDatabase extends RoomDatabase {

    public abstract ContactDao contactDao();

    private static volatile AppDatabase INSTANCE;

    // Define the migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Since the 'phone' column was added, add it to the contact_table
            database.execSQL("ALTER TABLE contact_table ADD COLUMN phone TEXT");
            // If you want to give it a default value for existing rows, you can do:
            // database.execSQL("ALTER TABLE contact_table ADD COLUMN phone TEXT DEFAULT ''");
            // Or set it to NOT NULL and provide a default
            // database.execSQL("ALTER TABLE contact_table ADD COLUMN phone TEXT NOT NULL DEFAULT ''");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "contact_database")
                            // Add the migration
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}