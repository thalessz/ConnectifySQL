# ConnectifySQL

Este repositório contém uma aplicação Android para gerenciar operações de banco de dados via API. A aplicação realiza consultas SQL e exibe os resultados em uma interface gráfica.

## Estrutura do Projeto

### MainActivity.java

A classe `MainActivity` é a atividade principal da aplicação. Ela inicializa a interface gráfica e executa uma consulta SQL ao iniciar.

#### Funções Principais

- **onCreate**: Inicializa a atividade e executa a consulta SQL "SELECT NOME FROM PESSOA".

### DatabaseManager.java

A classe `DatabaseManager` gerencia operações de banco de dados via API. Ele se comunica com a API para executar consultas SQL e armazena os resultados em uma lista de pares chave-valor.

#### Funções Principais

- **DatabaseManager(TextView txtResult)**: Construtor que inicializa a referência ao `TextView` e a lista de resultados.
- **setApiUrl(String url)**: Define a URL da API.
- **execute(String query)**: Executa uma consulta SQL.
- **fetchAll()**: Retorna todos os resultados da consulta.
- **getValueAt(int index)**: Retorna o valor no índice especificado na lista de resultados.

#### Classe Interna

- **QueryExecutorTask**: Executa consultas SQL em segundo plano usando `AsyncTask`.
  - **doInBackground(String... params)**: Executa a consulta SQL em segundo plano.
  - **onPostExecute(Boolean success)**: Exibe os resultados após a execução da consulta.

### DatabaseListener.java

A interface `DatabaseListener` define métodos para retornar os resultados de consultas SQL e operações de inserção.

#### Funções Principais

- **onQueryResult(List<JsonObject> result)**: Retorna uma lista de objetos JSON com os resultados da consulta.
- **onInsertResult(boolean success)**: Indica o sucesso de uma inserção.

## API Necessária

Para que a aplicação Android funcione corretamente, é necessário configurar uma API que atenda às seguintes especificações:

### Estrutura da API

A API deve ser construída usando Flask e Flask-RESTful, com suporte a consultas em bancos de dados MySQL e Firebird.

### Configuração da API

1. **Frameworks Necessários**:
   - Flask
   - Flask-RESTful
   - Flask-SQLAlchemy
   - python-dotenv

2. **Configuração do Banco de Dados**:
   - Configure a URL do banco de dados MySQL e Firebird usando variáveis de ambiente.
   - Utilize SQLAlchemy para gerenciar a conexão com os bancos de dados.

3. **Rotas da API**:
   - **POST /mysql/query**: Executa uma consulta SQL no banco de dados MySQL.
   - **POST /fdb/query**: Executa uma consulta SQL no banco de dados Firebird.

### Estrutura da Requisição

A requisição para os endpoints deve ser um JSON no seguinte formato:

```json
{
    "query": "SELECT * FROM tabela",
    "params": {}
}
```
-- **query**: A consulta SQL a ser executada.
-- **params**: Um dicionário de parâmetros pra a consulta (opcional).

### Estrutura da Reposta
A resposta da API será um JSON no seguinte formato:
```json
{
    "status": "success",
    "data": [
        {
            "coluna1": "valor1",
            "coluna2": "valor2"
        },
        ...
    ]
}
```
-- **status:** Indica o status da operação (Sucess ou Error)
-- **data:** Uma lista de objetos com os resultados da consulta
### Exemplo de resposta
```json
{
    "status": "success",
    "data": [
        {
            "nome": "João",
            "idade": 30
        },
        {
            "nome": "Maria",
            "idade": 25
        }
    ]
}
```
### Passos para Configurar a API
1. **Instale as dependências necessárias:**
   ```bash
       pip install flask flask-restful flask-sqlalchemy python-dotenv
   ```
2. **Configure as variáveis de ambiente:**
   Crie um arquivo '.env' na raiz do seu projeto e adicione as URLs de conexão do MySQL e Firebird:
    ```bash
       MYSQL_DATABASE_URI=mysql+pymysql://usuario:senha@localhost/nome_do_banco
       FIREBIRD_DATABASE_URI=firebird+fdb://usuario:senha@localhost/nome_do_banco
   ```
3. **Crie a aplicação Flask:**
   Configure a aplicação Flask para utilizar as variávei de ambiente e defina as rotas para as consultas.
