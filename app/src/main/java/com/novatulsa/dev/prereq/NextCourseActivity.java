package com.novatulsa.dev.prereq;

import android.content.Intent;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class NextCourseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_courses);

        // Setup user information from Intent
        final String USER_EMAIL = getIntent().getStringExtra("UserEmail");

        // OnClickListener that starts the CourseListActivity to return to adding classes
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddCourse);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NextCourseActivity.this, CourseListActivity.class);
                intent.putExtra("UserEmail", USER_EMAIL);
                startActivity(intent);
            }
        });
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
