package com.sobag.androidcamtut;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobag.androidcamtut.util.BitmapUtility;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity
{
    // ------------------------------------------------------------------------
    // members
    // ------------------------------------------------------------------------

    private String imagePath;
    public static int CAPTURE_IMAGE_RESULT = 49; // why 47? just like this number...

    // UI components...
    private ImageView ivReference;
    private RelativeLayout rlReference;
    private LinearLayout llSlider;

    // ------------------------------------------------------------------------
    // default stuff
    // ------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // apply font...
        TextView tvTitle = (TextView)findViewById(R.id.tv_title);
        String desiredFont = getString(R.string.default_font);
        Typeface typeface = Typeface.createFromAsset(getAssets(), desiredFont);
        tvTitle.setTypeface(typeface);

        // init UI component accessors...
        ivReference = (ImageView)findViewById(R.id.iv_reference);
        rlReference = (RelativeLayout)findViewById(R.id.rl_reference_container);
        llSlider = (LinearLayout)findViewById(R.id.ll_slider);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAPTURE_IMAGE_RESULT && resultCode == RESULT_OK)
        {
            File image = null;
            try
            {
                image = new File(new URI(imagePath));
            }
            catch (URISyntaxException ex)
            {
                ex.printStackTrace();
            }

            if(image.exists())
            {
                Bitmap myBitmap = null;
                int boxWidth = ivReference.getWidth();
                int boxHeight = ivReference.getHeight();

                // decode bitmap using our super helper....
                try
                {
                    myBitmap = new BitmapUtility(getApplicationContext()).createScaledBitmapFromFile(Uri.parse(imagePath),
                            boxWidth, boxHeight);
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }

                RelativeLayout rlNew = new RelativeLayout(this);
                rlNew.setLayoutParams(rlReference.getLayoutParams());

                ImageView ivNew = new ImageView(this);
                ivNew.setLayoutParams(ivReference.getLayoutParams());

                ivNew.setImageBitmap(myBitmap);
                ivNew.getLayoutParams().width = ivReference.getWidth();
                ivNew.getLayoutParams().height = ivReference.getHeight();
                ivNew.setScaleType(ImageView.ScaleType.FIT_XY);

                RelativeLayout.LayoutParams reference_params = (RelativeLayout.LayoutParams) ivReference.getLayoutParams();
                reference_params.setMargins(10, 10, 10, 10);
                ivReference.setLayoutParams(reference_params);

                rlNew.addView(ivNew);
                llSlider.addView(rlNew, 0);

                ivReference = ivNew;
                rlReference= rlNew;
            }
        }
    }

    // ------------------------------------------------------------------------
    // public usage
    // ------------------------------------------------------------------------

    public void onTakePhoto(View view)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File image = null;
            try
            {
                image = createImageFile();
            }
            catch (IOException ex)
            {
                // Error occurred while creating the File..handle
            }
            // Continue only if the File was successfully created
            if (image != null)
            {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(image));
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_RESULT);
            }
        }
    }

    // ------------------------------------------------------------------------
    // private usage
    // ------------------------------------------------------------------------

    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File img = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imagePath = "file:" + img.getAbsolutePath();
        return img;
    }
}
