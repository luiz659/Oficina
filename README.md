# Oficina — ordens de serviço

API REST de uma **oficina mecânica**. O cliente deixa o carro, a oficina abre uma ordem de serviço (OS), um mecânico é atribuído e a OS termina concluída ou cancelada.

O projeto segue o passo a passo da atividade prática (camadas Controller → Service → Repository → Model, JPA e PostgreSQL). O domínio dos slides é uma assistência técnica (`Cliente`, `Tecnico`, equipamento). Aqui o mesmo desenho foi passado para a oficina: entram o **veículo** e o **mecânico**.

Caminho da OS:

`ABERTA` → `EM_ANDAMENTO` → `CONCLUIDA`

Antes de concluir, a OS também pode ir para `CANCELADA`. Depois de concluída ou cancelada, ela não muda mais.

## Stack

- Java 17
- Maven
- Spring Boot **4.1.2** (versão do material de criação do projeto)
- Spring Web MVC, Spring Data JPA e Validation
- PostgreSQL em `localhost:5432`, banco `oficinadb`, usuário e senha `postgres`

Pacote das classes: `com.oficina.os`.

## Como rodar

1. Crie o banco (uma vez):

```sql
CREATE DATABASE oficinadb;
```

O usuário da aplicação é o `postgres`, com senha `postgres`.

2. Confira `src/main/resources/application.properties`:

```properties
spring.application.name=oficina
spring.datasource.url=jdbc:postgresql://localhost:5432/oficinadb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
server.error.include-message=always
spring.web.error.include-message=always
```

No Spring Boot 4.1.2 a mensagem de erro sai no JSON pela propriedade `spring.web.error.include-message`. A linha `server.error.include-message` é a do slide; nesta versão ela não é mais lida, então as duas ficam no arquivo.

Com `ddl-auto=update` o Hibernate cria e atualiza as tabelas (`clientes`, `veiculos`, `mecanicos`, `ordens_servico`) ao subir a aplicação. Não é preciso rodar um script de tabelas.

3. Na raiz do projeto:

```bash
mvn spring-boot:run
```

A API fica em `http://localhost:8080`. Envie `Content-Type: application/json` nos POST e PUT que têm corpo. As mensagens de erro (400 e 404) voltam no JSON, no campo `message`.

Para só compilar:

```bash
mvn -q -DskipTests package
```

## Regras de negócio

Cada regra fica em um método, marcada com `// REGRA N`.

| Regra | Na oficina | Método |
|---|---|---|
| 1 | Não pode existir dois clientes com o mesmo CPF | `ClienteService.salvar` |
| 2 | A OS só abre para um veículo cadastrado, de um cliente da oficina | `validarVeiculoCadastrado` |
| 3 | Ao abrir, a OS recebe a data de abertura e o status `ABERTA` | `abrir` |
| 4 | OS `CONCLUIDA` ou `CANCELADA` não muda mais | `validarOsNaoFinalizada` |
| 5 | Ao atribuir um mecânico ativo, a OS passa para `EM_ANDAMENTO` | `atribuirMecanico` |
| 6 | Só conclui OS que tenha mecânico | `validarMecanicoAtribuido` |
| 7 | Ao concluir, grava valor, data de fechamento e status `CONCLUIDA` | `concluir` |
| 8 | Ao cancelar, grava data de fechamento e status `CANCELADA` | `cancelar` |
| 9 | Cliente com OS (por meio dos veículos) não pode ser excluído; veículo com OS também não | `ClienteService` e `VeiculoService` |
| 10 | O valor para concluir a OS tem que ser maior que zero | `validarValorConclusao`, chamado pelo `concluir` |

A Regra 5 também recusa mecânico inativo: o campo `ativo` existe para isso. A placa do veículo é única, no mesmo espírito do CPF, mas não é uma regra numerada.

No `PUT` do cliente o CPF não muda. No `PUT` do veículo a placa, a marca, o modelo e o ano mudam; o dono continua o mesmo.

## Endpoints

Base: `http://localhost:8080`.

### Clientes — `/clientes`

| Método | Caminho | Sucesso |
|---|---|---|
| GET | `/clientes` | 200 |
| GET | `/clientes/{id}` | 200 |
| POST | `/clientes` | 201 |
| PUT | `/clientes/{id}` | 200 |
| DELETE | `/clientes/{id}` | 204 |

```http
POST /clientes
Content-Type: application/json

{
  "nome": "Ana Ribeiro",
  "cpf": "123.456.789-00",
  "telefone": "(41) 99999-0000",
  "email": "ana@email.com"
}
```

### Veículos — `/veiculos`

| Método | Caminho | Sucesso |
|---|---|---|
| GET | `/veiculos` | 200 |
| GET | `/veiculos/{id}` | 200 |
| GET | `/veiculos/cliente/{clienteId}` | 200 |
| POST | `/veiculos` | 201 |
| PUT | `/veiculos/{id}` | 200 |
| DELETE | `/veiculos/{id}` | 204 |

