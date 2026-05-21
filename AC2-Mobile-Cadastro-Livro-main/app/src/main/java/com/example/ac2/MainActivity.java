package com.example.ac2;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    EditText titulo, autor, ano, busca;
    Spinner genero, status, filtro;
    CheckBox favorito;
    Button salvar, buscarBtn;
    ListView lista;

    ArrayList<Livro> livros = new ArrayList<>();

    String idLivro = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        titulo = findViewById(R.id.etTitulo);
        autor = findViewById(R.id.etAutor);
        ano = findViewById(R.id.etAno);
        busca = findViewById(R.id.etBusca);
        genero = findViewById(R.id.spGenero);
        status = findViewById(R.id.spStatus);
        filtro = findViewById(R.id.spFiltro);
        favorito = findViewById(R.id.cbFavorito);
        salvar = findViewById(R.id.btnSalvar);
        buscarBtn = findViewById(R.id.btnBuscar);
        lista = findViewById(R.id.listaLivros);

        carregarSpinners();

        salvar.setOnClickListener(v -> salvarLivro());

        buscarBtn.setOnClickListener(v -> {
            String textoBusca = busca.getText().toString().trim();

            if (!textoBusca.isEmpty()) {
                buscarPorTitulo(textoBusca);
            } else {
                listarLivros();
            }
        });

        lista.setOnItemClickListener((parent, view, position, id) -> editarLivro(position));

        lista.setOnItemLongClickListener((parent, view, position, id) -> {
            excluirLivro(position);
            return true;
        });

        listarLivros();
    }

    private void carregarSpinners() {
        String[] generos = {
                "Selecione",
                "Romance",
                "Fantasia",
                "Terror",
                "Ficção Científica",
                "Biografia"
        };

        String[] statusLeitura = {
                "Selecione",
                "Quero ler",
                "Lendo",
                "Concluído"
        };

        ArrayAdapter<String> adapterGenero =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, generos);

        ArrayAdapter<String> adapterStatus =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusLeitura);

        genero.setAdapter(adapterGenero);
        status.setAdapter(adapterStatus);
        filtro.setAdapter(adapterGenero);
    }

    private void salvarLivro() {
        if (titulo.getText().toString().isEmpty()) {
            Toast.makeText(this, "Digite o título", Toast.LENGTH_SHORT).show();
            return;
        }

        Livro livro = new Livro();

        livro.setTitulo(titulo.getText().toString());
        livro.setAutor(autor.getText().toString());
        livro.setGenero(genero.getSelectedItem().toString());
        livro.setAno(Integer.parseInt(ano.getText().toString()));
        livro.setStatus(status.getSelectedItem().toString());
        livro.setFavorito(favorito.isChecked());

        if (idLivro == null) {
            db.collection("livros").add(livro);
        } else {
            db.collection("livros").document(idLivro).set(livro);
        }

        limparCampos();
        listarLivros();
    }

    private void listarLivros() {
        db.collection("livros").get().addOnSuccessListener(snapshot -> {
            livros.clear();

            for (var doc : snapshot) {
                Livro l = doc.toObject(Livro.class);
                l.setId(doc.getId());
                livros.add(l);
            }

            lista.setAdapter(new LivroAdapter(this, livros));
        });
    }

    private void buscarPorTitulo(String tituloBusca) {
        db.collection("livros").get().addOnSuccessListener(snapshot -> {
            livros.clear();

            for (var doc : snapshot) {
                Livro l = doc.toObject(Livro.class);
                l.setId(doc.getId());

                if (l.getTitulo().toLowerCase().contains(tituloBusca.toLowerCase())) {
                    livros.add(l);
                }
            }

            lista.setAdapter(new LivroAdapter(this, livros));
        });
    }

    private void editarLivro(int pos) {
        Livro l = livros.get(pos);

        idLivro = l.getId();

        titulo.setText(l.getTitulo());
        autor.setText(l.getAutor());
        ano.setText(String.valueOf(l.getAno()));
        favorito.setChecked(l.isFavorito());
    }

    private void excluirLivro(int pos) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage("Deseja excluir?")
                .setPositiveButton("Sim", (d, w) -> {
                    db.collection("livros")
                            .document(livros.get(pos).getId())
                            .delete();

                    listarLivros();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void limparCampos() {
        titulo.setText("");
        autor.setText("");
        ano.setText("");
        busca.setText("");
        favorito.setChecked(false);
        idLivro = null;
    }
}