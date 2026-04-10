package com.example.ac1;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText Nome, Telefone, Email, Cidade;
    Spinner Categoria, Filtro;
    CheckBox Favorito;
    ListView ListaContatos;
    DatabaseHelper bancoDeDados;
    ArrayList<String> listaDeContatos;
    int idContatoSelecionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Nome = findViewById(R.id.etNome);
        Telefone = findViewById(R.id.etTelefone);
        Email = findViewById(R.id.etEmail);
        Cidade = findViewById(R.id.etCidade);
        Categoria = findViewById(R.id.spCategoria);
        Filtro = findViewById(R.id.spFiltro);
        Favorito = findViewById(R.id.cbFavorito);
        ListaContatos = findViewById(R.id.listView);

        bancoDeDados = new DatabaseHelper(this);

        Button botaoSalvar = findViewById(R.id.btnSalvar);
        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarContato();
            }
        });

        ListaContatos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                carregarContato(position);
            }
        });

        ListaContatos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                excluirContato(position);
                return true;
            }
        });

        Filtro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listarContatos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        listarContatos();
    }

    private void salvarContato() {
        String nome = Nome.getText().toString();
        String telefone = Telefone.getText().toString();
        String email = Email.getText().toString();
        String categoria = Categoria.getSelectedItem().toString();
        String cidade = Cidade.getText().toString();

        int favorito;
        if (Favorito.isChecked()) {
            favorito = 1;
        } else {
            favorito = 0;
        }

        if (nome.isEmpty() || telefone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase banco = bancoDeDados.getWritableDatabase();

        if (idContatoSelecionado == -1) {
            banco.execSQL("INSERT INTO contatos VALUES (null,?,?,?,?,?,?)",
                    new Object[]{nome, telefone, email, categoria, cidade, favorito});
        } else {
            banco.execSQL("UPDATE contatos SET nome=?, telefone=?, email=?, categoria=?, cidade=?, favorito=? WHERE id=?",
                    new Object[]{nome, telefone, email, categoria, cidade, favorito, idContatoSelecionado});
            idContatoSelecionado = -1;
        }

        limparCampos();
        listarContatos();
    }

    private void listarContatos() {
        listaDeContatos = new ArrayList<>();

        SQLiteDatabase banco = bancoDeDados.getReadableDatabase();

        String categoriaSelecionada;

        if (Filtro.getSelectedItem() == null) {
            categoriaSelecionada = "";
        } else {
            categoriaSelecionada = Filtro.getSelectedItem().toString();
        }

        Cursor cursor = banco.rawQuery(
                "SELECT * FROM contatos WHERE categoria LIKE ?",
                new String[]{categoriaSelecionada}
        );

        while (cursor.moveToNext()) {
            String item = cursor.getInt(0) + " - " +
                    cursor.getString(1) + " | " +
                    cursor.getString(2) + " | " +
                    cursor.getString(3) + " | " +
                    cursor.getString(4);

            listaDeContatos.add(item);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                listaDeContatos
        );

        ListaContatos.setAdapter(adapter);
    }

    private void carregarContato(int posicao) {
        String item = listaDeContatos.get(posicao);
        idContatoSelecionado = Integer.parseInt(item.split(" - ")[0]);

        SQLiteDatabase banco = bancoDeDados.getReadableDatabase();
        Cursor cursor = banco.rawQuery(
                "SELECT * FROM contatos WHERE id=?",
                new String[]{String.valueOf(idContatoSelecionado)}
        );

        if (cursor.moveToFirst()) {
            Nome.setText(cursor.getString(1));
            Telefone.setText(cursor.getString(2));
            Email.setText(cursor.getString(3));
            Cidade.setText(cursor.getString(5));

            if (cursor.getInt(6) == 1) {
                Favorito.setChecked(true);
            } else {
                Favorito.setChecked(false);
            }
        }
    }

    private void excluirContato(int posicao) {
        String item = listaDeContatos.get(posicao);
        int id = Integer.parseInt(item.split(" - ")[0]);

        SQLiteDatabase banco = bancoDeDados.getWritableDatabase();
        banco.execSQL("DELETE FROM contatos WHERE id=?", new Object[]{id});

        listarContatos();
    }

    private void limparCampos() {
        Nome.setText("");
        Telefone.setText("");
        Email.setText("");
        Cidade.setText("");
        Favorito.setChecked(false);
    }
}