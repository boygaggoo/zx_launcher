/*
 * Copyright (c) 2016. Naivor.All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ds05.launcher.common.utils;

import android.content.Context;
import android.widget.Toast;


/**
 * ToastUtil  提示工具类,处理整个应用的toast提示，使用单例实现，避免在大量Toast消息弹出的时候某些消息无法显示
 *
 * @author tianlai
 */
public class ToastUtil {
    private static Toast toast;
    private static long time;
    private static String mMessage;

    /**
     * 显示消息
     *
     * @param message 消息
     */
    public static void showToast(Context context, String message) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            time = System.currentTimeMillis();
            mMessage = message;
            toast.show();
        } else {
            long now = System.currentTimeMillis();

            if (message.equals(mMessage)) {
                if (isNeedShow(now)) {

                    toast.show();

                    time = now;
                }

            } else {
                toast.setText(message);

                toast.show();

                time = now;
                mMessage = message;
            }
        }
    }

    /**
     * 判断是否需要显示消息
     *
     * @param now 现在时间
     */
    private static boolean isNeedShow(long now) {
        return time == 0 || (now - time) > 2000;
    }

    /**
     * 取消显示的toast
     */
    public static void cancleToast() {

        if (toast!=null){
            toast.cancel();
        }
    }

}
