# Checklist de entrega — Organização de Recursos

## Requisitos funcionais obrigatórios (verificação em 13/11/2026)

Esta seção registra a verificação do código atual. A avaliação externa de 08/11/2026 abaixo é um registro histórico de outro checkout.

| Requisito | Status | Evidência |
|---|---|---|
| [x] Autenticação e autorização por perfil | Implementado: login com senha BCrypt e perfis `ADMIN`, `PROFESSOR` e `SOLICITANTE`; rotas e operações administrativas são protegidas. | `src/main/java/com/example/educationalqualityproject/config/SecurityConfig.java`; `service/UserDetailsServiceImpl.java`; `entity/UserRole.java` |
| [x] Cadastro e consulta de salas, professores e materiais | Implementado: salas e materiais são tipos de recurso; há cadastro, consulta, edição e remoção de recursos e professores. | `entity/ResourceType.java`; `controller/ResourcePageController.java`; `controller/TeacherController.java`; `controller/api/` |
| [x] Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Implementado: filtros de recursos incluem os cinco critérios; professores também podem ser pesquisados por competência e disponibilidade. | `service/EducationalResourceService.java`; `service/TeacherService.java`; `templates/resource/list.html` |
| [x] Criação, alteração e cancelamento de reservas | Implementado pela API e pela interface web; edição e cancelamento só são oferecidos para reservas pendentes ou aprovadas e a edição valida o solicitante. | `controller/api/ReservationApiController.java`; `controller/ReservationPageController.java`; `templates/reservation/list.html`; `service/ReservationService.java` |
| [x] Detecção de sobreposição de horários, inclusive na agenda do professor | Implementado com bloqueios por minuto para cada recurso e professor; reservas pendentes de aprovação também ocupam os horários. | `service/ReservationService.java`; `entity/ReservationSlot.java`; `config/DatabaseBootstrap.java` |
| [x] Proteção contra dupla reserva em solicitações simultâneas | Implementada com transações MongoDB e índice único por recurso/professor e minuto; uma colisão retorna conflito em vez de confirmar a reserva. Não houve teste de integração concorrente nesta rodada porque o MongoDB local não estava disponível. | `config/MongoTransactionConfig.java`; `config/DatabaseBootstrap.java`; `controller/api/ApiExceptionHandler.java`; `controller/ReservationPageController.java` |
| [x] Aprovação obrigatória para recursos restritos | Implementado: solicitação para recurso restrito fica pendente e só perfil administrador pode aprovar ou rejeitar. | `service/ReservationService.java`; `controller/AdminReservationController.java`; `config/SecurityConfig.java` |

## Requisitos funcionais adicionais (verificação em 13/11/2026)

| Requisito | Status | Evidência |
|---|---|---|
| [x] Bloqueio de recursos em manutenção | Implementado: estado editável no catálogo; novas reservas/retiradas são rejeitadas e a pesquisa por disponibilidade não lista o recurso. | `entity/EducationalResource.java`; `service/ReservationService.java`; `service/EducationalResourceService.java`; `service/MaterialCheckoutService.java` |
| [x] Registro de retirada e devolução de materiais e equipamentos | Implementado por API administrativa, com responsável e data/hora, restrito a reservas aprovadas. | `entity/MaterialCheckout.java`; `service/MaterialCheckoutService.java`; `controller/api/AdminOperationsApiController.java` |
| [x] Histórico auditável de mudanças | Implementado para contas, estudantes, professores, recursos, reservas, movimentações e conflitos; registra ator, ação, entidade, data/hora e detalhes. | `entity/AuditEvent.java`; `service/AuditLogService.java`; `controller/api/AdminAuditApiController.java` |
| [x] Notificação simulada ou integração com API externa | Implementado como notificações simuladas persistidas, sem chamada externa; eventos são consultáveis por administrador. | `entity/NotificationEvent.java`; `service/SimulatedNotificationService.java`; `controller/api/AdminAuditApiController.java` |
| [x] Relatório de utilização por recurso, carga horária alocada e conflitos evitados | Implementado por API administrativa; horas contam reservas aprovadas e conflitos contam colisões registradas por recurso. | `dto/ResourceUsageReport.java`; `service/ResourceUsageReportService.java`; `controller/api/AdminOperationsApiController.java` |
| [x] Interface responsiva e mensagens de erro compreensíveis | Bootstrap responsivo, tabelas com rolagem em telas estreitas, mensagem web localizada e respostas de erro da API em Problem Details. | `templates/layout.html`; `templates/error.html`; `controller/WebErrorControllerAdvice.java`; `controller/api/ApiExceptionHandler.java` |
| [x] Documentação de API ou dos fluxos públicos do sistema | Documentação de autenticação, permissões, endpoints, payloads, relatório e erros. | `docs/api.md`; `README.md` |

