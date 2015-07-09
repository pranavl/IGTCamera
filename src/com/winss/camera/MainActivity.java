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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

    /**
     * Camera object.
     */
    private Camera mCamera;

    /**
     * CameraPreview object.
     */
    private CameraPreview mPreview;

    /**
     * Connected?
     */
    private boolean con;

    /**
     * Server port.
     */
    private final int SERVER_PORT = 8099;

    /**
     * Server socket.
     */
    private ServerSocket servSock;

    /**
     * Button used to capture frame.
     */
    private Button captureButton;

    /**
     * TextView to display status messages.
     */
    private TextView txtStat;

    /**
     * Picture Callback
     */
    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
//            if (con == true) {
//                try {
//                    trace += "a";
//                    txtStat.setText(trace);
//                    //outToClient.write(data);
//                    txtStat.setText(trace);
//                    trace += "b";
//                } catch (IOException ex) {
//                    txtStat.setText("Exception 2");
//                    mCamera.release();
//                    Logger.getLogger(MainActivity.class.getName()).log(
//                            Level.SEVERE, null, ex);
//                }
//            }
        }
    };

    /**
     * Called when activity is first created.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Create an instance of Camera
        this.mCamera = getCameraInstance();

        this.txtStat = (TextView) findViewById(R.id.txt_status);

        // Create Preview view and set it as the content of the activity.
        this.mPreview = new CameraPreview(this, this.mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cam_preview);
        preview.addView(this.mPreview);

        // Event listener to the Capture button
        captureButton = (Button) findViewById(R.id.btn_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        //mCamera.takePicture(null, null, mPicture);
                        runServer();
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

    private void runServer() {
        try {

            Socket clntSock = this.servSock.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clntSock.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(clntSock.getOutputStream());
            String clientSentence = inFromClient.readLine();
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

    /**
     * When app is paused, release the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.mCamera.release();
//        this.ds.close();
    }

    /**
     * When app resumes, start listening again.
     */
    @Override
    protected void onResume() {
        super.onResume();
//        this.ds.connect(myIp, port);
    }

    /**
     * Check if this device has a camera.
     *
     * @param context
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
