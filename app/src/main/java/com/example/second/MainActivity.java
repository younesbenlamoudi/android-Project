package com.example.second;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private EditText nameEditText, emailEditText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> userList;
    private List<User> userDataList;
    private User selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        listView = findViewById(R.id.listView);

        userList = new ArrayList<>();
        userDataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUser = userDataList.get(position);
                nameEditText.setText(selectedUser.getName());
                emailEditText.setText(selectedUser.getEmail());
            }
        });

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });

        Button updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        addValueEventListener();
    }

    private void addUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email)) {
            String userId = databaseReference.push().getKey();
            User user = new User(userId, name, email);
            databaseReference.child(userId).setValue(user);
            clearEditTextFields();
        }
    }

    private void updateUser() {
        if (selectedUser != null) {
            String newName = nameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();

            if (!TextUtils.isEmpty(newName) && !TextUtils.isEmpty(newEmail)) {
                selectedUser.setName(newName);
                selectedUser.setEmail(newEmail);
                databaseReference.child(selectedUser.getId()).setValue(selectedUser);
                clearEditTextFields();
                selectedUser = null;
            }
        }
    }

    private void deleteUser() {
        if (selectedUser != null) {
            databaseReference.child(selectedUser.getId()).removeValue();
            clearEditTextFields();
            selectedUser = null;
        }
    }

    private void addValueEventListener() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                userDataList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user.getName());
                        userDataList.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to read data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearEditTextFields() {
        nameEditText.setText("");
        emailEditText.setText("");
    }
}