## Checklist de validação prática dos sete requisitos

Marque cada caixa ao repetir a verificação no ambiente de entrega. Os resultados abaixo registram apenas o que foi exercitado nesta rodada; verificações visuais e não concluídas continuam pendentes.

| Requisito | Verificação objetiva | Resultado em 13/11/2026 |
|---|---|---|
| Bloqueio em manutenção | [ ] Marcar um recurso como em manutenção; confirmar que `POST /api/reservations` é recusado e que `GET /api/resources?availableFrom=...&availableUntil=...` não o lista. | Reserva recusada com HTTP 400 no teste pela API. A ausência na pesquisa de disponibilidade não foi confirmada nesta rodada. |
| Retirada e devolução | [x] Para uma reserva aprovada, chamar `POST /api/admin/checkouts` e depois `POST /api/admin/checkouts/{id}/return`; conferir estado, responsável e horários. | Testado pela API no MongoDB isolado: retirada HTTP 201 e devolução HTTP 200, persistidas com estado `RETURNED` e responsáveis. |
| Histórico auditável | [x] Consultar `GET /api/admin/audit` após criar/alterar recurso, reservar, retirar/devolver e provocar conflito; verificar ator, ação, entidade, data e detalhes. | Eventos de criação/alteração, reserva, conflito e movimentações foram persistidos e consultados; o banco de teste registrou seis eventos. |
| Notificação simulada ou integração | [x] Executar uma operação notificável e consultar `GET /api/admin/notifications`; conferir destinatário, evento, mensagem e canal. | Teste unitário confirmou persistência no canal `SIMULATED`; a API e o banco isolado também registraram notificações. |
| Relatório de utilização | [x] Consultar `GET /api/admin/reports/resource-usage`; comparar reservas aprovadas, horas alocadas e conflitos evitados com os dados de teste. | API retornou 1,00 hora alocada e 1 conflito evitado para o recurso de teste; teste unitário focado também passou. |
| Responsividade e erros compreensíveis | [ ] Abrir as telas em viewport móvel e desktop; conferir navegação e tabelas sem quebra. Provocar erro de validação/permissão e ler a mensagem exibida. | A página inicial respondeu HTTP 200 e o bloqueio retornou HTTP 400. A conferência visual em viewport móvel permanece pendente. |
| Documentação de API/fluxos | [x] Conferir autenticação, permissões, endpoints, exemplos de payload, respostas/erros e os fluxos de manutenção e movimentação. | Conferido em `docs/api.md`; este checklist aponta verificações práticas e pendências. |

### Ambiente e escopo desta rodada

- O MongoDB do Docker respondeu ao ping. A aplicação foi iniciada com um banco de teste separado (`qualidade_software_checklist_20261113`); os bancos existentes não foram usados para gravação.
- Passaram cinco testes unitários focados em retirada/devolução, relatório e notificação: `MaterialCheckoutServiceTest`, `ResourceUsageReportServiceTest` e `SimulatedNotificationServiceTest`.
- O teste pela API cobriu manutenção, reserva, conflito, retirada/devolução, auditoria, notificação e relatório. Isso não substitui a validação visual responsiva nem a repetição em ambiente limpo de entrega.

## Escopo e método

- Avaliação externa realizada em 08/11/2026 sobre clone limpo do repositório, sem usar o `target/` ou cache Maven do checkout do desenvolvedor.
- Clone feito com `git clone --no-local` em diretório temporário. Revisão do branch padrão `main`, commit `d090289e7d6eb7a07d597d62c785ba6fa27ea256`.
- Branches de trabalho foram consultadas para identificar artefatos fora de `main`; itens nessas branches não foram considerados entregues no branch padrão.
- Nenhum código da aplicação foi alterado. Este checklist é o único arquivo criado no checkout selecionado.

## Pacote de entregáveis nesta execução (13/11/2026)

