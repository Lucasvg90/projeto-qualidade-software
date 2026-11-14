# Resultado do teste de carga

**Execução:** 13/11/2026, horário local UTC-03  
**Ferramenta:** Apache JMeter 5.6.3  
**Alvo:** Spring Boot em `localhost:8080` + MongoDB 7 replica set de nó único em Docker  
**Dados:** banco MongoDB isolado `qualidade_load_20261113`, usuário e sala sintéticos; nenhuma base existente foi usada para gravação.

## Cenário executado

Três rodadas independentes. Cada uma autenticou 20 sessões, sincronizou 20 threads e enviou uma solicitação para o mesmo recurso e intervalo futuro. Os intervalos foram distintos entre rodadas. Resultado conferido tanto no HTTP quanto na persistência.

| Rodada | Requisições de reserva | HTTP 201 | HTTP 409 | Outros status | Asserções JMeter | Média | p50 | p95 | p99 / máxima |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 20 | 1 | 19 | 0 | 0 falhas | 132,2 ms | 132 ms | 145 ms | 150 ms |
| 2 | 20 | 1 | 19 | 0 | 0 falhas | 79,3 ms | 81 ms | 85 ms | 86 ms |
| 3 | 20 | 1 | 19 | 0 | 0 falhas | 76,8 ms | 79 ms | 85 ms | 85 ms |
| **Total** | **60** | **3** | **57** | **0** | **0 falhas** | — | — | — | — |

O JMeter considera respostas HTTP 409 como sampler “unsuccessful”; por isso o resumo padrão mostra 19 erros por rodada. Neste cenário, esses 409 são os conflitos esperados. Asserções de status não falharam e não houve 5xx nas três rodadas.

## Conferência do banco

No banco descartável, as três rodadas finais resultaram em três reservas distintas, cada uma com os slots correspondentes, e 57 eventos de conflito evitado. O banco também continha dados das tentativas de diagnóstico anteriores; considerando o histórico total do banco de teste, foram observadas seis reservas e 95 conflitos registrados. Nenhum dado foi escrito nos outros bancos.

## Defeito encontrado e correção

A primeira tentativa concorrente revelou que MongoDB pode devolver `TransientTransactionError`/`WriteConflict` durante inserção simultânea de slots. A aplicação retornava HTTP 500 aos perdedores, embora só uma reserva fosse persistida. A API foi ajustada para converter especificamente essa falha transitória em HTTP 409 e registrar o conflito na auditoria, sem mascarar outras falhas MongoDB. O novo cenário de carga passou nas três rodadas subsequentes; ver D-11 em [registro de defeitos](./registro-defeitos.md).

## Limitações e decisão

- Esta é uma prova de contenção/concorrência, não uma medição de capacidade sustentada. Não houve aquecimento de dois minutos, teste de longa duração, múltiplas máquinas ou cluster MongoDB de produção.
- O p95 observado é válido apenas para este host local, massa mínima e perfil de carga; não é SLO nem capacidade de produção.
- Os JTLs brutos foram gerados em `target/` (diretório ignorado pelo Git); os resultados reprodutíveis são sumarizados aqui e o JMX versionado está em `jmeter-reservas.jmx`.
- Resultado funcional do requisito de concorrência: **aprovado para o cenário testado**. Automatização no CI e teste de carga sustentada continuam pendentes.
