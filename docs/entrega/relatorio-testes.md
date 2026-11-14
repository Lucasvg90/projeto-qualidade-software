# Relatório de testes automatizados

**Data da execução:** 13/11/2026  
**Comando:** `.\mvnw.cmd -B clean verify`  
**Ambiente observado:** Windows, Java 23.0.1, Maven via wrapper. O projeto declara Java 17. MongoDB Docker estava indisponível durante esta execução limpa.

## Resultado

| Métrica | Resultado |
|---|---:|
| Build Maven | Sucesso |
| Testes executados | 21 |
| Aprovados | 21 |
| Falhas | 0 |
| Erros | 0 |
| Ignorados | 0 |
| Classes analisadas pelo JaCoCo | 52 |
| Cobertura de linhas | 41,12% (447/1.087) |
| Cobertura de branches | 18,04% (57/316) |

Relatório detalhado: `target/site/jacoco/index.html` e `target/site/jacoco/jacoco.xml`. O relatório do Maven fica em `target/surefire-reports/`; a CI publica ambos como artefato de execução.

## Escopo executado

- Testes Spring/MockMvc de página inicial, login, permissões e mensagens de erro.
- Testes de serviço para reservas, bloqueio de manutenção, retirada/devolução, notificação e relatório.
- Testes de controlador para traduzir conflito transitório de transação em HTTP 409/auditoria e não mascarar outros erros MongoDB.
- Inicialização do contexto Spring no perfil `test`.

## Interpretação

Build verde não significa conformidade integral. A cobertura está abaixo das metas do projeto (80% linhas / 70% branches). Não houve teste de integração automatizado com persistência Mongo, E2E de navegador ou WireMock nesta execução. O teste concorrente real foi executado separadamente por JMeter e está documentado em [resultado de carga](./resultado-teste-carga.md). As lacunas seguem abertas no [plano de testes](./plano-testes.md) e na [RTM](../RTM.md).

## Reprodutibilidade

1. Iniciar MongoDB local com `docker compose up -d --wait`.
2. Executar `.\mvnw.cmd -B clean verify`.
3. Abrir `target/site/jacoco/index.html` e conferir `target/surefire-reports/`.

O workflow `.github/workflows/ci.yml` executa os mesmos passos com JDK 17 e registra os relatórios como artefato. Não há execução remota registrada enquanto as alterações não forem enviadas ao GitHub.