| Entregável | Estado | Evidência / pendência |
|---|---|---|
| Repositório com histórico de trabalho e revisão por pares | Parcial | Histórico Git e PR [#1](https://github.com/Lucasvg90/projeto-qualidade-software/pull/1) existem; a PR está aberta sem aprovação de revisão registrada. Alterações locais ainda não estão integradas. |
| Aplicação executável e instruções | Implementado no working tree | Instruções no `README.md`; `mvnw clean verify` passou e os testes de página validam `/`. Execução remota/merge pendentes. |
| README e documentação da API | Implementado | `README.md`; `docs/api.md`. |
| RTM requisito/risco/teste/evidência | Criado | `docs/RTM.md`; inclui pendências e critérios de encerramento. |
| Diagramas de contexto, componentes e sequências | Criado | `docs/diagrams.md`. |
| Plano e suíte automatizada | Parcial | `docs/entrega/plano-testes.md`; 21 testes passam; integração/Testcontainers, WireMock e E2E faltam. Concorrência foi testada manualmente com JMeter. |
| Relatório de testes | Criado | `docs/entrega/relatorio-testes.md`; JaCoCo 41,12% linhas / 18,04% branches, abaixo da meta. |
| Pipeline de integração contínua | Criado, ainda não executado remotamente | `.github/workflows/ci.yml`; exige push/PR para obter run e artefatos. |
| Relatórios SonarCloud e JaCoCo | JaCoCo local disponível; SonarCloud pendente | `target/site/jacoco/`; workflow Sonar requer secrets/vars e run remoto. |
| Plano e resultado de teste de carga | Executado em três rodadas locais; limitado | `docs/entrega/plano-teste-carga.md`, `jmeter-reservas.jmx` e `resultado-teste-carga.md`; contenção passou, carga sustentada ainda pendente. |
| Registro de defeitos | Criado | `docs/entrega/registro-defeitos.md`; itens abertos e correções locais diferenciados. |
| Relatório final de qualidade | Preliminar, aprovação bloqueada | `docs/entrega/relatorio-qualidade.md`. |
| Apresentação e demonstração | Roteiro criado | `docs/entrega/apresentacao.md`; preparar dados e capturas na equipe. |

## Inventário histórico de 08/11/2026

Os registros abaixo descrevem um clone/commit avaliado em 08/11; não representam automaticamente o estado atual do working tree.

| Item | Existe | Atualizado | Caminho / evidência |
|---|---|---|---|
| Repositório com revisão por pares | Parcial; há PRs, mas não foi comprovada revisão aprovada e integração dos entregáveis | Não comprovado; os PRs listados estavam abertos ou fechados sem merge | [Repositório e PRs](https://github.com/afonsolelis/qualidade_software/pulls); revisões consultadas em [PR #13](https://github.com/afonsolelis/qualidade_software/pull/13) e [PR #18](https://github.com/afonsolelis/qualidade_software/pull/18) sem registros |
| Aplicação executável | Parcial; o servidor sobe, mas `GET /` responde HTTP 500 | Não; há erro de template em execução | `src/main/java/` e `src/main/resources/templates/home.html`; detalhes em “Execução” |
| README | Sim | Não; contém instruções, mas inclui credencial de banco publicada e não documenta configuração segura/local | `README.md` |
| Documentação da API | Parcial; o README enumera endpoints, sem contratos, exemplos de payload/resposta ou códigos de erro | Não | Seção “API Endpoints” em `README.md` |
| Matriz de rastreabilidade (`docs/rtm.md`) | Não em `main`; existe `rtm.md` na branch `001-test-planning` | Não entregue em `main` e fora do caminho especificado | `rtm.md` em `origin/001-test-planning` |
| Diagrama de contexto | Não encontrado nas branches verificadas | Não aplicável | Ausente em `main` e nas branches de trabalho verificadas |
| Diagrama de componentes | Não encontrado nas branches verificadas | Não aplicável | Ausente em `main` e nas branches de trabalho verificadas |
| Diagramas de sequências críticas | Parcial; há diagramas de testes/API em branches não incorporadas | Não entregue em `main`; cobertura das sequências críticas do sistema não comprovada | `docs/e2e-sequence-diagrams.md` e `docs/uml/` em `origin/002-white-black-box` e branches posteriores |
| Plano de testes | Não encontrado nos branches verificados | Não aplicável | Ausente em `main`; `src/test/` contém testes, não um plano documentado |
| Relatório de testes | Não há relatório versionado de entrega | Não; o `verify` falhou | Relatórios temporários em `target/surefire-reports/` do clone de avaliação, não entregues no repositório |
| Pipeline de CI | Não em `main`; o arquivo existe em branches de trabalho | Não entregue em `main`; as execuções recentes consultadas eram de Pages, não de CI do projeto | `.github/workflows/ci.yml` em `origin/004-ci`, `origin/005-test-jest-in-ci` e `origin/006-vcr-cont` |
| Relatório SonarCloud | Não encontrado | Não aplicável | Ausente nos branches verificados |
| Relatório JaCoCo | Não encontrado | Não aplicável | Ausente nos branches verificados |
| Plano de teste de carga | Não encontrado | Não aplicável | Ausente nos branches verificados |
| Resultado de teste de carga | Não encontrado | Não aplicável | Ausente nos branches verificados |
| Registro de defeitos | Parcial; há issues abertos e documento de atividade em branch de trabalho, mas não há registro integrado em `main` | Não; os defeitos consultados permanecem abertos | [Issues abertas](https://github.com/afonsolelis/qualidade_software/issues); `docs/atividade-gestao-bugs.md` em `origin/003-gestao-bugs` e posteriores |
| Relatório final de qualidade | Não encontrado | Não aplicável | Ausente nos branches verificados |

## Execução e passos que faltaram ou falharam

1. **Clone limpo:** concluído com `git clone --no-local`; a validação ocorreu no commit indicado acima.
2. **Pré-requisitos do README:** Java 23 estava disponível, compatível com a exigência de Java 17 ou superior, mas `JAVA_HOME` não estava definido. O primeiro `mvnw.cmd -B verify` parou imediatamente com `JAVA_HOME not found`. Foi definido `JAVA_HOME` para o JDK instalado e repetido o comando.
3. **Comando POSIX no ambiente Windows:** não havia Bash disponível para executar literalmente `./mvnw -B verify`. Foi usado o wrapper Windows equivalente, `mvnw.cmd -B verify`.
4. **Isolamento:** `USERPROFILE`, `HOME` e o repositório Maven foram direcionados para diretório temporário. Como o README contém credencial de banco remoto, a avaliação não acessou esse serviço: `SPRING_DATA_MONGODB_URI` foi sobrescrita para um endereço local sem serviço MongoDB, evitando tráfego ou alterações em banco de terceiros.
5. **Aviso do wrapper:** o wrapper imprimiu que `.mvn/wrapper/maven-wrapper-distribution.properties` não foi encontrado, mas continuou a execução e baixou/rodou Maven.
6. **`verify`:** terminou em `BUILD FAILURE` no Surefire. Foram executados 2 testes: 1 passou e 1 teve erro. `HomeControllerTest.shouldReturnHomeView` não conseguiu injetar `MockMvc` (`No qualifying bean of type 'MockMvc'`). O log também registrou falha de conexão Mongo ao endereço local isolado.
7. **Subida conforme README:** foi executado `mvnw.cmd -B spring-boot:run`, equivalente Windows ao comando `./mvnw spring-boot:run` do README. Spring Boot e Tomcat iniciaram na porta 8080. Ao consultar `GET /`, a resposta foi HTTP 500: erro de parsing Thymeleaf em `home.html`, linha 2, coluna 53, porque a expressão `layout :: html(~{::#content})` não corresponde à assinatura do fragmento.
8. **Encerramento:** o processo iniciado para a avaliação foi encerrado após a consulta HTTP. Nenhuma correção foi aplicada à aplicação.

## Bloqueadores — ordem de prioridade

1. **Credencial publicada no README:** revogar/rotacionar a credencial exposta e removê-la do histórico/documentação; parametrizar o acesso sem segredo versionado.
2. **Entrega não passa no `verify`:** corrigir a configuração do teste MockMvc e fornecer dependência/configuração Mongo isolada para testes reproduzíveis; obter execução verde em ambiente limpo.
3. **Página principal indisponível:** corrigir a incompatibilidade entre o fragmento Thymeleaf de `home.html` e a assinatura em `layout.html`; validar a resposta HTTP 200 da rota documentada.
4. **Revisão por pares não comprovada:** concluir revisão registrada e integrar as mudanças aprovadas ao branch de entrega; há PRs abertos e revisões consultadas sem registros.
5. **Entregáveis em branches, não na entrega principal:** integrar e validar o RTM, diagramas, registro de defeitos e pipeline que existem apenas em branches de trabalho; o RTM ainda está fora do caminho `docs/rtm.md`.
6. **Documentação de qualidade ausente:** produzir e versionar plano e relatório de testes, relatórios SonarCloud/JaCoCo, plano e resultado de carga, diagramas de contexto/componentes e relatório final.
7. **Documentação da API incompleta:** acrescentar contratos, exemplos de requisição/resposta e códigos de status; a lista de endpoints do README não permite validar a integração de forma suficiente.
