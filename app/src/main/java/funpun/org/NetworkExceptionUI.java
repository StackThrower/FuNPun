package funpun.org;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class NetworkExceptionUI {

    public static void showMessageNoInternetConnection(final Context context, String message) {

        showMessage(context, message);
    }


    public static void showMessageInternalError(final Context context, String message) {
        showMessage(context, message);
    }


    private static void showMessage(final Context context, final String message) {
        try {
            if (context != null) {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(context, message,
                                Toast.LENGTH_LONG).show();
                    }
                });

            }

        } catch (Exception e) {

        }
    }

}
