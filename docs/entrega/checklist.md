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

## Escopo e método

- Avaliação externa realizada em 08/11/2026 sobre clone limpo do repositório, sem usar o `target/` ou cache Maven do checkout do desenvolvedor.
- Clone feito com `git clone --no-local` em diretório temporário. Revisão do branch padrão `main`, commit `d090289e7d6eb7a07d597d62c785ba6fa27ea256`.
- Branches de trabalho foram consultadas para identificar artefatos fora de `main`; itens nessas branches não foram considerados entregues no branch padrão.
- Nenhum código da aplicação foi alterado. Este checklist é o único arquivo criado no checkout selecionado.

## Entregáveis

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
