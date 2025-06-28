// ContactDao.java
package com.example.contactapp; // Or your specific package

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ContactDao {
    @Insert
    void insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("DELETE FROM contact_table") // Assuming your table is named contact_table
    void deleteAllContacts();


    @Query("DELETE FROM contact_table WHERE id = :contactId")
    void deleteById(int contactId);

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    LiveData<List<Contact>> getAllContacts();

    @Query("SELECT * FROM contact_table WHERE id = :contactId")
    LiveData<Contact> getContactById(int contactId);

    @Query("SELECT * FROM contact_table")
    List<Contact> getAllContactsStatic();


    // ✅ Add this method for search
    @Query("SELECT * FROM contact_table WHERE name LIKE '%' || :query || '%' OR phone_number LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<Contact>> search(String query);

}