# Registro de defeitos e pendências

Este registro distingue defeitos observados de trabalho ainda não demonstrado. “Corrigido no working tree” não significa integrado à branch principal; validar novamente após merge.

| ID | Prioridade | Defeito / risco | Evidência inicial | Correção/evidência atual | Situação |
|---|---|---|---|---|---|
| D-01 | P1 | Rota inicial `/` falhava ao renderizar Thymeleaf no checkout avaliado em 08/11/2026 | Registro histórico em `docs/entrega/checklist.md` | `HomeControllerTest` verifica HTTP 200 e renderização; execução API local respondeu HTTP 200 | Corrigido no working tree; requer CI/merge |
| D-02 | P1 | `verify` falhava ao injetar MockMvc no teste da página inicial | Registro histórico em `docs/entrega/checklist.md` | `@AutoConfigureMockMvc`; execução atual: 21 testes, zero falhas | Corrigido no working tree; requer CI/merge |
| D-03 | P1 | Ausência de proteção de CI em PR | GitHub Actions não tinha workflows nem execuções registradas | Workflow `.github/workflows/ci.yml` criado e validação local passa | Aguardando push e primeira execução remota |
| D-04 | P1 | Corrida concorrente não coberta por teste de integração na CI | Inspeção da suíte: testes de serviço usam Mockito; não há Testcontainers | Três rodadas JMeter, 20 threads cada, contra MongoDB Docker; 1 HTTP 201 + 19 HTTP 409 por rodada | Parcial; falta automatizar no pipeline e adicionar teste de integração persistente |
| D-05 | P2 | Cobertura abaixo das metas do projeto | JaCoCo em execução `clean verify`: 41,12% linhas e 18,04% branches | Relatório em `docs/entrega/relatorio-testes.md` | Aberto; elevar cobertura, priorizando regras de conflito/permissão |
| D-06 | P2 | Relatório SonarCloud não disponível | Sem execução de workflow/relatório no repositório | Job opcional criado; exige `SONAR_TOKEN`, `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY` | Aguardando configuração do mantenedor |
| D-07 | P2 | Revisão por pares não comprovada | PR #1 está aberto sem revisões aprovadas | Nenhuma aprovação humana fabricada; solicitar revisão registrada antes do merge | Aberto; depende da equipe |
| D-08 | P2 | Teste de carga ainda não automatizado/representativo de sustentação | Plano inicial pedia JMeter e réplica Mongo | JMeter 5.6.3 executado em três rajadas concorrentes; detalhes em `resultado-teste-carga.md` | Parcial; resultado de contenção concluído, mas sem teste sustentado com aquecimento/ambiente de produção |
| D-09 | P2 | Teste visual responsivo não executado | Checklist funcional registra viewport móvel pendente | `docs/diagrams.md` e testes API não substituem validação visual | Aberto; validar 375 px e desktop |
| D-10 | P2 | Integração real de persistência e WireMock ausentes da suíte | `pom.xml` não inclui Testcontainers nem WireMock; testes observados usam mocks/contexto | Mongo em Docker configurado para CI, mas sem teste de integração de domínio | Aberto |
| D-11 | P1 | Conflitos transacionais MongoDB eram retornados como HTTP 500 sob carga concorrente | Primeiro teste JMeter: 1 reserva aceita e 19 erros Mongo `TransientTransactionError`/`WriteConflict` | `ReservationApiController` agora traduz apenas falhas transitórias em HTTP 409 e registra conflitos; três rodadas posteriores tiveram 1 HTTP 201 e 19 HTTP 409 cada, sem outros status | Corrigido no working tree e verificado no banco isolado; aguarda CI/revisão/merge |

## Política de priorização

- **P1:** bloqueia confiança em entrega, execução ou integridade crítica; corrigir antes de release.
- **P2:** evidência de qualidade/robustez incompleta; resolver antes da aceitação final.
- Reclassificar após revisão, anexando link para issue/PR e teste de regressão.
