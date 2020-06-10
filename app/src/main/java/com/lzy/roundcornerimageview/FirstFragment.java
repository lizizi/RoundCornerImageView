package com.lzy.roundcornerimageview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.lzy.roundcornerimageview.view.RoundCornerImageView;

public class FirstFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        RoundCornerImageView imageView = view.findViewById(R.id.iv_test);
        Bitmap bitmap = getSimpleScaleBitmap();
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 简单缩放
     * @return bitmap
     */
    private Bitmap getSimpleScaleBitmap(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.mipmap.test, options);
        int width = options.outWidth;
        int height = options.outHeight;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int scale = 1;
        while (width > screenWidth || height > screenHeight){
            width = width / 2;
            height = height / 2;
            scale = scale * 2;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;
        return BitmapFactory.decodeResource(getResources(), R.mipmap.test, options);
    }
}