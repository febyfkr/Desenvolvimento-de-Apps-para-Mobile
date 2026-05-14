// ============================================================
// build.gradle (nível do APP) - arquivo: app/build.gradle.kts
// ============================================================
// ALTERAÇÕES EM RELAÇÃO À AC1:
//   1. Adicionado plugin: id("com.google.gms.google-services")
//   2. Adicionadas dependências do Firebase (BoM + Auth + Firestore)
//   3. REMOVIDA dependência do SQLite (não precisa de nada extra,
//      pois SQLite é nativo do Android)
// ============================================================

plugins {
    alias(libs.plugins.android.application)

    // NOVO: Plugin do Google Services (necessário para o Firebase funcionar)
    // Lê o arquivo google-services.json que você baixou do console Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ac1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ac1"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ============================================================
    // FIREBASE DEPENDENCIES - NOVAS LINHAS
    // ============================================================

    // Firebase BoM (Bill of Materials)
    // Gerencia as versões de TODAS as libs do Firebase automaticamente.
    // Com o BoM, você não precisa especificar versão individualmente.
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    // Firebase Authentication - para login/cadastro de usuários
    // (usado nas telas de login do projeto)
    implementation("com.google.firebase:firebase-auth")

    // Firebase Firestore - banco de dados na nuvem
    // Substitui o SQLite da AC1
    implementation("com.google.firebase:firebase-firestore")

    // ============================================================
    // NOTA: NÃO é necessário adicionar dependência para o SQLite,
    // pois ele é nativo do Android. O Firebase, por ser externo,
    // precisa ser declarado aqui.
    // ============================================================
}
