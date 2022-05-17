package funpun.org;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
//import catcut.net.fragments.ShortLinkListFragment.OnListFragmentInteractionListener;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
//import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static io.fabric.sdk.android.Fabric.TAG;

public class FunImageRecyclerViewAdapter extends
        RecyclerView.Adapter<FunImageRecyclerViewAdapter.ViewHolder> {


    private static final int AD_TYPE = 1;
    private static final int CONTENT_TYPE = 0;
    private static final int LIST_AD_DELTA = 4;

    private final List<FunImage> mValues = new ArrayList<>();
    private Activity activity;
//    private final OnListFragmentInteractionListener mListener;


    public FunImageRecyclerViewAdapter(Activity activity) {
        this.activity = activity;
//        mListener = listener;
    }


    public void addImages(List<FunImage> list) {

        if(list != null && list.size() > 0)
            mValues.addAll(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == AD_TYPE) {
            AdView adView = new AdView(parent.getContext());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId("ca-app-pub-3579118192425679/4770154063");

            return new ViewHolder(adView);

        } else
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_images_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (getItemViewType(position) == CONTENT_TYPE) {

            holder.mItem = mValues.get(position);

            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;




//
            Picasso picasso = Picasso.get();
            picasso.setLoggingEnabled(true);

            Picasso.get().load(holder.mItem.url)
                    .resize(width, Math.round(height/2))
//                    .centerInside()
                    .into(holder.mImage);



            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (null != mListener) {
//                        // Notify the active callbacks interface (the activity, if the
//                        // fragment is attached to one) that an item has been selected.
//                        mListener.onListFragmentInteraction(holder, "selected");
//                    }
                }
            });


            holder.mShare.setOnClickListener(new ImageButton.OnClickListener() {


                private Uri saveImage(Bitmap image) {
                    //TODO - Should be processed in another thread
                    File imagesFolder = new File(activity.getCacheDir(), "images");
                    Uri uri = null;
                    try {
                        imagesFolder.mkdirs();
                        File file = new File(imagesFolder, "shared_image.png");

                        FileOutputStream stream = new FileOutputStream(file);
                        image.compress(Bitmap.CompressFormat.PNG, 90, stream);
                        stream.flush();
                        stream.close();
                        uri = FileProvider.getUriForFile(activity, "funpun.org.fileprovider", file);

                    } catch (IOException e) {
                        Log.d(TAG, "IOException while trying to write file for sharing: " + e.getMessage());
                    }
                    return uri;
                }

                @Override
                public void onClick(View v) {



                    Bundle params = new Bundle();
//                    params.putString("url", shareBody);
                    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
                    mFirebaseAnalytics.logEvent("share_content", params);


                    Drawable mDrawable = holder.mImage.getDrawable();
                    Bitmap mBitmap = ((BitmapDrawable)mDrawable).getBitmap();


                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("image/*");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getResources().getString(R.string.share));

                    sharingIntent.putExtra(Intent.EXTRA_STREAM, saveImage(mBitmap));

                    activity.startActivity(Intent.createChooser(sharingIntent, activity.getResources().getString(R.string.share)));
                }
            });

            holder.mCounter.setText(String.valueOf(holder.mItem.liked));
            holder.mLike.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {

                holder.mCounter.setText(String.valueOf(holder.mItem.liked + 1));

                holder.mLike.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary),
                        android.graphics.PorterDuff.Mode.SRC_IN);

                FunImageTask funImageTask = new FunImageTask(FunImageTask.FunImageMode.LIKE);
                funImageTask.id = holder.mItem.id;
                funImageTask.execute((Void) null);


                    Bundle params = new Bundle();
                    FirebaseAnalytics mFirebaseAnalytics =
                            FirebaseAnalytics.getInstance(activity);
                    mFirebaseAnalytics.logEvent("like_content", params);


                }
            });

        } else {

            AdRequest adRequest = new AdRequest.Builder().build();

            float density = holder.mView.getContext().getResources().getDisplayMetrics().density;
            int height = Math.round(AdSize.BANNER.getHeight() * (int)(density * 1.75));
            AbsListView.LayoutParams params =
                    new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, height);

            if (holder.mView instanceof AdView) {

                holder.mView.setLayoutParams(params);
                ((AdView) holder.mView).setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        Bundle params = new Bundle();
                        FirebaseAnalytics mFirebaseAnalytics =
                                FirebaseAnalytics.getInstance(activity);
                        mFirebaseAnalytics.logEvent("ad_load", params);
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Code to be executed when an ad request fails.
                    }

                    @Override
                    public void onAdOpened() {
                        Bundle params = new Bundle();
                        FirebaseAnalytics mFirebaseAnalytics =
                                FirebaseAnalytics.getInstance(activity);
                        mFirebaseAnalytics.logEvent("ad_opened", params);
                    }

                    @Override
                    public void onAdLeftApplication() {
                        Bundle params = new Bundle();
                        FirebaseAnalytics mFirebaseAnalytics =
                                FirebaseAnalytics.getInstance(activity);
                        mFirebaseAnalytics.logEvent("ad_left_app", params);
                    }

                    @Override
                    public void onAdClosed() {
                        // Code to be executed when when the user is about to return
                        // to the app after tapping on an ad.
                    }
                });
                ((AdView) holder.mView).loadAd(adRequest);
            }


        }
    }


    @Override
    public int getItemViewType(int position) {

        if(MainActivity.isAdEnabled()) {
            if (position > 0 && position % LIST_AD_DELTA == 0 || position == 1)
                return AD_TYPE;
        }

        return CONTENT_TYPE;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImage;
        public final ImageView mLike;
        public final ImageView mShare;
        public final TextView mCounter;
        public FunImage mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImage = view.findViewById(R.id.image);
            mShare = view.findViewById(R.id.share);
            mLike = view.findViewById(R.id.like);
            mCounter = view.findViewById(R.id.counter);
        }
    }
}
