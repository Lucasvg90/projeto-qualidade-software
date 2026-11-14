# Relatório final de qualidade (preliminar)

**Data:** 13/11/2026  
**Escopo:** working tree da branch `fix/login`; não representa a branch `main` nem uma release integrada.

## Resumo executivo

A aplicação compila, a suíte automatizada atual passa e os fluxos centrais de reserva, manutenção, movimentação, auditoria, notificações simuladas e relatório estão implementados. A qualidade ainda **não está aprovada para entrega final**: cobertura está muito abaixo da meta, concorrência não tem teste automatizado de integração/carga, não existe execução remota de CI/SonarCloud, e revisão humana não foi registrada.

## Evidências

| Dimensão | Evidência | Avaliação |
|---|---|---|
| Compilação e testes | `mvnw clean verify`: 21 testes, 21 aprovados, zero falhas/erros | Verde local |
| Cobertura JaCoCo | 41,12% linhas; 18,04% branches | Não atende 80%/70% |
| Persistência / concorrência | MongoDB replica set no Compose; índice único de slots; três rodadas JMeter | 1 reserva aceita e 19 HTTP 409 em cada rodada; carga manual, não integrada à CI |
| Segurança | Spring Security por perfil; testes HTTP de 401/403 | Parcial; falta matriz de rotas e revisão independente |
| Documentação | README, API, RTM, diagramas, planos e registros | Presente no working tree; revisar links/entrega no merge |
| CI | Workflow GitHub Actions criado | Ainda sem execução remota |
| SonarCloud | Job condicionado à configuração do projeto | Sem relatório/resultado |
| Desempenho | Três rodadas de 20 threads simultâneas no JMeter | Integridade concorrente passou; teste não mede capacidade sustentada nem representa produção |
| Revisão por pares | PR #1 sem review aprovada registrada | Pendente |
| UI responsiva | Bootstrap e viewport configurados | Inspeção visual em dispositivo ainda pendente |

## Decisão

**Resultado: APROVAÇÃO CONDICIONAL / release bloqueada.** Não publicar como entrega final antes de:

1. Enviar as mudanças para branch/PR e obter revisão humana aprovada.
2. Executar CI remota e confirmar build verde em JDK 17.
3. Elevar cobertura até 80% linhas e 70% branches, sem excluir código relevante apenas para inflar o número.
4. Integrar o teste de concorrência/JMeter à CI e executar carga sustentada com aquecimento; anexar artefatos e resultado.
5. Configurar e revisar SonarCloud; corrigir bugs/vulnerabilidades críticas.
6. Completar integração de banco, WireMock, inspeção responsiva e testes E2E.
7. Atualizar este relatório com evidências da mesma revisão/commit de release.

## Limitações

Os resultados locais são de um working tree com alterações não commitadas e incluem dependências não verificadas em CI. Esta avaliação não constitui auditoria formal de segurança, teste de carga nem aprovação por pares. Ver [registro de defeitos](./registro-defeitos.md) e [RTM](../RTM.md).
