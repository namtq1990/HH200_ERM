/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.HH100.CameraUtil;

import android.HH100.R;
import android.app.Activity;
import android.os.Bundle;

public class VideoActivity extends Activity {

    public static String path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        path = getIntent().getStringExtra("path");

        if (null == savedInstanceState)
        {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, VideoFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        }
    }

    public interface  onKeyBackPressedListener
    {
        public void onBack();
    }
    public onKeyBackPressedListener mOnKeyBackPressedListener;

    public void setOnKeyBackPressedListener(onKeyBackPressedListener listener)
    {
        mOnKeyBackPressedListener = listener;
    }


    @Override
    public void onBackPressed()
    {

        if (mOnKeyBackPressedListener != null)
        {
            mOnKeyBackPressedListener.onBack();
        }
        else
        {
            finish();
           // super.onBackPressed();
        }

    }


}
