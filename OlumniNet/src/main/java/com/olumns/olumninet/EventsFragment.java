package com.olumns.olumninet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

import com.teamolumn.olumninet.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by zach on 11/2/13.
 */
public class EventsFragment extends Fragment {
    //Activity
    MainActivity activity;
    DBHandler db;

    String curGroup = "Events";

    //Views
    ThreadListAdapter threadListAdapter;
    ListView threadList;
    ArrayList<Post> threads = new ArrayList<Post>();


    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    //On Fragment Creation
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.threads_fragment,null);
        setupUI(v);
        setHasOptionsMenu(true);

        db = new DBHandler(activity);
        db.open();

        threads = db.getThreadsByGroup(curGroup);
        Log.i("Threads",threads.toString());
        // Set up the ArrayAdapter for the Thread List
        threadListAdapter = new ThreadListAdapter(activity, threads);
        threadList = (ListView) v.findViewById(R.id.thread_list);
        threadList.setAdapter(threadListAdapter);

        threadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Add Connection to invisible Tab
                refreshListView();
                activity.curPost = EventsFragment.this.threads.get(i);
                PostFragment newFragment = new PostFragment();
                FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();

                transaction.replace(R.id.fragmentContainer, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        return v;
    }
    //Refresh Group List View
    public void refreshListView(){
        EventsFragment.this.threads = EventsFragment.this.db.getThreadsByGroup("Event");
        Log.i("Threads",threads.toString());
        this.threadListAdapter = new ThreadListAdapter(activity, threads);
        this.threadList.setAdapter(this.threadListAdapter);
        this.threadListAdapter.notifyDataSetChanged();
    }

    //Add Thread
    public void addThread() {
        //Inflate Dialog View
        final View view = activity.getLayoutInflater().inflate(R.layout.add_events,null);

        final EditText event = (EditText) view.findViewById(R.id.inputName);
        final EditText location = (EditText) view.findViewById(R.id.inputLocation);
        final EditText description = (EditText) view.findViewById(R.id.inputDescription);
        final EditText date = (EditText) view.findViewById(R.id.inputDate);
        final EditText time = (EditText) view.findViewById(R.id.inputStart);

        date.setFocusable(false);
        date.setClickable(true);

        time.setFocusable(false);
        time.setClickable(true);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar myCalendar = Calendar.getInstance();
                new DatePickerDialog(activity,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                                myCalendar.set(Calendar.YEAR, i);
                                myCalendar.set(Calendar.MONTH, i2);
                                myCalendar.set(Calendar.DAY_OF_MONTH, i3);
                                String myFormat = "MM/dd/yy";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                                date.setText(sdf.format(myCalendar.getTime()));
                            }
                        },
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar start = Calendar.getInstance();
                int hour = start.get(Calendar.HOUR_OF_DAY);
                int minute = start.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(activity,new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute){
                        String timea, AMPM;
                        if (selectedHour >= 12){
                            if (selectedHour%12 == 0) timea = "12";
                            else timea = String.valueOf(selectedHour%12);
                            AMPM = "PM";}
                        else{
                            if (selectedHour%12 == 0) timea = "12";
                            else timea = String.valueOf(selectedHour%12);
                            AMPM = "AM";}
                        timea = timea + ":" + String.valueOf(selectedMinute) + AMPM;
                        time.setText(timea);
                    }},hour,minute,false);
                timePicker.setTitle("Select Start Time");
                timePicker.show();
            }
        });
        //Create Dialog BoxcurGroup
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String inputEvent = event.getText().toString();
                        String inputLocation = location.getText().toString();
                        String inputDescription = description.getText().toString();
                        String inputDate = date.getText().toString();
                        String inputTime = time.getText().toString();

                        Post newPost = new Post(activity.fullName, curGroup, inputEvent, inputLocation + ": " + inputDescription, String.valueOf(System.currentTimeMillis()), curGroup, "Unresolved", activity.curGroup + "&");
                        newPost.setEventDate(inputDate);
                        newPost.setEventTime(inputTime);

                        //Add post to server
                        addThreadToServer(newPost);
                        threads.add(newPost);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        })
                .show();
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(activity);
                    return false;
                }
            });
        }
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


        //Create Options Menu
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.thread_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Add a Post to the Server
    public void addThreadToServer(final Post post){
        new AsyncTask<Void, String, String>() {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;

            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }

            protected String doInBackground(Void... voids) {
                try {
                    String website = "http://olumni-server.herokuapp.com/" + "createPost";
                    HttpPost createSessions = new HttpPost(website);

                    JSONObject json = new JSONObject();
                    json.put("group",post.groups);
                    json.put("parentItem",post.parent);
                    json.put("username",post.poster);
                    json.put("date",post.date);
                    json.put("subject",post.subject);
                    /*json.put("lastDate",post.lastDate);*/
                    json.put("message",post.message);
                    json.put("viewers", "public");
                    json.put("reply", "true");

                    Log.i("group",post.groups);
                    Log.i("parentItem",post.parent);
                    Log.i("username",post.poster);
                    Log.i("subject",post.subject);
                    Log.i("date",post.date);
                    Log.i("message",post.message);
                    Log.i("viewers", "public");
                    Log.i("reply", "false");


                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    createSessions.setEntity(se);

                    response = client.execute(createSessions);
                }
                catch (Exception e) {e.printStackTrace(); Log.e("Server", "Cannot Establish Connection");}
                String result = "";
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"),8);
                    StringBuilder sb = new StringBuilder();

                    String line;
                    String nl = System.getProperty("line.separator");
                    while ((line = reader.readLine())!= null){
                        sb.append(line + nl);
                    }
                    result = sb.toString();
                    Log.i("RESULT PRINT FROM THING", result);
                }catch (Exception e){e.printStackTrace();}
                //READ THE RESULT INTO A JSON OBJECT
                try {
                    JSONObject res = new JSONObject(result);
                    if (res.getString("error").equals("false"))
                        return res.getString("postid");
                    else
                        return "ServerError";
                } catch (JSONException e){e.printStackTrace();}
                return "JSONServerError";
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                EventsFragment.this.db.open();
                post.setId(s);
                EventsFragment.this.db.addPost(post);
                refreshListView();
            }
        }.execute();
    }

    //Setup Options Menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action1:
                addThread();
                break;
            default:
                break;
        }
        return true;
    }

}
