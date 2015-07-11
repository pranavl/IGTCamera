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
import android.widget.FrameLayout;
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
     * Button used to capture frame.
     */
    private Button captureButton;

    /**
     * TextView to display status messages.
     */
    private TextView txtStat;

    /**
     * Camera object.
     */
    private Camera mCamera;

// VARIABLES ===================================================================
    /**
     * Server port.
     */
    private final int SERVER_PORT = 8099;

    /**
     * Byte array to be transferred.
     */
    byte[] d;

// OBJECTS =====================================================================
    /**
     * CameraPreview object.
     */
    private CameraPreview mPreview;

    /**
     * Server socket.
     */
    private ServerSocket servSock;

    /**
     * Picture Callback.
     */
    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            d = data;
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
            try {
                Socket clntSock = servSock.accept();
                BufferedReader inFromClient = new BufferedReader(
                        new InputStreamReader(clntSock.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(
                        clntSock.getOutputStream());
                inFromClient.readLine();
                //mCamera.takePicture(null, null, mPicture);
                outToClient.write(d);
                outToClient.flush();
                outToClient.close();
            } catch (IOException ex) {
                txtStat.setText("Server thread expection");
                mCamera.release();
                Logger.getLogger(
                        MainActivity.class.getName()).log(
                                Level.SEVERE, null, ex);
            }
        }
    }

// METHODS =====================================================================
    /**
     * Called when activity is first created.
     *
     * @param savedInstanceState .
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

        // Create Preview view and set it as the content of the activity.
        this.mPreview = new CameraPreview(this, this.mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cam_preview);
        preview.addView(this.mPreview);

        // Event listener to the Capture button
        this.captureButton = (Button) findViewById(R.id.btn_capture);
        this.captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                        serverThread = new Thread(new ServerThread());
                        serverThread.start();
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
    }

    /**
     * Check if this device has a camera.
     *
     * @param context .
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
        }
        return c;
    }

}

/** OTHER ======================================================================

    private void runServer() {
        try {

            Socket clntSock = this.servSock.accept();
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(clntSock.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(
                    clntSock.getOutputStream());
            inFromClient.readLine();
            this.con = true;
            //this.mCamera.takePicture(null, null, this.mPicture);
            outToClient.writeBytes("TEXT");
            outToClient.flush();
            outToClient.close();
        } catch (IOException ex) {
            this.txtStat.setText("Exception 1");
            this.mCamera.release();
            Logger.getLogger(
                    MainActivity.class.getName()).log(
                            Level.SEVERE, null, ex);
        }

    }
*/
