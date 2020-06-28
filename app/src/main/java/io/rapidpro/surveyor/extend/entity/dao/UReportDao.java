package io.rapidpro.surveyor.extend.entity.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.local.UReportLocal;

@Dao
public interface UReportDao {

    @Insert
    void insert(UReportLocal... uReportLocals);

    @Update
    void update(UReportLocal... uReportLocals);

    @Delete
    void delete(UReportLocal uReportLocals);

    @Query("SELECT COUNT(*) FROM ureport WHERE ureport_id = :ureport_id")
    int doesUReportExists(int ureport_id);

    @Query("SELECT primaryKey FROM ureport WHERE ureport_id = :ureport_id")
    int getUreport_pKey(int ureport_id);

    @Query("SELECT * FROM ureport")
    List<UReportLocal> getUReports();

    @Query("SELECT * FROM ureport WHERE data_category = :cat")
    List<UReportLocal> getUReportsByCategory(String cat);

    @Query("SELECT * FROM ureport WHERE id = :id")
    UReportLocal getUReportById(int id);

    @Query("SELECT DISTINCT data_category FROM ureport")
    List<String> getUreportCategories();

    @Query("DELETE FROM ureport")
    void deleteAllUReport();
}
