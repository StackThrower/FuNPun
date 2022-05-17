package funpun.org;

import android.app.Activity;
import android.os.AsyncTask;

public class GeneralTask extends AsyncTask<Void, Void, Boolean> {

    protected ProgressCallBack showProgressCallback = null;

    Activity activity;
    private AsyncTask asyncTask;

    @Override
    protected Boolean doInBackground(Void... voids) {
        return null;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setShowProgressCallback(ProgressCallBack showProgressCallback) {
        this.showProgressCallback = showProgressCallback;
    }

    public void setTask(AsyncTask asyncTask) {
        this.asyncTask = asyncTask;
    }


    void showProgress(boolean state) {
        if (showProgressCallback != null) {
            showProgressCallback.onShowProgress(state);
        }
    }


    @Override
    protected void onPostExecute(final Boolean success) {
        asyncTask = null;
    }

    @Override
    protected void onCancelled(final Boolean success) {
        asyncTask = null;
    }


}
