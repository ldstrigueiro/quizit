package com.example.quizit.quizit.com.quizit.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.quizit.quizit.com.quizit.objetos.Aluno;
import com.example.quizit.quizit.com.quizit.util.Network;
import com.example.quizit.quizit.R;
import com.example.quizit.quizit.com.quizit.util.Util;
import com.example.quizit.quizit.com.quizit.util.Validator;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity implements View.OnClickListener {

    //=============== Variáveis Globais =================
    private TextView txtForgot;
    private EditText edtMatricula;
    private EditText edtSenha;
    private TextView txtCadastrar;
    private Intent intent;
    private Button btnLogar;
    private String endereco;

    private JSONTaskGet jsonTaskGet = new JSONTaskGet();
    private Aluno aluno;
    private Validator validator = new Validator();
    private AlertDialog.Builder dlg;
    private Util util = new Util();

    private SharedPreferences sp;

    private String url = "http://apitccapp.azurewebsites.net/Aluno/autenticaAluno/";



    //============ onCreate & onClick ===============
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCadastrar = (TextView) findViewById(R.id.txtCadastrar);
        btnLogar = (Button) findViewById(R.id.btnLogar);
        txtForgot = (TextView) findViewById(R.id.txtForgot);

        txtCadastrar.setOnClickListener(this);
        btnLogar.setOnClickListener(this);
        txtForgot.setOnClickListener(this);

        //Armazena os valores para resgatar e logar automaticamente na próxima tentativa
        sp = getSharedPreferences("login",MODE_PRIVATE);

        if(sp.getBoolean("logged", false)){
            //String matricula = sp.getString("matricula", "");
            jsonTaskGet.execute(url+sp.getString("matricula", "")+"/"+sp.getString("senha", ""));
        }

    }

    //@Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.txtCadastrar:
                intent = new Intent(this, CadastroActivity.class);
                startActivity(intent);
                break;
            case R.id.btnLogar:
                edtMatricula = (EditText) findViewById(R.id.edtLogin);
                edtSenha = (EditText) findViewById(R.id.edtSenha);

                //se os campos não estiveres vazios ele entra...
                if(!validaCampo(edtMatricula.getText().toString(), edtSenha.getText().toString())){
                    endereco = url+edtMatricula.getText().toString().toUpperCase()+"/"+edtSenha.getText().toString();
                    jsonTaskGet = new JSONTaskGet();
                    jsonTaskGet.execute(endereco);
                }
                   break;
            case R.id.txtForgot:
                intent = new Intent(this, ForgotPassActivity.class);
                startActivity(intent);
                break;
        }
    }

    //================== MÉTODOS ==================

    //Sintese: Popula o aluno a partir do JSON
    //Entrada:  Json
    //Saída: Aluno populado
    private Aluno getAlunoJson (String json){

        Aluno aluno = new Aluno();
        try {
            JSONObject jsonObj = new JSONObject(json);

            aluno.setIdAluno(jsonObj.getInt("idAluno"));
            aluno.setNome(jsonObj.getString("nome"));
            aluno.setEmail(jsonObj.getString("email"));
            aluno.setSemestre(jsonObj.getInt("semestre"));
            aluno.setSexo(jsonObj.getString("sexo"));
            aluno.setCurso(jsonObj.getString("curso"));
            aluno.setSenha(jsonObj.getString("senha"));
            aluno.setMatricula(jsonObj.getString("matricula"));
            aluno.setAvatar(jsonObj.getInt("avatar"));

            return aluno;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }




    //Objetivo: Validar campos de matricula e senha da página de login
    private boolean validaCampo(String matricula, String senha){
        boolean res;
        dlg = new AlertDialog.Builder(this);


        if(res = validator.isCampoVazio(matricula)) {
            edtMatricula.requestFocus();
            util.mensagem("Opa!", "Campo matrícula vazio!", "Ok", dlg);
        }else
            if(res = validator.isCampoVazio(senha)) {
                edtSenha.requestFocus();
                util.mensagem("Opa!", "Campo senha vazio!", "Ok", dlg);
            }

        return res;
    }

    //============= JSON TASKS ===============





    private class JSONTaskGet extends AsyncTask<String, String, String>{


        ProgressDialog progressDialog;

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "Aguarde", "Verificando Credenciais");

        }

        @Override
        protected String doInBackground(String... params) {
            return Network.httpGet(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            //Validar S == null
            if (s != null){


                aluno = getAlunoJson(s);

                if(aluno == null){ //Valida se o aluno é nulo e trata ele com o aviso antes de passar pra HomeActivity
                    dlg = new AlertDialog.Builder(LoginActivity.this);
                    util.mensagem("Opa!", "Matricula/Senha não cadastrados", "Ok", dlg);
                    progressDialog.dismiss();
                }else{
                    goToHomeActivity();
                    //Seta o sharedPreferences para true
                    sp.edit().putBoolean("logged",true).apply();
                    sp.edit().putString("matricula", aluno.getMatricula()).apply();
                    sp.edit().putString("senha", aluno.getSenha()).apply();
                }
            }else{
                dlg = new AlertDialog.Builder(LoginActivity.this);
                util.mensagem("Opa!", "ERRO AO ESTABELECER CONEXAO", "Tente novamente", dlg);
                progressDialog.dismiss();
            }
        }

        public void goToHomeActivity(){
            intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("ObjAluno", aluno);
            progressDialog.dismiss();
            startActivity(intent);
            finish();
        }
    }
}
