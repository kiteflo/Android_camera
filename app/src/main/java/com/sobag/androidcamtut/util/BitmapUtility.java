package com.sobag.androidcamtut.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by tzhmufl2 on 27.07.14.
 */
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
