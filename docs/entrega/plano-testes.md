# Plano de testes

## Objetivo e escopo

Validar regras de alocação, perfis, manutenção, movimentações de material, auditoria, notificações, relatórios, tratamento de erro e execução da aplicação. Os requisitos e riscos rastreados estão em [RTM](../RTM.md).

## Estratégia

| Nível | Ferramentas / abordagem | Cobertura esperada |
|---|---|---|
| Unidade | JUnit 5 e Mockito | Regras de reserva, autorização de domínio, manutenção, auditoria, notificações e cálculos de relatório |
| Integração | Spring Boot Test + MongoDB replica set | Persistência, transações, índices únicos, rollback e auditoria persistida |
| API caixa-preta | HTTP contra aplicação iniciada | Status, payload, autenticação, autorização, CSRF, erros Problem Details |
| Interface / E2E | Navegador em viewport desktop e móvel | Login, busca, reserva, aprovação e movimentação; mensagens e navegação |
| Concorrência / carga | JMeter contra API + MongoDB replica set | Submissões concorrentes para o mesmo recurso/horário; uma única reserva aceita |
| CI / cobertura | GitHub Actions, Maven, JaCoCo | `verify` em PR; meta 80% de linhas e 70% de branches |

## Pré-condições e dados

- JDK 17 e Docker com Docker Compose.
- Executar `docker compose up -d --wait` para iniciar MongoDB 7 como replica set local.
- Testes Maven usam o perfil `test` e banco `quality_test`; nunca apontar testes para uma base compartilhada/de produção.
- Testes API autenticados devem usar contas e recursos sintéticos isolados e remover seus dados ao terminar.
- Proibir credenciais reais em fixtures, logs e relatórios.

## Casos de aceitação prioritários

| ID | Cenário | Resultado esperado |
|---|---|---|
| T-01 | Recurso marcado em manutenção é reservado | HTTP 400; reserva ausente |
| T-02 | Recurso em manutenção é retirado | HTTP 400; retirada ausente |
| T-03 | Recurso disponível recebe reserva válida | Reserva criada e slots gravados |
| T-04 | Duas solicitações concorrentes disputam o mesmo recurso/minuto | Um sucesso; demais HTTP 409; uma única reserva ativa |
| T-05 | Reserva restrita é criada por solicitante | Estado pendente; só administrador decide |
| T-06 | Administrador registra retirada e devolução | Responsável e datas persistidos; duplicidade recusada |
| T-07 | Alteração de reserva/recurso e conflito | Ator, ação, entidade, instante e detalhes auditados |
| T-08 | Operação notificável | Evento persistido com canal `SIMULATED` e destinatário esperado |
| T-09 | Relatório por recurso | Contagem, horas aprovadas e conflitos conferem com fixture |
| T-10 | Usuário anônimo acessa API protegida | HTTP 401 Problem Details |
| T-11 | Perfil sem permissão acessa rota administrativa | HTTP 403 Problem Details |
| T-12 | Erro de validação/entrada | Mensagem compreensível; sem stack trace ou segredo |
| T-13 | UI em 375 px e desktop | Navegação acessível e tabelas sem quebra horizontal inesperada |

## Execução local

```powershell
docker compose up -d
docker compose ps
.\mvnw.cmd -B clean verify
```

Relatório JaCoCo: `target/site/jacoco/index.html`. Para desligar o Mongo sem apagar volume: `docker compose down`. A CI encerra o ambiente efêmero com `docker compose down -v`.

## Critérios de saída

- Suíte sem falhas e aplicação iniciando.
- Todos os casos críticos T-01 a T-12 automatizados; T-13 registrado com captura por viewport.
- T-04 executado em banco Mongo realista; mocks não contam como prova de concorrência/persistência.
- Meta do projeto: 80% linhas e 70% branches. A execução em 13/11/2026 ainda não atende (ver [relatório](./relatorio-testes.md)).
- Falhas abertas recebem ID, severidade, impacto, responsável, correção e teste de regressão.

## Limitações da suíte atual

No registro desta entrega, a suíte tem 21 testes e passa, mas usa principalmente testes unitários e MockMvc. Testcontainers, WireMock e E2E de navegador ainda precisam ser adicionados. A concorrência real foi exercitada manualmente por JMeter contra MongoDB em Docker; falta integrá-la à CI. As lacunas não devem ser apresentadas como cobertura concluída.
