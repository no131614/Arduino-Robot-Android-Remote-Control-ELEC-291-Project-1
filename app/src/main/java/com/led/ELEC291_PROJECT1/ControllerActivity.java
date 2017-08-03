package com.led.ELEC291_PROJECT1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;

/*This class contains most of the functionality of the controller for the DF Robot*/
public class ControllerActivity extends ActionBarActivity {

    //Initialize all variables and other objects
    String address = null;
    Context context = this;
    private ProgressDialog progress;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;

    private boolean isBtConnected = false;
    private boolean Opmode = false;
    private boolean autoMode = false;
    private boolean hiddenPressed = false;

    ListView list;
    Button btnReturn, btnSetSpeed, btnSend, btnBack;
    ImageView Imgforward, Imgbackward, Imgleft, Imgright, ImgOpmode, ImgNormal, ImgStop,
            ImgAuto, ImgDialogSend, ImgHidden, ImgDance;
    SeekBar speed;
    TextView speed_count, switchStatus, modeStatus;
    Switch OnOffSwitch;
    EditText Text_send;




    //SPP UUID.
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    int curr_speed = 0;//Intialize the current speed

    //Sets everything upon activity initialization
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);


        switchStatus = (TextView) findViewById(R.id.switchStatus);


        OnOffSwitch = (Switch) findViewById(R.id.switchOnOff);
        OnOffSwitch.setChecked(false);
        OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {


            //The method that defines the ON OFF switch
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                //Check the switch status of the DF Robot (ON or OFF)
                if(isChecked){
                    //If it is on then turn ON the robot
                    switchStatus.setText("DF Robot is currently ON");
                    DFFOn();
                }else{
                    //Else turn on the robot
                    switchStatus.setText("DF Robot is currently OFF");
                    DFFOff();
                }

            }
        });

        //Checking the status of the ROBOT (ON or OFF)
        if(OnOffSwitch.isChecked()){
            //Display the current status
            switchStatus.setText("DF Robot is currently ON");
        }
        else {
            //Display the current status
            switchStatus.setText("DF Robot is currently OFF");
        }

        //Create an intent to call the ConnectBT class
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        new ConnectBT().execute(); //Call the class to connect


        //Initialize the functionality of Return button that disconnect the app
        //and return to the Device List Activity
        btnReturn = (Button)findViewById(R.id.button6);
        btnReturn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(v.getContext(), DeviceList.class);
                Disconnect();//Disconnect the app from the robot
                startActivity(i);//Open the DeviceList Page
            }
        });

        final MediaPlayer mp6 = MediaPlayer.create(this, R.raw.set_speed);
        //Initialize the functionality of set speed button
        btnSetSpeed = (Button)findViewById(R.id.btnSetSpeed);
        btnSetSpeed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try {
                    //Set and display the current speed upon button press
                    btSocket.getOutputStream().write(String.valueOf(curr_speed).getBytes());
                    speed_count.setText(String.valueOf(curr_speed));
                    mp6.start();
                }
                catch (IOException e){
                    msg("Error");
                }
            }
        });

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.forward);
        //Initialize the functionality of the move forward button
        Imgforward = (ImageView)findViewById(R.id.imageViewForward);
        Imgforward.setOnClickListener(new View.OnClickListener() {
            @Override
            //Tells the robot to move in the forward direction upon button press
            public void onClick(View v) {
                msg("Pressed Forward");
                mp.start();
                moveForward();//Command the robot to move forward
            }
        });

        final MediaPlayer mp2 = MediaPlayer.create(this, R.raw.left);
        //Initialize the functionality of the left button
        Imgleft = (ImageView)findViewById(R.id.imageViewLeft);
        Imgleft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            //Tells the robot to move in the move left direction upon button press
            public boolean onTouch(View v, MotionEvent event) {
                boolean set;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    moveLeft();
                    mp2.start();
                    set = true;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write("A".toString().getBytes());
                            mp2.start();
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }

                    while(event.getAction() == MotionEvent.ACTION_MOVE){

                    }
                    try
                    {
                        btSocket.getOutputStream().write("A".toString().getBytes());
                        mp2.start();
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }

                }
                return false;
            }
        });

        final MediaPlayer mp3 = MediaPlayer.create(this, R.raw.right);
        //Initialize the functionality of the move right button
        Imgright = (ImageView)findViewById(R.id.imageViewRight);
        Imgright.setOnClickListener(new View.OnClickListener() {
            @Override
            //Tells the robot to move in the right direction upon button press
            public void onClick(View v) {
                msg("Pressed Right");
                moveRight();//Command the robot to move to the right
                mp3.start();
            }
        });

        final MediaPlayer mp4 = MediaPlayer.create(this, R.raw.backward);
        //Initialize the functionality of the move back button
        Imgbackward = (ImageView)findViewById(R.id.imageViewBackward);
        Imgbackward.setOnClickListener(new View.OnClickListener() {
            @Override
            //Tells the robot to move in the backward direction upon button press
            public void onClick(View v) {
                msg("Pressed Backward");
                moveBackward();//Command the robot to move backward
                mp4.start();
            }
        });

        final MediaPlayer mp5 = MediaPlayer.create(this, R.raw.stop);
        //Initialize the functionality of the stop button
        ImgStop = (ImageView)findViewById(R.id.imageViewStop);
        ImgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg("Pressed Stop");
                Stop();//Set the speed of the robot to 0(Stop)
                mp5.start();
            }
        });


        //Initialize the functionality of the normal mode button
        ImgNormal = (ImageView)findViewById(R.id.imageViewNormal);
        ImgNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg("Pressed NormalMode");
                //Sets the robot to normal mode if it is on Op mode
                //or auto mode upon button press
                if(Opmode == true || autoMode == true) {
                    NormalMode();
                    modeStatus.setText("Mode: Normal");
                    Opmode = false;
                    autoMode = false;
                }
                else{
                    msg("Robot is already in NormalMode");
                }
            }
        });


        //Initialize the functionality of the dance button
        ImgDance = (ImageView) findViewById(R.id.ImgViewDance);
        ImgDance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hiddenPressed == true) {
                    ImgDance.setVisibility(View.VISIBLE);
                    Dance();//Commands the robot to dance
                }
            }
        });

        //Initialize the functionality of a hidden button
        ImgHidden = (ImageView)findViewById(R.id.ImgViewHidden);
        ImgHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenPressed = true;
                ImgDance.setVisibility(View.VISIBLE);
                //Sets the dance button to be visible upon button press
            }
        });


        //Initialize the functionality of the op mode button
        modeStatus = (TextView) findViewById(R.id.modeStatus);
        ImgOpmode = (ImageView)findViewById(R.id.imageView_HyperDrive);
        ImgOpmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg("Pressed Op Sensor");
                if (Opmode == false) {
                    new AlertDialog.Builder(context)//Creates an alert dialog upon button press
                            .setTitle("Turning On Op Sensor")
                            .setMessage("Turning On Op Sensor make the robot tries to follow a black line. Are you sure you want to continue ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Opmode();//Switch the robot to Op mode
                                    modeStatus.setText("Mode: Op Sensor");
                                    Opmode = true;
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
                else{
                    //Do nothing
                }
            }
        });


        //Initialize the functionality of the Auto mode button
        ImgAuto = (ImageView)findViewById(R.id.Auto_icon);
        ImgAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg("Pressed Auto");
                //Checks whether the robot is already on auto mode
                if (autoMode == false) {
                    new AlertDialog.Builder(context)//Creates an alert dialog upon button press
                            .setTitle("Autonomous Mode")
                            .setMessage("This option will make the robot runs automatically. Are you sure you want to continue ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Automatic();//Sets the robot to auto mode
                                    modeStatus.setText("Mode: Autonomous");
                                    autoMode = true;
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
                else{
                    msg("DFRobot is already in Autonomous Mode");
                }
            }
        });





        //Initialize the seek bar to set the speed of the robot
        speed = (SeekBar)findViewById(R.id.speed_control);
        speed_count = (TextView)findViewById(R.id.speed_count);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //Set the speed based on the seek bar
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true)
                {
                    curr_speed = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });

    }

