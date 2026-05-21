package com.example.ac2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class LivroAdapter extends ArrayAdapter<Livro> {

    public LivroAdapter(Context context, List<Livro> livros) {
        super(context, 0, livros);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_livro, parent, false);
        }

        Livro livro = getItem(position);

        TextView txtTitulo = convertView.findViewById(R.id.txtTitulo);
        TextView txtAutor = convertView.findViewById(R.id.txtAutor);
        TextView txtGenero = convertView.findViewById(R.id.txtGenero);
        TextView txtAno = convertView.findViewById(R.id.txtAno);
        TextView txtStatus = convertView.findViewById(R.id.txtStatus);

        if (livro != null) {
            String estrela = livro.isFavorito() ? " ⭐" : "";

            txtTitulo.setText(livro.getTitulo() + estrela);
            txtAutor.setText("Autor: " + livro.getAutor());
            txtGenero.setText("Gênero: " + livro.getGenero());
            txtAno.setText("Ano: " + livro.getAno());
            txtStatus.setText("Status: " + livro.getStatus());
        }

        return convertView;
    }
}