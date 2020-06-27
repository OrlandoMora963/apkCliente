package com.cor.frii.persistence.dao;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cor.frii.persistence.entity.Acount;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface AcountDao {

    @Query("SELECT * FROM acount")
    List<Acount> getUsers();

    @Query("SELECT * FROM acount WHERE id = :id")
    Acount getUser(int id);

    @Insert
    void addUser(Acount... user);

    @Delete
    void deleteUser(Acount user);

    @Update
    void updateUser(Acount... user);

    @Query("SELECT * FROM acount WHERE email=:username AND password=:password")
    Acount login(String username, String password);

    @Query("DELETE FROM acount WHERE id= :id")
    void deleteById(int id);

}