package com.example.contactapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

public class ContactDetailActivity extends AppCompatActivity {

    private TextView detailName, detailPhone;
    private Button btnCall, btnMessage, btnEdit, btnDelete;

    private ContactDao contactDao;
    private int contactId;
    private Contact currentContact; // will be updated from DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        detailName = findViewById(R.id.detailName);
        detailPhone = findViewById(R.id.detailPhone);

        ImageView btnEdit = findViewById(R.id.btnEdit);
        ImageView btnCall = findViewById(R.id.btnCall);
        ImageView btnMessage = findViewById(R.id.btnMessage);
        ImageView btnDelete = findViewById(R.id.btnDelete);
        ImageView shareIcon = findViewById(R.id.shareIcon);

        shareIcon.setOnClickListener(v -> {
            TextView nameView = findViewById(R.id.detailName);
            TextView phoneView = findViewById(R.id.detailPhone);

            String name = nameView.getText().toString();
            String phone = phoneView.getText().toString();

            String shareText = "Contact Info:\nName: " + name + "\nPhone: " + phone;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact Info");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });


        contactDao = ContactDatabase.getInstance(this).contactDao();

        contactId = getIntent().getIntExtra("contact_id", -1);
        if (contactId == -1) {
            Toast.makeText(this, "Invalid contact ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Observe live contact from DB
        contactDao.getContactById(contactId).observe(this, new Observer<Contact>() {
            @Override
            public void onChanged(Contact contact) {
                if (contact != null) {
                    currentContact = contact;
                    detailName.setText(contact.getName());
                    detailPhone.setText(contact.getPhoneNumber());
                }
            }
        });

        btnCall.setOnClickListener(v -> {
            if (currentContact != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + currentContact.getPhoneNumber()));
                startActivity(intent);
            }
        });

        btnMessage.setOnClickListener(v -> {
            if (currentContact != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + currentContact.getPhoneNumber()));
                startActivity(intent);
            }
        });

        btnEdit.setOnClickListener(v -> {
            if (currentContact != null) {
                Intent intent = new Intent(this, EditContactActivity.class);
                intent.putExtra("contact_id", currentContact.getId());
                intent.putExtra("contact_name", currentContact.getName());
                intent.putExtra("contact_phone", currentContact.getPhoneNumber());
                startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (currentContact != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Contact")
                        .setMessage("Are you sure you want to delete this contact?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            new Thread(() -> {
                                contactDao.delete(currentContact);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }).start();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }
}
