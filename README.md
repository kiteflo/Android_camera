Android camera tutorial (10 minutes)
==============

<p align="center">
    <img src="images/camera-icon_512.png">
</p>

"I simply wanna take a picture using camera intent and process the image in my app (display, store, post etc.)" - if that's what you're heading for this tutorial might help you. Why did I create this tutorial as  googling for "android camera intent" produces thousnds of result? Because using 99% of the Google result will make you ending up like this...:

1) Hurra - got a snippet firing up the camera

2) WTF - why is the image received that small...?

3) Ok...need to do some more complex magic in order to process the intent result...

4) SHIT F#$§/#ing tutorials - how can I access the image directory using some weired Android path magig...?

5) uh no - all camera images are landscape even these have been taken in portrait mode!

...

12) FUCK - I WILL CHANGE PLATFORMS AND MIGRATE TO WINDOWS PHONE

...

19) Try to get the adress of the guy who posted a code snippet you used in step 13 in order to smack his bottom as his snippet is crap!

...

24) it's working!

25) watch the clock - ok this one took me more than 48 hours...

**Ok this can be in a much easier way I thought, that's the reason I created this tutorial so simply follow the steps below, use the provided classes and you will end up in a nice intent based camera solution within couple of minutes...**

# Get started

As I hate tutorials with unstyled results (styled ones somehow result in increased motivation...) I have created a very little bolierplate construct providing a camera button and a HorizontalScrollView which will display the images taken via camera intent. So if you're interested in the camera magic only simply ignore the styling stuff, in case you wanna create a styled camera app the whole bolierplate might be a good starting point. We will not cover the aspects of styling within this tutorial.

We create a simple scenario: 
1. pressing the green camera button will launch the camera activity
2. image is taken and result is passed back to your activity
3. within your activity we will access the result and display the results within the scroll view

(whether you are displaying the results in a scroll view or whether you are processing the results in order to display these somewhere else does not matter...it's about the magic of accessing the results!)

# prepare & launch camera intent

Launching the camera and processing results is basically pretty simple, it's just firing an intent and processing results via *onActivityResult* method. But the problem with this approach: you will receive a preview of the image only! So scaling up this preview results in pixled results and the whole apporach is simply not usable except for the sake of saying "camera integration in Android is so simple...".

So the approach enabling you to really work with the results is little more complex, dont be afraid, it's just a KnowHow, not a big deal at all. Core of the approach: you need to create a File on your own (which is empty after creation) and pass this file along to your camera intent. Then the camera intent will store the image to your empty file and you can process the results by simply accessing this file. Simply like this (**onTakeImage** is triggered when the user hits the image button...):

```java
// ------------------------------------------------------------------------
// members
// ------------------------------------------------------------------------

private String imagePath;
public static int CAPTURE_IMAGE_RESULT = 49; // why 49? just like this number...

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
```

Let's simply sumn up what's happening here:

1. An intent is prepared (type *ACTION_IMAGE_CAPTURE*)
2. An empty file with a datestamp as name is created within the *createImageFile()* method. The path to this file is stored in a local member variable!
3. The intent gets extended by putting in an extra field *.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image))*
4. The intent is fired

That's all you need to do! By now we know where the image is stored (we have this *imagePath*-member) so when the result is returned we simply can access the image file! But wait, there's another little thing you should be aware of when processing the result...

**IMPORTANT:** make sure you add permissions to your AndroidManifest.xml in order to read and write external storag - this can be achieved by simply adding the snippet below within your <application> tags..:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

# access image result

