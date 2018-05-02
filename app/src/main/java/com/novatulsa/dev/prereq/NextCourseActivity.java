package com.novatulsa.dev.prereq;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class NextCourseActivity extends AppCompatActivity {

    // User variables
    private static String USER_EMAIL;
    private static int CWID;

    // Arrays and Lists
    private NextCourseListTask fetchTask = null;
    private ArrayAdapter<String> courseAdapter;
    public ArrayList<String> fetchedCourse;

    // UI References
    private ListView nextCourseListView;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_courses);

        // Setup user information from Intent
        USER_EMAIL = getIntent().getStringExtra("UserEmail");
        CWID = getIntent().getIntExtra("CWID", 0);

        // Initialize Arrays and Adapters
        fetchedCourse = new ArrayList<String>();
        courseAdapter = new ArrayAdapter<String>(this, R.layout.course_item, R.id.courseItem, fetchedCourse);

        // Initialize UI elements
        nextCourseListView = (ListView) findViewById(R.id.nextCourseList);
        nextCourseListView.setAdapter(courseAdapter);

        // OnClickListener that starts the CourseListActivity to return to adding classes
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAddCourse);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NextCourseActivity.this, CourseListActivity.class);
                intent.putExtra("UserEmail", USER_EMAIL);
                intent.putExtra("CWID", CWID);
                startActivity(intent);
            }
        });

        fetchTask = new NextCourseListTask(CWID);
        fetchTask.execute();
    }

    public class NextCourseListTask extends AsyncTask<Void, Void, Boolean> {

        private final int cwid;

        // Constructor
        NextCourseListTask(int cwid) {
            this.cwid = CWID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection connection = connection();
                if (connection == null) return false;
                else {
                    // Clear out the array for a new result set
                    fetchedCourse.clear();

                    String sql = "EXEC dbo.uspGetNextCourseList " + cwid + ";";
                    ResultSet resultSet = connection.createStatement().executeQuery(sql);

                    // Read through resultSet
                    while (resultSet.next()) {
                        // TODO: Update this or move it to a Course class later
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
