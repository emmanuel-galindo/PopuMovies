package com.popumovies;

import com.facebook.stetho.Stetho;

/**
 * Created by manu on 4/29/16.
 */
public class MyDebugApplication extends android.app.Application {

        @Override
        public void onCreate() {
            super.onCreate();
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .build());

        }
}
