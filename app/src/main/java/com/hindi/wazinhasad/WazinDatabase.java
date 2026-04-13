package com.hindi.wazinhasad;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Transaction.class, Category.class}, version = 4, exportSchema = false)
public abstract class WazinDatabase extends RoomDatabase {

    public abstract TransactionPro transactionDao();


    public abstract CategoryDao categoryDao();


    private static volatile WazinDatabase INSTANCE;

    public static WazinDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WazinDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    WazinDatabase.class, "wazin_hasad_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}