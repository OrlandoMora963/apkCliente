package com.cor.frii.persistence.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cor.frii.persistence.entity.ECart;

import java.util.List;

@Dao
public interface CartDao {

    @Query("SELECT * FROM cart")
    List<ECart> getCarts();

    @Query("SELECT * FROM cart WHERE uid = :uid  ")
    ECart getCart(String uid);

    @Insert
    void addCart(ECart... cart);

    @Delete
    void deleteCart(ECart... cart);

    @Update
    void updateCart(ECart... cart);

    @Query("DELETE FROM cart")
    void deleteAllCart();
}