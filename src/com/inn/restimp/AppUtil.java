package com.inn.restimp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class AppUtil {
	public static int SELECTED_STEP_NO = 0;
	private static char[] chunkBuffer = new char[1024];

	/**
	 * @param view
	 * @return
	 */
	public final static Bitmap takeScreenShot(View view) {
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		return bitmap;
	}

	/**
	 * checks if application package is installed on device or not
	 * 
	 * @param packagename
	 * @param context
	 * @return
	 */
	public static final boolean isPackageInstalled(String packagename, Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * @param filePath
	 * @param width
	 * @param height
	 * @return
	 */
	public final static Bitmap getBitmapFromFile(String filePath, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		options.inSampleSize = calculateInSampleSize(options, width, height);

		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

		ExifInterface ei = null;
		try {
			ei = new ExifInterface(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_90:
			bitmap = rotateImage(bitmap, 90);
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			bitmap = rotateImage(bitmap, 180);
			break;
		}

		return bitmap;
	}

	/**
	 * @param array
	 * @param toFind
	 * @return
	 */
	public static final int indexOf(String[] array, String toFind) {
		for (int i = 0; i < array.length; i++) {
			if (toFind.equals(array[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @param pBitmap
	 * @param angle
	 * @return
	 */
	private static Bitmap rotateImage(Bitmap pBitmap, int angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(pBitmap, 0, 0, pBitmap.getWidth(), pBitmap.getHeight(), matrix, true);
	}

	/**
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public final static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		if (reqWidth == 0 && reqHeight == 0)
			return 1;
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public final static double calDistance(double x1, double y1, double x2, double y2) {
		double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		return distance;
	}

	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param p
	 * @param q
	 * @return
	 */
	public static double calDistanceBtwLineAndPoint(double x1, double y1, double x2, double y2, double p, double q) {
		double m = (y2 - y1) / (x2 - x1);
		double c = y1 - (m * x1);
		double res = Math.abs(q - m * p - c) / Math.sqrt(1 + m * m);
		return res;
	}

	/**
	 * @param timeInMillis
	 * @param format
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public final static String getFormattedDate(long timeInMillis, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeInMillis);
		String timeStamp = new SimpleDateFormat(format).format(cal.getTime());
		return timeStamp;
	}

	/**
	 * @param date
	 * @param format
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public final static String getFormattedDate(Date date, String format) {
		if (date == null)
			return "-";

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String timeStamp = new SimpleDateFormat(format).format(cal.getTime());
		return timeStamp;
	}

	/**
	 * @param rd
	 * @return
	 */
	public synchronized static String readData(InputStreamReader rd) {
		try {
			StringBuffer sb = new StringBuffer();
			while (true) {
				int read = rd.read(chunkBuffer, 0, chunkBuffer.length);
				if (read == -1)
					break;
				sb.append(chunkBuffer, 0, read);
			}
			return sb.toString();
		} catch (IOException e) {
		} finally {
			try {
				rd.close();
			} catch (IOException e) {
			}
		}
		return "";
	}

	/**
	 * @param activity
	 */
	public static void hideSoftKeyboard(Activity activity) {
		if (activity.getCurrentFocus() == null)
			return;
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}

	/**
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static void setFontToAllViews(ViewGroup layout, Typeface font) {
		for (int i = 0; i < layout.getChildCount(); i++) {
			View childAt = layout.getChildAt(i);
			if (childAt instanceof ViewGroup) {
				setFontToAllViews((ViewGroup) childAt, font);
			} else {
				if (childAt instanceof TextView) {
					TextView child = (TextView) childAt;
					if (!child.getText().equals("Verify")) {
						Typeface typeface = child.getTypeface();
						if (typeface != null) {
							child.setTypeface(font, typeface.getStyle());
						} else {
							child.setTypeface(font);
						}
					}
				}
			}
		}
	}

	public static void setFontToAllStep3(ViewGroup layout, Typeface font, Typeface boldfont) {
		for (int i = 0; i < layout.getChildCount(); i++) {
			View childAt = layout.getChildAt(i);
			if (childAt instanceof ViewGroup) {
				setFontToAllViews((ViewGroup) childAt, font);
			} else {
				if (childAt instanceof TextView) {
					TextView child = (TextView) childAt;
					if (!child.getText().equals("Verify")) {
						Typeface typeface = child.getTypeface();
						if (typeface != null) {
							child.setTypeface(font, typeface.getStyle());
						} else {
							child.setTypeface(font);
						}
					} else if (child.getTag().equals("question")) {
						System.out.println("setting font in question");
						Typeface typeface = child.getTypeface();
						child.setTypeface(boldfont);
					}
				}
			}
		}
	}

	public static final String formatTo2Decimals(double val) {
		DecimalFormat df2 = new DecimalFormat("00.00");
		return df2.format(val);
	}

	/**
	 * @param file
	 * @param data
	 */
	public static void writeOnFile(File file, String data) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(data);
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final String getSuffix(int dayOfMonth) {
		String suffix = "";
		switch (dayOfMonth) {
		case 1:
		case 21:
		case 31:
			suffix = "st";
			break;
		case 2:
		case 22:
			suffix = "nd";
			break;
		case 3:
		case 23:
			suffix = "rd";
			break;
		default:
			suffix = "th";
		}
		return suffix;
	}

	public static void setOverflowMenu(Context context) {
		try {
			ViewConfiguration config = ViewConfiguration.get(context);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
