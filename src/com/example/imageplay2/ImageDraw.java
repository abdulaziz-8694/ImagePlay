package com.example.imageplay2;

import java.net.Proxy;

import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import com.example.imageplay2.CropView;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ImageDraw extends Activity {
	Button captureButton,loadButton,resetButton;
	CropView imageView;
	static int effNum=1;
	private static String IPAdress = "172.16.8.70";
	private static final String NAMESPACE = "http://imageProcess.org/";
    private static String URL="http://"+IPAdress+":4848/ImageProcess/imgProcessService?wsdl";
    private static final String METHOD_NAME = "getImage";
    private static final String SOAP_ACTION =  "http://imageProcess.org/getImage";
    byte[] ex=null;
    private class MyTask extends AsyncTask< Integer, Void, byte[]>{
        ProgressDialog progress = new ProgressDialog(ImageDraw.this);
        @Override
        protected byte[] doInBackground(Integer... params) {
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                request.addProperty("arg0",ex);
                request.addProperty("arg1", params[0]);
                SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.dotNet = false;
        soapEnvelope.setOutputSoapObject(request);
        new MarshalBase64().register(soapEnvelope);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(Proxy.NO_PROXY, URL, 20000);
                System.out.println(URL);
    try {
        androidHttpTransport.call(SOAP_ACTION,soapEnvelope);
        System.out.println("transported");
        SoapPrimitive response= (SoapPrimitive) soapEnvelope.getResponse();
        String str = response.toString();
        byte arr[]=Base64.decode(str);
        return arr;
   
    }catch (Exception e) {
        showMessage("Check IP settings");
        showIPdialog();
        e.printStackTrace();
        return null;
    }
        }
       
        @Override
        protected void onPostExecute(byte[] array){
                if (array!=null)
                {      
                        CropView croppedImg = (CropView)findViewById(R.id.image);
                        Bitmap bmp = BitmapFactory.decodeByteArray(array, 0, array.length);
                        croppedImg.setImageBitmap(bmp);
                }
                this.progress.dismiss();
        }
       
        @Override
        protected void onPreExecute(){
                super.onPreExecute();
                this.progress.setMessage("Please Wait...");
                this.progress.show();
        }
}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_draw);
		showIPdialog();
		imageView=(CropView)findViewById(R.id.image);
		//destination = new File(Environment.getExternalStorageDirectory(),"image.jpg");
		captureButton=(Button)findViewById(R.id.capture);
		loadButton=(Button)findViewById(R.id.load);
		Button confirmButton=(Button)findViewById(R.id.process);
		resetButton=(Button)findViewById(R.id.reset);
		captureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent,2);
				
			}
		});
		loadButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK,
				           android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, 1);
				
			}
		});
		confirmButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(null!=imageView.getDrawable()){
				ex=((CropView)findViewById(R.id.image)).getCroppedImage();
				if(ex!=null)
					if(effNum>0){
						System.out.println("Task created");
					new MyTask().execute(effNum);
					}
					else{
							Bitmap bitmap= BitmapFactory.decodeByteArray(ex, 0, ex.length);
							imageView.setImageBitmap(bitmap);
					}
				}
				
			}
		});
		resetButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				imageView.resetPoints();
				imageView.invalidate();
			}
		});
		
	}
    private void showIPdialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("IP Settings");
		final EditText inputIP = new EditText(this);
		inputIP.setText(IPAdress);
		LinearLayout temp = new LinearLayout(this);
		temp.addView(inputIP);
		builder.setView(temp);
		
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		                @Override
		                public void onClick(DialogInterface dialog, int which) {
		                        IPAdress = inputIP.getText().toString();
		                        URL = "http://"+IPAdress+":4848/ImageProcess/imgProcessService?wsdl";
		                        showMessage("IP settings saved");
		                }
		        })
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
		        // User cancelled the dialog
		    }
		});
		AlertDialog dialog = builder.create();
		
		dialog.show();
		}
    private void showMessage(CharSequence text){
        Context context = getApplicationContext();
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
		        if(requestCode == 2) {
		        	imageView.resetPoints();
		        	Bundle extras = data.getExtras();
		            Bitmap image = (Bitmap) extras.get("data");
		            imageView.setImageBitmap(image);
		            }
		        else if(requestCode==1){
		        	imageView.resetPoints();
		        	Uri imageData=data.getData(); 
		        	//imageView.setImageURI(imageData);
		        	String[] filePathColumn = {MediaStore.Images.Media.DATA};
		            Cursor cursor = getContentResolver().query(imageData, filePathColumn, null, null, null); 
		            cursor.moveToFirst();
		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		            String filePath = cursor.getString(columnIndex);
		            cursor.close();
		            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
		            imageView.setImageBitmap(bitmap);
		        }
	
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_draw, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem Item){
		Button process=(Button)findViewById(R.id.process);
		switch(Item.getItemId()){
		case R.id.action_settings:
			showIPdialog();
			return true;
		case R.id.none:
			effNum=0;
			process.setText("Process");
		case R.id.blur:
			effNum=1;
			process.setText("Blur");
			return true;
		case R.id.greyscale:
			effNum=2;
			process.setText("grey");
			return true;
		case R.id.edge:
			effNum=3;
			process.setText("EdgeDetect");
			return true;
		case R.id.negative:
			effNum=4;
			process.setText("Negative");
			return true;
		case R.id.sepia:
			effNum=5;
			process.setText("Sepia");
			return true;
		case R.id.noise:
			effNum=5;
			process.setText("Noise");
		default:
			
		}
		return true;
	}

}