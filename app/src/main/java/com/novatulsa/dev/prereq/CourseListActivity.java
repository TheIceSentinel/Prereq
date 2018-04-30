package com.novatulsa.dev.prereq;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseListActivity extends AppCompatActivity {

    private GetCourseTask fetchTask = null;

    // UI References
    private ListView classListView = (ListView) findViewById(R.id.completeCourseList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
    }

    /**
     * Asynchronous SELECT task to populate the ListView.
     */
    public class GetCourseTask extends AsyncTask<Void, Void, Boolean> {

        private final String email;
        // private final String degree;

        // Constructor
        GetCourseTask(String email, String password) {
            this.email = email;
            // this.degree = degree;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection connection = connection();
                if(connection == null) return false;
                else {
                    String sql =
                            "SELECT CourseID, CourseName " +
                            "FROM Course " +
                            "WHERE CourseID NOT IN (" +
                                "SELECT CourseID " +
                                "FROM Student_Course " +
                                "WHERE CWID = (" +
                                    "SELECT CWID " +
                                    "FROM Student " +
                                    "WHERE Email = '" + email + "'));";
                    ResultSet resultSet = connection.createStatement().executeQuery(sql);

                    // read through resultSet
                    if(resultSet.next()) {
                        //
                        connection.close();
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                return false;
            }

            // Default return of false
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            // Once done, null task and hide progress bar
            fetchTask = null;

            if (success) {
                finish();
            } else {
                // WHEN COURSE LIST CANNOT BE RETRIEVED
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
