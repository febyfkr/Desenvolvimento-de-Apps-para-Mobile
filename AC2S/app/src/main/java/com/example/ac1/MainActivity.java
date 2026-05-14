package com.example.ac1;

// ============================================================
// MAINACTIVITY.JAVA - VERSÃO FIREBASE (AC2)
// ============================================================
// DIFERENÇAS PRINCIPAIS em relação à AC1 (SQLite):
//
//  AC1 (SQLite)                 → AC2 (Firebase Firestore)
//  -----------------------------------------------------------
//  DatabaseHelper               → FirebaseFirestore db
//  SQLiteDatabase banco         → db.collection("contatos")
//  banco.execSQL(INSERT)        → db.collection().add()
//  banco.execSQL(UPDATE)        → db.collection().document().set()
//  banco.execSQL(DELETE)        → db.collection().document().delete()
//  banco.rawQuery(SELECT *)     → db.collection().get()
//  WHERE categoria = ?          → .whereEqualTo("categoria", valor)
//  int idContatoSelecionado=-1  → String idContatoSelecionado=null
//  ArrayList<String>            → ArrayList<Contato>
//
// IMPORTANTE: Firestore é ASSÍNCRONO!
//   O SQLite retornava resultado na hora.
//   O Firestore usa callbacks: .addOnSuccessListener() e .addOnFailureListener()
//   Por isso o código continua DENTRO do listener, não depois da chamada.
// ============================================================

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

