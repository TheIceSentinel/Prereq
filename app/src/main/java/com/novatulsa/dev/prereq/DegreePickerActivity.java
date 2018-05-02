package com.novatulsa.dev.prereq;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// At present, only one degree may be selected per student

public class DegreePickerActivity extends AppCompatActivity {

    // User variables
    private static String USER_EMAIL;
    private static int CWID;

    private DegreeListTask fetchTask = null;
    private DegreeRegisterTask registerTask = null;

    // Arrays and Lists
    private ArrayAdapter<String> degreeAdapter;
    public ArrayList<String> fetchedDegree;
    public ArrayList<String> selectedDegree;

    // UI References
    private ListView degreeListView;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_degree_picker);

        // Setup user information from Intent
        USER_EMAIL = getIntent().getStringExtra("UserEmail");
        CWID = getIntent().getIntExtra("CWID", 0);

        // Initialize Arrays and Adapters
        fetchedDegree = new ArrayList<String>();
        selectedDegree = new ArrayList<String>();
        degreeAdapter = new ArrayAdapter<String>(this, R.layout.degree_choice, R.id.degreeChoice, fetchedDegree);

        // Initialize UI elements
        degreeListView = (ListView) findViewById(R.id.degreeList);
        degreeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        degreeListView.setAdapter(degreeAdapter);

        // Populate the degree list
        fetchTask = new DegreeListTask();
        fetchTask.execute((Void) null);

        // Capture user interactions
        degreeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Capture user selection
                String selectedItem = ((TextView)view).getText().toString();
                Log.e("Variable", selectedItem);

                // Determine if the item is not checked
                // If so, toggle the check mark (check)
                if (!selectedDegree.contains(selectedItem)) {
                    selectedDegree.add(selectedItem);
                } else {
                    // Otherwise, toggle the check mark off
                    selectedDegree.remove(selectedItem);
                }
            }
        });

        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate the ArrayList for only one selection
                if (selectedDegree.size() > 1) {
                    Toast.makeText(getApplicationContext(), "Currently, only 1 degree may be selected", Toast.LENGTH_LONG).show();
                } else {
                    // Only pass the first degree choice for the moment
                    String degree = selectedDegree.get(0).toString();
                    registerTask = new DegreeRegisterTask(CWID, degree);
                    registerTask.execute((Void) null);
                }
            }
        });
    }

    public class DegreeRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private String selectedDegree;
        private int cwid;

        DegreeRegisterTask(int cwid, String selectedDegree) {
            this.selectedDegree = selectedDegree;
            this.cwid = cwid;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Connection connection = connection();
                if (connection == null) {
                    return false;
                } else {
                    String sql = "INSERT INTO Student_Degree (CWID, DegreeID) " +
                            "VALUES (" + cwid +
                            ", (SELECT DegreeID FROM Degree " +
                            "WHERE DegreeName = '" + selectedDegree +
                            "'));";
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
                Intent intent = new Intent(DegreePickerActivity.this, NextCourseActivity.class);
                intent.putExtra("UserEmail", USER_EMAIL);
                intent.putExtra("CWID", CWID);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Degree selection failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class DegreeListTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection connection = connection();
                if (connection == null) return false;
                else {
                    // Clear out the array for a new result set
                    fetchedDegree.clear();

                    String sql = "SELECT DegreeName FROM Degree;";
                    ResultSet resultSet = connection.createStatement().executeQuery(sql);

                    // Read through resultSet
                    while (resultSet.next()) {
                        fetchedDegree.add(resultSet.getString(1));
                    }

                    // Close connection after data is read
                    connection.close();
                    return true;
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            // Once done, null task (opt. remove progress)
            fetchTask = null;

            if (success) {
                degreeAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onCancelled() {
            // If cancelled, null task and hide progress bar
            fetchTask = null;
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
