package com.example.contactapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditContactActivity extends AppCompatActivity {

    private EditText editName, editPhone;
    private Button saveButton;
    private ContactDao contactDao;
    private int contactId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact); // Ensure layout file exists

        editName = findViewById(R.id.editContactName);
        editPhone = findViewById(R.id.editContactPhone);
        saveButton = findViewById(R.id.saveButton);

        contactDao = ContactDatabase.getInstance(this).contactDao();

        // Retrieve data from intent
        if (getIntent() != null) {
            contactId = getIntent().getIntExtra("contact_id", -1);
            String name = getIntent().getStringExtra("contact_name");
            String phone = getIntent().getStringExtra("contact_phone");

            if (name != null) editName.setText(name);
            if (phone != null) editPhone.setText(phone);
        }

        saveButton.setOnClickListener(v -> {
            String updatedName = editName.getText().toString().trim();
            String updatedPhone = editPhone.getText().toString().trim();

            if (updatedName.isEmpty() || updatedPhone.isEmpty()) {
                Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (contactId == -1) {
                Toast.makeText(this, "Invalid contact ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Contact updatedContact = new Contact(updatedName, updatedPhone);
            updatedContact.setId(contactId);  // Room needs the ID to update

            new Thread(() -> {
                contactDao.update(updatedContact);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }
}
