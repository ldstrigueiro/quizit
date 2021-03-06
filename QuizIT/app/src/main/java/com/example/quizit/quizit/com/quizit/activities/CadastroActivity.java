package com.example.quizit.quizit.com.quizit.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.quizit.quizit.com.quizit.util.Network;
import com.example.quizit.quizit.R;
import com.example.quizit.quizit.com.quizit.util.Util;
import com.example.quizit.quizit.com.quizit.util.Validator;

import org.json.JSONException;
import org.json.JSONObject;


public class CadastroActivity extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    //=============== Variáveis Globais =================
    private Button btn_Cadastrar;

    //Bloco dos campos de cadastro
    private EditText edt_Nome;
    private EditText edt_Email;
    private EditText edt_Senha;
    private EditText edt_Matricula;
    private EditText edt_Semestre;

    //Bloco de variaveis relacionadas ao spinner da opcao sexo.
    private Spinner spin_sexo;
    private String spin_valor;
    private String[] sexo = {"M", "F"};
    private String emailSemPonto;
    private String urlCadastro = "http://apitccapp.azurewebsites.net/api/Aluno";
    private String urlVerificaMatricula = "http://apitccapp.azurewebsites.net/Aluno/autenticaAlunoMatricula/";
    private String urlVerificaEmail = "http://apitccapp.azurewebsites.net/Aluno/autenticaAlunoEmail/";
    private int isMatriculaValida;
    private Validator validator = new Validator();
    private AlertDialog.Builder dlg;
    private Intent intent;
    private JSONTaskGet jsonTaskGet;
    private JSONTaskPost jsonTaskPost = new JSONTaskPost();

    Util util = new Util();


    //============ onCreate & onClick ===============
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //Spinner (dropdownlist) da opção sexo
        spin_sexo = findViewById(R.id.spinner_Sexo);
        spin_sexo.setOnItemSelectedListener(this);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sexo);
        spin_sexo.setAdapter(spinAdapter);


        //Botão cadastrar
        btn_Cadastrar = findViewById(R.id.btn_Cadastrar);
        btn_Cadastrar.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        edt_Nome = (EditText) findViewById(R.id.edt_Nome);
        edt_Email = (EditText) findViewById(R.id.edt_Email);
        edt_Senha = (EditText) findViewById(R.id.edt_Senha);
        edt_Matricula = (EditText) findViewById(R.id.edt_Matricula);
        edt_Semestre = (EditText) findViewById(R.id.edt_Semestre);

        //Validar os campos aqui
        if (!isCamposValidado(edt_Nome.getText().toString(), edt_Email.getText().toString(),
                edt_Senha.getText().toString(), edt_Matricula.getText().toString().toUpperCase(),
                edt_Semestre.getText().toString())) {


            //Tira "." e coloca "-" na string do email
            emailSemPonto = edt_Email.getText().toString().replace(".", "-");

            //Desativa o botao
            btn_Cadastrar.setEnabled(false);

            //Valida se a matricula ja existe e cadastra
            jsonTaskGet = new JSONTaskGet();
            jsonTaskGet.execute(urlVerificaEmail + emailSemPonto);

        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                spin_valor = sexo[0];
                break;
            case 1:
                spin_valor = sexo[1];
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }


    //=============== Métodos ================
    //Objetivo: Validar campos de matricula e senha da página de login
    private boolean isCamposValidado(String nome, String email, String senha, String matricula, String semestre) {
        boolean res;
        dlg = new AlertDialog.Builder(this);
        int semestreInt = strToInt(semestre, 0);


        if (res = validator.isCampoVazio(nome)) {
            btn_Cadastrar.setEnabled(true);
            edt_Nome.requestFocus();
            util.mensagem("Nome inválido!",
                    "O nome não pode ser vazio.",
                    "Ok", dlg);
        } else if (res = !validator.isEmailValido(email)) {
            btn_Cadastrar.setEnabled(true);
            edt_Email.requestFocus();
            util.mensagem("Email inválido!",
                    "Confira se o email inserido está correto.",
                    "Ok", dlg);
        } else if (res = validator.isCampoVazio(senha)) {
            btn_Cadastrar.setEnabled(true);
            edt_Senha.requestFocus();
            util.mensagem("Senha inválida!",
                    "A senha não pode ser vazia.",
                    "Ok", dlg);
        }else if(res = !validator.isValidPassword(senha.trim())){
            btn_Cadastrar.setEnabled(true);
            edt_Senha.requestFocus();
            util.mensagem("Senha inválida", "A senha deverá ser composta por letras maiuscula e minuscula, numeros e ter pelo menos 8 caracteres", "OK", dlg);

        } else if (res = (validator.isCampoVazio(matricula) || !validator.isPadraoMatricula(matricula))) {
            btn_Cadastrar.setEnabled(true);
            edt_Matricula.requestFocus();
            util.mensagem("Matrícula inválida!",
                    "A matrícula está vazia ou não está no padrão correto. Ex: UC12345678",
                    "Ok", dlg);
        } else if (res = (validator.isCampoVazio(semestre) || (semestreInt <= 0 || semestreInt > 8))) {
            btn_Cadastrar.setEnabled(true);
            edt_Semestre.requestFocus();
            util.mensagem("Semestre inválido!",
                    "O semestre tem que estár entre 1 e 8.",
                    "Ok", dlg);
        }
        return res;
    }

    public int strToInt(String valor, int padrao) {
        try {
            return Integer.valueOf(valor); // Para retornar um Integer, use Integer.parseInt
        } catch (NumberFormatException e) {  // Se houver erro na conversão, retorna o valor padrão
            return padrao;
        }
    }

    private boolean isOnline() {
        try {
            ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conn.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Erro ao verificar se estava online! (" + ex.getMessage() + ")", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //================== JSONTasks ===================
    private class JSONTaskPost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                //Popula o JSON com os dados dos campos ta tela de cadastro
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("matricula", edt_Matricula.getText().toString().toUpperCase());
                jsonObject.put("nome", edt_Nome.getText().toString());
                jsonObject.put("email", edt_Email.getText().toString());
                jsonObject.put("semestre", edt_Semestre.getText());
                jsonObject.put("sexo", spin_valor);
                jsonObject.put("senha", edt_Senha.getText().toString());
                jsonObject.put("curso", "Ciência da Computação");
                jsonObject.put("avatar", 0);

                Log.e("params", jsonObject.toString());

                //Obtem a conexao, transforma o JSON em URL e envia pro AZURE para popular o banco.
                return Network.httpPost(jsonObject, urlCadastro);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            boolean isCorrect = Boolean.parseBoolean(s);

            //Retorna false se matricula estiver repetida
            if(!isCorrect){
                edt_Matricula.requestFocus();
                util.mensagem("OPA!", "Matricula já cadastrada no sistema!", "Ok", dlg);
                btn_Cadastrar.setEnabled(true);
            }else{
                Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!!", Toast.LENGTH_LONG).show();
                //Popular aluno e passar como parametro para a activity home.
                finish();
            }
        }
    }


    private class JSONTaskGet extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CadastroActivity.this, "Aguarde", "Cadastrando...");
        }

        @Override
        protected String doInBackground(String... params) { //  Retorna o resultado do json passado pelo parametro params[0] em
            return Network.httpGet(params[0]); //  forma de string.
        }

        @Override
        protected void onPostExecute(String s) { //  String s = resultado do json. Se for nulo (sem conexão pra consumir o json)
            super.onPostExecute(s);
            //  ele retorna do doInBackground uma string null
            if (isOnline()) {
                if (s != null) {
                    boolean isEmailRepetido = Boolean.parseBoolean(s);

                    if (isEmailRepetido == true) {
                        edt_Email.requestFocus();
                        util.mensagem("OPA!", "Email já cadastrado no sistema!", "Ok", dlg);
                        btn_Cadastrar.setEnabled(true);
                    } else {
                        jsonTaskPost = new JSONTaskPost();
                        jsonTaskPost.execute(urlCadastro);

                    }
                } else {
                    Toast.makeText(CadastroActivity.this, "ERRO AO OBTER DADOS COM O SERVIDOR", Toast.LENGTH_LONG).show();
                    btn_Cadastrar.setEnabled(true);
                }
            } else {
                util.mensagem("Opa!",
                        "Parece que você está sem conexão...",
                        "Tentar novamente", dlg);
            }
            progressDialog.dismiss();
        }
    }
}



