package io.rapidpro.surveyor.extend.entity.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.local.PollsLocal;
import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;

@Dao
public interface StoriesDao {

    @Insert
    public void insert(StoriesLocal... storiesLocals);

    @Update
    public void update(StoriesLocal... storiesLocals);

    @Delete
    public void delete(StoriesLocal storiesLocals);

    @Query("SELECT * FROM stories WHERE status = 1 ORDER BY id DESC")
    public List<StoriesLocal> getStoriesList();

    @Query("SELECT * FROM stories WHERE id = :id AND status = 1")
    public StoriesLocal getStoryById(int id);

    @Query("SELECT COUNT(*) FROM stories WHERE id = :id")
    int doesStoryExists(int id);

    @Query("SELECT primaryKey FROM stories WHERE id = :id")
    int getStory_pKey(int id);

    @Query("DELETE FROM stories")
    void deleteAllStories();

    @Query("DELETE FROM stories WHERE id = :id")
    void deleteFromStoryById(int id);
}
