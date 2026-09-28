# Acessor de Investimentos - Serviço de Perfil

Aplica o questionário, guarda as respostas e calcula o perfil do investidor
(`CONSERVATIVE`, `MODERATE` ou `AGGRESSIVE`). Quando o perfil muda, publica o
evento `profile.updated` no RabbitMQ.

## Requisitos

- Java 25
- Maven
- Docker (PostgreSQL e RabbitMQ)

## Como rodar

Tudo com Docker Compose (serviço + PostgreSQL + RabbitMQ):

```bash
cp .env.example .env
docker compose up --build
```

Ou só a infraestrutura no Docker e a aplicação local:

```bash
docker compose up -d postgres rabbitmq
mvn spring-boot:run
```

O schema do banco é criado pelas migrations do Flyway em
`src/main/resources/db/migration`.

## Endpoints

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/profiles/questionnaire` | Responde o questionário (header `X-User-Id`) |
| PUT | `/profiles/questionnaire` | Atualiza as respostas (header `X-User-Id`) |
| GET | `/profiles/{userId}` | Perfil do investidor |
| GET | `/profiles/{userId}/questionnaire` | Respostas do questionário |

## Testes

```bash
mvn verify
```

- `unit/`: testes unitários dos services, publisher e controller
- `integration/`: fluxo completo com H2 (modo PostgreSQL) e RabbitMQ mockado

O relatório JaCoCo é gerado em `tests/`. O build exige pelo menos 80% de
cobertura de instruções no projeto e em cada classe do pacote `service`.
