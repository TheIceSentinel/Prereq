package com.novatulsa.dev.prereq;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RegisterActivity extends AppCompatActivity {

    // UI References
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText emailText;
    private EditText createPassText;
    private EditText confirmPassText;
    private Button btnSignUp;

    private UserRegisterTask registerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Setup UI elements
        firstNameText = (EditText) findViewById(R.id.firstName);
        lastNameText = (EditText) findViewById(R.id.lastName);
        emailText = (EditText) findViewById(R.id.email);
        createPassText = (EditText) findViewById(R.id.createPass);
        confirmPassText = (EditText) findViewById(R.id.confirmPass);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);

    }

    public void onSignUpClick(View view) {
        attemptRegister();
    }

    /**
     * Attempts to register the user using the info specified by the form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no registration attempt is made.
     */
    private void attemptRegister() {
        // Check to see if the registerTask is already running
        if (registerTask != null) {
            return;
        }

        // Reset errors.
        firstNameText.setError(null);
        lastNameText.setError(null);
        emailText.setError(null);
        createPassText.setError(null);
        confirmPassText.setError(null);

        // Store values at the time of the registration attempt.
        String firstName = firstNameText.getText().toString();
        String lastName = lastNameText.getText().toString();
        String email = emailText.getText().toString();
        String createPass = createPassText.getText().toString();
        String confirmPass = confirmPassText.getText().toString();

        // Create a View to set the focus on if an error is made
        boolean cancel = false;
        View focusView = null;

        // Validate user input, working from bottom to top.

        // Check to make sure password fields match each other
        // and for a valid password, if the user entered one.

        if (TextUtils.isEmpty(confirmPass)) {
            confirmPassText.setError(getString(R.string.error_field_required));
            focusView = confirmPassText;
            cancel = true;
        } else if (!confirmPass.equals(createPass)) {
            confirmPassText.setError(getString(R.string.error_password_match)); //change error
            focusView = confirmPassText;
            cancel = true;
        }
        if (TextUtils.isEmpty(createPass)) {
            createPassText.setError(getString(R.string.error_field_required));
            focusView = createPassText;
            cancel = true;
        } else if (!isPasswordValid(createPass)) {
            createPassText.setError(getString(R.string.error_invalid_password));
            focusView = createPassText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailText.setError(getString(R.string.error_field_required));
            focusView = emailText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailText.setError(getString(R.string.error_invalid_email));
            focusView = emailText;
            cancel = true;
        }

        // Check for a valid first name, if user entered one. (<=50)
        if (!TextUtils.isEmpty(firstName)) {
            if (!isNameValid(firstName)) {
                firstNameText.setError(getString(R.string.error_invalid_name)); // change error
                focusView = firstNameText;
                cancel = true;
            }
        }

        // Check for a valid last name, if user entered one. (<=50)
        if (!TextUtils.isEmpty(lastName)) {
            if (!isNameValid(lastName)) {
                lastNameText.setError(getString(R.string.error_invalid_name)); // change error
                focusView = lastNameText;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
            registerTask = new UserRegisterTask(email, createPass);
            registerTask.execute((Void) null);
        } else if (!TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
            registerTask = new UserRegisterTask(firstName, email, createPass);
            registerTask.execute((Void) null);
        } else if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
            registerTask = new UserRegisterTask(firstName, lastName, email, createPass);
            registerTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        // Replace this with your own logic
        return (email.contains("@okstate.edu")
                && email.length() <= 50);
    }

    private boolean isPasswordValid(String password) {
        // Replace this with your own logic
        return (password.length() > 8
                && password.length() < 26
                && password.matches("[a-z]+")
                && password.matches("[A-Z]+")
                && password.matches("[0-9]+"));
    }

    private boolean isNameValid(String name) {
        // Replace this with your own logic
        return (name.length() <= 50
                && name.matches("[a-zA-Z]+"));
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private String firstName; // Not required
        private String lastName; // Not required
        private String email;
        private String password;

        // Required fields only
        UserRegisterTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        UserRegisterTask(String firstName, String email, String password) {
            this.firstName = firstName;
            this.email = email;
            this.password = password;
        }

        // Constructor with all optional fields
        UserRegisterTask(String firstName, String lastName, String email, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Connection connection = connection();
                if(connection == null) {
                    return false;
                } else if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
                    // All fields provided
                    String sql = "INSERT INTO Student (Email, Password, FirstName, LastName) " +
                            "VALUES (" + email +
                            ", " + password +
                            ", " + firstName +
                            ", " + lastName +
                            ");";
                    connection.createStatement().executeUpdate(sql);
                    connection.close();
                    return true;
                } else if (!TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
                    // No last name provided
                    String sql = "INSERT INTO Student (Email, Password, FirstName) " +
                            "VALUES (" + email +
                            ", " + password +
                            ", " + firstName +
                            ");";
                    connection.createStatement().executeUpdate(sql);
                    connection.close();
                    return true;
                } else {
                    // Default (no names provided)
                    String sql = "INSERT INTO Student (Email, Password) " +
                            "VALUES (" + email +
                            ", " + password +
                            ");";
                    connection.createStatement().executeUpdate(sql);
                    connection.close();
                    return true;
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            registerTask = null;
            if (success) {
                finish();
                Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_LONG).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), "Registration attempt failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public Connection connection() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://novatulsa.database.windows.net:1433;" +
                    "DatabaseName=Catalog;" +
                    "user=jdoty@novatulsa;" +
                    "password=3Ecfsd%Tbhfg&Umkhj9;" +
                    "encrypt=true;" +
                    "trustServerCertificate=false;" +
                    "hostNameInCertificate=*.database.windows.net;" +
                    "loginTimeout=30;";
            connection = DriverManager.getConnection(ConnectionURL);
        } catch (ClassNotFoundException e) {
            Log.e("Error 1: ", e.getMessage());
        } catch (SQLException e) {
            Log.e("Error 2: ", e.getMessage());
        } catch (Exception e) {
            Log.e("Error 3: ", e.getMessage());
        }
        return connection;
    }
}

