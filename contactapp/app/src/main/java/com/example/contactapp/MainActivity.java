package com.example.contactapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private Button addContactButton;
    private EditText searchField;
    private ContactDao contactDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        addContactButton = findViewById(R.id.addContactButton);
        searchField = findViewById(R.id.searchField);

        adapter = new ContactAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ContactDatabase db = ContactDatabase.getInstance(this);
        contactDao = db.contactDao();

        contactDao.getAllContacts().observe(this, contacts -> adapter.setContacts(contacts));

        addContactButton.setOnClickListener(v -> showAddContactDialog());

        searchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    contactDao.getAllContacts().observe(MainActivity.this, contacts -> adapter.setContacts(contacts));
                } else {
                    contactDao.search(query).observe(MainActivity.this, contacts -> adapter.setContacts(contacts));
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        adapter.setOnContactActionListener(new ContactAdapter.OnContactActionListener() {
            @Override
            public void onContactClick(Contact contact) {
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                intent.putExtra("contact_id", contact.getId());
                intent.putExtra("contact_name", contact.getName());
                intent.putExtra("contact_phone", contact.getPhoneNumber());
                startActivity(intent);
            }

            @Override
            public void onCallClick(Contact contact) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
                startActivity(callIntent);
            }
        });
    }

    private void showAddContactDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        EditText editName = dialogView.findViewById(R.id.editContactName);
        EditText editPhone = dialogView.findViewById(R.id.editContactPhone);

        new AlertDialog.Builder(this)
                .setTitle("Add New Contact")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String phone = editPhone.getText().toString().trim();

                    if (!name.isEmpty() && !phone.isEmpty()) {
                        Contact newContact = new Contact(name, phone);
                        new Thread(() -> contactDao.insert(newContact)).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_export) {
            exportContactsToJson();
            return true;
        } else if (id == R.id.menu_import) {
            importContactsFromJson();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportContactsToJson() {
        new Thread(() -> {
            try {
                List<Contact> contacts = contactDao.getAllContactsStatic(); // Must exist in DAO
                Gson gson = new Gson();
                String json = gson.toJson(contacts);

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "contacts_backup.json");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void importContactsFromJson() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "contacts_backup.json");
        if (!file.exists()) {
            Toast.makeText(this, "No import file found.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Contact[] contacts = gson.fromJson(reader, Contact[].class);

                for (Contact contact : contacts) {
                    contact.setId(0); // Let Room auto-generate ID
                    contactDao.insert(contact);
                }

                runOnUiThread(() ->
                        Toast.makeText(this, "Contacts imported successfully", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
