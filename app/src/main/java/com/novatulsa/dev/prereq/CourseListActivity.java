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

public class CourseListActivity extends AppCompatActivity {

    // User variables
    private static String USER_EMAIL;
    private static int CWID;

    // Class variables
    private CourseListTask fetchTask = null;
    private CourseRegisterTask registerTask = null;

    private ArrayAdapter<String> courseAdapter;
    public ArrayList<String> fetchedCourse;
    public ArrayList<String> selectedCourse;

    // UI References
    private ListView courseListView;
    private Button btnConfirm;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        // Setup user information from Intent
        USER_EMAIL = getIntent().getStringExtra("UserEmail");
        CWID = getIntent().getIntExtra("CWID", 0);

        // Initialize Arrays and Adapters
        fetchedCourse = new ArrayList<String>(); // Holds courses that you can take
        selectedCourse = new ArrayList<String>(); // Holds courses you want to add
        courseAdapter = new ArrayAdapter<String>(this, R.layout.course_choice, R.id.courseChoice, fetchedCourse);

        // Initialize UI elements
        courseListView = (ListView) findViewById(R.id.completeCourseList);
        courseListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        courseListView.setAdapter(courseAdapter);

        // Populate the course list
        fetchTask = new CourseListTask(CWID);
        fetchTask.execute();

        // OnClickListeners for buttons and list items
        btnConfirm = (Button) findViewById(R.id.confirm_button);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add the selected courses to the users course list
                attemptRegister();

                Intent intent = new Intent(CourseListActivity.this, NextCourseActivity.class);
                intent.putExtra("UserEmail", USER_EMAIL);
                intent.putExtra("CWID", CWID);
                startActivity(intent);
            }
        });

        btnCancel = (Button) findViewById(R.id.cancel_button);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Should clear out the array of any selected items
                selectedCourse.clear();

                Intent intent = new Intent(CourseListActivity.this, NextCourseActivity.class);
                intent.putExtra("UserEmail", USER_EMAIL);
                intent.putExtra("CWID", CWID);
                startActivity(intent);
            }
        });

        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Capture user selection
                String selectedItem = ((TextView)view).getText().toString();

                // Determine if the item is not checked
                // If so, toggle the check mark (check)
                if (!selectedCourse.contains(selectedItem)) {
                    selectedCourse.add(selectedItem);
                } else {
                    // Otherwise, toggle the check mark off
                    selectedCourse.remove(selectedItem);
                }
            }
        });
    }

    private void attemptRegister() {
        if (registerTask != null) {
            return;
        }

        // TODO: add course selection validation according to new business rules
        // For now, courses are not validated to allow flexibility.
        // For example, a student could enter with pre-existing credit or
        // test out of a course and not take the prerequisite. In a future version,
        // more complex course validation can be added.

        registerTask = new CourseRegisterTask(CWID, selectedCourse);
        registerTask.execute();
    }

    public class CourseRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private ArrayList<String> courses;
        private int cwid;

        CourseRegisterTask(int cwid, ArrayList<String> courses) {
            this.courses = courses;
            this.cwid = cwid;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Connection connection = connection();
                if (connection == null) {
                    return false;
                } else {
                    // Iterate through the list of selected courses
                    for (String course : courses) {
                        // Parse out just the CourseID first
                        course = course.substring(0, course.indexOf(" "));
                        String sql = "INSERT INTO Student_Course (CWID, CourseID) " +
                                "VALUES (" + cwid +
                                ", '" + course + "');";
                        connection.createStatement().executeUpdate(sql);
                    }

                    connection.close();
                    return true;
                }
            } catch (Exception e) {
                Log.e("Error ", e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            registerTask = null;
            Intent intent = new Intent(CourseListActivity.this, NextCourseActivity.class);
            intent.putExtra("UserEmail", USER_EMAIL);
            intent.putExtra("CWID", CWID);

            if (success) {
                Toast.makeText(getApplicationContext(), "Courses added successfully", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Error encountered while adding courses", Toast.LENGTH_LONG).show();
                finish();
            }

            startActivity(intent);
        }
    }

    /**
     * Asynchronous SELECT task to populate the ListView.
     */
    public class CourseListTask extends AsyncTask<Void, Void, Boolean> {

        private final int cwid;

        // Constructor
        CourseListTask(int cwid) {
            this.cwid = CWID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection connection = connection();
                if (connection == null) return false;
                else {
                    String sql =
                        "SELECT CourseID, CourseName " +
                        "FROM Course " +
                        "WHERE CourseID NOT IN (" +
                            "SELECT CourseID " +
                            "FROM Student_Course " +
                            "WHERE CWID = " + cwid + ");";
                    ResultSet resultSet = connection.createStatement().executeQuery(sql);
                    while (resultSet.next()) {
                        // CourseID + " " + CourseName
                        String courseRow = resultSet.getString(1) + " " + resultSet.getString(2);
                        fetchedCourse.add(courseRow);
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
                courseAdapter.notifyDataSetChanged();
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