// Imports do Firebase Firestore
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Campos do formulário - mesmos da AC1
    EditText Nome, Telefone, Email, Cidade;
    Spinner Categoria, Filtro;
    CheckBox Favorito;
    ListView ListaContatos;

    // ============================================================
    // FIREBASE FIRESTORE
    // Substitui: DatabaseHelper bancoDeDados;
    // O FirebaseFirestore é o objeto principal para acessar o banco
    // ============================================================
    FirebaseFirestore db;

    // Lista de objetos Contato (substitui ArrayList<String> da AC1)
    // Agora guardamos os objetos completos para facilitar edição/exclusão
    ArrayList<Contato> listaDeContatos = new ArrayList<>();

    // Lista de strings formatadas para exibir na ListView
    ArrayList<String> listaExibicao = new ArrayList<>();

    // Adapter da ListView
    ArrayAdapter<String> adapter;

    // ============================================================
    // ID DO CONTATO SELECIONADO PARA EDIÇÃO
    // AC1: int idContatoSelecionado = -1;  (inteiro, -1 = nenhum)
    // AC2: String idContatoSelecionado = null; (String, null = nenhum)
    //       O Firestore usa IDs String gerados automaticamente
    //       Ex: "6pJiPFTDCUGEK9HxUL24"
    // ============================================================
    String idContatoSelecionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referências aos componentes da tela (igual à AC1)
        Nome = findViewById(R.id.etNome);
        Telefone = findViewById(R.id.etTelefone);
        Email = findViewById(R.id.etEmail);
        Cidade = findViewById(R.id.etCidade);
        Categoria = findViewById(R.id.spCategoria);
        Filtro = findViewById(R.id.spFiltro);
        Favorito = findViewById(R.id.cbFavorito);
        ListaContatos = findViewById(R.id.listView);

        // ============================================================
        // INICIALIZAÇÃO DO FIREBASE FIRESTORE
        // AC1: bancoDeDados = new DatabaseHelper(this);
        // AC2: db = FirebaseFirestore.getInstance();
        //      getInstance() retorna a instância do banco do Firebase
        //      configurado no google-services.json do projeto
        // ============================================================
        db = FirebaseFirestore.getInstance();

        // Configura o adapter da ListView com a lista de exibição
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaExibicao);
        ListaContatos.setAdapter(adapter);

        // Botão Salvar
        Button botaoSalvar = findViewById(R.id.btnSalvar);
        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarContato();
            }
        });

        // Click simples na lista → carregar para edição
        ListaContatos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                carregarContato(position);
            }
        });

        // Click longo na lista → excluir
        ListaContatos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                excluirContato(position);
                return true;
            }
        });

        // Filtro de categoria → recarregar lista ao mudar
        Filtro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listarContatos();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Carrega os contatos ao iniciar o app
        listarContatos();
    }

    // ============================================================
    // SALVAR CONTATO (INSERT ou UPDATE)
    // AC1 usava: banco.execSQL("INSERT...") ou banco.execSQL("UPDATE...")
    // AC2 usa:
    //   INSERT → db.collection("contatos").add(objeto)
    //   UPDATE → db.collection("contatos").document(id).set(objeto)
    // ============================================================
    private void salvarContato() {
        String nome = Nome.getText().toString();
        String telefone = Telefone.getText().toString();
        String email = Email.getText().toString();
        String categoria = Categoria.getSelectedItem().toString();
        String cidade = Cidade.getText().toString();
        boolean favorito = Favorito.isChecked();

        // Validação (igual à AC1)
        if (nome.isEmpty() || telefone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cria o objeto Contato
        // No AC1, usávamos ContentValues ou passávamos parâmetros diretamente no execSQL
        // No AC2, criamos um objeto e o Firestore serializa automaticamente
        Contato contato = new Contato(null, nome, telefone, email, categoria, cidade, favorito);

        if (idContatoSelecionado == null) {
            // ========================================================
            // INSERT - Novo contato
            // AC1: banco.execSQL("INSERT INTO contatos VALUES (null,?,?,?,?,?,?)", ...)
            // AC2: db.collection("contatos").add(objeto)
            //
            // .add() gera um ID automaticamente para o documento
            // O resultado vem no .addOnSuccessListener com a referência do documento
            // ========================================================
            db.collection("contatos")
                .add(contato)
                .addOnSuccessListener(documentReference -> {
                    // documentReference.getId() retorna o ID gerado (ex: "6pJiPFTDCUGEK9HxUL24")
                    Toast.makeText(this, "Contato salvo!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                    listarContatos(); // Atualiza a lista
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        } else {
            // ========================================================
            // UPDATE - Editar contato existente
            // AC1: banco.execSQL("UPDATE contatos SET ... WHERE id=?", ...)
            // AC2: db.collection("contatos").document(id).set(objeto)
            //
            // .document(id) especifica qual documento atualizar pelo ID
            // .set() substitui todos os campos do documento
            // ========================================================
            contato.setId(idContatoSelecionado);
            db.collection("contatos").document(idContatoSelecionado)
                .set(contato)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Contato atualizado!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                    listarContatos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        }
    }

    // ============================================================
    // LISTAR CONTATOS (SELECT com ou sem filtro)
    // AC1 usava: Cursor cursor = banco.rawQuery("SELECT * FROM contatos WHERE categoria LIKE ?", ...)
    // AC2 usa:
    //   SEM filtro → db.collection("contatos").get()
    //   COM filtro → db.collection("contatos").whereEqualTo("campo", valor).get()
    // ============================================================
    private void listarContatos() {
        String filtroSelecionado = Filtro.getSelectedItem() != null
                ? Filtro.getSelectedItem().toString()
                : "Todos";

        Query query;

        if (filtroSelecionado.equals("Todos")) {
            // ======================================================
            // SELECT * FROM contatos
            // AC2: db.collection("contatos") sem filtros
            // ======================================================
            query = db.collection("contatos");
        } else {
            // ======================================================
            // SELECT * FROM contatos WHERE categoria = 'Família'
            // AC2: .whereEqualTo("categoria", "Família")
            // O primeiro parâmetro é o nome do CAMPO no Firestore
            // O segundo é o valor a comparar
            // ======================================================
            query = db.collection("contatos")
                    .whereEqualTo("categoria", filtroSelecionado);
        }

        // .get() busca os dados - resultado vem no addOnSuccessListener
        query.get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Limpa as listas antes de repopular
                listaDeContatos.clear();
                listaExibicao.clear();

                // Percorre todos os documentos retornados
                // No AC1: while (cursor.moveToNext()) { ... }
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                    // doc.toObject(Contato.class) converte o documento em objeto Contato
                    // O Firestore faz isso automaticamente usando os getters/setters da classe
                    Contato c = doc.toObject(Contato.class);

                    // Define o ID (o ID do documento não vem automaticamente no toObject)
                    c.setId(doc.getId());

                    // Adiciona à lista de objetos
                    listaDeContatos.add(c);

                    // Formata string para exibição na ListView
                    // No AC1: cursor.getInt(0) + " - " + cursor.getString(1) + ...
                    String item = c.getNome() + " | " +
                                  c.getTelefone() + " | " +
                                  c.getEmail() + " | " +
                                  c.getCategoria() +
                                  (c.isFavorito() ? " ⭐" : "");
                    listaExibicao.add(item);
                }

                // Notifica o adapter que os dados mudaram → atualiza a ListView
                // No AC1 isso era feito recriando o adapter
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao carregar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    // ============================================================
    // CARREGAR CONTATO PARA EDIÇÃO (ao clicar na lista)
    // AC1: buscava no banco por ID com rawQuery
    // AC2: os dados já estão na lista listaDeContatos em memória,
    //      basta pegar pelo índice da posição clicada
    // ============================================================
    private void carregarContato(int posicao) {
        // Pega o objeto Contato da lista pelo índice
        Contato c = listaDeContatos.get(posicao);

        // Guarda o ID do Firestore para usar no UPDATE
        // AC1: idContatoSelecionado = Integer.parseInt(item.split(" - ")[0]);
        // AC2: idContatoSelecionado = c.getId(); (já é String)
        idContatoSelecionado = c.getId();

        // Preenche os campos do formulário
        Nome.setText(c.getNome());
        Telefone.setText(c.getTelefone());
        Email.setText(c.getEmail());
        Cidade.setText(c.getCidade());
        Favorito.setChecked(c.isFavorito());

        // Seleciona a categoria correta no Spinner
        ArrayAdapter<CharSequence> categoriaAdapter = (ArrayAdapter<CharSequence>) Categoria.getAdapter();
        int pos = categoriaAdapter.getPosition(c.getCategoria());
        if (pos >= 0) Categoria.setSelection(pos);
    }

    // ============================================================
    // EXCLUIR CONTATO (ao segurar na lista)
    // AC1: banco.execSQL("DELETE FROM contatos WHERE id=?", ...)
    // AC2: db.collection("contatos").document(id).delete()
    // ============================================================
    private void excluirContato(int posicao) {
        Contato c = listaDeContatos.get(posicao);

        // .document(id) aponta para o documento específico
        // .delete() remove o documento
        db.collection("contatos").document(c.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Contato excluído!", Toast.LENGTH_SHORT).show();
                listarContatos(); // Atualiza a lista
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    // ============================================================
    // LIMPAR CAMPOS (igual à AC1, mas idContatoSelecionado vira null)
    // ============================================================
    private void limparCampos() {
        Nome.setText("");
        Telefone.setText("");
        Email.setText("");
        Cidade.setText("");
        Favorito.setChecked(false);
        idContatoSelecionado = null; // AC1 era: idContatoSelecionado = -1;
    }
}
