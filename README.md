# XY Inc - Sistema CRUD dinâmico para MongoDB

## Descrição da arquitetura

O projeto utiliza o conceito de descritor de entidade, que consiste em ter uma coleção com a descrição das configurações de cada coleção dinâmica, incluindo seus campos declarados, suas chaves primárias e qual seu campo sequencial, se existir. Esta coleção também utiliza a mesma estrutura das outras coleções, porém declarada dentro do serviço `EntityManagementService` como singleton.   
Como plataforma de desenvolvimento, foi utilizado _Java_ com _SpringBoot_, modulado como projeto _Maven_.

## Requisitos para uso
* MongoDB 3.4+
* Java 8 ou 9
* Maven 3+ (para compilar)

## Construir jar executável
`mvn clean package`

O arquivo jar poderá ser encontrado dentro da pasta `target`.

## Rodar testes
`mvn clean test`

## Linha de comando para execução

`java -jar dyndata-1.0.jar [--dburl=127.0.0.1] [--dbport=27017] [--dbname=dyndata]`

### --dburl
Host da instância do MongoDB
### --dbport
Porta da instância do MongoDB
### --dbname
Nome da Database na instância do MongoDB

## Objeto JSON de descritor de entidade
### Entidade
```
{
  "uriName": <String>,
  "entityName": <String>,
  "sequenceField": <String|null>,
  "keys": <String[]>,
  "fields": <Campos[]>
}
```
* *uriName*: URI da entidade a ser usado nas requisições de CRUD da entidade. Deve obedecer o padrão `/^[0-9a-zA-Z._]{1,100}$/`.
* *entityName*: Nome da entidade
* *sequenceField*: Nome do campo que receberá a numeração automática gerada ao criar um novo objeto da entidade
  * O campo deve ser `Integer` ou `Long`, e estar declarado no campo `fields`.
  * O campo não precisa necessariamente estar no array `keys`
* *keys*: Campos que serão a chave primária da entidade. Deve conter ao menos um campo.
  * Tipos de campos que podem ser chave: `Integer`, `Long`, `String`, `Boolean` ou `Timestamp`.
* *fields*: Lista de objetos que descrevem os campos, conforme abaixo. No mínimo, todos os campos declarados como chave ou sequencial devem ser declarados.

*Observação:* As entidades tem seus dados guardados em coleções que contém a URI da entidade precedido de `dyn.`.

### Campos
```
{
      "fieldName": <String>,
      "fieldType": <String>,
      "nullable": <Boolean>,
      "allowedValues": <String[]|null>,
      "min": <Decimal|null>,
      "max": <Decimal|null>,
      "minLength": <Integer|null>,
      "maxLength": <Integer|null>,
      "defaultValue": <String|null>,
      "documentFields": <Campos[]|null>
}
```
* *fieldName*: Nome do campo
* *fieldType*: Tipo do campo. Tipos disponíveis: 
  * `String`;
  * `Long`;
  * `Integer`;
  * `Timestamp`;
  * `Decimal`;
  * `Bool`;
  * `Document`;
  * `String[]`;
  * `Long[]`;
  * `Integer[]`;
  * `Timestamp[]`
  * `Decimal[]`;
  * `Bool[]`;
  * `Document[]`;
* *nullable*: Indica se o valor do campo pode ser nulo.
  * Valor padrão: `true`.
  * Ignorado nos casos de chaves (que nunca são anuláveis) e de campos sequenciais (são preenchidos com o próximo número da sequência).
* *allowedValues*: Array com valores String que são permitidos no campo. Válido apenas para campos tipo `String`.
* *min*: Valor mínimo aceito no campo. Válido apenas para `Integer`, `Decimal` e  `Long`.
* *max*: Valor máximo aceito no campo. Válido apenas para `Integer`, `Decimal` e  `Long`.
* *minLength*: Tamanho mínimo do valor do campo. Válido apenas para `String` e tipos array (`[]`).
* *maxLength*: Tamanho máximo do valor do campo. Válido apenas para `String` e tipos array (`[]`).
* *defaultValue*: Valor padrão do campo, em String. Ignorado para `Document` e tipos array (`[]`).
  * Nos campos não-string, tentará-se converter a String no tipo do objeto.