//--------------------------------------------------------------------------------------------------
//This section contains all of the implementation of the commands

    private void moveForward()
    {
        if (btSocket!=null)
        {
            try
            {
                msg("Forward");
                btSocket.getOutputStream().write("w".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Connection Error");
                isBtConnected = false;
                msg("Trying to reestablished connection");
                try
                {
                    btSocket.close(); //close connection
                }
                catch (IOException ei)
                { msg("CANNOT DISMISS");}
                new ConnectBT().execute();
            }
        }

    }

    private void moveBackward()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("s".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Connection Error");
                isBtConnected = false;
                msg("Trying to reestablished connection");
                try
                {
                    btSocket.close(); //close connection
                }
                catch (IOException ei)
                { msg("CANNOT DISMISS");}
                new ConnectBT().execute();
            }
        }
    }

    private void moveLeft()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("a".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Connection Error");
                isBtConnected = false;
                msg("Trying to reestablished connection");
                try
                {
                    btSocket.close(); //close connection
                }
                catch (IOException ei)
                { msg("CANNOT DISMISS");}
                new ConnectBT().execute();
            }
        }
    }

    private void moveRight()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("d".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Connection Error");
                isBtConnected = false;
                msg("Trying to reestablished connection");
                try
                {
                    btSocket.close(); //close connection
                }
                catch (IOException ei)
                { msg("CANNOT DISMISS");}
                new ConnectBT().execute();
            }
        }
    }

    private void Stop()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("q".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Connection Error");
                isBtConnected = false;
                msg("Trying to reestablished connection");
                try
                {
                    btSocket.close(); //close connection
                }
                catch (IOException ei)
                { msg("CANNOT DISMISS");}
                new ConnectBT().execute();
            }
        }
    }

    private void Opmode()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("h".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Connection Error on Op Sensor");
                isBtConnected = false;
                msg("Trying to reestablished connection");
                try
                {
                    btSocket.close(); //close connection
                }
                catch (IOException ei)
                { msg("CANNOT DISMISS");}
                new ConnectBT().execute();
            }
        }
    }

    private void Automatic()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("@".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Connection Error on Autonomous");
                isBtConnected = false;
                msg("Trying to reestablished connection");
                try
                {
                    btSocket.close(); //close connection
                }
                catch (IOException ei)
                { msg("CANNOT DISMISS");}
                new ConnectBT().execute();
            }
        }
    }

    private void NormalMode()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("n".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void DFFOn()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("o".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Turning ON DF Robot");
            }
        }
    }

    private void DFFOff()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("x".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Turning Off DF Robot");
            }
        }
    }

    private void Dance()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("*".toString().getBytes());
                msg("!DO DIGITAL DANCING!");
            }
            catch (IOException e)
            {
                msg("Connection Error on Dance");
                isBtConnected = false;
                msg("Trying to reestablished connection");
                try
                {
                    btSocket.close(); //close connection
                }
                catch (IOException ei)
                { msg("CANNOT DISMISS");}
                new ConnectBT().execute();
            }
        }
    }


    public void decideMovement(float x_enter,float y_enter,float x_exit,float y_exit ){

        float diffY = y_exit - y_enter;
        float diffX = x_exit - x_enter;

        if (diffX > 0) {
            moveLeft();
        } else {
            moveRight();
        }


    }


//--------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //Method to display a Toast
    public void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    //The purpose of this class is to create and maintain the bluetooth connection
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ControllerActivity.this, "Connecting...", "Please wait!!!");  //Show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //While the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//Get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//Connects to the device's address and checks if it is available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//Create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//Start the connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//If the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            //Sends an error message if the connection is failed
            if (!ConnectSuccess)
            {
                msg("Connection Failed. Please try again.");
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    //Method to disconnect the bluetooth connection
    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }

        finish();
    }



}
