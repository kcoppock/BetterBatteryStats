/*
 * Copyright (C) 2011-12 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asksven.betterbatterystats;

import java.util.Calendar;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.asksven.android.common.utils.DateUtils;
import com.asksven.android.common.utils.GenericLogger;
import com.asksven.betterbatterystats.R;

/**
 * @author sven
 *
 */
public class BbsWidgetProvider extends AppWidgetProvider
{

	private static final String TAG = "BbsWidgetProvider";
	public static final String WIDGET_UPDATE = "BBS_WIDGET_UPDATE";
	public static final String WIDGET_PREFS_REFRESH = "BBS_WIDGET_PREFS_REFRESH";
	public static final String WIDGET_LOG = "bbs_widget_log";

	
	protected void setAlarm(Context context)
	{
		// set the alarm for next round
		//prepare Alarm Service to trigger Widget
		Intent intent = new Intent(LargeWidgetProvider.WIDGET_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				1234567, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int freqMinutes = Integer.valueOf(sharedPrefs.getString("widget_refresh_freq", "30"));
//		freqMinutes = 1;
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (freqMinutes * 60 * 1000),
				pendingIntent);
		
	}
	
	protected void removeAlarm(Context context)
	{
		Intent intent = new Intent(LargeWidgetProvider.WIDGET_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				1234567, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);		
	}
	
	protected void startService(Context context, Class callerClass, AppWidgetManager appWidgetManager, Class serviceClass)
	{
		// Get all ids
		ComponentName thisWidget = new ComponentName(context, callerClass);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Build the intent to call the service
		Intent intent = new Intent(context.getApplicationContext(), serviceClass);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

		// Update the widgets via the service
		context.startService(intent);

	}
	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);

		if ( (LargeWidgetProvider.WIDGET_UPDATE.equals(intent.getAction())) ||
				intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE") )
		{
			if (LargeWidgetProvider.WIDGET_UPDATE.equals(intent.getAction()))
			{
				Log.d(TAG, "Alarm called: updating");
				GenericLogger.i(LargeWidgetProvider.WIDGET_LOG, TAG, "LargeWidgetProvider: Alarm to refresh widget was called");
			}
			else
			{
				Log.d(TAG, "APPWIDGET_UPDATE called: updating");
			}

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(),
					BbsWidgetProvider.class.getName());
			int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(thisAppWidget);

			onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		// called when widgets are deleted
		// see that you get an array of widgetIds which are deleted
		// so handle the delete of multiple widgets in an iteration
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context)
	{
		super.onDisabled(context);
		// runs when all of the instances of the widget are deleted from
		// the home screen
		
		// remove the alarms

	}

	@Override
	public void onEnabled(Context context)
	{
		super.onEnabled(context);
		// runs when all of the first instance of the widget are placed
		// on the home screen
		setAlarm(context);
	}
	
	
}
