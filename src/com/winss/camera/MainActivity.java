package com.winss.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main activity.
 *
 * @author Pranav
 */
public class MainActivity extends Activity {

// UI ELEMENTS =================================================================
    /**
     * Switch to enable/disable server
     */
    private Switch servSwitch;

    /**
     * TextView to display status messages.
     */
    private TextView txtStat;

// VARIABLES ===================================================================
    /**
     * Server port.
     */
    private final int SERVER_PORT = 8099;

    /**
     * Flag to run server.
     */
    private boolean serverFlag;

// OBJECTS =====================================================================
    /**
     * Camera object.
     */
    Camera mCamera;

    /**
     * CameraPreview object.
     */
    CameraPreview mPreview;

    /**
     * Server socket.
     */
    private ServerSocket servSock;

    /**
     * Byte array of picture.
     */
    byte[] d;

    /**
     * Picture Callback.
     */
    private final PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //d = data;
        }
    };

    /**
     * Thread for server.
     */
    Thread serverThread = null;

// THREADS =====================================================================
    /**
     * Server thread.
     */
    class ServerThread implements Runnable {

        @Override
        public void run() {
            while (serverFlag) {
                try {
                    Socket clntSock = servSock.accept();
                    BufferedReader inFromClient = new BufferedReader(
                            new InputStreamReader(clntSock.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(
                            clntSock.getOutputStream());
                    inFromClient.readLine();
                    mCamera.takePicture(null, null, mPicture);
                    outToClient.writeBytes("TEXT");
                    outToClient.flush();
                    outToClient.close();
                } catch (IOException ex) {
                    txtStat.setText("Thread expection");
                    mCamera.release();
                    Logger.getLogger(
                            MainActivity.class.getName()).log(
                                    Level.SEVERE, null, ex);
                }
            }
        }

    }

// METHODS =====================================================================
    /**
     * Called when activity is first created.
     *
     * @param savedInstanceState ...
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Connect UI elements
        this.txtStat = (TextView) findViewById(R.id.txt_status);
        this.txtStat.setText("Server port: " + this.SERVER_PORT);

        // Create an instance of Camera
        this.mCamera = getCameraInstance();
        if (mCamera == null) {
            this.txtStat.setText("Camera is null");
        }
        // Create Preview view and set it as the content of the activity.
        this.mPreview = new CameraPreview(this, this.mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cam_preview);
        preview.addView(this.mPreview);

        // Set up server switch
        this.servSwitch = (Switch) findViewById(R.id.server_switch);
        this.servSwitch.setChecked(false);
        // Event listener for server switch
        this.servSwitch.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        if (isChecked) {
                            serverFlag = true;
                            // Start server thread
                            serverThread = new Thread(new ServerThread());
                            serverThread.start();
                        } else {
                            serverFlag = false;
                        }
                    }
                }
        );

        // Set up server socket
        try {
            this.servSock = new ServerSocket(this.SERVER_PORT);
        } catch (IOException ex) {
            this.mCamera.release();
            Logger.getLogger(
                    MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * When app is paused, release the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.mCamera.release();
    }

    /**
     * When app is stopped, release the camera.
     */
    @Override
    protected void onStop() {
        super.onStop();
        this.mCamera.release();
    }

    /**
     * When app resumes, start listening again.
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.mCamera = getCameraInstance();
    }

    /**
     * Check if this device has a camera.
     *
     * @param context ...
     * @return true if device has camera, false otherwise
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * Get an instance of the Camera object.
     *
     * @return Camera object, null if unavailable
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // Get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Logger.getLogger(
                    MainActivity.class.getName()).log(Level.SEVERE, null, e);
        }
        return c;
    }

}