Ok using the snippet above will fire up the camera and enable you to take a photo - in order to access the results we have to implement *onActivityResult*. Within this method we will access the file and finally hae a clean bitmap in access which the can be added to the UI, processed, shared, sent etc....

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data)
{
    if (requestCode == CAPTURE_IMAGE_RESULT && resultCode == RESULT_OK)
    {
        File image = null;
        try
        {
            image = new File(new URI(mCurrentPhotoPath));
        }
        catch (URISyntaxException ex)
        {
            ex.printStackTrace();
        }

        if(image != null && image.exists())
        {
            Bitmap myBitmap = null;
            int boxWidth = iv_reference.getWidth();
            int boxHeight = iv_reference.getHeight();

            // decode bitmap using our super helper....
            try
            {
                 myBitmap = new BitmapUtility(getApplicationContext()).getDownsampledBitmap(Uri.parse(mCurrentPhotoPath),
                        boxWidth, boxHeight);
            }
            catch (IOException ex)
            {
                // display exception...
            }

            // do something with your bitmap!! (imageView.setBitmap(myBitmap)
        }
    }
}
```
As soon as you dropped this snippet to your code you will notice: BitmapUtility can not be resolved. Well, this little bitmap helper we provide to you as well because simply manually decoding the bitmap using *BitmapFactory* will fail gracefully (OutOfMemory etc.). When dealing with images in Android Android behaves pretty much pussy-like - processing an image taken with a state of the art camera will always result in memory issues. So we created this nice utility for you enabling you to simply specify the size of the image you wanna proceed with - specifying this size enables us to down scale the image and memory isnt an issue at all. The BitmapUtility is displayed below, either copy from here or from repository...simply use or step through the code on your own, there's no rocket science within this snippet so eitehr simply use or simply understand/modify etc.:

```java
public class BitmapUtility
{
    // ------------------------------------------------------------------------
    // members
    // ------------------------------------------------------------------------

    private Context context;

    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

    public BitmapUtility(Context context)
    {
        this.context = context;
    }

    // ------------------------------------------------------------------------
    // public usage
    // ------------------------------------------------------------------------

    public Bitmap createScaledBitmapFromFile(Uri uri, int targetWidth, int targetHeight)
            throws IOException
    {
        Bitmap bitmap = null;
        try
        {
            BitmapFactory.Options outDimens = getBitmapDimensions(uri);

            int sampleSize = calSampleSize(outDimens.outWidth, outDimens.outHeight, targetWidth, targetHeight);

            bitmap = downscaleBitmap(uri, sampleSize);

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (bitmap != null)
        {
            ExifInterface exif = new ExifInterface(uri.getPath());
            String exifOrientation = exif
                    .getAttribute(ExifInterface.TAG_ORIENTATION);
            float degree = getDegree(exifOrientation);
            if (degree != 0)
                bitmap = createRotatedBitmap(bitmap, degree);
        }

        return bitmap;
    }

    // ------------------------------------------------------------------------
    // private usage
    // ------------------------------------------------------------------------

    private BitmapFactory.Options getBitmapDimensions(Uri uri) throws FileNotFoundException, IOException
    {
        BitmapFactory.Options outDimens = new BitmapFactory.Options();
        outDimens.inJustDecodeBounds = true; // the decoder will return null (no bitmap)

        InputStream is= context.getContentResolver().openInputStream(uri);
        // if Options requested only the size will be returned
        BitmapFactory.decodeStream(is, null, outDimens);
        is.close();

        return outDimens;
    }

    private int calSampleSize(int width, int height, int targetWidth, int targetHeight)
    {
        int inSampleSize = 1;

        if (height > targetHeight || width > targetWidth)
        {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) targetHeight);
            final int widthRatio = Math.round((float) width / (float) targetWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    private Bitmap downscaleBitmap(Uri uri, int sampleSize) throws FileNotFoundException, IOException
    {
        Bitmap resizedBitmap;
        BitmapFactory.Options outBitmap = new BitmapFactory.Options();
        outBitmap.inJustDecodeBounds = false; // the decoder will return a bitmap
        outBitmap.inSampleSize = sampleSize;

        InputStream is = context.getContentResolver().openInputStream(uri);
        resizedBitmap = BitmapFactory.decodeStream(is, null, outBitmap);
        is.close();

        return resizedBitmap;
    }

    private float getDegree(String exifOrientation)
    {
        float degree = 0;
        if (exifOrientation.equals("6"))
            degree = 90;
        else if (exifOrientation.equals("3"))
            degree = 180;
        else if (exifOrientation.equals("8"))
            degree = 270;
        return degree;
    }

    private Bitmap createRotatedBitmap(Bitmap bm, float degree)
    {
        Bitmap bitmap = null;
        if (degree != 0)
        {
            Matrix matrix = new Matrix();
            matrix.preRotate(degree);
            bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        }

        return bitmap;
    }
}
```