* *documentFields*: Descrição de objetos aninhados dentro do objeto da entidade. Utilizado apenas nos campos de tipo `Document` e `Document[]`.


## Serviços de gerenciamento de entidades
### `GET /configuration/entity`
#### Response
```
  <Entidade[]>
```
#### Status Code
* *200*: Sucesso
* *500*: Erro Interno

Retorna a lista com todas as entidades cadastradas.

### `GET /configuration/entity/{uriEntidade}`
#### Response
```
  <Entidade>
```
#### Status Code
* *200*: Sucesso
* *404*: Não encontrado
* *500*: Erro Interno

Retorna a entidade com a URI indicada.

### `POST /configuration/entity`
#### Request
```
  <Entidade>
```
#### Response
Sem Response
#### Status Code
* *201*: Sucesso
* *400*: Erro na valiação dos dados enviados
* *500*: Erro Interno

Cadastra uma nova entidade

### `PUT /configuration/entity/{uriEntidade}`
#### Request
```
  <Entidade>
```
#### Response
Sem Response
#### Status Code
* *204*: Sucesso
* *400*: Erro na valiação dos dados enviados
* *404*: Não encontrado
* *500*: Erro Interno

Edita a entidade.
#### Observações
* Não é possível alterar:
  * URI da entidade;
  * Campos chave - quais são e seus descritores de campo;
  * Campo sequencial - qual é e seu descritor de campo;
 
 
### `DELETE /configuration/entity/{uriEntidade}`
#### Request
```
  <Entidade>
```
#### Response
Sem Response
#### Status Code
* *204*: Sucesso
* *404*: Não encontrado
* *500*: Erro Interno

Apaga o descritor da entidade. Os dados da coleção que a entidade representa, porém, serão mantidos.


## Serviços de CRUD de entidades
### `GET /{uriEntidade}`
*Observações:*  `ObjEntidade` (nos requests e responses abaixo) representa os campos declarados conforme no descritor da entidade. Campos não declarados no descritor serão ignorados no caso de serem enviados nos métodos de criação ou edição.
#### Response
```
  <ObjEntidade[]>
```
#### Status Code
* *200*: Sucesso
* *500*: Erro Interno

Retorna a lista com todos os objetos inseridos nesta entidade.

### `GET /{uriEntidade}/{id}`
#### Response
```
  <ObjEntidade>
```
#### Status Code
* *200*: Sucesso
* *404*: Não encontrado
* *500*: Erro Interno

Retorna objeto da entidade com o id indicado.

*Observação:* Para entidades de chave composta `["chave1","chave2",...]`, envie as chaves no campo `{id}` na mesma ordem declarada, separando-os com `/` (`{chave1}/{chave2}/...`)

*Atenção!* Evite usar a URI `configuration` com chave primária única do tipo `String`: caso haja um objeto com a chave `entity`, esse objeto não será acessível neste método devido a sobreposição ao método `GET /configuration/entity`.

### `POST /{uriEntidade}`
#### Request
```
  <ObjEntidade>
```
#### Response
Sem Response
#### Status Code
* *201*: Sucesso
* *400*: Erro na valiação dos dados enviados
* *500*: Erro Interno

Cadastra uma novo objeto na entidade indicada.

### `PUT /{uriEntidade}/{id}`
#### Request
```
  <ObjEntidade>
```
#### Response
Sem Response
#### Status Code
* *204*: Sucesso
* *400*: Erro na valiação dos dados enviados
* *404*: Não encontrado
* *500*: Erro Interno

Edita o objeto da entidade indicada.
*Observações:* Não é possível alterar o valor das chaves do  objeto.
 
### `DELETE /{uriEntidade}/{id}`
#### Request
```
  <ObjEntidade>
```
#### Response
Sem Response
#### Status Code
* *204*: Sucesso
* *404*: Não encontrado
* *500*: Erro Interno

Apaga o objeto da entidade indicada.
