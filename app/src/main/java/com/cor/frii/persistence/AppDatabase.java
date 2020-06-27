package com.cor.frii.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cor.frii.persistence.dao.CartDao;
import com.cor.frii.persistence.entity.Acount;
import com.cor.frii.persistence.entity.ECart;
import com.cor.frii.persistence.dao.AcountDao;

@Database(entities = {
        Acount.class,
        ECart.class
}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CartDao getCartDao();
    public abstract AcountDao getAcountDao();

}