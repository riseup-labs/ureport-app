package io.rapidpro.surveyor.extend.entity.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.local.SurveyorLocal;
import io.rapidpro.surveyor.extend.entity.local.UReportLocal;

@Dao
public interface SurveyorDao {

    @Insert
    void insert(SurveyorLocal... surveyorLocals);

    @Update
    void update(SurveyorLocal... surveyorLocals);

    @Delete
    void delete(SurveyorLocal surveyorLocals);

    @Query("SELECT COUNT(*) FROM surveyor WHERE flow_id = :flow_id")
    int doesSurveyExists(String flow_id);

    @Query("SELECT primaryKey FROM surveyor WHERE flow_id = :flow_id")
    int getSurvey_pKey(String flow_id);

    @Query("SELECT * FROM surveyor WHERE status = 1 ORDER BY id DESC")
    List<SurveyorLocal> getSurveys();

    @Query("SELECT * FROM surveyor WHERE data_category = :cat AND status = 1 ORDER BY id DESC")
    List<SurveyorLocal> getSurveysByCategory(String cat);

    @Query("SELECT * FROM surveyor WHERE flow_id = :flow_id")
    SurveyorLocal getSurveyByFlowId(String flow_id);

    @Query("SELECT DISTINCT data_category FROM surveyor")
    List<String> getSurveyCategories();

    @Query("DELETE FROM surveyor")
    void deleteAllSurveyor();
}
