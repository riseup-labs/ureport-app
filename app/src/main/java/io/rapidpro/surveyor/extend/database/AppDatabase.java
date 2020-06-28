package io.rapidpro.surveyor.extend.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import io.rapidpro.surveyor.extend.entity.dao.StoriesDao;
import io.rapidpro.surveyor.extend.entity.dao.SurveyorDao;
import io.rapidpro.surveyor.extend.entity.dao.UReportDao;
import io.rapidpro.surveyor.extend.entity.local.PollsLocal;
import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;
import io.rapidpro.surveyor.extend.entity.local.SurveyorLocal;
import io.rapidpro.surveyor.extend.entity.local.UReportLocal;

/**
 * Standard Room Database
 * Important: Always increase version number
 * in-case of database schema change.
 */
@Database(entities = {PollsLocal.class, StoriesLocal.class, UReportLocal.class, SurveyorLocal.class},
                      version = 12, exportSchema = false)

/**
 * Abstract Class for Room Database
 */
public abstract class AppDatabase extends RoomDatabase {
    public abstract StoriesDao getStories();
    public abstract UReportDao getUReports();
    public abstract SurveyorDao getSurveyor();
}


