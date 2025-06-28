// ContactRepository.java

package com.example.contactapp;
import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.contactapp.ContactDao;

import java.util.List;


public class ContactRepository {
    private ContactDao contactDao;
    private LiveData<List<Contact>> allContacts;

    public ContactRepository(Application application) {
        ContactDatabase database = ContactDatabase.getInstance(application);
        contactDao = database.contactDao();
        allContacts = contactDao.getAllContacts();
    }
    public LiveData<List<Contact>> search(String query) {
        return contactDao.search(query);
    }

    public void insert(Contact contact) {
        // Room executes insertions on a separate thread by default
        // For other operations like update/delete, you should handle background execution
        ContactDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.insert(contact);
        });
    }

    public void update(Contact contact) {
        ContactDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.update(contact);
        });
    }

    public void delete(Contact contact) {
        ContactDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.delete(contact);
        });
    }

    public void deleteAllContacts() {
        ContactDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.deleteAllContacts();
        });
    }

    public LiveData<List<Contact>> getAllContacts() {
        return allContacts;
    }

    public LiveData<Contact> getContactById(int contactId) {
        return contactDao.getContactById(contactId);
    }
}