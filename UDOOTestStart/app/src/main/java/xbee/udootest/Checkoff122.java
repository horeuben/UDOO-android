package xbee.udootest;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import wlsvm.WLSVM;

public class Checkoff122 extends AppCompatActivity {
    private Button TrainButton;
    private Button ClassifyButton;
    private EditText SepalLength,PetalLength,PetalWidth,SepalWidth;
    File svmModel;
    private TextView confusionMatrix;
    private TextView accuracyMeasure;
    WLSVM test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkoff122);
        ClassifyButton=(Button) findViewById(R.id.button_classify);
        TrainButton = (Button) findViewById(R.id.button_train);
        SepalLength = (EditText) findViewById(R.id.editText_sl);
        SepalLength.setText("4.7");
        SepalWidth= (EditText) findViewById(R.id.editText_sw);
        SepalWidth.setText("3.2");
        PetalLength = (EditText) findViewById(R.id.editText_pl);
        PetalLength.setText("1.6");
        PetalWidth = (EditText) findViewById(R.id.editTextpw);
        PetalWidth.setText("0.2");
        accuracyMeasure = (TextView) findViewById(R.id.acc1);
        confusionMatrix= (TextView) findViewById(R.id.conmat1);
        TrainButton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v){
                WLSVM svmCls = new WLSVM();
                File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File f = new File(root,"iris_train.arff");
                BufferedReader inputReader= null;
                try {
                    inputReader = new BufferedReader(new FileReader(f));
                    Instances data = new Instances(inputReader);
                    data.setClassIndex(data.numAttributes()-1);
                    svmCls.buildClassifier(data);
                    svmModel = new File(root,"svmModel.model");

                   // test = svmCls;
                    weka.core.SerializationHelper.write(svmModel.getAbsolutePath(), svmCls);
                    Toast.makeText(getApplicationContext(),"Finished training",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        ClassifyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    WLSVM svmCls = (WLSVM) weka.core.SerializationHelper.read(svmModel.getAbsolutePath());
                    Attribute Attribute1 = new Attribute("sepallength");
                    Attribute Attribute2 = new Attribute("sepalwidth");
                    Attribute Attribute3 = new Attribute("petallength");
                    Attribute Attribute4 = new Attribute("petalwidth");
                    // Declare the class attribute along with its values (nominal)
                    FastVector fvClassVal = new FastVector(3);
                    fvClassVal.addElement("Iris-setosa");
                    fvClassVal.addElement("Iris-versicolor");
                    fvClassVal.addElement("Iris-virginica");
                    Attribute ClassAttribute = new Attribute("class", fvClassVal);
                    // Declare the feature vector template
                    FastVector fvWekaAttributes = new FastVector(5);
                    fvWekaAttributes.addElement(Attribute1);
                    fvWekaAttributes.addElement(Attribute2);
                    fvWekaAttributes.addElement(Attribute3);
                    fvWekaAttributes.addElement(Attribute4);
                    fvWekaAttributes.addElement(ClassAttribute);

                    Instances testingSet = new Instances("TestingInstance", fvWekaAttributes, 1);
                    testingSet.setClassIndex(testingSet.numAttributes() - 1);
                    // Create and fill an instance, and add it to the testingSet
                    Instance iExample = new Instance(testingSet.numAttributes());
                    iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), Double.parseDouble(SepalLength.getText().toString()));
                    iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), Double.parseDouble(SepalWidth.getText().toString()));
                    iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), Double.parseDouble(PetalLength.getText().toString()));
                    iExample.setValue((Attribute)fvWekaAttributes.elementAt(3), Double.parseDouble(PetalWidth.getText().toString()));
                    iExample.setValue((Attribute)fvWekaAttributes.elementAt(4), "Iris-setosa"); // dummy
                    // add the instance
                    testingSet.add(iExample);

                    testingSet.setClassIndex(testingSet.numAttributes()-1);
                    int[][] confMat = new int[3][3];
                    double trueCase = 0;
                    double allCase = 0;

                    for (int i=0; i<testingSet.numInstances();i++){
                        double pred = -1;
                        try {
                            pred = svmCls.classifyInstance(testingSet.instance(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        double act = testingSet.instance(i).classValue();

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        }
}
