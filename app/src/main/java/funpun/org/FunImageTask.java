package funpun.org;



import java.util.List;

public class FunImageTask extends GeneralTask {

    enum FunImageMode {
        GET_LIST,
        LIKE,
        ADD
    }

    FunImageMode mode;
    List<FunImage> funImages;
    public int page = 0;
    public String url;
    public Integer id;

    protected FunImageTaskProcessing funImageTaskProcessing;

    public FunImageTask(FunImageMode mode) {
        this.mode = mode;
        showProgress(true);
    }



    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            Network networkImpl = new Network();

            switch (mode) {
                case GET_LIST:
                    funImages = networkImpl.getList(page);
                    break;
                case ADD:
                    networkImpl.add(url);
                    break;
                case LIKE:
                    networkImpl.like(id);
                    break;
            }
        } catch (Exception e) {
            NetworkExceptionUI.showMessageNoInternetConnection(activity, e.getMessage());
        }

        return true;
    }

    private void processData() {


        switch (mode) {
            case GET_LIST:

                if (funImageTaskProcessing != null)
                    funImageTaskProcessing.onProcessFunImagesResponse(funImages);
                break;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        super.onPostExecute(success);
        processData();
        showProgress(false);
    }

    @Override
    protected void onCancelled(final Boolean success) {
        super.onCancelled(success);
        processData();
        showProgress(false);
    }


    public void setProcessing(FunImageTaskProcessing funImageTaskProcessing) {
        this.funImageTaskProcessing = funImageTaskProcessing;
    }
}
