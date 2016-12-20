/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.popumovies;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;

public class SampleDebugApplication extends android.app.Application {
  private static final String TAG = "SampleDebugApplication";

  @Override
  public void onCreate() {
    super.onCreate();

    long startTime = SystemClock.elapsedRealtime();
    initializeStetho(this);
    long elapsed = SystemClock.elapsedRealtime() - startTime;
    Log.i(TAG, "Stetho initialized in " + elapsed + " ms");
//    OkHttpClient client = new OkHttpClient.Builder()
//            .addNetworkInterceptor(new StethoInterceptor())
//            .build();
//      super.onCreate();
//      Stetho.initializeWithDefaults(this);
  }

  private void initializeStetho(final Context context) {
    // See also: Stetho.initializeWithDefaults(Context)
    Stetho.initialize(Stetho.newInitializerBuilder(context)
//        .enableDumpapp(new DumperPluginsProvider() {
//          @Override
//          public Iterable<DumperPlugin> get() {
//            return new Stetho.DefaultDumperPluginsBuilder(context)
//                .provide(new HelloWorldDumperPlugin())
//                .provide(new APODDumperPlugin(context.getContentResolver()))
//                .finish();
//          }
//        })
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(new ExtInspectorModulesProvider(context))
        .build());
  }

  private static class ExtInspectorModulesProvider implements InspectorModulesProvider {

    private Context mContext;

    ExtInspectorModulesProvider(Context context) {
      mContext = context;
    }

    @Override
    public Iterable<ChromeDevtoolsDomain> get() {
      return new Stetho.DefaultInspectorModulesBuilder(mContext)
//          .provideDatabaseDriver(createContentProviderDatabaseDriver(mContext))
          .finish();
    }

//    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//    private ContentProviderDatabaseDriver createContentProviderDatabaseDriver(Context context) {
//      ContentProviderSchema calendarsSchema = new ContentProviderSchema.Builder()
//          .table(new Table.Builder()
//              .uri(CalendarContract.Calendars.CONTENT_URI)
//              .projection(new String[] {
//                  CalendarContract.Calendars._ID,
//                  CalendarContract.Calendars.NAME,
//                  CalendarContract.Calendars.ACCOUNT_NAME,
//                  CalendarContract.Calendars.IS_PRIMARY,
//              })
//              .build())
//          .build();
//
//      // sample events content provider we want to support
//      ContentProviderSchema eventsSchema = new ContentProviderSchema.Builder()
//          .table(new Table.Builder()
//              .uri(CalendarContract.Events.CONTENT_URI)
//              .projection(new String[]{
//                  CalendarContract.Events._ID,
//                  CalendarContract.Events.TITLE,
//                  CalendarContract.Events.DESCRIPTION,
//                  CalendarContract.Events.ACCOUNT_NAME,
//                  CalendarContract.Events.DTSTART,
//                  CalendarContract.Events.DTEND,
//                  CalendarContract.Events.CALENDAR_ID,
//              })
//              .build())
//          .build();
//      return new ContentProviderDatabaseDriver(context, calendarsSchema, eventsSchema);
//    }
  }

}
