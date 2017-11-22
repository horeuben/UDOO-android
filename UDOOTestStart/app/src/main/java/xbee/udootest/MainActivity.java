package xbee.udootest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import me.palazzetti.adktoolkit.AdkManager;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;


public class MainActivity extends Activity {

//	private static final String TAG = "UDOO_AndroidADKFULL";	 

    private AdkManager mAdkManager;

    private ToggleButton buttonLED;
    private TextView distance;
    private TextView pulse;
    private TextView position;
    private Button checkoffButton;
    private TextView accuracyMeasure;
    private Button checkoff1part2button;
    private TextView confusionMatrix;


    private AdkReadTask mAdkReadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdkManager = new AdkManager((UsbManager) getSystemService(Context.USB_SERVICE));


//		register a BroadcastReceiver to catch UsbManager.ACTION_USB_ACCESSORY_DETACHED action
        registerReceiver(mAdkManager.getUsbReceiver(), mAdkManager.getDetachedFilter());

        buttonLED = (ToggleButton) findViewById(R.id.toggleButtonLED);
        distance  = (TextView) findViewById(R.id.textView_distance);
        pulse  = (TextView) findViewById(R.id.textView_pulse);
        position  = (TextView) findViewById(R.id.textView_position);
        checkoffButton = (Button) findViewById(R.id.checkoff1Button);
        accuracyMeasure = (TextView) findViewById(R.id.accuracy);
        confusionMatrix= (TextView) findViewById(R.id.confusionMatrix);
        checkoff1part2button = (Button) findViewById(R.id.checkoff1_2Button);

        checkoff1part2button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, Checkoff122.class));
            }
        });
    }





    public void Checkoff1 (View v) throws Exception {
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File f = new File(root, "iris_train.arff");
        BufferedReader inputReader;
        inputReader = readFile(f);
        Instances data = new Instances(inputReader);
        data.setClassIndex(data.numAttributes()-1);

        Classifier ibk = new IBk();
        ibk.buildClassifier(data);
        f = new File(root, "iris_test.arff");
        inputReader = readFile(f);

        Instances test=null;
        try {
            test = new Instances(inputReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        test.setClassIndex(test.numAttributes()-1);

        int[][] confMat = new int[3][3];
        double trueCase = 0;
        double allCase = 0;


        for (int i=0; i<test.numInstances();i++){
            double pred = -1;
            try {
                pred = ibk.classifyInstance(test.instance(i));
            } catch (Exception e) {
                e.printStackTrace();
            }

            double act = test.instance(i).classValue();

            confMat[(int) pred][(int) act]++;

            if (pred == act) {
                trueCase++;
            }
            allCase++;
        }


        double accuracyTotal = trueCase / allCase;

        accuracyMeasure.setText("accuracy is"+ Double.toString(accuracyTotal));
        String confusionMatrixWIP="";

        confusionMatrixWIP+= Integer.toString(confMat[0][0])+"   "+ Integer.toString(confMat[0][1])+"   "+ Integer.toString(confMat[0][2])+"   "+"\n"
                + Integer.toString(confMat[1][0])+"   "+ Integer.toString(confMat[1][1])+"   "+ Integer.toString(confMat[1][2])+"   "+"\n"
                + Integer.toString(confMat[2][0])+"   "+ Integer.toString(confMat[2][1])+"   "+ Integer.toString(confMat[2][2])+"   "+"\n";

        confusionMatrix.setText(confusionMatrixWIP);

    }

    private BufferedReader readFile(File f) throws FileNotFoundException {
        BufferedReader result=new BufferedReader(new FileReader(f));
        return result;
    }


    public void onResume() {
        super.onResume();
        mAdkManager.open();

        mAdkReadTask = new AdkReadTask();
        mAdkReadTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdkManager.close();

        mAdkReadTask.pause();
        mAdkReadTask = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAdkManager.getUsbReceiver());
    }

    // ToggleButton method - send message to SAM3X
    public void blinkLED(View v){
        if (buttonLED.isChecked()) {
            // writeSerial() allows you to write a single char or a String object.
            mAdkManager.writeSerial("1");
        } else {
            mAdkManager.writeSerial("0");
        }
    }


    /*
     * We put the readSerial() method in an AsyncTask to run the
     * continuous read task out of the UI main thread
     */


    private class AdkReadTask extends AsyncTask<Void, String, Void> {

        private boolean running = true;

        public void pause(){
            running = false;
        }

        protected Void doInBackground(Void... params) {
//	    	Log.i("ADK demo bi", "start adkreadtask");
            while(running) {
                publishProgress(mAdkManager.readSerial()) ;
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {

            float pulseRate= (int)progress[0].charAt(0);
            float oxygenLvl= (int)progress[0].charAt(1);
            float pos= (int)progress[0].charAt(2);
            int max = 255;
            if (pulseRate>max) pulseRate=max;
            if (oxygenLvl>max) oxygenLvl=max;
            if (pos>max) pos=max;

//            DecimalFormat df = new DecimalFormat("#.#");
            distance.setText(pulseRate + " (bpm)");
            pulse.setText(oxygenLvl + " (pct)");
            position.setText(pos + "");
        }
    }




}
