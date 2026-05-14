# GUIA DE ESTUDO - AC2: SQLite → Firebase Firestore
## Desenvolvimento de Apps Mobile - Facens

---

## PASSO A PASSO PARA CONFIGURAR O FIREBASE NO PROJETO

### 1. No Console do Firebase (firebase.google.com)
- Criar projeto → nomear → continuar
- Na tela do projeto: clicar no ícone Android
- Registrar app com o **package name** do seu projeto (ex: `com.example.ac1`)
- Baixar o **google-services.json**
- Colocar o `google-services.json` na pasta `app/` do projeto

### 2. No Android Studio - build.gradle raiz (nível do projeto)
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

### 3. No Android Studio - build.gradle do app (nível do módulo)
```kotlin
plugins {
    id("com.google.gms.google-services") // adicionar
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
}
```

### 4. AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 5. No Console Firebase - Firestore
- Criar banco de dados Firestore
- Nas regras de segurança, para testes, usar:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

---

## TABELA COMPARATIVA: SQLite (AC1) vs Firestore (AC2)

| Operação       | SQLite (AC1)                                      | Firebase Firestore (AC2)                              |
|----------------|---------------------------------------------------|-------------------------------------------------------|
| Inicializar    | `new DatabaseHelper(this)`                        | `FirebaseFirestore.getInstance()`                     |
| INSERT         | `banco.execSQL("INSERT INTO...")`                 | `db.collection("x").add(objeto)`                      |
| UPDATE         | `banco.execSQL("UPDATE ... WHERE id=?")`          | `db.collection("x").document(id).set(objeto)`         |
| DELETE         | `banco.execSQL("DELETE ... WHERE id=?")`          | `db.collection("x").document(id).delete()`            |
| SELECT *       | `banco.rawQuery("SELECT * FROM...")`              | `db.collection("x").get()`                            |
| SELECT WHERE   | `rawQuery("...WHERE campo=?", new String[]{val})` | `db.collection("x").whereEqualTo("campo", val).get()` |
| Resultado      | Cursor (síncrono)                                 | QueryDocumentSnapshot (assíncrono, listener)          |
| ID             | `int` (AUTOINCREMENT)                             | `String` (gerado pelo Firebase)                       |
| Arquivo config | `DatabaseHelper.java`                             | `google-services.json` + `Contato.java`               |
| Permissão      | Não precisa                                       | `INTERNET` no Manifest                                |

---

## CONCEITOS IMPORTANTES PARA A PROVA

### Classe Modelo (Contato.java)
O Firestore precisa de uma classe Java com:
- ✅ **Construtor vazio** (sem parâmetros) - obrigatório!
- ✅ **Getters e Setters** para todos os campos
- ✅ O campo `id` guarda o ID do documento (não é salvo no Firestore, é o ID do doc)

### Assincronicidade
O Firestore é **assíncrono**. O resultado não vem imediatamente:
```java
// ERRADO (jeito SQLite):
List<Contato> lista = db.collection("contatos").get(); // isso não existe!

// CERTO (jeito Firestore):
db.collection("contatos").get()
    .addOnSuccessListener(querySnapshot -> {
        // O código de dentro do listener roda QUANDO os dados chegam
        for (QueryDocumentSnapshot doc : querySnapshot) {
            Contato c = doc.toObject(Contato.class);
        }
    });
```

### Estrutura do Firestore
```
Firestore Database
└── contatos (coleção = tabela)
    ├── 6pJiPFTDCUGEK9HxUL24 (documento = linha)
    │   ├── nome: "João"
    │   ├── telefone: "11999999999"
    │   ├── email: "joao@email.com"
    │   ├── categoria: "Família"
    │   ├── cidade: "São Paulo"
    │   └── favorito: true
    └── eM73ze1L2Kwpchf8ejgu (outro documento)
        ├── nome: "Maria"
        ...
```

### Métodos principais do Firestore

**Adicionar (INSERT):**
```java
db.collection("contatos").add(objeto)
    .addOnSuccessListener(docRef -> { /* sucesso */ })
    .addOnFailureListener(e -> { /* erro */ });
```

**Atualizar (UPDATE):**
```java
db.collection("contatos").document(id).set(objeto)
    .addOnSuccessListener(aVoid -> { /* sucesso */ });
```

**Excluir (DELETE):**
```java
db.collection("contatos").document(id).delete()
    .addOnSuccessListener(aVoid -> { /* sucesso */ });
```

**Buscar todos (SELECT *):**
```java
db.collection("contatos").get()
    .addOnSuccessListener(query -> {
        for (QueryDocumentSnapshot doc : query) {
            Contato c = doc.toObject(Contato.class);
            c.setId(doc.getId()); // importante!
        }
    });
```

**Buscar com filtro (SELECT WHERE):**
```java
db.collection("contatos")
    .whereEqualTo("categoria", "Família")
    .get()
    .addOnSuccessListener(query -> { ... });
```

### Firebase Authentication (extra - se cair na prova)

**Instanciar:**
```java
FirebaseAuth mAuth = FirebaseAuth.getInstance();
```

**Cadastrar usuário:**
```java
mAuth.createUserWithEmailAndPassword(email, senha)
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) { /* cadastrou */ }
        else { /* erro */ }
    });
```

**Fazer login:**
```java
mAuth.signInWithEmailAndPassword(email, senha)
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) { /* logou */ }
        else { /* erro */ }
    });
```

---

## ARQUIVOS ALTERADOS (resumo)

| Arquivo                         | O que mudou                                          |
|---------------------------------|------------------------------------------------------|
| `DatabaseHelper.java`           | ❌ REMOVIDO (não usa mais SQLite)                    |
| `Contato.java`                  | ✅ CRIADO (modelo para o Firestore)                  |
| `MainActivity.java`             | 🔄 ATUALIZADO (SQLite → Firestore)                   |
| `build.gradle` (app)            | 🔄 ATUALIZADO (adicionadas dependências Firebase)    |
| `build.gradle` (projeto)        | 🔄 ATUALIZADO (adicionado plugin google-services)    |
| `AndroidManifest.xml`           | 🔄 ATUALIZADO (permissão INTERNET)                   |
| `res/values/strings.xml`        | 🔄 ATUALIZADO (adicionado array filtro com "Todos")  |
| `res/layout/activity_main.xml`  | 🔄 ATUALIZADO (filtro usa novo array)                |
| `google-services.json`          | ✅ ADICIONADO (baixar do console Firebase)           |
