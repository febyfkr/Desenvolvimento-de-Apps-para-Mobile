package com.example.ac1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, "bancoCadastral.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase banco) {
        banco.execSQL("CREATE TABLE contatos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT, " +
                "telefone TEXT, " +
                "email TEXT, " +
                "categoria TEXT, " +
                "cidade TEXT, " +
                "favorito INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase banco, int versaoAntiga, int versaoNova) {
        banco.execSQL("DROP TABLE IF EXISTS contatos");
        onCreate(banco);
    }
}