```http
POST /veiculos
Content-Type: application/json

{
  "placa": "ABC1D23",
  "marca": "Fiat",
  "modelo": "Argo",
  "ano": 2020,
  "cliente": { "id": 1 }
}
```

A placa é gravada em maiúsculas, sem espaços nas pontas.

### Mecânicos — `/mecanicos`

| Método | Caminho | Sucesso |
|---|---|---|
| GET | `/mecanicos` | 200 |
| GET | `/mecanicos/{id}` | 200 |
| POST | `/mecanicos` | 201 |
| PUT | `/mecanicos/{id}` | 200 |
| DELETE | `/mecanicos/{id}` | 204 |

```http
POST /mecanicos
Content-Type: application/json

{
  "nome": "Carlos Lima",
  "especialidade": "Motor",
  "ativo": true
}
```

Se `ativo` não vier no POST, o mecânico entra ativo.

### Ordens de serviço — `/ordens`

Não há PUT nem DELETE genérico da OS. O estado muda pelos endpoints abaixo.

| Método | Caminho | O que faz | Regras |
|---|---|---|---|
| GET | `/ordens` | Lista todas | — |
| GET | `/ordens?status=ABERTA` | Filtra por status | — |
| GET | `/ordens/{id}` | Busca uma OS | — |
| GET | `/ordens/cliente/{clienteId}` | OS dos veículos daquele cliente | — |
| GET | `/ordens/veiculo/{veiculoId}` | OS de um veículo | — |
| GET | `/ordens/prioridade/{prioridade}` | OS da prioridade (`BAIXA`, `MEDIA`, `ALTA`) | — |
| POST | `/ordens` | Abre uma OS | 2 e 3 |
| PUT | `/ordens/{id}/mecanico/{mecanicoId}` | Atribui o mecânico | 4 e 5 |
| PUT | `/ordens/{id}/concluir?valor=350.00` | Conclui a OS | 4, 6, 7 e 10 |
| PUT | `/ordens/{id}/cancelar` | Cancela a OS | 4 e 8 |

```http
POST /ordens
Content-Type: application/json

{
  "descricaoProblema": "Motor falhando na lenta",
  "prioridade": "ALTA",
  "veiculo": { "id": 1 }
}
```

O veículo vai só com o id. A data de abertura e o status `ABERTA` são preenchidos pela API. A OS nasce sem mecânico, sem valor e sem data de fechamento.

`servicoExecutado` é opcional na abertura. Na conclusão também pode ser informado:

```http
PUT /ordens/1/concluir?valor=350.00&servicoExecutado=Troca%20de%20velas
```

## Sequência sugerida de testes

Rode nesta ordem. Os números seguem os testes 6.1 a 6.10 dos slides, já no domínio da oficina, e no final entram os exercícios do último slide.

1. `POST /clientes` com a Ana Ribeiro. Esperado: **201**.
2. `POST /clientes` de novo, com o mesmo CPF. Esperado: **400**, CPF já existe (Regra 1).
3. `POST /veiculos` com o Argo da Ana. Esperado: **201**.
4. `POST /veiculos` com a mesma placa. Esperado: **400**.
5. `POST /mecanicos` com o Carlos. Esperado: **201**.
6. `POST /ordens` com `"veiculo": { "id": 99 }`. Esperado: **404** (Regra 2).
7. `POST /ordens` com o veículo 1 e prioridade `ALTA`. Esperado: **201**, status `ABERTA` e `dataAbertura` preenchida (Regra 3).
8. `PUT /ordens/1/concluir?valor=350.00`. Esperado: **400**, falta mecânico (Regra 6).
9. `PUT /ordens/1/mecanico/1`. Esperado: **200**, status `EM_ANDAMENTO` (Regra 5).
10. `PUT /ordens/1/concluir?valor=0`. Esperado: **400**, valor tem que ser maior que zero (Regra 10).
11. `PUT /ordens/1/concluir?valor=350.00`. Esperado: **200**, `CONCLUIDA`, valor e `dataFechamento` (Regra 7).
12. `PUT /ordens/1/cancelar`. Esperado: **400**, OS já concluída (Regra 4).
13. `POST /ordens` de novo (abre a OS 2) e `PUT /ordens/2/cancelar`. Esperado: **200**, `CANCELADA` e data de fechamento (Regra 8).
14. `GET /ordens/prioridade/ALTA`. Esperado: **200**, as OS de prioridade alta.
15. `DELETE /clientes/1`. Esperado: **400**, cliente com OS não pode ser excluído (Regra 9).
16. `DELETE /veiculos/1`. Esperado: **400**, veículo com OS não pode ser excluído (Regra 9).
17. `GET /ordens?status=CANCELADA` e `GET /ordens/cliente/1` para conferir as consultas.

Um mecânico com `"ativo": false` não pode ser colocado numa OS: o `PUT /ordens/{id}/mecanico/{id}` responde **400**.
