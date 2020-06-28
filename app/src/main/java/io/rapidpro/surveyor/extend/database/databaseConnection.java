package io.rapidpro.surveyor.extend.database;

import android.content.Context;

import androidx.room.Room;

public class databaseConnection {

    // Static Database Object
    static AppDatabase database = null;

    /**
     * Connect to Room Database with no return
     * @param context
     */
    public databaseConnection(Context context){
        if(database == null){
            database = Room.databaseBuilder(context, AppDatabase.class, "com.riseuplabs.ureport")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
    }

    /**
     * Initializes Room Database and returns object.
     * @param context
     * @return
     */
    public static AppDatabase getDatabase(Context context){
        if(database == null){
            database = Room.databaseBuilder(context, AppDatabase.class, "com.riseuplabs.ureport")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
            return database;
        }else{
            return database;
        }
    }

}
