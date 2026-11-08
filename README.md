# Gestão de Recursos Educacionais

Aplicação Java 17 / Spring Boot 3.2 para autenticação por perfil, catálogo de salas, professores e materiais, pesquisa de disponibilidade e reservas com aprovação de recursos restritos.

## Requisitos

- Java 17 ou superior
- Maven 3.6 ou superior (ou use o wrapper `mvnw.cmd`)
- MongoDB acessível pela aplicação. Reservas simultâneas usam transações e índice único; o MongoDB precisa operar como replica set ou cluster compatível com transações.

## Configuração

A credencial de administrador deve ser definida por variáveis de ambiente. Não publique senhas nem URI do banco em arquivos, commits ou logs. A credencial do banco que foi compartilhada anteriormente deve ser rotacionada.

### MongoDB local com Docker

Docker Compose inicia um MongoDB 7 local em `127.0.0.1:27017`, configura um replica set de nó único para suportar transações e persiste os dados em um volume. O perfil `local` já é o padrão; o MongoDB precisa estar saudável antes de iniciar a aplicação.

No PowerShell:

```powershell
docker compose up -d
docker compose ps
.\mvnw.cmd spring-boot:run
```

Aguarde o serviço `mongodb` ficar `healthy` em `docker compose ps`. Para parar sem apagar dados, use `docker compose down`; para apagar também o banco e os dados locais, use `docker compose down -v`. O Mongo local não usa autenticação e deve ser usado apenas para desenvolvimento.

Se preferir usar o banco embarcado sem Docker, ative explicitamente `embedded` (`$env:SPRING_PROFILES_ACTIVE = 'embedded'`). Esse modo baixa os binários do MongoDB na primeira inicialização e persiste os dados em `data/mongodb`.

### MongoDB hospedado

Para usar um MongoDB hospedado, não ative o perfil `local`. Defina `MONGODB_URI` com a URI completa e válida fornecida pelo administrador do banco e, se necessário, `MONGODB_DATABASE` (padrão `qualidade_software`). Verifique também se a rede permite saída para o host/porta e se o provedor permite conexões do seu IP.

```powershell
$env:MONGODB_URI = 'mongodb://<usuario>:<senha>@<host>:<porta>/?authSource=admin'
$env:MONGODB_DATABASE = 'qualidade_software'
$env:ADMIN_EMAIL = 'admin@exemplo.com'
$env:ADMIN_PASSWORD = '<senha-forte-com-pelo-menos-8-caracteres>'
.\mvnw.cmd spring-boot:run
```

`ADMIN_EMAIL` e `ADMIN_PASSWORD` são usados para criar a conta inicial de administrador se ela ainda não existir. Defina ambos; não há senha padrão.

Na primeira inicialização, o app cria as coleções `students`, `teachers`, `user_accounts`, `educational_resources`, `reservations` e `reservation_slots`, além dos índices de e-mail e bloqueio de horários. O MongoDB não impõe esquema rígido; os campos dos documentos são definidos pelas entidades Java em `src/main/java/com/example/educationalqualityproject/entity`.

## Perfis e acesso

- `SOLICITANTE`: criado pelo formulário público de cadastro; pode consultar recursos e criar, alterar ou cancelar as próprias reservas.
- `PROFESSOR`: conta criada por administrador; também pode reservar recursos.
- `ADMIN`: conta inicial por variáveis de ambiente e contas adicionais criadas por administrador; gerencia catálogo, contas e aprovações.

Apenas administradores criam contas com perfis privilegiados. Reservas com qualquer recurso marcado como restrito ficam pendentes até aprovação administrativa. Reservas pendentes ocupam o horário, impedindo pedidos concorrentes.

## Executar e testar

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Os testes usam o perfil `test` e não se conectam a nenhum banco. O perfil padrão é `local` e usa o MongoDB do Docker Compose; para o ambiente hospedado, defina `SPRING_PROFILES_ACTIVE=prod` e configure `MONGODB_URI`.

## Interface web

- `/` — página inicial
- `/login` e `/register` — autenticação e cadastro de solicitante
- `/resources` — busca por tipo (`SALA` ou `MATERIAL`), capacidade mínima, localização, competência e disponibilidade
- `/teachers` — consulta de professores por competência e disponibilidade; usuários autenticados podem cadastrar um professor e administradores também podem editar ou excluir registros
- `/reservations` — criação e consulta das próprias reservas
- `/admin/reservations` — aprovação ou rejeição de recursos restritos (administrador)

## API

Todas as rotas `/api/**` exigem autenticação. As rotas de escrita do catálogo e `/api/admin/**` exigem perfil administrador. A API usa a sessão de login e proteção CSRF do Spring Security.

### Recursos e professores

- `GET /api/resources?type=SALA&capacity=20&location=bloco&competence=multimidia&availableFrom=2026-11-09T10:00:00Z&availableUntil=2026-11-09T11:00:00Z` — pesquisa combinável
- `GET /api/resources` e `GET /api/resources/{id}` — consulta do catálogo
- `POST /api/resources`, `PUT /api/resources/{id}`, `DELETE /api/resources/{id}` — CRUD do catálogo (escrita administrativa)
- `GET /api/teachers?competence=matemática&availableFrom=2026-11-09T10:00:00Z&availableUntil=2026-11-09T11:00:00Z` — pesquisa de professores por competência e disponibilidade"
- `/api/teachers` e `/api/students` — operações CRUD existentes, disponíveis para administrador
- `GET /api/admin/users` e `POST /api/admin/users` — consulta e provisionamento de contas/perfis (administrador)

Exemplo de recurso:

```json
{
  "name": "Sala multimídia 1",
  "type": "SALA",
  "location": "Bloco A",
  "capacity": 30,
  "competencies": ["multimídia"],
  "restricted": true
}
```

### Reservas

- `GET /api/reservations` — reservas próprias; administrador consulta todas
- `POST /api/reservations` — criar reserva
- `PUT /api/reservations/{id}` — alterar (somente solicitante ou administrador)
- `DELETE /api/reservations/{id}` — cancelar (somente solicitante ou administrador)
- `POST /api/reservations/{id}/approve` e `/reject` — decisão administrativa; nota opcional por parâmetro

Exemplo de reserva (datas em UTC, alinhadas ao minuto):

```json
{
  "resourceIds": ["<id-da-sala>", "<id-do-material>"],
  "teacherId": "<id-do-professor>",
  "startAt": "2026-11-09T10:00:00Z",
  "endAt": "2026-11-09T11:00:00Z"
}
```

Uma tentativa simultânea para horário ocupado retorna HTTP `409 Conflict`. Reservas devem durar até 24 horas e podem ser agendadas em intervalos de minutos inteiros.
