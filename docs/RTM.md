# Matriz de rastreabilidade (RTM)

Base: requisitos do projeto [Organização de Recursos](https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html). A matriz relaciona cada requisito a riscos, testes e evidências observáveis. “Parcial” significa que há implementação, mas falta evidência automatizada/operacional suficiente; não equivale a aprovação.

## Requisitos funcionais

| ID | Requisito | Risco principal | Teste/evidência | Situação observada |
|---|---|---|---|---|
| RF-01 | Autenticação e autorização por perfil | Acesso indevido a dados e operações administrativas | `HomeControllerTest.apiRequiresAuthentication`, `HomeControllerTest.adminApiExplainsForbiddenAccessToOtherProfiles`; `SecurityConfig` | Parcial: testes de acesso API passam; matriz completa de papéis/rotas ainda não existe |
| RF-02 | Cadastro e consulta de salas, professores e materiais | Dados inválidos ou catálogo inconsistente | `TeacherControllerTest`; `EducationalResourceApiController`; `TeacherApiController`; `StudentApiController` | Parcial: testes focados de recursos e estudantes faltam |
| RF-03 | Pesquisa por tipo, capacidade, local, competência e disponibilidade | Recurso incompatível ou indisponível aparece como elegível | `EducationalResourceService.search`; `TeacherService`; `docs/api.md` | Parcial: documentação e código existem; cenários de filtros não estão cobertos de ponta a ponta |
| RF-04 | Criar, alterar e cancelar reservas | Perda/alteração indevida de reserva | `ReservationServiceTest`; `ReservationPageControllerTest`; `ReservationApiController` | Parcial: criação e autorização testadas; CRUD API completo precisa de testes |
| RF-05 | Detectar sobreposição por recurso e professor | Dupla alocação de sala, equipamento ou docente | `ReservationServiceTest.restrictedResourceRequiresApprovalAndLocksEveryMinuteForTeacherAndResource`; `ReservationSlot`; índice único no `DatabaseBootstrap` | Parcial: criação de slots coberta por unidade; colisão persistida precisa de teste de integração |
| RF-06 | Proteger contra solicitações simultâneas | Duas reservas aceitas para o mesmo slot | Índice único `reservation_slots`; `ReservationApiControllerTest`; três execuções JMeter documentadas em `docs/entrega/resultado-teste-carga.md` | Implementado e exercitado no MongoDB Docker: cada rodada teve 1 HTTP 201 e 19 HTTP 409; a execução de carga ainda não faz parte da CI |
| RF-07 | Aprovar recursos restritos | Uso de recurso restrito sem autorização | `ReservationServiceTest`; `AdminReservationController`; regras em `SecurityConfig` | Parcial: estado pendente testado; fluxo de aprovação HTTP e ator autorizado precisam de teste automatizado |
| RF-08 | Bloquear recursos em manutenção | Reserva/retirada de recurso indisponível | `ReservationServiceTest.resourceUnderMaintenanceCannotBeReserved`; `MaterialCheckoutServiceTest.preventsWithdrawalOfEquipmentUnderMaintenance` | Implementado; teste de reserva e retirada passa; consulta de disponibilidade deve ganhar teste |
| RF-09 | Registrar retirada e devolução | Extravio ou falta de responsável/data | `MaterialCheckoutServiceTest`; `MaterialCheckout`; endpoints em `AdminOperationsApiController` | Implementado; unidade e fluxo API isolado exercitados |
| RF-10 | Manter histórico auditável de mudanças | Alterações sem autoria ou rastreabilidade | `AuditLogService`; `AuditEvent`; `docs/entrega/checklist.md` | Parcial: eventos foram inspecionados no banco isolado; falta teste automatizado de persistência/consulta |
| RF-11 | Enviar notificação simulada ou integrar API externa | Evento importante não comunicado | `SimulatedNotificationServiceTest`; `NotificationEvent`; `NotificationSender` | Implementado como simulação persistida; sem integração externa |
| RF-12 | Relatar uso, horas e conflitos evitados | Métricas incorretas para decisão de alocação | `ResourceUsageReportServiceTest`; `ResourceUsageReport`; endpoint administrativo | Implementado; unidade e resposta de API isolada conferidas |
| RF-13 | Interface responsiva e erros compreensíveis | Uso inacessível em celular ou falha sem orientação | `WebErrorControllerAdviceTest`; `HomeControllerTest`; `layout.html`; `error.html` | Parcial: erros e viewport declarados/testados; inspeção visual em breakpoints ainda pendente |
| RF-14 | Documentar API ou fluxos públicos | Integração e operação ambíguas | `docs/api.md`; `README.md` | Implementado; revisão de atualização por release continua necessária |

