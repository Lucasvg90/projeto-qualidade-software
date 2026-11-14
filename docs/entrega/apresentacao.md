# Apresentação e roteiro de demonstração

## Roteiro sugerido (8–10 minutos)

1. **Problema e objetivo (1 min):** organizar salas, professores e materiais sem sobreposição; perfis solicitante e administrador.
2. **Arquitetura (1 min):** apresentar [diagrama de contexto e componentes](../diagrams.md); Spring Boot, Thymeleaf/API e MongoDB replica set.
3. **Fluxo do solicitante (2 min):** entrar, pesquisar recurso/disponibilidade e criar uma reserva.
4. **Regra crítica (1 min):** mostrar recurso restrito aguardando aprovação; explicar slots por minuto e índice único contra dupla alocação.
5. **Operação administrativa (2 min):** marcar manutenção; bloquear reserva; aprovar recurso restrito; registrar retirada e devolução.
6. **Rastreabilidade (1 min):** consultar histórico auditável, notificações simuladas e relatório de uso.
7. **Qualidade e limites (1–2 min):** mostrar suíte/JaCoCo e CI; informar com transparência cobertura atual, carga pendente e revisão humana pendente.

## Preparação da demonstração

- Seguir a instalação no [README](../../README.md); iniciar MongoDB com Docker Compose.
- Criar conta admin por variáveis de ambiente e dados de demonstração descartáveis. Não projetar credenciais reais.
- Preparar uma sala comum, um recurso restrito e um equipamento; manter horários futuros e livres.
- Demonstrar uma tentativa de reserva de recurso em manutenção e observar mensagem compreensível.
- Demonstrar criação de reserva, decisão administrativa, retirada e devolução.
- Conferir registros em `/api/admin/audit`, `/api/admin/notifications` e `/api/admin/reports/resource-usage`.
- Ter plano alternativo: screenshots previamente capturadas e relatório local de testes; não alegar CI remoto ou SonarCloud sem execução. O resultado JMeter disponível cobre concorrência pontual, não capacidade sustentada.

## Evidências a capturar

- Tela responsiva em viewport móvel e desktop.
- Resposta de conflito HTTP 409 em cenário de colisão.
- Histórico com ator, ação e instante.
- Resumo JaCoCo e run CI quando disponível.
- Relatório JMeter somente após execução verdadeira.

## Perguntas para a banca

- Como o índice único protege contra duas gravações simultâneas?
- Quais estados ocupam os slots e quando são liberados?
- O que está coberto por teste automatizado e o que é apenas inspeção manual?
- Qual a diferença entre notificação simulada e integração externa?
- Quais riscos continuam abertos e qual a decisão de release?
