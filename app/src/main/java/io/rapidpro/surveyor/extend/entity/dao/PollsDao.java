package io.rapidpro.surveyor.extend.entity.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.rapidpro.surveyor.extend.entity.local.PollsLocal;

@Dao
public interface PollsDao {

    @Insert
    public void insert(PollsLocal... pollsLocals);

    @Update
    public void update(PollsLocal... pollsLocals);

    @Delete
    public void delete(PollsLocal pollsLocals);

    @Query("SELECT * FROM polls")
    public PollsLocal getPollsList();

    @Query("SELECT * FROM polls WHERE count = :name")
    public PollsLocal getPollsByCount(int name);

    @Query("DELETE FROM polls")
    void deleteAllPolls();
}