## Requisitos de qualidade e entregáveis

| ID | Requisito | Risco principal | Teste/evidência | Situação observada |
|---|---|---|---|---|
| RQ-01 | Testes unitários, integração, API caixa-preta e E2E | Regressões não detectadas | `docs/entrega/plano-testes.md`; suíte `src/test/java` | Parcial: 21 testes unitários/MockMvc passam; integração com persistência e E2E ainda não estão automatizados |
| RQ-02 | Banco realista com Testcontainers e WireMock | Diferença entre mocks e serviços reais | `compose.yaml`; `docs/entrega/plano-testes.md` | Pendente: CI usa MongoDB containerizado; Testcontainers e WireMock não estão configurados |
| RQ-03 | Concorrência e proteção contra dupla reserva | Corrida de gravação | `ReservationSlot`; `docs/entrega/jmeter-reservas.jmx`; relatório de três execuções | Parcial: teste de carga manual contra MongoDB realista passou; falta automatizar a execução de carga no pipeline |
| RQ-04 | TDD/BDD com evidência | Regras sem cenário de aceitação | Histórico Git e testes associados | Pendente: não há registro verificável de ciclo TDD/BDD nesta entrega |
| RQ-05 | CI em pull requests | Defeitos chegam à branch principal | `.github/workflows/ci.yml` | Implementado no repositório de trabalho; execução remota depende de push |
| RQ-06 | JaCoCo 80% linhas / 70% branches | Cobertura insuficiente em regras críticas | `target/site/jacoco/`; relatório em `docs/entrega/relatorio-testes.md` | Não atingido: 41,12% linhas e 18,04% branches na execução registrada |
| RQ-07 | SonarCloud integrado | Bugs e hotspots sem análise contínua | Job opcional no workflow CI | Configuração pronta; pendente token, organização, chave do projeto e execução remota |
| RQ-08 | JMeter: plano e execução de carga | Degradação sob uso simultâneo | `docs/entrega/plano-teste-carga.md`; `docs/entrega/resultado-teste-carga.md` | Plano documentado; resultado válido depende de execução em ambiente com JMeter |
| RQ-09 | Autorização, validação e erros seguros | Escalada de privilégios ou vazamento de detalhes | `SecurityConfig`; `ApiExceptionHandler`; `WebErrorControllerAdviceTest` | Parcial: respostas de autenticação/autorização testadas; falta matriz completa de segurança |
| RQ-10 | Revisão por pares e histórico de entrega | Alteração sem avaliação independente | Histórico Git e PR #1 | Histórico existe; revisão humana aprovada ainda não comprovada |

## Critérios de encerramento

- Atualizar “Situação observada” somente com evidência reproduzível anexada ou ligada.
- Não marcar como concluído requisito cujo único suporte seja inspeção visual do código.
- Para RF-06/RQ-03, exigir teste concorrente real contra MongoDB replica set e exatamente uma criação aceita.
- Para RQ-06, exigir ao menos 80% de linhas e 70% de branches; a execução atual está abaixo das metas.
- Para revisão por pares, vincular PR integrado com aprovação humana registrada.
