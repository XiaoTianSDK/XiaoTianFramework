package com.xiaotian.frameworkxt.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @version 1.0.0
 * @author XiaoTian
 * @name BRNetworkError
 * @description Network Error Receiver
 * @date 2014-8-26
 * @link gtrstudio@qq.com
 * @copyright Copyright © 2009-2014 广州线上线下信息科技 Ltd, All Rights Reserved.
 */
public class BRToast extends BroadcastReceiver {
	public static final String ACTION = "com.xiaotian.frameworkxt.android.service.BRToast";
	public static final String PARAM_CONTENT = "com.xiaotian.frameworkxt.android.service.Name";
	public static final String PARAM_SHOW_TIME = "com.xiaotian.frameworkxt.android.service.Showtime";

	@Override
	public void onReceive(Context context, Intent intent) {
		String text = intent.getStringExtra(PARAM_CONTENT);
		if (text == null || text.equals("")) return;
		Toast.makeText(context, text, intent.getIntExtra(PARAM_SHOW_TIME, Toast.LENGTH_SHORT)).show();
	}
}
