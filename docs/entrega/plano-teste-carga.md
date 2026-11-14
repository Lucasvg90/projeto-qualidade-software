# Plano de teste de carga

## Objetivo

Medir resposta e integridade do endpoint de criação de reserva sob concorrência, especialmente o requisito de não aceitar duas reservas do mesmo recurso no mesmo minuto. O objetivo não é declarar capacidade de produção sem um ambiente, massa e carga aprovados.

## Ferramenta e cenário

- Ferramenta-alvo: Apache JMeter 5.6.x, plano versionado em `docs/entrega/jmeter-reservas.jmx`.
- Ambiente: aplicação Spring Boot + MongoDB 7 replica set local de teste; nunca produção.
- Massa: um recurso sintético não restrito, uma janela futura comum, usuário de teste; banco dedicado e descartável.
- Carga: 20 threads simultâneas (o `Synchronizing Timer` também está configurado para 20), uma requisição por thread para `POST /api/reservations` com o mesmo recurso e intervalo. Ajustar ambos juntos se alterar a carga.
- Aquecimento: 2 minutos em carga baixa antes da medição.
- Repetições: 3 execuções independentes após limpeza da reserva e slots entre execuções.
- Requisição: autenticar cada thread, atualizar CSRF após login, manter sessão/token, disparar POST em rendezvous/barreira para maximizar a colisão.
- Aserções: um único HTTP 201; os demais HTTP 409; exatamente uma reserva e seus slots ativos; zero erro 5xx.
- Métricas: latência p50/p95/p99, throughput, taxa por status, erros e contagem persistida.
- Critério funcional: exatamente um vencedor por grupo de 20. Metas de latência devem ser acordadas antes de usar os resultados para capacidade.
- No JMeter, HTTP 409 é uma resposta esperada de concorrência, embora o sampler seja contado como “unsuccessful” no resumo padrão. Avaliar o resultado pelas contagens de status e pela asserção `201|409`, não pelo campo agregado `Err`.

## Execução planejada

```powershell
docker compose up -d --wait
 $env:LOAD_TEST_ADMIN_EMAIL = 'admin-de-teste@exemplo.local'
 $env:LOAD_TEST_ADMIN_PASSWORD = '<senha-de-teste>'
 @"
 adminEmail=$env:LOAD_TEST_ADMIN_EMAIL
 adminPassword=$env:LOAD_TEST_ADMIN_PASSWORD
 "@ | Set-Content -Encoding ascii target/jmeter-loadtest.properties
 jmeter -n -q target/jmeter-loadtest.properties -t docs/entrega/jmeter-reservas.jmx `
  -Jhost=localhost `
  -Jport=8080 `
  -Jthreads=20 `
  -JresourceId=<id-do-recurso-sintetico> `
  -JstartAt=2026-12-01T10:00:00Z `
  -JendAt=2026-12-01T11:00:00Z `
  -l target/jmeter-reservas.jtl `
  -e -o target/jmeter-report
```

Preparar previamente o usuário administrador e o recurso de teste; o arquivo de propriedades local fica em `target/` (ignorado pelo Git). Removê-lo após a execução. O teste extrai e atualiza CSRF após login e sincroniza as threads antes do POST. Não gravar senha real no JMX, linha de comando compartilhada, relatório ou Git. Trocar a janela do exemplo por um intervalo futuro, ainda livre, alinhado a minutos.

## Riscos e limites

- Usar threads da mesma sessão não reproduz usuários distintos; o plano deve manter cookie e CSRF por thread.
- Replica set em nó único valida o índice/transação, não disponibilidade de cluster.
- Máquina local, cache quente e base pequena não representam produção.
- Qualquer resposta 500, mais de uma reserva aceita ou divergência entre resposta e persistência bloqueia a aprovação.

