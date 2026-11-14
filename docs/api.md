# API e fluxos do sistema

Base local: `http://localhost:8080`. Todos os horários da API usam ISO-8601 em UTC.

## Autenticação e permissões

A API usa a sessão do Spring Security: faça login em `POST /login` com os campos `username` (e-mail) e `password`. As chamadas seguintes devem reutilizar o cookie de sessão. Requisições de escrita também precisam enviar o token CSRF da sessão.

| Perfil | Permissões principais |
|---|---|
| `SOLICITANTE` | Consultar catálogo e gerenciar as próprias reservas |
| `PROFESSOR` | Consultar catálogo e gerenciar as próprias reservas |
| `ADMIN` | Gerenciar catálogo e contas, aprovar reservas e acessar operações administrativas |

Todas as rotas `/api/**` exigem autenticação. `/api/admin/**` e as operações de escrita do catálogo exigem `ADMIN`.

## Catálogo de recursos

### Pesquisar e consultar

- `GET /api/resources?type=SALA&capacity=20&location=bloco&competence=multimidia&availableFrom=2026-12-01T10:00:00Z&availableUntil=2026-12-01T11:00:00Z`
- `GET /api/resources/{id}`

Os filtros são opcionais e combináveis. `type` aceita `SALA`, `MATERIAL` e `EQUIPAMENTO`; `capacity` representa capacidade mínima. Na consulta de disponibilidade, recursos em manutenção ou ocupados por uma reserva pendente/aprovada não são retornados. A consulta sem período ainda pode exibir recursos em manutenção, com seu estado indicado.

### Criar, alterar e excluir

- `POST /api/resources` — administrador; retorna `201 Created`.
- `PUT /api/resources/{id}` — administrador.
- `DELETE /api/resources/{id}` — administrador; retorna `204 No Content`.

Exemplo de corpo:

```json
{
  "name": "Projetor móvel",
  "type": "EQUIPAMENTO",
  "location": "Bloco A",
  "capacity": 1,
  "description": "Projetor da sala multimídia",
  "competencies": ["multimídia"],
  "restricted": false,
  "underMaintenance": false
}
```

Defina `underMaintenance` como `true` para impedir novas reservas e retiradas. O cadastro e cada alteração ficam no histórico de auditoria.

## Reservas

- `GET /api/reservations` — solicitante/professor recebe as próprias reservas; administrador recebe todas.
- `POST /api/reservations` — cria reserva.
- `PUT /api/reservations/{id}` — altera uma reserva própria ou, como administrador, qualquer reserva editável.
- `DELETE /api/reservations/{id}` — cancela uma reserva própria ou, como administrador, qualquer reserva ainda aberta.
- `POST /api/reservations/{id}/approve` — administrador; parâmetro opcional `note`.
- `POST /api/reservations/{id}/reject` — administrador; parâmetro opcional `note`.

Exemplo de corpo para criar/alterar:

```json
{
  "resourceIds": ["<id-da-sala>", "<id-do-material>"],
  "teacherId": "<id-do-professor>",
  "startAt": "2026-12-01T10:00:00Z",
  "endAt": "2026-12-01T11:00:00Z"
}
```

O intervalo deve ser futuro, positivo, de até 24 horas e alinhado a minutos inteiros. Reservas com recurso restrito ficam `PENDING_APPROVAL`; as demais ficam `APPROVED`. Reservas canceladas/rejeitadas não podem ser alteradas. Conflitos de horário de recurso ou professor são impedidos pelo índice único de ocupação.

## Retirada e devolução de materiais/equipamentos

Todas as rotas abaixo são administrativas:

- `GET /api/admin/checkouts` — histórico de retiradas e devoluções.
- `POST /api/admin/checkouts` — registra retirada; exige reserva aprovada e recurso da reserva do tipo `MATERIAL` ou `EQUIPAMENTO`.
- `POST /api/admin/checkouts/{id}/return` — registra a devolução.

Corpo para registrar retirada:

```json
{
  "reservationId": "<id-da-reserva-aprovada>",
  "resourceId": "<id-do-material-ou-equipamento>"
}
```

O sistema guarda responsável e horário da retirada/devolução, impede registrar a mesma retirada mais de uma vez e notifica o solicitante por meio simulado.

## Auditoria, notificações e relatório

- `GET /api/admin/audit?entityType=RESERVATION&entityId=<id>` — histórico ordenado do mais recente para o mais antigo. Sem filtros retorna o histórico completo.
- `GET /api/admin/notifications` — notificações simuladas persistidas, com canal `SIMULATED`.
- `GET /api/notifications` — notificações simuladas do próprio usuário autenticado.
- `GET /api/admin/reports/resource-usage` — totais de reservas aprovadas e horas alocadas por recurso, além de conflitos de horário evitados.

O relatório considera a vida inteira dos dados armazenados, conta somente reservas `APPROVED` para horas alocadas e conta uma colisão de gravação por recurso no campo `preventedConflicts`. Reservas pendentes ocupam horários, mas ainda não entram nas horas alocadas.

Alterações de contas, estudantes, professores, recursos, reservas e movimentações de materiais/equipamentos registram ator, ação, entidade, data/hora e detalhes antes/depois quando aplicável. Eventos de conflito também entram na auditoria e no relatório.

## Professores, estudantes e contas

- `GET /api/teachers?competence=matemática&availableFrom=2026-12-01T10:00:00Z&availableUntil=2026-12-01T11:00:00Z` — consulta autenticada.
- `/api/teachers` — CRUD; escrita administrativa.
- `/api/students` — CRUD administrativo.
- `GET /api/admin/users` e `POST /api/admin/users` — consulta e criação administrativa de contas.

O cadastro público de solicitante é feito em `GET /register` e `POST /register`; depois, o login ocorre em `GET /login` e `POST /login`.

## Erros

Respostas de erro da API usam `application/problem+json`, com `status` e `detail`.

| HTTP | Quando ocorre |
|---|---|
| `400 Bad Request` | Campos inválidos, intervalo de reserva inválido ou tentativa incompatível com o estado do recurso |
| `401 Unauthorized` | Sessão ausente/expirada |
| `403 Forbidden` | Perfil sem autorização ou tentativa de alterar reserva de outra pessoa |
| `404 Not Found` | Recurso, reserva ou retirada inexistente |
| `409 Conflict` | Colisão de horário ou retirada/devolução já registrada |

Na interface web, erros de validação e de permissão são apresentados em uma página responsiva com uma explicação em português e o código HTTP.
